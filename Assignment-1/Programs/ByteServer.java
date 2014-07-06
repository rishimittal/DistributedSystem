import com.sun.org.apache.xpath.internal.SourceTree;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rishimittal on 22/1/14.
 */
public class ByteServer{

    private static final String BYTE_FOLDER_PATH = "../";
    private static final String BYTE_INTER = "/byte-INTER";
    //private static final String INPUT_FILE_PATH = "/home/rishimittal/Documents/SEM2/DS/DS_DEMO/input";
    //private String inputFilePath = null;

    private void makeByteIR(String line, PrintStream ps) {

       // try {

        String colon_sp[] = line.split(":");

        //Segment  by the : first is name and second is entire course list
        String name = colon_sp[0];

        byte course_len = (byte) (colon_sp.length - 1);
        //System.out.println(course_len);
        byte name_len = (byte) name.length();
        //Writing the course length
        ps.write(course_len);
        //Writing name length
        ps.write(name_len);
        try {
            //Write the name
            ps.write(name.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

       // byte[] course_nums = new byte[(int)Math.ceil((double)(course_len * 7 ) / 8 ) ];
        byte [] course_nums = new byte[course_len];
        int k = 0;
        int p = 1;
        int l = 6;
        for(int j = 1 ; j < colon_sp.length ; j++ ) {
            //Segment the second list by comma first is course name and second is course marks
            String comma_sp[] = colon_sp[j].split(",");
            //System.out.println(comma_sp[0]);
            byte course_name_len = (byte) comma_sp[0].length();
            //System.out.println(course_name_len);
            ps.write(course_name_len);
            try {
                ps.write(comma_sp[0].getBytes());

                byte course_num = (byte) Integer.parseInt(comma_sp[1]);
                //System.out.println(Integer.toBinaryString(course_num));
                //Apply one left shift
                //byte after_shift = (byte)((byte) course_num << j);
                //System.out.println(Integer.toBinaryString(after_shift));
                //if(j == 1 ){
                    course_nums[k] = course_num;
                    //System.out.println(Integer.toBinaryString(course_num));
                //}

                if( j > 1  ) {
                    if( p % 8 == 0) p = 1;
                    //System.out.println(l + " == l , p ==" + p);
                    course_nums[k - 1] = (byte)(course_nums[k - 1] << (p % 8) | course_nums[k] >> l);
                    //System.out.println(Integer.toBinaryString(course_nums[k - 1]));
                    if(j == colon_sp.length - 1 ) {
                        p++;
                        course_nums[k] = (byte)(course_nums[k] << (p % 8));
                    }

                p++;
                l--;
                if( l < 0 ) {
                    l =  6;
                    continue;
                }
                }
                k++;
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println(comma_sp[0]);
            //System.out.println(comma_sp[1]);
            }
            //System.out.println(course_nums.length);
            for(int a = 0 ; a < course_nums.length ; a++ ) {

               if((a == course_nums.length - 1) && ( (course_nums[course_nums.length - 1] & course_nums[course_nums.length - 1] - 1) == 0) ){
                    continue;
                }
                ps.write(course_nums[a]);
            }

            //for(int m = 0 ; m < course_nums.length ; m++) {
            //    System.out.println(Integer.toBinaryString(course_nums[m] & 0xFF));
            //}

       // }
    }
    public void tranferFile(){

        ServerSocket serverSocket = null;
        File fft = new File(BYTE_FOLDER_PATH + BYTE_INTER);
        try {
            serverSocket = new ServerSocket(3000);

            Socket mySocket = serverSocket.accept();

            FileInputStream f = new FileInputStream(fft);
            OutputStream op = mySocket.getOutputStream();
            while(true) {

                int ch = f.read();
                if(ch == -1 ) {
                    break;
                }
                //System.out.println((char)ch);
                op.write(ch);
            }
            op.close();

            f.close();
            fft.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static void main(String arr[]){

        ByteServer bs = new ByteServer();
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(BYTE_FOLDER_PATH + BYTE_INTER));
            BufferedReader bufferedReader = new BufferedReader(new FileReader(arr[0]));

            while(true) {

                String line = bufferedReader.readLine();

                if(line == null ) break;

                bs.makeByteIR(line, ps);

                //System.out.println(line);

            }
            bufferedReader.close();
            bs.tranferFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
