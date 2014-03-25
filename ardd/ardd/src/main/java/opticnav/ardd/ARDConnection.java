package opticnav.ardd;

/**
 * An ARD Connection represents the state of a connected device.
 * It stores information about the instance it has joined (if any), and state such as the device location.
 * 
 * This class is not thread-safe!
 */
public class ARDConnection implements AutoCloseable {
    private final int ardID;
    private final ARDConnectedList connectedList;
    
    public ARDConnection(int ardID, ARDConnectedList connectedList) {
        this.ardID = ardID;
        this.connectedList = connectedList;
    }

    public void close() {
        this.connectedList.removeConnectedByID(ardID);
    }
}