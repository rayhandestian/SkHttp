package lol.aabss.skhttp.elements.websocket.effects;
import lol.aabss.skhttp.SkHttpRegistry;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Name("Websocket Send Status")
@Description("Sends a websocket status.")
@Examples({
        "post close with status code 1000 and reason \"normal closure\" by {_websocket}",
        "post binary message \"binary message\" by {_websocket}",
        "post ping message \"ping message\" by {_websocket}",
        "post pong message \"pong message\" by {_websocket}",
        "post last text \"hello\" by {_websockets::*}"
})
@Since("1.3")
public class EffWebsocketSendStatus extends AsyncEffect {

    static {
        SkHttpRegistry.effect(EffWebsocketSendStatus.class,
                "(send|post) close with status code %integer% and reason %string% (with|by|using) [websocket] %websockets%",
                "(send|post) [last|:partial] binary message %string% (with|by|using) [websocket] %websockets%",
                "(send|post) ping message %string% (with|by|using) [websocket] %websockets%",
                "(send|post) pong message %string% (with|by|using) [websocket] %websockets%",
                "(send|post) [last|:partial] (text|message) %string% (with|by|using) [websocket] %websockets%"
        );
    }

    private Expression<WebSocket> webSocket;
    private Expression<Integer> code;
    private Expression<String> message;
    // A websocket message is complete unless the script explicitly marks it as a partial fragment.
    private boolean last;
    private int pattern;

    @Override
    protected void execute(@NotNull Event e) {
        String message = this.message.getSingle(e);
        if (message == null){
            return;
        }
        for (WebSocket webSocket : this.webSocket.getArray(e)){
            if (pattern == 0) {
                Integer code = this.code.getSingle(e);
                if (code == null){
                    return;
                }
                webSocket.sendClose(code, message);
            } else if (pattern == 1) {
                webSocket.sendBinary(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)), last);
            } else if (pattern == 2) {
                webSocket.sendPing(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            } else if (pattern == 3) {
                webSocket.sendPong(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            } else if (pattern == 4) {
                webSocket.sendText(message, last);
            }
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "websocket send status";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = matchedPattern;
        last = !parseResult.hasTag("partial");
        if (matchedPattern == 0){
            code = (Expression<Integer>) exprs[0];
            message = (Expression<String>) exprs[1];
            webSocket = (Expression<WebSocket>) exprs[2];
        } else {
            message = (Expression<String>) exprs[0];
            webSocket = (Expression<WebSocket>) exprs[1];
        }
        return true;
    }
}
