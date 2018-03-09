package Client;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileWriter;


public class ChatClient {
    private int DEFAULTPORT = 80;

    public ChatClient(String command, String url, int port){
        connect(url, port);

        if (command.equals("GET") || command.equals("HEAD"))
            fetch(command, url, port);
        else if (command.equals("PUT") || command.equals("POST"))
            place(command, url, port);
    }

    public void fetch(String command, String url, int port){
        try {
            String message = "GET "+url+" "+ String.valueOf(port)+" HTTP/1.1";
            PrintWriter writer = new PrintWriter(this.connection.getOutputStream());
            writer.println(message);

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
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void connect(String url){
        try{
            this.connection = new Socket(url, DEFAULTPORT);
            this.writer = new PrintWriter(this.connection.getOutputStream());
        } catch(Exception e){
            System.out.print(e);
        }
    }


    private PrintWriter writer;
    private Socket connection;
}
