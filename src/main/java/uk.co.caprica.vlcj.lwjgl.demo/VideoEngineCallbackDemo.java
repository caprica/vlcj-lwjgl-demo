package uk.co.caprica.vlcj.lwjgl.demo;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetProcAddress;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
//import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
//import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
//import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.concurrent.Semaphore;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import com.sun.jna.Pointer;

import uk.co.caprica.vlcj.binding.internal.libvlc_video_engine_t;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoEngineVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.videoengine.VideoEngineCallbackAdapter;

/**
 * Example showing how to use vlcj and the native LibVLC video engine callbacks to render video in an LWJGL application.
 * <p>
 * This example was adapted from the HelloWorld example provided in the LWJGL bundle.
 */
public class VideoEngineCallbackDemo {

	/**
	 * This application.
	 */
	private static VideoEngineCallbackDemo app;
	
	/**
	 * Native video engine callback handler.
	 */
	private final VideoEngineCallback videoEngineCallback = new VideoEngineHandler();
	
	/**
	 * Semaphore used to synchronise the GL context - the context can only be active on one thread at a time.
	 */
	private final Semaphore contextSemaphore = new Semaphore(0);
	
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
	private VideoEngineVideoSurface videoSurface;
	
	/**
	 * Window.
	 */
	private long window;

	/**
	 * Create a new demo application.
	 */
	public VideoEngineCallbackDemo() {
		this.mediaPlayerFactory = new MediaPlayerFactory((NativeDiscovery) null);
		this.mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
		this.videoSurface = mediaPlayerFactory.videoSurfaces().newVideoSurface(libvlc_video_engine_t.libvlc_video_engine_opengl, videoEngineCallback);

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
		mediaPlayer.controls().stop();
		mediaPlayer.videoSurface().set(null);
 
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	/**
	 * Initialise the main window.
	 */
	private void init() {
		// Setup an error callback - the default implementation will print the error message to System.err
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialise GLFW - most GLFW functions will not work before doing this
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// Optionally configure GLFW, the current window hints are already the default
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(800, 600, "vlcj OpenGL callback rendering", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		// Setup a key callback - it will be called every time a key is pressed, repeated or released
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				// We will detect this in the rendering loop
				glfwSetWindowShouldClose(window, true); 
			}
		});

		glfwSetWindowPos(window, 200, 200);
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
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		glfwMakeContextCurrent(0L);
		contextSemaphore.release();
		
		// Run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
		while (!glfwWindowShouldClose(window)) {
			// Clear the framebuffer (no need to since the video will render the whole surface every frame)
//			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
//			glfwSwapBuffers(window);

			// Poll for window events - the key callback above will only be invoked during this call
			glfwPollEvents();
		}
	}

	/**
	 * This class is the bridge between the native video engine and the LWJGL rendering surface.
	 * <p>
	 * The semaphore may be a bit overkill since in this example the main thread never sets the current context again
	 * after it has finished initialisation, however the first acquire at least protects us from a race during startup.
	 * <p>
	 * The callback methods here all execute on a native thread coming from LibVLC.
	 */
	private class VideoEngineHandler extends VideoEngineCallbackAdapter {
		@Override
		public long onGetProcAddress(Pointer opaque, String functionName) {
			return glfwGetProcAddress(functionName);
		}

		@Override
		public boolean onMakeCurrent(Pointer opaque, boolean enter) {
			if (enter) {
				try {
					contextSemaphore.acquire();
					glfwMakeContextCurrent(window);
				}
				catch (InterruptedException e) {
					return false;
				}
				catch (Exception e) {
					glfwMakeContextCurrent(0L);
					contextSemaphore.release();
					return false;
				}
			} else {
				try {
					glfwMakeContextCurrent(0L);
				}
				finally {
					contextSemaphore.release();
				}
			}
			return true;
		}

		@Override
		public void onSwap(Pointer opaque) {
			glfwSwapBuffers(window);
		}

		@Override
		public void onUpdateOutput(Pointer opaque, int width, int height) {
			glfwSetWindowPos(window, 200, 200);
			glfwSetWindowSize(window, width, height);
		}
	}
	
	/**
	 * Application entry point.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		System.out.println(String.format("LWJGL %s", Version.getVersion()));

		if (args.length != 1) {
			System.out.println("Specify an MRL to play");
			System.exit(-1);
		}

		app = new VideoEngineCallbackDemo();
		app.run(args[0]);
	}

}
