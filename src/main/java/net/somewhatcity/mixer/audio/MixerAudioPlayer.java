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
import net.somewhatcity.mixer.MixerVoicechatPlugin;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.bukkit.Location;

import java.util.*;

public class MixerAudioPlayer {

    private static final float[] BASS_BOOST = { 0.2f, 0.15f, 0.1f, 0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
    private static final VoicechatServerApi API = (VoicechatServerApi) MixerVoicechatPlugin.api;
    private static final AudioPlayerManager APM = new DefaultAudioPlayerManager();

    private LocationalAudioChannel channel;
    private AudioPlayer audioPlayer;
    private AudioTrack audioTrack;
    private Timer audioTimer;

    private EqualizerFactory equalizer;

    static {
        AudioSourceManagers.registerRemoteSources(APM);
        APM.setFrameBufferDuration(500);
        APM.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public MixerAudioPlayer(List<Location> locs) {
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

        equalizer = new EqualizerFactory();
        audioPlayer = APM.createPlayer();
        audioTimer = new Timer();
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
