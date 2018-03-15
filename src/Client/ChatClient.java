package Client;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ChatClient {
    private static int DEFAULTPORT = 80;

    public ChatClient(String command, String url, int port){
        connect(url, port);

        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, url, port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, url, port);
    }

    public ChatClient(String command, String url){
        this(command, url, DEFAULTPORT);
    }


    public void fetch(String command, String url, int port){
        try {
            outputWtr.println(command+" /index.html HTTP/1.1");
            outputWtr.println("Host: "+url+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();

            StringBuilder response = new StringBuilder();
            int contentLength = 1;
            Boolean readingContent = false;
            StringBuilder line = new StringBuilder();
            int currentChar;
            while (contentLength > 0 && (currentChar = inFromServer.read()) != -1){
                line.append(currentChar);

            }




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
