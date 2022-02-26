import net.voidhttp.HttpServer;

public class ServerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();

        server.get("/", (req, res) -> {
            res.status(418).message("I'm a teapot");
        });

        server.listen(80, () -> {
            System.out.println("Server started");
        });
    }
}
