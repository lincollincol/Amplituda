package com.linc.amplituda.callback;

import com.linc.amplituda.exceptions.AmplitudaException;

/**
 * Callback interface for error events
 */
public interface AmplitudaErrorListener {
    void onError(final AmplitudaException exception);
}
