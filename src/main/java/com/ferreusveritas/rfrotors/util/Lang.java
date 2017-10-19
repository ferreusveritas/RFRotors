package com.ferreusveritas.rfrotors.util;

import com.ferreusveritas.rfrotors.lib.Constants;

import net.minecraft.util.text.translation.I18n;

/**
 * Contains localization functions.
 */
public class Lang {

    /**
     * Localize the given info string. Used for info strings only, not block
     * names or textures.
     * @param pText identifier for the info string
     * @return Localized version of the string
     */
    public static String localize(String pText) {
        return I18n.translateToLocal("info." + Constants.MODID + "." + pText);
    }
}
