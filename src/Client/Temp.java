package Client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Temp {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("www.google.be",80);
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter outputWriter = new PrintWriter(outputStream);
        outputWriter.println("GET /images/branding/googlelogo/1x/googlelogo_white_background_color_272x92dp.png HTTP/1.1");
        outputWriter.println("Host: www.google.be:80");
        outputWriter.println("");
        outputWriter.flush();
        // Initialize the streams.
        final FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\Xander\\IdeaProjects\\ComputerNetworksAssignment\\webpages\\www.google.be\\images\\branding\\googlelogo\\1x\\googlelogo_white_background_color_272x92dp..png");
        final InputStream inputStream = socket.getInputStream();
        boolean headerRead=false;
        String string = "",line = "";
        int contentLength=0;
        while(!headerRead){
            char nextChar = (char) inputStream.read();
            line += String.valueOf(nextChar);
            if( line.contains("\r\n")){
                string += line;
                if(line.contains("Content-Length:")){
                    contentLength = Integer.valueOf(line.trim().split(" ")[1]);
                }
                line ="";
            }
            if(string.contains("\r\n\r\n")){
                headerRead = true;
            }
        }

        byte[] bytes =new byte[contentLength];
        inputStream.read(bytes);
        fileOutputStream.write(bytes, 0, contentLength);
        inputStream.close();
        fileOutputStream.close();

    }
    private static void print(String string){System.out.println(string);}
}
