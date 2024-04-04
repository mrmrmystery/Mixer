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
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.core.MixerPlugin;
import net.somewhatcity.mixer.core.audio.IMixerAudioPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

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
                Bukkit.getScheduler().runTask(MixerPlugin.getPlugin(), () -> {
                    jukeboxState.stopPlaying();
                    jukeboxState.update(true);
                });
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
                Bukkit.getScheduler().runTask(MixerPlugin.getPlugin(), () -> {
                    jukeboxState.stopPlaying();
                    jukeboxState.update(true);
                });
            }

            if(e.getItem() == null) return;
            NBTItem nbtItem = new NBTItem(e.getItem());
            if(!nbtItem.hasKey("mixer_data")) return;
            String url = nbtItem.getString("mixer_data");
            e.setCancelled(true);

            IMixerAudioPlayer audioPlayer = new IMixerAudioPlayer(location);
            audioPlayer.load(url);

            /*

            NBTTileEntity jukebox = new NBTTileEntity(block.getBlock().getState());
            String data = jukebox.getPersistentDataContainer().getString("mixer_links");

            List<Location> locations = new ArrayList<>();

            if(data == null || data.isEmpty()) {
                locations.add(block);
            } else {
                JsonArray links = (JsonArray) JsonParser.parseString(data);

                links.forEach(link -> {
                    JsonObject obj = link.getAsJsonObject();
                    Location location = new Location(
                            Bukkit.getWorld(obj.get("world").getAsString()),
                            obj.get("x").getAsDouble(),
                            obj.get("y").getAsDouble(),
                            obj.get("z").getAsDouble()
                    );
                    locations.add(location);
                });
            }


            String redstones = jukebox.getPersistentDataContainer().getString("mixer_redstones");

            List<RedstonePoint> redstonePoints = new ArrayList<>();

            if(redstones != null && !redstones.isEmpty()) {
                JsonArray rePoints = (JsonArray) JsonParser.parseString(redstones);
                rePoints.forEach(point -> {
                    JsonObject obj = point.getAsJsonObject();
                    Location location = new Location(
                            Bukkit.getWorld(obj.get("world").getAsString()),
                            obj.get("x").getAsDouble(),
                            obj.get("y").getAsDouble(),
                            obj.get("z").getAsDouble()
                    );
                    int mag = obj.get("mag").getAsInt();
                    int trigger = obj.get("trigger").getAsInt();
                    int delay = obj.get("delay").getAsInt();

                    redstonePoints.add(new RedstonePoint(location, mag, trigger, delay));
                });
            }

            String objectiveName = null;
            if(e.getClickedBlock().getRelative(BlockFace.UP).getState() instanceof Sign sign) {
                String line0 = MiniMessage.miniMessage().serialize(sign.getSide(Side.FRONT).line(0));
                String line1 = MiniMessage.miniMessage().serialize(sign.getSide(Side.FRONT).line(1));

                if(line0.equals("[scoreboard]") && !line1.isEmpty()) {
                    objectiveName = line1;
                }
            }

            MAudioPlayer player = new MAudioPlayer(locations, Utils.loadNbtData(block, "mixer_dsp"));
            player.load(url);

            /*
            MixerAudioPlayer audioPlayer = new MixerAudioPlayer(locations, redstonePoints, objectiveName);
            audioPlayer.loadAudio(url, true, info -> {
                e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<green>Now playing <white>" + info.title + "<green> by <white>" + info.author + "<green>!"));
                Bukkit.getScheduler().runTask(Mixer.getPlugin(), () -> {
                    jukeboxState.startPlaying();
                    jukeboxState.update(true);
                });
            });


            playerHashMap.put(block, player);

             */


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
