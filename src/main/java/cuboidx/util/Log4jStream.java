/*
 * cuboidx - A 3D sandbox game
 * Copyright (C) 2023  XenFork Union
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cuboidx.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Log4jStream extends PrintStream {
    private final Logger logger;
    private final boolean err;

    public Log4jStream(@NotNull OutputStream out, boolean err) {
        super(out);
        this.logger = LogManager.getLogger(err ? "STDERR" : "STDOUT");
        this.err = err;
    }

    @Override
    public void print(boolean b) {
        if (err) logger.error(b);
        else logger.info(b);
    }

    @Override
    public void print(char c) {
        if (err) logger.error(c);
        else logger.info(c);
    }

    @Override
    public void print(int i) {
        if (err) logger.error(i);
        else logger.info(i);
    }

    @Override
    public void print(long l) {
        if (err) logger.error(l);
        else logger.info(l);
    }

    @Override
    public void print(float f) {
        if (err) logger.error(f);
        else logger.info(f);
    }

    @Override
    public void print(double d) {
        if (err) logger.error(d);
        else logger.info(d);
    }

    @Override
    public void print(char @NotNull [] s) {
        if (err) logger.error(String.valueOf(s));
        else logger.info(String.valueOf(s));
    }

    @Override
    public void print(@Nullable String s) {
        if (err) logger.error(s);
        else logger.info(s);
    }

    @Override
    public void print(@Nullable Object obj) {
        if (err) logger.error(obj);
        else logger.info(obj);
    }

    @Override
    public void println() {
        if (err) logger.error("");
        else logger.info("");
    }

    @Override
    public void println(boolean x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(char x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(int x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(long x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(float x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(double x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(char @NotNull [] x) {
        if (err) logger.error(String.valueOf(x));
        else logger.info(String.valueOf(x));
    }

    @Override
    public void println(@Nullable String x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public void println(@Nullable Object x) {
        if (err) logger.error(x);
        else logger.info(x);
    }

    @Override
    public PrintStream format(@NotNull String format, Object... args) {
        if (err) logger.error(String.format(format, args));
        else logger.info(String.format(format, args));
        return this;
    }

    @Override
    public PrintStream format(Locale l, @NotNull String format, Object... args) {
        if (err) logger.error(String.format(l, format, args));
        else logger.info(String.format(l, format, args));
        return this;
    }
}
