import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishimittal on 22/1/14.
 */
public class ByteClient {

    private static final String BYTE_FOLDER_PATH = "../";
    private static final String BYTE_OP = "/byte-Output";
    private static final String BYTE_IR = "/byte-IR";



    private void convertToOriginalForm() {

        //Reset j after reading the each student.
        String opString;
        int numberOfStudents = 0;
        int noOfCourses = 0;
        double noOfCourseByte = 0;
        int nameLength = 0;
        String studentName = "";
        String courseName = "";
        int courseLen = 0;
        StringBuilder marksString = new StringBuilder();
        int k = 0;
        List<String> courseList = new ArrayList<String>();
        try {

            FileInputStream fread = new FileInputStream(BYTE_FOLDER_PATH + BYTE_IR);
            PrintWriter pws = new PrintWriter(new FileOutputStream(BYTE_FOLDER_PATH + BYTE_OP));
            //Reset j after reading the each student.
            int j = 0;
            while(true){


                int ch = fread.read();
                if(ch == -1 ) break;
                //System.out.println(ch);

                //To be repeated for every student
                if(j == 0) {
                    //System.out.println("po");
                    noOfCourses = ch;
                    noOfCourseByte = Math.ceil((ch * 7 ) / 8D);
                    //double n = Math.ceil ( ( ch * 7 ) / 8D );
                    //System.out.println(noOfCourseByte);
                    //System.out.println(noOfCourses);
                    j++;
                    continue;
                }

                if(j == 1) {
                    //System.out.println("lp");
                    nameLength = ch;
                    //System.out.println(nameLength);
                    j++;
                    continue;
                }

                if(nameLength > 0){
                    //System.out.println(studentName);
                    studentName += (char)ch;
                    nameLength--;
                    j++;
                    continue;
                }

                if( noOfCourses > 0 ) {
                    //System.out.println("li");
                    if(courseLen == 0 ) {
                        courseLen = ch;
                        //System.out.println(courseLen);
                        continue;
                    }

                    //System.out.println("-------------"+courseLen+"--------");

                    if(courseLen > 0 ) {
                        courseName += (char)ch;
                        courseLen--;
                        //System.out.println(courseName);
                    }

                    if(courseLen == 0 ){
                        courseList.add(courseName);
                        //System.out.println(courseName);
                        k++;
                        courseName = "";
                        noOfCourses--;
                    }
                    continue;
                }

                if(noOfCourseByte > 0) {
                    //System.out.println(noOfCourseByte);
                    //System.out.println(String.format("%8s",Integer.toBinaryString(ch)).replace(" ", "0"));
                    marksString.append(String.format("%8s", Integer.toBinaryString(ch)).replace(" ", "0"));
                    noOfCourseByte--;
                    j++;
                  //continue;
                }
                //System.out.println("ko");
                if(noOfCourseByte == 0){
                    //System.out.println("pi");
                    j = 0;
                    //studentName = ""
                    //-----------------------------------------------------------
                    //System.out.println(studentName);

                    //for (String n : courseList) {
                      //  System.out.println(n);
                    //}
                    //System.out.println(marksString);
                    String fString = populateString(studentName, courseList , marksString);
                    //System.out.println(fString);
                    pws.println(fString);
                    pws.flush();
                    //-----------------------------------------------------------
                    studentName = "";
                    nameLength = 0;
                    courseName= "";
                    courseList.clear();
                    marksString.setLength(0);
                }

                //j++;
            }
            pws.close();
            fread.close();

            //System.out.println(studentName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String populateString(String sname, List<String> courseList , StringBuilder marksString){

        StringBuilder fString = new StringBuilder();
        fString.append(sname);

        int courseCount = courseList.size();

        //List<String> course_marks_list = new ArrayList<String>();
        //System.out.println(marksString);
        //System.out.println(marksString.length());

        for(int n = 0 ; n < courseCount ; n++ ){
            if(n == 0){
                //course_marks_list.add("0" + marksString.substring( 0 , 7 ));
                fString.append(":").append(courseList.get(n)).append(",").append(Integer.parseInt("0" + marksString.substring( 0 , 7 ), 2));
                //System.out.println(Integer.parseInt("0" + marksString.substring( 0 , 7 ), 2));
            }else {
                //System.out.println( (7 * n) + 1);
                fString.append(":").append(courseList.get(n)).append(",").append(Integer.parseInt("0" + marksString.substring( (7 * n)  , 7 * (n + 1) ), 2));
                //System.out.println(Integer.parseInt("0" + marksString.substring( (7 * n)  , 7 * (n + 1) ), 2));
                //course_marks_list.add("0" + marksString.substring(  (7 * n)  ,  7 * (n + 1) ) );
            }
        }
        //System.out.println(fString);
        return fString.toString();
    }

    public static void main(String arr[]){

        Socket sock = null;
        ByteClient bc = new ByteClient();
        File f_inter = new File(BYTE_FOLDER_PATH + BYTE_IR);

        try {
            sock = new Socket("localhost", 3000);

            InputStream is = sock.getInputStream();

            BufferedOutputStream brOutputStream = new BufferedOutputStream(new FileOutputStream(f_inter));

            int in;

            while ((in = is.read()) != -1 ) {
                brOutputStream.write((char)in);
            }

            brOutputStream.close();

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        bc.convertToOriginalForm();

        //f_inter.delete();
    }

}
