package foundation.web;

import foundation.database.FoundationDatabaseController;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TileGridUtils;
import observability.PerformanceUtils;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Callback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class EndpointController {
    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;

    public EndpointController(MapImageGetter mapImageGetter, FoundationDatabaseController dbController) {
        this.mapImageGetter = mapImageGetter;
        this.mapImageColorizer = new MapImageColorizer(dbController);
    }

    public boolean handleMapTileImage(Request request, Response response, Callback callback) {
        if (request.getMethod().equals("OPTIONS")) {
            response.getHeaders().put("Access-Control-Allow-Origin", "*");
            response.getHeaders().put("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");
            response.getHeaders().put("Access-Control-Allow-Credentials", "true");

            response.setStatus(200);
            callback.succeeded();
            return true;
        }

        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        Fields queryParams = null;
        try {
            queryParams = Request.getParameters(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int searchId = 0, x = 0, y = 0, z = 0;
        try {
            searchId = Integer.parseInt(queryParams.getValue("searchId"));

                x = Integer.parseInt(queryParams.getValue("x"));
                y = Integer.parseInt(queryParams.getValue("y"));
                z = Integer.parseInt(queryParams.getValue("z"));
        } catch (NumberFormatException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Bad request - invalid numeric value", callback);

            return true;
        }

        BufferedImage img = mapImageGetter.getMapTile(x, y, z);
        if (img == null) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get map image", callback);

            return true;
        }

        try {
            int finalSearchId = searchId;
            int finalZ = z;
            int finalX = x;
            int finalY = y;
            PerformanceUtils.logDuration(() -> {
                try {
                    mapImageColorizer.colorizeImage(img, TileGridUtils.tileZXYToLatLonBBox(finalZ, finalX, finalY),
                            3, 3, 0, finalSearchId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "colorize image");

            ImageIO.write(img, "png", Content.Sink.asOutputStream(response));
            callback.succeeded();
            response.setStatus(200);

            return true;

        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not colorize image" + e.getMessage(), callback);

            return true;
        }
    }
}
