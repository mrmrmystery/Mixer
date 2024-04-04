/*
 * Copyright (c) 2024 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.api;

import net.somewhatcity.mixer.api.MixerApi;
import net.somewhatcity.mixer.api.MixerAudioPlayer;
import net.somewhatcity.mixer.core.MixerPlugin;
import net.somewhatcity.mixer.core.audio.IMixerAudioPlayer;
import org.bukkit.Location;

import java.util.HashMap;

public class ImplMixerApi implements MixerApi {
    private MixerPlugin plugin;
    public ImplMixerApi(MixerPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public IMixerAudioPlayer createPlayer(Location location) {
        if(plugin.playerHashMap().containsKey(location)) throw new IllegalStateException("player at this location already exists");
        return new IMixerAudioPlayer(location);
    }

    @Override
    public IMixerAudioPlayer getMixerAudioPlayer(Location location) {
        return plugin.playerHashMap().get(location);
    }
}
