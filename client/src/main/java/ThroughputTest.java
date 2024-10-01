import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThroughputTest {

    public static void main(String[] args) throws InterruptedException {
        int numRequests = 1;  // Número de peticiones
        int numThreads = 1;     // Número de hilos concurrentes

        // Medir el tiempo de inicio
        long startTime = System.nanoTime();

        // Crear un pool de hilos para realizar solicitudes concurrentes
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numRequests; i++) {
            executor.submit(() -> {
                try {
                    // Hacer una solicitud al servidor
                    sendRequestToServer("localhost", 9099);  // Ajusta la dirección y puerto
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Apagar el executor y esperar a que todas las tareas terminen
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        // Medir el tiempo de finalización
        long endTime = System.nanoTime();

        // Calcular el throughput
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        double throughput = numRequests / (durationInMillis / 1000.0);

        System.out.println("Throughput: " + throughput + " requests/second");
    }

    // Método para enviar una solicitud TCP al servidor
    public static void sendRequestToServer(String host, int port) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Enviar un mensaje al servidor (ajusta según la lógica de tu servidor)
            out.println("1");  // Cambia "your_request_here" por el comando que tu servidor espera

            // Leer la respuesta del servidor
            String response = in.readLine();
            System.out.println("Response: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
