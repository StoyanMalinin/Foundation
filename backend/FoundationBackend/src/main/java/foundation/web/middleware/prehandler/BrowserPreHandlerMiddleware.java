package foundation.web.middleware.prehandler;

import foundation.web.middleware.HandlerFunction;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class BrowserPreHandlerMiddleware implements PreHandlerMiddleware {
    private final String frontendUrl;

    public BrowserPreHandlerMiddleware(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public boolean preHandle(HandlerFunction handlerFn, Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", frontendUrl);
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");
        response.getHeaders().put("Access-Control-Allow-Credentials", "true");

        return handlerFn.apply(request, response, callback);
    }
}
