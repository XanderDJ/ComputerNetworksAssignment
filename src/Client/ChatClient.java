package Client;
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
            Scanner inputServer = new Scanner(inFromServer);
            outputWtr.println(command+" /"+location+" HTTP/1.1");
            outputWtr.println("Host: "+dnsAdress+":"+String.valueOf(port));
            outputWtr.println("");
            outputWtr.flush();

            StringBuilder response = new StringBuilder();
            boolean endChunkNotGiven = true;
            while(endChunkNotGiven && inputServer.hasNext()){

                String nextLine = inputServer.nextLine();
                if(nextLine.equals("0")){
                    endChunkNotGiven =false;
                }
                if(nextLine.matches("-?[0-9a-fA-F]+")){
                    nextLine = "";
                }
                response.append(nextLine + "\r\n");
            }


//            StringBuilder response = new StringBuilder();
//            int contentLength = 1;
//            Boolean readingContent = false;
//            StringBuilder line = new StringBuilder();
//            int currentChar;
//            while (contentLength > 0 && (currentChar = inFromServer.read()) != -1){
//                line.append(Character.toChars(currentChar));
//
//                if (readingContent){
//                    contentLength -= 1;
//                }
//
//                // De lijn is volledig
//                if (currentChar == '\n'){
//                    response.append(line);
//                    if (line.toString().contains("Content-Length:")){
//                        contentLength = Integer.valueOf(line.toString().trim().split(" ")[1]);
//                    } else if (line.toString().equals("\r\n")){
//                        readingContent = true;
//                    }
//                    line = new StringBuilder();
//
//                }
//            }
//            response.append(line);
            saveFiles(dnsAdress,location,response.toString(),".html");
            //System.out.print(response);

        } catch (Exception e){
            System.out.println(e);
        }
    }
    private int counter;


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


    private Socket connection;
    private PrintWriter outputWtr;
    private InputStreamReader inFromServer;
}
