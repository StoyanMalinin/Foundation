package foundation.map.tomtom;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CachedTomTomAPICommunicator implements TomTomAPICommunicator {
    private TomTomAPICommunicator api;
    private JedisPool jedisPool;

    private static final int JEDIS_PORT = 2000;

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
    public BufferedImage getMapByGrid(int x, int y, int z) throws Exception {
        BufferedImage img = null;
        byte[] key = gridQueryToByteArray(x, y, z);

        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.exists(key)) {
                img = ImageIO.read(new ByteArrayInputStream(jedis.get(key)));
            }
        }

        if (img == null) {
            img = api.getMapByGrid(x, y, z);
        }

        return null;
    }
}
