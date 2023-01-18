package org.example.lights;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.Shader;
import org.example.models.Cube;
import org.example.models.Model;
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
    protected Vector3f lightPosition;
    protected Vector3f modelPosition;
    protected Model model;
    protected float constant;
    protected float linear;
    protected float quadratic;
    protected int vao;

    @Override
    public void applyUniforms(Shader shader) {
        super.applyUniforms(shader);
        shader.setVec3(name() + ".position", lightPosition);
        shader.setFloat(name() + ".constant", constant);
        shader.setFloat(name() + ".linear", linear);
        shader.setFloat(name() + ".quadratic", quadratic);
    }

    public abstract void draw(Shader shader);
}
