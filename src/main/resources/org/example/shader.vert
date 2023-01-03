#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTextCord;

out vec4 vertexColor;
out vec2 textCord;

uniform mat4 transform;
void main()
{
    gl_Position = transform * vec4(aPos, 1.0f);
    textCord = aTextCord;
}