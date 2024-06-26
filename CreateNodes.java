import java.rmi.Naming;

public class CreateNodes {

    public static void main(String[] args) {
        int num_nodes = 8;
        String hostname = "localhost";
        int rmiport = 1099;
        Node[] nodes = new Node[8];
        try{
            for(int i = 0; i < num_nodes; i++) {
                String url = "//" + hostname + ":" + rmiport + "/node_" + i;
                nodes[i] = (Node) Naming.lookup(url);
                nodes[i].join();
            }
            //Print Finger Tables for all Nodes
            for(int i = 0; i < num_nodes; i++) {
                System.out.println(nodes[i].printFingerTable());
            }
            //Shutdown
            for(int i = 0; i < num_nodes; i++) {
                nodes[i].shutdown();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
