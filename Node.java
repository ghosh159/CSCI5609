import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    boolean acquireJoinLock(String url) throws RemoteException;
    void releaseJoinLock(String url) throws RemoteException;
    String findSuccessor(int key, boolean traceFlag) throws RemoteException;
    String findPredecessor(int key, boolean traceFlag) throws RemoteException;
    void insert(String word, String definition) throws RemoteException;
    String lookup(String word) throws RemoteException;
    void printFingerTable() throws RemoteException;
    void printDictionary() throws RemoteException;
}