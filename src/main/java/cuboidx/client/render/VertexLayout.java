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

package cuboidx.client.render;

import org.jetbrains.annotations.Unmodifiable;
import org.overrun.gl.opengl.GL;

import java.lang.foreign.*;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final /* value */ class VertexLayout {
    public static final VertexLayout POSITION_COLOR = new VertexLayout(VertexFormat.POSITION, VertexFormat.COLOR);
    public static final VertexLayout POSITION_COLOR_TEXTURE = new VertexLayout(VertexFormat.POSITION, VertexFormat.COLOR, VertexFormat.UV0);
    private final int stride;
    private final List<VertexFormat> formats;
    private final MemorySegment[] pointers;
    private final StructLayout layout;

    public VertexLayout(VertexFormat... formats) {
        this.formats = List.of(formats);
        MemoryLayout[] layouts = new MemoryLayout[formats.length];
        MemorySegment[] pointers = new MemorySegment[formats.length];
        int finalStride = 0;
        int index = 0;
        for (VertexFormat format : formats) {
            layouts[index] = format.layout();
            pointers[index] = MemorySegment.ofAddress(finalStride);
            finalStride += format.byteSize();
            index++;
        }
        this.stride = finalStride;
        this.pointers = pointers;
        this.layout = MemoryLayout.structLayout(layouts);
    }

    public void bindLocations(SegmentAllocator allocator, int program) {
        for (VertexFormat format : formats) {
            if (format.isPadding()) continue;
            GL.bindAttribLocation(allocator, program, format.id(), format.name());
        }
    }

    public void specifyAttributes() {
        for (int i = 0, formatsSize = formats.size(); i < formatsSize; i++) {
            VertexFormat format = formats.get(i);
            if (format.isPadding()) continue;
            GL.enableVertexAttribArray(format.id());
            GL.vertexAttribPointer(
                format.id(),
                format.size(),
                format.type().enumValue(),
                format.normalized(),
                stride,
                pointers[i]
            );
        }
    }

    public int stride() {
        return stride;
    }

    public @Unmodifiable List<VertexFormat> formats() {
        return formats;
    }

    public StructLayout layout() {
        return layout;
    }
}
