package foundation.map.tomtom;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CachedTomTomAPICommunicator implements TomTomAPICommunicator {
    private TomTomAPICommunicator api;
    private JedisPool jedisPool;

    private static final int JEDIS_PORT = 6379;

    public CachedTomTomAPICommunicator(TomTomAPICommunicator api) {
        this.api = api;
        this.jedisPool = new JedisPool("localhost", JEDIS_PORT);
    }

    private byte[] gridQueryToByteArray(int x, int y, int z) {
        return (x + "$" + y + "$" + z).getBytes();
    }

    private byte[] bufferedImageToByteArray(BufferedImage img) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", stream);
        } catch (IOException e) {

        }

        return stream.toByteArray();
    }

    @Override
    public BufferedImage getMapByGrid(int x, int y, int z) {
        BufferedImage img = null;
        byte[] key = gridQueryToByteArray(x, y, z);

        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.exists(key)) {
                img = ImageIO.read(new ByteArrayInputStream(jedis.get(key)));
            }
            else {
                System.out.println("Cache miss(" + x + "," + y + "," + z + ")");

                img = api.getMapByGrid(x, y, z);
                jedis.set(key, bufferedImageToByteArray(img));
            }
        } catch (Exception e) {
            System.out.println("Redis exception: " + e.getMessage());
            img = api.getMapByGrid(x, y, z);
        }

        return img;
    }
}
