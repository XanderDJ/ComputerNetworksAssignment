package Client;

import java.io.*;
import java.net.Socket;
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
        String[] parsedURL = parseURL(url);
        connect(parsedURL[0], port);

        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, parsedURL[0], parsedURL[1], port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, parsedURL[0], parsedURL[1], port);
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
     * Parse the given url by cutting off the "http://" and "http://" parts and splitting them
     * into:
     * - a DNS adress, for example www.example.com
     * - a File name/File location on the requested server, for example /index.html
     *
     * @param url      The URL to be parsed.
     * @return         An array containging the parsed URL with:
     *                      result[0]: DNS-adress
     *                      result[1]: File name/File location
     */
    private String[] parseURL(String url){
        if( url.contains("https://")){
            url = url.substring(8);
        }else if (url.contains("http://")){
            url = url.substring(7);
        }
        return url.split("/", 2);
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
     * @param dnsAdress The host-adress where the File/Header has to be retreived.
     * @param fileName  The name of the file that has to be retreived.
     * @param port      The port to use.
     */
    public void fetch(String command, String dnsAdress, String fileName, int port){
        try {
            // --- Send request
            outputWtr.println(command+" /"+fileName+" HTTP/1.1");
            outputWtr.println("Host: "+dnsAdress+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();

            // --- Receive the header
            //StringBuilder header = new StringBuilder();
            String line,contentType = ".html" ;
            int contentLength = -1;
            while ((line = inFromServer.readLine()) != null) {
                //System.out.println(line);
                //header.append(line);
                //header.append("\r\n");
                if(line.contains("Content-Type: ")){
                    contentType  = line.contains(";")? line.split("/")[1].substring(0,4) :"." + line.split("/")[1];
                }
                if (line.contains("Content-Length:")) {
                    contentLength = Integer.valueOf(line.trim().split(" ")[1]);
                }
                 else if (line.length() == 0) {
                    break;
                }
            }

            if (command.equals("GET")) {
                String content;
                if (contentLength >= 0) {
                    content = readData(contentLength);
                } else
                    content = readChunked();

                saveFiles(dnsAdress, fileName, content, contentType);
               //System.out.print(content);
            }
        } catch (Exception e){
            System.out.println(e);
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
    private String readData(int length) {
        try {
            char[] text = new char[length];
            int i = inFromServer.read(text, 0, length);
            //System.out.println(text);
            return new String(text);

        } catch (IOException e) {
            System.out.println(e);
        }
        return "";
    }


    /**
     * This method is a helper method of the method fetch.
     *
     * Reads a server response with "Transfer-Encoding: Chunked" from the InputStream of the connection
     * of this ChatClient.
     * This method will read all chunks comming in from the server until the server sends 0 as the next
     * Chunk-length.
     *
     * @return A string that contains the full response from a server with Transfer-encoding: Chunked.
     */
    private String readChunked(){
        try {
            StringBuilder response = new StringBuilder();
            String lengthStr;
            while ((lengthStr = inFromServer.readLine()) != null && !lengthStr.equals("0")){
                if (lengthStr.length() == 0)
                    continue;
                System.out.println(lengthStr);
                if(lengthStr.matches("-?[0-9a-fA-F]+"))
                response.append(readData(Integer.valueOf(lengthStr, 16) ));
                else{
                    response.append(lengthStr);
                }
            }
            return response.toString();
        } catch (IOException e){
            System.out.println(e);
        }
        return "";
    }


    /**
     *
     * @param command
     * @param host
     * @param location
     * @param port
     */
    public void place(String command, String host,String location, int port){
        try{
            // interative prompt from command line
            Scanner inputScanner = new Scanner(System.in);
            // prompt user to give command
            System.out.println("Give the request for " + command + " command, close the prompt by typing \"exit\":");

            // Retreive the text to send in the PUT request:
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
            String splittedLocation[] = location.split("\\.");
            String fileType = splittedLocation[splittedLocation.length-1];
            if (fileType.equals("html"))
                type = "text/html";
            else if (fileType.equals("jpeg") || fileType.equals("jpg"))
                type = "image/jpeg";
            else if (fileType.equals("png"))
                type = "image/png";

            // ---Send the request
            outputWtr.println(command + " /" + location + " /HTTP1.1");
            outputWtr.println("Host: "+host+":"+String.valueOf(port));
            outputWtr.println("Content-type: "+ type);
            outputWtr.println("Content-length: "+file.length());
            outputWtr.println("");
            outputWtr.print(file);
            outputWtr.flush();

            StringBuilder answer = new StringBuilder();
            String line;
            while((line = inFromServer.readLine()) != null){
                answer.append(line);
                answer.append("\r\n");
                if (line.length() == 0)
                    break;
            }
            System.out.println(answer);

        }catch (Exception e){
            System.out.println(e);
        }
    }



    public void saveFiles(String host, String fileName, String content,String type){
        String[] array = fileName.split("/");
        String path = "webpages/"+host;
        for(int i = 0; i <array.length-1;i++){
            path += "/"+ array[i];
        }
        System.out.println(path);
        File dir = new File(path);
        dir.mkdirs();
        System.out.println();
        try {
            File file = new File(dir, array[array.length-1].split("\\?")[0]);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void connect(String url, int port){
        try{
            this.connection = new Socket(url, port);
            this.outputWtr = new PrintWriter(connection.getOutputStream());
            this.inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            System.out.println("Connected to: "+url+"\n");
        } catch(Exception e){
            System.out.println(e);
        }
    }


    private Socket connection;
    private PrintWriter outputWtr;
    private BufferedReader inFromServer;
}
