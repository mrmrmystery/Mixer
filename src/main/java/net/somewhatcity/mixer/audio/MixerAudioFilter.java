/*
 * Copyright (c) 2024 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.audio;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.Arrays;

public class MixerAudioFilter implements FloatPcmAudioFilter {

    private FloatPcmAudioFilter next;
    private float[] samples;

    public MixerAudioFilter(FloatPcmAudioFilter next) {
        this.next = next;
    }
    @Override
    public void process(float[][] floats, int i, int i1) throws InterruptedException {
        samples = floats[0];

        next.process(floats, i, i1);
    }

    @Override
    public void seekPerformed(long l, long l1) {

    }

    @Override
    public void flush() throws InterruptedException {

    }

    @Override
    public void close() {

    }

    public double[] getSamples() {
        double[] converted = new double[samples.length];
        for(int i = 0; i < samples.length; i++) {
            converted[i] = samples[i];
        }
        return converted;
    }
}
