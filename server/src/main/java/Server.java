import java.io.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.SocketException;
import com.zeroc.Ice.Util;
import java.io.*;
import java.net.NetworkInterface;
import java.util.*;


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
        List<Integer> fibonnaciList = new ArrayList<>();
        int a = 0, b = 1;
        while (n-- > 0) {
            fibonnaciList.add(a);
            int temp = a + b;
            a = b;
            b = temp;
        }
        return fibonnaciList.toString();
    }

    public static String primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.toString();
    }

    public static String listInterfaces() throws java.net.SocketException {
        StringBuilder stringBuilder = new StringBuilder();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
            stringBuilder.append(networkInterface.getDisplayName()).append("\n");
        }

        return stringBuilder.toString();
    }

    public static String listPorts(String ipAddress) {
        StringBuilder output = new StringBuilder();
        try {

            Process process = Runtime.getRuntime().exec("nmap -p- " + ipAddress);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            output.append("Error executing nmap.");
        }
        return output.toString();
    }

    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            output.append("Error executing command.");
        }
        return output.toString();
    }
}