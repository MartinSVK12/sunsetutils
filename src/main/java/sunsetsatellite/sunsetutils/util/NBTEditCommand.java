package sunsetsatellite.sunsetutils.util;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.command.*;

import java.util.Objects;

public class NBTEditCommand extends Command {
    public NBTEditCommand() {
        super("nbtedit", "nbt");
    }

    public static NBTTagCompound copy;

    @Override
    public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] args) {
        if(commandSender instanceof PlayerCommandSender){
            if(Objects.equals(args[0], "hand")){
                if(Objects.equals(args[1],"copy")){
                    if(commandSender.getPlayer().inventory.getCurrentItem() != null){
                        copy = commandSender.getPlayer().inventory.getCurrentItem().tag;
                        commandSender.sendMessage("Copied!");
                        return true;
                    }
                }
                if(Objects.equals(args[1],"paste")){
                    if(copy == null){
                        throw new CommandError("Copy some data first!");
                    }
                    if(commandSender.getPlayer().inventory.getCurrentItem() != null){
                        commandSender.getPlayer().inventory.getCurrentItem().tag = copy;
                        commandSender.sendMessage("Pasted!");
                        return true;
                    }
                }
                if(Objects.equals(args[1],"set")){
                    if(args.length > 4){
                        ItemStack stack = commandSender.getPlayer().inventory.getCurrentItem();
                        switch (args[2]){
                            case "integer":
                                stack.tag.setInteger(args[3],Integer.parseInt(args[4]));
                                return true;
                            case "string":
                                stack.tag.setString(args[3],args[4]);
                                return true;
                            case "byte":
                                stack.tag.setByte(args[3],Byte.parseByte(args[4]));
                                return true;
                            case "bool":
                                stack.tag.setBoolean(args[3],Boolean.parseBoolean(args[4]));
                                return true;
                            case "double":
                                stack.tag.setDouble(args[3],Double.parseDouble(args[4]));
                                return true;
                            case "float":
                                stack.tag.setDouble(args[3],Float.parseFloat(args[4]));
                                return true;
                            case "long":
                                stack.tag.setDouble(args[3],Long.parseLong(args[4]));
                                return true;
                            case "short":
                                stack.tag.setDouble(args[3],Short.parseShort(args[4]));
                                return true;
                        }
                    }
                }
                if(Objects.equals(args[1],"get")) {
                    if (args.length > 2) {
                        ItemStack stack = commandSender.getPlayer().inventory.getCurrentItem();
                        switch (args[2]) {
                            case "integer":
                                commandSender.sendMessage(String.valueOf(stack.tag.getInteger(args[3])));
                                return true;
                            case "string":
                                commandSender.sendMessage(stack.tag.getString(args[3]));
                                return true;
                            case "byte":
                                commandSender.sendMessage(String.valueOf(stack.tag.getByte(args[3])));
                                return true;
                            case "bool":
                                commandSender.sendMessage(String.valueOf(stack.tag.getBoolean(args[3])));
                                return true;
                            case "double":
                                commandSender.sendMessage(String.valueOf(stack.tag.getDouble(args[3])));
                                return true;
                            case "float":
                                commandSender.sendMessage(String.valueOf(stack.tag.getFloat(args[3])));
                                return true;
                            case "long":
                                commandSender.sendMessage(String.valueOf(stack.tag.getLong(args[3])));
                                return true;
                            case "short":
                                commandSender.sendMessage(String.valueOf(stack.tag.getShort(args[3])));
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean opRequired(String[] strings) {
        return true;
    }

    @Override
    public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {
        if (commandSender instanceof PlayerCommandSender) {
            commandSender.sendMessage("/nbt hand copy/paste");
            commandSender.sendMessage("/nbt hand set integer/string/byte/bool/double/float/long/short <name> <value>");
        }
    }
}
