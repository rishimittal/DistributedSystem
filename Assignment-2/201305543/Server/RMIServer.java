/**
 * Created by rishimittal on 13/2/14.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.*;

import java.rmi.registry.*;

import java.rmi.server.*;

import java.net.*;


public class RMIServer extends java.rmi.server.UnicastRemoteObject implements ReadWriteInterface{

    int      thisPort;
    String   thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.
    //private static String serverPort = "";

    //public void receiveMessage(String x) throws RemoteException {
    //    System.out.println(x);
    //}

    public RMIServer(String inputServerPort) throws RemoteException {

        try{
            // get the address of this host.
            thisAddress= (InetAddress.getLocalHost()).toString();

        }catch(Exception e){
            throw new RemoteException("can't get inet address.");
        }

        thisPort=Integer.parseInt(inputServerPort);
        //System.out.println("this address="+thisAddress+",port="+thisPort);

        try{
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry( thisPort );
            registry.rebind("rmiServer", this);

        }catch(RemoteException e){

            throw e;
        }
    }

    @Override
    public int FileWrite64K(String filename, long offset, byte[] data) throws IOException, RemoteException {

        String newFolderPath = "./" + filename;
        File fd = new File(newFolderPath);
        //Make folder if not exists.
        if(!fd.exists()){
            fd.mkdir();
        }
        //Make chunk file in folder
        FileOutputStream  fos = new FileOutputStream(new File(newFolderPath+"/chunk"+offset));
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


    public static void main(String args[])  {
        //serverFolderPath = "/home/rishimittal/Documents/SEM2/DS/Assignment-2/ServerFiles/";
        String serverPort = args[0];
        try{
            RMIServer s=new RMIServer(serverPort);
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}

