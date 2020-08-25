package linc.com.amplituda;

public class Amplituda {

    static {
        System.loadLibrary("native-lib");
    }

    public void init() {
        System.out.println("AMPLITUDE ===================== " + stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    native String stringFromJNI();
}
