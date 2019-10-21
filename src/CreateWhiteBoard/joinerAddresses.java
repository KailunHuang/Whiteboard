package CreateWhiteBoard;

import whiteboard.Canvas;
import whiteboard.DShapeModel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

public class joinerAddresses extends UnicastRemoteObject implements IjoinerAddresses {

    protected joinerAddresses() throws RemoteException {
    }

    @Override
    public Hashtable<String, Integer> getAddressed() {
        return Manager.postHashtable();
    }

    public ArrayList<DShapeModel> get_whiteBoard_Info() {
        return Canvas.post_whiteboard_info();
    }
}