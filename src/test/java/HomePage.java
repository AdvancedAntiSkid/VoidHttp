import net.voidhttp.router.Middleware;
import net.voidhttp.request.Request;
import net.voidhttp.response.Response;

public class HomePage implements Middleware {
    /**
     * Handle the incoming HTTP request.
     * @param req client request
     * @param res server response
     */
    @Override
    public void handle(Request req, Response res) {
        res.status(418).message("I'm a teapot");
    }
}
