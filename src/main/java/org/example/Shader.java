package org.example;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL33.*;

public class Shader {
    private final int shaderProgram;

    // constructor reads and builds the shader
    public Shader(String vertexPath, String fragmentPath) throws IOException {
        String vertexShaderSource = Files.readString(new File(vertexPath).toPath());
        String fragmentShaderSource = Files.readString(new File(fragmentPath).toPath());

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        IntBuffer success = BufferUtils.createIntBuffer(1);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        success.rewind();
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        success.rewind();
        glGetShaderiv(shaderProgram, GL_LINK_STATUS, success);
        if(success.get() == 0)
        {
            System.out.println("error");
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

    }
    // use/activate the shader
    public void use(){
        glUseProgram(shaderProgram);
    }
    // utility uniform functions
    void setBool(String name, Boolean value) {
        glUniform1i(glGetUniformLocation(shaderProgram, name), value.compareTo(false));
    }
    void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(shaderProgram, name), value);
    }
    void setFloat(String name, float value) {
        glUniform1f(glGetUniformLocation(shaderProgram, name), value);
    }

    void set4Float(String name, float v1, float v2, float v3, float v4) {
        glUniform4f(glGetUniformLocation(shaderProgram, name), v1, v2, v3, v4);
    }
    void setMatrix4fv(String name, FloatBuffer fb) {
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, name), false, fb);
    }
}
