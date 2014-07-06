import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Comparator;

/**
 * Created by rishimittal on 21/1/14.
 */
public class XmlClient extends DefaultHandler {

    private static final String XML_FOLDER_PATH = "../";
    private static final String XML_OP = "/xml-Output";
    private static final String XML_IR = "/xml-IR";
    private StringBuilder tagContent;
    private String writeString ;
    private PrintWriter out;

    public XmlClient() {
        tagContent = new StringBuilder();
        try {
            out  = new PrintWriter(new FileOutputStream(XML_FOLDER_PATH + XML_OP));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void convertToOriginalForm(){

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try{

            SAXParser parser = factory.newSAXParser();
            File xmlFile = new File(XML_FOLDER_PATH + XML_IR);
            //System.out.println(xmlFileName);
            parser.parse(xmlFile, this);

        }catch(ParserConfigurationException e){
            System.out.println("ParserConfig error");
            e.printStackTrace();
        }catch (SAXException e){
            System.out.println("SAX Exception : xml not well formed");
            e.printStackTrace();
        }catch(IOException e){
            System.out.println("IO Error");
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localname, String elementName, Attributes attributes) {
            if(elementName.equalsIgnoreCase("student")){
                writeString = "";
                tagContent.setLength(0);
            }
            if(elementName.equalsIgnoreCase("course")){
                writeString += ":";
                writeString += attributes.getValue("name");
                writeString += ",";
                writeString += attributes.getValue("marks");
            }
    }

    @Override
    public void endElement(String uri, String localname, String elementName) {


        if(elementName.equalsIgnoreCase("name")){
            writeString = new String(tagContent);
        }
        if(elementName.equalsIgnoreCase("student")){
            //Write these lines to the file named as XMl_output
            //System.out.println(writeString);
            out.println(writeString);
        }

    }

    @Override
    public void characters(char[] chars, int start, int length) {
        tagContent.append(new String(chars, start, length));
    }

    public static void main(String arr[]) {

        Socket sock = null;
        XmlClient xc = new XmlClient();
        File f_inter = new File(XML_FOLDER_PATH + XML_IR);

        try {
            sock = new Socket("localhost", 4000);

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

        xc.convertToOriginalForm();
        xc.out.close();
        //f_inter.delete();
    }

}
