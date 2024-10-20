package foundation.web;

import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Search;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TileGridUtils;
import spark.Request;
import spark.Response;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.URIParameter;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.time.Duration;
import java.time.Instant;

public class EndpointController {

    private FoundationDatabaseController dbController;
    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;

    public EndpointController(MapImageGetter mapImageGetter, FoundationDatabaseController dbController) {
        this.mapImageGetter = mapImageGetter;
        this.dbController = dbController;
        this.mapImageColorizer = new MapImageColorizer(dbController);
    }

    public String handleMapImage(Request request, Response response) {

        response.header("Access-Control-Allow-Origin", "*");

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
            return "Internal logic error " + e.getMessage();
        }

        if (search == null) {
            final int searchNotValidCode = 402;

            response.status(searchNotValidCode);
            return "Bad request - search not valid";
        }

        try {
            final int res = 10;
            final int currTimestamp = 0;

            Instant start = Instant.now();

            BufferedImage img = mapImageGetter.getMap(lon, lat, sz);

            Instant afterImageGetting = Instant.now();
            System.out.println("Getting the map image took: " + Duration.between(start, afterImageGetting).toMillis() + "ms");

            mapImageColorizer.colorizeImage(img, TileGridUtils.getBoundingBox(lon, lat, sz),
                    res, res, currTimestamp, search);

            Instant end = Instant.now();
            System.out.println("Colorizing the image took: " + Duration.between(afterImageGetting, end).toMillis() + "ms");

            ImageIO.write(img, "png", response.raw().getOutputStream());
        } catch (Exception e) {
            final int internalLogicError = 500;

            response.status(internalLogicError);
            return "Internal logic error " + e.getMessage();
        }

        return "Success";
    }

    public String handleMapTileImage(Request request, Response response) {
        response.header("Access-Control-Allow-Origin", "*");

        int searchId = 0, x = 0, y = 0, z = 0;
        try {
            searchId = Integer.parseInt(request.queryParams("searchId"));

            if (request.queryParams().contains("x")) {
                x = Integer.parseInt(request.queryParams("x"));
            }
            if (request.queryParams().contains("y")) {
                y = Integer.parseInt(request.queryParams("y"));
            }
            if (request.queryParams().contains("z")) {
                z = Integer.parseInt(request.queryParams("z"));
            }
        } catch (NumberFormatException e) {
            final int invalidNumericValueCode = 401;

            response.status(invalidNumericValueCode);
            return "Bad request - invalid numeric value";
        }

        BufferedImage img = mapImageGetter.getMapTile(x, y, z);
        if (img == null) {
            response.status(500);
            return "Internal server error - could not get map image";
        }

        /*
        Search search = null;
        try {
            search = dbController.getSearchById(searchId);
        } catch (SQLException e) {
            final int internalLogicError = 500;

            response.status(internalLogicError);
            return "Internal logic error " + e.getMessage();
        }
         */

        try {
//            mapImageColorizer.colorizeImage(img, TileGridUtils.tileZXYToLatLonBBox(z, x, y), 10, 10, 0, search);
            ImageIO.write(img, "png", response.raw().getOutputStream());
        } catch (Exception e) {
            response.status(500);
            return "Internal server error - could not colorize image";
        }

        response.status(200);
        return "Success";
    }
}
