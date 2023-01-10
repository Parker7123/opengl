package org.example;

import org.example.models.Model;
import org.joml.Matrix4f;
import org.joml.SimplexNoise;
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

    static float[] aVertices = {
            // positions          // normals           // texture coords
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
            0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,

            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
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

    static Vector3f cameraPos = new Vector3f(0, 0, 3);
    static Vector3f cameraUp = new Vector3f(0, 1, 0);
    static Vector3f cameraFront = new Vector3f(0, 0, -1);

    static float lastX = 400, lastY = 300;
    static float yaw = -90f, pitch = 0;
    static float fov = 45;
    static boolean firstMouse;
    static Camera camera = new Camera();

    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastFrame = 0.0f; // Time of last frame

    private static final String vertexShaderSource = HelloWorld.class.getResource("shader.vert").getFile();

    private static final String fragmentShaderSource = HelloWorld.class.getResource("shader.frag").getFile();

    private static final String lightVertexShaderSource = HelloWorld.class.getResource("light_shader.vert").getFile();

    private static final String lightFragmentShaderSource = HelloWorld.class.getResource("light_shader.frag").getFile();

    static Vector3f lightColor = new Vector3f(1, 1, 1);
    static Vector3f lightPos = new Vector3f(-2, 1, -5);

    public static void main(String[] args) throws IOException {

        // init
        GLFWErrorCallback.createPrint(System.err).set();
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SAMPLES, 4);

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
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        camera.startProcessingMouseMovement(window);
        // end init

        int vertices = 3;
        int vertex_size = 3; // X, Y, Z,
        int color_size = 3; // R, G, B,

        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(36 * 8);
        vertex_data.put(aVertices);
        vertex_data.flip();

        // texsture
//        int texture = loadTexture("og.jpg");
//        int specularMap = loadTexture("og_specular.jpg");
        int VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 32, 12);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 32, 24);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        // light
        int lightVAO = glGenVertexArrays();
        glBindVertexArray(lightVAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        Shader lightShader = new Shader(lightVertexShaderSource, lightFragmentShaderSource);
        lightShader.use();
        lightShader.setVec3("lightColor", lightColor);

        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
        shader.use();
        shader.setInt("material.diffuse", 0);
        shader.setInt("material.specular", 1);

        shader.setVec3("light.position", lightPos);
        shader.setVec3("light.ambient", new Vector3f(0.2f, 0.2f, 0.2f));
        shader.setVec3("light.diffuse", new Vector3f(0.8f, 0.8f, 0.8f));
        shader.setVec3("light.specular", new Vector3f(1f, 1f, 1f));

        shader.setFloat("light.constant",  1.0f);
        shader.setFloat("light.linear",    0.045f);
        shader.setFloat("light.quadratic", 0.0075f);
        shader.setInt("useTexture", 0);
        Model backpack = new Model(HelloWorld.class.getResource("luigi.obj").getPath());
        Model track = new Model(HelloWorld.class.getResource("crircuito.obj").getPath());
        Model car = new Model(HelloWorld.class.getResource("RacingCar.obj").getPath());

//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;
            processInput(window);
            camera.processInput(window, deltaTime);

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            var view = camera.getViewMatrix();
            var projection = new Matrix4f()
                    .perspective((float) toRadians(camera.getFov()), 800.0f / 600.0f, 0.1f, 100.0f);

            // triangles
            shader.use();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                var model = new Matrix4f()
//                        .scale(0.01f)
                        .rotate((float) toRadians(0f), new Vector3f(1f, 0f, 0f));
                shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                shader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                shader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                shader.setVec3("viewPos", camera.getCameraPos());
                shader.setVec3("light.position",  camera.getCameraPos());
                shader.setVec3("light.direction", camera.getCameraFront());
                shader.setFloat("light.cutOff",   (float) cos(toRadians(10)));
                shader.setFloat("light.outerCutOff",   (float) cos(toRadians(17.5)));
                shader.setInt("useTexture", 0);
                track.draw(shader);
                model = new Matrix4f()
                        .translate(new Vector3f((float) cos(glfwGetTime()) - 3, 0.1f, (float) sin(glfwGetTime()) + 3))
                        .rotate((float) glfwGetTime(), new Vector3f(0f, 1f, 0f))
                        .scale(0.01f);
                shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                shader.setInt("useTexture", 1);
                backpack.draw(shader);

                // car
                model = new Matrix4f()
                        .scale(0.01f);
                shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                shader.setInt("useTexture", 1);
                car.draw(shader);
            }
            // light
            lightShader.use();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var model = new Matrix4f()
                        .translate(lightPos)
                        .scale(0.2f);
                lightShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                lightShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                lightShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                glBindVertexArray(lightVAO);
                glDrawArrays(GL_TRIANGLES, 0, 36);
            }
            glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(VAO);
        glDeleteBuffers(VBO);
        glfwTerminate();
    }

    private static int loadTexture(String path) {
        int[] w = new int[1];
        int[] h = new int[1];
        int[] components = new int[1];
        stbi_set_flip_vertically_on_load(true);
        String imagePath = new File(HelloWorld.class.getResource(path).getPath()).toString();
        ByteBuffer image = stbi_load(imagePath, w, h, components, 0);
        int format = GL_RGB;
        if(components[0] == 4) format = GL_RGBA;
        System.out.println(components[0]);
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w[0], h[0], 0, format, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        return texture;
    }

    private static void processInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}