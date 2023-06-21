package sunsetsatellite.sunsetutils.util.multiblocks;

import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Structure {
    public String modId;
    public Class<?>[] modClasses;
    public String translateKey;
    public String filePath;
    public NBTTagCompound data;
    public boolean placeAir;
    public boolean replaceBlocks;

    public static HashMap<String,Structure> internalStructures = new HashMap<>();

    public Structure(String modId, Class<?>[] modClasses, String translateKey, NBTTagCompound data, boolean placeAir, boolean replaceBlocks){
        this.modId = modId;
        this.modClasses = modClasses;
        this.translateKey = "structure."+modId+"."+translateKey+".name";
        this.data = data;
        this.filePath = null;
        this.placeAir = placeAir;
        this.replaceBlocks = replaceBlocks;
    }

    public Structure(String modId, Class<?>[] modClasses, String translateKey, String filePath, boolean placeAir, boolean replaceBlocks){
        this.modId = modId;
        this.modClasses = modClasses;
        this.translateKey = "structure."+modId+"."+translateKey+".name";
        this.placeAir = placeAir;
        this.replaceBlocks = replaceBlocks;
        loadFromNBT(filePath);
    }

    public String getTranslatedName(){
        return StringTranslate.getInstance().translateNamedKey(this.translateKey);
    }

    public String getFullFilePath(){
        if(filePath != null){
            return "/assets/"+modId+"/structures/"+filePath+".nbt";
        } else {
            return null;
        }
    }

    //data.getCompoundTag("Data").func_28110_c()

    public boolean placeStructure(World world, int originX, int originY, int originZ){
        for (Object o : data.getCompoundTag("Data").func_28110_c()) {
            NBTTagCompound block = (NBTTagCompound) o;
            if (!replaceBlocks && world.getBlockId(block.getInteger("x") + originX, block.getInteger("y") + originY, block.getInteger("z") + originZ) != 0) {
                return false;
            }
        }
        for (Object o : data.getCompoundTag("Data").func_28110_c()) {
            NBTTagCompound block = (NBTTagCompound) o;
            int id = getBlockId(block);
            if(id != 0 || placeAir){
                world.setBlockAndMetadataWithNotify(block.getInteger("x")+originX,block.getInteger("y")+originY, block.getInteger("z")+originZ, id, block.getInteger("meta"));
            }
        }
        return true;
    }

    public static Structure saveStructureAroundOrigin(World world, Vec3i origin, Vec3i size, String filePath, boolean placeAir, boolean replaceBlocks){
        if(size.x >= 0 && size.y >= 0 && size.z >= 0){
            int n = 0;
            NBTTagCompound data = new NBTTagCompound();
            NBTTagCompound struct = new NBTTagCompound();
            Structure structure = new Structure(SunsetUtils.MOD_ID,new Class<?>[]{},filePath,struct,placeAir,replaceBlocks);
            structure.filePath = filePath;
            for (int x = origin.x-size.x; x <= origin.x+size.x; x++) {
                for (int y = origin.y-size.y; y <= origin.y+size.y; y++) {
                    for (int z = origin.z-size.z; z <= origin.z+size.z; z++) {
                        if(world.getBlockId(x,y,z) != 0 || placeAir){
                            NBTTagCompound block = new NBTTagCompound();
                            block.setInteger("x",x-origin.x);
                            block.setInteger("y",y-origin.y);
                            block.setInteger("z",z-origin.z);
                            String s = structure.getBlockFieldName(Block.getBlock(world.getBlockId(x,y,z)));
                            if(!s.contains("Block.")){
                                s = s.replace(".",":");
                                block.setString("id",s);
                            } else {
                                block.setInteger("id",world.getBlockId(x,y,z));
                            }
                            block.setInteger("meta",world.getBlockMetadata(x,y,z));
                            data.setCompoundTag(String.valueOf(n),block);
                            n++;
                        }
                    }
                }
            }
            struct.setCompoundTag("Data",data);
            SunsetUtils.LOGGER.info(n+" blocks saved.");
            return structure;
        } else {
            SunsetUtils.LOGGER.error("Invalid parameters!");
            return null;
        }
    }

    public static Structure saveStructure(World world, ArrayList<BlockInstance> blocks, String filePath, boolean placeAir, boolean replaceBlocks){
        NBTTagCompound struct = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        Structure structure = new Structure(SunsetUtils.MOD_ID,new Class<?>[]{},filePath,struct,placeAir,replaceBlocks);
        structure.filePath = filePath;
        int n = 0;
        for (BlockInstance blockInstance : blocks) {
            NBTTagCompound block = new NBTTagCompound();
            block.setInteger("x",blockInstance.pos.x);
            block.setInteger("y",blockInstance.pos.y);
            block.setInteger("z",blockInstance.pos.z);
            String s = structure.getBlockFieldName(blockInstance.block);
            if(s.isEmpty()){
                block.setInteger("id",blockInstance.block.blockID);
            } else if(!s.contains("Block.")){
                s = s.replace(".",":");
                block.setString("id",s);
            } else {
                block.setInteger("id",blockInstance.block.blockID);
            }
            block.setInteger("meta",blockInstance.meta);
            data.setCompoundTag(String.valueOf(n),block);
            n++;
        }
        struct.setCompoundTag("Data",data);
        SunsetUtils.LOGGER.info(n+" blocks saved.");
        return structure;
    }

    public static Structure saveStructure(World world, Vec3i pos1, Vec3i pos2, String filePath, boolean placeAir, boolean replaceBlocks){
        NBTTagCompound struct = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        Structure structure = new Structure(SunsetUtils.MOD_ID,new Class<?>[]{},filePath,struct,placeAir,replaceBlocks);
        structure.filePath = filePath;
        Vec3i diff = new Vec3i(pos1.x-pos2.x,pos1.y-pos2.y,pos1.z-pos2.z);
        int n = 0;
        SunsetUtils.LOGGER.info(diff.toString());
        if(pos1.x < pos2.x){
            int temp = pos1.x;
            pos1.x = pos2.x;
            pos2.x = temp;
        }
        if(pos1.y < pos2.y){
            int temp = pos1.y;
            pos1.y = pos2.y;
            pos2.y = temp;
        }
        if(pos1.z < pos2.z){
            int temp = pos1.z;
            pos1.z = pos2.z;
            pos2.z = temp;
        }
        int i = 0,j = 0,k = 0;
        for (int x = pos2.x; x <= pos1.x; x++) {
            for(int y = pos2.y; y <= pos1.y; y++){
                for(int z = pos2.z; z <= pos1.z; z++){
                    if(world.getBlockId(x,y,z) != 0 || placeAir){
                        i = x - pos1.x;
                        j = y - pos1.y;
                        k = z - pos1.z;
                        NBTTagCompound block = new NBTTagCompound();
                        block.setInteger("x",i);
                        block.setInteger("y",j);
                        block.setInteger("z",k);
                        String s = structure.getBlockFieldName(Block.getBlock(world.getBlockId(x,y,z)));
                        if(s.isEmpty()){
                            block.setInteger("id",world.getBlockId(x,y,z));
                        } else if(!s.contains("Block.")){
                            s = s.replace(".",":");
                            block.setString("id",s);
                        } else {
                            block.setInteger("id",world.getBlockId(x,y,z));
                        }
                        block.setInteger("meta",world.getBlockMetadata(x,y,z));
                        data.setCompoundTag(String.valueOf(n),block);
                        n++;
                    }
                }
            }
        }
        struct.setCompoundTag("Data",data);
        SunsetUtils.LOGGER.info(n+" blocks saved.");
        return structure;
    }

    public boolean placeStructure(World world, int originX, int originY, int originZ, String direction){

        for (Object o : data.getCompoundTag("Data").func_28110_c()) {
            Vec3i pos;
            NBTTagCompound block = (NBTTagCompound) o;
            Direction dir = Direction.getFromName(direction);
            if (dir != null) {
                pos = new Vec3i(block.getInteger("x"),block.getInteger("y"),block.getInteger("z")).rotate(new Vec3i(originX,originY,originZ),dir);
            } else {
                return false;
            }
            
            if (!replaceBlocks && world.getBlockId(pos.x, pos.y, pos.z) != 0) {
                return false;
            }
        }
        for (Object o : data.getCompoundTag("Data").func_28110_c()) {
            Vec3i pos;
            Direction dir = Direction.getFromName(direction);
            NBTTagCompound block = (NBTTagCompound) o;
            if (dir != null) {
                pos = new Vec3i(block.getInteger("x"),block.getInteger("y"),block.getInteger("z")).rotate(new Vec3i(originX,originY,originZ),dir);
            } else {
                return false;
            }
            
            int id = getBlockId(block);
            if(id != 0 || placeAir){
                world.setBlockAndMetadataWithNotify(pos.x, pos.y, pos.z, id, block.getInteger("meta"));
            }
        }
        return true;
    }

    public static int getBlockId(NBTTagCompound block){
        NBTBase nbt = block.getTag("id");
        if(nbt instanceof NBTTagInt){
            return ((NBTTagInt) nbt).intValue;
        } else if (nbt instanceof NBTTagString) {
            String[] args = ((NBTTagString) nbt).stringValue.split(":");
            try {
                Class<?> clazz = Class.forName(args[0]);
                Field field = clazz.getDeclaredField(args[1]);
                Block b = (Block) field.get(null);
                return b.blockID;
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | ClassCastException e) {
               e.printStackTrace();
               return 0;
            }

        }
        return 0;
    }
    
    protected void loadFromNBT(String name) {
        try (InputStream resource = this.getClass().getResourceAsStream("/assets/" + modId + "/structures/" + name + ".nbt")) {
            if (resource != null) {
                this.data = CompressedStreamTools.func_1138_a(resource);
                SunsetUtils.LOGGER.info(String.format("Structure '%s' contains %d blocks.",name,this.data.getCompoundTag("Data").func_28110_c().size()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean saveToNBT(){
        File file;
        String s = String.format("%s\\%s.nbt", Minecraft.getMinecraftDir(), this.filePath);
        file = new File(s);
        try {
            if(file.createNewFile()){
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    CompressedStreamTools.writeGzippedCompoundToOutputStream(this.data,fileOutputStream);
                    SunsetUtils.LOGGER.info(String.format("Structure '%s' saved to %s",this.filePath,s));
                }
            } else {
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    CompressedStreamTools.writeGzippedCompoundToOutputStream(this.data,fileOutputStream);
                    SunsetUtils.LOGGER.info(String.format("Structure '%s' saved to %s",this.filePath,s));
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getBlockFieldName(Block item){
        try{
            ArrayList<Field> fields = new ArrayList<>(Arrays.asList(Block.class.getDeclaredFields()));
            for (Field field : fields) {
                if(field.getType().isAssignableFrom(Block.class) && Modifier.isStatic(field.getModifiers())){
                    field.setAccessible(true);
                    Block fieldItem = (Block) field.get(null);
                    if(fieldItem.equals(item)){
                        return "Block."+field.getName();
                    }
                }
            }
            for (Class<?> aClass : modClasses) {
                fields = new ArrayList<>(Arrays.asList(aClass.getDeclaredFields()));
                for (Field field : fields) {
                    if (field.getType().isAssignableFrom(Block.class) && Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        Block fieldItem = (Block) field.get(null);
                        if (fieldItem.equals(item)) {
                            return aClass.getName()+"." + field.getName();
                        }
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

}
