package sunsetsatellite.sunsetutils.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.Item;
import sunsetsatellite.sunsetutils.SunsetUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public String modId;
    public Class<?>[] idClasses = null;
    public Map<String,String> props = new HashMap<>();
    private final File configFile;
    public Config(String modId, Map<String,String> props, Class<?>[] idClasses){
        Path mcDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = new File(mcDir + "/" + modId + ".cfg");
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
        Path mcDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = new File(mcDir + "/" + modId + ".cfg");
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
        Path mcDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = new File(mcDir + "/" + modId + ".cfg");
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
            File mcDir = FabricLoader.getInstance().getConfigDir().toFile();
            mcDir.mkdirs();
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

    public int getFromConfig(String key, int base){
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

    public double getFromConfig(String key, double base){
        try {
            SunsetUtils.LOGGER.info("("+modId+") "+"Getting value for: "+key+" (base: "+base+")");
            BufferedReader configReader = new BufferedReader(new FileReader(configFile));
            for (String s : Files.readAllLines(configFile.toPath())) {
                if (s.contains("=") && !s.startsWith("//")) {
                    String[] as = s.split("=");
                    String name = as[0];
                    double id;
                    try {
                        id = Double.parseDouble(as[1]);
                    } catch (NumberFormatException e){
                        continue;
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

    public String getFromConfig(String key, String base){
        try {
            SunsetUtils.LOGGER.info("("+modId+") "+"Getting value for: "+key+" (base: "+base+")");
            BufferedReader configReader = new BufferedReader(new FileReader(configFile));
            for (String s : Files.readAllLines(configFile.toPath())) {
                if (s.contains("=") && !s.startsWith("//")) {
                    String[] as = s.split("=");
                    String name = as[0];
                    String id = as[1];
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
