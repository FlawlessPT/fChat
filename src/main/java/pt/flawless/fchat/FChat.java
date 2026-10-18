package pt.flawless.fchat;

import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;

public class FChat extends JavaPlugin {
    private final FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    @Override
    public void onEnable() {
        saveDefaultConfig();
        consoleLogger.sendMessage("ativado", EMessageTypeEnum.SUCCESS);
    }

    @Override
    public void onDisable() {
        consoleLogger.sendMessage("desativado", EMessageTypeEnum.INFO);
    }
}
