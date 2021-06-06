package linc.com.amplituda;

import android.os.Build;
import android.os.Debug;

import java.util.Locale;

public class MemoryState {


    static void showState() {
//        Build.VERSION

        Runtime runtime = Runtime.getRuntime();
        long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
        long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;


        System.out.println("=============STATE MEMORY");
        System.out.println(String.format(Locale.US, "Used memory mb = %d\n Max size mb = %d\nAvailable size mb = %d",
            usedMemInMB, maxHeapSizeInMB, availHeapSizeInMB
        ));

        runtime = null;

        /*long nativeHeapSize = Debug.getNativeHeapSize();
        long nativeHeapFreeSize = Debug.getNativeHeapFreeSize();
        long usedMemInBytes = nativeHeapSize - nativeHeapFreeSize;
        long usedMemInPercentage = usedMemInBytes * 100 / nativeHeapSize;*/



    }

}

