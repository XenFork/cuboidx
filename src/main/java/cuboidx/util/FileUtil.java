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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class FileUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @Nullable
    public static String readString(String path) {
        return readString(STACK_WALKER.getCallerClass(), path);
    }

    @Nullable
    public static String readString(Class<?> cls, String path) {
        return readString(cls.getClassLoader(), path);
    }

    @Nullable
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
            logger.error(STR."Failed to load file '\{path}'", e);
            return null;
        }
    }

    @Nullable
    public static MemorySegment readBinary(String path, int bufferSize) {
        return readBinary(STACK_WALKER.getCallerClass(), path, bufferSize);
    }

    @Nullable
    public static MemorySegment readBinary(Class<?> cls, String path, int bufferSize) {
        return readBinary(cls.getClassLoader(), path, bufferSize);
    }

    @Nullable
    public static MemorySegment readBinary(ClassLoader classLoader, String path, int bufferSize) {
        final boolean isHttp = path.startsWith("http");
        final Path filePath = isHttp ? null : Path.of(path);
        try {
            // Check whether on local
            if (filePath != null && Files.isReadable(filePath)) {
                try (FileChannel channel = FileChannel.open(filePath)) {
                    return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), Arena.ofAuto());
                }
            }

            // On classpath
            try (
                var is = isHttp ?
                    new URI(path).toURL().openStream() :
                    Objects.requireNonNull(classLoader.getResourceAsStream(path),
                        STR."Failed to load resource '\{path}'!")
            ) {
                final Arena arena = Arena.ofAuto();
                MemorySegment segment = arena.allocate(bufferSize);

                // Creates a byte array to avoid creating it each loop
                final byte[] bytes = new byte[8192];
                long pos = 0;
                int count;
                while ((count = is.read(bytes)) > 0) {
                    if (pos + count >= segment.byteSize()) {
                        segment = resizeSegment(arena, segment, Math.ceilDiv(segment.byteSize() * 3, 2)); // 50%
                    }
                    MemorySegment.copy(bytes, 0, segment, ValueLayout.JAVA_BYTE, pos, count);
                    pos += count;
                }

                return segment.asSlice(0, pos);
            }
        } catch (Exception e) {
            logger.error(STR."Failed to load file '\{path}'", e);
            return null;
        }
    }

    private static MemorySegment resizeSegment(Arena arena, MemorySegment segment, long newCapacity) {
        return arena.allocate(newCapacity).copyFrom(segment);
    }
}
