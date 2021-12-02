package com.mosc.simo.ptuxiaki3741.util;

import java.util.Locale;
import java.util.Random;

public final class EncryptUtil {
    private EncryptUtil(){}

    public static String convert4digit(long id){
        Random random = new Random();
        random.setSeed((id*4)/2);
        return String.format(Locale.getDefault(),"%04d", random.nextInt(10000));
    }
    public static String removeSpecialCharacters(String string){
        String ans = string.trim();
        ans = ans.replaceAll(
                "[@#€_&\\-+)(/?!;:'\"*✓™®©%{}\\[\\]=°^¢$¥£~`|\\\\•√π÷×¶∆<>,.]",
                ""
        );
        return ans.replaceAll(" +", " ");
    }
}
