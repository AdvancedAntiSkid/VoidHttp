package misc;

import lombok.SneakyThrows;
import net.voidhttp.HttpServer;
import net.voidhttp.header.Headers;
import net.voidhttp.header.HttpHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UnicodeTest {
    @SneakyThrows
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        server.post("/test", (req, res) -> {
            System.out.println("request body: " + req.body());
            res.send("hello world");
        });

        server.listen(80);

        postJson("test", "áéíóú");
    }

    public static String postJson(String endpoint, String data) throws Exception {
        return postJson(endpoint, data, HttpHeaders.empty());
    }

    public static String postJson(String endpoint, String data, Headers headers) throws Exception {
        HttpURLConnection connection = getHttpURLConnection(endpoint, data);

        for (Map.Entry<String, String> entry : headers.getHeaders().entrySet())
            connection.setRequestProperty(entry.getKey(), entry.getValue());

        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(data.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                response.append(line);
        }
        return response.toString();
    }

    private static HttpURLConnection getHttpURLConnection(String endpoint, String data) throws IOException {
        URL url = new URL("http://localhost/" + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", data.getBytes(StandardCharsets.UTF_8).length + "");

        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
