package net.voidhttp.response.cookie;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a HTTP response cookie.
 * For further information: https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies.
 */
public class Cookie {
    /**
     * The date format used for cookie expiration date formatting.
     */
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    /**
     * The name of the cookie.
     */
    private String name;

    /**
     * The value of the cookie.
     */
    private String value;

    /**
     * The expiration date of the cookie.
     */
    private Date expires;

    /**
     * The time interval determines how long the cookie exists.
     */
    private Integer maxAge;

    /**
     * The domain the cookie belongs to (including subdomains).
     */
    private String domain;

    /**
     * The url path that the cookie belongs to.
     */
    private String path;

    /**
     * Determine if the cookie should be only sent via HTTPS.
     */
    private boolean secure;

    /**
     * Determine if the cookie should be inaccessible to javascript.
     */
    private boolean httpOnly;

    /**
     * The cookie cross-site request rule.
     */
    private String sameSite;

    /**
     * Initialize response cookie.
     * @param name cookie name
     * @param value cookie value
     */
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Initialize empty response cookie.
     */
    public Cookie() {
    }

    /**
     * Get the name of the cookie.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the cookie.
     * @param name new cookie name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of the cookie.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value of the cookie.
     * @param value new cookie value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the expiration date of the cookie.
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * Set the expiration date of the cookie.
     * @param expires new cookie expiration date
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * Get the time interval determines how long the cookie exists.
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Set the time interval determines how long the cookie exists.
     * @param maxAge new cookie life length
     */
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Get the domain the cookie belongs to (including subdomains).
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set the domain the cookie belongs to (including subdomains).
     * @param domain new cookie domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get the url path that the cookie belongs to.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the url path that the cookie belongs to.
     * @param path new cookie path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Determine if the cookie should be only sent via HTTPS.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Determine if the cookie should be only sent via HTTPS.
     * @param secure only HTTPs is allowed
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Determine if the cookie should be inaccessible to javascript.
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Determine if the cookie should be inaccessible to javascript.
     * @param httpOnly cookie is inaccessible to javascript
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * Get the cookie cross-site request rule.
     */
    public String getSameSite() {
        return sameSite;
    }

    /**
     * Set the cookie cross-site request rule.
     * @param sameSite new cookie rule
     */
    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    /**
     * Parse the cookie data to string to be passed with the headers.
     * @return parsed cookie data
     */
    public String parse() {
        // set the name and value of the cookie
        StringBuilder builder = new StringBuilder(name).append("=").append(value);
        // set the expiration date of the cookie
        if (expires != null)
            builder.append("; Expires=").append(DATE_FORMAT.format(expires)).append(" GMT");
        // set the life length of the cookie
        if (maxAge != null)
            builder.append("; Max-Age=").append(maxAge);
        // set the domain of the cookie
        if (domain != null)
            builder.append("; Domain=").append(domain);
        // set the path of the cookie
        if (path != null)
            builder.append("; Path=").append(path);
        // set the http rule of the cookie
        if (secure)
            builder.append("; Secure");
        // set the javascript accessibility of the cookie
        if (httpOnly)
            builder.append("; HttpOnly");
        // set the cross-site request rule of the cookie
        if (sameSite != null)
            builder.append("; SameSite=").append(sameSite);
        return builder.toString();
    }

    /**
     * Debug the data of the cookie.
     */
    @Override
    public String toString() {
        return "Cookie{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", expires=" + expires +
            ", maxAge=" + maxAge +
            ", domain='" + domain + '\'' +
            ", path='" + path + '\'' +
            ", secure=" + secure +
            ", httpOnly=" + httpOnly +
            ", sameSite='" + sameSite + '\'' +
            '}';
    }

    /**
     * Get the date format used for cookie expiration date formatting.
     */
    public static SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    /**
     * Set the date format used for cookie expiration date formatting.
     * @param dateFormat new date format
     */
    public static void setDateFormat(SimpleDateFormat dateFormat) {
        DATE_FORMAT = dateFormat;
    }
}
