package lol.aabss.skhttp.objects;

import ch.njol.skript.lang.Variable;
import com.google.gson.*;
import lol.aabss.skhttp.SkHttp;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static lol.aabss.skhttp.SkHttp.SKRIPT_REFLECT_SUPPORTED;

@SuppressWarnings("unused")
public class Json {

    public Json(JsonElement element, @Nullable Event e){
        this.element = element;
        this.event = e;
    }

    private Json(String key, Object value, @Nullable Event e){
        this(new JsonObject(), e);
        add(key, value, e);
    }

    public Json(boolean array, @Nullable Event e){
        this(new JsonObject(), e);
        if (array) this.element = new JsonArray();
    }

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private JsonElement element;
    @Nullable
    private final Event event;

    public Json add(String key, Object value, @Nullable Event e){
        if (value instanceof Variable<?> && e != null){
            if (((Variable<?>) value).isList()) {
                List<?> list = Arrays.stream(((Variable<?>) value).getArray(e)).toList();
                return add(key, list, e);
            } else if (((Variable<?>) value).isSingle()){
                return add(key, ((Variable<?>) value).getSingle(e), e);
            }
        } else if (SKRIPT_REFLECT_SUPPORTED && value instanceof com.btk5h.skriptmirror.ObjectWrapper) {
            return add(key, ((com.btk5h.skriptmirror.ObjectWrapper) value).get(), e);
        } else if (value instanceof Boolean || value instanceof Number || value instanceof String || value instanceof JsonPrimitive){
            return addProperty(key, value);
        } else if (value == null){
            return addProperty(key, null);
        } else if (value instanceof Iterable<?>){
            JsonArray array = new JsonArray();
            for (Object object : (Iterable<?>) value){
                Json json = new Json(gson.toJsonTree(object), e);
                if (json.getElement() instanceof JsonObject){
                    json = json.add("internal_class_name", object.getClass().getName(), e);
                }
                array.add(json.element);
            }
            if (element instanceof JsonObject) {
                ((JsonObject) element).add(key, array);
            } else if (element instanceof JsonArray) {
                ((JsonArray) element).add(array);
            }
        } else {
            if (element instanceof JsonObject) {
                JsonElement jsonObject = gson.toJsonTree(value);
                if (jsonObject instanceof JsonObject) {
                    ((JsonObject) jsonObject).addProperty("internal_class_name", value.getClass().getName());
                }
                ((JsonObject) element).add(key, jsonObject);
            } else if (element instanceof JsonArray) {
                JsonElement jsonObject = gson.toJsonTree(value);
                if (jsonObject instanceof JsonObject) {
                    ((JsonObject) jsonObject).addProperty("internal_class_name", value.getClass().getName());
                }
                ((JsonArray) element).add(jsonObject);
            }
        }
        return this;
    }

    public Json addProperty(String key, Object value){
        if (value instanceof String){
            if (element instanceof JsonObject){
                ((JsonObject) element).addProperty(key, (String) value);
            } else if (element instanceof JsonArray){
                ((JsonArray) element).add((String) value);
            }
        } else if (value instanceof Number){
            if (element instanceof JsonObject){
                ((JsonObject) element).addProperty(key, (Number) value);
            } else if (element instanceof JsonArray){
                ((JsonArray) element).add((Number) value);
            }
        } else if (value instanceof Boolean){
            if (element instanceof JsonObject){
                ((JsonObject) element).addProperty(key, (Boolean) value);
            } else if (element instanceof JsonArray){
                ((JsonArray) element).add((Boolean) value);
            }
        } else if (value instanceof JsonElement){
            if (element instanceof JsonObject){
                ((JsonObject) element).add(key, (JsonElement) value);
            } else if (element instanceof JsonArray){
                ((JsonArray) element).add((JsonElement) value);
            }
        }
        return this;
    }

    public Json removeIndex(int index){
        if (element instanceof JsonArray){
            ((JsonArray) element).remove(index);
        } else if (element instanceof JsonObject){
            ((JsonObject) element).remove((((JsonObject) element).asMap().keySet().stream().toList().get(index)));
        }
        return this;
    }

    public Json removeAll(Object object, Event e){
        return remove(object, true, e);
    }

    public Json removeFirst(Object object, Event e){
        return remove(object, false, e);
    }

    private Json remove(Object object, boolean all, Event e){
        if (object instanceof Variable<?> && e != null){
            if (((Variable<?>) object).isList()) {
                List<?> list = Arrays.stream(((Variable<?>) object).getArray(e)).toList();
                return remove(list, all, e);
            } else if (((Variable<?>) object).isSingle()){
                return remove(((Variable<?>) object).getSingle(e), all, e);
            }
        } else if (SKRIPT_REFLECT_SUPPORTED && object instanceof com.btk5h.skriptmirror.ObjectWrapper) {
            return remove(((com.btk5h.skriptmirror.ObjectWrapper) object).get(), all, e);
        } else if (object instanceof JsonElement) {
            // When removing a single element, prefer the exact instance the script referenced so a value-equal duplicate elsewhere is not removed instead.
            if (!all && removeMatching(candidate -> candidate == object, false, true)) {
                return this;
            }
            return removeMatching(candidate -> candidate.equals(object), all);
        } else if (object instanceof String){
            return removeMatching(candidate -> candidate.isJsonPrimitive() && candidate.getAsJsonPrimitive().isString() && candidate.getAsString().equals(object), all);
        } else if (object instanceof Boolean){
            return removeMatching(candidate -> candidate.isJsonPrimitive() && candidate.getAsJsonPrimitive().isBoolean() && candidate.getAsBoolean() == (Boolean) object, all);
        } else if (object instanceof Number){
            return removeMatching(candidate -> candidate.isJsonPrimitive() && candidate.getAsJsonPrimitive().isNumber() && candidate.getAsDouble() == ((Number) object).doubleValue(), all);
        } else if (object instanceof Iterable<?>){
            JsonArray array = new JsonArray();
            for (Object entry : (Iterable<?>) object){
                JsonElement entryElement = gson.toJsonTree(entry);
                if (entryElement instanceof JsonObject){
                    ((JsonObject) entryElement).addProperty("internal_class_name", entry.getClass().getName());
                }
                array.add(entryElement);
            }
            return removeMatching(candidate -> candidate.equals(array), all);
        } else if (object != null) {
            JsonElement tree = gson.toJsonTree(object);
            if (tree instanceof JsonObject) {
                ((JsonObject) tree).addProperty("internal_class_name", object.getClass().getName());
            }
            return removeMatching(candidate -> candidate.equals(tree), all);
        }
        return this;
    }

    // Gson's asList/asMap are live views, so matches are collected against a copy before removing to avoid ConcurrentModificationException.
    private Json removeMatching(Predicate<JsonElement> matcher, boolean all) {
        removeMatching(matcher, all, false);
        return this;
    }

    private boolean removeMatching(Predicate<JsonElement> matcher, boolean all, boolean identity) {
        boolean removed = false;
        if (element instanceof JsonArray array) {
            for (JsonElement candidate : new ArrayList<>(array.asList())) {
                if (matcher.test(candidate)) {
                    if (identity) {
                        array.asList().removeIf(entry -> entry == candidate);
                    } else {
                        array.remove(candidate);
                    }
                    removed = true;
                    if (!all) return true;
                }
            }
        } else if (element instanceof JsonObject object) {
            for (String key : new ArrayList<>(object.keySet())) {
                if (matcher.test(object.get(key))) {
                    object.remove(key);
                    removed = true;
                    if (!all) return true;
                }
            }
        }
        return removed;
    }

    public Object get(String key) {
        JsonElement object = null;
        if (element instanceof JsonObject){
            object = ((JsonObject) element).get(key);
        } else if (element instanceof JsonArray) {
            for (JsonElement elem : element.getAsJsonArray()) {
                if (Objects.equals(Objects.toString(fromJsonElement(elem)), key)) {
                    object = elem;
                }
            }
        }
        return fromJsonElement(object);
    }

    public Object get(Integer index) {
        JsonElement object = null;
        try {
            if (element instanceof JsonObject) {
                object = (((JsonObject) element).asMap().values().stream().toList().get(index));
            } else if (element instanceof JsonArray) {
                object = ((JsonArray) element).get(index);
            }
        } catch (IndexOutOfBoundsException ignored){
            return null;
        }
        return fromJsonElement(object);
    }

    public static Object fromJsonElement(JsonElement element){
        if (element == null) return null;
        if (element.isJsonNull()){
            return null;
        } else if (element.isJsonObject()){
            return element.getAsJsonObject();
        } else if (element.isJsonArray()){
            return element.getAsJsonArray();
        } else if (element.isJsonPrimitive()){
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()){
                return primitive.getAsNumber();
            } else if (primitive.isBoolean()){
                return primitive.getAsBoolean();
            } else if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return null;
    }

    public static JsonElement toJsonElement(Object object, Event e){
        if (object instanceof JsonElement) {
            return (JsonElement) object;
        } else if (object instanceof Variable<?> && e != null){
            if (((Variable<?>) object).isList()) {
                List<?> list = Arrays.stream(((Variable<?>) object).getArray(e)).toList();
                return toJsonElement(list, e);
            } else if (((Variable<?>) object).isSingle()){
                return toJsonElement(((Variable<?>) object).getSingle(e), e);
            }
        } else if (SKRIPT_REFLECT_SUPPORTED && object instanceof com.btk5h.skriptmirror.ObjectWrapper) {
            return toJsonElement(((com.btk5h.skriptmirror.ObjectWrapper) object).get(), e);
        } else if (object == null){
            return JsonNull.INSTANCE;
        } else if (object instanceof Iterable<?>) {
            JsonArray array = new JsonArray();
            for (Object obj : (Iterable<?>) object) {
                JsonElement entryElement = gson.toJsonTree(obj);
                if (entryElement instanceof JsonObject){
                    ((JsonObject) entryElement).addProperty("internal_class_name", obj.getClass().getName());
                }
                array.add(entryElement);
            }
            return array;
        } else if (object instanceof String){
            try {
                return JsonParser.parseString((String) object);
            } catch (JsonParseException ignored) {
                // Plain text that is not valid JSON still needs to become a value, not an error.
                return new JsonPrimitive((String) object);
            }
        } else {
            JsonElement json = gson.toJsonTree(object);
            if (json instanceof JsonObject) {
                ((JsonObject) json).addProperty("internal_class_name", object.getClass().getName());
            }
            return json;
        }
        return new JsonObject();
    }

    public boolean hasKey(String key, Event e){
        if (element instanceof JsonObject){
            for (String jsonKey : ((JsonObject) element).asMap().keySet()){
                if (key.equals(jsonKey)) {
                    return true;
                }
            }
        } else if (element instanceof JsonArray){
            return ((JsonArray) element).contains(toJsonElement(key, e));
        }
        return false;
    }

    public boolean hasValue(Object object, Event e){
        if (!(object instanceof JsonElement)) {
            object = toJsonElement(object, e);
        }
        if (element instanceof JsonObject){
            for (JsonElement element : ((JsonObject) element).asMap().values()){
                if (element.equals(object)) {
                    return true;
                }
            }
        } else if (element instanceof JsonArray){
            return ((JsonArray) element).contains(toJsonElement(object, e));
        }
        return false;
    }

    public JsonElement getElement(){
        return element;
    }

    public @Nullable Event getEvent(){
        return event;
    }

    public String toString() {
        if (SkHttp.instance.getConfig().getBoolean("pretty-print-json", true)) {
            return gson.toJson(element);
        }
        return element.toString();
    }

}