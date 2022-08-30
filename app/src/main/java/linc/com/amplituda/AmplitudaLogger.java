package linc.com.amplituda;

import android.util.Log;

import java.util.Locale;

final class AmplitudaLogger {

    static final String OPERATION_PROCESSING = "Processing";
    static final String OPERATION_PREPARING = "Preparing";
    private static final String LIB_TAG = "AMPLITUDA";
    private static int priority = Log.DEBUG;
    private static boolean enable = false;

    /**
     * Print message to logcat
     * @param operationLabel - operation label for message
     * @param start - operation start time in millis
     */
    synchronized static void logOperationTime(
            final String operationLabel,
            final long start
    ) {
        log(String.format(
                Locale.getDefault(),
                "%s time: %.04f seconds",
                operationLabel,
                ((System.currentTimeMillis() - start) / 1000f)
        ));
    }

    /**
     * Print message to logcat
     * @param msg - message
     */
    synchronized static void log(final String msg) {
        if(enable) Log.println(priority, LIB_TAG, msg);
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
     * @param logPrior - android Log priority constant.
     */
    synchronized static void priority(final int logPrior) {
        priority = logPrior;
    }

}
