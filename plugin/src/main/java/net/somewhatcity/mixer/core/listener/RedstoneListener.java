/*
 * Copyright (c) 2024 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.api.MixerAudioPlayer;
import net.somewhatcity.mixer.core.MixerPlugin;
import net.somewhatcity.mixer.core.util.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedstoneListener implements Listener {

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        Block block = e.getBlock();
        if(!block.getType().equals(Material.REPEATER)) return;

        Repeater repeater = (Repeater) block.getBlockData();
        if(repeater.isPowered()) return;

        Directional directional = (Directional) block.getBlockData();
        BlockFace facing = directional.getFacing().getOppositeFace();

        if(!block.getRelative(facing).getType().equals(Material.JUKEBOX)) return;
        Block jukebox = block.getRelative(facing);


        if(!jukebox.getRelative(BlockFace.UP).getType().equals(Material.BARREL)) return;
        Barrel barrel = (Barrel) jukebox.getRelative(BlockFace.UP).getState();

        MixerAudioPlayer mixerPlayer = MixerPlugin.getPlugin().api().getMixerAudioPlayer(jukebox.getLocation());
        if(mixerPlayer != null) {
            mixerPlayer.stop();
        }

        mixerPlayer = MixerPlugin.getPlugin().api().createPlayer(jukebox.getLocation());

        /*
        MAudioPlayer oldPlayer = MixerPlugin.playerInteractListener.playerHashMap.get(jukebox.getLocation());
        if(oldPlayer != null) {
            oldPlayer.stop();
        }

        NBTTileEntity jukeboxNbt = new NBTTileEntity(jukebox.getState());
        String data = jukeboxNbt.getPersistentDataContainer().getString("mixer_links");

        List<Location> locations = new ArrayList<>();

        if(data == null || data.isEmpty()) {
            locations.add(jukebox.getLocation());
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


        MAudioPlayer audioPlayer = new MAudioPlayer(locations, Utils.loadNbtData(jukebox.getLocation(), "mixer_dsp"));

         */

        List<String> loadList = new ArrayList<>();

        for(ItemStack item : barrel.getInventory()) {
            if(item == null) continue;
            if(Utils.isDisc(item)) {
                NamespacedKey mixerData = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_data");
                if(!item.getPersistentDataContainer().getKeys().contains(mixerData)) return;
                String url = item.getPersistentDataContainer().get(mixerData, PersistentDataType.STRING);
                loadList.add(url);
            }
            else if(item.getType().equals(Material.WRITABLE_BOOK)) {
                BookMeta bookMeta = (BookMeta) item.getItemMeta();
                StringBuilder sb = new StringBuilder();

                for(Component component : bookMeta.pages()) {
                    sb.append(MiniMessage.miniMessage().serialize(component));
                }

                loadList.add(getTtsUrl(sb.toString()));
            }
        }

        mixerPlayer.load(loadList.toArray(String[]::new));
        //MixerPlugin.playerInteractListener.playerHashMap.put(jukebox.getLocation(), audioPlayer);

    }

    private static final String TTS_URL = "https://api.streamelements.com/kappa/v2/speech?voice=Vicki&text=%s";
    public static String getTtsUrl(String text) {
        text = text.replace(" ", "%20");
        return TTS_URL.formatted(text);
    }
}
