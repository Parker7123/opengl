package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.Shader;
import org.joml.Vector3f;

@Getter
@Setter
@SuperBuilder
public class SpotLight extends AbstractLightWithPosition {
    protected Vector3f direction;
    protected float cutOff;
    protected float outerCutOff;

    @Override
    public void applyUniforms(Shader shader) {
        super.applyUniforms(shader);
        shader.setVec3(name() + ".direction", direction);
        shader.setFloat(name() + ".cutOff", cutOff);
        shader.setFloat(name() + ".outerCutOff", outerCutOff);
    }

    @Override
    public String name() {
        return "spotLights[" + index + "]";
    }
}
