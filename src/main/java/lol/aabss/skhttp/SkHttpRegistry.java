package lol.aabss.skhttp;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEvent;
import org.bukkit.event.Event;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

/**
 * Central registration helper for SkHttp's syntax elements, built on the modern
 * org.skriptlang addon API (Skript 2.15+). Each element registers itself from its
 * own static initializer by calling into here; {@link #init(SkriptAddon)} must run
 * before the element classes are loaded.
 */
public final class SkHttpRegistry {

    private static SkriptAddon addon;

    /** Expression priorities, re-exposed so element classes need only this one import. */
    public static final Priority SIMPLE = SyntaxInfo.SIMPLE;
    public static final Priority COMBINED = SyntaxInfo.COMBINED;

    private SkHttpRegistry() {}

    public static void init(SkriptAddon skriptAddon) {
        addon = skriptAddon;
    }

    private static SyntaxRegistry registry() {
        return addon.syntaxRegistry();
    }

    public static <E extends Effect> void effect(Class<E> c, String... patterns) {
        registry().register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(c)
                .priority(SyntaxInfo.COMBINED)
                .addPatterns(patterns)
                .build());
    }

    public static <E extends Section> void section(Class<E> c, String... patterns) {
        registry().register(SyntaxRegistry.SECTION, SyntaxInfo.builder(c)
                .addPatterns(patterns)
                .build());
    }

    public static <E extends Condition> void condition(Class<E> c, String... patterns) {
        registry().register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(c)
                .priority(SyntaxInfo.COMBINED)
                .addPatterns(patterns)
                .build());
    }

    public static <E extends Expression<T>, T> void expression(Class<E> c, Class<T> returnType, Priority priority, String... patterns) {
        registry().register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(c, returnType)
                .priority(priority)
                .addPatterns(patterns)
                .build());
    }

    public static <T> void property(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
        registry().register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(c, returnType, property, fromType, false).build());
    }

    public static <E extends Condition> void propertyCondition(Class<E> c, PropertyType propertyType, String property, String fromType) {
        registry().register(SyntaxRegistry.CONDITION,
                PropertyCondition.infoBuilder(c, propertyType, property, fromType).build());
    }

    /** Property condition with the default {@link PropertyType#BE} ("... is/are ..."). */
    public static <E extends Condition> void propertyCondition(Class<E> c, String property, String fromType) {
        propertyCondition(c, PropertyType.BE, property, fromType);
    }

    public static <E extends Event, V> void eventValue(Class<E> event, Class<V> value, Converter<E, V> getter) {
        addon.registry(EventValueRegistry.class).register(EventValue.builder(event, value)
                .getter(getter)
                .time(EventValue.Time.NOW)
                .build());
    }

    public static void event(String name, Class<? extends SkriptEvent> c, Class<? extends Event> event,
                             String description, String example, String since, String... patterns) {
        String[] fixed = patterns.clone();
        for (int i = 0; i < fixed.length; i++) {
            fixed[i] = BukkitSyntaxInfos.fixPattern(fixed[i]);
        }
        registry().register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(c, name)
                .addEvent(event)
                .addPatterns(fixed)
                .addDescription(description)
                .addExamples(example)
                .addSince(since)
                .build());
    }
}
