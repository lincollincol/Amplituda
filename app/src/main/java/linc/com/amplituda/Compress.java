package linc.com.amplituda;

public final class Compress {

    static final int NONE = 0;
    public static final int SKIP = 1;
    public static final int PEEK = 2;
    public static final int AVERAGE = 3;

    private final int type;
    private final int framesPerSecond;

    private Compress(final int type, final int framesPerSecond) {
        this.type = type;
        this.framesPerSecond = framesPerSecond;
    }

    public static Compress params(final int type, final int framesPerSecond) {
        return new Compress(type, framesPerSecond);
    }

    public int getType() {
        return type;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }
}
