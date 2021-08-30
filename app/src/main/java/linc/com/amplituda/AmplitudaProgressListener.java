package linc.com.amplituda;

public abstract class AmplitudaProgressListener {

    private ProgressOperation operation;

    void onOperationChanged(ProgressOperation operation) {
        this.operation = operation;
    }

    void onProgressInternal(int progress) {
        onProgress(operation, progress);
    }

    /**
     * Public API
     */

    public void onStartProgress() {
    }

    public void onStopProgress() {
    }

    public abstract void onProgress(ProgressOperation operation, int progress);

}
