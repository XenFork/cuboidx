/*
 * cuboidx - A 3D sandbox game
 * Copyright (C) 2023  XenFork Union
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cuboidx.util;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternMatch;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PerformanceSensitive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
@Plugin(name = "LoggerNamePatternSelector", category = Node.CATEGORY, elementType = PatternSelector.ELEMENT_TYPE)
@PerformanceSensitive("allocation")
public final class LoggerNamePatternSelector implements PatternSelector {
    private static final class LoggerNameSelector {
        private final String name;
        private final boolean isPackage;
        private final PatternFormatter[] formatters;

        private LoggerNameSelector(String name, PatternFormatter[] formatters) {
            this.name = name;
            this.isPackage = name.endsWith(".");
            this.formatters = formatters;
        }

        private PatternFormatter[] get() {
            return this.formatters;
        }

        private boolean test(String s) {
            return this.isPackage ? s.startsWith(this.name) : s.equals(this.name);
        }
    }

    private final PatternFormatter[] defaultFormatters;
    private final List<LoggerNameSelector> formatters = new ArrayList<>();

    private LoggerNamePatternSelector(String defaultPattern, PatternMatch[] properties,
                                      boolean alwaysWriteExceptions, boolean disableAnsi, boolean noConsoleNoAnsi, Configuration config) {
        PatternParser parser = PatternLayout.createPatternParser(config);
        PatternFormatter[] emptyFormatters = new PatternFormatter[0];
        this.defaultFormatters = parser.parse(defaultPattern, alwaysWriteExceptions, disableAnsi, noConsoleNoAnsi)
            .toArray(emptyFormatters);
        for (PatternMatch property : properties) {
            PatternFormatter[] formatters = parser.parse(property.getPattern(), alwaysWriteExceptions, disableAnsi, noConsoleNoAnsi)
                .toArray(emptyFormatters);
            for (String name : property.getKey().split(",")) {
                this.formatters.add(new LoggerNameSelector(name, formatters));
            }
        }
    }

    @Override
    public PatternFormatter[] getFormatters(LogEvent event) {
        final String loggerName = event.getLoggerName();
        if (loggerName != null) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < this.formatters.size(); i++) {
                LoggerNameSelector selector = this.formatters.get(i);
                if (selector.test(loggerName)) {
                    return selector.get();
                }
            }
        }
        return this.defaultFormatters;
    }

    @PluginFactory
    public static LoggerNamePatternSelector createSelector(
        @Required(message = "Default pattern is required") @PluginAttribute(value = "defaultPattern") String defaultPattern,
        @PluginElement("PatternMatch") PatternMatch[] properties,
        @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) boolean alwaysWriteExceptions,
        @PluginAttribute("disableAnsi") boolean disableAnsi,
        @PluginAttribute("noConsoleNoAnsi") boolean noConsoleNoAnsi,
        @PluginConfiguration Configuration config) {
        return new LoggerNamePatternSelector(defaultPattern, properties, alwaysWriteExceptions, disableAnsi, noConsoleNoAnsi, config);
    }
}
