package net.somewhatcity.mixer;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.somewhatcity.mixer.commands.MixerCommand;
import net.somewhatcity.mixer.listener.PlayerInteractListener;
import net.somewhatcity.mixer.util.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Mixer extends JavaPlugin {

    private static Mixer plugin;
    private static final String PLUGIN_ID = "mixer";

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        plugin = this;

        new Metrics(this,19824);

        CommandAPI.onEnable();

        BukkitVoicechatService vcService = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if(vcService != null) {
            MixerVoicechatPlugin voicechatPlugin = new MixerVoicechatPlugin();
            vcService.registerPlugin(voicechatPlugin);
        } else {
            getLogger().info("VoiceChat not found");
        }

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractListener(), this);

        new MixerCommand();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    public static Mixer getPlugin() {
        return plugin;
    }

    public static String getPluginId() {
        return PLUGIN_ID;
    }
}
