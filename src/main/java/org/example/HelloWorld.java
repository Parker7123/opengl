package org.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {

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
        if (window == NULL)
        {
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

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(vertices * (vertex_size + color_size));
        vertex_data.put(new float[] { -0.5f, -0.5f, 0f, 1.0f, 0.0f, 0.0f });
        vertex_data.put(new float[] { -0.5f, 0.5f, 0f, 0.0f, 1.0f, 0.0f});
        vertex_data.put(new float[] { 0.5f, -0.5f, 0f, 0.0f, 0.0f, 1.0f});
        vertex_data.flip();

        FloatBuffer vertex_data2 = BufferUtils.createFloatBuffer(vertices * vertex_size);
        vertex_data2.put(new float[] { -0.5f, 0.5f, 0f, });
        vertex_data2.put(new float[] { 0.5f, 0.5f, 0f, });
        vertex_data2.put(new float[] { 0.5f, -0.5f, 0f, });
        vertex_data2.flip();

        IntBuffer indices = BufferUtils.createIntBuffer(6);
        indices.put(new int[] { 0, 1, 3 });
        indices.put(new int[] { 1, 2, 3 });
        indices.flip();



        int VAO = glGenVertexArrays();
        int VAO2 = glGenVertexArrays();
        int VBO = glGenBuffers();
        int VBO2 = glGenBuffers();
//        int EBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0); // stride 6 * sizeof(float)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12); // stride 6 * sizeof(float)
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glBindVertexArray(VAO2);
        glBindBuffer(GL_ARRAY_BUFFER, VBO2);
        glBufferData(GL_ARRAY_BUFFER, vertex_data2, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0); // stride 3 * sizeof(float)
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        while(!glfwWindowShouldClose(window))
        {
            processInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            // triangles
            shader.use();
            double timeValue = glfwGetTime();
            float greenValue = ((float) (sin(timeValue)) / 2.0f) + 0.5f;
            shader.set4Float("uniformColor", 0f, greenValue, 0f, 1f);

            glBindVertexArray(VAO);
            glDrawArrays(GL_TRIANGLES, 0, 3);
//            glBindVertexArray(VAO2);
//            glDrawArrays(GL_TRIANGLES, 0, 3);
//            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glfwTerminate();
    }

    private static void processInput(long window)
    {
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}