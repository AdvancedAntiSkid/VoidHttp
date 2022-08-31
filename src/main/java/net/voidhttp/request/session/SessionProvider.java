package net.voidhttp.request.session;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a request session provider interface.
 */
public interface SessionProvider {
    /**
     * Get the session data from the cache by it's session token.
     * @param token session token
     * @return null if the there is no session
     * with this token, or the session data
     */
    @Nullable
    Session getSession(String token);

    /**
     * Create a session in the cache with the given session token.
     * @param token session token
     * @param life session life length
     * @return new session data
     */
    @Nonnull
    Session createSession(String token, long life);

    /**
     * Create a session in the cache with the given session token.
     * @param token session token
     * @return new session data
     */
    @Nonnull
    Session createSession(String token);

    /**
     * Remove a session from cache by it's session token.
     * @param token session token
     * @return null if there was no session
     * with this token, or the removed session data
     */
    @Nullable
    Session removeSession(String token);

    /**
     * Remove all the expired sessions from the cache.
     * @return true if any sessions were removed
     */
    boolean removeExpiredSessions();

    /**
     * Remove all sessions from the cache.
     */
    void clearSessions();
}
