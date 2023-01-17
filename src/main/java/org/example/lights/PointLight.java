package org.example.lights;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.Shader;
import org.joml.Vector3f;

@Getter
@Setter
@SuperBuilder
public class PointLight extends AbstractLightWithPosition {

    @Override
    public void draw(Shader shader) {
        super.draw(shader);
    }

    @Override
    public String name() {
        return "pointLights[" + index + "]";
    }
}
