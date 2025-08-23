package main.java.foundation.web.middleware;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

@FunctionalInterface
public interface HandlerFunction {
    boolean apply(Request request, Response response, Callback callback);
}