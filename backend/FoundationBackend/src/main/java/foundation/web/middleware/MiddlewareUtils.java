package main.java.foundation.web.middleware;

import java.util.Arrays;
import java.util.Collections;

public class MiddlewareUtils {
    public static HandlerFunction applyMiddleware(
            HandlerFunction handlerFn,
            Middleware... middlewares
     ) {
        return ((request, response, callback) -> {
            HandlerFunction currentHandler = handlerFn;

            Collections.reverse(Arrays.asList(middlewares)); // ensure middleware is applied in the order they were added
            for (Middleware middleware : middlewares) {
                final HandlerFunction nextHandler = currentHandler;
                currentHandler = (req, res, cb) -> middleware.handle(nextHandler, req, res, cb);
            }

            return currentHandler.apply(request, response, callback);
        });
    }
}
