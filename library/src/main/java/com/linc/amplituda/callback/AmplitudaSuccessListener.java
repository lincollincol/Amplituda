package com.linc.amplituda.callback;

import com.linc.amplituda.AmplitudaResult;

/**
 * Callback interface for success processing event
 */
public interface AmplitudaSuccessListener<T> {
    void onSuccess(final AmplitudaResult<T> result);
}
