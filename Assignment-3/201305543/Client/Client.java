import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by rishimittal on 29/3/14.
 */
public class Client implements Runnable{

    private static String ipAddress;
    private static int portNumber;
    List<String> listOfServers;
    Registry registry;
    private static String filePath = "";
    private static int OFFSET = 65536;
    private static int listSize;
    private static int currentFileOffset = 0;
    private static int currentChunkOffset = 0;
    private static String fname = null;
    private static RandomAccessFile raf;
    private static long chunks;

    public  void lookUpRegistry(){
        RegistryInterface registryServer;
        // get the registry
        try {
            registry= LocateRegistry.getRegistry(
                    ipAddress,
                    (new Integer(portNumber)).intValue()
            );
            registryServer = (RegistryInterface) registry.lookup("registryServer");

            listOfServers = registryServer.geFileServers();
            System.out.println(listOfServers);
            listSize = listOfServers.size();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public String getCurrentMaster(){

        System.out.println("Master File Server : " + listOfServers.get(0));

        return listOfServers.get(0);

    }

    public ReadWriteInterface lookUpFileServerForMaster(){

        ReadWriteInterface readWriteInterface = null;

        try {
            registry= LocateRegistry.getRegistry(
                    ipAddress,
                    (new Integer(portNumber)).intValue()
            );

            readWriteInterface = (ReadWriteInterface) registry.lookup(getCurrentMaster());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return readWriteInterface;
    }

    private int sendFilToServerInChunks(ReadWriteInterface rmiServer, String fName){

        File fd = new File(filePath);
        //System.out.println(filePath);
        long numOfChunks = fd.length() / OFFSET;
        //System.out.println(numOfChunks);
        if(numOfChunks == 0 ) {
            System.out.println("File less than 64kb");
            return 0;
        }
        //System.out.println(numOfChunks);
        int byteCount = 0;
        try {
            FileInputStream fis = new FileInputStream(fd);
            byte []sendByte = new byte[OFFSET];
            int i = 0;
            int j = 0;
            while(true){

                if(byteCount == OFFSET){
                    byteCount = 0;
                    //function call

                    i = 0;

                    rmiServer.FileWrite64K(fName, j, sendByte);
                    j++;
                }
                byte ch = (byte)fis.read();
                sendByte[i++] = ch;
                byteCount++;
                if(ch == -1)  break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void main(String arr[]){
        //Input Parameters
        String fileName = arr[0];
        ipAddress = arr[1];
        portNumber = Integer.parseInt(arr[2]);


        File fe = new File(fileName);
        filePath = fe.getAbsolutePath();
        fname = fe.getName();

        try {
            Client c = new Client();
            //Look up for RMI registry and call the methods of RegistryInterface
            c.lookUpRegistry();

            //Look up for File Server , of Master.
            ReadWriteInterface readWriteInterface = c.lookUpFileServerForMaster();

            //File Written by the Master File Server
            c.sendFilToServerInChunks(readWriteInterface, fname);

            //Number of Chunks read .
            chunks = readWriteInterface.NumFileChunks(fname);

            System.out.println("Number of Chunks : " + chunks);


            File fd = new File("./output");
            if(!fd.exists()){
                fd.mkdir();
            }



            long noOfThreads = 0;
            if(chunks < listSize ){
                //If chunks are smaller than the listSize/Total FileServers
                noOfThreads  = chunks;
                for(int j = 0 ; j < noOfThreads ;j++){

                    Thread mt = new Thread(c);
                    mt.start();
                    mt.join();
                }

            }else{
                //If FileServers are less than chunks
                noOfThreads = listSize;

            while(currentChunkOffset < chunks)
                for(int j = 0 ; j < noOfThreads ;j++){

                    Thread mt = new Thread(c);
                    //System.out.println(mt.getName());
                    mt.start();
                    mt.join();
                }
            }


         } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void run() {

            if(currentChunkOffset >= chunks)   return;

        try {

                raf = new RandomAccessFile("./output/"+ fname ,"rwd");

                ReadWriteInterface rwi = (ReadWriteInterface) registry.lookup(listOfServers.get(currentFileOffset % listSize));

                //System.out.println("rir"+currentFileOffset);

                byte[] r = rwi.FileRead64K(fname, currentChunkOffset);
                //System.out.println(new String(r));
                raf.seek(currentChunkOffset * 65536);

                currentChunkOffset++;
                currentFileOffset++;
                raf.write(r);
                raf.close();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
