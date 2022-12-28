package org.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {

    private static final String vertexShaderSource = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec4 aColor;
            
            out vec4 vertexColor;
            void main()
            {
                gl_Position = vec4(aPos, 1.0);
                vertexColor = aColor;
            }
            """;

    private static final String fragmentShaderSource = """
            #version 330 core
            out vec4 FragColor;
            in vec4 vertexColor;
            
            void main()
            {
                FragColor = vertexColor;
            }
            """;

    public static void main(String[] args) {

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

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(vertices * vertex_size);
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

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        IntBuffer success = BufferUtils.createIntBuffer(1);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        success.rewind();
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }

        int shaderProgram;
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        success.rewind();
        glGetShaderiv(shaderProgram, GL_LINK_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0); // stride 3 * sizeof(float)
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glBindVertexArray(VAO2);
        glBindBuffer(GL_ARRAY_BUFFER, VBO2);
        glBufferData(GL_ARRAY_BUFFER, vertex_data2, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0); // stride 3 * sizeof(float)
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

//        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        while(!glfwWindowShouldClose(window))
        {
            processInput(window);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            // triangles
            glUseProgram(shaderProgram);
            double timeValue = glfwGetTime();
            float greenValue = ((float) (sin(timeValue)) / 2.0f) + 0.5f;
            int vertexColorLocation = glGetUniformLocation(shaderProgram, "ourColor");
            glUniform4f(vertexColorLocation, 0.0f, greenValue, 0.0f, 1.0f);

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
        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private static void processInput(long window)
    {
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}