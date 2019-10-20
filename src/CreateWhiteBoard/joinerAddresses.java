package CreateWhiteBoard;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class joinerAddresses extends UnicastRemoteObject implements IjoinerAddresses {

    protected joinerAddresses() throws RemoteException {
}

    @Override
    public Hashtable<String, Integer> getAddressed(){
        return Manager.postHashtable();
    }

}