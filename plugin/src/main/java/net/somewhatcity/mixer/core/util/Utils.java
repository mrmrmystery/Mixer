/*
 * Copyright (c) 2023 mrmrmystery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next paragraph) shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.mixer.core.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.somewhatcity.mixer.core.MixerPlugin;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Utils {
    public static boolean isDisc(ItemStack item) {
        return item.getType().name().contains("MUSIC_DISC");
    }

    public static OkHttpClient client = new OkHttpClient();

    public static JsonObject loadNbtData(Location location, String category) {
        if(!location.getBlock().getType().equals(Material.JUKEBOX)) return null;

        Jukebox jukebox = (Jukebox) location.getBlock().getState();
        NamespacedKey key = new NamespacedKey(MixerPlugin.getPlugin(), category);

        String data = jukebox.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(data == null || data.isEmpty()) return new JsonObject();

        return (JsonObject) JsonParser.parseString(data);
    }

    public static void saveNbtData(Location location, String category, JsonObject data) {
        if(!location.getBlock().getType().equals(Material.JUKEBOX)) return;
        Jukebox jukebox = (Jukebox) location.getBlock().getState();
        NamespacedKey key = new NamespacedKey(MixerPlugin.getPlugin(), category);

        jukebox.getPersistentDataContainer().set(key, PersistentDataType.STRING, data.toString());
    }

    public static byte[] shortToByte(short[] input) {
        int index;
        int iterations = input.length;

        ByteBuffer bb = ByteBuffer.allocate(input.length * 2);

        for(index = 0; index != iterations; ++index)
        {
            bb.putShort(input[index]);
        }

        return bb.array();
    }

    public static short[] byteToShort(byte[] input) {
        int iterations = input.length / 2;
        short[] output = new short[iterations];

        ByteBuffer bb = ByteBuffer.wrap(input);

        for (int index = 0; index < iterations; index++) {
            output[index] = bb.getShort();
        }

        return output;
    }

    public static String requestCobaltMediaUrl(String url) {
        JsonObject send = new JsonObject();
        send.addProperty("url", url);
        send.addProperty("isAudioOnly", true);

        RequestBody body = RequestBody.create(send.toString().getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .get()
                .url("https://api.cobalt.tools/api/json")
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try(Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            JsonObject json = (JsonObject) JsonParser.parseString(res);
            if(json.has("url")) {
                return json.get("url").getAsString();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
