package Client;
import Util.HtmlParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;


public class ChatClient {
    private static int DEFAULTPORT = 80;

    public ChatClient(String command, String url, int port){
        String[] parsedURL = parseURL(url);
        connect(parsedURL[0], port);

        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, parsedURL[0], parsedURL[1], port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, parsedURL[0], parsedURL[1], port);
    }

    public ChatClient(String command, String url){
        this(command, url, DEFAULTPORT);
    }

    private String[] parseURL(String url){
        if( url.contains("https://")){
            url = url.substring(8);
        }else if (url.contains("http://")){
            url = url.substring(7);
        }
        return url.split("/", 2);
    }

    public void fetch(String command, String dnsAdress, String location, int port){
        try {
            // --- Send request
            outputWtr.println(command+" /"+location+" HTTP/1.1");
            outputWtr.println("Host:"+dnsAdress+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();

            // --- Receive the header
            StringBuilder header = new StringBuilder();
            StringBuilder line = new StringBuilder();
            int currentChar;
            int contentLength = -1;
            while ((currentChar = inFromServer.read()) != -1) {
                line.append(Character.toChars(currentChar));

                // De lijn is volledig
                if (currentChar == '\n') {
                    header.append(line);

                    if (line.toString().contains("Content-Length:")) {
                        contentLength = Integer.valueOf(line.toString().trim().split(" ")[1]);
                    } else if (line.toString().equals("\r\n")) {
                        break;
                    }
                    line = new StringBuilder();
                }
            }
            System.out.println(header);
            StringBuilder content;
            if  (contentLength >= 0){
                content = HtmlParser.readData(contentLength,inFromServer);
            } else
                content = HtmlParser.readChunked(inFromServer);

            saveFiles(dnsAdress, location, content.toString(), ".html");
            System.out.print(content);

        } catch (Exception e){
            System.out.println(e);
        }
    }



    public void place(String command, String host,String location, int port){
        try{
            // interative prompt from command line
            Scanner inputScanner = new Scanner(System.in);
            // prompt user to give command
            System.out.println("Give the request for " + command + " command:");
            Scanner inputServer = new Scanner(inFromServer);
            String file = inputScanner.nextLine();
            outputWtr.println(command + " /" + location + "/" + file + " /HTTP1.1");
            outputWtr.println("Host: "+host+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();
            String answer = "";
            while(inputServer.hasNext()){
                answer += inputServer.nextLine() + "\n";
            }
            System.out.println(answer);

        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void saveFiles(String host, String location, String content,String type){
        File dir = new File("webpages/" + host);
        dir.mkdirs();
        try {
            File file = new File(dir, location.split(type,2)[0] + type);
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
            this.inFromServer = new InputStreamReader(connection.getInputStream());
            System.out.println("Connected to: "+url+"\n");
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public boolean handleResponseCode(String lineWithResponse){
        String responseCode = lineWithResponse.substring(9);
        System.out.print(responseCode);
        if( responseCode.contains("200")) return true;
        return false;

    }


    private Socket connection;
    private PrintWriter outputWtr;
    private InputStreamReader inFromServer;
}
