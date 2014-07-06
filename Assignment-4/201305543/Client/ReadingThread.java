import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishimittal on 6/4/14.
 */

public class ReadingThread implements Runnable{

    public Registry registry;
    public RandomAccessFile raf;
    public List<String> listOfServers;
    public String fname;
    public int listSize;
    public long chunks;
    public int threadNumber ;
    public Thread t;


    public ReadingThread(int threadNumber, Registry registry , List<String> listOfServers, String fname, int listSize, long chunks){
        this.threadNumber = threadNumber;
        this.registry = registry;
        this.listOfServers = listOfServers;
        this.fname = fname;
        this.listSize = listSize;
        this.chunks = chunks;
    }


    @Override
    public void run() {

        System.out.println("Running  : " + threadNumber + "listSize : "+ listSize);
        try {

            ReadWriteInterface rwi = (ReadWriteInterface) registry.lookup(listOfServers.get(threadNumber));

            int currentChunkOffset = threadNumber;

            while(currentChunkOffset < chunks) {

                //if(Client.chunk_stat[currentChunkOffset] == 0 ) {
                    byte[] r = rwi.FileRead64K(fname, currentChunkOffset);
                    raf.seek(currentChunkOffset * 65536);
                    System.out.println("Thread Number : " + threadNumber + " ,writing chunk No. " + currentChunkOffset);
                    Client.chunk_stat[currentChunkOffset] = 1;
                    raf.write(r);
                    currentChunkOffset += listSize;
                //}
            }

        } catch (RemoteException e) {
            System.out.println("File Server Down : " + threadNumber);
            Client.failedThreads.add(listOfServers.get(threadNumber));
            //e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread beginThread() {

        System.out.println("Starting " +  threadNumber );
        try {
            raf = new RandomAccessFile("./output/"+ fname ,"rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (t == null)
        {
            t = new Thread (this, String.valueOf(threadNumber));
            t.start ();
        }
        return t;
    }

}
