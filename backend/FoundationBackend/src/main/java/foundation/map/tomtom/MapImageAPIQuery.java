package main.java.foundation.map.tomtom;

public class MapImageAPIQuery implements APIQuery {

    enum Layer {
        basic,
        hybrid,
        labels,
    }

    enum Style {
        main,
        night
    }

    enum Format {
        png,
        jpg
    }

    private int x;
    private int y;
    private int z;
    private Layer layer;
    private Style style;
    private Format format;

    private MapImageAPIQuery(MapImageAPIQueryBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.layer = builder.layer;
        this.style = builder.style;
        this.format = builder.format;
    }

    public static class MapImageAPIQueryBuilder {
        private int x;
        private int y;
        private int z;
        private Layer layer;
        private Style style;
        private Format format;

        private MapImageAPIQueryBuilder() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.layer = Layer.basic;
            this.style = Style.main;
            this.format = Format.png;
        }

        public MapImageAPIQueryBuilder x(int x) {
            this.x = x;
            return this;
        }
        public MapImageAPIQueryBuilder y(int y) {
            this.y = y;
            return this;
        }
        public MapImageAPIQueryBuilder z(int z) {
            this.z = z;
            return this;
        }
        public MapImageAPIQueryBuilder layer(Layer layer) {
            this.layer = layer;
            return this;
        }
        public MapImageAPIQueryBuilder style(Style style) {
            this.style = style;
            return this;
        }
        public MapImageAPIQueryBuilder format(Format format) {
            this.format = format;
            return this;
        }

        public MapImageAPIQuery build() {
            return new MapImageAPIQuery(this);
        }
    }

    static public MapImageAPIQueryBuilder builder() {
        return new MapImageAPIQueryBuilder();
    }

    @Override
    public String toURL(String key) {
        StringBuilder url = new StringBuilder();

        url.append("https://api.tomtom.com/map/1/tile/");
        url.append(layer.toString() + "/");
        url.append(style.toString() + "/");
        url.append(z + "/");
        url.append(x + "/");
        url.append(y);
        url.append("." + format.toString() + "?");
        url.append("tileSize=256&view=Unified&language=NGT");
        url.append("&key=" + key);

        return url.toString();
    }
}
