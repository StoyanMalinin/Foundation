package main.java.foundation.map.tomtom;

import main.java.foundation.map.MapImageGetter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BaiscTomTomAPICommunicator implements MapImageGetter {
    private final HttpClient httpClient;
    private final String apiKey;

    public BaiscTomTomAPICommunicator(String apiKey) {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
    }

    private HttpResponse sendRequest(APIQuery query) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(query.toURL(this.apiKey)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    @Override
    public BufferedImage getMapTile(int x, int y, int z) {
        HttpResponse response = null;
        try {
            response = sendRequest(MapImageAPIQuery.builder().x(x).y(y).z(z).build());
        } catch (IOException | InterruptedException e) {
            return null;
        }

        if (response.statusCode() == 200) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new ByteArrayInputStream((byte[]) response.body()));
            } catch (IOException e) {
                return null;
            }
            return image;
        } else {
            System.out.println("Error from TomTom API: " + response.statusCode() + " | " + response.body());
        }

        return null;
    }
}
