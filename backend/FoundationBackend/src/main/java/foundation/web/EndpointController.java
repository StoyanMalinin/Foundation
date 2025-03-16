package foundation.web;

import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Search;
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
import java.sql.SQLException;

public class EndpointController {

    private FoundationDatabaseController dbController;
    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;

    public EndpointController(MapImageGetter mapImageGetter, FoundationDatabaseController dbController) {
        this.mapImageGetter = mapImageGetter;
        this.dbController = dbController;
        this.mapImageColorizer = new MapImageColorizer(dbController);
    }

    public boolean handleMapTileImage(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "http://localhost:3000");
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

        Search search = null;
        try {
            search = dbController.getSearchById(searchId);
        } catch (SQLException e) {
            final int internalLogicError = 500;

            response.setStatus(internalLogicError);
            Content.Sink.write(response, true, "Internal logic error " + e.getMessage(), callback);

            return true;
        }

        try {
            Search finalSearch = search;
            int finalZ = z;
            int finalX = x;
            int finalY = y;
            PerformanceUtils.logDuration(() -> {
                try {
                    mapImageColorizer.colorizeImage(img, TileGridUtils.tileZXYToLatLonBBox(finalZ, finalX, finalY),
                            10, 10, 0, finalSearch);
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
            Content.Sink.write(response, true, "Internal server error - could not colorize image", callback);

            return true;
        }
    }
}
