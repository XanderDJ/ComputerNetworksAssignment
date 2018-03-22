package Server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler implements Runnable {

    public RequestHandler(Socket connection){
        this.connection = connection;
        try {
            this.inFromClient = new InputStreamReader(connection.getInputStream());
            this.outputWtr = new PrintWriter(connection.getOutputStream());
        } catch (IOException e){
            System.out.println(e);
        }
    }


    @Override
    public void run() {


    }

    private PrintWriter outputWtr;
    private InputStreamReader inFromClient;
    private final Socket connection;
}
