package Client;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatClient {
    private static int DEFAULTPORT = 80;

    public ChatClient(String command, String url, int port){
        String[] parsedURL = parseURL(url);
        connect(parsedURL[0], port);

        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, parsedURL[0], parsedURL[1], port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, url, port);
    }

    public ChatClient(String command, String url){
        this(command, url, DEFAULTPORT);
    }

    private String[] parseURL(String url){
        return url.split("/", 2);
    }

    public void fetch(String command, String dnsAdress, String location, int port){
        try {
            outputWtr.println(command+" /"+location+" HTTP/1.1");
            outputWtr.println("Host: "+dnsAdress+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();

            StringBuilder response = new StringBuilder();
            int contentLength = 1;
            Boolean readingContent = false;
            StringBuilder line = new StringBuilder();
            int currentChar;
            while (contentLength > 0 && (currentChar = inFromServer.read()) != -1){
                line.append(Character.toChars(currentChar));

                if (readingContent){
                    contentLength -= 1;
                }

                // De lijn is volledig
                if (currentChar == '\n'){
                    response.append(line);
                    if (line.toString().contains("Content-Length:")){
                        contentLength = Integer.valueOf(line.toString().trim().split(" ")[1]);
                    } else if (line.toString().equals("\r\n")){
                        readingContent = true;
                    }
                    line = new StringBuilder();

                }
            }
            response.append(line);

            System.out.print(response);

        } catch (Exception e){
            System.out.println(e);
        }
    }

    public void place(String command, String url, int port){
        //TODO
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


    private Socket connection;
    private PrintWriter outputWtr;
    private InputStreamReader inFromServer;
}
