import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class ChordNode extends UnicastRemoteObject implements Node {
    private String url;
    private int id;
    private String predecessor;
    private String[] fingerTable;
    private ConcurrentHashMap<String, String> dictionary;

    public ChordNode(String url) throws RemoteException {
        this.url = url;
        this.id = hash32(url);
        this.predecessor = null;
        this.fingerTable = new String[31];
        this.dictionary = new ConcurrentHashMap<>();
    }

    // Implement the methods from the Node interface
    // ...

    private int hash32(String key) {
        // Implement the FNV-1a hash function
        // ...
    }

    private int modulo31Add(int n, int m) {
        int tmp = n + m;
        return (tmp & Integer.MAX_VALUE);
    }
}