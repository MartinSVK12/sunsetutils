package sunsetsatellite.sunsetutils;

import net.fabricmc.api.ModInitializer;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sunsetsatellite.sunsetutils.util.models.NBTModel;
import sunsetsatellite.sunsetutils.util.models.RenderCustomTileEntityModel;
import turniplabs.halplibe.helper.BlockHelper;
import turniplabs.halplibe.helper.EntityHelper;


public class SunsetUtils implements ModInitializer {
    public static final String MOD_ID = "sunsetutils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Sunset Utils initialized.");
    }
}
