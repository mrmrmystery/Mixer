package net.somewhatcity.mixer.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private static List<Material> discs = Arrays.asList(
        Material.MUSIC_DISC_5,
        Material.MUSIC_DISC_11,
        Material.MUSIC_DISC_13,
        Material.MUSIC_DISC_BLOCKS,
        Material.MUSIC_DISC_CAT,
        Material.MUSIC_DISC_CHIRP,
        Material.MUSIC_DISC_FAR,
        Material.MUSIC_DISC_MALL,
        Material.MUSIC_DISC_MELLOHI,
        Material.MUSIC_DISC_STAL,
        Material.MUSIC_DISC_STRAD,
        Material.MUSIC_DISC_WAIT,
        Material.MUSIC_DISC_WARD,
        Material.MUSIC_DISC_PIGSTEP,
        Material.MUSIC_DISC_RELIC,
        Material.MUSIC_DISC_OTHERSIDE
    );

    public static boolean isDisc(ItemStack item) {
        return discs.contains(item.getType());
    }
}
