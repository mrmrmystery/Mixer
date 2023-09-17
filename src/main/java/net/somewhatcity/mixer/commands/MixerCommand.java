/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.Mixer;
import net.somewhatcity.mixer.audio.MixerAudioPlayer;
import net.somewhatcity.mixer.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class MixerCommand extends CommandAPICommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public MixerCommand() {
        super("mixer");
        withSubcommand(new CommandAPICommand("burn")
                .withPermission("mixer.command.burn")
                .withArguments(new GreedyStringArgument("url"))
                .executesPlayer((player, args) -> {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (!Utils.isDisc(item)) {
                        player.sendMessage("§cYou must be holding a music disc!");
                        return;
                    }

                    String url = (String) args.get(0);

                    NBTItem nbtItem = new NBTItem(item);
                    nbtItem.setString("mixer_data", url);
                    nbtItem.applyNBT(item);

                    MixerAudioPlayer ghostPlayer = new MixerAudioPlayer(Collections.singletonList(player.getLocation()));
                    ghostPlayer.loadAudio(url, false, info -> {
                        Bukkit.getScheduler().runTask(Mixer.getPlugin(), () -> {
                            ItemMeta meta = item.getItemMeta();
                            meta.displayName(MM.deserialize("<reset>%s".formatted(info.title)));
                            meta.lore(Arrays.asList(
                                    MM.deserialize("<reset>%s".formatted(info.author))
                            ));
                            item.setItemMeta(meta);
                        });
                        ghostPlayer.stop();
                    });
                })
        );
        withSubcommand(new CommandAPICommand("link")
                .withPermission("mixer.command.link")
                .withArguments(new LocationArgument("jukebox", LocationType.BLOCK_POSITION))
                .executesPlayer((player, args) -> {
                    Location jukeboxLoc = (Location) args.get(0);
                    Block block = jukeboxLoc.getBlock();

                    if(!block.getType().equals(Material.JUKEBOX)) {
                        player.sendMessage(MM.deserialize("<red>No jukebox at location"));
                        return;
                    }

                    JsonArray linked;

                    NBTTileEntity jukebox = new NBTTileEntity(block.getState());
                    String data = jukebox.getPersistentDataContainer().getString("mixer_links");
                    if(data == null || data.isEmpty()) {
                        linked = new JsonArray();
                    } else {
                        linked = (JsonArray) JsonParser.parseString(data);
                    }

                    Location loc = player.getLocation().toCenterLocation();

                    JsonObject locData = new JsonObject();
                    locData.addProperty("x", loc.getX());
                    locData.addProperty("y", loc.getY());
                    locData.addProperty("z", loc.getZ());
                    locData.addProperty("world", loc.getWorld().getName());

                    linked.add(locData);
                    jukebox.getPersistentDataContainer().setString("mixer_links", linked.toString());

                    player.sendMessage(MM.deserialize("<green>Location linked to jukebox"));
                })
        );
        register();
    }
}
