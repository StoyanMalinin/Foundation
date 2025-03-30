package foundation.map;

import com.peertopark.java.geocalc.DegreeCoordinate;
import com.peertopark.java.geocalc.EarthCalc;
import com.peertopark.java.geocalc.Point;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.Presence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MapImageColorizer {
    private FoundationDatabaseController dbController;


    public MapImageColorizer(FoundationDatabaseController dbController) {
        this.dbController = dbController;
    }

    private static double distanceDecayFunction(double d) {
        final double k = 0.03;
        return 1.0 / (1.0  + k * d);
    }

    private static double timeDecayFunction(double t) {
        final double k = 0;
        return 1.0 / (1.0  + k * t);
    }

    // this is an attempt to produce a coefficient [0, 1] from to [0, 1] coefficients,
    // so that both coefficients are taken into account.
    private static double coefCombiner(double a, double b, double weightA, double weightB) {
        return Math.pow(Math.pow(a, weightA) * Math.pow(b, weightB), 1.0 / (weightA + weightB));
    }

    // this is an attempt to apply a coefficient to a value, so that the value goes to up when the coefficient goes up
    // both are between [0, 1]
    private static double coefApplier(double x, double goalX, double coef) {
        double delta = goalX - x;
        return x + delta * coef;
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double evalCoef(List<Presence> presences, long currTimestamp, double x, double y) {
        final double presenceWeightCoef = 0.02;
        double sumCoef = presences.stream()
                        .map(p ->
                                coefCombiner(
                                    MapImageColorizer.distanceDecayFunction(
                                            EarthCalc.getDistance(
                                                    new Point(new DegreeCoordinate(p.y()), new DegreeCoordinate(p.x())),
                                                    new Point(new DegreeCoordinate(y), new DegreeCoordinate(x))
                                            )
                                    ),
                                    MapImageColorizer.timeDecayFunction(currTimestamp - p.timestamp()),
                            0.99, 0.01
                                )
                        )
                        .reduce(0.0, Double::sum);

        return (sigmoid(sumCoef * presenceWeightCoef) - 0.5) * 2; // sumCoef is only positive
    }

    public void colorizeImage(BufferedImage img, BoundingBox imgBoundingBox,
                              int vertRes, int horRes, long currTimestamp, int searchId) throws Exception {
        double[][] tileCoef = new double[vertRes][horRes];
        final double horizontalQueryBuffer = 0.1;
        final double verticalQueryBuffer = 0.1;

        Instant startTime = Instant.now();
        List<Presence> presences = dbController.getAllPresencesOfSearchInsideBoundingBox(searchId,
                imgBoundingBox.minX() - horizontalQueryBuffer, imgBoundingBox.maxX() + horizontalQueryBuffer,
                imgBoundingBox.minY() - verticalQueryBuffer, imgBoundingBox.maxY() + verticalQueryBuffer);
        Instant endTime = Instant.now();
        System.out.println("DB time: " + (endTime.toEpochMilli() - startTime.toEpochMilli()) + "ms");

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

                Color c = new Color(img.getRGB(j, i));
                c = new Color(
                        (int) (coefApplier(c.getRed() / 256.0, 1, coef) * 255),
                        (int) (coefApplier(c.getGreen() / 256.0, 0, coef) * 255),
                        (int) (coefApplier(c.getBlue() / 256.0, 0, coef) * 255));

                img.setRGB(j, i, c.getRGB());
            }
        }
    }
}