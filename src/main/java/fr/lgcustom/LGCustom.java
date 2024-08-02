package fr.lgcustom;

import fr.ph1lou.werewolfapi.GetWereWolfAPI;
import fr.ph1lou.werewolfapi.annotations.Author;
import fr.ph1lou.werewolfapi.annotations.ModuleWerewolf;
import fr.ph1lou.werewolfapi.enums.UniversalMaterial;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

@Getter
@ModuleWerewolf(key = LGCustom.KEY,
        loreKeys = {},
        item = UniversalMaterial.GOLDEN_APPLE,
        defaultLanguage = "fr",
        authors = @Author(uuid = "056be797-2a0b-4807-9af5-37faf5384396", name = "Ph1Lou"))
public final class LGCustom extends JavaPlugin {
    public static final String KEY = "nicknqck";
    private WereWolfAPI api;
    private GetWereWolfAPI ww;
    @Getter
    private static LGCustom instance;
    public static Random RANDOM = new Random();
    @Override
    public void onEnable() {
        instance = this;
        GetWereWolfAPI ww = getServer().getServicesManager().load(GetWereWolfAPI.class);
        if (ww != null) {
            api = ww.getWereWolfAPI();
            this.ww = ww;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
