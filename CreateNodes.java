import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class CreateNodes {

    public static void main(String[] args) {
        int num_nodes = 3;
        String hostname = "localhost";
        int rmiport = 1099;
        try{
            for(int i = 0; i < num_nodes; i++) {
                String url = "//" + hostname + ":" + rmiport + "/node_" + i;
                Node node = (Node) Naming.lookup(url);
                node.join();
            }
            //Print Finger Tables for all Nodes
            for(int i = 0; i < num_nodes; i++) {
                String url = "//" + hostname + ":" + rmiport + "/node_" + i;
                Node node = (Node) Naming.lookup(url);
                System.out.println(node.printFingerTable());
            }
            //Shutdown
            for(int i = 0; i < num_nodes; i++) {
                String url = "//" + hostname + ":" + rmiport + "/node_" + i;
                Node node = (Node) Naming.lookup(url);
                node.shutdown();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
