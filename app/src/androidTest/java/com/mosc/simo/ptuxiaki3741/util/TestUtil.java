package com.mosc.simo.ptuxiaki3741.util;

public class TestUtil {
    public static String getRandomAlphaNumericString(int size) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "0123456789";
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}
