import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by rishimittal on 29/3/14.
 */
public class FileServer extends java.rmi.server.UnicastRemoteObject implements ReadWriteInterface {


    private static Registry registry;
    private static String ipAddress;
    private static int portNumber;

    protected FileServer() throws RemoteException {
        super();
    }

    public static String generateName(){

        long secs = System.currentTimeMillis() / 1001;

        return "Server_" + String.valueOf(secs);
    }

    public RegistryInterface lookUpRegistry(){
        RegistryInterface registryServer = null;
        // get the registry
        try {
            registry= LocateRegistry.getRegistry(
                    ipAddress,
                    (new Integer(portNumber)).intValue()
            );
            registryServer = (RegistryInterface) registry.lookup("registryServer");


        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    return registryServer;
    }

    public int FileWrite64K(String filename, long offset, byte[] data) throws IOException, RemoteException {

        String newFolderPath = "./" + filename;
        File fd = new File(newFolderPath);
        //Make folder if not exists.
        if(!fd.exists()){
            fd.mkdir();
        }
        //Make chunk file in folder
        FileOutputStream fos = new FileOutputStream(new File(newFolderPath+"/chunk"+offset));
        //System.out.println("Written : chunk" + offset);
        fos.write(data);
        fos.flush();
        fos.close();
        return 0;
    }

    @Override
    public long NumFileChunks(String filename) throws IOException, RemoteException {
        File fg = new File("./" + filename);
        long chunkCount = fg.listFiles().length;
        return chunkCount;
    }

    @Override
    public byte[] FileRead64K(String filename, long offset) throws IOException, RemoteException {

        String filePath = "./" + filename + "/chunk" + offset;
        // Reads one chunk , sets the data in byte array returns
        // to the client.
        FileInputStream fis = new FileInputStream(filePath);
        byte [] fbytes = new byte[fis.available()];
        fis.read(fbytes);
        fis.close();
        return fbytes;
    }

    public static void main(String arr[]) throws RemoteException {

        ipAddress = arr[0];
        portNumber = Integer.parseInt(arr[1]);
        String name = generateName();
        System.out.println("File Server : " + name);

        FileServer fileServer = new FileServer();

        RegistryInterface registryInterface = fileServer.lookUpRegistry();

        registryInterface.registerServer(name);

        //registry.bind(na);
        ReadWriteInterface readWriteInterface = new FileServer();
        try {
            registry.bind(name, readWriteInterface);
        } catch (AlreadyBoundException e) {

        }

    }

}
