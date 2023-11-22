import net.voidhttp.HttpServer;
import net.voidhttp.request.form.FormEntry;

public class FormTest {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();

        server.get("/", (req, res) -> {
            res.send("Hello, world!");
        });

        server.post("/test", (req, res) -> {
            res.send("Request received!");

            for (FormEntry entry : req.multipart().entries())
                System.out.println(entry);
        });

        server.listen(80, () -> {
            System.out.println("Server started");
        });
    }
}
