package org.example.models;

import org.example.Shader;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {

    private List<Mesh> meshes = new ArrayList<>();
    private String directory;
    public Model(String path) {
        loadModel(path);
    }

    private void loadModel(String path) {
        path = new File(path).toPath().toString();
        System.out.println(path);
        AIScene scene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs);
        if (scene == null || (scene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null) {
            System.out.println("error loading model " + path);
            System.out.println(Assimp.aiGetErrorString());
            return;
        }
        directory = path.substring(0, path.lastIndexOf('\\'));
        processNode(scene.mRootNode(), scene);
    }

    private void processNode(AINode node, AIScene scene) {
        if (node.mMeshes() != null) {
            for (int i = 0; i<node.mNumMeshes(); i++) {
                int meshIndex = node.mMeshes().get(i);
                AIMesh mesh = AIMesh.createSafe(scene.mMeshes().get(meshIndex));
                if (mesh == null) {
                    System.out.println("null mesh");
                    return;
                }
                meshes.add(processMesh(mesh, scene));
            }
        }
        for (int i = 0; i<node.mNumChildren(); i++) {
            AINode childNode = AINode.createSafe(node.mChildren().get(i));
            if (childNode == null) {
                System.out.println("null child node");
                return;
            }
            processNode(childNode, scene);
        }
    }
    private Mesh processMesh(AIMesh mesh, AIScene scene) {
        List<Vertex> vertices = new ArrayList<>();
        List<Texture> textures = new ArrayList<>();

        for (int i = 0; i < mesh.mNumVertices(); i++) {
            Vector3f vector = new Vector3f(mesh.mVertices().get(i).x(),
                    mesh.mVertices().get(i).y(),
                    mesh.mVertices().get(i).z());
            Vector3f normal = new Vector3f(mesh.mNormals().get(i).x(),
                    mesh.mNormals().get(i).y(),
                    mesh.mNormals().get(i).z());
            Vector2f textureCoords;
            if(mesh.mTextureCoords(0) != null) // does the mesh contain texture coordinates?
            {
                textureCoords = new Vector2f(
                        mesh.mTextureCoords(0).get(i).x(),
                        mesh.mTextureCoords(0).get(i).y()
                );
            }
            else {
                textureCoords = new Vector2f(0, 0);
            }
            vertices.add(new Vertex(vector, normal, textureCoords));
        }
        List<Integer> indices = new ArrayList<>();
        mesh.mFaces().stream().forEach(aiFace -> {
            for (int i = 0;i < aiFace.mNumIndices(); i++) {
                indices.add(aiFace.mIndices().get(i));
            }
        });

        if (mesh.mMaterialIndex() > 0) {
            long materialIndex = scene.mMaterials().get(mesh.mMaterialIndex());
            AIMaterial material = AIMaterial.createSafe(materialIndex);
            if (material == null) {
                System.out.println("material index 0, textures not loaded");
                return new Mesh(vertices, indices, textures, Material.DEFAULT_MATERIAL);
            }
            List<Texture> diffuseMaps = loadMaterialTextures(material, Assimp.aiTextureType_DIFFUSE, "texture_diffuse");
            return new Mesh(vertices, indices, textures, loadMaterial(material));
        }
        return new Mesh(vertices, indices, textures, Material.DEFAULT_MATERIAL);
    }

    private Material loadMaterial(AIMaterial material) {
        AIColor4D colour = AIColor4D.create();
        Vector3f diffuse = new Vector3f();
        var result = Assimp.aiGetMaterialColor(material, Assimp.AI_MATKEY_COLOR_DIFFUSE, Assimp.aiTextureType_NONE, 0, colour);
        if (result == 0) {
            return new Material(new Vector3f(colour.r(), colour.g(), colour.b()));
        }
        return Material.DEFAULT_MATERIAL;
    }

    // TODO: migrate typeName to enum
    List<Texture> loadMaterialTextures(AIMaterial material, int type, String typeName) {
        List<Texture> textures = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString aiString = AIString.create();
            Assimp.aiGetMaterialTexture(material, type, i, aiString, (IntBuffer) null, null, null,
                    null, null, null);
            int textureId = loadTextureFromFile(aiString.dataString(), directory);
            Texture texture = new Texture(textureId, typeName);
        }
        return null;
    }

    private int loadTextureFromFile(String s, String directory) {
        System.out.println(s);
        System.out.println(directory);
        return 0;
    }

    public void draw(Shader shader) {
        meshes.forEach(mesh -> mesh.draw(shader));
    }
}
