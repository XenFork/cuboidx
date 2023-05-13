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

import cuboidx.client.render.GameRenderer;
import cuboidx.util.Log4jStream;
import cuboidx.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.overrun.glib.RuntimeHelper;
import org.overrun.glib.gl.GL;
import org.overrun.glib.gl.GLLoader;
import org.overrun.glib.glfw.Callbacks;
import org.overrun.glib.glfw.GLFW;
import org.overrun.glib.glfw.GLFWErrorCallback;
import org.overrun.glib.glfw.GLFWVidMode;
import org.overrun.glib.util.MemoryStack;
import org.overrun.glib.util.value.Value2;
import org.overrun.timer.Timer;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private GameRenderer gameRenderer;
    private Timer timer;
    private Timer clientTimer;
    private final AtomicInteger width = new AtomicInteger();
    private final AtomicInteger height = new AtomicInteger();
    private final AtomicBoolean resized = new AtomicBoolean();

    private CuboidX() {
        System.setOut(new Log4jStream(System.out, false));
        System.setErr(new Log4jStream(System.err, true));
        RuntimeHelper.setApiLogger(logger::error);
        GLFWErrorCallback.createLog(logger::error).set();
        RuntimeHelper.check(GLFW.init(), "Unable to initialize GLFW");
        GLFW.windowHint(GLFW.VISIBLE, false);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 3);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
        GLFW.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_COMPAT_PROFILE);
        GLFW.windowHint(GLFW.OPENGL_FORWARD_COMPAT, true);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            window = GLFW.createWindow(stack, 854, 480, "CuboidX", MemorySegment.NULL, MemorySegment.NULL);
        }
        RuntimeHelper.check(!RuntimeHelper.isNullptr(window), "Failed to create the GLFW window");
        GLFW.setFramebufferSizeCallback(window, (h /* TODO: _ */, width, height) -> {
            this.width.set(width);
            this.height.set(height);
            this.resized.set(true);
        });

        final GLFWVidMode.Value videoMode = GLFW.getVideoMode(GLFW.getPrimaryMonitor());
        if (videoMode != null) {
            final Value2.OfInt size = GLFW.getWindowSize(window);
            GLFW.setWindowPos(window,
                (videoMode.width() - size.x()) / 2,
                (videoMode.height() - size.y()) / 2);
        }

        try {
            logger.info("Starting CuboidX");
            logger.info("""
                Detected 2 mods:
                    - {} {}
                    - java {}""", ResourceLocation.DEFAULT_NAMESPACE, VERSION, Runtime.version());

            final Value2.OfInt size = GLFW.getFramebufferSize(window);
            width.set(size.x());
            height.set(size.y());

            timer = Timer.ofGetter(TPS, GLFW::getTime);

            GLFW.showWindow(window);
            renderThread = new Thread(this, "RenderThread");
            renderThread.start();
            try {
                while (!GLFW.windowShouldClose(window)) {
                    timer.advanceTime();
                    timer.performTicks(this::tick);
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
        } finally {
            Callbacks.free(window);
            GLFW.destroyWindow(window);
            GLFW.terminate();
        }
    }

    public void tick() {
    }

    public void render(double partialTick) {
        if (resized.get()) {
            resized.compareAndSet(true, false);
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
        gameRenderer = new GameRenderer();
        gameRenderer.init();
        clientTimer = Timer.ofGetter(TPS, GLFW::getTime);
        while (!GLFW.windowShouldClose(window)) {
            clientTimer.advanceTime();
            render(clientTimer.partialTick());
            GLFW.swapBuffers(window);
            clientTimer.calcFPS();
        }
        renderClose();
    }

    private void renderClose() {
        gameRenderer.close();
    }

    private void close() {
    }

    public Thread renderThread() {
        return renderThread;
    }

    public static CuboidX getInstance() {
        if (instance == null) {
            instance = new CuboidX();
        }
        return instance;
    }
}
