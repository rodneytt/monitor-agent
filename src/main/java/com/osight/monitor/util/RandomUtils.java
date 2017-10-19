package com.osight.monitor.util;

import java.util.Random;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class RandomUtils {
    private static Random random = new Random();

    public static String getRandomString(int length) {
        String base = "0123456789abcdefghijklmnopqrstuvwxyz";
        int baseLength = base.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(baseLength);
            sb.append(base.charAt(index));
        }
        return sb.toString();
    }

}
