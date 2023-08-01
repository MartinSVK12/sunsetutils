package sunsetsatellite.sunsetutils.util.multiblocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.net.command.*;
import net.minecraft.core.world.World;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.util.ArrayList;

public class StructureCommand extends Command {
    public StructureCommand(String name, String... alts) {
        super(name, alts);
    }

    public ArrayList<BlockInstance> stack = new ArrayList<>();
    public BlockInstance origin = null;

    @Override
    public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] args) {
        World world = Minecraft.getMinecraft(Minecraft.class).theWorld;
        if(commandSender instanceof PlayerCommandSender){
            EntityPlayer player = commandSender.getPlayer();
            if(args.length > 0){
                switch (args[0]){
                    case "list":
                        commandSender.sendMessage("List of internal structures:");
                        commandSender.sendMessage(Structure.internalStructures.keySet().toString());
                        commandSender.sendMessage("List of multiblocks:");
                        commandSender.sendMessage(Multiblock.multiblocks.keySet().toString());
                        return true;
                    case "stack":
                        if(args.length > 1){
                            switch (args[1]){
                                case "origin": {
                                    Block block = Block.blocksList[world.getBlockId(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z)];
                                    Vec3i pos = new Vec3i(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z);
                                    Vec3i offset = new Vec3i(0,0,0);
                                    int meta = world.getBlockMetadata(pos.x,pos.y,pos.z);
                                    BlockInstance inst = new BlockInstance(block,pos,meta,null);
                                    origin = inst;
                                    stack.add(new BlockInstance(block,offset,meta,null));
                                    commandSender.sendMessage(String.format("Set origin to %s:%s at %s!",block.getLanguageKey(world.getBlockMetadata(pos.x,pos.y,pos.z)),meta,pos));
                                    return true;
                                }
                                case "remove":
                                    try {
                                        if(origin == null){
                                            throw new CommandError("No origin set!");
                                        }
                                        if(args.length == 3){
                                            stack.remove(Integer.parseInt(args[2]));
                                            commandSender.sendMessage(String.format("Removed block at index %s!",args[2]));
                                        }
                                    } catch (IndexOutOfBoundsException e){
                                        e.printStackTrace();
                                    }
                                    return true;
                                case "add":
                                    if(args.length == 2){
                                        if(origin == null){
                                            throw new CommandError("No origin set!");
                                        }
                                        Block block = Block.blocksList[world.getBlockId(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z)];
                                        Vec3i pos = new Vec3i(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z);
                                        Vec3i offset = pos.copy().subtract(origin.pos);
                                        int meta = world.getBlockMetadata(pos.x,pos.y,pos.z);
                                        BlockInstance inst = new BlockInstance(block,offset,meta,null);
                                        stack.add(inst);
                                        commandSender.sendMessage(String.format("Added %s:%s at %s (offset %s) to stack!",block.getLanguageKey(world.getBlockMetadata(pos.x,pos.y,pos.z)),meta,pos,offset));
                                    }
                                    return true;
                                case "replace":
                                    if(args.length == 3){
                                        try {
                                            if(origin == null){
                                                throw new CommandError("No origin set!");
                                            }
                                            Block block = Block.blocksList[world.getBlockId(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z)];
                                            Vec3i pos = new Vec3i(Minecraft.getMinecraft(Minecraft.class).objectMouseOver.x, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.y, Minecraft.getMinecraft(Minecraft.class).objectMouseOver.z);
                                            Vec3i offset = pos.copy().subtract(origin.pos);
                                            int meta = world.getBlockMetadata(pos.x,pos.y,pos.z);
                                            BlockInstance inst = new BlockInstance(block,offset,meta,null);
                                            stack.set(Integer.parseInt(args[2]), inst);
                                            commandSender.sendMessage(String.format("Set block at index %s to %s:%s at %s (offset %s)!",args[2],block.getLanguageKey(world.getBlockMetadata(pos.x,pos.y,pos.z)),meta,pos,offset));
                                        } catch (IndexOutOfBoundsException e){
                                            e.printStackTrace();
                                        }
                                    }
                                    return true;
                                case "save":
                                    if(args.length == 3){
                                        if(origin == null){
                                            throw new CommandError("No origin set!");
                                        }
                                        commandSender.sendMessage(String.format("Saving structure '%s' at origin %s...",args[2],origin.pos));
                                        Structure structure = Structure.saveStructure(world,stack,args[2],true,true);
                                        if(structure.saveToNBT()){
                                            commandSender.sendMessage("Saved!");
                                        } else {
                                            throw new CommandError("Couldn't save structure!");
                                        }
                                    }
                                    return true;
                                case "clear":
                                    stack.clear();
                                    origin = null;
                                    commandSender.sendMessage("Stack cleared!");
                                    return true;
                                case "list":
                                    commandSender.sendMessage(String.format("Stack contains %s blocks.",stack.size()));
                                    commandSender.sendMessage("List is in the debug log!");
                                    SunsetUtils.LOGGER.info(stack.toString());
                                    return true;
                            }
                        }
                        break;
                }
                if(args.length == 6) {
                    if(args[0].equals("place")){
                        if (Structure.internalStructures.containsKey(args[1])) {
                            Structure struct = Structure.internalStructures.get(args[1]);
                            try {
                                boolean r = struct.placeStructure(player.world, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
                                if (r) {
                                    commandSender.sendMessage(String.format("Structure '%s' placed at %s facing %s!", args[1], new Vec3i(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])), args[5]));
                                }
                                return r;
                            } catch (NumberFormatException e) {
                                commandSender.sendMessage("Invalid coordinates provided!");
                                return true;
                            }
                        } else if (Multiblock.multiblocks.containsKey(args[1])) {
                            Structure struct = Multiblock.multiblocks.get(args[1]);
                            try {
                                boolean r = struct.placeStructure(player.world, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
                                if (r) {
                                    commandSender.sendMessage(String.format("Structure '%s' placed at %s facing %s!", args[1], new Vec3i(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])), args[5]));
                                }
                                return r;
                            } catch (NumberFormatException e) {
                                commandSender.sendMessage("Invalid coordinates provided!");
                                return true;
                            }
                        } else {
                            commandSender.sendMessage("Invalid structure!");
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean opRequired(String[] args) {
        return true;
    }

    @Override
    public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {
        if (commandSender instanceof PlayerCommandSender) {
            commandSender.sendMessage("/structure place <name> <x> <y> <z> <r>");
            //commandSender.sendMessage("/structure pos1/pos2 (<x> <y> <z>)");
            //commandSender.sendMessage("/structure clearpos");
            commandSender.sendMessage("/structure stack origin");
            commandSender.sendMessage("/structure stack remove <idx>/<x> <y> <z>");
            commandSender.sendMessage("/structure stack add (<x> <y> <z>)");
            commandSender.sendMessage("/structure stack replace <idx> (<x> <y> <z>)");
            commandSender.sendMessage("/structure stack clear");
            commandSender.sendMessage("/structure stack list");
            commandSender.sendMessage("/structure stack save <name>");
            commandSender.sendMessage("/structure list");

            //commandSender.sendMessage("/structure save origin <name> <originX> <originY> <originZ> <sizeX> <sizeY> <sizeZ>");
        }
    }
}
