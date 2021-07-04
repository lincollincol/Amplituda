package linc.com.amplituda;

import android.util.Log;

public class AmplitudaLogger {

    private static final String LIB_TAG = "AMPLITUDA";
    private static int priority = Log.DEBUG;
    private static boolean enable = false;

    synchronized static void log(final String msg) {
        if(enable)
            Log.println(priority, LIB_TAG, msg);
    }

    synchronized static void enable(final boolean logEnable) {
        enable = logEnable;
    }

    synchronized static void priority(final int logPrior) {
        priority = logPrior;
    }

}
