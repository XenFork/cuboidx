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

package cuboidx.client;

import cuboidx.client.event.MouseEvent;
import cuboidx.client.render.Camera;
import cuboidx.client.render.GameRenderer;
import cuboidx.client.render.world.BlockRenderer;
import cuboidx.client.render.world.WorldRenderer;
import cuboidx.client.texture.TextureManager;
import cuboidx.event.EventBus;
import cuboidx.event.RegistryEvent;
import cuboidx.registry.Registries;
import cuboidx.util.AtomicDouble;
import cuboidx.util.ResourceLocation;
import cuboidx.util.math.Direction;
import cuboidx.world.HitResult;
import cuboidx.world.World;
import cuboidx.world.block.BlockTypes;
import cuboidx.world.entity.Entity;
import cuboidx.world.entity.EntityTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
import org.overrun.timer.Timer;
import overrungl.OverrunGL;
import overrungl.glfw.GLFW;
import overrungl.glfw.GLFWCallbacks;
import overrungl.glfw.GLFWErrorCallback;
import overrungl.glfw.GLFWVidMode;
import overrungl.opengl.GL;
import overrungl.opengl.GLLoader;
import overrungl.util.CheckUtil;
import overrungl.util.value.Pair;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class CuboidX implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    public static final String VERSION = "0.1.0";
    public static final double TPS = 20.0;
    public static final EventBus EVENT_BUS = new EventBus();
    private static final double MOUSE_SENSITIVITY = 0.15;
    private Thread renderThread;
    private MemorySegment window;
    private Mouse mouse;
    private final AtomicInteger width = new AtomicInteger();
    private final AtomicInteger height = new AtomicInteger();
    private final AtomicBoolean resized = new AtomicBoolean();
    private final AtomicBoolean shouldRender = new AtomicBoolean(true);
    private final AtomicBoolean interrupted = new AtomicBoolean();
    private final AtomicLong partialTick = new AtomicLong();
    private final AtomicLong currentTime = new AtomicLong();
    private final DoubleSupplier currentTimeGetter = () -> AtomicDouble.get(currentTime);
    private Timer timer;
    private Timer clientTimer;
    private TextureManager textureManager;
    private GameRenderer gameRenderer;
    private final Camera camera = new Camera();
    private BlockRenderer blockRenderer;
    private World world;
    private WorldRenderer worldRenderer;
    private Entity player;

    private CuboidX() {
        OverrunGL.setApiLogger(logger::error);
        GLFWErrorCallback.createLog(logger::error).set();
        CheckUtil.check(GLFW.init(), "Unable to initialize GLFW");
        GLFW.windowHint(GLFW.VISIBLE, false);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 3);
        GLFW.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
        GLFW.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_COMPAT_PROFILE);
        GLFW.windowHint(GLFW.OPENGL_FORWARD_COMPAT, true);
        window = GLFW.createWindow(854, 480, STR. "CuboidX \{ VERSION }" , MemorySegment.NULL, MemorySegment.NULL);
        CheckUtil.checkNotNullptr(window, "Failed to create the GLFW window");
        mouse = new Mouse(window);
        EVENT_BUS.addListener(MouseEvent.CursorPos.ID, this::onCursorPos);
        EVENT_BUS.addListener(MouseEvent.Button.ID, this::onMouseButton);
        GLFW.setKeyCallback(window, this::onKey);
        GLFW.setFramebufferSizeCallback(window, this::onResize);
        GLFW.setWindowIconifyCallback(window, (/* TODO: unnamed parameter is NOT supported */ h, iconified) -> this.shouldRender.set(!iconified));

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
            EVENT_BUS.post(new RegistryEvent<>(Registries.BLOCK_TYPE));
            logger.info("Registered {} blocks", Registries.BLOCK_TYPE.size());
            EntityTypes.load();
            EVENT_BUS.post(new RegistryEvent<>(Registries.ENTITY_TYPE));
            logger.info("Registered {} entities", Registries.ENTITY_TYPE.size());

            world = new World(256, 256, 256);
            player = world().spawn(EntityTypes.PLAYER, 128, 100, 128).get();

            timer = Timer.ofGetter(TPS, currentTimeGetter);

            GLFW.showWindow(window);
            renderThread = new Thread(this, "Render thread");
            renderThread.setUncaughtExceptionHandler((t, e) -> {
                logger.error("Exception thrown in render thread", e);
                GLFW.setWindowShouldClose(window, true);
            });
            renderThread.start();
            try {
                while (!GLFW.windowShouldClose(window)) {
                    AtomicDouble.set(currentTime, GLFW.getTime());
                    timer.advanceTime();
                    timer.performTicks(this::tick);
                    lateUpdate();
                    AtomicDouble.set(partialTick, timer.partialTick());
                    GLFW.pollEvents();
                }
                logger.info("Stopping!");
                try {
                    renderThread.join(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                close();
            }
        } catch (Exception e) {
            interrupted.set(true);
            logger.error(e);
            renderThread.interrupt();
        } finally {
            GLFWCallbacks.free(window);
            GLFW.destroyWindow(window);
            GLFW.terminate();
        }
    }

    private void onCursorPos(MouseEvent.CursorPos event) {
        if (mouse.mouseGrabbed()) {
            player().rotate(
                -Math.toRadians(event.deltaY() * MOUSE_SENSITIVITY),
                -Math.toRadians(event.deltaX() * MOUSE_SENSITIVITY),
                0);
        }
    }

    private void onMouseButton(MouseEvent.Button event) {
        if (event.action() == GLFW.PRESS) {
            switch (event.button()) {
                case GLFW.MOUSE_BUTTON_LEFT -> {
                    final HitResult result = worldRenderer().hitResult();
                    if (result != null && !result.missed()) {
                        world().setBlock(result.x(), result.y(), result.z(), BlockTypes.AIR);
                    }
                }
                case GLFW.MOUSE_BUTTON_RIGHT -> {
                    final HitResult result = worldRenderer().hitResult();
                    if (result != null && !result.missed()) {
                        final Direction side = result.side();
                        world().setBlock(result.x() + side.axisX(), result.y() + side.axisY(), result.z() + side.axisZ(), BlockTypes.DIRT);
                    }
                }
            }
        }
    }

    private void onKey(MemorySegment window, int key, int scancode, int action, int mods) {
        if (action == GLFW.RELEASE) {
            if (key == GLFW.KEY_GRAVE_ACCENT) {
                mouse.setMouseGrabbed(!mouse.mouseGrabbed());
            }
            if (key == GLFW.KEY_F3 && worldRenderer() != null) {
                worldRenderer().setShouldRenderDebugHud(!worldRenderer().shouldRenderDebugHud());
            }
        }
    }

    private void onResize(MemorySegment window, int width, int height) {
        this.width.set(width);
        this.height.set(height);
        this.resized.set(true);
    }

    public void tick() {
        player().tick();
        camera().tick();
        double xo = 0.0, yo = 0.0, zo = 0.0;
        if (GLFW.getKey(window, GLFW.KEY_A) == GLFW.PRESS) xo--;
        if (GLFW.getKey(window, GLFW.KEY_D) == GLFW.PRESS) xo++;
        if (GLFW.getKey(window, GLFW.KEY_LEFT_SHIFT) == GLFW.PRESS) yo--;
        if (GLFW.getKey(window, GLFW.KEY_SPACE) == GLFW.PRESS) yo++;
        if (GLFW.getKey(window, GLFW.KEY_W) == GLFW.PRESS) zo--;
        if (GLFW.getKey(window, GLFW.KEY_S) == GLFW.PRESS) zo++;
        final boolean sprint = GLFW.getKey(window, GLFW.KEY_LEFT_CONTROL) == GLFW.PRESS;
        player().moveRelative(xo, yo, zo, sprint ? 0.8 : 0.6);
    }

    public void lateUpdate() {
        camera().moveToEntity(player());
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
        CheckUtil.checkNotNull(GLLoader.load(GLFW::getProcAddress, true), "Failed to load OpenGL");
        GL.clearColor(0.4f, 0.6f, 0.9f, 1.0f);
        textureManager = new TextureManager();
        gameRenderer = new GameRenderer(this);
        blockRenderer = new BlockRenderer(this);
        worldRenderer = new WorldRenderer(this, world);

        clientTimer = Timer.ofGetter(TPS, currentTimeGetter);
        try {
            while (!GLFW.windowShouldClose(window)) {
                clientTimer.advanceTime();
                if (shouldRender.get() && !interrupted.get()) {
                    clientRender(partialTick());
                    GLFW.swapBuffers(window);
                }
                clientTimer.calcFPS();
            }
        } finally {
            clientClose();
        }
    }

    private void clientClose() {
        logger.info("Cleaning up client resources");
        gameRenderer.close();
        textureManager.close();
        worldRenderer().close();
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
        return AtomicDouble.get(partialTick);
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

    public Entity player() {
        return player;
    }

    public static CuboidX getInstance() {
        class Holder {
            private static final CuboidX INSTANCE = new CuboidX();
        }
        return Holder.INSTANCE;
    }
}
