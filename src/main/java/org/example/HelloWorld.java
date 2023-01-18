package org.example;

import org.example.lights.DirectionalLight;
import org.example.lights.PointLight;
import org.example.lights.SpotLight;
import org.example.models.CameraMovementType;
import org.example.models.Model;
import org.example.models.ShaderType;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static org.example.models.CameraMovementType.*;
import static org.example.models.ShaderType.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("DataFlowIssue")
public class HelloWorld {
    static Camera camera = new Camera();
    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame
    static float spotLightRotation = -5f;
    static Vector3f worldUp = new Vector3f(0, 1, 0);

    private static final String phongVertexShaderSource = HelloWorld.class.getResource("shaders/phong_shader.vert").getFile();

    private static final String phongFragmentShaderSource = HelloWorld.class.getResource("shaders/phong_shader.frag").getFile();
    private static final String gouraudVertexShaderSource = HelloWorld.class.getResource("shaders/gouraud_shader.vert").getFile();

    private static final String gouraudFragmentShaderSource = HelloWorld.class.getResource("shaders/gouraud_shader.frag").getFile();
    private static final String flatVertexShaderSource = HelloWorld.class.getResource("shaders/flat_shader.vert").getFile();

    private static final String flatFragmentShaderSource = HelloWorld.class.getResource("shaders/flat_shader.frag").getFile();

    private static final List<PointLight> pointLights = new ArrayList<>();
    private static final List<SpotLight> spotLights = new ArrayList<>();
    private static final List<DirectionalLight> directionalLights = new ArrayList<>();
    private static CameraMovementType cameraMovementType = FOLLOW;
    private static ShaderType shaderType = PHONG;

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

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        // initial shader and light configuration
        Shader phongShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        Shader gouraudShader = new Shader(gouraudVertexShaderSource, gouraudFragmentShaderSource);
        Shader flatShader = new Shader(flatVertexShaderSource, flatFragmentShaderSource);
        setupLights();

        Model luigi = new Model(HelloWorld.class.getResource("luigi.obj").getPath());
        Model track = new Model(HelloWorld.class.getResource("crircuito.obj").getPath());
        Model car = new Model(HelloWorld.class.getResource("RacingCar.obj").getPath());

        List<Vector3f[]> positions = Files.readAllLines(new File(HelloWorld.class.getResource("position.txt").getPath()).toPath()).stream()
                .map(line -> line.split(" "))
                .map(l-> new Vector3f[] {
                        new Vector3f(Float.parseFloat(l[0]), Float.parseFloat(l[1]), Float.parseFloat(l[2])),
                        new Vector3f(Float.parseFloat(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]))
                })
                .toList();
        int frame = 0;
        int framesInSecond = 0;
        float prevFullSecondTime = 0;
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        var carFront = new Vector2f(0f, -1f).normalize();

        while (!glfwWindowShouldClose(window)) {
            // frames
            frame++;
            framesInSecond ++;
            float currTime = (float) glfwGetTime();
            deltaTime = currTime - lastTime;
            lastTime = currTime;
            if (currTime - prevFullSecondTime >= 1f) {
                glfwSetWindowTitle(window, "FPS: " + framesInSecond);
                framesInSecond = 0;
                prevFullSecondTime = currTime;
            }

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
            } else if (cameraMovementType == FREE) {
                camera.processInput(window, deltaTime);
                camera.startProcessingMouseMovement(window);
            }

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            final var view = camera.getViewMatrix();
            final var projection = new Matrix4f()
                    .perspective((float) toRadians(camera.getFov()), 1920.0f / 1080.0f, 0.1f, 100.0f);

            // choosing a shader
            Shader currentShader = switch (shaderType) {
                case GOURAUD -> gouraudShader;
                case FLAT -> flatShader;
                default -> phongShader;
            };
            currentShader.use();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                // spotlights
                var spotlightRotationAxis = new Vector3f(nextCarFront.x, 0, nextCarFront.y).cross(worldUp)
                        .normalize();
                var spotLightDirection = new Vector3f(nextCarFront.x, 0, nextCarFront.y)
                        .rotateAxis((float) toRadians(spotLightRotation),
                                spotlightRotationAxis.x, spotlightRotationAxis.y, spotlightRotationAxis.z)
                        .normalize();
                var leftLight = spotLights.get(0);
                var rightLight = spotLights.get(1);
                leftLight.setLightPosition(new Vector3f(carPosGround)
                        .add(new Vector3f(worldUp).mul(0.1f))
                        .sub(new Vector3f(spotlightRotationAxis).mul(0.15f))
                        .add(new Vector3f(spotLightDirection).mul(0.1f)));
                leftLight.setDirection(spotLightDirection);
                rightLight.setLightPosition(new Vector3f(carPosGround)
                        .add(new Vector3f(worldUp).mul(0.1f))
                        .add(new Vector3f(spotlightRotationAxis).mul(0.15f))
                        .add(new Vector3f(spotLightDirection).mul(0.1f)));
                rightLight.setDirection(spotLightDirection);

                // light uniforms
                pointLights.forEach(light -> light.applyUniforms(currentShader));
                spotLights.forEach(light -> light.applyUniforms(currentShader));
                directionalLights.forEach(light -> light.applyUniforms(currentShader));

                // track
                var model = new Matrix4f()
                        .rotate((float) toRadians(0f), new Vector3f(1f, 0f, 0f));
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                currentShader.setMatrix4fv("view", view.get(stack.mallocFloat(16)));
                currentShader.setMatrix4fv("projection", projection.get(stack.mallocFloat(16)));
                currentShader.setVec3("viewPos", camera.getCameraPos());
                track.draw(currentShader);

                // luigi
                model = new Matrix4f()
                        .translate(new Vector3f((float) cos(glfwGetTime()) - 3, 0.1f, (float) sin(glfwGetTime()) + 3))
                        .rotate((float) glfwGetTime(), new Vector3f(0f, 1f, 0f))
                        .scale(0.01f);
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                luigi.draw(currentShader);

                // car
                model = new Matrix4f()
                        .translate(carPosGround)
                        .rotate(new Vector2f(nextCarFront).angle(carFront), new Vector3f(0f, 1f, 0f))
                        .scale(0.002f);
                currentShader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
                car.draw(currentShader);

                // lamps
                pointLights.forEach(pointLight -> pointLight.draw(currentShader));
            }
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        glfwTerminate();
    }

    private static void setupLights() {
        Model lamp = new Model(HelloWorld.class.getResource("street_lamp_02.obj").getPath());
        DirectionalLight directionalLight = DirectionalLight.builder()
                .direction(new Vector3f(0, -1f, 0))
                .ambient(new Vector3f(0.1f, 0.1f, 0.1f))
                .diffuse(new Vector3f(0.1f, 0.1f, 0.1f))
                .specular(new Vector3f(0.1f, 0.1f, 0.1f))
                .build();
        directionalLights.add(directionalLight);
        //  PointLights
        Vector3f[] pointLightPositions = {
                new Vector3f(new Vector3f(-2, 0, -5)),
                new Vector3f(new Vector3f(11.3f, 0, -3.8f)),
                new Vector3f(new Vector3f(7f, 0, 7.7f)),
                new Vector3f(new Vector3f(-0.65f, 0, 4.36f)),
                new Vector3f(new Vector3f(-11.1168f, 0, -1.7f)),
        };
        int index = 0;
        for (var position : pointLightPositions) {
            PointLight pointLight = PointLight.builder()
                    .index(index++)
                    .model(lamp)
                    .modelPosition(position)
                    .lightPosition(new Vector3f(0, 1.35f, 0).add(position))
                    .ambient(new Vector3f(0.05f, 0.05f, 0.05f))
                    .diffuse(new Vector3f(0.4f, 0.4f, 0.4f))
                    .specular(new Vector3f(0.1f, 0.1f, 0.1f))
                    .constant(1.0f)
                    .linear(0.025f)
                    .quadratic(0.0075f)
                    .build();
            pointLights.add(pointLight);
        }
        // SpotLights
        IntStream.range(0, 2).forEach(i -> {
            SpotLight spotLight = SpotLight.builder()
                    .index(i)
                    .lightPosition(new Vector3f(-10, 1, -5))
                    .direction(new Vector3f(0, -1f, 0))
                    .ambient(new Vector3f(0.15f, 0.15f, 0.10f))
                    .diffuse(new Vector3f(0.3f, 0.3f, 0.2f))
                    .specular(new Vector3f(0.3f, 0.53f, 0.2f))
                    .constant(1.0f)
                    .linear(0.045f)
                    .quadratic(0.0075f)
                    .cutOff((float) cos(toRadians(13)))
                    .outerCutOff((float) cos(toRadians(20)))
                    .build();
            spotLights.add(spotLight);
        });
    }

    private static void processInput(long window) {
        if(glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
            camera.stopProcessingMouseMovement(window);
            cameraMovementType = STATIC;
            camera.setCameraPos(new Vector3f(-13.186f, 30.337f, 1.486f));
            camera.setCameraFront( new Vector3f(0.38f, -0.99f, 0f).normalize());
        } else if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) {
            camera.stopProcessingMouseMovement(window);
            cameraMovementType = FOLLOW;
        } else if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS) {
            camera.stopProcessingMouseMovement(window);
            cameraMovementType = FPV;
        } else if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS) {
            cameraMovementType = FREE;
        } else if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            spotLightRotation += deltaTime * 10f;
            spotLightRotation = spotLightRotation > 30 ? 30 : spotLightRotation;
        } else if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            spotLightRotation -= deltaTime * 10f;
            spotLightRotation = spotLightRotation < -30 ? -30 : spotLightRotation;
        }  else if (glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
            shaderType = PHONG;
        }  else if (glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS) {
            shaderType = GOURAUD;
        }  else if (glfwGetKey(window, GLFW_KEY_F) == GLFW_PRESS) {
            shaderType = FLAT;
        }
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window, true);
    }
}