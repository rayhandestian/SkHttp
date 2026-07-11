package lol.aabss.skhttp.objects.websocket;

import lol.aabss.skhttp.SkHttp;
import lol.aabss.skhttp.objects.websocket.events.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

public class WebsocketBukkitListener implements WebSocket.Listener {

    // These callbacks run on the JDK WebSocket executor, but Bukkit only allows synchronous events to be fired from the main thread. Buffers are decoded before scheduling because the JDK may reuse them once the callback returns.
    private static void callSync(Event event) {
        try {
            Bukkit.getScheduler().runTask(SkHttp.instance, () -> Bukkit.getPluginManager().callEvent(event));
        } catch (Exception e) {
            // runTask throws if the plugin is disabled mid-flight (/reload, shutdown).
            SkHttp.LOGGER.warn("Dropped websocket event " + event.getEventName() + " (is the plugin being disabled?): " + e.getMessage());
        }
    }

    private static String decode(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer.duplicate()).toString();
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        callSync(new WebsocketOpenEvent(webSocket));
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        callSync(new WebsocketTextEvent(webSocket, data.toString(), last));
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        callSync(new WebsocketBinaryEvent(webSocket, decode(data), last));
        return WebSocket.Listener.super.onBinary(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        callSync(new WebsocketPingEvent(webSocket, decode(message)));
        return WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        callSync(new WebsocketPongEvent(webSocket, decode(message)));
        return WebSocket.Listener.super.onPong(webSocket, message);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        callSync(new WebsocketCloseEvent(webSocket, statusCode, reason));
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        callSync(new WebsocketErrorEvent(webSocket, error.getMessage()));
        WebSocket.Listener.super.onError(webSocket, error);
    }
}
