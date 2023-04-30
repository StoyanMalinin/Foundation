package foundation.map;

import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Presence;
import foundation.database.structure.Search;
import foundation.map.tomtom.Position;
import foundation.map.tomtom.TileGridUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapImageColorizer {
    private Search search;
    private FoundationDatabaseController dbController;

    public MapImageColorizer(FoundationDatabaseController dbController, Search search) {
        this.search = search;
        this.dbController = dbController;
    }

    private static double distanceDecayFunction(double d) {
        final double k = 1;
        return 1.0 / (1.0  + k * d);
    }

    private static double timeDecayFunction(double t) {
        final double k = 0;
        return 1.0 / (1.0  + k * t);
    }

    private double evalCoef(double x, double y, long currTimestamp) throws Exception {
        final double searchSz = 1e4;

        BoundingBox boundingBox = TileGridUtils.getBoundingBox(x, y, searchSz);
        //List<Presence> presences = dbController.getAllPresencesOfSearchInsideBoundingBox(search.id(),
        //        boundingBox.minX(), boundingBox.maxX(),
        //        boundingBox.minY(), boundingBox.maxY());
        List<Presence> presences = dbController.getAllPresencesOfSearch(search.id());

        return presences.stream()
                .map(p ->
                        MapImageColorizer.distanceDecayFunction(
                            EarthCalc.getDistance(
                                    new Point(
                                            new DegreeCoordinate(p.posId().minY()),
                                            new DegreeCoordinate(p.posId().minX())),
                                    new Point(
                                            new DegreeCoordinate(y),
                                            new DegreeCoordinate(x)))) *
                        MapImageColorizer.timeDecayFunction(currTimestamp - p.timestamp()))
                .reduce(0.0, Double::sum);
    }

    public void colorizeImage(BufferedImage img, BoundingBox imgBoundingBox, int vertRes, int horRes, long currTimestamp) throws Exception {
        final int zoomLevel = 20;

        double[][] tileCoef = new double[vertRes][horRes];

        for (int i = 0; i < vertRes; i++) {
            for (int j = 0; j < horRes; j++) {
                double posX = imgBoundingBox.minX()
                        + (imgBoundingBox.maxX() - imgBoundingBox.minX()) / horRes * (j + 0.5);
                double posY = imgBoundingBox.minY()
                        + (imgBoundingBox.maxY() - imgBoundingBox.minY()) / vertRes * (i + 0.5);

                Position<Integer> tilePosition = TileGridUtils.latLonToTileZXY(posY, posX, zoomLevel);
                BoundingBox tileBoundingBox = TileGridUtils.tileZXYToLatLonBBox(zoomLevel,
                        tilePosition.x(), tilePosition.y());

                double queryPosX = (tileBoundingBox.minX() + tileBoundingBox.maxX()) / 2;
                double queryPosY = (tileBoundingBox.minY() + tileBoundingBox.maxY()) / 2;
                double coef = evalCoef(queryPosX, queryPosY, currTimestamp);

                tileCoef[i][j] = coef;
            }
        }

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int tileRow = i / (img.getHeight() / vertRes + 1);
                int tileCol = j / (img.getWidth() / horRes + 1);

                double coef = tileCoef[tileRow][tileCol];
                int red = (int) (Math.min(coef, 1) * 255);

                Color c = new Color(img.getRGB(j, i));
                c = new Color(red, c.getBlue() / 2, c.getGreen() / 2);

                img.setRGB(j, i, c.getRGB());
            }
        }
    }
}
