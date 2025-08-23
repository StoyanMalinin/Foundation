package main.java.foundation.web.middleware;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class BrowserMiddleware implements Middleware {
    private final String frontendUrl;

    public BrowserMiddleware(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public boolean handle(HandlerFunction handlerFn, Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", frontendUrl);
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");
        response.getHeaders().put("Access-Control-Allow-Credentials", "true");

        return handlerFn.apply(request, response, callback);
    }
}
