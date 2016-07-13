package net.acomputerdog.webchat;

import net.acomputerdog.webchat.util.WrappingArray;

import java.util.function.Consumer;

public class ChatList {
    private final WrappingArray<String> lines;
    private final Object lock;

    private int version;

    public ChatList() {
        lines = new WrappingArray<>(PluginWebChat.MAX_LINES);
        lock = new Object();
        version = 0;
    }

    public void forEach(Consumer<String> consumer) {
        synchronized (lock) {
            for (String str : lines) {
                if (str != null) {//workaround for bigger problem
                    consumer.accept(str);
                }
            }
        }
    }

    public void addLine(String line) {
        synchronized (lock) {
            lines.add(filter(line));
            version++;
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
}
