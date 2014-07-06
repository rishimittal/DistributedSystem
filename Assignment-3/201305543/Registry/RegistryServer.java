
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishimittal on 29/3/14.
 */
public class RegistryServer extends java.rmi.server.UnicastRemoteObject implements RegistryInterface{

    //The first entry is the master file Server.
    public   List<String> fileServerList;
    public   Registry registry;
    private  static int thisPortA;
    private  static String thisAddress;


    public RegistryServer() throws RemoteException{
        try{
            // Create the registry.
            registry = LocateRegistry.createRegistry(thisPortA);
            registry.bind("registryServer", this);
            fileServerList = new ArrayList<String>();
        }catch(RemoteException e){

            throw e;
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    public boolean registerServer(String name) throws RemoteException{

        boolean isMaster = false;
        //if it is not replica then it is master
        //System.out.println(name);
        fileServerList.add(name);
        if(fileServerList.size() == 1){
            isMaster = true;
        }

        return  isMaster;
    }

    public List<String> geFileServers() throws RemoteException{

        return fileServerList;

    }

    public static void main(String arr[]) throws RemoteException {

            thisAddress = arr[0];
            thisPortA = Integer.parseInt(arr[1]);
            System.out.println("Registry Server Running....!");
            new RegistryServer();

    }

}
