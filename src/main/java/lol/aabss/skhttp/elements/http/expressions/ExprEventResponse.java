package lol.aabss.skhttp.elements.http.expressions;
import lol.aabss.skhttp.SkHttpRegistry;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import lol.aabss.skhttp.elements.http.sections.EffSecSendHttpRequest;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpResponse;

@Name("Event Response")
@Description("Gets a event-response.")
@Examples({
        "send the response"
})
@Since("1.6")
public class ExprEventResponse extends SimpleExpression<HttpResponse> {

    static {
        SkHttpRegistry.expression(ExprEventResponse.class, HttpResponse.class, SkHttpRegistry.SIMPLE,
                "(the |event-)[http[ |-]]response"
        );
    }

    @Override
    protected HttpResponse @NotNull [] get(@NotNull Event e) {
        if (e instanceof EffSecSendHttpRequest.SendHttpRequestEvent) {
            HttpResponse<?> response = ((EffSecSendHttpRequest.SendHttpRequestEvent) e).getResponse();
            // The response is null when the request failed; the expression is then simply not set.
            if (response != null) {
                return new HttpResponse[]{response};
            }
        }
        return new HttpResponse[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends HttpResponse> getReturnType() {
        return HttpResponse.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "event response";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }
}
