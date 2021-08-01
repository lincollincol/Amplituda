package linc.com.amplituda.entity;

class AmplitudaProcessedEntity<T> {

    private final String path;
    private final int duration;
    private final T data;

    AmplitudaProcessedEntity(final String path, final int duration, final T data) {
        this.path = path;
        this.duration = duration;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public int getDuration() {
        return duration;
    }

    public T getData() {
        return data;
    }
}
