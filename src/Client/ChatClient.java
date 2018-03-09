package Client;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileWriter;


public class ChatClient {
    private int DEFAULTPORT = 80;

    public static void main(String[] args){
        if( args[0] == "GET"){
            System.out.println(args[1]);
            System.out.println(args[2]);
        }

    }



    public Socket connect(String url){
        try{
        Socket mySocket = new Socket(url, DEFAULTPORT);
        return mySocket;
        }
        catch(Exception ignored){}

        return null;
    }


}
