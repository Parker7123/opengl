package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

@Getter
@Setter
@SuperBuilder
public class PointLight extends AbstractLightWithPosition {

    @Override
    public void draw(Shader shader) {
        // TODO: remember to set view and projection before calling this
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var model = new Matrix4f()
                    .translate(modelPosition)
                    .scale(0.5f);
            shader.setMatrix4fv("model", model.get(stack.mallocFloat(16)));
            this.model.draw(shader);
        }
    }

    @Override
    public String name() {
        return "pointLights[" + index + "]";
    }
}
