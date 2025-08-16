package foundation.web.middleware.prehandler;

import foundation.web.middleware.HandlerFunction;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public interface PreHandlerMiddleware {
    boolean preHandle(HandlerFunction handlerFn,
                      Request request, Response response, Callback callback);
}
