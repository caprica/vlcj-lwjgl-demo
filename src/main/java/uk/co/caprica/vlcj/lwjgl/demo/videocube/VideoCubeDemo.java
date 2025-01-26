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

package uk.co.caprica.vlcj.lwjgl.demo.videocube;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.lwjgl.demo.videocube.cubes.MultiTextureCube;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.StandardBufferFormat;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.system.MemoryUtil.NULL;
import static uk.co.caprica.vlcj.lwjgl.demo.videocube.textures.EmptyTexture.emptyTexture;
import static uk.co.caprica.vlcj.lwjgl.demo.videocube.textures.ImageFileTexture.imageFileTexture;

/**
 * This is a somewhat naive proof-of-concept, it does not pretend to be an optimal OpenGL/LWJGL implementation.
 */
public class VideoCubeDemo {

    private final int WINDOW_WIDTH = 1200;
    private final int WINDOW_HEIGHT = 1200;

    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;

    private final int videoWidth = 1920;
    private final int videoHeight = 1090;

    private final int[] buffer = new int[videoWidth * videoHeight];
    private final int[] glBuffer = new int[buffer.length];

    {
        Arrays.fill(buffer, 0xFF000000);
        Arrays.fill(glBuffer, 0xFF000000);
    }

    private GLFWKeyCallback keyCallback;

    private long window;

    private int textureIdVideo;
    private int textureIdImage;

    public VideoCubeDemo() {
        mediaPlayerFactory = new MediaPlayerFactory("--no-audio");
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(new CallbackVideoSurface(new CubeBufferFormatCallback(), new CubeRenderFormatCallback(buffer, glBuffer), true));
    }

    public void run(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Specify a video file");
                System.exit(1);
            }

            init();
            loop(args);

            glfwDestroyWindow(window);
            keyCallback.free();
        } finally {
            glfwTerminate();
        }
    }

    private void init() {
        // Set up an error callback - the default implementation will print the error message to System.err
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW, most GLFW functions will not work before doing this
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "vlcj video cube demo", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode == null) {
            throw new RuntimeException("Failed to get the primary monitor video mode");
        }
        glfwSetWindowPos(
            window,
            (vidmode.width() - WINDOW_WIDTH) / 2,
            (vidmode.height() - WINDOW_HEIGHT) / 2
        );

        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        glfwShowWindow(window);
    }

    private void loop(String[] args) {
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
        // externally. LWJGL detects the context that is current in the current thread, creates the ContextCapabilities
        // instance and makes the OpenGL bindings available for use
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0, 0, 0, 0);
        glEnable(GL_BLEND);

        float rotAngle = 0.3f;

        textureIdVideo = emptyTexture(videoWidth, videoHeight);
        textureIdImage = imageFileTexture("./side.jpg");

        MultiTextureCube cube = new MultiTextureCube(new int[] {textureIdImage, textureIdImage, textureIdVideo, textureIdVideo, textureIdVideo, textureIdVideo});
//        SingleTextureCube cube = new SingleTextureCube(textureId);

        mediaPlayer.media().play(args[0]);

        // Run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glTranslatef(0.0f, 0, 0.0f);

            glMatrixMode(GL_MODELVIEW);

            GL11.glRotatef(rotAngle, 0.5f, 0.5f, -0.5f);

            synchronized (glBuffer) {
                glBindTexture(GL_TEXTURE_2D, textureIdVideo);
                GL11.glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGBA, GL_UNSIGNED_BYTE, glBuffer);
            }

            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR) {
                System.out.println("OpenGL Error: " + error);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LESS);

            cube.render();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    private static class CubeBufferFormatCallback extends BufferFormatCallbackAdapter {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new StandardBufferFormat(sourceWidth, sourceHeight);
        }
    }

    private static class CubeRenderFormatCallback extends RenderCallbackAdapter {

        private final int[] textureBuffer;

        public CubeRenderFormatCallback(int[] videoBuffer, int[] textureBuffer) {
            this.textureBuffer = textureBuffer;
            setBuffer(videoBuffer);
        }

        @Override
        protected void onLock(MediaPlayer mediaPlayer) {
        }

        @Override
        protected void onDisplay(MediaPlayer mediaPlayer, int[] buffer) {
            // This is very naive, but does prevent OpenGL rendering the texture buffer while the video buffer is
            // updating - a more sophisticated implementation would not have this extra buffer and could also make use
            // of lock/unlock
            synchronized (textureBuffer) {
                System.arraycopy(buffer, 0, textureBuffer, 0, buffer.length);
            }
        }

        @Override
        protected void onUnlock(MediaPlayer mediaPlayer) {
        }
    }

    public static void main(String[] args) {
        new VideoCubeDemo().run(args);
    }
}
