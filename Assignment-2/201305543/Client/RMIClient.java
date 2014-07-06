/**
 * Created by rishimittal on 13/2/14.
 */
import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class RMIClient{

    private static String filePath = "";
    private static int OFFSET = 65536;
    //private static String NEW_FILE_NAME = "/newFile";

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
                    j++;
                    i = 0;

                    rmiServer.FileWrite64K(fName, j, sendByte);
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

    private void receiveAndIntegrateFilesFromServer(ReadWriteInterface rmiServer, long chunks, String fname) {

        int i = 1;
        //int in = filePath.lastIndexOf("/");
        //String fileFoler = filePath.substring(0, in);

        File fd = new File("./output");
        if(!fd.exists()){
            fd.mkdir();
        }

        try {
            FileOutputStream fos = new FileOutputStream(new File("./output/"+ fname));
            while(i <= chunks) {
                try {
                    byte[] r = rmiServer.FileRead64K(fname, i);
                    //System.out.println(new String(r));
                    fos.write(r);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        ReadWriteInterface rmiServer;
        Registry registry;
        //filepath = arr[0] ,  ip = arr[1] , port no = arr[2]
        //String serverAddress="127.0.1.1";
        //String serverPort="3232";
        ///String text="Hey..!Whatspp?";
        //filePath = "home/rishimittal/Documents/SEM2/DS/Assignment-2/c";
        String inp = args[0];
        File fe = new File(inp);
        filePath = fe.getAbsolutePath();
        String fname = fe.getName();

        String serverAddress= args[1];
        String serverPort= args[2];

        //System.out.println("sending "+text+" to "+serverAddress+":"+serverPort);
        try{
            // get the �gregistry�h
            registry=LocateRegistry.getRegistry(
                    serverAddress,
                    (new Integer(serverPort)).intValue()
            );
            // look up the remote object
            rmiServer=
                    (ReadWriteInterface)(registry.lookup("rmiServer"));
            // call the remote method
            //rmiServer.receiveMessage(text);

            //Find the filename out of the file Path
            //int fg = filePath.lastIndexOf("/");
            //String fname = filePath.substring(fg + 1);

            RMIClient rmi = new RMIClient();
            //Sends file to server in chunks .
            rmi.sendFilToServerInChunks(rmiServer, fname);
            //Receives the count of the chunks from server
            long chunks = rmiServer.NumFileChunks(fname);
            //Receives the file and intergrate the file in to
            // the nrew file int the given file path.
            rmi.receiveAndIntegrateFilesFromServer(rmiServer, chunks, fname);
        }
        catch(RemoteException e){
            e.printStackTrace();
        }
        catch(NotBoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}