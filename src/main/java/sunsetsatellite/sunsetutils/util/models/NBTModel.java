package sunsetsatellite.sunsetutils.util.models;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagInt;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.Vec2f;
import sunsetsatellite.sunsetutils.util.Vec3f;
import sunsetsatellite.sunsetutils.util.Vec4f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipException;

public class NBTModel {
    public String modId;
    public String filePath;
    public NBTTagCompound data;
    public ModelSurface[] surfaces;
    public String[] textures;
    public boolean rotatable;

    public static HashMap<String,NBTModel> models = new HashMap<>();

    public NBTModel(String modId, String filePath, String[] textures, boolean rotatable) {
        this.modId = modId;
        this.filePath = filePath;
        this.textures = textures;
        this.rotatable = rotatable;
        loadFromNBT(filePath);
    }

    protected void loadFromNBT(String filePath) {
        try (InputStream resource = this.getClass().getResourceAsStream("/assets/" + modId + "/models/" + filePath + ".nbt")) {
            if (resource != null) {
                this.data = CompressedStreamTools.func_1138_a(resource);
                ArrayList<ModelSurface> surfaces = new ArrayList<>();
                for (Object o : data.func_28110_c()) {
                    NBTTagCompound surfaceTag = (NBTTagCompound) o;
                    int vertexAmount = surfaceTag.getCompoundTag("Vertices").func_28110_c().size();
                    int indexAmount = surfaceTag.getCompoundTag("Indices").func_28110_c().size();
                    NBTTagCompound translationTag = surfaceTag.getCompoundTag("Translation");
                    NBTTagCompound rotationTag = surfaceTag.getCompoundTag("Rotation");
                    NBTTagCompound scaleTag = surfaceTag.getCompoundTag("Scale");
                    Vec3f[] vertices = new Vec3f[vertexAmount];
                    Vec2f[] uvs = new Vec2f[vertexAmount];
                    Vec3f[] normals = new Vec3f[vertexAmount];
                    Vec3f translation = new Vec3f(translationTag);
                    Vec4f rotation = new Vec4f(rotationTag);
                    Vec3f scale = new Vec3f(scaleTag);
                    int[] indices = new int[indexAmount];
                    for (Object o2 : surfaceTag.getCompoundTag("Indices").func_28110_c()) {
                        NBTTagInt index = (NBTTagInt) o2;
                        indices[Integer.parseInt(index.getKey())] = index.intValue;
                    }
                    for (int i = 0; i < vertexAmount; i++) {
                        NBTTagCompound vertex = surfaceTag.getCompoundTag("Vertices").getCompoundTag(String.valueOf(i));
                        NBTTagCompound pos = vertex.getCompoundTag("XYZ");
                        NBTTagCompound uv = vertex.getCompoundTag("UV");
                        NBTTagCompound norm = vertex.getCompoundTag("Normal");
                        Vec3f vertexPos = new Vec3f(pos.getDouble("x"),pos.getDouble("y"),pos.getDouble("z"));
                        Vec2f vertexUV = new Vec2f(uv.getDouble("x"),uv.getDouble("y"));
                        Vec3f vertexNorm = new Vec3f(norm.getDouble("x"),norm.getDouble("y"),norm.getDouble("z"));
                        vertices[i] = vertexPos;
                        uvs[i] = vertexUV;
                        normals[i] = vertexNorm;
                    }
                    ModelSurface surface = new ModelSurface(vertices,uvs,indices,normals,surfaceTag.getInteger("Texture"),translation,rotation,scale);
                    surfaces.add(surface);
                }
                this.surfaces = surfaces.toArray(new ModelSurface[0]);
                SunsetUtils.LOGGER.info(String.format("Model '%s' loaded.",filePath));
                SunsetUtils.LOGGER.info(String.format("Model '%s' contains %s surfaces.",filePath,surfaces.size()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
