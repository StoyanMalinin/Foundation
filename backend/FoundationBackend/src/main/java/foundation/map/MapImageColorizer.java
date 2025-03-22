package foundation.map;

import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Presence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MapImageColorizer {
    private FoundationDatabaseController dbController;
    private ExecutorService executorService;


    public MapImageColorizer(FoundationDatabaseController dbController) {
        this.dbController = dbController;
        this.executorService = Executors.newFixedThreadPool(20);
    }

    private static double distanceDecayFunction(double d) {
        final double k = 1;
        return 1.0 / (1.0  + k * d);
    }

    private static double timeDecayFunction(double t) {
        final double k = 0;
        return 1.0 / (1.0  + k * t);
    }

    private double evalCoef(List<Presence> presences, long currTimestamp, double x, double y) {
        return presences.stream()
                .map(p ->
                        MapImageColorizer.distanceDecayFunction(
                                EarthCalc.getDistance(
                                        new Point(new DegreeCoordinate(p.posId().minY()), new DegreeCoordinate(p.posId().minX())),
                                        new Point(new DegreeCoordinate(y), new DegreeCoordinate(x)))) *
                                MapImageColorizer.timeDecayFunction(currTimestamp - p.timestamp()))
                .reduce(0.0, Double::sum);
    }

    public void colorizeImage(BufferedImage img, BoundingBox imgBoundingBox,
                              int vertRes, int horRes, long currTimestamp, int searchId) throws Exception {
        double[][] tileCoef = new double[vertRes][horRes];
        double horizontalQueryBuffer = 0.5;
        double verticalQueryBuffer = 0.5;

        List<Presence> presences = dbController.getAllPresencesOfSearchInsideBoundingBox(searchId,
                imgBoundingBox.minX() - horizontalQueryBuffer, imgBoundingBox.maxX() + horizontalQueryBuffer,
                imgBoundingBox.minY() - verticalQueryBuffer, imgBoundingBox.maxY() + verticalQueryBuffer);

        for (int i = 0; i < vertRes; i++) {
            for (int j = 0; j < horRes; j++) {
                double x = (j + 0.5) * (imgBoundingBox.maxX() - imgBoundingBox.minX()) / horRes + imgBoundingBox.minX();
                double y = ((vertRes - i - 1) + 0.5) * (imgBoundingBox.maxY() - imgBoundingBox.minY()) / vertRes + imgBoundingBox.minY();
                tileCoef[i][j] = evalCoef(presences, currTimestamp, x, y);
            }
        }

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int tileRow = i / (img.getHeight() / vertRes + 1);
                int tileCol = j / (img.getWidth() / horRes + 1);

                double coef = tileCoef[tileRow][tileCol];
                int red = (int) (Math.min(coef, 1) * 255);

                Color c = new Color(img.getRGB(j, i));
                c = new Color((int) (red * 0.4 + c.getRed() * 0.6), c.getGreen(), c.getBlue());

                img.setRGB(j, i, c.getRGB());
            }
        }
    }
}