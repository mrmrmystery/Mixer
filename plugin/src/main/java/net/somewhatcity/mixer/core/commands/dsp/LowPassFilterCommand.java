/*
 * Copyright (c) 2024 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.commands.dsp;

import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.somewhatcity.mixer.core.util.Utils;
import org.bukkit.Location;

public class LowPassFilterCommand extends CommandAPICommand {
    public LowPassFilterCommand() {
        super("lowPassFilter");
        withArguments(new FloatArgument("frequency"));
        executes((sender, args) -> {
            Location location = (Location) args.get(0);
            float cutoffFrequency = (float) args.get(1);

            JsonObject obj = Utils.loadNbtData(location, "mixer_dsp");
            if(obj == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No jukebox at location"));
                return;
            }

            JsonObject settings = new JsonObject();
            settings.addProperty("frequency", cutoffFrequency);

            obj.add("lowPassFilter", settings);

            Utils.saveNbtData(location, "mixer_dsp", obj);
        });
    }
}
