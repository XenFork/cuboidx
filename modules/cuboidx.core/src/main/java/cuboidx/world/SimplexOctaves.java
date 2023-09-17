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

package cuboidx.world;

import org.joml.SimplexNoise;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class SimplexOctaves {
    // https://cmaher.github.io/posts/working-with-simplex-noise/
    public static float sumOctaves(int numIterations, float x, float y, float persistence, float scale, float low, float high) {
        float maxAmp = 0f;
        float amp = 1f;
        float freq = scale;
        float noise = 0f;

        // add successively smaller, higher-frequency terms
        for (int i = 0; i < numIterations; i++) {
            noise += SimplexNoise.noise(x * freq, y * freq) * amp;
            maxAmp += amp;
            amp *= persistence;
            freq *= 2f;
        }

        // take the average value of the iterations
        noise /= maxAmp;

        // normalize the result
        return noise * (high - low) * 0.5f + (high + low) * 0.5f;
    }

    public static float sumOctaves(int numIterations, float x, float y, float z, float persistence, float scale, float low, float high) {
        float maxAmp = 0f;
        float amp = 1f;
        float freq = scale;
        float noise = 0f;

        // add successively smaller, higher-frequency terms
        for (int i = 0; i < numIterations; i++) {
            noise += SimplexNoise.noise(x * freq, y * freq, z * freq) * amp;
            maxAmp += amp;
            amp *= persistence;
            freq *= 2f;
        }

        // take the average value of the iterations
        noise /= maxAmp;

        // normalize the result
        return noise * (high - low) * 0.5f + (high + low) * 0.5f;
    }

    public static float sumOctaves(int numIterations, float x, float y, float z, float w, float persistence, float scale, float low, float high) {
        float maxAmp = 0f;
        float amp = 1f;
        float freq = scale;
        float noise = 0f;

        // add successively smaller, higher-frequency terms
        for (int i = 0; i < numIterations; i++) {
            noise += SimplexNoise.noise(x * freq, y * freq, z * freq, w * freq) * amp;
            maxAmp += amp;
            amp *= persistence;
            freq *= 2f;
        }

        // take the average value of the iterations
        noise /= maxAmp;

        // normalize the result
        return noise * (high - low) * 0.5f + (high + low) * 0.5f;
    }
}
