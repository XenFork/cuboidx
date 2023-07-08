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

package cuboidx.client;

import cuboidx.client.render.Camera;
import cuboidx.client.render.GameRenderer;
import cuboidx.client.render.world.BlockRenderer;
import cuboidx.client.render.world.WorldRenderer;
import cuboidx.client.texture.TextureManager;
import cuboidx.registry.Registries;
import cuboidx.util.ResourceLocation;
import cuboidx.world.World;
import cuboidx.world.block.BlockTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import overrungl.RuntimeHelper;
import overrungl.opengl.GL;
import overrungl.opengl.GLLoader;
import overrungl.glfw.Callbacks;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.util.MemoryStack;
import overrungl.util.value.Pair;
import org.overrun.timer.Timer;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class CuboidX implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    public static final String VERSION = "0.1.0";
    public static final double TPS = 20.0;
    private static CuboidX instance;
    private Thread renderThread;
    private MemorySegment window;
    private final AtomicInteger width = new AtomicInteger();
    private final AtomicInteger height = new AtomicInteger();
    private final AtomicBoolean resized = new AtomicBoolean();
    private final AtomicLong partialTick = new AtomicLong();
    private Timer timer;
    private Timer clientTimer;
    private TextureManager textureManager;
    private GameRenderer gameRenderer;
    private final Camera camera = new Camera();
    private BlockRenderer blockRenderer;
    private World world;
    private WorldRenderer worldRenderer;

    private CuboidX() {
        RuntimeHelper.setApiLogger(logger::error);
        GLFWErrorCallback.createLog(logger::error).set();
        RuntimeHelper.check(GLFW.init(), "Unable to initialize GLFW");
        GLFW.windowHint(GLFW.VISIBLE, false);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 3);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
        GLFW.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_COMPAT_PROFILE);
        GLFW.windowHint(GLFW.OPENGL_FORWARD_COMPAT, true);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            window = GLFW.createWindow(stack, 854, 480, "CuboidX " + VERSION, MemorySegment.NULL, MemorySegment.NULL);
        }
        RuntimeHelper.check(!RuntimeHelper.isNullptr(window), "Failed to create the GLFW window");
        GLFW.setFramebufferSizeCallback(window, (h /* TODO: _ doesn't support? */, width, height) -> {
            this.width.set(width);
            this.height.set(height);
            this.resized.set(true);
        });

        final GLFWVidMode.Value videoMode = GLFW.getVideoMode(GLFW.getPrimaryMonitor());
        if (videoMode != null) {
            final Pair.OfInt size = GLFW.getWindowSize(window);
            GLFW.setWindowPos(window,
                (videoMode.width() - size.x()) / 2,
                (videoMode.height() - size.y()) / 2);
        }

        try {
            logger.info("Starting CuboidX {}", VERSION);
            logger.info("""
                Detected 2 mods:
                    - {} {}
                    - java {}""", ResourceLocation.DEFAULT_NAMESPACE, VERSION, Runtime.version());

            final Pair.OfInt size = GLFW.getFramebufferSize(window);
            width.set(size.x());
            height.set(size.y());

            BlockTypes.load();
            logger.info("Registered {} blocks", Registries.BLOCK_TYPE.size());
            world = new World(256, 64, 256);
            camera.setPosition(128, 20, 128);

            timer = Timer.ofGetter(TPS, GLFW::getTime);

            GLFW.showWindow(window);
            renderThread = new Thread(this, "Render thread");
            renderThread.setUncaughtExceptionHandler((t, e) -> {
                logger.error("Exception thrown in render thread", e);
                GLFW.setWindowShouldClose(window, true);
            });
            renderThread.start();
            try {
                while (!GLFW.windowShouldClose(window)) {
                    timer.advanceTime();
                    timer.performTicks(this::tick);
                    partialTick.set(Double.doubleToLongBits(timer.partialTick()));
                    GLFW.pollEvents();
                }
                logger.info("Stopping!");
                try {
                    renderThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                close();
            }
        } catch (Exception e) {
            logger.error(e);
            renderThread.interrupt();
        } finally {
            Callbacks.free(window);
            GLFW.destroyWindow(window);
            GLFW.terminate();
        }
    }

    public void tick() {
        camera.tick();
        double xo = 0.0, yo = 0.0, zo = 0.0;
        if (GLFW.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo--;
        if (GLFW.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo++;
        if (GLFW.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo--;
        if (GLFW.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo++;
        if (GLFW.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo--;
        if (GLFW.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo++;
        if (GLFW.getKey(window, GLFW.KEY_LEFT_CONTROL) == GLFW.PRESS) {
            xo *= 3.0;
            yo *= 3.0;
            zo *= 3.0;
        }
        camera.moveRelative(xo, yo, zo, 0.1);
    }

    public void clientRender(double partialTick) {
        if (resized.get() && resized.compareAndSet(true, false)) {
            GL.viewport(0, 0, width.get(), height.get());
        }
        GL.clear(GL.COLOR_BUFFER_BIT | GL.DEPTH_BUFFER_BIT);
        gameRenderer.render(partialTick);
    }

    @Override
    public void run() {
        GLFW.makeContextCurrent(window);
        RuntimeHelper.check(GLLoader.loadConfined(true, GLFW::ngetProcAddress) != null, "Failed to load OpenGL");
        GL.clearColor(0.4f, 0.6f, 0.9f, 1.0f);
        textureManager = new TextureManager();
        gameRenderer = new GameRenderer(this);
        gameRenderer.init();
        blockRenderer = new BlockRenderer(this);
        worldRenderer = new WorldRenderer(this, world);

        clientTimer = Timer.ofGetter(TPS, GLFW::getTime);
        while (!GLFW.windowShouldClose(window)) {
            clientTimer.advanceTime();
            clientRender(partialTick());
            GLFW.swapBuffers(window);
            clientTimer.calcFPS();
        }
        clientClose();
    }

    private void clientClose() {
        gameRenderer.close();
        textureManager.close();
        worldRenderer.close();
    }

    private void close() {
    }

    public Thread renderThread() {
        return renderThread;
    }

    public MemorySegment window() {
        return window;
    }

    public int width() {
        return width.get();
    }

    public int height() {
        return height.get();
    }

    public double partialTick() {
        return Double.longBitsToDouble(partialTick.get());
    }

    public TextureManager textureManager() {
        return textureManager;
    }

    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    public Camera camera() {
        return camera;
    }

    public BlockRenderer blockRenderer() {
        return blockRenderer;
    }

    public World world() {
        return world;
    }

    public WorldRenderer worldRenderer() {
        return worldRenderer;
    }

    public static CuboidX getInstance() {
        if (instance == null) {
            instance = new CuboidX();
        }
        return instance;
    }
}
