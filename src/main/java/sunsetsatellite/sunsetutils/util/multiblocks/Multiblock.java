package sunsetsatellite.sunsetutils.util.multiblocks;

import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.world.World;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;

public class Multiblock extends Structure{

    public static final HashMap<String,Multiblock> multiblocks = new HashMap<>();

    public Multiblock(String modId, Class<?>[] modClasses, String translateKey, CompoundTag data, boolean includeAir) {
        super(modId, modClasses, translateKey, data, includeAir, false);
        this.translateKey = "multiblock."+modId+"."+translateKey+".name";
    }

    public Multiblock(String modId, Class<?>[] modClasses, String translateKey, String filePath, boolean includeAir) {
        super(modId, modClasses, translateKey, filePath, includeAir, false);
        this.translateKey = "multiblock."+modId+"."+translateKey+".name";
    }

    public ArrayList<BlockInstance> getTileEntities(World world, BlockInstance origin, Direction dir){
        ArrayList<BlockInstance> tiles = new ArrayList<>();
        for (Object o : data.getCompound("Data").getValues()) {
            CompoundTag block = (CompoundTag) o;
            int x = block.getInteger("x");
            int y = block.getInteger("y");
            int z = block.getInteger("z");
            Vec3i pos = new Vec3i(x,y,z).rotate(origin.pos,dir);
            boolean isTile = block.getBoolean("tile");
            if(isTile){
                if(world.getBlockTileEntity(pos.x,pos.y,pos.z) != null){
                    BlockInstance blockInstance = new BlockInstance(Block.blocksList[world.getBlockId(pos.x,pos.y,pos.z)],pos,world.getBlockMetadata(pos.x, pos.y, pos.z),world.getBlockTileEntity(pos.x, pos.y, pos.z));
                    tiles.add(blockInstance);
                }
            } else {
                for (Object sub : data.getCompound("Substitutions").getValues()) {
                    int subX = ((CompoundTag) sub).getInteger("x");
                    int subY = ((CompoundTag) sub).getInteger("y");
                    int subZ = ((CompoundTag) sub).getInteger("z");
                    if(subX == x && subY == y && subZ == z){
                        boolean isSubTile = ((CompoundTag) sub).getBoolean("tile");
                        if(isSubTile){
                            if(world.getBlockTileEntity(pos.x,pos.y,pos.z) != null){
                                BlockInstance blockInstance = new BlockInstance(Block.blocksList[world.getBlockId(pos.x,pos.y,pos.z)],pos,world.getBlockMetadata(pos.x, pos.y, pos.z),world.getBlockTileEntity(pos.x, pos.y, pos.z));
                                tiles.add(blockInstance);
                            }
                        }
                    }
                }
            }
        }
        return tiles;
    }

    public boolean isValidAt(World world, BlockInstance origin, Direction dir){
        for (Object o : data.getCompound("Data").getValues()) {
            CompoundTag block = (CompoundTag) o;
            int id = getBlockId(block);
            int meta = block.getInteger("meta");
            int x = block.getInteger("x");
            int y = block.getInteger("y");
            int z = block.getInteger("z");
            Vec3i pos = new Vec3i(x,y,z).rotate(origin.pos,dir);

            if (world.getBlockId(pos.x, pos.y, pos.z) != id || (world.getBlockId(pos.x, pos.y, pos.z) == id
                    && (world.getBlockMetadata(pos.x, pos.y, pos.z) != meta || meta == -1)
                    && !pos.equals(origin.pos))
            ) {
                boolean foundSub = false;
                for (Object sub : data.getCompound("Substitutions").getValues()) {
                    int subX = ((CompoundTag) sub).getInteger("x");
                    int subY = ((CompoundTag) sub).getInteger("y");
                    int subZ = ((CompoundTag) sub).getInteger("z");
                    int subId = Structure.getBlockId((CompoundTag) sub);
                    int subMeta = ((CompoundTag) sub).getInteger("meta");
                    if(subX == x && subY == y && subZ == z){
                        if(world.getBlockId(pos.x,pos.y,pos.z) == subId && ((world.getBlockMetadata(pos.x,pos.y, pos.z) == subMeta || subMeta == -1))){
                            foundSub = true;
                        }
                    }
                }
                if(!foundSub){
                    SunsetUtils.LOGGER.error(String.format("Multiblock invalid at %d %d %d (should be %d:%d, is %d:%d)",pos.x,pos.y,pos.z,id,meta,world.getBlockId(pos.x,pos.y,pos.z),world.getBlockMetadata(pos.x,pos.y, pos.z)));
                    return false;
                }
            }
        }
        return true;
    }

}
