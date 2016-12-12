package net.acomputerdog.webchat;

import net.acomputerdog.webchat.util.BoundedSet;

import java.util.function.Consumer;

public class ChatList {
    private final BoundedSet<String> lines;
    private final Object lock;

    //increments after every new chat message, can safely overflow to negative
    private int version;

    public ChatList(PluginWebChat plugin) {
        lines = new BoundedSet<>(plugin.maxLines);
        lock = new Object();
        version = 0;
    }

    public void forEach(Consumer<String> consumer) {
        synchronized (lock) {
            for (String str : lines) {
                consumer.accept(str);
            }
        }
    }

    public void addLine(String line) {
        if (line != null) {
            line = filter(line);
            synchronized (lock) {
                lines.add(line);
                version++;
            }
        }
    }

    public int getVersion() {
        synchronized (lock) {
            return version;
        }
    }

    private String filter(String line) {
        StringBuilder builder = new StringBuilder(line.length());
        for (char chr : line.toCharArray()) {
            switch (chr) {
                case '<':
                case '>':
                case '{':
                case '}':
                    break; //ignore those characters
                default:
                    builder.append(chr);//otherwise add
            }
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        forEach(line -> {
            builder.append(line);
            builder.append("\n");
        });
        return builder.toString();
    }
}
