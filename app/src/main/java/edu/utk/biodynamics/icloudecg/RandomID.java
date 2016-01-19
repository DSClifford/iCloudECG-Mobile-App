package edu.utk.biodynamics.icloudecg;

/**
 * Created by DSClifford on 8/8/2015.
 */

import java.util.UUID;

//Creates random file/user IDs

public class RandomID {

    public static String generateString()
    {

        String rng = UUID.randomUUID().toString();
        String text1 = rng.replaceAll("-", "");
        int length = 10;

        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = text1.charAt(i);
        }
        return new String(text);
    }

}
