package linc.com.amplituda;

import android.util.Log;

public final class AmplitudaLogger {

    private static final String LIB_TAG = "AMPLITUDA";
    private static int priority;
    private static boolean enable;

    /**
     * Print message to logcat
     * @param msg - message
     */
    synchronized static void log(final String msg) {
        if(enable)
            Log.println(priority, LIB_TAG, msg);
    }

    /**
     * Turn on/off logger
     * @param logEnable - on or off boolean flag
     */
    synchronized static void enable(final boolean logEnable) {
        enable = logEnable;
    }

    /**
     * Set log message priority
     * @param - android Log priority constant.
     */
    synchronized static void priority(final int logPrior) {
        priority = logPrior;
    }

}
