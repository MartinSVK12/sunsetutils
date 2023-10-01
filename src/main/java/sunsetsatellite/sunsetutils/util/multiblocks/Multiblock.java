package sunsetsatellite.sunsetutils.util.multiblocks;

import com.mojang.nbt.CompoundTag;
import net.minecraft.core.world.World;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Direction;

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

    public boolean isValidAt(World world, BlockInstance origin, Direction dir){
        ArrayList<BlockInstance> blocks = getBlocks(origin.pos,dir);
        ArrayList<BlockInstance> substitutions = getSubstitutions(origin.pos,dir);
        for (BlockInstance block : blocks) {
            if (!block.exists(world)) {
                boolean foundSub = substitutions.stream().anyMatch((BI) -> BI.pos.equals(block.pos) && BI.exists(world));
                if (!foundSub) {
                    return false;
                }
            }
        }
        return true;
    }

}
