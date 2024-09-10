import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.*;
import com.zeroc.Ice.Exception;

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

            System.out.println("Welcome " + username + " on " + hostname + ".");

            while (true) {
                System.out.print("Enter  " +
                        "a number to get the Fibonacci series up to that number " +
                        "'listifs' for network interfaces" +
                        "'listports <ip address> for open ports on <ip address>'" +
                        "'!command' to execute command on server linux console" +
                        "or 'exit' to quit: ");
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
            System.err.println("Error detallado:");
            e.printStackTrace();
            if (e instanceof com.zeroc.Ice.LocalException) {
                System.err.println("Error de Ice: " + e.getClass().getSimpleName());
                System.err.println("Mensaje: " + e.getMessage());
            }
            System.exit(1);
        }
    }
}