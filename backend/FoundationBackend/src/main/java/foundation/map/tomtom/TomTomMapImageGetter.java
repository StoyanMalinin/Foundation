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

    @Override
    public BufferedImage getMap(double longitude, double latitude, double sz) throws Exception {
        BoundingBox boundingBox = TileGridUtils.getBoundingBox(longitude, latitude, sz);

        final int zoomLevel = TileGridUtils.getZoomLevelBySize(sz);
        final double tileSz = TileGridUtils.zoomLevelToTileSize(zoomLevel);

        Position<Integer>[][] tilePositions = new Position[2][2];
        tilePositions[1][0] = TileGridUtils.latLonToTileZXY(boundingBox.minY(), boundingBox.minX(), zoomLevel);
        tilePositions[1][1] = TileGridUtils.latLonToTileZXY(boundingBox.minY(), boundingBox.maxX(), zoomLevel);
        tilePositions[0][0] = TileGridUtils.latLonToTileZXY(boundingBox.maxY(), boundingBox.minX(), zoomLevel);
        tilePositions[0][1] = TileGridUtils.latLonToTileZXY(boundingBox.maxY(), boundingBox.maxX(), zoomLevel);

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

        double imgMinX = TileGridUtils.tileZXYToLatLonBBox(zoomLevel,
                tilePositions[0][0].x(), tilePositions[0][0].y()).minX();
        double imgMaxX = TileGridUtils.tileZXYToLatLonBBox(zoomLevel,
                tilePositions[0][1].x(), tilePositions[0][1].y()).maxX();

        double imgMinY = TileGridUtils.tileZXYToLatLonBBox(zoomLevel,
                tilePositions[1][0].x(), tilePositions[1][0].y()).minY();
        double imgMaxY = TileGridUtils.tileZXYToLatLonBBox(zoomLevel,
                tilePositions[0][0].x(), tilePositions[0][0].y()).maxY();

        int minPixelX = (int) ((boundingBox.minX() - imgMinX) / (imgMaxX - imgMinX) * img.getWidth());
        int maxPixelX = (int) ((boundingBox.maxX() - imgMinX) / (imgMaxX - imgMinX) * img.getWidth());

        int maxPixelY = img.getHeight() - 1 -
                (int) ((boundingBox.minY() - imgMinY) / (imgMaxY - imgMinY) * img.getHeight());
        int minPixelY = img.getHeight() - 1 -
                (int) ((boundingBox.maxY() - imgMinY) / (imgMaxY - imgMinY) * img.getHeight());

        return img.getSubimage(minPixelX, minPixelY, (maxPixelX - minPixelX + 1), (maxPixelY - minPixelY + 1));
    }
}