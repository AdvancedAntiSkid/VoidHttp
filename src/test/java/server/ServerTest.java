package server;

import dev.inventex.octa.console.Logger;
import net.voidhttp.HttpServer;

public class ServerTest {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();
        server.getConfig().setSendStackTrace(false);

        server.get("/", (req, res) -> {
            // System.out.println("Received request");
            res.send("Hello, World!");
        });

        server.post("/test", (req, res) -> {
            res.send("Example post response!");
        });

        server.get("/failed", (req, res) -> {
            throw new IllegalStateException("Example error");
        });

        server.get("/admin", (req, res) -> {
            res.status(418).message("I'm a teapot");
        });

        server.listen(1234, () -> {
            Logger.success("Webserver has been started");
        });

        System.in.read();
    }
}
