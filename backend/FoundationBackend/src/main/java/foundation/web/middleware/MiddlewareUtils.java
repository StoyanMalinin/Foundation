package foundation.web.middleware;

import foundation.web.middleware.prehandler.PreHandlerMiddleware;

import java.util.Arrays;
import java.util.Collections;

public class MiddlewareUtils {
    public static HandlerFunction applyPreHandlerMiddleware(
            HandlerFunction handlerFn,
            PreHandlerMiddleware... preHandlerMiddleware
     ) {
        return ((request, response, callback) -> {
            HandlerFunction currentHandler = handlerFn;

            Collections.reverse(Arrays.asList(preHandlerMiddleware)); // ensure middleware is applied in the order they were added
            for (PreHandlerMiddleware middleware : preHandlerMiddleware) {
                final HandlerFunction nextHandler = currentHandler;
                currentHandler = (req, res, cb) -> middleware.preHandle(nextHandler, req, res, cb);
            }

            return currentHandler.apply(request, response, callback);
        });
    }
}
