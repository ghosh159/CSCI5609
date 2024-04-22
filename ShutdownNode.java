import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public class ShutdownNode {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ShutdownNode <chord_node_url>");
            System.exit(1);
        }

        String chordNodeUrl = args[0];

        try {
            Node node = (Node) Naming.lookup(chordNodeUrl);

            // Get the count of words stored in the node's dictionary
            ConcurrentHashMap<String, String> dictionary = node.getDictionary();
            int wordCount = dictionary.size();

            System.out.println("Node URL: " + chordNodeUrl);
            System.out.println("Word count: " + wordCount);

            // Shutdown the node
            node.shutdown();
            System.out.println("Node shutdown complete");
        } catch (Exception e) {
            System.err.println("Error shutting down node: " + e.getMessage());
            e.printStackTrace();
        }
    }
}