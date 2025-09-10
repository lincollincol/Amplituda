# Amplituda<img align="right" src="https://github.com/lincollincol/Amplituda/blob/master/img/amplituda_preview.png" width="25%">  

![GitHub release (latest by date)](https://img.shields.io/github/v/release/lincollincol/Amplituda)
[![](https://jitpack.io/v/lincollincol/Amplituda/month.svg)](https://jitpack.io/#lincollincol/Amplituda)
![GitHub](https://img.shields.io/github/license/lincollincol/Amplituda)

![GitHub followers](https://img.shields.io/github/followers/lincollincol?style=social)
![GitHub stars](https://img.shields.io/github/stars/lincollincol/Amplituda?style=social)
![GitHub forks](https://img.shields.io/github/forks/lincollincol/Amplituda?style=social)

## What is Amplituda?
Amplituda - an android library based on FFMPEG which process audio file and provide an array of samples. Based on the processed data, you can easily draw custom waveform using the value of the processed data array as the height of the single column.  
Average processing time is equal to 1 second for audio with duration **3 min 20 seconds** and **1 hour** audio will be processed in approximately 20 seconds.

Amplituda could be used with:
* [Compose AudioWaveform](https://github.com/lincollincol/compose-audiowaveform) - lightweight `Jetpack Compose` library which draws waveform.
* [WaveformSeekBar](https://github.com/massoudss/waveformSeekBar) - an android library which draws waveform using `android.view.View` (XML).
* Custom `View` or `Composable` implementation.
<p align="center">
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveforms.png" width="100%"/>
</p>

## How to use Amplituda? 
#### Example
``` java

/* Step 1: create Amplituda */
Amplituda amplituda = new Amplituda(context);

/* Step 2: process audio and handle result */
amplituda.processAudio("/storage/emulated/0/Music/Linc - Amplituda.mp3")
        .get(result -> {
            List<Integer> amplitudesData = result.amplitudesAsList();
            List<Integer> amplitudesForFirstSecond = result.amplitudesForSecond(1);
            long duration = result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS);
            String source = result.getAudioSource();
            InputAudio.Type sourceType = result.getInputAudioType();
            // etc
        }, exception -> {
            if(exception instanceof AmplitudaIOException) {
                System.out.println("IO Exception!");
            }
        });

/* And that's all! You can read full documentation below for more information about Amplituda features */

``` 

## Download
``` groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
``` groovy
dependencies {
  implementation 'com.github.lincollincol:amplituda:x.y.z'
}
```

### Full documentation 
#### • Process audio
``` java
Amplituda amplituda = new Amplituda(context);

/**
 * AmplitudaProcessingOutput<T> - wrapper class for Amplituda processing result.
 * This class stores the output audio processing data
 * and provides functions to obtain the result
 * Generic type T - audio source type: File, String (URL or path), Integer (res/raw) 
 */
AmplitudaProcessingOutput<T> processingOutput;

/* Process audio */

// Local audio file
processingOutput = amplituda.processAudio(new File("/storage/emulated/0/Music/Linc - Amplituda.mp3"));

// Path to local audio file
processingOutput = amplituda.processAudio("/storage/emulated/0/Music/Linc - Amplituda.mp3");

// URL audio
processingOutput = amplituda.processAudio("https://audio-url-example.com/amplituda.mp3");

// Resource audio
processingOutput = amplituda.processAudio(R.raw.amplituda);

```
#### • Compress output data
``` java
/** 
 * Compress result data (optional)
 * The output data can contain a lot of samples. 
 * For example: 
 *  - 5-second audio can contain 100+ samples
 *  - 3-minute audio can contain 7000+ samples
 * You can pass `Compress` params with the number of 
 * preferred samples per second and compress type as a parameter.
 * Compress types:
 * - Compress.SKIP    - take first number of `preferredSamplesPerSecond` and skip others 
 * - Compress.PEEK    - take peek number of `preferredSamplesPerSecond` and skip others
 * - Compress.AVERAGE - merge all samples to number of `preferredSamplesPerSecond`
 */ 
 
// Example: input audio duration - 10 seconds
 
amplituda.processAudio(<audio>, Compress.withParams(Compress.AVERAGE, 1));
// Output: data with 1 sample per 1 second. 
// Approximate output data size - 60 [duration] * 1 [preferredSamplePerSecond] = 60 [samples]

amplituda.processAudio(<audio>, Compress.withParams(Compress.AVERAGE, 5));
// Output: data with 5 sample per 1 second
// Approximate output data size - 60 [duration] * 5 [preferredSamplePerSecond] = 300 [samples]
```

#### • Cache output data
``` java
/** 
 * Cache result data (optional)
 * Amplituda provides cache implementation. 
 * You can reuse already processed audio files. 
 * Cache states:
 * - Cache.REUSE   - use already cached data or write
 *                   processed data to cache (if cache data doesn't exist) 
 * - Cache.REFRESH - update existing cache data. 
 *                   WARNING: use this cache state, when you need to update existing cache data  
 * Key (optional)  - custom cache key. Should be unique for each audio.
 */ 
 
// Enable amplituda cache feature:
amplituda.processAudio(<audio>, Cache.withParams(Cache.REUSE));
// Cache with custom key. The key should be unique 
amplituda.processAudio(<audio>, Cache.withParams(Cache.REUSE, "my-unique-key"));

// Clear all cache data
amplituda.clearCache();
// Clear cache data for specific audio (if exist)
amplituda.clearCache(<audio>);
amplituda.clearCache("/storage/emulated/0/Music/Linc - Amplituda.mp3", false);
// Clear cache data by custom key. 
amplituda.clearCache("my-unique-key", true);

```

#### • Handle progress
``` java
/**
 * AmplitudaProgressListener - progress listener class.
 * This class has 3 methods that describe current progress:
 *  - void onStartProgress() - amplituda start processing (optional)
 *  - void onStopProgress()  - amplituda stop processing (optional)
 *  - void onProgress(ProgressOperation operation, int progress) -
 *       amplituda process audio and share current operation and progress in percent (0-100)
 * onProgress() also inform about current operation:
 *  - PROCESSING  - amplituda start process audio
 *  - DECODING    - amplituda decode raw/res audio 
 *  - DOWNLOADING - amplituda download audio from url 
 */
amplituda.processAudio(
    <audio>,
    new AmplitudaProgressListener() {
        @Override
        public void onStartProgress() {
            super.onStartProgress();
            System.out.println("Start Progress");
        }

        @Override
        public void onStopProgress() {
            super.onStopProgress();
            System.out.println("Stop Progress");
        }

        @Override
        public void onProgress(ProgressOperation operation, int progress) {
            String currentOperation = "";
            switch (operation) {
                case PROCESSING: currentOperation = "Process audio"; break;
                case DECODING: currentOperation = "Decode resource"; break;
                case DOWNLOADING: currentOperation = "Download audio from url"; break;
            }
            System.out.printf("%s: %d%% %n", currentOperation, progress);
        }
    }
)
```

#### • Handle result and errors
``` java
AmplitudaProcessingOutput<T> processingOutput;
// . . . process audio . . .

/**
 * AmplitudaResult<T> - wrapper class for the final result
 * This class also provides many functions to format result: List<Integer>, String etc
 * and info about input audio: 
 *  - Input audio source: path to audio/resource or url. 
 *  - Source type according to input audio: FILE, PATH, URL or RESOURCE 
 *  - Audio duration
 * Generic type T - audio source type: File, String (URL or path), Integer (res/raw)
 */
 AmplitudaResult<T> result;
 
 /**
  * After processing you can get the result by calling the function get() 
  * This function has multiple overloads:
  *  - void get(successListener, errorListener)
  *  - void get(successListener)
  *  - AmplitudaResult<T> get(errorListener)
  *  - AmplitudaResult<T> get(successListener)
  */
 
// Overload #1: result as a callback with error listener
processingOutput.get(result -> { 
        /* handle result here */ 
    }, exception -> { 
        /* handle errors here */ 
    });

// Overload #2: result as a callback without error listener
processingOutput.get((AmplitudaSuccessListener<T>) result -> { 
    /* handle result here */ 
});

// Overload #3: result as a returned object with error listener
result = processingOutput.get((AmplitudaException exception) -> { 
    /* handle errors here */ 
});

// Overload #4: result as a returned object without error listener
result = processingOutput.get();

/**
 * Exceptions. More info about exceptions here:
 * https://github.com/lincollincol/Amplituda/tree/master/app/src/main/java/linc/com/amplituda/exceptions
 */
processingOutput.get((AmplitudaException exception) -> {
    if(exception instanceof AmplitudaIOException) {
        // Handle io exceptions
    } else if(exception instanceof AmplitudaProcessingException) {
        // Handle processing exceptions
    } else {
        exception.printStackTrace();
    }
});

```

#### • Format result


``` java
/**
 * Format result. As written earlier, AmplitudaResult
 * has many functions for formatting the result
 */
 AmplitudaResult<T> result;
 
 // Get result as list:
 List<Integer> samples = result.amplitudesAsList(); 
 System.out.println(Arrays.toString(samples.toArray()));
 // Output: [0, 0, 0, 0, 0, 5, 3, 6, . . . , 6, 4, 7, 1, 0, 0, 0]
 
 // Get result as list only for second `1`
 List<Integer> samplesForFirstSecond = result.amplitudesForSecond(1);
 System.out.println(Arrays.toString(samples.toArray()));
 // Output: [0, 0, 5, . . . 6, 4, 0]

// Get result as json format String
System.out.println(result.amplitudesAsJson());
// Output: [0, 0, 0, 0, 0, 5, 3, 6, . . . , 6, 4, 7, 1, 0, 0, 0]

// Get result as single line sequence format String (horizontal String)
System.out.println(result.amplitudesAsSequence(
        AmplitudaResult.SequenceFormat.SINGLE_LINE)
);
// Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0

// Get result as single line sequence format String with custom delimiter `*` (horizontal String)
System.out.println(result.amplitudesAsSequence(
        AmplitudaResult.SequenceFormat.SINGLE_LINE, " * ")
);
// Output: 0 * 0 * 0 * 0 * 0 * 5 * 3 * 6 * . . . 6 * 4 * 7 * 1 * 0 * 0 * 0

// Get result as new line sequence format String  
System.out.println(result.amplitudesAsSequence(
        AmplitudaResult.SequenceFormat.NEW_LINE)
);
/* Output: 
    0 
    0 
    5 
    3 
    . . . 
    6 
    1 
    0 
    0
    */

```

#### • Enable Amplituda logs (optional)
``` java
Amplituda amplituda = new Amplituda(context);

// Use default android Log constants to set priority. The second parameter - enable or disable logs. 
// Amplituda logs are disabled by default
amplituda.setLogConfig(Log.DEBUG, true);
```

### Permissions
Add permissions to Manifest.xml file in your app and grant it, before using Amplituda
``` xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- Android 10+ -->
<application
    android:requestLegacyExternalStorage="true">
    <activity . . ./>
</application>
```
### Reduce size
Add ``` android:extractNativeLibs="false" ``` to application in the Manifest.xml

``` xml
<application
      . . .
    android:extractNativeLibs="false"
      . . . >
    <activity . . ./>
</application>
```

### Build scripts (FFmpeg .so binaries configuration)
The FFmpeg binaries for this project were assembled with help of [ffmpeg-android-maker](https://github.com/Javernaut/ffmpeg-android-maker) by Javernaut (Oleksandr Berezhnyi).
Custom Amplituda configuration (`configuration-build.sh`) is available in the root directory of the project.

## Feedback
<a href="https://mail.google.com">andriy.serb1@gmail.com</a>

# License

```
   Copyright 2020-present lincollincol

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   FFmpeg
   -------
   Third-party components:
   This software uses libraries from the FFmpeg project under the LGPLv2.1. 
   FFmpeg is a separate project and is licensed under the LGPL. See https://ffmpeg.org for details.
   See FFmpeg License and Legal Considerations (https://www.ffmpeg.org/legal.html)
```