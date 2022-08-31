package net.voidhttp.request.session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client request session cache.
 */
public class CacheSessionManager implements SessionProvider {
    /**
     * The registry of the session data.
     */
    private final Map<String, SessionData> sessionMap;

    /**
     * Initialize session manager.
     * @param sessionMap session data
     */
    public CacheSessionManager(Map<String, SessionData> sessionMap) {
        this.sessionMap = sessionMap;
    }

    /**
     * Initialize session manager.
     */
    public CacheSessionManager() {
        this(new HashMap<>());
    }

    /**
     * Get the session data from the cache by it's session token.
     * @param token session token
     * @return null if the there is no session
     * with this token, or the session data
     */
    @Nullable
    @Override
    public Session getSession(String token) {
        SessionData data = sessionMap.get(token);
        return data != null ? data.session : null;
    }

    /**
     * Create a session in the cache with the given session token.
     * @param token session token
     * @param life session life length
     * @return new session data
     */
    @Nonnull
    @Override
    public Session createSession(String token, long life) {
        Session session = new RequestSession();
        sessionMap.put(token, new SessionData(session, life));
        return session;
    }

    /**
     * Create a session in the cache with the given session token.
     * @param token session token
     * @return new session data
     */
    @Nonnull
    @Override
    public Session createSession(String token) {
        return createSession(token, -1);
    }

    /**
     * Remove a session from cache by it's session token.
     * @param token session token
     * @return null if there was no session
     * with this token, or the removed session data
     */
    @Nullable
    @Override
    public Session removeSession(String token) {
        SessionData data = sessionMap.remove(token);
        return data != null ? data.session : null;
    }

    /**
     * Remove all the expired sessions from the cache.
     * @return true if any sessions were removed
     */
    @Override
    public boolean removeExpiredSessions() {
        long now = System.currentTimeMillis();
        return sessionMap.entrySet().removeIf(entry -> {
            SessionData data = entry.getValue();
            return data.life > 0 && now - data.timestamp > data.life;
        });
    }

    /**
     * Remove all sessions from the cache.
     */
    @Override
    public void clearSessions() {
        sessionMap.clear();
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
