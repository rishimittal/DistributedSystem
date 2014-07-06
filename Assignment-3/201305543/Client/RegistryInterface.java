import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by rishimittal on 29/3/14.
 */
public interface RegistryInterface extends Remote {

    public boolean registerServer(String name) throws RemoteException;
    public List<String> geFileServers() throws RemoteException;

}
