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

package cuboidx.util.math;

import org.joml.FrustumIntersection;
import org.joml.Intersectiond;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author squid233
 * @since 0.1.0
 */
public final /* value */ class AABBox {
    private static final AABBox EMPTY = new AABBox(true, 0, 0, 0, 0, 0, 0);
    private static final AABBox FULL_CUBE = of(0, 0, 0, 1, 1, 1);
    private final boolean isEmpty;
    private final double minX, minY, minZ, maxX, maxY, maxZ;

    private AABBox(boolean isEmpty,
                   double minX,
                   double minY,
                   double minZ,
                   double maxX,
                   double maxY,
                   double maxZ) {
        this.isEmpty = isEmpty;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static AABBox of(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABBox(false, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static AABBox empty() {
        return EMPTY;
    }

    public static AABBox fullCube() {
        return FULL_CUBE;
    }

    public AABBox move(double x, double y, double z) {
        return of(minX() + x, minY() + y, minZ() + z, maxX() + x, maxY() + y, maxZ() + z);
    }

    public boolean test(FrustumIntersection frustum) {
        return frustum.testAab((float) minX(), (float) minY(), (float) minZ(), (float) maxX(), (float) maxY(), (float) maxZ());
    }

    public Direction testSide(double originX, double originY, double originZ,
                              double dirX, double dirY, double dirZ) {
        for (Direction direction : Direction.list()) {
            final double t = intersectSide(originX, originY, originZ, dirX, dirY, dirZ, direction);
            if (t != -1) return direction;
        }
        return Direction.SOUTH;
    }

    public double intersectSide(
        double originX, double originY, double originZ,
        double dirX, double dirY, double dirZ,
        Direction side) {
        final double epsilon = 0.001;
        return switch (side) {
            case WEST, EAST -> {
                final double vX = side.axisX() < 0 ? minX() : maxX();
                final double v0Z = side.axisX() < 0 ? minZ() : maxZ();
                final double v1Z = side.axisX() < 0 ? maxZ() : minZ();
                final double t1 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    vX, maxY(), v0Z,
                    vX, minY(), v0Z,
                    vX, minY(), v1Z,
                    epsilon
                );
                final double t2 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    vX, minY(), v1Z,
                    vX, maxY(), v1Z,
                    vX, maxY(), v0Z,
                    epsilon
                );
                yield t1 == -1 ? t2 : t1;
            }
            case DOWN, UP -> {
                final double vY = side.axisY() < 0 ? minY() : maxY();
                final double v0Z = side.axisY() < 0 ? maxZ() : minZ();
                final double v1Z = side.axisY() < 0 ? minZ() : maxZ();
                final double t1 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX(), vY, v0Z,
                    minX(), vY, v1Z,
                    maxX(), vY, v1Z,
                    epsilon
                );
                final double t2 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX(), vY, v1Z,
                    maxX(), vY, v0Z,
                    minX(), vY, v0Z,
                    epsilon
                );
                yield t1 == -1 ? t2 : t1;
            }
            case NORTH, SOUTH -> {
                final double vZ = side.axisZ() < 0 ? minZ() : maxZ();
                final double v0X = side.axisZ() < 0 ? maxX() : minX();
                final double v1X = side.axisZ() < 0 ? minX() : maxX();
                final double t1 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    v0X, maxY(), vZ,
                    v0X, minY(), vZ,
                    v1X, minY(), vZ,
                    epsilon
                );
                final double t2 = Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    v1X, minY(), vZ,
                    v1X, maxY(), vZ,
                    v0X, maxY(), vZ,
                    epsilon
                );
                yield t1 == -1 ? t2 : t1;
            }
        };
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public double minX() {
        return minX;
    }

    public double minY() {
        return minY;
    }

    public double minZ() {
        return minZ;
    }

    public double maxX() {
        return maxX;
    }

    public double maxY() {
        return maxY;
    }

    public double maxZ() {
        return maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AABBox aabBox)) return false;
        return isEmpty == aabBox.isEmpty && Double.compare(minX, aabBox.minX) == 0 && Double.compare(minY, aabBox.minY) == 0 && Double.compare(minZ, aabBox.minZ) == 0 && Double.compare(maxX, aabBox.maxX) == 0 && Double.compare(maxY, aabBox.maxY) == 0 && Double.compare(maxZ, aabBox.maxZ) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEmpty, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AABBox.class.getSimpleName() + "[", "]")
            .add("isEmpty=" + isEmpty)
            .add("minX=" + minX)
            .add("minY=" + minY)
            .add("minZ=" + minZ)
            .add("maxX=" + maxX)
            .add("maxY=" + maxY)
            .add("maxZ=" + maxZ)
            .toString();
    }
}
