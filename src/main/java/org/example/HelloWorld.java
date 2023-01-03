package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {

    static float aVertices[] = {
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    };

    static Vector3f cubePositions[] = {
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(2.0f, 5.0f, -15.0f),
            new Vector3f(-1.5f, -2.2f, -2.5f),
            new Vector3f(-3.8f, -2.0f, -12.3f),
            new Vector3f(2.4f, -0.4f, -3.5f),
            new Vector3f(-1.7f, 3.0f, -7.5f),
            new Vector3f(1.3f, -2.0f, -2.5f),
            new Vector3f(1.5f, 2.0f, -2.5f),
            new Vector3f(1.5f, 0.2f, -1.5f),
            new Vector3f(-1.3f, 1.0f, -1.5f)
    };

    private static final String vertexShaderSource = HelloWorld.class.getResource("shader.vert").getFile();

    private static final String fragmentShaderSource = HelloWorld.class.getResource("shader.frag").getFile();

    public static void main(String[] args) throws IOException {

        // init
        GLFWErrorCallback.createPrint(System.err).set();
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(800, 600, "LearnOpenGL", NULL, NULL);
        if (window == NULL) {
            System.out.println("Failed to create GLFW window");
            glfwTerminate();
            return;
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glViewport(0, 0, 800, 600);
        glfwSetFramebufferSizeCallback(window, (window1, width, height) -> {
            glViewport(0, 0, width, height);
        });
        // end init

        int vertices = 3;
        int vertex_size = 3; // X, Y, Z,
        int color_size = 3; // R, G, B,

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(36 * 5);
        vertex_data.put(aVertices);
        vertex_data.flip();

        // testure
        int[] w = new int[1];
        int[] h = new int[1];
        stbi_set_flip_vertically_on_load(true);
        String imagePath = new File(HelloWorld.class.getResource("og.jpg").getPath()).toString();
        ByteBuffer image = stbi_load(imagePath, w, h, new int[1], 0);
        System.out.println(w[0]);
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w[0], h[0], 0, GL_RGB, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0); // stride 6 * sizeof(float)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12); // stride 6 * sizeof(float)
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);

        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
        shader.use();
        shader.setInt("ourTexture", 0);

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        while (!glfwWindowShouldClose(window)) {
            processInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // triangles
            shader.use();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                System.out.println(glfwGetTime());
                var model = new Matrix4f()
                        .rotate((float) toRadians(45), new Vector3f(1f, 0f, 0f).normalize())
                        .rotate((float) toRadians(0), new Vector3f(0f, -1f, -1f).normalize())
                        .rotate((float) glfwGetTime(), new Vector3f(-1f, 1f, -1f).normalize());
                var view = new Matrix4f()
                        .translate(0, 0, -3.0f);
                var projection = new Matrix4f()
                        .perspective((float) toRadians(45.0f), 800.0f / 600.0f, 0.1f, 100.0f);
                var fb = new Matrix4f(projection).mul(view).mul(model)
                        .get(stack.mallocFloat(16));
                shader.setMatrix4fv("transform", fb);
            }
            glBindVertexArray(VAO);
//            for(int i = 0; i < 10; i++)
//            {
//                glm::mat4 model = glm::mat4(1.0f);
//                model = glm::translate(model, cubePositions[i]);
//                float angle = 20.0f * i;
//                model = glm::rotate(model, glm::radians(angle), glm::vec3(1.0f, 0.3f, 0.5f));
//                ourShader.setMat4("model", model);
//
//                glDrawArrays(GL_TRIANGLES, 0, 36);
//            }
            glDrawArrays(GL_TRIANGLES, 0, 36);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glfwTerminate();
    }

    private static void processInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}