package Server;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
        try {
            StringBuilder request = new StringBuilder();

            StringBuilder line = new StringBuilder();
            int lineNb = 0;
            int currentChar;

            Map<String, String> requestOptions = new HashMap<String, String>();
            String method = "";
            String location = "";
            String[] parsedLine;
            while ((currentChar = inFromClient.read()) != -1) {
                line.append(Character.toChars(currentChar));

                if (currentChar == '\n'){
                    request.append(line);

                    if (lineNb == 0) {
                        String[] res = line.toString().split(" ");
                        method = res[0];
                        location = res[1];

                        // Interpret the line
                    } else if ((parsedLine = line.toString().split(" ", 2))[0].equals("Connection:")){
                        if (parsedLine[1].equals("Close"))
                            closeConnection = true;

                    } else if (parsedLine.length == 2)
                        requestOptions.put(parsedLine[0], parsedLine[1]);

                    lineNb++;
                    // End of request
                    if (line.toString().equals("\r\n"))
                        break;

                    line = new StringBuilder();
                }
            }

            System.out.println(request.toString());
            // --Call the proper method
            switch (method) {
                case "GET":
                    get(location, requestOptions);
                    break;
                case "HEAD":
                    head(location, requestOptions);
                    break;
                case "POST":
                    place(location, requestOptions, true);
                    break;
                case "PUT":
                    place(location, requestOptions, false);
                    break;
            }

            if (closeConnection)
                connection.close();

        } catch (IOException e){
            System.out.println(e);
        }
    }

    public void get(String file, Map<String,String> options){
        boolean continueOutputting = head(file, options);
        if(!continueOutputting){
            return;
        }
        File newFile = new File(file);
        outputWtr.print(newFile);
        outputWtr.print("");
        outputWtr.flush();

    }

    public boolean head(String file, Map<String,String> options){
        //get the file
        File newFile = new File(file);
        //build response
        StringBuilder response = new StringBuilder();

        //check if it exists
        boolean exists = newFile.exists();
        long lastModified = newFile.lastModified();
        SimpleDateFormat format = new SimpleDateFormat();
        String date = format.format(lastModified);
        long time = System.currentTimeMillis();
        if(!exists){
            response.append("HTTP/1.1 404 Not Found");
            response.append("Lastmodified: " + date+ "\r\n");

            return false;
        }
        else if(!options.containsKey("host :")){
            response.append("HTTP/1.1 400 Bad Request");
            response.append("Lastmodified: " + date+ "\r\n");

            return false;
        }
        else if( time > lastModified){
            response.append("HTTP/1.1 304 Not Modified");
            response.append("Lastmodified: " + date+ "\r\n");
            respond(response.toString());
            return false;
            }
        else{
            response.append("HTTP/1.1 200 OK\r\n");
            response.append("Lastmodified: " + date+ "\r\n");
            response.append("Expires: -1");
            response.append("Content-Type: " +  (file.split(".")[1].equals("html") ? "text/html" : "image/png") );
            response.append("Content-length: " + newFile.length() + "\r\n");
            response.append("Server: localhost");
            respond(response.toString());
            return true;
        }

    }

    public void place(String file, Map<String,String> options, boolean append){
        // Create new directory for the localhost if it doesnt exist yet
        File dir = new File("webpages/localhost");
        dir.mkdirs();
        try {
            // -- Retreive the text to be written
            int contentLength = Integer.valueOf(options.get("Content-length:").trim());
            String text = retreiveText(contentLength);

            // -- Create/overwrite the file
            File newFile = new File(dir, file);
            boolean exists = newFile.exists();
            FileWriter writer = new FileWriter(newFile, append);
            writer.append(text);
            writer.flush();
            writer.close();

            // -- Respond to the request
            StringBuilder response = new StringBuilder();
            if (!exists){
                response.append("HTTP/1.1 201 Created\r\n");
            } else
                response.append("HTTP/1.1 204 No Content\r\n");
            response.append("Content-Location: ");
            response.append(file);
            response.append("\r\n");
            respond(response.toString());
        } catch (IOException e){
            System.out.println(e);
        }
    }

    private String retreiveText(int contentLength) {
        try {
            char[] text = new char[contentLength];
            int i = inFromClient.read(text, 0, contentLength);
            return new String(text);
        } catch (IOException e) {
            System.out.println(e);
        }
        return "";
    }


    private void respond(String response){
        ZonedDateTime date = ZonedDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDate = date.format(format);
        outputWtr.write(response);
        outputWtr.write("Date: ");
        outputWtr.write(formattedDate);
        outputWtr.write("\r\n");
        // Empty line to close the response
        outputWtr.write("\r\n");
        outputWtr.flush();

    }

    private boolean closeConnection = false;

    private PrintWriter outputWtr;
    private InputStreamReader inFromClient;
    private final Socket connection;
}
