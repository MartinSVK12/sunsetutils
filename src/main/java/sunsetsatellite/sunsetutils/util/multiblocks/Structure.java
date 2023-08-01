package sunsetsatellite.sunsetutils.util.multiblocks;

import com.mojang.nbt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.World;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Structure {
    public String modId;
    public Class<?>[] modClasses;
    public String translateKey;
    public String filePath;
    public CompoundTag data;
    public boolean placeAir;
    public boolean replaceBlocks;

    public static HashMap<String,Structure> internalStructures = new HashMap<>();

    public Structure(String modId, Class<?>[] modClasses, String translateKey, CompoundTag data, boolean placeAir, boolean replaceBlocks){
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
        return I18n.getInstance().translateNameKey(this.translateKey);
    }

    public String getFullFilePath(){
        if(filePath != null){
            return "/assets/"+modId+"/structures/"+filePath+".nbt";
        } else {
            return null;
        }
    }

    //data.getCompound("Data").getValues()

    public boolean placeStructure(World world, int originX, int originY, int originZ){
        for (Object o : data.getCompound("Data").getValues()) {
            CompoundTag block = (CompoundTag) o;
            if (!replaceBlocks && world.getBlockId(block.getInteger("x") + originX, block.getInteger("y") + originY, block.getInteger("z") + originZ) != 0) {
                return false;
            }
        }
        for (Object o : data.getCompound("Data").getValues()) {
            CompoundTag block = (CompoundTag) o;
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
            CompoundTag data = new CompoundTag();
            CompoundTag struct = new CompoundTag();
            Structure structure = new Structure(SunsetUtils.MOD_ID,new Class<?>[]{},filePath,struct,placeAir,replaceBlocks);
            structure.filePath = filePath;
            for (int x = origin.x-size.x; x <= origin.x+size.x; x++) {
                for (int y = origin.y-size.y; y <= origin.y+size.y; y++) {
                    for (int z = origin.z-size.z; z <= origin.z+size.z; z++) {
                        if(world.getBlockId(x,y,z) != 0 || placeAir){
                            CompoundTag block = new CompoundTag();
                            block.putInt("x",x-origin.x);
                            block.putInt("y",y-origin.y);
                            block.putInt("z",z-origin.z);
                            String s = structure.getBlockFieldName(Block.getBlock(world.getBlockId(x,y,z)));
                            if(!s.contains("Block.")){
                                s = s.replace(".",":");
                                block.putString("id",s);
                            } else {
                                block.putInt("id",world.getBlockId(x,y,z));
                            }
                            block.putInt("meta",world.getBlockMetadata(x,y,z));
                            data.putCompound(String.valueOf(n),block);
                            n++;
                        }
                    }
                }
            }
            struct.putCompound("Data",data);
            SunsetUtils.LOGGER.info(n+" blocks saved.");
            return structure;
        } else {
            SunsetUtils.LOGGER.error("Invalid parameters!");
            return null;
        }
    }

    public static Structure saveStructure(World world, ArrayList<BlockInstance> blocks, String filePath, boolean placeAir, boolean replaceBlocks){
        CompoundTag struct = new CompoundTag();
        CompoundTag data = new CompoundTag();
        Structure structure = new Structure(SunsetUtils.MOD_ID,new Class<?>[]{},filePath,struct,placeAir,replaceBlocks);
        structure.filePath = filePath;
        int n = 0;
        for (BlockInstance blockInstance : blocks) {
            CompoundTag block = new CompoundTag();
            block.putInt("x",blockInstance.pos.x);
            block.putInt("y",blockInstance.pos.y);
            block.putInt("z",blockInstance.pos.z);
            String s = structure.getBlockFieldName(blockInstance.block);
            if(s.isEmpty()){
                block.putInt("id",blockInstance.block.id);
            } else if(!s.contains("Block.")){
                s = s.replace(".",":");
                block.putString("id",s);
            } else {
                block.putInt("id",blockInstance.block.id);
            }
            block.putInt("meta",blockInstance.meta);
            data.putCompound(String.valueOf(n),block);
            n++;
        }
        struct.putCompound("Data",data);
        SunsetUtils.LOGGER.info(n+" blocks saved.");
        return structure;
    }

    public static Structure saveStructure(World world, Vec3i pos1, Vec3i pos2, String filePath, boolean placeAir, boolean replaceBlocks){
        CompoundTag struct = new CompoundTag();
        CompoundTag data = new CompoundTag();
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
                        CompoundTag block = new CompoundTag();
                        block.putInt("x",i);
                        block.putInt("y",j);
                        block.putInt("z",k);
                        String s = structure.getBlockFieldName(Block.getBlock(world.getBlockId(x,y,z)));
                        if(s.isEmpty()){
                            block.putInt("id",world.getBlockId(x,y,z));
                        } else if(!s.contains("Block.")){
                            s = s.replace(".",":");
                            block.putString("id",s);
                        } else {
                            block.putInt("id",world.getBlockId(x,y,z));
                        }
                        block.putInt("meta",world.getBlockMetadata(x,y,z));
                        data.putCompound(String.valueOf(n),block);
                        n++;
                    }
                }
            }
        }
        struct.putCompound("Data",data);
        SunsetUtils.LOGGER.info(n+" blocks saved.");
        return structure;
    }

    public boolean placeStructure(World world, int originX, int originY, int originZ, String direction){

        for (Object o : data.getCompound("Data").getValues()) {
            Vec3i pos;
            CompoundTag block = (CompoundTag) o;
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
        for (Object o : data.getCompound("Data").getValues()) {
            Vec3i pos;
            Direction dir = Direction.getFromName(direction);
            CompoundTag block = (CompoundTag) o;
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

    public static int getBlockId(CompoundTag block){
        Tag<?> nbt = block.getTag("id");
        if(nbt instanceof IntTag){
            return ((IntTag) nbt).getValue();
        } else if (nbt instanceof StringTag) {
            String[] args = ((StringTag) nbt).getValue().split(":");
            try {
                Class<?> clazz = Class.forName(args[0]);
                Field field = clazz.getDeclaredField(args[1]);
                Block b = (Block) field.get(null);
                return b.id;
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
                this.data = NbtIo.readCompressed(resource);
                SunsetUtils.LOGGER.info(String.format("Structure '%s' contains %d blocks.",name,this.data.getCompound("Data").getValues().size()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean saveToNBT(){
        File file;
        String s = String.format("%s\\%s.nbt", Minecraft.getMinecraft(Minecraft.class).getMinecraftDir(), this.filePath);
        file = new File(s);
        try {
            if(file.createNewFile()){
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    NbtIo.writeCompressed(this.data,fileOutputStream);
                    SunsetUtils.LOGGER.info(String.format("Structure '%s' saved to %s",this.filePath,s));
                }
            } else {
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    NbtIo.writeCompressed(this.data,fileOutputStream);
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
