package org.example.lights;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.Shader;
import org.example.models.Cube;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

@Getter @Setter
@SuperBuilder
public abstract class AbstractLightWithPosition extends AbstractLight {
    private static final Vector3f lightColor = new Vector3f(1, 1, 1);
    protected Vector3f position;
    protected float constant;
    protected float linear;
    protected float quadratic;
    protected int vao;

    @Override
    public void applyUniforms(Shader shader) {
        super.applyUniforms(shader);
        shader.setVec3(name() + ".position", position);
        shader.setFloat(name() + ".constant", constant);
        shader.setFloat(name() + ".linear", linear);
        shader.setFloat(name() + ".quadratic", quadratic);
    }

    public void init() {
        // TODO: change BufferUtils to MemoryUtils and refactor
        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(36 * 8);
        vertex_data.put(Cube.vertices);
        vertex_data.flip();
        int VBO = glGenBuffers();
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    @Override
    public void draw(Shader shader) {
        // TODO: remember to set view and projection before calling this
        // TODO: refactor to model
        shader.setVec3("lightColor", lightColor);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var model = new Matrix4f()
                    .translate(position)
                    .scale(0.2f);
            shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
        glBindVertexArray(0);
    }
}
