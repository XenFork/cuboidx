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

import cuboidx.client.gl.GLDataType;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author squid233
 * @since 0.1.0
 */
public final /* value */ class VertexFormat {
    public static final VertexFormat POSITION = new VertexFormat(0, "Position", 3, GLDataType.FLOAT, false);
    public static final VertexFormat COLOR = new VertexFormat(1, "Color", 4, GLDataType.UNSIGNED_BYTE, true);
    public static final VertexFormat UV0 = new VertexFormat(2, "UV0", 2, GLDataType.FLOAT, false);
    private final int id;
    private final String name;
    private final int size;
    private final GLDataType type;
    private final boolean normalized;
    private final SequenceLayout layout;
    private final List<VarHandleKey> varHandleKeys;

    private VertexFormat(int id, String name, int size, GLDataType type, boolean normalized) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.type = type;
        this.normalized = normalized;
        SequenceLayout finalLayout = MemoryLayout.sequenceLayout(size, type.layout());
        if (!isPadding()) {
            finalLayout = finalLayout.withName(name);
        }
        this.layout = finalLayout;
        this.varHandleKeys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.varHandleKeys.add(new VarHandleKey(name, i));
        }
    }

    public static VertexFormat padding(int size, int index) {
        return new VertexFormat(-1, "Padding" + index, size, GLDataType.BYTE, false);
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public  /* value */ record VarHandleKey(String name, int index) {
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isPadding() {
        return id == -1;
    }

    public int size() {
        return size;
    }

    public GLDataType type() {
        return type;
    }

    public boolean normalized() {
        return normalized;
    }

    public SequenceLayout layout() {
        return layout;
    }

    public MemoryLayout.PathElement pathElement() {
        return MemoryLayout.PathElement.groupElement(name);
    }

    public VarHandleKey varHandleKey(int index) {
        return varHandleKeys.get(index);
    }

    public int byteSize() {
        return size * type.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexFormat that = (VertexFormat) o;
        return id == that.id && size == that.size && normalized == that.normalized && Objects.equals(name, that.name) && type == that.type && Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size, type, normalized, layout);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VertexFormat.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("name='" + name + "'")
            .add("size=" + size)
            .add("type=" + type)
            .add("normalized=" + normalized)
            .add("layout=" + layout)
            .toString();
    }
}
