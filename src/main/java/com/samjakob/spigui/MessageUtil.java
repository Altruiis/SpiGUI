package com.samjakob.spigui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageUtil {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component color(String text) {
        return mm.deserialize(text);
    }

    public static Component raw(String text) {
        return mm.deserialize(mm.stripTags(text));
    }

    public static String toString(Component component) {
        return mm.serialize(component);
    }

}
