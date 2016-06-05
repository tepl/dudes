package com.tearulez.dudes;

/**
 * Created by Teplovoz on 6/5/2016.
 */

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.Map;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class HelloWorld {

    private GameModel model = new GameModel();

    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try {
            initModel();
            initGraphics();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void initModel() {
        model.init(0);
    }

    private void initGraphics() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        int WIDTH = 300;
        int HEIGHT = 300;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (!model.isInitialized()) return;
                switch (key) {
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
                        break;
                    case GLFW_KEY_LEFT:
                        model.movePlayer(-0.01f, 0);
                        break;
                    case GLFW_KEY_RIGHT:
                        model.movePlayer(0.01f, 0);
                        break;
                    case GLFW_KEY_DOWN:
                        model.movePlayer(0, -0.01f);
                        break;
                    case GLFW_KEY_UP:
                        model.movePlayer(0, 0.01f);
                        break;
                }
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            if (model.isInitialized()) {
                int playerId = model.getPlayerId();
                for (Map.Entry<Integer, Position> positionEntry : model.getPositions().entrySet()) {
                    Position p = positionEntry.getValue();
                    int characterId = positionEntry.getKey();
                    // Draw blue rectangle
                    if (characterId == playerId) {
                        glColor3f(0.0f, 1.0f, 0.0f);
                    } else {
                        glColor3f(1.0f, 0.0f, 0.0f);
                    }
                    float halfSize = 0.1f;
                    glRectf(p.x - halfSize, p.y - halfSize, p.x + halfSize, p.y + halfSize);
                }
            }

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

}