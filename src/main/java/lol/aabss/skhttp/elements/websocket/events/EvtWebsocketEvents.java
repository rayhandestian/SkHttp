package lol.aabss.skhttp.elements.websocket.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import lol.aabss.skhttp.SkHttpRegistry;
import lol.aabss.skhttp.objects.websocket.events.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.WebSocket;

public class EvtWebsocketEvents extends SkriptEvent {

    static {
        SkHttpRegistry.event("Websocket Binary Event", EvtWebsocketEvents.class, WebsocketBinaryEvent.class,
                "Called whenever a binary data was received.", "on websocket binary:", "1.3",
                "websocket binary [receive[d]]");
        SkHttpRegistry.eventValue(WebsocketBinaryEvent.class, WebSocket.class, WebsocketBinaryEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketBinaryEvent.class, String.class, WebsocketBinaryEvent::getData);
        SkHttpRegistry.eventValue(WebsocketBinaryEvent.class, Boolean.class, WebsocketBinaryEvent::getLast);

        SkHttpRegistry.event("Websocket Close Event", EvtWebsocketEvents.class, WebsocketCloseEvent.class,
                "Called whenever a websocket was closed.", "on websocket close:", "1.3",
                "websocket close[d]");
        SkHttpRegistry.eventValue(WebsocketCloseEvent.class, WebSocket.class, WebsocketCloseEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketCloseEvent.class, String.class, WebsocketCloseEvent::getReason);
        SkHttpRegistry.eventValue(WebsocketCloseEvent.class, Integer.class, WebsocketCloseEvent::getStatusCode);

        SkHttpRegistry.event("Websocket Error Event", EvtWebsocketEvents.class, WebsocketErrorEvent.class,
                "Called whenever a error is received.", "on websocket error:", "1.3",
                "websocket error [receive[d]]");
        SkHttpRegistry.eventValue(WebsocketErrorEvent.class, WebSocket.class, WebsocketErrorEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketErrorEvent.class, String.class, WebsocketErrorEvent::getError);

        SkHttpRegistry.event("Websocket Open Event", EvtWebsocketEvents.class, WebsocketOpenEvent.class,
                "Called whenever a websocket opens.", "on websocket open:", "1.3",
                "websocket open[ed|s]");
        SkHttpRegistry.eventValue(WebsocketOpenEvent.class, WebSocket.class, WebsocketOpenEvent::getWebSocket);

        SkHttpRegistry.event("Websocket Ping Event", EvtWebsocketEvents.class, WebsocketPingEvent.class,
                "Called whenever a ping get received.", "on websocket ping:", "1.3",
                "websocket ping[ed]");
        SkHttpRegistry.eventValue(WebsocketPingEvent.class, WebSocket.class, WebsocketPingEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketPingEvent.class, String.class, WebsocketPingEvent::getData);

        SkHttpRegistry.event("Websocket Pong Event", EvtWebsocketEvents.class, WebsocketPongEvent.class,
                "Called whenever a pong gets sent out.", "on websocket pong:", "1.3",
                "websocket pong[ed]");
        SkHttpRegistry.eventValue(WebsocketPongEvent.class, WebSocket.class, WebsocketPongEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketPongEvent.class, String.class, WebsocketPongEvent::getData);

        SkHttpRegistry.event("Websocket Text Event", EvtWebsocketEvents.class, WebsocketTextEvent.class,
                "Called whenever a websocket sends out a text to all listeners.", "on websocket text:", "1.3",
                "websocket text [receive[ed]]");
        SkHttpRegistry.eventValue(WebsocketTextEvent.class, WebSocket.class, WebsocketTextEvent::getWebSocket);
        SkHttpRegistry.eventValue(WebsocketTextEvent.class, String.class, WebsocketTextEvent::getData);
        SkHttpRegistry.eventValue(WebsocketTextEvent.class, Boolean.class, WebsocketTextEvent::getLast);
    }


    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return e != null ? e.getEventName() : "websocket event";
    }
}
