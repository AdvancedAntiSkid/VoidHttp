import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.voidhttp.HttpServer;
import net.voidhttp.controller.ControllerInjector;
import net.voidhttp.controller.dto.*;
import net.voidhttp.controller.handler.Body;
import net.voidhttp.controller.route.Controller;
import net.voidhttp.controller.route.Post;
import net.voidhttp.controller.validator.IsStrongPassword;
import net.voidhttp.controller.validator.Length;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ControllerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();

        ControllerInjector injector = new ControllerInjector();
        injector.inject(server, new TestController());

        server.listenAsync(80, () -> System.out.println("Listening on port 80"));

        String endpoint = "auth/login?x=100";
        String data = "{\"username\":\"admin\",\"password\":\"S3curePa$$w0rd\"}";
        String response = postJson(endpoint, data);

        System.out.println(response);
    }

    @Dto
    @Getter
    @ToString
    public static class LoginRequest {
        @Length(min = 1, max = 10)
        private String username;

        @IsStrongPassword
        private String password;
    }

    @Dto
    @AllArgsConstructor
    @ToString
    public static class LoginResponse {
        private boolean success;
        private String message;
    }

    @Controller("auth")
    public static class TestController {
        @Post("login")
        public LoginResponse login(@Body LoginRequest data) {
            System.out.println("try login " + data);
            return new LoginResponse(true, "success");
        }
    }

    private static String postJson(String endpoint, String data) throws Exception {
        HttpURLConnection connection = getHttpURLConnection(endpoint, data);

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
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
