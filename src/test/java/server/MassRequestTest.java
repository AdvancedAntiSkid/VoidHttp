package server;

import dev.inventex.octa.random.Randomizer;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// very precise and accurate benchmarking tool
public class MassRequestTest {
    private static final String URL = "http://localhost:1234/test";

    private static final int MAX_REQUESTS = 100_000;

    private static int completed;
    private static int failed;
    private static int total;

    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < MAX_REQUESTS; i++)
                executor.submit(MassRequestTest::sendRequest);

            while (total < MAX_REQUESTS)
                Thread.sleep(1);

            long end = System.currentTimeMillis();

            System.out.println("Completed: " + completed + " | Failed: " + failed + " | Total: " + total);
            System.out.println("Time taken: " + (end - start) + "ms");
        }
    }

    @SneakyThrows
    private static void sendRequest() {
        try {
            String data = Randomizer.randomString(32);

            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes(StandardCharsets.UTF_8).length));
            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.getOutputStream().write(data.getBytes());

            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line);
            }

            completed++;
        } catch (Exception e) {
            failed++;
        } finally {
            total++;
        }
    }
}
