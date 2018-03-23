package Client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Temp {
    public static void main(String[] args) throws IOException {
        Socket connection = new Socket("www.google.be",80);
        PrintWriter outputWtr = new PrintWriter(connection.getOutputStream());
        outputWtr.println("GET /images/branding/googlelogo/2x/googlelogo_color_120x44dp.png HTTP/1.1");
        outputWtr.println("Host: " + "www.google.be" + ":" + 80);
        outputWtr.println("");
        outputWtr.flush();
        String host = "www.google.be";
        String url = "/images/branding/googlelogo/2x/googlelogo_color_120x44dp.png";
        InputStream inputStream = connection.getInputStream();
        String[] res = head(inputStream);
        int contentLength = !res[0].equals("") ? Integer.valueOf(res[0]) : 0;
        byte[] bytes = new byte[contentLength];
        inputStream.read(bytes,0,contentLength);
        FileOutputStream outputStream = new FileOutputStream(new File("./webpages/Client/" + host + url));
        outputStream.write(bytes,0,contentLength);
    }


    private static void print(String string){System.out.println(string);}

    /**
     * Prints the header of the response given by the server
     *
     * @param inputStream the response stream from the server
     * @return An array of Strings| Strings[0] == Content-Length if content length didn't exist this will be an empty string
     *                            | Strings[1] == Content-Type if content type didn't exist this will be an empty string
     * @throws IOException
     */
    public static String[] head(InputStream inputStream) throws IOException {
        boolean headerRead=false;
        StringBuilder header = new StringBuilder(""),line = new StringBuilder(""),contentLength =new StringBuilder(""),contentType =new StringBuilder(".");
        while(!headerRead){
            char nextChar = (char) (inputStream.read() & 0xFF);
            line.append(nextChar);
            if( line.toString().contains("\r\n")){
                header.append(line);
                if(line.toString().contains("Content-Length:")){
                    contentLength.append(line.toString().trim().split(" ")[1]);
                }
                if(line.toString().contains("Content-Type: ")){
                    contentType.append( line.toString().contains(";")? line.toString().split("/")[1].substring(0,4) :"." + line.toString().split("/")[1]);
                }
                line.delete(0,line.length());
            }
            if(header.toString().contains("\r\n\r\n")){
                headerRead = true;
            }
        }
        System.out.println(header);
        String[] res = new String[2];
        res[0] = contentLength.toString();
        res[1] = contentType.toString();
        return res;
    }

    /**
     * Helper method for the fetch and readChunked methods.
     *
     * This method will read "length" chars from the InputStream of the connection of this ChatClient and
     * return the result as one string.
     *
     * @param length    The amount of chars to be read.
     * @return          A string read from the InputStream of the connection of this ChatClient.
     *                  OR an empty string if an exception occured.
     */
    private static String readData(int length,InputStream inputStream) {
        try {
            byte[] text = new byte[length];
            inputStream.read(text, 0, length);
            return new String(text, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void saveFiles(String content, String type){
        StringBuilder filePath = new StringBuilder("webpages/Client/");
        filePath.append(80);

        String[] array = "/images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png".split("/");
        for(int i = 0; i < array.length-1;i++){
            filePath.append("/");
            filePath.append(array[i]);
        }
        if (array.length == 0){
            System.out.println("Error: Could not resolve a suitable name for the document to be saved.");
            return;
        }
        File dir = new File(filePath.toString());
        dir.mkdirs();
        try {
            File file = new File(dir, array[array.length-1].split("\\.")[0]+type);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
