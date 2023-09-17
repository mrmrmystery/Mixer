/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
