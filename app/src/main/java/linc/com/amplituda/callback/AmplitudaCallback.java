package linc.com.amplituda.callback;

/**
 * Base Callback interface
 */
interface AmplitudaCallback<T> {
    void call(final T data);
}
