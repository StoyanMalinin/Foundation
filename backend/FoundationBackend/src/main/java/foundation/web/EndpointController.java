package foundation.web;

import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Search;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TileGridUtils;
import spark.Request;
import spark.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.URIParameter;
import java.sql.SQLException;

public class EndpointController {

    private FoundationDatabaseController dbController;
    private MapImageGetter mapImageGetter;

    public EndpointController(MapImageGetter mapImageGetter, FoundationDatabaseController dbController) {
        this.mapImageGetter = mapImageGetter;
        this.dbController = dbController;
    }

    public String handleMapImage(Request request, Response response) {

        double sz = 1;
        double lat = 0;
        double lon = 0;

        int searchId = -1;

        if (!request.queryParams().contains("searchId")) {
            final int invalidNumericValueCode = 402;

            response.status(invalidNumericValueCode);
            return "Bad request - no searchId parameter found";
        }

        try {
            searchId = Integer.parseInt(request.queryParams("searchId"));

            if (request.queryParams().contains("sz")) {
                sz = Double.parseDouble(request.queryParams("sz"));
            }
            if (request.queryParams().contains("lat")) {
                lat = Double.parseDouble(request.queryParams("lat"));
            }
            if (request.queryParams().contains("lon")) {
                lon = Double.parseDouble(request.queryParams("lon"));
            }
        } catch (NumberFormatException e) {
            final int invalidNumericValueCode = 401;

            response.status(invalidNumericValueCode);
            return "Bad request - invalid numeric value";
        }

        Search search = null;
        try {
            search = dbController.getSearchById(searchId);
        } catch (SQLException e) {
            final int internalLogicError = 500;

            response.status(internalLogicError);
            return "Internal logic error";
        }

        if (search == null) {
            final int searchNotValidCode = 402;

            response.status(searchNotValidCode);
            return "Bad request - search not valid";
        }

        try {
            final int res = 10;
            final int currTimestamp = 0;

            BufferedImage img = mapImageGetter.getMap(lon, lat, sz);
            (new MapImageColorizer(dbController, search))
                    .colorizeImage(img, TileGridUtils.getBoundingBox(lon, lat, sz), res, res, currTimestamp);

            ImageIO.write(img, "png", response.raw().getOutputStream());
        } catch (Exception e) {
            final int internalLogicError = 500;

            response.status(internalLogicError);
            return "Internal logic error";
        }

        return "Success";
    }
}
