package CreateWhiteBoard;
import whiteboard.Whiteboard;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

public class modelTable extends UnicastRemoteObject implements ImodelTable {

    protected modelTable() throws RemoteException {
    }

    @Override
    public Hashtable<Integer, Manager.WhiteBoardInfo> getAddressed(){
        return Manager.postModelTable();
    }

}