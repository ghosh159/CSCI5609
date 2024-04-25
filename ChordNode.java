import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class ChordNode implements Node {
    private String url;
    private int id;
    public String predecessor;
    public String successor;
    private String[] fingerTable;
    private int m;
    private ConcurrentHashMap<String, String> dictionary;
    private boolean isLocked;
    private int rmiport;
    private String hostname;
    private int nodeId;

    public ChordNode(String hostname, int rmiport, int nodeId) throws RemoteException {
        super();

        this.hostname = hostname;
        this.nodeId = nodeId;
        this.rmiport = rmiport;
        this.url = "//" + this.hostname + ":" + this.rmiport + "/node_" + this.nodeId;
        this.id = FNV1aHash.hash32(url);
        this.predecessor = null;
        this.successor = null;
        this.m = 31;
        this.fingerTable = new String[m];
        this.dictionary = new ConcurrentHashMap<>();
        this.isLocked = false;

        logMessage("Node created with URL: " + this.url);
    }

    private void logMessage(String message) {
        String logFileName = "node_" + this.nodeId + ".log";
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);
            writer.println(timestamp + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean acquireJoinLock(String nodeURL) throws RemoteException {
        if (this.nodeId == 0) {
            if (!this.isLocked) {
                this.isLocked = true;
                logMessage("Join lock acquired by " + nodeURL);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean releaseJoinLock(String nodeURL) throws RemoteException {
        if (this.nodeId == 0) {
            if (this.isLocked) {
                this.isLocked = false;
                logMessage("Join lock released by " + nodeURL);
                return true;
            }
        }
        return false;
    }

    public void join() {
        try {
            if (this.nodeId != 0) {
                String nodeZeroURL = "//" + this.hostname + ":" + this.rmiport + "/node_0";
                Node nodeZero = (Node) Naming.lookup(nodeZeroURL);
                while (!nodeZero.acquireJoinLock(this.url)) {
                    Thread.sleep(100); // Wait for 1 second before trying again
                }
            }

            if (this.nodeId == 0) {
                for (int i = 0; i < m; i++) {
                    fingerTable[i] = this.url;
                }
                this.predecessor = this.url;
            } else {
                init_finger_table("//" + this.hostname + ":" + this.rmiport + "/node_0");
                update_others();
            }

            logMessage("Joined the Chord Ring");

            if (this.nodeId != 0) {
                String nodeZeroURL = "//" + this.hostname + ":" + this.rmiport + "/node_0";
                Node nodeZero = (Node) Naming.lookup(nodeZeroURL);
                nodeZero.releaseJoinLock(this.url);
            }
        } catch (InterruptedException | RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    private void init_finger_table(String n_prime_url) {
        try {
            Node node_prime = (Node) Naming.lookup(n_prime_url);
            int n = this.id;
            this.fingerTable[0] = node_prime.findSuccessor(FNV1aHash.modulo31Add(n, (int) Math.pow(2, 0)), false);
            Node successor_node = (Node) Naming.lookup(this.successor());
            this.predecessor = successor_node.predecessor();
            successor_node.setPredecessor(this.url);
            for (int i = 0; i < m - 1; i++) {
                if (left_half_open(this.id, FNV1aHash.modulo31Add(n, (int) Math.pow(2, i + 1)), FNV1aHash.hash32(this.fingerTable[i]))) {
                    this.fingerTable[i + 1] = this.fingerTable[i];
                } else {
                    this.fingerTable[i + 1] = node_prime.findSuccessor(FNV1aHash.modulo31Add(n, (int) Math.pow(2, i + 1)), false);
                }
            }

            logMessage("Initialized finger table");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void update_others() {
        try {
            for (int i = 0; i < m; i++) {
                String p = findPredecessor(FNV1aHash.modulo31Add(FNV1aHash.modulo31Subtract(this.id, (int) Math.pow(2, i)), 1), false);
                Node p_node = (Node) Naming.lookup(p);
                p_node.update_finger_table(this.url, i);
            }

            logMessage("Updated finger tables of other nodes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean closed_left(int x, int y, int z) {
        if (z < x) {
            return y >= x || y < z;
        } else {
            return y >= x && y < z;
        }
    }

    public void update_finger_table(String s, int i) {
        try {
            if (closed_left(FNV1aHash.modulo31Add(this.id, (int) Math.pow(2, i)), FNV1aHash.hash32(s), FNV1aHash.hash32(this.fingerTable[i]))) {
                this.fingerTable[i] = s;
                Node p_node = (Node) Naming.lookup(this.predecessor);
                p_node.update_finger_table(this.url, i);
            }

            logMessage("Updated finger table entry " + i + " with " + s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String findSuccessor(int key, boolean traceFlag) throws RemoteException {
        try {
            Node n_prime_node = (Node) Naming.lookup(findPredecessor(key, traceFlag));
            String successorURL = n_prime_node.successor();
            logMessage("Found successor " + successorURL + " for key " + key);
            return successorURL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean left_half_open(int x, int y, int z) {
        if (z < x) {
            return y > x || y <= z;
        } else {
            return y > x && y <= z;
        }
    }

    @Override
    public String findPredecessor(int key, boolean traceFlag) throws RemoteException {
        try {
            String n_prime_url = this.url;
            Node n_prime_node = (Node) Naming.lookup(n_prime_url);

            while (!(left_half_open(FNV1aHash.hash32(n_prime_url), key, FNV1aHash.hash32(n_prime_node.successor())))) {
                if (n_prime_url.equals(n_prime_node.successor())) {
                    return n_prime_url;
                }
                if (key == this.id) {
                    return this.predecessor;
                }
                n_prime_url = n_prime_node.closestPrecedingFinger(key);
                n_prime_node = (Node) Naming.lookup(n_prime_url);
            }

            logMessage("Found predecessor " + n_prime_url + " for key " + key);
            return n_prime_url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPredecessor(String predecessor) {
        this.predecessor = predecessor;
        logMessage("Set predecessor to " + predecessor);
    }

    private boolean open_range(int x, int y, int z) {
        if (z < x) {
            return y > x || y < z;
        } else {
            return y > x && y < z;
        }
    }

    @Override
    public String closestPrecedingFinger(int key) throws RemoteException {
        for (int i = m - 1; i >= 0; i--) {
            if (open_range(this.id, FNV1aHash.hash32(this.fingerTable[i]), key)) {
                logMessage("Found closest preceding finger " + this.fingerTable[i] + " for key " + key);
                return this.fingerTable[i];
            }
        }
        logMessage("Closest preceding finger not found for key " + key + ", returning self URL " + this.url);
        return this.url;
    }

    @Override
    public String successor() throws RemoteException {
        logMessage("Returning successor " + this.fingerTable[0]);
        return this.fingerTable[0];
    }

    @Override
    public String predecessor() throws RemoteException {
        logMessage("Returning predecessor " + this.predecessor);
        return this.predecessor;
    }

    @Override
    public boolean insert(String word, String definition) throws RemoteException {
        try {
            this.dictionary.put(word, definition);
            logMessage("Inserted word: " + word + ", definition: " + definition);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String lookup(String word) throws RemoteException {
        String definition = this.dictionary.get(word);
        if (definition != null) {
            logMessage("Lookup successful for word: " + word);
        } else {
            logMessage("Lookup failed for word: " + word);
        }
        return definition;
    }

    @Override
    public String printFingerTable() throws RemoteException {
        StringBuilder output = new StringBuilder("Finger Table for Node " + this.nodeId + "\n");
        output.append("Key: " + this.id + "\n");
        output.append("Predecessor: " + this.predecessor + " " + this.predecessor() + "\n");
        for (int i = 0; i < m; i++) {
            output.append(i).append(" -> ").append(this.fingerTable[i]).append("\n");
        }
        output.append("END OF TABLE\n");

        logMessage("Printed finger table");

        return output.toString();
    }

    @Override
    public String printDictionary() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("Dictionary contents for Node ").append(this.nodeId).append(":\n");
        for (String word : this.dictionary.keySet()) {
            sb.append(word).append(": ").append(this.dictionary.get(word)).append("\n");
        }

        logMessage("Printed dictionary contents");

        return sb.toString();
    }

    @Override
    public void shutdown() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(rmiport);
            registry.unbind("node_" + this.nodeId);
            UnicastRemoteObject.unexportObject(this, true);

            logMessage("Node shutdown completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public ConcurrentHashMap<String, String> getDictionary() throws RemoteException {
        int wordCount = this.dictionary.size();
        logMessage("Dictionary word count: " + wordCount);
        return this.dictionary;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ChordNode <node-ID> <rmiport>");
            System.exit(1);
        }

        int nodeID = Integer.parseInt(args[0]);
        int rmiport = 1099;

        if (args.length == 2) {
            rmiport = Integer.parseInt(args[1]);
        }

        try {
            ChordNode node;
            Node chordNodeStub;

            String url = "//localhost:" + rmiport + "/node_" + nodeID;

            System.setProperty("java.rmi.server.hostname", "localhost");
            node = new ChordNode("localhost", rmiport, nodeID);

            chordNodeStub = (Node) UnicastRemoteObject.exportObject(node, 0);

            Naming.bind(url, chordNodeStub);

            System.out.println("Started Node_" + nodeID + " on host: " + InetAddress.getLocalHost().getCanonicalHostName());

            int seconds = 3600;
            System.out.println("Server will shutdown in " + seconds + " seconds\n");
            Thread.sleep(seconds * 1000);
            chordNodeStub.shutdown();
            System.out.println("Completed shutting down the RMI server\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}