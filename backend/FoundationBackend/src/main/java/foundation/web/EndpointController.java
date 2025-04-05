package foundation.web;

import com.google.gson.Gson;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.SearchMetadata;
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
import java.util.List;

public class EndpointController {
    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;
    private FoundationDatabaseController dbController;

    public EndpointController(MapImageGetter mapImageGetter, FoundationDatabaseController dbController) {
        this.mapImageGetter = mapImageGetter;
        this.mapImageColorizer = new MapImageColorizer(dbController);
        this.dbController = dbController;
    }

    public boolean handleMapTileImage(Request request, Response response, Callback callback) {
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

    public boolean handleGetSearchesMetadata(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        Gson gson = new Gson();

        List<SearchMetadata> searchMetadataList = null;
        try {
            searchMetadataList = dbController.getSearchesMetadata();
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get searches metadata: " + e.getMessage(), callback);

            return true;
        }

        String json = gson.toJson(searchMetadataList);

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, json, callback);

        return true;
    }
}
