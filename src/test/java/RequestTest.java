import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class RequestTest {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://127.0.0.1");
        URLConnection connection = url.openConnection();
        connection.setDoInput(true);

        InputStream inputStream = connection.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader reader = new BufferedReader(streamReader);

        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        Map<String, List<String>> headerFields = connection.getHeaderFields();
        System.out.println(headerFields);

        System.out.println(builder);
    }
}
