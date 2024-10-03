/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.core.commands.dsp.DspCommand;
import net.somewhatcity.mixer.core.MixerPlugin;
import net.somewhatcity.mixer.core.util.MessageUtil;
import net.somewhatcity.mixer.core.util.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MixerCommand extends CommandAPICommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final AudioPlayerManager APM = new DefaultAudioPlayerManager();

    static {
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true, new Client[] {
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new AndroidLiteWithThumbnail()
        });
        APM.registerSourceManager(youtube);
        APM.registerSourceManager(new YandexMusicAudioSourceManager(true));
        APM.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        APM.registerSourceManager(new BandcampAudioSourceManager());
        APM.registerSourceManager(new VimeoAudioSourceManager());
        APM.registerSourceManager(new TwitchStreamAudioSourceManager());
        APM.registerSourceManager(new BeamAudioSourceManager());
        APM.registerSourceManager(new GetyarnAudioSourceManager());
        APM.registerSourceManager(new NicoAudioSourceManager());
        APM.registerSourceManager(new HttpAudioSourceManager());
        APM.registerSourceManager(new LocalAudioSourceManager());

        APM.setFrameBufferDuration(100);
    }

    public MixerCommand() {
        super("mixer");
        withSubcommand(new CommandAPICommand("burn")
                .withPermission("mixer.command.burn")
                .withArguments(new GreedyStringArgument("url"))
                .executesPlayer((player, args) -> {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (!Utils.isDisc(item)) {
                        MessageUtil.sendErrMsg(player, "You must be holding a music disc!");
                        return;
                    }

                    MessageUtil.sendMsg(player, "Loading track. Please wait...");



                    EXECUTOR_SERVICE.submit(() -> {
                        String url = (String) args.get(0);
                        String oldUrl;

                        if(url.startsWith("file:")) {
                            String filename = url.substring(5);
                            File file = new File(filename);
                            if(file.exists() && file.isFile()) {
                                url = file.getAbsolutePath();
                            }
                        }
                        if(url.startsWith("cobalt:")) {
                            String uri = url.substring(7);
                            oldUrl = url;
                            url = Utils.requestCobaltMediaUrl(uri);
                            if(url == null) {
                                player.sendMessage("§cError while loading cobalt media");
                                return;
                            }
                        }
                        else if (url.startsWith("https://www.youtube.com/") || url.startsWith("https://music.youtube.com/")) {
                            oldUrl = url;
                            url = Utils.requestCobaltMediaUrl(url);
                            if(url == null) {
                                player.sendMessage("§cError while loading cobalt media");
                                return;
                            }
                        }
                        else {
                            oldUrl = "";
                        }
                        String finalUrl = url;
                        APM.loadItem(url, new AudioLoadResultHandler() {

                            @Override
                            public void trackLoaded(AudioTrack audioTrack) {
                                AudioTrackInfo info = audioTrack.getInfo();
                                Bukkit.getScheduler().runTask(MixerPlugin.getPlugin(), () -> {
                                    String urlToSet;
                                    if(!oldUrl.isEmpty()) {
                                        urlToSet = oldUrl;
                                    } else {
                                        urlToSet = finalUrl;
                                    }

                                    item.editMeta((meta) -> {
                                        meta.displayName(MM.deserialize("<reset>%s".formatted(info.title)).decoration(TextDecoration.ITALIC, false));
                                        meta.lore(List.of(
                                                MM.deserialize("<reset><gray>%s".formatted(info.author)).decoration(TextDecoration.ITALIC, false)
                                        ));

                                        NamespacedKey mixerData = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_data");
                                        meta.getPersistentDataContainer().set(mixerData, PersistentDataType.STRING, urlToSet);

                                        JukeboxPlayableComponent playableComponent = meta.getJukeboxPlayable();
                                        playableComponent.setSong(JukeboxSong.BLOCKS);
                                        playableComponent.setShowInTooltip(false);

                                        meta.setJukeboxPlayable(playableComponent);
                                    });

                                    MessageUtil.sendMsg(player, "Successfully loaded track %s", info.title);
                                });
                            }

                            @Override
                            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                                AudioTrackInfo info = audioPlaylist.getSelectedTrack().getInfo();
                                Bukkit.getScheduler().runTask(MixerPlugin.getPlugin(), () -> {
                                    item.editMeta(meta -> {
                                        meta.displayName(MM.deserialize("<reset>%s".formatted(info.title)).decoration(TextDecoration.ITALIC, false));
                                        meta.lore(List.of(
                                                MM.deserialize("<reset><gray>%s".formatted(info.author)).decoration(TextDecoration.ITALIC, false)
                                        ));

                                        NamespacedKey mixerData = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_data");
                                        meta.getPersistentDataContainer().set(mixerData, PersistentDataType.STRING, finalUrl);

                                        JukeboxPlayableComponent playableComponent = meta.getJukeboxPlayable();
                                        playableComponent.setSong(JukeboxSong.BLOCKS);
                                        playableComponent.setShowInTooltip(false);

                                        meta.setJukeboxPlayable(playableComponent);
                                    });

                                    MessageUtil.sendMsg(player, "Successfully loaded track %s", info.title);
                                });
                            }

                            @Override
                            public void noMatches() {
                                MessageUtil.sendErrMsg(player, "No matches found");
                            }

                            @Override
                            public void loadFailed(FriendlyException e) {
                                MessageUtil.sendErrMsg(player, e.getMessage());
                            }
                        });
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
                        MessageUtil.sendErrMsg(player, "No jukebox found at location");
                        return;
                    }

                    JsonArray linked;

                    Jukebox jukebox = (Jukebox) block.getState();
                    NamespacedKey mixerLinks = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_links");

                    String data = jukebox.getPersistentDataContainer().get(mixerLinks, PersistentDataType.STRING);
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
                    jukebox.getPersistentDataContainer().set(mixerLinks, PersistentDataType.STRING, linked.toString());

                    MessageUtil.sendMsg(player, "Location linked to jukebox");
                })
        );
        withSubcommand(new CommandAPICommand("redstone")
                .withPermission("mixer.command.redstone")
                .withArguments(new LocationArgument("jukebox", LocationType.BLOCK_POSITION))
                .withArguments(new IntegerArgument("magnitude", 0, 2048))
                .withArguments(new IntegerArgument("trigger", 0))
                .withArguments(new IntegerArgument("delay", 0))
                .executesPlayer(((player, args) -> {
                    Location jukeboxLoc = (Location) args.get(0);
                    Block block = jukeboxLoc.getBlock();

                    if(!block.getType().equals(Material.JUKEBOX)) {
                        MessageUtil.sendErrMsg(player, "No jukebox found at location");
                        return;
                    }

                    JsonArray redstones;
                    Jukebox jukebox = (Jukebox) block.getState();
                    NamespacedKey mixerRedstones = new NamespacedKey(MixerPlugin.getPlugin(), "mixer_redstones");
                    String data = jukebox.getPersistentDataContainer().get(mixerRedstones, PersistentDataType.STRING);
                    if(data == null || data.isEmpty()) {
                        redstones = new JsonArray();
                    } else {
                        redstones = (JsonArray) JsonParser.parseString(data);
                    }

                    if(player.getTargetBlockExact(10) == null) {
                        MessageUtil.sendErrMsg(player, "Not looking at a block");
                        return;
                    }

                    Location loc = player.getTargetBlockExact(10).getLocation();

                    JsonObject locData = new JsonObject();
                    locData.addProperty("x", loc.getX());
                    locData.addProperty("y", loc.getY());
                    locData.addProperty("z", loc.getZ());
                    locData.addProperty("world", loc.getWorld().getName());
                    locData.addProperty("mag", (int) args.get(1));
                    locData.addProperty("trigger", (int) args.get(2));
                    locData.addProperty("delay", (int) args.get(3));

                    redstones.add(locData);
                    jukebox.getPersistentDataContainer().set(mixerRedstones, PersistentDataType.STRING, redstones.toString());

                    MessageUtil.sendMsg(player, "Redstone location linked to jukebox");
                }))
        );

        withSubcommand(new DspCommand());
        register();
    }
}
