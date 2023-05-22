package foundation.map.tomtom;

import java.awt.image.BufferedImage;

public interface TomTomAPICommunicator {
    BufferedImage getMapByGrid(int x, int y, int z) throws Exception;
}
