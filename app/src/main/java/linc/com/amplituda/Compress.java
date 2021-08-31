package linc.com.amplituda;

public final class Compress {

    static final int NONE = 0;
    public static final int SKIP = 1;
    public static final int PEEK = 2;
    public static final int AVERAGE = 3;

    private final int type;
    private final int preferredSamplesPerSecond;

    private Compress(final int type, final int preferredSamplesPerSecond) {
        this.type = type;
        this.preferredSamplesPerSecond = preferredSamplesPerSecond;
    }

    public static Compress withParams(final int type, final int preferredSamplesPerSecond) {
        return new Compress(type, preferredSamplesPerSecond);
    }

    public int getType() {
        return type;
    }

    public int getPreferredSamplesPerSecond() {
        return preferredSamplesPerSecond;
    }
}
