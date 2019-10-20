package CreateWhiteBoard;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface ImodelTable extends Remote {

    public Hashtable<Integer, Manager.WhiteBoardInfo> getAddressed() throws RemoteException;
}