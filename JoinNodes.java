import java.rmi.Naming;

public class JoinNodes {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JoinNodes <number_of_nodes>");
            return;
        }

        int num_nodes = Integer.parseInt(args[0]);
        String hostname = "localhost";
        int rmiport = 1099;
        Node[] nodes = new Node[num_nodes];
        Thread[] threads = new Thread[num_nodes];

        try {
            // Create and start threads for joining the nodes
            for (int i = 0; i < num_nodes; i++) {
                final int nodeIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        String url = "//" + hostname + ":" + rmiport + "/node_" + nodeIndex;
                        nodes[nodeIndex] = (Node) Naming.lookup(url);
                        nodes[nodeIndex].join();
                        System.out.println("Node " + nodeIndex + " joined the Chord ring");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            //Start all threads
            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }

            // Wait for all threads to finish
            for (int i = 0; i < num_nodes; i++) {
                threads[i].join();
            }

            // Print Finger Tables for all Nodes
            for (int i = 0; i < num_nodes; i++) {
                System.out.println(nodes[i].printFingerTable());
            }

            // Print Dictionary contents for all Nodes
            for (int i = 0; i < num_nodes; i++) {
                System.out.println(nodes[i].printDictionary());
            }

            // Shutdown
            // for (int i = 0; i < num_nodes; i++) {
            //     nodes[i].shutdown();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}