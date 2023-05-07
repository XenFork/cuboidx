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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class FileUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static String readString(String path) {
        return readString(STACK_WALKER.getCallerClass(), path);
    }

    public static String readString(Class<?> cls, String path) {
        return readString(cls.getClassLoader(), path);
    }

    public static String readString(ClassLoader classLoader, String path) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(path)))
        )) {
            final StringBuilder sb = new StringBuilder(512);
            String line = reader.readLine();
            if (line != null) {
                sb.append(line);
            }
            while ((line = reader.readLine()) != null) {
                sb.append('\n').append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Failed to load file '" + path + "'", e);
            return null;
        }
    }
}
