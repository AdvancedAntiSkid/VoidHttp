import net.voidhttp.HttpServer;
import net.voidhttp.config.Flag;
import net.voidhttp.util.console.Logger;

public class ServerTest {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.enableFlags(Flag.NO_STACK_TRACE);

        server.get("/", (req, res) -> {
            res.send("Hello, World!");
        });

        server.get("/failed", (req, res) -> {
            throw new IllegalStateException("Example error");
        });

        server.get("/admin", (req, res) -> {
            res.status(418).message("I'm a teapot");
        });

        server.listen(80, () -> Logger.success("Webserver has been started"));
    }
}
