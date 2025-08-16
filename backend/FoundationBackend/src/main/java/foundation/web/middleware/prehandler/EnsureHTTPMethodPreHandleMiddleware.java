package foundation.web.middleware.prehandler;

import foundation.web.middleware.HandlerFunction;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class EnsureHTTPMethodPreHandleMiddleware implements PreHandlerMiddleware {
    private final String httpMethod;

    public EnsureHTTPMethodPreHandleMiddleware(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public boolean preHandle(HandlerFunction handlerFn, Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Methods", "OPTIONS, " + httpMethod);

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);
            callback.succeeded();
            return true;
        }
        if (!request.getMethod().equals(httpMethod)) {
            response.setStatus(405);
            Content.Sink.write(response, true, "Method not allowed - only GET is allowed", callback);

            return true;
        }

        return handlerFn.apply(request, response, callback);
    }
}
