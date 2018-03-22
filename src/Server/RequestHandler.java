package Server;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a universal handler that is used by the server. Each time a connection is
 * established with the server, the server launches a new thread containing this class.
 *
 * This class will handle http-requests from clients
 * Implemented http-methods are:
 *      - GET
 *      - HEAD
 *      - PUT
 *      - POST
 *
 * Authors: De Heel Benson, De Jaegere Xander
 * Version: 1.0
 */
public class RequestHandler implements Runnable {

    /**
     * Create a new requestHandler that will handle the requets of the client on the other side of the
     * given connection.
     *
     * @param connection        The client to handle.
     */
    public RequestHandler(Socket connection){
        this.connection = connection;
        try {
            this.inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.outputWtr = new PrintWriter(connection.getOutputStream());
        } catch (IOException e){
            System.out.println(e);
        }
    }

    /**
     * Read and interpret the client's request.
     * This method will:
     *      - Read the client's request on the InputStream
     *      - Parse the request and decide which http-method is desired
     *      - Call the right http-method
     *      - Close the connection if the client sent "Connection: Close" in the request
     */
    @Override
    public void run() {
        try {
            StringBuilder request = new StringBuilder();

            boolean firstLine = true;

            Map<String, String> requestOptions = new HashMap<>();
            String method = "";
            String location = "";
            String[] parsedLine;
            String line;
            while ((line = inFromClient.readLine()) != null) {
                request.append(line);
                request.append("\r\n");

                if (firstLine) {
                    String[] res = line.split(" ");
                    method = res[0];
                    location = res[1];
                    firstLine = false;

                // Interpret the line
                } else if ((parsedLine = line.split(" ", 2))[0].equals("Connection:")){
                    if (parsedLine[1].equals("Close"))
                        closeConnection = true;
                } else if (parsedLine.length == 2)
                    requestOptions.put(parsedLine[0], parsedLine[1]);

                // End of request
                if (line.length() == 0)
                    break;
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


    /**
     * This method deals with "PUT" and "POST" requests from the client.
     * This method will:
     *          - Read the file to be stored on the server trough the InputStream of the connection
     * IF append == true:
     *          - Append the file to the existing file, or creating a new file if non-existent, this
     *            represents a "POST"-request
     * ELSE:
     *          - Overwrite any existing file with the same name and
     * FINALLY:
     *          - Create and send a response to the client.
     *
     * @param file      The file to be stored on the server.
     * @param options   The optiona that came with the client's http-request
     * @param append    If true, the given file will be appended to any existing file with the same
     *                  file name, or created if non-existent
     *                  If false, the given file will overwrite any existing file with the same name.
     */
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

    /**
     * Get contentLength characters from the inputStream of this RequestHandler's connection.
     *
     * @param contentLength    The amount of characters to be read.
     * @return  The contentLength characters that the client connected to this RequestHandler has sent,
     *          as one string.
     */
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

    /**
     * Sends the given response trough the connection of this RequestHandler and sends an additional
     * line with the current Date and time.
     *
     * @param response  The response to be send.
     */
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
    private BufferedReader inFromClient;
    private final Socket connection;
}
