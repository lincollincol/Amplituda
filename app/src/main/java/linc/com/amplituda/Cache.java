package linc.com.amplituda;

public class Cache {

    static final int NONE = 1;
    static final String DEFAULT_KEY = "";
    public static final int REUSE = 2;
    public static final int REFRESH = 3;

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
        } else if(key == null) {
            return withParams(state, DEFAULT_KEY);
        }
        return new Cache(state, key);
    }

    public int getState() {
        return state;
    }

    public String getKey() {
        return key;
    }

    boolean isEnabled() {
        return getState() != Cache.NONE;
    }

    static Cache getDefault() {
        return withParams(NONE);
    }

}