import Demo.Response;
import com.zeroc.Ice.Current;
import java.net.SocketException;
import java.util.function.Supplier;

public class PrinterI implements Demo.Printer {
    @Override
    public Response printString(String message, Current __current) {
        try {
            String[] splitMessage = message.split(":", 2);
            if (splitMessage.length < 2) {
                return new Response(0, "Invalid message format");
            }

            String userHost = splitMessage[0];
            String command = splitMessage[1].trim();
            CommandProcessor processor = getCommandProcessor(command);
            if (processor == null) {
                return new Response(0, "Unknown command");
            }
            long startTime = System.currentTimeMillis();
            String result = processor.process();
            long timetotal = System.currentTimeMillis() - startTime;
            System.out.println(userHost + ": " + processor.getDescription() + ": " + result);
            return new Response(timetotal, result);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(0, "Error processing message: " + e.getMessage());
        }
    }

    private CommandProcessor getCommandProcessor(String command) throws SocketException {
        if (command.matches("\\d+")) {
            int n = Integer.parseInt(command);
            return new CommandProcessor(
                    () -> Server.fibonacci(n) + " - Prime factors: " + Server.primeFactors(n),
                    "Fibonacci and prime factors for " + n
            );
        } else if (command.startsWith("listifs")) {
            return new CommandProcessor(() -> {
                try {
                    return Server.listInterfaces();
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }, "Network interfaces");
        } else if (command.startsWith("listports")) {
            String[] parts = command.split(" ", 2);
            if (parts.length > 1) {
                String ipAddress = parts[1];
                return new CommandProcessor(() -> Server.listPorts(ipAddress), "Open ports for " + ipAddress);
            }
        } else if (command.startsWith("!")) {
            String cmd = command.substring(1);
            return new CommandProcessor(() -> Server.executeCommand(cmd), "Command execution");
        } else if (command.startsWith("list clients")) {
            return new CommandProcessor(Server::listClients, "List clients");
        } else if (command.startsWith("to ")) {
            String[] parts = command.split(":", 2);
            String hostname = parts[0].substring(3).trim();
            String message = parts[1].trim();
            return new CommandProcessor(() -> {
                Server.sendMessageToClient(hostname, message);
                return "Message sent to " + hostname;
            }, "Send message to client " + hostname);
        } else if (command.startsWith("BC")) {
            String message = command.substring(2).trim();
            return new CommandProcessor(() -> {
                Server.broadcastMessage(message);
                return "Broadcast message sent";
            }, "Broadcast message");
        }
        return null;
    }

    private static class CommandProcessor {
        private final Supplier<String> processor;
        private final String description;

        CommandProcessor(Supplier<String> processor, String description) {
            this.processor = processor;
            this.description = description;
        }

        String process() {
            return processor.get();
        }

        String getDescription() {
            return description;
        }
    }
}