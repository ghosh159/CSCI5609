import java.rmi.Naming;
import java.io.BufferedReader;
import java.io.FileReader;

public class DictionaryLoader {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java DictionaryLoader <ChordNodeURL> <dictionaryFile>");
            return;
        }

        String chordNodeURL = args[0];
        String dictionaryFile = args[1];

        try {
            Node node = (Node) Naming.lookup(chordNodeURL);
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String definition = parts[1].trim();
                    int key = hash32(word);
                    String nodeURL = node.findSuccessor(key, false);
                    Node destinationNode = (Node) Naming.lookup(nodeURL);
                    destinationNode.insert(word, definition);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int hash32(String key) {
        // Implement the FNV-1a hash function
        // ...
    }
}