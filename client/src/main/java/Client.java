import Demo.CallbackPrx;
import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    private static final String SERVER_PROXY = "Printer.Proxy";
    private static final String EXIT_COMMAND = "exit";

    public static void main(String[] args) {
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);
        AtomicInteger unprocessedRequests = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Array multidimensional para almacenar las métricas
        String[][] metricsArray = new String[5][2]; // 5 filas (número de métricas), 2 columnas (métrica y valor)

        try (Communicator communicator = Util.initialize(args, "config.client");
             Scanner scanner = new Scanner(System.in)) {

            ObjectAdapter adapter = communicator.createObjectAdapter("Callback.Client");
            CallbackReceiverI callbackReceiver = new CallbackReceiverI();

            ObjectPrx prx = adapter.add(callbackReceiver, Util.stringToIdentity("CallbackReceiver"));
            CallbackPrx callbackPrx = CallbackPrx.checkedCast(prx);

            adapter.activate();
            PrinterPrx server = PrinterPrx.checkedCast(communicator.propertyToProxy(SERVER_PROXY));
            if (server == null) throw new Error("Invalid proxy");
            String username = System.getProperty("user.name");
            String hostname = InetAddress.getLocalHost().getHostName();
            server.register(username + "@" + hostname + UUID.randomUUID(), callbackPrx);
            String userPrefix = username + "@" + hostname + ":";

            System.out.println("Welcome " + username + " on " + hostname + ".");

            while (true) {
                System.out.print("Enter a number to get the Fibonacci series up to that number\n" +
                        "'listifs' for network interfaces\n" +
                        "'listports <ip address>' for open ports on <ip address>\n" +
                        "'!command' to execute command on server linux console\n" +
                        "'list clients' to list all connected clients\n" +
                        "'to <hostname>: <message>' to send a message to a specific client\n" +
                        "'BC <message>' to broadcast a message to all clients\n" +
                        "or 'exit' to quit: ");
                String input = scanner.nextLine().trim();

                if (EXIT_COMMAND.equalsIgnoreCase(input)) break;

                totalRequests.incrementAndGet();
                try {
                    String message = userPrefix + input;
                    long requestStartTime = System.currentTimeMillis();
                    Response response = server.printString(message);

                    long totalTime = System.currentTimeMillis() - requestStartTime;

                    System.out.printf("Server response: %s%n", response.value);
                    System.out.printf("Total time: %dms%n", totalTime);
                    System.out.printf("Server processing time: %dms%n", response.responseTime);
                    System.out.printf("Network latency: %dms%n", totalTime - response.responseTime);

                    successfulRequests.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Request failed:");
                    e.printStackTrace();
                    failedRequests.incrementAndGet();
                }
            }

            // Calcular los unprocessed
            unprocessedRequests.set(totalRequests.get() - successfulRequests.get() - failedRequests.get());

            // Calcular el throughput
            long totalTime = System.currentTimeMillis() - startTime;
            double throughput = totalRequests.get() / (double) totalTime * 1000; // solicitudes por segundo

            // Almacenar los valores en el array multidimensional
            metricsArray[0][0] = "Total Requests";
            metricsArray[0][1] = String.valueOf(totalRequests.get());

            metricsArray[1][0] = "Successful Requests";
            metricsArray[1][1] = String.valueOf(successfulRequests.get());

            metricsArray[2][0] = "Failed Requests";
            metricsArray[2][1] = String.valueOf(failedRequests.get());

            metricsArray[3][0] = "Unprocessed Requests";
            metricsArray[3][1] = String.valueOf(unprocessedRequests.get());

            metricsArray[4][0] = "Throughput (requests/sec)";
            metricsArray[4][1] = String.format("%.4f", throughput);

            // Imprimir la tabla con las métricas
            System.out.println("\n--- Metrics Summary ---");
            System.out.format("+------------------------+---------------------+%n");
            System.out.format("| Metric                 | Value               |%n");
            System.out.format("+------------------------+---------------------+%n");

            for (String[] row : metricsArray) {
                System.out.format("| %-22s | %-19s |%n", row[0], row[1]);
            }
            System.out.format("+------------------------+---------------------+%n");

        } catch (Exception e) {
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