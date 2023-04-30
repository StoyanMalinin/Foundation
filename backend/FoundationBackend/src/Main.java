import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
        Sandbox.run();
    }
}