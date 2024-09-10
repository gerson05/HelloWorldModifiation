import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.*;
import com.zeroc.Ice.Exception;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final String EXIT_COMMAND = "exit";
    private static final String SERVER_PROXY = "SimpleServer:default -p 9099";

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "config.client");
             Scanner scanner = new Scanner(System.in)) {

            PrinterPrx server = PrinterPrx.checkedCast(communicator.stringToProxy(SERVER_PROXY));
            if (server == null) throw new Error("Invalid proxy");

            String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();
            String userPrefix = username + "@" + hostname + ":";

            System.out.println("Welcome " + username + " on " + hostname + ".");

            while (true) {
                System.out.print("Enter a command (or 'exit' to quit): ");
                String input = scanner.nextLine().trim();

                if (EXIT_COMMAND.equalsIgnoreCase(input)) break;

                String message = username + "@" + hostname + ":" + input;
                long startTime = System.currentTimeMillis();
                Response response = server.printString(message);
                long totalTime = System.currentTimeMillis() - startTime;

                System.out.printf("Server response: %s%n ms", response.value);
                System.out.printf("Total time: %dms%n ms", totalTime);
                System.out.printf("Server processing time: %dms%n ms", response.responseTime);
                System.out.printf("Network latency: %dms%n ms", totalTime - response.responseTime);
            }
        } catch (Exception | UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}