package linc.com.amplituda;

public final class Compress {

    static final int NONE = 1;
    public static final int SKIP = 2;
    public static final int PEEK = 3;
    public static final int AVERAGE = 4;

    private final int type;
    private final int preferredSamplesPerSecond;

    private Compress(final int type, final int preferredSamplesPerSecond) {
        this.type = type;
        this.preferredSamplesPerSecond = preferredSamplesPerSecond;
    }

    public static Compress withParams(final int type, final int preferredSamplesPerSecond) {
        if(type > NONE && preferredSamplesPerSecond <= 0) {
            return getDefault();
        }
        return new Compress(type, preferredSamplesPerSecond);
    }

    public int getType() {
        return type;
    }

    public int getPreferredSamplesPerSecond() {
        return preferredSamplesPerSecond;
    }

    static Compress getDefault() {
        return withParams(NONE, NONE);
    }
}
