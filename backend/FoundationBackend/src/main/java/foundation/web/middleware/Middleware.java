package main.java.foundation.web.middleware;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public interface Middleware {
    boolean handle(HandlerFunction handlerFn,
                   Request request, Response response, Callback callback);
}
