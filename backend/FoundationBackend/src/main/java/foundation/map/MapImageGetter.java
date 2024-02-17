package foundation.map;

import java.awt.image.BufferedImage;

public interface MapImageGetter {
    BufferedImage getMap(double longitude, double latitude, double sz) throws Exception;
    BufferedImage getMapTile(int x, int y, int z);
}
