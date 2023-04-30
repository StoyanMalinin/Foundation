package foundation.map.tomtom;

import com.peertopark.java.geocalc.*;
import foundation.map.BoundingBox;
import foundation.map.MapImageGetter;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TomTomMapImageGetter implements MapImageGetter {

    private static final String API_KEY = "pGjCPMXkBdcms0zeNqcy7VHQGmoqnUC4";

    private HttpClient httpClient;

    public TomTomMapImageGetter() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private HttpResponse sendRequest(APIQuery query) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(query.toURL(API_KEY)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public BufferedImage getMapByGrid(int x, int y, int z) throws Exception {

        System.out.println(x + ", " + y);

        HttpResponse response = sendRequest(MapImageAPIQuery.builder().x(x).y(y).z(z).build());

        final int successStatusCode = 200;

        if (response.statusCode() == successStatusCode) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream((byte[]) response.body()));
            return image;
        } else {
            System.out.println("fak");
        }

        return null;
    }

    public BufferedImage getMapByGrid(Position<Integer> position, int z) throws Exception {
        return getMapByGrid(position.x(), position.y(), z);
    }

    private static double zoomLevelToTileSize(int z) {
        final double originalTileSize = 40075017;
        return originalTileSize / Math.pow(2, z);
    }

    private static Position<Integer> latLonToTileZXY(double lat, double lon, int z) {
        final int MIN_ZOOM_LEVEL = 0;
        final int MAX_ZOOM_LEVEL = 22;
        final double MIN_LAT = -85.051128779807;
        final double MAX_LAT = 85.051128779806;
        final double MIN_LON = -180.0;
        final double MAX_LON = 180.0;

        if ((z < MIN_ZOOM_LEVEL) || (z > MAX_ZOOM_LEVEL)) {
            throw new IllegalArgumentException("Zoom level value is out of range [" +
                    Integer.toString(MIN_ZOOM_LEVEL) + ", " +
                    Integer.toString(MAX_ZOOM_LEVEL) + "]");
        }

        if (!Double.isFinite(lat) || (lat < MIN_LAT) || (lat > MAX_LAT)) {
            throw new IllegalArgumentException("Latitude value is out of range [" +
                    Double.toString(MIN_LAT) + ", " +
                    Double.toString(MAX_LAT) + "]");
        }

        if (!Double.isFinite(lon) || (lon < MIN_LON) || (lon > MAX_LON)) {
            throw new IllegalArgumentException("Longitude value is out of range [" +
                    Double.toString(MIN_LON) + ", " +
                    Double.toString(MAX_LON) + "]");
        }

        int xyTilesCount = (int) Math.pow(2, z);
        int x = (int) Math.floor((lon + 180.0) / 360.0 * xyTilesCount);
        int y = (int) Math.floor((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * xyTilesCount);

        return new Position(x, y);
    }

    private static BoundingBox tileZXYToLatLonBBox(int z, int x, int y) {
        final int MIN_ZOOM_LEVEL = 0;
        final int MAX_ZOOM_LEVEL = 22;

        if ((z < MIN_ZOOM_LEVEL) || (z > MAX_ZOOM_LEVEL)) {
            throw new IllegalArgumentException("Zoom level value is out of range [" +
                    Integer.toString(MIN_ZOOM_LEVEL) + ", " +
                    Integer.toString(MAX_ZOOM_LEVEL) + "]");
        }

        int minXY = 0;
        int maxXY = (int) (Math.pow(2, z) - 1);
        if ((x < minXY) || (x > maxXY)) {
            throw new IllegalArgumentException("Tile x value is out of range [" +
                    Integer.toString(minXY) + ", " +
                    Integer.toString(maxXY) + "]");
        }

        if ((y < 0) || (y > maxXY)) {
            throw new IllegalArgumentException("Tile y value is out of range [" +
                    Integer.toString(minXY) + ", " +
                    Integer.toString(maxXY) + "]");
        }

        double lon1 = (double) x / Math.pow(2, z) * 360.0 - 180.0;

        double n1 = Math.PI - 2.0 * Math.PI * (double) y / Math.pow(2, z);
        double lat1 = 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n1) - Math.exp(-n1)));

        double lon2 = (double) (x + 1) / Math.pow(2, z) * 360.0 - 180.0;

        double n2 = Math.PI - 2.0 * Math.PI * (double) (y + 1) / Math.pow(2, z);
        double lat2 = 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n2) - Math.exp(-n2)));

        return new BoundingBox(lon1, lon2, lat2, lat1);
    }

    private int getZoomLevelBySize(double sz) {
        int zoomLevel = 0;
        for (int i = 22; i >= 0; i--) {
            if (zoomLevelToTileSize(i) < sz) {
            } else {
                zoomLevel = i;
                break;
            }
        }

        return zoomLevel;
    }

    private static BoundingBox getBoundingBox(double longitude, double latitude, double sz) {
        Point kew = new Point(new DegreeCoordinate(latitude), new DegreeCoordinate(longitude));
        BoundingArea area = EarthCalc.getBoundingArea(kew, sz / 2);

        return new BoundingBox(area.getWestLongitude(), area.getEastLongitude(),
                area.getSouthLatitude(), area.getNorthLatitude());
    }

    @Override
    public BufferedImage getMap(double longitude, double latitude, double sz) throws Exception {
        BoundingBox boundingBox = getBoundingBox(longitude, latitude, sz);

        final int zoomLevel = getZoomLevelBySize(sz);
        final double tileSz = zoomLevelToTileSize(zoomLevel);

        Position<Integer>[][] tilePositions = new Position[2][2];
        tilePositions[1][0] = latLonToTileZXY(boundingBox.minY(), boundingBox.minX(), zoomLevel);
        tilePositions[1][1] = latLonToTileZXY(boundingBox.minY(), boundingBox.maxX(), zoomLevel);
        tilePositions[0][0] = latLonToTileZXY(boundingBox.maxY(), boundingBox.minX(), zoomLevel);
        tilePositions[0][1] = latLonToTileZXY(boundingBox.maxY(), boundingBox.maxX(), zoomLevel);

        int rowSz = 2;
        if (tilePositions[0][0].y().equals(tilePositions[1][0].y())) {
            rowSz = 1;
        }

        int colSz = 2;
        if (tilePositions[0][0].x().equals(tilePositions[0][1].x())) {
            colSz = 1;
        }

        BufferedImage[][] imgGrid = new BufferedImage[rowSz][colSz];
        for (int i = 0; i < rowSz; i++) {
            for (int j = 0; j < colSz; j++) {
                imgGrid[i][j] = getMapByGrid(tilePositions[i][j], zoomLevel);
            }
        }

        final int subImageSz = imgGrid[0][0].getHeight();
        BufferedImage img = new BufferedImage(colSz * subImageSz, rowSz * subImageSz, imgGrid[0][0].getType());
        for (int i = 0; i < rowSz; i++) {
            for (int j = 0; j < colSz; j++) {
                img.getSubimage(j * subImageSz, i * subImageSz, subImageSz, subImageSz)
                        .setData(imgGrid[i][j].getRaster());
            }
        }

        double imgMinX = tileZXYToLatLonBBox(zoomLevel, tilePositions[0][0].x(), tilePositions[0][0].y()).minX();
        double imgMaxX = tileZXYToLatLonBBox(zoomLevel, tilePositions[0][1].x(), tilePositions[0][1].y()).maxX();

        double imgMinY = tileZXYToLatLonBBox(zoomLevel, tilePositions[1][0].x(), tilePositions[1][0].y()).minY();
        double imgMaxY = tileZXYToLatLonBBox(zoomLevel, tilePositions[0][0].x(), tilePositions[0][0].y()).maxY();

        int minPixelX = (int) ((boundingBox.minX() - imgMinX) / (imgMaxX - imgMinX) * img.getWidth());
        int maxPixelX = (int) ((boundingBox.maxX() - imgMinX) / (imgMaxX - imgMinX) * img.getWidth());

        int maxPixelY = img.getHeight() - 1 - (int) ((boundingBox.minY() - imgMinY) / (imgMaxY - imgMinY) * img.getHeight());
        int minPixelY = img.getHeight() - 1 - (int) ((boundingBox.maxY() - imgMinY) / (imgMaxY - imgMinY) * img.getHeight());

        return img.getSubimage(minPixelX, minPixelY, (maxPixelX - minPixelX + 1), (maxPixelY - minPixelY + 1));
    }
}
