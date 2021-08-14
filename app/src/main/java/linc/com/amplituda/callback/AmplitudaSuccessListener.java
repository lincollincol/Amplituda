package linc.com.amplituda.callback;

import linc.com.amplituda.AmplitudaResult;

/**
 * Callback interface for success processing event
 */
public interface AmplitudaSuccessListener<T> {
    void onSuccess(final AmplitudaResult<T> result);
}
