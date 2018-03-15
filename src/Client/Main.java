package Client;

public class Main {
    public static void main(String[] args){
        if(args.length == 3){
        ChatClient client = new ChatClient(args[0], args[1],Integer.parseInt(args[2]));}
        else{
            ChatClient client = new ChatClient(args[0],args[1]);}
    }
}

