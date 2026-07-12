package lol.aabss.skhttp.elements.json.conditions;
import lol.aabss.skhttp.SkHttpRegistry;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import lol.aabss.skhttp.SkHttp;
import lol.aabss.skhttp.objects.Json;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Json - Has Element")
@Description("Returns true if the json has the specified value or key")
@Examples({
        "if {_json} has value \"aabss\":",
        "if {_json} has key \"name\":",
        "\treturn true"
})
@Since("1.4")
public class CondJsonHas extends Condition {

    static {
        if (SkHttp.instance.getConfig().getBoolean("json-elements", true)) {
            SkHttpRegistry.condition(CondJsonHas.class,
                    "%jsonarrays/jsonobjects% (has|contains) (value|:key) %object%"
            );
        }
    }

    private Expression<?> json;
    private Expression<Object> object;
    private boolean key;

    @Override
    public boolean check(@NotNull Event e) {
        Object object = this.object.getSingle(e);
        if (object == null){
            return false;
        }
        // A variable matching %jsonarrays/jsonobjects% yields a plain Object[], so the values must be filtered rather than cast.
        Object[] values = json.getArray(e);
        if (values.length == 0){
            return false;
        }
        for (Object value : values){
            if (!(value instanceof JsonElement element)){
                return false;
            }
            if (key) {
                if (!new Json(element, e).hasKey(Classes.toString(object), e)) {
                    return false;
                }
            } else {
                if (!new Json(element, e).hasValue(object, e)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json has value";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        json = exprs[0];
        object = (Expression<Object>) exprs[1];
        key = parseResult.hasTag("key");
        if (this.object instanceof UnparsedLiteral) {
            object = LiteralUtils.defendExpression(object);
        }
        return LiteralUtils.canInitSafely(object);
    }
}
