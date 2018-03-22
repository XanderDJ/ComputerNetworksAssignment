package Util;

import java.io.IOException;
import java.io.InputStreamReader;

public class HtmlParser {
    public static StringBuilder readData(int length, InputStreamReader inFromServer){
        try {
            StringBuilder response = new StringBuilder();
            StringBuilder line = new StringBuilder();
            int currentChar;

            while (length > 0 && (currentChar = inFromServer.read()) != -1) {
                line.append(Character.toChars(currentChar));

                // De lijn is volledig
                if (currentChar == '\n') {
                    response.append(line);
                    line = new StringBuilder();
                }
                length -= 1;
            }
            // Add the last line
            response.append(line);

            return response;
        } catch (IOException e){
            System.out.println(e);
        }

        return new StringBuilder();
    }

    public static StringBuilder readChunked(InputStreamReader inFromServer){
        try {
            StringBuilder response = new StringBuilder();
            StringBuilder line = new StringBuilder();
            int currentChar;
            boolean ignoreNewline = false;
            while ((currentChar = inFromServer.read()) != '0' && currentChar != -1) {
                if (currentChar == '\n'){
                    if (!(ignoreNewline)) {
                        response.append(readData(Integer.valueOf(line.toString().trim(), 16),inFromServer));
                        line = new StringBuilder();
                        ignoreNewline = true;
                        continue;
                    } else
                        ignoreNewline = false;
                }
                line.append(Character.toChars(currentChar));
            }

            return response;
        } catch (IOException e){
            System.out.println(e);
        }
        return new StringBuilder();
    }
}
