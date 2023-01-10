#version 330 core
out vec4 fragColor;
in vec3 fragPos;
in vec3 normal;
in vec2 TexCoords;

struct Material {
    sampler2D texture_diffuse1;
    sampler2D specular;
    vec3 diffuseColor;
};

struct Light {
    vec3  position;
    vec3  direction;
    float cutOff;
    float outerCutOff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

uniform Material material;
uniform Light light;
uniform vec3 viewPos;
uniform int useTexture;

void main()
{
    vec3 diffuseColor;
    if(useTexture > 0) {
        diffuseColor= vec3(texture(material.texture_diffuse1, TexCoords));
    } else {
        diffuseColor = material.diffuseColor;
    }
    float distance    = length(light.position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));

    // ambient
    vec3 ambient = light.ambient * diffuseColor;

    // diffuse
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normalize(normal), lightDir), 0.0);
    vec3 diffuse = diff * light.diffuse * diffuseColor;

    // specular
    float specularStrength = 0.2;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, normalize(normal));
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32) * specularStrength;
    vec3 specular = light.specular * spec * diffuseColor;

    // spotlight
    float theta     = dot(lightDir, normalize(-light.direction));
    float epsilon   = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    diffuse  *= intensity;
    specular *= intensity;

    // result color
    ambient  *= attenuation;
    diffuse  *= attenuation;
    specular *= attenuation;

    vec3 result = (ambient + diffuse + specular);
    fragColor = vec4(result, 1.0f);
}