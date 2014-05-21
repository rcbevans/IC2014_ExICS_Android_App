package rce10.ic.ac.uk.exics.Utilities;

/**
 * Created by Rich on 21/05/2014.
 */
public class wsCommunicationManager {
    private wsCommunicationManager instance = null;

    private wsCommunicationManager() {
    }

    public wsCommunicationManager getInstance() {
        if (instance == null) {
            instance = new wsCommunicationManager();
        }
        return instance;
    }

    ;

    public void openConnection(String hostname, int portNumber) {

    }

}
