/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.audio;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import net.kyori.adventure.text.Component;
import net.somewhatcity.mixer.Mixer;
import net.somewhatcity.mixer.MixerVoicechatPlugin;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Comparator;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Redstone;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MixerAudioPlayer {

    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
    private static final VoicechatServerApi API = (VoicechatServerApi) MixerVoicechatPlugin.api;
    private static final AudioPlayerManager APM = new DefaultAudioPlayerManager();

    private LocationalAudioChannel channel;
    private AudioPlayer audioPlayer;
    private AudioTrack audioTrack;
    private Timer audioTimer;
    private EqualizerFactory equalizer;
    private MixerFactory mixerFilter;
    private BlockDisplay[] displays = new BlockDisplay[2048];
    private Executor executor = Executors.newCachedThreadPool();
    private int realCooldown = 0;
    public static Graphics2D g2;

    static {
        AudioSourceManagers.registerRemoteSources(APM);
        APM.setFrameBufferDuration(0);
        APM.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public MixerAudioPlayer(List<Location> locs, List<RedstonePoint> redstones, String objectiveName) {
        List<LocationalAudioChannel> channels = new ArrayList<>();

        locs.forEach(loc -> {
            loc.toCenterLocation().add(0, 1, 0);
            LocationalAudioChannel channel = API.createLocationalAudioChannel(
                    UUID.randomUUID(),
                    API.fromServerLevel(loc.getWorld()),
                    API.createPosition(loc.getX(), loc.getY(), loc.getZ())
            );
            channel.setCategory("mixer");
            channel.setDistance(100);
            channels.add(channel);
        });
        Objective objective = null;
        if(objectiveName != null) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            objective = scoreboard.getObjective(objectiveName);
            if(objective == null) objective = scoreboard.registerNewObjective(objectiveName, Criteria.DUMMY, Component.text("mixer_" + objectiveName));
        }


        equalizer = new EqualizerFactory();
        mixerFilter = new MixerFactory();

        audioPlayer = APM.createPlayer();

        audioPlayer.setFilterFactory(mixerFilter);

        audioTimer = new Timer();
        Objective finalObjective = objective;
        audioTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    AudioFrame frame = audioPlayer.provide();
                    if (frame != null) {
                        byte[] data = frame.getData();
                        channels.forEach(ch -> {
                            ch.send(data);
                        });
                    }

                    double[] mags = smooth(mixerFilter.getMagnitudes(), 4);

                    executor.execute(() -> {
                        try {
                            Thread.sleep(320);
                            if(finalObjective != null) {
                                for(int i = 0; i < mags.length; i++) {
                                    Score score = finalObjective.getScore("#" + i);
                                    score.setScore((int) (mags[i] / 10));
                                }
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    redstones.forEach(point -> {
                        int delay = point.getDelay();

                        int mag = point.getMagnitude();
                        if(mag > 0 && mag < mags.length) {
                            int newMag = (int) Math.round(mags[mag] / 10);
                            if(newMag > point.getTrigger()) {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Bukkit.getScheduler().runTask(Mixer.getPlugin(), () -> {
                                            point.getLocation().getBlock().setType(Material.REDSTONE_BLOCK);
                                        });
                                    }
                                }, delay); // 320 default
                            } else {
                                Bukkit.getScheduler().runTask(Mixer.getPlugin(), () -> {
                                    point.getLocation().getBlock().setType(Material.AIR);
                                });
                            }
                        }

                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20);

        audioPlayer.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                audioTimer.cancel();
            }

            @Override
            public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
                audioTimer.cancel();
            }
        });
    }

    public void loadAudio(String url, boolean directPlayback, AudioPlayerCallback callback) {

        APM.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioTrack = track;
                callback.onLoaded(track.getInfo());
                if(directPlayback) audioPlayer.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                audioTrack = playlist.getTracks().get(0);
                callback.onLoaded(audioTrack.getInfo());
                if(directPlayback) audioPlayer.playTrack(audioTrack);
            }

            @Override
            public void noMatches() {
                System.out.println("No matches");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Load failed");
            }
        });
    }


    public void loadAudio(String url, AudioPlayerCallback callback) {
        loadAudio(url, false, callback);
    }

    public void play() {
        audioPlayer.setPaused(false);
    }

    public void pause() {
        audioPlayer.setPaused(true);
    }

    public void stop() {
        audioPlayer.stopTrack();
        audioTimer.cancel();
    }

    public String createFromMag(double mag) {
        int iMag = (int) Math.round(mag / 10);
        return "|".repeat(Math.max(0, iMag));
    }

    public static double[] smooth(double[] data, int windowSize) {
        if (data == null || data.length == 0 || windowSize <= 0) {
            return data;
        }

        double[] smoothedData = new double[data.length];
        int halfWindowSize = windowSize / 2;

        for (int i = 0; i < data.length; i++) {
            double sum = 0.0;
            int count = 0;

            for (int j = Math.max(0, i - halfWindowSize); j <= Math.min(data.length - 1, i + halfWindowSize); j++) {
                sum += data[j];
                count++;
            }

            smoothedData[i] = sum / count;
        }

        return smoothedData;
    }



    public void bassBoost(float percentage) {
        float multiplier = percentage / 100.0f;
        for(int i = 0; i < BASS_BOOST.length; i++) {
            equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }
        audioPlayer.setFilterFactory(equalizer);
    }

    public void resetFilters() {
        audioPlayer.setFilterFactory(null);
    }
}
