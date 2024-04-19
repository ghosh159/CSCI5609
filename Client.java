import java.rmi.Naming;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Client <ChordNodeURL>");
            return;
        }

        String chordNodeURL = args[0];

        try {
            Node node = (Node) Naming.lookup(chordNodeURL);
            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.println("Enter 1 to lookup, 2 to insert a new (key-value) item, or 3 to exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        System.out.print("Enter a word: ");
                        String lookupWord = scanner.nextLine();
                        int lookupKey = FNV1aHash.hash32(lookupWord);
                        String lookupNodeURL = node.findSuccessor(lookupKey, false);
                        Node lookupNode = (Node) Naming.lookup(lookupNodeURL);
                        String definition = lookupNode.lookup(lookupWord);
                        System.out.println("Result: " + definition);
                        break;
                    case 2:
                        System.out.print("Enter a word: ");
                        String insertWord = scanner.nextLine();
                        System.out.print("Enter the meaning: ");
                        String insertDefinition = scanner.nextLine();
                        int insertKey = FNV1aHash.hash32(insertWord);
                        String insertNodeURL = node.findSuccessor(insertKey, false);
                        Node insertNode = (Node) Naming.lookup(insertNodeURL);
                        insertNode.insert(insertWord, insertDefinition);
                        System.out.println("Result status: Inserted word " + insertWord + " (key = " + insertKey + ") at node " + insertNodeURL);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } while (choice != 3);
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}