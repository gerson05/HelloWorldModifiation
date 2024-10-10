import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final String EXIT_COMMAND = "exit";
    private static final String SERVER_PROXY = "Printer.Proxy";

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "config.client");
             Scanner scanner = new Scanner(System.in)) {

            PrinterPrx server = PrinterPrx.checkedCast(communicator.propertyToProxy(SERVER_PROXY));
            if (server == null) throw new Error("Invalid proxy");

            String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();
            String userPrefix = username + "@" + hostname + ":";

            // Register client
            server.printString("register " + hostname);

            System.out.println("Welcome " + username + " on " + hostname + ".");

            while (true) {
                System.out.print("Enter a command (or 'exit' to quit): ");
                String input = scanner.nextLine().trim();

                if (EXIT_COMMAND.equalsIgnoreCase(input)) break;

                String message = userPrefix + input;
                long startTime = System.currentTimeMillis();
                Response response = server.printString(message);
                long totalTime = System.currentTimeMillis() - startTime;

                System.out.printf("Server response: %s%n", response.value);
                System.out.printf("Total time: %dms%n", totalTime);
                System.out.printf("Server processing time: %dms%n", response.responseTime);
                System.out.printf("Network latency: %dms%n", totalTime - response.responseTime);
            }
        } catch ( UnknownHostException e) {
            System.err.println("Error detallado:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}