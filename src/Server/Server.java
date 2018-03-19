package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private static int DEFAULT_PORT = 6268;

    public Server(int port){
        try {
            this.connection = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
        }

        this.port = port;
    }

    public Server(){
        this(DEFAULT_PORT);
    }

    public void runServer(){
        isActive = true;

        try {
            while(isActive){
                Socket user = connection.accept();

                RequestHandler handler = new RequestHandler(user);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ServerSocket connection;

    public int getPort(){
        return this.port;
    }
    private final int port;

    private boolean isActive = false;
}
