package lol.aabss.skhttp.elements.http.sections;
import lol.aabss.skhttp.SkHttpRegistry;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import lol.aabss.skhttp.SkHttp;
import lol.aabss.skhttp.objects.RequestObject;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

@Name("Send Http Request")
@Description({
        "Sends an, optionally async, http request.",
        "# Note:",
        "If you are sending an async request, you will have to use the section in order to get the response on time."
})
@Examples({
        "send async request using {_client} and {_request}:",
        "\tbroadcast body of the response",
        "send request using {_request}"
})
@Since("1.0, 1.2 (effect section)")
public class EffSecSendHttpRequest extends EffectSection {

    static {
        SkHttpRegistry.section(EffSecSendHttpRequest.class,
                "(send|post) [[:a]sync[hronous]] [http] request using [[client] %-httpclient% and] [request] %httprequest%",
                "(send|post) [http] request using [[client] %-httpclient% and] [request] %httprequest% [:a]synchronously"
        );
        SkHttpRegistry.eventValue(SendHttpRequestEvent.class, HttpResponse.class, SendHttpRequestEvent::getResponse);
    }

    public static class SendHttpRequestEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final HttpResponse<?> response;

        public SendHttpRequestEvent(HttpResponse<?> response) {
            this.response = response;
        }

        public HttpResponse<?> getResponse() {
            return response;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return handlers;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }
    }

    private static final HttpClient DEFAULT_CLIENT = lol.aabss.skhttp.objects.HttpClientFactory.newClient();

    @Nullable
    private Trigger trigger;
    @Nullable
    private Expression<HttpClient> client;
    private Expression<RequestObject> request;
    private boolean async;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
        if (hasSection()) {
            assert sectionNode != null;
            trigger = loadCode(sectionNode, "http request event", SendHttpRequestEvent.class);
        }
        client = (Expression<HttpClient>) exprs[0];
        request = (Expression<RequestObject>) exprs[1];
        async = parseResult.hasTag("a");
        // The sync form resumes the trigger when the response arrives instead of blocking the thread, which is a delay in Skript's model.
        if (!async) {
            getParser().setHasDelayBefore(Kleenean.TRUE);
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        debug(event, true);
        RequestObject request = this.request.getSingle(event);
        if (request == null){
            SkHttp.LOGGER.warn("Cannot send http request: the request is not set.");
            return super.walk(event, false);
        }
        HttpClient httpClient = null;
        if (this.client != null) {
            httpClient = this.client.getSingle(event);
        }
        if (httpClient == null) {
            httpClient = DEFAULT_CLIENT;
        }
        if (async) {
            sendAsync(event, request, httpClient);
            return super.walk(event, false);
        }
        sendSyncContinuation(event, request, httpClient);
        return null;
    }

    private void sendAsync(@NotNull Event event, RequestObject request, HttpClient httpClient) {
        Object localVars = Variables.copyLocalVariables(event);
        httpClient.sendAsync(request.request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        SkHttp.LOGGER.warn("Async http request failed: " + throwable.getMessage());
                    }
                    // On failure the section still runs with no response, matching 1.5, so scripts can detect it.
                    runOnMainThread(() -> {
                        SkHttp.LAST_RESPONSE = response;
                        if (trigger != null) {
                            SendHttpRequestEvent requestEvent = new SendHttpRequestEvent(response);
                            Variables.setLocalVariables(requestEvent, localVars);
                            TriggerItem.walk(trigger, requestEvent);
                            Variables.removeLocals(requestEvent);
                        }
                    });
                });
    }

    private void sendSyncContinuation(@NotNull Event event, RequestObject request, HttpClient httpClient) {
        TriggerItem next = getNext();
        // The trigger is resumed Delay-style when the response arrives; blocking here would freeze the main thread and deadlock requests sent to this server's own endpoints.
        Object localVars = Variables.removeLocals(event);
        httpClient.sendAsync(request.request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    if (throwable != null) {
                        SkHttp.LOGGER.warn("Http request failed: " + throwable.getMessage());
                    }
                    runOnMainThread(() -> {
                        Delay.addDelayedEvent(event);
                        SkHttp.LAST_RESPONSE = response;
                        Object vars = localVars;
                        if (trigger != null) {
                            SendHttpRequestEvent requestEvent = new SendHttpRequestEvent(response);
                            // The outer trigger's variable map is shared, not copied, so variables set inside the section stay visible after it.
                            Variables.setLocalVariables(requestEvent, vars);
                            TriggerItem.walk(trigger, requestEvent);
                            Object harvested = Variables.removeLocals(requestEvent);
                            if (harvested != null) {
                                vars = harvested;
                            }
                        }
                        if (next != null) {
                            Variables.setLocalVariables(event, vars);
                            TriggerItem.walk(next, event);
                            Variables.removeLocals(event);
                        }
                    });
                });
    }

    // Exceptions must be caught here: whenComplete swallows anything thrown into its unobserved stage, and runTask throws if the plugin is disabled mid-flight (/reload, shutdown).
    private static void runOnMainThread(Runnable task) {
        try {
            Bukkit.getScheduler().runTask(SkHttp.instance, task);
        } catch (Exception e) {
            SkHttp.LOGGER.warn("Dropped http response handling (is the plugin being disabled?): " + e.getMessage());
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "send "+(async ? "a" : "")+ "sync http request";
    }
}
