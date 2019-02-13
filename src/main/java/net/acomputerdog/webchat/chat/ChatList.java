package net.acomputerdog.webchat.chat;

import net.acomputerdog.webchat.PluginWebChat;
import net.acomputerdog.webchat.util.BoundedSet;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Stores list of chat messages
 */
public class ChatList {
    private final BoundedSet<String> lines;
    private final Semaphore lock;
    private final ChatFilter filter;

    //increments after every new chat message, can safely overflow to negative
    private int version;

    public ChatList(PluginWebChat plugin) {
        lines = new BoundedSet<>(plugin.getMaxLines());
        lock = new Semaphore(1, true);
        filter = plugin.getChatFilter();
        version = 0;
    }

    public void forEach(Consumer<String> consumer) {
        lock.acquireUninterruptibly();
        try {
            for (String str : lines) {
                consumer.accept(str);
            }
        } finally {
            lock.release();
        }
    }

    public void addLine(String line) {
        if (line != null) {
            line = filter(line);

            lock.acquireUninterruptibly();
            try {
                lines.add(line);
                version++;
            } finally {
                lock.release();
            }
        }
    }

    public int getVersion() {
        lock.acquireUninterruptibly();
        try {
            return version;
        } finally {
            lock.release();
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
            builder.append(filter.filterOutgoingLine(line));
            builder.append("\n");
        });
        return builder.toString();
    }
}
