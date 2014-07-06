import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishimittal on 29/3/14.
 */
public class Client {

    private static String ipAddress;
    private static int portNumber;
    private static List<String> listOfServers;
    private static Registry registry;
    private static String filePath = "";
    private static int OFFSET = 65536;
    private static int listSize;
    private static int currentFileOffset = 0;
    private static int currentChunkOffset = 0;
    private static String fname = null;
    private static long chunks;
    public static int [] chunk_stat= null;
    public static List<String> failedThreads;


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

            chunk_stat = new int[(int) chunks];
            failedThreads = new ArrayList<String>();

            File fd = new File("./output");
            if(!fd.exists()){
                fd.mkdir();
            }

            long noOfThreads = 0;
            Thread [] threads = null;
            if(chunks < listSize ){
                //If chunks are smaller than the listSize/Total FileServers
                noOfThreads  = chunks;
                threads = new Thread[(int)noOfThreads];

                for(int j = 0 ; j < noOfThreads ;j++){
                    ReadingThread th = new ReadingThread(j,registry,listOfServers,fname, listSize,chunks);
                    threads[j] = th.beginThread();
                }

            }else{
                //If FileServers are less than chunks
                noOfThreads = listSize;
                threads = new Thread[(int)noOfThreads];

                for(int i = 0 ; i < noOfThreads ; i++ ){

                    ReadingThread rth = new ReadingThread(i,registry , listOfServers,fname, listSize,chunks);
                    threads[i] = rth.beginThread();
                }
            }

            for(int u = 0 ; u < threads.length; u++ ){
                try {
                    threads[u].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//          System.out.println("Must print at last");
            System.out.println("Failed Threads");
             for(int h = 0 ;h < failedThreads.size() ; h++ ){
                 System.out.println(failedThreads.get(h));

             }

            List<String> activeFileServers = new ArrayList<String>();
            activeFileServers.addAll(listOfServers);
            activeFileServers.removeAll(failedThreads);
            System.out.println("Active Threads");
            for(int h = 0 ;h < activeFileServers.size() ; h++ ){
                System.out.println(activeFileServers.get(h));

            }

            if(chunk_stat.length != 0 ){

                RandomAccessFile raf = new RandomAccessFile("./output/"+ fname ,"rwd");
                ReadWriteInterface rwi = null;
                try {
                    rwi = (ReadWriteInterface) registry.lookup(activeFileServers.get(0));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                //System.out.println("Writing Failed Chunks");
                for(int p = 0 ; p < chunk_stat.length ; p++ ){
                    if(chunk_stat[p] == 0) {
                        //System.out.print(p + ",");
                        byte[] rq = rwi.FileRead64K(fname, p);
                        raf.seek(p * 65536);
                        raf.write(rq);
                    }
                }
                System.out.println();
            }

         } catch (RemoteException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
