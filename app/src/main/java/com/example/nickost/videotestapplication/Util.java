package com.example.nickost.videotestapplication;

/**
 * Created by nickost on 7/28/17.
 */

public class Util {

    /**
     * Given a progress percentage, return the time in the legacy_video associated with that
     * completion percentage.
     * @param progress percentage complete, 0-100
     * @return time in ms associated with that percentage
     */
    public static int getSeekTimeFromProgressPercentage(int progress, int duration) {
        return (progress * duration) / 100;
    }
}
