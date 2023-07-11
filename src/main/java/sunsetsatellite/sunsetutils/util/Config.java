package sunsetsatellite.sunsetutils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import sunsetsatellite.sunsetutils.SunsetUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public String modId;
    public Class<?>[] idClasses = null;
    public Map<String,String> props = new HashMap<>();
    private final File configFile;
    public Config(String modId, Map<String,String> props, Class<?>[] idClasses){
        this.configFile = new File((Minecraft.getMinecraftDir()) + "/config/" + modId + ".cfg");
        this.modId = modId;
        this.idClasses = idClasses;
        this.props = props;
        if (!configFile.exists()) {
            writeConfig();
        } else {
            int currentIds = 0;
            for (Class<?> idClass : idClasses) {
                for (Field field : idClass.getDeclaredFields()) {
                    if (Item.class.isAssignableFrom(field.getType()) || Block.class.isAssignableFrom(field.getType())) {
                        currentIds++;
                    }
                }
            }
            int recordedIds = getFromConfig("TOTAL_IDS",0);
            int currentProps = props.size();
            int recordedProps = getFromConfig("TOTAL_PROPS",0);
            if((currentIds != recordedIds) || (currentProps != recordedProps)){
                try {
                    if(configFile.delete() && configFile.createNewFile()){
                        writeConfig();
                    } else {
                        SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    }
                } catch (IOException e) {
                    SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    e.printStackTrace();
                }
            }
        }
    }

    public Config(String modId, Map<String,String> props){
        this.configFile = new File((Minecraft.getMinecraftDir()) + "/config/" + modId + ".cfg");
        this.modId = modId;
        this.props = props;
        if (!configFile.exists()) {
            writeConfig();
        } else {
            int currentProps = props.size();
            int recordedProps = getFromConfig("TOTAL_PROPS",0);
            if(currentProps != recordedProps){
                try {
                    if(configFile.delete() && configFile.createNewFile()){
                        writeConfig();
                    } else {
                        SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    }
                } catch (IOException e) {
                    SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    e.printStackTrace();
                }
            }
        }
    }

    public Config(String modId, Class<?>[] idClasses) {
        this.configFile = new File((Minecraft.getMinecraftDir()) + "/config/" + modId + ".cfg");
        this.modId = modId;
        this.idClasses = idClasses;
        if (!configFile.exists()) {
            writeConfig();
        } else {
            int currentIds = 0;
            for (Class<?> idClass : idClasses) {
                for (Field field : idClass.getDeclaredFields()) {
                    if (Item.class.isAssignableFrom(field.getType()) || Block.class.isAssignableFrom(field.getType())) {
                        currentIds++;
                    }
                }
            }
            int recordedIds = getFromConfig("TOTAL_IDS",0);
            if(currentIds != recordedIds){
                try {
                    if(configFile.delete() && configFile.createNewFile()){
                        writeConfig();
                    } else {
                        SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    }
                } catch (IOException e) {
                    SunsetUtils.LOGGER.error("Failed to update config for "+modId+"!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeConfig(){
        try {
            if(!configFile.mkdirs()){
                SunsetUtils.LOGGER.error("Failed to create config for "+modId+"!");
            }
            BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile));
            configWriter.write("//"+modId+" configuration file. If a property is null or invalid a default value will be used. Configure options here:");
            for (Map.Entry<String, String> entry : props.entrySet()) {
                String K = entry.getKey();
                String V = entry.getValue();
                configWriter.write(System.getProperty("line.separator")+K + "=" + V);
            }
            configWriter.write(System.getProperty("line.separator")+"// Total amount of recorded properties. Changing this will remake the config file.");
            configWriter.write(System.getProperty("line.separator")+"TOTAL_PROPS="+props.size());
            if(idClasses != null){
                int i = 0;
                configWriter.write(System.getProperty("line.separator")+"// Configure Item/Block ids here:");
                for (Class<?> idClass : idClasses) {
                    for (Field field : idClass.getDeclaredFields()) {
                        if(Item.class.isAssignableFrom(field.getType()) || Block.class.isAssignableFrom(field.getType())){
                            configWriter.write(System.getProperty("line.separator")+field.getName()+ "=null");
                            i++;
                        }
                    }
                }
                configWriter.write(System.getProperty("line.separator")+"// Total amount of recorded ids. Changing this will remake the config file.");
                configWriter.write(System.getProperty("line.separator")+"TOTAL_IDS="+i);
            }
            configWriter.close();
        } catch (Exception e){
            SunsetUtils.LOGGER.error("Failed to create config for "+modId+"!");
            e.printStackTrace();
        }
    }

    public Integer getFromConfig(String key, Integer base){
        try {
            SunsetUtils.LOGGER.info("("+modId+") "+"Getting value for: "+key+" (base: "+base+")");
            BufferedReader configReader = new BufferedReader(new FileReader(configFile));
            for (String s : Files.readAllLines(configFile.toPath())) {
                if (s.contains("=") && !s.startsWith("//")) {
                    String[] as = s.split("=");
                    String name = as[0];
                    int id;
                    try {
                        id = Integer.parseInt(as[1]);
                    } catch (NumberFormatException e){
                        continue;
                    }
                    if (id > 16384){
                        id -= 16384 * (id % 16384);
                    }
                    if (name.equalsIgnoreCase(key)){
                        SunsetUtils.LOGGER.info("("+modId+") "+"Value: "+id);
                        return id;
                    }
                }
            }
            configReader.close();
        } catch (Exception e) {
            SunsetUtils.LOGGER.error("Failed to read "+key+" from config for "+modId+"!");
            e.printStackTrace();
        }
        SunsetUtils.LOGGER.info("("+modId+") "+"No value defined, returning base: "+base);
        return base;
    }
}
