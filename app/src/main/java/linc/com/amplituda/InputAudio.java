package linc.com.amplituda;

public final class InputAudio<T> {
    private final T source;
    private long duration;
    private InputAudio.Type type;

    InputAudio(final T source, long duration, final InputAudio.Type type) {
        this.source = source;
        this.type = type;
        this.duration = duration;
    }

    public InputAudio(T source, long duration) {
        this.source = source;
        this.duration = duration;
    }

    public InputAudio(T source, Type type) {
        this.source = source;
        this.type = type;
    }

    public InputAudio(T source) {
        this.source = source;
    }

    public T getSource() {
        return source;
    }

    public long getDuration() {
        return duration;
    }

    void setDuration(final long duration) {
        this.duration = duration;
    }

    public Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        FILE, PATH, URL, RESOURCE, INPUT_STREAM, BYTE_ARRAY
    }
}
