import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestTest {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://127.0.0.1");

        URLConnection connection = url.openConnection();
        connection.setDoInput(true);

        InputStream inputStream = connection.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        String builder = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        Map<String, List<String>> headerFields = connection.getHeaderFields();

        System.out.println("Response Header:" + headerFields);
        System.out.println("Response Content: " + builder);
    }
}
