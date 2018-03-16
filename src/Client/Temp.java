package Client;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Temp {
    public static void main(String[] args){

        String str = "0\r\n\r\n";
        Scanner input = new Scanner(str);
        str = input.nextLine() + "\r\n";
        System.out.println(str.contains("0\r\n"));
    }

    private static String[] parseURL(String url){
        return url.split("/", 2);
    }
}
