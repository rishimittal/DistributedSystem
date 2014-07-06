
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by rishimittal on 21/1/14.
 */
public class XmlServer{


    private static final String XML_FOLDER_PATH = "../";
    private static final String XML_INTER = "/xml-inter";
    //private String inputFilePath = null;
    //private static final String INPUT_FILE_PATH = "/home/rishimittal/Documents/SEM2/DS/DS_DEMO/input";
    //private boolean isStopped = false;


    //public XmlServer(String inputFilepath) {
    //       this.inputFilePath = inputFilepath;
    //}

    public void addElementsToXML(String line, XMLStreamWriter writer){

        try {
        String colon_sp[] = line.split(":");

        //Segment  by the : first is name and second is entire course list
        String name = colon_sp[0];

        writer.writeStartElement("name");
        writer.writeCharacters(name);
        writer.writeEndElement();
        for(int j = 1 ; j < colon_sp.length ; j++ ) {
            //Segment the second list by comma first is course name and second is course marks

            String comma_sp[] = colon_sp[j].split(",");
            writer.writeStartElement("course");

            writer.writeAttribute("name", comma_sp[0]);
            writer.writeAttribute("marks", comma_sp[1]);

            //System.out.println(comma_sp[0]);
            //System.out.println(comma_sp[1]);
            writer.writeEndElement();
        }

        }catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    public void tranferFile(){

        ServerSocket serverSocket = null;
        File fft = new File(XML_FOLDER_PATH + XML_INTER);
        try {
            serverSocket = new ServerSocket(4000);

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

            XmlServer xs = new XmlServer();


        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(arr[0]));

            XMLOutputFactory factory      = XMLOutputFactory.newInstance();

            XMLStreamWriter writer = factory.createXMLStreamWriter(
                    new FileWriter(XML_FOLDER_PATH + XML_INTER));

            writer.writeStartDocument();

            writer.writeStartElement("student-record");

            while(true) {

                String line = bufferedReader.readLine();

                if(line == null ) break;

                writer.writeStartElement("student");

                xs.addElementsToXML(line, writer);

                writer.writeEndElement();
                //System.out.println(line);

            }

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();
            bufferedReader.close();

            xs.tranferFile();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

}
