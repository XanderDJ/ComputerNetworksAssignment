package Client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

/**
 * Class that represents a Chat-Client.
 *
 * These Chat-clients can sent http requests to servers and interpret their answers.
 *
 * Implemented http functions:
 * GET - Request and save a document from a server.
 * HEAD - Request the header of a document from a server.
 * PUT - Send a file to the server that will be stored on the server side and overwritten if the server
 *       already has a similar named file.
 * POST - Send a file to the server that will be appended to any existing file with the same name on
 *        the server.
 *
 * Authors: De Heel Benson, De Jaegere Xander
 * Version: 1.0
 */
public class ChatClient {
    private static int DEFAULTPORT = 80;

    /**
     * Initialize a new ChatClient.
     * When invoked, the ChatClient will connect to the given DNS-adress on the given port.
     * The constructor will automatically invoke the right http-request, depending om the given command.
     *
     * @param command   The http-request to invoke.
     * @param url       The adress to connect to.
     * @param port      The port to be used.
     */
    public ChatClient(String command, String url, int port){
        try {
            connectionURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        connect(port);
        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, port);
    }

    /**
     * Initialize a new ChatClient.
     * This consstructor has the same effect as
     *      ChatClient(command, url, DEFAULT_PORT)
     *
     * @param command   The http-request to invoke.
     * @param url       The adress to connect to.
     */
    public ChatClient(String command, String url){
        this(command, url, DEFAULTPORT);
    }


    /**
     * Prints the header of the response given by the server
     *
     * @param inputStream the response stream from the server
     * @return An array of Strings| Strings[0] == Content-Length if content length didn't exist this will be an empty string
     *                            | Strings[1] == Content-Type if content type didn't exist this will be an empty string
     * @throws IOException
     */
    public String[] head(InputStream inputStream) throws IOException {
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
     * This method represents the "GET" and "HEADER" http-method.
     *
     * If command == "Get" this method will:
     *      - Send an http-GET request for the file "fileName" to the open connection trough the given port
     *      - Retreive, interpret and print out the header-response of the server
     *      - If the request was sucessful, this method will invoke a helper method to save the retreived
     *        file
     *
     *  IF command == "HEADER" this method will:
     *      - Send an http-HEADER reques for the file "fileName" to the open connecton trough the given port
     *      - Retreive and print out the header-response of the server
     *
     * @param command   A String which has to be "GET" or "HEADER", depending on the http-request
     * @param port      The port to use.
     */
    public void fetch(String command, int port){
        try {
            // --- Send request
            outputWtr.println(command+" "+ connectionURL.getFile()+ " HTTP/1.1");
            outputWtr.println("Host: "+ connectionURL.getHost()+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();
            InputStream inputStream = connection.getInputStream();

            System.out.println(command+" " +connectionURL.getFile()+" HTTP/1.1");
            System.out.println("Host: "+ connectionURL.getHost()+":"+String.valueOf(port));
            System.out.println("");

            // --- Receive the header results
            String[] headerResults= head(inputStream);
            int contentLength = !headerResults[0].equals("") ? Integer.valueOf(headerResults[0]):-1;
            String contentType = headerResults[1];
            if (command.equals("GET")) {
                String content;
                if (contentLength >= 0) {
                    content = readData(contentLength,inputStream);
                } else
                    content = readChunked(inputStream);
                saveFiles(content, contentType);
                List<String> imageURLs = getLinks();
                for(String URL: imageURLs){
                    getImage(URL);
                }
                System.out.print(content);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
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
    private String readData(int length,InputStream inputStream) {
        try {
            byte[] text = new byte[length];
            inputStream.read(text, 0, length);
            return new String(text,Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * This method is a helper method of the method fetch.
     *
     * Reads a server response with "Transfer-Encoding: Chunked" from the InputStream of the connection
     * of this ChatClient.
     * This method will read all chunks coming in from the server until the server sends 0 as the next
     * Chunk-length.
     *
     * @return A string that contains the full response from a server with Transfer-encoding: Chunked.
     */
    private String readChunked(InputStream inputStream){
        try {
            boolean endChunkReached = false;
            StringBuilder line= new StringBuilder(""),content = new StringBuilder("");
            while (!endChunkReached){
                char nextChar = (char) (inputStream.read() & 0xFF);
                line.append(nextChar);
                //check if we are at the end chunk
                if(line.toString().contains("0\r\n")){
                    endChunkReached = true;
                    byte[] b = new byte[2];
                    inputStream.read(b,0,2);
                }
                //check if line is complete
                else if(line.toString().contains("\r\n")){
                    // check if line gives content length of next chunk
                    if(line.toString().trim().matches("-?[0-9a-fA-F]+")){
                        int contentLength = Integer.valueOf(line.toString().trim(),16);
                        content.append(readData(contentLength+4,inputStream));
                    }
                    // if line == "\r\n"
                    else{
                        content.append(line);
                    }
                    line.delete(0,line.length());
                }
            }
            return content.toString();
        } catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }


    /**
     * Represents the "PUT"- and "POST"-requests of the http-methods.
     * This method will prompt the user for input to put or post on the server which is connected to this
     * ChatClient using the given "port", under  file with "location" as the name on the server.
     *
     * This method will:
     *             - Promt the user for input to send with the PUT/POST request
     *             - Send the http PUT/POST-request (depending on "command") to the server
     *             - Receive and print the response of the server
     * @param command   This String can either be "PUT" or "POST" depending on which http-method the user
     *                  wants to use.
     * @param port      The port to use to reach the server.
     */
    public void place(String command, int port){
        try{
            // interactive prompt from command line
            Scanner inputScanner = new Scanner(System.in);
            // prompt user to give command
            System.out.println("Give the request for " + command + " command, close the prompt by typing \"exit\":");
            // Retrieve the text to send in the PUT request:
            StringBuilder file = new StringBuilder();
            while (inputScanner.hasNextLine()){
                String line = inputScanner.nextLine();

                if (line.equals("exit")) {
                    inputScanner.close();
                    break;
                }
                file.append(line);
                file.append("\r\n");
            }
            // -- Find out the type of this document
            String type = "text/plain";
            String location = connectionURL.getPath();
            if (location.contains(".html"))
                type = "text/html";
            else if (location.contains(".jpeg") || location.contains(".jpg"))
                type = "image/jpeg";
            else if (location.contains(".png"))
                type = "image/png";
            // ---Send the request
            outputWtr.println(command + " /" + connectionURL.getFile() + " /HTTP1.1");
            outputWtr.println("Host: "+connectionURL.getHost()+":"+String.valueOf(port));
            outputWtr.println("Content-type: "+ type);
            outputWtr.println("Content-length: "+file.length());
            outputWtr.println("");
            outputWtr.print(file);
            outputWtr.flush();
            InputStream inputStream = connection.getInputStream();
            head(inputStream);

        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void getImage(String url) throws IOException {
        this.connectionURL = new URL(url);
        outputWtr.println("GET " + connectionURL.getPath() + " HTTP/1.1");
        outputWtr.println("Host: " + connectionURL.getHost() + ":" + 80);
        outputWtr.println("");
        outputWtr.flush();
        System.out.println("GET " + connectionURL.getPath() + " HTTP/1.1");
        System.out.println("Host: " + connectionURL.getHost() + ":" + 80);
        System.out.println("");

        InputStream inputStream = connection.getInputStream();
        String[] res = head(inputStream);
        int contentLength = !res[0].equals("") ? Integer.valueOf(res[0]) : 0;
        byte[] bytes = new byte[contentLength];
        inputStream.read(bytes,0,contentLength);
        StringBuilder filePath = new StringBuilder("webpages/Client/");
        filePath.append(connectionURL.getHost());

        String[] array = connectionURL.getPath().split("/");
        for(int i = 0; i < array.length-1;i++) {
            filePath.append("/");
            filePath.append(array[i]);
        }
        File dir = new File(filePath.toString());
        dir.mkdirs();
        FileOutputStream outputStream = new FileOutputStream(new File(dir,array[array.length-1]));
        outputStream.write(bytes,0,contentLength);

    }


    /**
     * Saves the given "content" locally on this device under the name "path" in the webpages/Client/hostName
     * map. This method will create this directory if non-existent.
     *
     * @param content   The content of the saved file.
     * @param type      The type of the file.
     */
    public void saveFiles(String content, String type){
        StringBuilder filePath = new StringBuilder("webpages/Client/");
        filePath.append(connectionURL.getHost());

        String[] array = connectionURL.getPath().split("/");
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


    /**
     * Establish a connection with a server with "url" as adress using the given port.
     *
     * This method will initialize a socket and will create:
     *              - A BufferedReader associated with the InputStream of this socket.
     *              - A PrintWriter associated with the OutputStream of this socket.
     * @param port  The port to use for the connection.
     */
    public void connect(int port){
        try{
            this.connection = new Socket(connectionURL.getHost(), port);
            this.outputWtr = new PrintWriter(connection.getOutputStream());
            System.out.println("Connected to: "+connectionURL.getHost()+"\n");
        } catch(Exception e){
            System.out.println(e);
        }
    }



    private List<String> getLinks() throws IOException {
        List<String> list=  new ArrayList<>();
        Document doc = Jsoup.connect("https://" + connectionURL.getHost()).get();
        Elements media = doc.select("[src]");
        for(Element src:media){
            if(src.tagName().equals("img")) {
                String link = src.attr("abs:src");
                if(!link.contains("consent")){
                    list.add(link);

                }
            }
        }
        return list;
    }


    private URL connectionURL;
    private Socket connection;
    private PrintWriter outputWtr;
}
