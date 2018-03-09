package Client;

public class Main {
    public static void main(String[] args){
        ChatClient client = new ChatClient(args[0], args[1],Integer.parseInt(args[2]));
    }
}
