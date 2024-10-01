import java.io.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.SocketException;
import com.zeroc.Ice.Util;
import java.io.*;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Server
{
    public static void main(String[] args)
    {
        int status = 0;
        Communicator communicator = null;
        try {
            communicator = Util.initialize(args, "config.server");
            ObjectAdapter adapter = communicator.createObjectAdapter("ServerAdapter");
            Object object = new PrinterI();
            adapter.add((com.zeroc.Ice.Object) object, Util.stringToIdentity("SimpleServer"));
            adapter.activate();
            System.out.println("Server started...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            e.printStackTrace();
            status = 1;
        }
        if (communicator != null) {
            communicator.destroy();
        }
        System.exit(status);
    }

    public static String fibonacci(int n) {
        List<Integer> fibonacciList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            fibonacciList.add(i < 2 ? i : fibonacciList.get(i - 1) + fibonacciList.get(i - 2));
        }
        return fibonacciList.toString();
    }

    public static String primeFactors(int n) {
        List<Integer> primeFactorsList = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                primeFactorsList.add(i);
                n /= i;
            }
        }
        return primeFactorsList.toString();
    }

    public static String listInterfaces() throws java.net.SocketException {
        return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .map(NetworkInterface::getDisplayName)
                .collect(Collectors.joining("\n"));
    }

    public static String listPorts(String ipAddress) {
        return executeCommand("nmap -p- " + ipAddress);
    }

    public static String executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

        try {
            Process process = processBuilder.start();

            boolean finished = process.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "El comando excedió el tiempo de espera";
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String output = reader.lines().collect(Collectors.joining("\n"));
                String error = errorReader.lines().collect(Collectors.joining("\n"));

                if (!error.isEmpty()) {
                    return "Error: " + error;
                }

                return output;
            }
        } catch (Exception e) {
            return "Error al ejecutar el comando: " + e.getMessage();
        }
    }
}