import net.voidhttp.HttpServer;
import net.voidhttp.util.JsonBuilder;

public class ServerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();

        server.get("/", (req, res) -> {
            System.out.println(req.parameters());
            res.send(new JsonBuilder("skid", true).build());
        });

        server.listen(80, () -> {
            System.out.println("Server started");
        });
    }
}
