# Amplituda<img align="right" src="https://github.com/lincollincol/Amplituda/blob/master/img/amplituda_preview.png" width="250" height="250">  

![GitHub release (latest by date)](https://img.shields.io/github/v/release/lincollincol/Amplituda)
![GitHub](https://img.shields.io/github/license/lincollincol/Amplituda)

![GitHub followers](https://img.shields.io/github/followers/lincollincol?style=social)
![GitHub stars](https://img.shields.io/github/stars/lincollincol/Amplituda?style=social)
![GitHub forks](https://img.shields.io/github/forks/lincollincol/Amplituda?style=social)

## What is Amplituda?
Amplituda - an android library based on FFMPEG which process audio file and provide an array of samples. Based on the processed data, you can easily draw custom waveform using the value of the processed data array as the height of the single column.  
Average processing time is equal to 1 second for audio with duration **3 min 20 seconds** and **1 hour** audio will be processed in approximately 20 seconds.

You can also use <a href="https://github.com/massoudss/waveformSeekBar">WaveformSeekBar</a> library which is fully compatible with Amplituda 
<p align="center">
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform_1.jpg" width="250" height="50"/>&#10240 &#10240
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform_2.jpg" width="250" height="50"/>
  <br/><br/>
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform_3.jpg" width="250" height="50"/>&#10240 &#10240
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform_4.jpg" width="250" height="50"/>
   <br/><br/>
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform_5.jpg" width="250" height="50"/>&#10240 &#10240
</p>

## How to use Amplituda? 
### • Process audio
``` java
Amplituda amplituda = new Amplituda(context);
// From String path
amplituda.fromFile("/storage/emulated/0/Music/Linc - Amplituda.mp3");
// From File
amplituda.fromFile(new File("/storage/emulated/0/Music/Linc - Amplituda.mp3"));
// From res/raw file
amplituda.fromFile(R.raw.amplituda);

/** Amplituda will process your file immediately after fromFile() call */
```  

### • Handle result
``` java
// Get result as list
amplituda.amplitudesAsList(amplitudes -> {
    System.out.println(Arrays.toString(amplitudes.toArray())); 
    // Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
})

// Get result as list only for second `1`
.amplitudesForSecond(1, amplitudes -> {
    System.out.println(Arrays.toString(amplitudes.toArray()));
    // Output: 0 0 5 . . . 6 4 0
})

// Get result as json format String 
.amplitudesAsJson(json -> {
    System.out.println(json);
    // Output: [0, 0, 0, 0, 0, 5, 3, 6, . . . , 6, 4, 7, 1, 0, 0, 0]
})

// Get result as single line sequence format String (horizontal String) 
.amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, horizontalSequenceString -> {
    System.out.println(horizontalSequenceString);
    // Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
})

// Get result as single line sequence format String with custom delimiter `*` (horizontal String)
.amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ", customHorizontalSequenceString -> {
    System.out.println(customHorizontalSequenceString);
    // Output: 0 * 0 * 0 * 0 * 0 * 5 * 3 * 6 * . . . 6 * 4 * 7 * 1 * 0 * 0 * 0
})

// Get result as new line sequence format String  
.amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, newLineSequenceString -> {
    System.out.println(newLineSequenceString);
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
});
```

### • Compress amplitudes (reduce output size)  

``` java
// When you call compressAmplitudes(/***/) - Amplituda will merge result amplitudes according to samplesPerSecond. 
// After the call you will no longer be able to get previous Amplituda data

// Input audio file duration is equal to 200 seconds (example)
amplituda.amplitudesAsList(amplitudes -> {
    System.out.println(amplitudes.size());
    // Output: ~ 8000
})
// Amplituda result size is equal to 8000 samples here (before compressAmplituda call)
// . . .
// Pass the desired number of samples per second to the parameters. In this example - `1` sample per second
.compressAmplitudes(1)
// . . .
// Amplituda result size is equal to 200 samples here (after compressAmplituda call)
.amplitudesAsList(amplitudes -> {
    System.out.println(amplitudes.size());
    // Output: ~ 200
});

```

### • Get duration from input file
``` java
System.out.printf(
        Locale.getDefault(),
        "Seconds: %d\nMillis: %d%n",
        amplituda.getDuration(Amplituda.SECONDS),
        amplituda.getDuration(Amplituda.MILLIS)
);
/* Output: 
Seconds: 210
Millis: 210000
*/
```
### • Handle errors
All exceptions <a href="https://github.com/lincollincol/Amplituda/tree/master/app/src/main/java/linc/com/amplituda/exceptions">here</a>

``` java
amplituda.setErrorListener(error -> {
    if(error instanceof AmplitudaIOException) {
        System.out.println("IO Exception!");
    }
});
```

### • Enable Amplituda logs
``` java
// Use default android Log constants to set priority. The second parameter - enable or disable logs. 
// Amplituda logs are disabled by default
amplituda.setLogConfig(Log.DEBUG, true);
```

## Permissions
Add permissions to Manifest.xml file in your app and grant it, before using Amplituda
``` xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
## Reduce size
Add ``` android:extractNativeLibs="false" ``` to application in the Manifest.xml

``` xml
<application
      . . .
    android:extractNativeLibs="false"
      . . . >
    <activity . . ./>
</application>
```

#### <a href="https://github.com/lincollincol/Amplituda/tree/master/example">Example app here</a>

## Download
### Gradle
``` groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
``` groovy
dependencies {
  implementation 'com.github.lincollincol:Amplituda:2.0.2'
}
```

### Maven
``` xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
``` xml
<dependency>
  <groupId>com.github.lincollincol</groupId>
  <artifactId>Amplituda</artifactId>
  <version>2.0.2</version>
</dependency>
```

## Feedback
<a href="https://mail.google.com">linc.apps.sup@gmail.com</a>

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
```
