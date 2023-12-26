package misc;

import net.voidhttp.request.session.RequestSession;
import net.voidhttp.request.session.Session;

public class SessionTest {
    public static void main(String[] args) {
        Session session = new RequestSession();
        session.set("aaa", true);
        session.set("123", 3.1415);

        System.out.println(session.<Boolean>get("aaa"));
        System.out.println(session.<Double>get("123").intValue());

        double a = session.get("123");
        System.out.println(a);
    }
}
