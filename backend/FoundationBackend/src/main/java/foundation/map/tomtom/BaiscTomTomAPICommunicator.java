package foundation.map.tomtom;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BaiscTomTomAPICommunicator implements TomTomAPICommunicator {
    private static final String API_KEY = "pGjCPMXkBdcms0zeNqcy7VHQGmoqnUC4";

    private HttpClient httpClient;

    public BaiscTomTomAPICommunicator() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private HttpResponse sendRequest(APIQuery query) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(query.toURL(API_KEY)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public BufferedImage getMapByGrid(int x, int y, int z) {
        HttpResponse response = null;
        try {
            response = sendRequest(MapImageAPIQuery.builder().x(x).y(y).z(z).build());
        } catch (IOException | InterruptedException e) {
            return null;
        }

        final int successStatusCode = 200;

        if (response.statusCode() == successStatusCode) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new ByteArrayInputStream((byte[]) response.body()));
            } catch (IOException e) {
                return null;
            }
            return image;
        } else {
            System.out.println("fak");
        }

        return null;
    }
}
