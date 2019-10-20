package CreateWhiteBoard;
import whiteboard.DShape;
import whiteboard.DShapeModel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface IjoinerAddresses extends Remote {

    public Hashtable<String, Integer> getAddressed() throws RemoteException;

    public Hashtable<Integer, DShapeModel> get_whiteBoard_Info() throws RemoteException;
}