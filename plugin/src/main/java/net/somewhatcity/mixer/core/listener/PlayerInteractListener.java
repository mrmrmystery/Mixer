/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.core.MixerPlugin;
import net.somewhatcity.mixer.core.audio.IMixerAudioPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInteractListener implements Listener {


    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(!e.getClickedBlock().getType().equals(Material.JUKEBOX)) return;
        Jukebox jukeboxState = (Jukebox) e.getClickedBlock().getState();


        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Location location = e.getClickedBlock().getLocation();
            if(MixerPlugin.getPlugin().playerHashMap().containsKey(location)) {
                IMixerAudioPlayer audioPlayer = MixerPlugin.getPlugin().playerHashMap().get(location);
                audioPlayer.stop();
            }
        } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Location location = e.getClickedBlock().getLocation();
            if(MixerPlugin.getPlugin().playerHashMap().containsKey(location)) {
                IMixerAudioPlayer audioPlayer = MixerPlugin.getPlugin().playerHashMap().get(location);
                if(e.getPlayer().isSneaking()) {
                    /*
                    int boost = e.getPlayer().getInventory().getHeldItemSlot() * 100;
                    if(boost == 0) {
                        oldPlayer.resetFilters();
                        e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<blue>bassboost disabled"));
                        return;
                    }
                    oldPlayer.bassBoost(boost);
                    e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<blue>bassboost set to <b>" + boost + "%</b>"));
                    return;

                     */
                }

                e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<red>playback stopped"));
                audioPlayer.stop();
            }

            if(e.getItem() == null) return;

            NamespacedKey mixerData = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_data");
            if(!e.getItem().getPersistentDataContainer().getKeys().contains(mixerData)) return;
            String url = e.getItem().getPersistentDataContainer().get(mixerData, PersistentDataType.STRING);
            e.setCancelled(true);

            IMixerAudioPlayer audioPlayer = new IMixerAudioPlayer(location);
            audioPlayer.load(url);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockDestroyEvent e) {
        if(e.getBlock().getType().equals(Material.JUKEBOX)) {
            Location loc = e.getBlock().getLocation();
            if(MixerPlugin.getPlugin().playerHashMap().containsKey(loc)) {
                IMixerAudioPlayer audioPlayer = MixerPlugin.getPlugin().playerHashMap().get(loc);
                audioPlayer.stop();
            }
        }
    }
}
