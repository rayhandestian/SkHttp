package lol.aabss.skhttp.objects.server;

import com.google.gson.*;
import com.sun.net.httpserver.Headers;
import lol.aabss.skhttp.SkHttp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpExchange {
    public HttpExchange(com.sun.net.httpserver.HttpExchange exchange, String path) {
        this.exchange = exchange;
        this.path = path;
        SkHttp.LAST_EXCHANGE = this;
    }

    private final String path;
    public com.sun.net.httpserver.HttpExchange exchange;
    private final AtomicBoolean responded = new AtomicBoolean(false);
    private String requestBody;
    private String responseBody;

    public void respond(int responseCode){
        respond(null, responseCode);
    }

    public void respond(Object response, int responseCode){
        Gson gson;
        if (SkHttp.instance.getConfig().getBoolean("pretty-print-json", true)) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new GsonBuilder().create();
        }
        String string = null;
        if (response instanceof JsonElement) {
            string = gson.toJson(response);
        } else if (response != null){
            try {
                JsonElement element = JsonParser.parseString(response.toString());
                string = gson.toJson(element);
            } catch (Exception ignored){
                string = response.toString();
            }
        }
        if (!responded.compareAndSet(false, true)) {
            SkHttp.LOGGER.warn("A response was already sent for " + method() + " " + uri() + ", ignoring.");
            return;
        }
        try {
            if (string == null){
                exchange.sendResponseHeaders(responseCode, -1);
            } else {
                byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(responseCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                responseBody = string;
            }
        } catch (IOException e) {
            // The client aborted the connection before the response was written (browser refresh spam, timeouts). Not recoverable and not a script error, so don't throw.
            SkHttp.LOGGER.debug("Client disconnected before the response could be sent for " + method() + " " + uri());
        } finally {
            exchange.close();
        }
    }

    public boolean hasResponded(){
        return responded.get();
    }

    public Object attribute(String key){
        return exchange.getAttribute(key);
    }

    public void attribute(String key, Object value){
        exchange.setAttribute(key, value);
    }

    public String protocol(){
        return exchange.getProtocol();
    }

    public String method(){
        return exchange.getRequestMethod();
    }

    public InetSocketAddress localAddress(){
        return exchange.getLocalAddress();
    }

    public InetSocketAddress remoteAddress(){
        return exchange.getRemoteAddress();
    }

    public String requestBody(){
        // The underlying stream can only be consumed once, so the first read is cached.
        if (requestBody == null) {
            try {
                requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                requestBody = "";
            }
        }
        return requestBody;
    }

    public String responseBody(){
        return responseBody;
    }

    public Headers requestHeaders(){
        return exchange.getRequestHeaders();
    }

    public Headers responseHeaders(){
        return exchange.getResponseHeaders();
    }

    public URI uri(){
        return exchange.getRequestURI();
    }

    public int code(){
        return exchange.getResponseCode();
    }

    public void close(){
        exchange.close();
    }

    public String path(){
        return path;
    }

    public String fullPath(){
        return uri().getPath().replace("/"+path()+"/", "");
    }

    public Map<String, Object> parameters(){
        Map<String, Object> result = new HashMap<>();
        if (uri().getQuery() == null) {
            return result;
        }
        for (String param : uri().getQuery().split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), getObject(URLDecoder.decode(entry[1], StandardCharsets.UTF_8)));
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private Object getObject(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {}
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ignored) {}
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException ignored) {}
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException ignored) {}
        if (string.equalsIgnoreCase("true")){
            return true;
        } else if (string.equalsIgnoreCase("false")){
            return false;
        }
        try {
            return JsonParser.parseString(string).getAsJsonNull();
        } catch (JsonParseException | IllegalStateException ignored) {}
        try {
            return JsonParser.parseString(string).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ignored) {}
        try {
            return JsonParser.parseString(string).getAsJsonArray();
        } catch (JsonParseException | IllegalStateException ignored) {}
        return string;
    }

}
