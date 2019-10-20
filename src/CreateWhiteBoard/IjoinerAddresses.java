package CreateWhiteBoard;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface IjoinerAddresses extends Remote {

    public Hashtable<String, Integer> getAddressed() throws RemoteException;
}