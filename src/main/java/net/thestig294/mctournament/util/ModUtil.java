package net.thestig294.mctournament.util;

import net.minecraft.util.math.random.Random;

public class ModUtil {
//    Used for the fake room code shown on the bottom right of the QuestionScreen
//    (And potentially elsewhere, so I'm shoving this in a Util class :P)
    public static String getRandomString(int length, int numberChars){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String nums = "0123456789";
        char[] builder = new char[4];
        Random random = Random.create();

        for (int i = 0; i < length; i++) {
            if (random.nextBoolean() || numberChars == 0) {
                builder[i] = chars.charAt(random.nextInt(chars.length()));
            } else {
                builder[i] = nums.charAt(random.nextInt(nums.length()));
                numberChars--;
            }
        }

        return new String(builder);
    }

    public static int clampInt(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }
}
