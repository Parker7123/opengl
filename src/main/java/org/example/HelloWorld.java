package org.example;

import org.example.lights.DirectionalLight;
import org.example.lights.PointLight;
import org.example.lights.SpotLight;
import org.example.models.CameraMovementType;
import org.example.models.Model;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static org.example.models.CameraMovementType.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {
    static Camera camera = new Camera();
    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastFrame = 0.0f; // Time of last frame

    private static final String vertexShaderSource = HelloWorld.class.getResource("shader.vert").getFile();

    private static final String fragmentShaderSource = HelloWorld.class.getResource("shader.frag").getFile();

    private static final String lightVertexShaderSource = HelloWorld.class.getResource("light_shader.vert").getFile();

    private static final String lightFragmentShaderSource = HelloWorld.class.getResource("light_shader.frag").getFile();

    private static final List<PointLight> pointLights = new ArrayList<>();
    private static final List<SpotLight> spotLights = new ArrayList<>();
    private static final int NR_POINT_LIGHTS = 1;
    private static final int NR_SPOT_LIGHTS = 1;
    private static CameraMovementType cameraMovementType = FOLLOW;

    public static void main(String[] args) throws IOException {

        // init
        GLFWErrorCallback.createPrint(System.err).set();
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SAMPLES, 4);

        long window = glfwCreateWindow(1920, 1080, "LearnOpenGL", NULL, NULL);
        if (window == NULL) {
            System.out.println("Failed to create GLFW window");
            glfwTerminate();
            return;
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glViewport(0, 0, 1920, 1080);
        glfwSetFramebufferSizeCallback(window, (window1, width, height) -> {
            glViewport(0, 0, width, height);
        });
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
//        camera.startProcessingMouseMovement(window);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);


        // initial shader and light configuration
        Shader lightShader = new Shader(lightVertexShaderSource, lightFragmentShaderSource);
        Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
        shader.use();

        DirectionalLight directionalLight = DirectionalLight.builder()
                .direction(new Vector3f(0, -1f, 0))
                .ambient(new Vector3f(0.15f, 0.15f, 0.15f))
                .diffuse(new Vector3f(0.15f, 0.15f, 0.15f))
                .specular(new Vector3f(0.15f, 0.15f, 0.15f))
                .build();

        directionalLight.applyUniforms(shader);
        //  PointLights
        PointLight pointLight = PointLight.builder()
                .index(0)
                .position(new Vector3f(-2, 1, -5))
                .ambient(new Vector3f(0.15f, 0.15f, 0.15f))
                .diffuse(new Vector3f(0.25f, 0.25f, 0.25f))
                .specular(new Vector3f(0.3f, 0.53f, 0.3f))
                .constant(1.0f)
                .linear(0.045f)
                .quadratic(0.0075f)
                .build();
        pointLight.init();
        pointLights.add(pointLight);
        // SpotLights
        SpotLight spotLight = SpotLight.builder()
                .index(0)
                .position(new Vector3f(-10, 1, -5))
                .direction(new Vector3f(0, -1f, 0))
                .ambient(new Vector3f(0.15f, 0.15f, 0.15f))
                .diffuse(new Vector3f(0.3f, 0.3f, 0.3f))
                .specular(new Vector3f(0.3f, 0.53f, 0.3f))
                .constant(1.0f)
                .linear(0.045f)
                .quadratic(0.0075f)
                .cutOff((float) cos(toRadians(10)))
                .outerCutOff((float) cos(toRadians(17)))
                .build();
        spotLights.add(spotLight);

        shader.setInt("useTexture", 0);
        Model backpack = new Model(HelloWorld.class.getResource("luigi.obj").getPath());
        Model track = new Model(HelloWorld.class.getResource("crircuito.obj").getPath());
        Model car = new Model(HelloWorld.class.getResource("RacingCar.obj").getPath());

        File positionFile = new File("position.txt");
        List<Vector3f[]> positions = Files.readAllLines(new File(HelloWorld.class.getResource("position.txt").getPath()).toPath()).stream()
                .map(line -> line.split(" "))
                .map(l-> new Vector3f[] {
                        new Vector3f(Float.parseFloat(l[0]), Float.parseFloat(l[1]), Float.parseFloat(l[2])),
                        new Vector3f(Float.parseFloat(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]))
                })
                .toList();
        int frame = 0;
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        var carFront = new Vector2f(0f, -1f).normalize();

        while (!glfwWindowShouldClose(window)) {
            frame++;
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            // carPos
            var tFront = positions.get(frame % positions.size())[1];
            var nextCarFront = new Vector2f(tFront.x, tFront.z).normalize();
            var carPos = positions.get(frame % positions.size())[0];
            var carPosGround = new Vector3f(carPos.x, 0.048f, carPos.z);

            // camera movement
            processInput(window);
            if (cameraMovementType.equals(FPV)) {
                camera.setCameraPos(new Vector3f(0f,0.3f,0f).add(carPosGround));
                camera.setCameraFront(new Vector3f(nextCarFront.x, 0f , nextCarFront.y));
            } else if (cameraMovementType.equals(FOLLOW)) {
                camera.setCameraPos(new Vector3f(0f,0.5f,0f)
                        .add(new Vector3f(-nextCarFront.x * 3, 0f , -nextCarFront.y * 3))
                        .add(carPosGround));
                camera.setCameraFront(new Vector3f(nextCarFront.x, 0f , nextCarFront.y));
//                camera.processInput(window, deltaTime);
            }

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            var view = camera.getViewMatrix();
            var projection = new Matrix4f()
                    .perspective((float) toRadians(camera.getFov()), 1920.0f / 1080.0f, 0.1f, 100.0f);

            // triangles
            shader.use();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                var model = new Matrix4f()
                        .rotate((float) toRadians(0f), new Vector3f(1f, 0f, 0f));
                shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                shader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                shader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                shader.setVec3("viewPos", camera.getCameraPos());
                spotLights.forEach(light -> {
                    light.setDirection(new Vector3f(nextCarFront.x, 0, nextCarFront.y));
                    light.setPosition(carPosGround);
                    light.applyUniforms(shader);
                });
                pointLights.forEach(light -> light.applyUniforms(shader));
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
                        .translate(carPosGround)
                        .rotate(new Vector2f(nextCarFront).angle(carFront), new Vector3f(0f, 1f, 0f))
                        .scale(0.002f);
                shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                shader.setInt("useTexture", 1);
                car.draw(shader);
            }
            // light
            lightShader.use();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                lightShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                lightShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                pointLights.get(0).draw(lightShader);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        glfwTerminate();
    }

    private static void processInput(long window) {
        if(glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
            cameraMovementType = STATIC;
            camera.setCameraPos(new Vector3f(-13.186f, 30.337f, 1.486f));
            camera.setCameraFront( new Vector3f(0.38f, -0.99f, 0f).normalize());
        } else if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) {
            cameraMovementType = FOLLOW;
        } else if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS) {
            cameraMovementType = FPV;
        }
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}