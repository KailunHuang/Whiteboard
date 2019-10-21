package CreateWhiteBoard;
import whiteboard.DShape;
import whiteboard.DShapeModel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

public interface IjoinerAddresses extends Remote {

    public Hashtable<String, Integer> getAddressed() throws RemoteException;

    public ArrayList<DShapeModel> get_whiteBoard_Info() throws RemoteException;
}