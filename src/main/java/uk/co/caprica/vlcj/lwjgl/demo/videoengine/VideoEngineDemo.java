/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2025 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.lwjgl.demo.videoengine;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoEngineVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngine;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineWindowCallback;

import java.util.concurrent.Semaphore;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetProcAddress;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Example showing how to use vlcj and the native LibVLC video engine callbacks to render video in an LWJGL application.
 * <p>
 * This example was adapted from the HelloWorld example provided in the LWJGL bundle.
 */
public class VideoEngineDemo {

    /**
     * Flag if resize behaviour should be enabled.
     */
    private static boolean enableResize = true;

    /**
     * Flag to preserve aspect ratio when resizing the window.
     */
    private static boolean preserveAspectRatio = true;

    /**
     * This application.
     */
    private static VideoEngineDemo app;

    /**
     * Native video engine videocube handler.
     */
    private final VideoEngineCallback videoEngineCallback = new VideoEngineHandler();

    /**
     * Semaphore used to synchronise the GL context - the context can only be active on one thread at a time.
     */
    private final Semaphore contextSemaphore = new Semaphore(0, true);

    /**
     * Media player factory.
     */
    private final MediaPlayerFactory mediaPlayerFactory;

    /**
     * Media player.
     */
    private final EmbeddedMediaPlayer mediaPlayer;

    /**
     * Video surface for the media player.
     */
    private final VideoEngineVideoSurface videoSurface;

    /**
     * Window.
     */
    private long window;

    /**
     * Component used to send size changed and mouse events via callbacks.
     * <p>
     * The application should invoke methods in this component when the size of the OpenGL video  rendering surface
     * changes, or when a mouse button is pressed or released.
     */
    private VideoEngineWindowCallback windowCallback;

    /**
     * Create a new demo application.
     */
    public VideoEngineDemo() {
        this.mediaPlayerFactory = new MediaPlayerFactory("--quiet");

        this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.videoSurface = mediaPlayerFactory.videoSurfaces().newVideoSurface(VideoEngine.libvlc_video_engine_opengl, videoEngineCallback);

        this.mediaPlayer.videoSurface().set(videoSurface);
    }

    /**
     * Start the application.
     */
    private void run(String mrl) {
        init();

        mediaPlayer.media().play(mrl);

        loop();

        // We need to make sure the callbacks stop before we disappear, otherwise a fatal JVM crash may occur
        mediaPlayer.release();
        mediaPlayerFactory.release();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error videocube
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * Initialise the main window.
     */
    private void init() {
        // Set up an error videocube - the default implementation will print the error message to System.err
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialise GLFW - most GLFW functions will not work before doing this
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Optionally configure GLFW, the current window hints are already the default
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, enableResize ? GLFW_TRUE : GLFW_FALSE);

        // Create the window
        window = glfwCreateWindow(800, 450, "vlcj OpenGL videocube rendering", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if (enableResize) {
            glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
                @Override
                public void invoke(long window, int width, int height) {
                    windowCallback.setSize(width, height);
                }
            });
        }

        // Setup a key videocube - it will be called every time a key is pressed, repeated or released
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                // We will detect this in the rendering loop
                glfwSetWindowShouldClose(window, true);
            }

            if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                mediaPlayer.controls().pause();
            }
        });

        glfwSetWindowPos(window, 10, 10);
        glfwMakeContextCurrent(window); // Make the OpenGL context current
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);
    }

    /**
     * Main loop.
     */
    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities
        // instance and makes the OpenGL bindings available for use
        GL.createCapabilities();

        // Set the clear colour
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glfwMakeContextCurrent(0L);
        contextSemaphore.release();

        // Run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
        while (!glfwWindowShouldClose(window)) {
            // Clear the framebuffer (no need to since the video will render the whole surface every frame?)
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glfwSwapBuffers(window);

            // Poll for window events - the key videocube above will only be invoked during this call
            glfwPollEvents();

            // Implement the render/timing loop however you want - if you don't sleep the loop will run as fast as it
            // can and consume 100% of a CPU core
            try {
                Thread.sleep(1000 / 60);
            } catch (Exception e) {
            }
        }
    }

    /**
     * This class is the bridge between the native video engine and the LWJGL rendering surface.
     * <p>
     * The semaphore may be a bit overkill since in this example the main thread never sets the current context again
     * after it has finished initialisation, however the first acquire at least protects us from a race during startup.
     * <p>
     * The videocube methods here all execute on a native thread coming from LibVLC.
     */
    private class VideoEngineHandler extends VideoEngineCallbackAdapter {
        @Override
        public void onSetWindowCallback(VideoEngineWindowCallback windowCallback) {
            VideoEngineDemo.this.windowCallback = windowCallback;
            int[] width = new int[1];
            int[] height = new int[1];
            glfwGetWindowSize(window, width, height);
            windowCallback.setSize(width[0], height[0]);
        }

        @Override
        public long onGetProcAddress(Long opaque, String functionName) {
            return glfwGetProcAddress(functionName);
        }

        @Override
        public boolean onMakeCurrent(Long opaque, boolean enter) {
            if (enter) {
                try {
                    contextSemaphore.acquire();
                    glfwMakeContextCurrent(window);
                } catch (InterruptedException e) {
                    return false;
                } catch (Exception e) {
                    glfwMakeContextCurrent(0L);
                    contextSemaphore.release();
                    return false;
                }
            } else {
                try {
                    glfwMakeContextCurrent(0L);
                } finally {
                    contextSemaphore.release();
                }
            }
            return true;
        }

        @Override
        public void onSwap(Long opaque) {
            glfwSwapBuffers(window);
        }
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.out.printf("LWJGL %s%n", Version.getVersion());

        if (args.length != 1) {
            System.out.println("Specify an MRL to play");
            System.exit(-1);
        }

        app = new VideoEngineDemo();
        app.run(args[0]);
    }
}
