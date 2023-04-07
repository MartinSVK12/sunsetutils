package sunsetsatellite.sunsetutils;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SunsetUtils implements ModInitializer {
    public static final String MOD_ID = "sunsetutils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Sunset Utils initialized.");
    }
}
