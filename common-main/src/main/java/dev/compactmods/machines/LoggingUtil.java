package dev.compactmods.machines;

import dev.compactmods.machines.api.core.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingUtil {

    public static Logger modLog() {
        return LogManager.getLogger(Constants.MOD_ID);
    }
}
