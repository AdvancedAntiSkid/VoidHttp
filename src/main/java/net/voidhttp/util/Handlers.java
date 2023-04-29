package net.voidhttp.util;

import net.voidhttp.router.Middleware;
import net.voidhttp.util.asset.Asset;
import net.voidhttp.util.asset.MIMEType;
import net.voidhttp.util.asset.Resource;

/**
 * Represents an utility to create handlers.
 */
public final class Handlers {
    /**
     * Create a static folder handler.
     * @param folder static folder path
     * @param cache cache asset content
     */
    public static Middleware staticFolder(String folder, boolean cache) {
        // create a new handler for the resource files
        return (req, res) -> {
            // get the requested url
            String route = req.route();
            // move to the next handler if the route is not an asset file request
            if (!route.startsWith(folder + "/")) {
                req.next();
                return;
            }
            // split up url between every '/' char
            String[] parts = route.split("/");
            // get the last part of the url
            String file = parts[parts.length - 1];
            // split up filename and extension
            parts = file.split("\\.");
            // get file extension
            String extension = "." + parts[parts.length - 1];
            // get the MIME type of the file
            MIMEType type = MIMEType.fromExtensionOrDefault(extension, MIMEType.PLAIN_TEXT);
            // get the content of the asset file
            route = '.' + route;
            byte[] bytes = cache ? Asset.get(route) : Asset.load(route);
            if (bytes == null) {
                res.status(404).send("<pre>" + "Cannot " + req.method() + " " + req.route() + "</pre>");
                return;
            }
            // send the content of the asset
            res.send(bytes, type);
        };
    }

    /**
     * Create a static folder handler.
     * @param folder static folder path
     */
    public static Middleware staticFolder(String folder) {
        return staticFolder(folder, false);
    }

    /**
     * Create a static resources handler.
     * @param path static resources path
     */
    public static Middleware staticResources(String path, boolean cache) {
        // create a new handler for the resource files
        return (req, res) -> {
            // get the requested url
            String route = req.route();
            // move to the next handler if the route is not a resource file request
            if (!route.startsWith(path + "/")) {
                req.next();
                return;
            }
            // split up url between every '/' char
            String[] parts = route.split("/");
            // get the last part of the url
            String file = parts[parts.length - 1];
            // split up filename and extension
            parts = file.split("\\.");
            // get file extension
            String extension = "." + parts[parts.length - 1];
            // get the MIME type of the file
            MIMEType type = MIMEType.fromExtensionOrDefault(extension, MIMEType.PLAIN_TEXT);
            // get the content of the resource file
            byte[] bytes = cache ? Resource.get(route) : Resource.load(route);
            // send the content of the resource
            res.send(bytes, type);
        };
    }

    /**
     * Create a static resources handler.
     * @param path static resources path
     */
    public static Middleware staticResources(String path) {
        return staticResources(path, false);
    }

    private Handlers() {
    }
}
