package foundation.map.tomtom;

import com.peertopark.java.geocalc.BoundingArea;
import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
import foundation.map.BoundingBox;

// Most of the methods here are copy-pasta from
// https://developer.tomtom.com/map-display-api/documentation/zoom-levels-and-tile-grid
public class TileGridUtils {
    public static double zoomLevelToTileSize(int z) {
        final double originalTileSize = 40075017;
        return originalTileSize / Math.pow(2, z);
    }

    public static Position<Integer> latLonToTileZXY(double lat, double lon, int z) {
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

    public static BoundingBox tileZXYToLatLonBBox(int z, int x, int y) {
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

    public static int getZoomLevelBySize(double sz) {
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

    public static BoundingBox getBoundingBox(double longitude, double latitude, double sz) {
        Point kew = new Point(new DegreeCoordinate(latitude), new DegreeCoordinate(longitude));
        BoundingArea area = EarthCalc.getBoundingArea(kew, sz / 2);

        return new BoundingBox(area.getWestLongitude(), area.getEastLongitude(),
                area.getSouthLatitude(), area.getNorthLatitude());
    }
}
