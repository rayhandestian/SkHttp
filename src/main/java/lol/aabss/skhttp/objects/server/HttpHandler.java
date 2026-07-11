package lol.aabss.skhttp.objects.server;

import lol.aabss.skhttp.SkHttp;

import java.io.IOException;

public class HttpHandler {
    public HttpHandler(com.sun.net.httpserver.HttpHandler handler) {
        this.handler = handler;
    }

    public com.sun.net.httpserver.HttpHandler handler;

    public void handle(HttpExchange exchange){
        try {
            handler.handle(exchange.exchange);
        } catch (IOException e) {
            SkHttp.LOGGER.debug("Handler failed for " + exchange.method() + " " + exchange.uri() + ": " + e.getMessage());
        }
    }
}
