#version 330 core
out vec4 fragColor;
in vec3 normal;
in vec3 fragPos;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct Light {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform sampler2D ourTexture;
uniform Material material;
uniform Light light;
uniform vec3 viewPos;

void main()
{
    // ambient
    vec3 ambient = material.ambient * light.ambient;

    // diffuse
    vec3 lightDir = normalize(light.position - fragPos);
    float diff = max(dot(normalize(normal), lightDir), 0.0);
    vec3 diffuse = diff * light.diffuse * material.diffuse;

    // specular
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, normalize(normal));
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = light.specular * spec * material.specular;

    // result color
    vec3 result = (ambient + diffuse + specular);
    fragColor = vec4(result, 1.0);
}