package net.somewhatcity.mixer.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.audio.MixerAudioPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerInteractListener implements Listener {

    private HashMap<Location, MixerAudioPlayer> playerHashMap = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(!e.getClickedBlock().getType().equals(Material.JUKEBOX)) return;


        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Location block = e.getClickedBlock().getLocation();
            if(playerHashMap.containsKey(block)) {
                MixerAudioPlayer oldPlayer = playerHashMap.get(block);
                oldPlayer.stop();
            }
        } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Location block = e.getClickedBlock().getLocation();
            if(playerHashMap.containsKey(block)) {
                MixerAudioPlayer oldPlayer = playerHashMap.get(block);
                if(e.getPlayer().isSneaking()) {
                    int boost = e.getPlayer().getInventory().getHeldItemSlot() * 100;
                    if(boost == 0) {
                        oldPlayer.resetFilters();
                        e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<blue>bassboost disabled"));
                        return;
                    }
                    oldPlayer.bassBoost(boost);
                    e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<blue>bassboost set to <b>" + boost + "%</b>"));
                    return;
                }

                e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<red>playback stopped"));
                oldPlayer.stop();
                playerHashMap.remove(block);
            }

            if(e.getItem() == null) return;

            NBTItem nbtItem = new NBTItem(e.getItem());
            if(!nbtItem.hasKey("mixer_data")) return;

            String url = nbtItem.getString("mixer_data");

            e.setCancelled(true);

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

            MixerAudioPlayer audioPlayer = new MixerAudioPlayer(locations);
            audioPlayer.loadAudio(url, true, info -> {
                e.getPlayer().sendActionBar(MiniMessage.miniMessage().deserialize("<green>Now playing <white>" + info.title + "<green> by <white>" + info.author + "<green>!"));
            });
            playerHashMap.put(block, audioPlayer);


        }
    }

    @EventHandler
    public void onBlockBreak(BlockDestroyEvent e) {
        if(e.getBlock().getType().equals(Material.JUKEBOX)) {
            Location loc = e.getBlock().getLocation();
            if(playerHashMap.containsKey(loc)) {
                MixerAudioPlayer map = playerHashMap.get(loc);
                map.stop();
                playerHashMap.remove(loc);
            }
        }
    }
}
