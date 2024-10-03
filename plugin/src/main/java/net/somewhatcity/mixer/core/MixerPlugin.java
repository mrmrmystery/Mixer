/*
 * Copyright (c) 2024 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.somewhatcity.mixer.api.MixerApi;
import net.somewhatcity.mixer.core.api.ImplMixerApi;
import net.somewhatcity.mixer.core.audio.IMixerAudioPlayer;
import net.somewhatcity.mixer.core.commands.MixerCommand;
import net.somewhatcity.mixer.core.listener.PlayerInteractListener;
import net.somewhatcity.mixer.core.listener.RedstoneListener;
import net.somewhatcity.mixer.core.util.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class MixerPlugin extends JavaPlugin {
    private static MixerPlugin plugin;
    private ImplMixerApi api;
    private static final String PLUGIN_ID = "mixer";
    private HashMap<Location, IMixerAudioPlayer> playerHashMap = new HashMap<>();
    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    public static PlayerInteractListener playerInteractListener;

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


        playerInteractListener = new PlayerInteractListener();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerInteractListener, this);
        pm.registerEvents(new RedstoneListener(), this);

        new MixerCommand();

        this.api = new ImplMixerApi(this);
        Bukkit.getServicesManager().register(MixerApi.class, api, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }
    public HashMap<Location, IMixerAudioPlayer> playerHashMap() {
        return playerHashMap;
    }
    public MixerApi api() {
        return api;
    }

    public static MixerPlugin getPlugin() {
        return plugin;
    }

    public static String getPluginId() {
        return PLUGIN_ID;
    }
}
