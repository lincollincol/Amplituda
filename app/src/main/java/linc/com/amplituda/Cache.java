package linc.com.amplituda;

public class Cache {

    static final int NONE = 1;
    public static final int REUSE = 2;
    public static final int REFRESH = 3;
    public static final String DEFAULT_KEY = "default";

    private final int state;
    private final String key;

    private Cache(final int state, final String key) {
        this.state = state;
        this.key = key;
    }

    public static Cache withParams(final int state) {
        return withParams(state, DEFAULT_KEY);
    }

    public static Cache withParams(final int state, final String key) {
        if(state < NONE || state > REFRESH) {
            return withParams(NONE);
        }
        return new Cache(state, key);
    }

    public int getState() {
        return state;
    }

    public String getKey() {
        return key;
    }

    static Cache getDefault() {
        return withParams(NONE);
    }

}