package com.kadir.twitterbots.elephant.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author akadir
 * Date: 16/01/2019
 * Time: 20:37
 */
public class ElephantUtil {
    private ElephantUtil() {
    }

    public static List<long[]> divideArray(long[] array, final int L) {
        List<long[]> parts = new ArrayList<>();

        final int N = array.length;

        for (int i = 0; i < N; i += L) {
            long[] tmp = new long[Math.min(N, i + L) - i];
            System.arraycopy(array, i, tmp, 0, tmp.length);
            parts.add(tmp);
        }

        return parts;
    }

}
