#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 normal;
out vec3 fragPos;
out vec2 TexCoords;
out vec4 eyeSpacePosition;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
void main()
{
    mat4 mvMatrix = view * model;
    eyeSpacePosition = mvMatrix * vec4(aPos, 1.0f);
    gl_Position = projection * eyeSpacePosition;
    // move to cpu since inversions are costly
    normal = mat3(transpose(inverse(model))) * aNormal;
    // fixes wrongly generated normals on plain ground
    if (normal == vec3(0.0f, -1.0f, 0.0f)) {
        normal = vec3(0, 1, 0);
    }
    fragPos = vec3(model * vec4(aPos, 1.0f));
    TexCoords = aTexCoords;
}