package net.voidhttp.request.session;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client request session cache.
 */
public class SessionManager {
    /**
     * The registry of the session data.
     */
    private final Map<String, SessionData> sessionMap;

    /**
     * Initialize session manager.
     * @param sessionMap session data
     */
    public SessionManager(Map<String, SessionData> sessionMap) {
        this.sessionMap = sessionMap;
    }

    /**
     * Initialize session manager.
     */
    public SessionManager() {
        this(new HashMap<>());
    }

    /**
     * Get the session of the given session token.
     * @param token session token
     * @return cached session
     */
    public Session getSession(String token) {
        return sessionMap.get(token).session;
    }

    /**
     * Determine if the given session exists.
     * @param token session token
     * @return true if the session exists
     */
    public boolean hasSession(String token) {
        return sessionMap.containsKey(token);
    }

    /**
     * Create a session with the given token and life length.
     * @param token session token
     * @param life session life length
     * @return created session
     */
    public Session createSession(String token, long life) {
        Session session = new RequestSession();
        sessionMap.put(token, new SessionData(session, life));
        return session;
    }

    /**
     * Remove a session with the given token.
     * @param token session token
     * @return true if the session was removed
     */
    public Session removeSession(String token) {
        SessionData data = sessionMap.remove(token);
        return data != null ? data.session : null;
    }

    /**
     * Remove the expired sessions from the cache.
     * @return true if any sessions were removed
     */
    public boolean removeOldSessions() {
        long now = System.currentTimeMillis();
        return sessionMap.entrySet().removeIf(entry -> {
            SessionData data = entry.getValue();
            return data.life > 0 && now - data.timestamp > data.life;
        });
    }

    /**
     * Represents a session data holder.
     */
    public static class SessionData {
        /**
         * The session object.
         */
        private final Session session;

        /**
         * The session life length.
         */
        private final long life;

        /**
         * The session creation timestamp.
         */
        private final long timestamp;

        /**
         * Initialize session data.
         * @param session session object
         * @param life session life length
         */
        public SessionData(Session session, long life) {
            this.session = session;
            this.life = life;
            timestamp = System.currentTimeMillis();
        }

        /**
         * Get the session object.
         */
        public Session getSession() {
            return session;
        }

        /**
         * Get the session life length.
         */
        public long getLife() {
            return life;
        }

        /**
         * Get the session creation timestamp.
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
