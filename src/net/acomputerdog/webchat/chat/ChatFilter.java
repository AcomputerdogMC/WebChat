package net.acomputerdog.webchat.chat;

import net.acomputerdog.webchat.PluginWebChat;
import org.bukkit.configuration.Configuration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter {
    private final PluginWebChat plugin;

    private final boolean filterIn;
    private final boolean filterOut;
    private final boolean strictFilter;
    private final boolean stripSectionSigns;
    private final boolean limitLineLength;
    private final int maxLineLength;
    private final Pattern[] filterPatterns;

    public ChatFilter(PluginWebChat plugin, Configuration conf) {
        this.plugin = plugin;

        filterIn = conf.getBoolean("filter_in", true);
        filterOut = conf.getBoolean("filter_out", true);
        strictFilter = conf.getBoolean("strict_filter", false);
        stripSectionSigns = conf.getBoolean("strip_section_signs", true);
        limitLineLength = conf.getBoolean("limit_line_length", true);
        maxLineLength = conf.getInt("max_line_length", 256);

        List<String> patterns = conf.getStringList("regex_filters");
        List<String> quotes = conf.getStringList("quote_filters");
        for (String str : quotes) {
            patterns.add(Pattern.quote(str));
        }
        filterPatterns = new Pattern[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            filterPatterns[i] = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
    }

    public String filterIncomingLine(String line) {
        if (limitLineLength) line = limitLength(line);
        if (stripSectionSigns) line = stripSectionSigns(line);
        if (filterIn) line = filterPatterns(line);

        return line;
    }

    public String filterOutgoingLine(String line) {
        if (filterOut) {
            line = filterPatterns(line);
        }

        return line;
    }

    private String limitLength(String line) {
        if (line.length() > maxLineLength) {
            line = line.substring(0, maxLineLength);
        }
        return line;
    }

    private String filterPatterns(String line) {
        for (Pattern pattern : filterPatterns) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                int length = end - start;

                String newLine;
                if (!strictFilter) {
                    newLine = line.substring(0, start + 1);
                    for (int i = 0; i < length - 2; i++) {
                        newLine = newLine + "*";
                    }
                    if (end > 0 && end <= line.length()) {
                        newLine += line.substring(end - 1, line.length());
                    }
                } else {
                    newLine = line.substring(0, start);
                    for (int i = 0; i < length; i++) {
                        newLine = newLine + "*";
                    }
                    if (end < line.length()) {
                        newLine = line.substring(end);
                    }
                }
                line = newLine;
            }
        }
        return line;
    }

    private String stripSectionSigns(String line) {
        return line.replace('§', '&');
    }
}
