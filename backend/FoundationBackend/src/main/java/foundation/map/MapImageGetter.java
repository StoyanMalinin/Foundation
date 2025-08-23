package main.java.foundation.map;

import java.awt.image.BufferedImage;

public interface MapImageGetter {
    BufferedImage getMapTile(int x, int y, int z);
}
