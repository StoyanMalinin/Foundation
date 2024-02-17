package observability;

import java.time.Duration;
import java.time.Instant;

public class PerformanceUtils {
    public static Duration mesaureDuration(Runnable r) {
        Instant start = Instant.now();
        r.run();
        Instant end = Instant.now();

        return Duration.between(start, end);
    }

    public static void logDuration(Runnable r, String actionName) {
        Duration duration = mesaureDuration(r);
        System.out.println(actionName + " took " + duration.toMillis() + "ms");
    }
}
