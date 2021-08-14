package linc.com.amplituda.callback;


import linc.com.amplituda.exceptions.AmplitudaException;

/**
 * Callback interface for error events
 */
public interface AmplitudaErrorListener {
    void onError(final AmplitudaException exception);
}
