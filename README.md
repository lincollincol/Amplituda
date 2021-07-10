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

## Simple Example
### • Process audio and handle result
#### Java
``` java
Amplituda amplituda = new Amplituda(this);
// . . .
amplituda.fromFile("/storage/emulated/0/Music/Linc - Amplituda.mp3")
    .amplitudesAsList(amplitudes -> {
        wavefrom.setSamples(amplitudes);
    })
    .amplitudesForSecond(5, amplitudes -> {
        System.out.println(String.format(
            Locale.getDefault(),
            "Amplitudes for second 5: %s",
            Arrays.toString(amplitudes.toArray())
        ));
    });
```  
#### Kotlin
``` kotlin
val amplituda = Amplituda(this)
// . . .
amplituda.apply {
    fromFile("/storage/emulated/0/Music/Linc - Amplituda.mp3")
    amplitudesAsList { amplitudes: List<Int> ->
        wavefrom.setSamples(amplitudes)
    }
    amplitudesForSecond(5) { amplitudes: List<Int> ->
        println(String.format(
            Locale.getDefault(),
            "Amplitudes for second 5: %s",
            Arrays.toString(amplitudes.toTypedArray())
        ))
    }
}

```

### • Error handling

#### Java
``` java
Amplituda amplituda = new Amplituda(this);
// . . . 
amplituda.setLogConfig(Log.DEBUG, true)
    .setErrorListener(error -> {
        if(error instanceof AmplitudaIOException) {
            System.out.println("IO Exception!");
        }
    });
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(this)
// . . . 
amplituda.setLogConfig(Log.DEBUG, true)
    .setErrorListener { error: AmplitudaException? ->
        if (error is AmplitudaIOException) {
            println("IO Exception!")
        }
    }
```
# Usage 

Amplituda library provide processed audio data to draw a waveform. You can get this data in different format: 
* sequence (single/new line)
* json
* list of integers.

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

## Examples 

### • Get amplitudes from audio as json (String):  

#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsJson(json -> {
            System.out.println("As json ====== " + json);
         });
// Output: [0, 0, 0, 0, 0, 5, 3, 6, . . . , 6, 4, 7, 1, 0, 0, 0]
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsJson {
            println("As json ====== $it")
         }
// Output: [0, 0, 0, 0, 0, 5, 3, 6, . . . , 6, 4, 7, 1, 0, 0, 0]
```  

### • Get amplitudes from audio as list of integers (List):  

#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsList(list -> {
            System.out.print("As list ====== ");
            for(int tmp : list) {
                System.out.print(tmp + " ");
            }
            System.out.println();
         });
// Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsList {
            print("As list ====== ")
            for(tmp in it) {
                print("$tmp ")
            }
            println()
         }
// Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
```  

### • Get amplitudes from audio as default single line sequence (String):  

#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, defSeq -> {
            System.out.println("As sequence default ====== " + defSeq);
         });
// Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT) {
            println("As sequence default ====== $it")
         }
// Output: 0 0 0 0 0 5 3 6 . . . 6 4 7 1 0 0 0
```  

### • Get amplitudes from audio as custom single line sequence (String):  

#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ", customSeq -> {
            System.out.println("As sequence custom ====== " + customSeq);
         });
// Output: 0 * 0 * 0 * 0 * 0 * 5 * 3 * 6 * . . . 6 * 4 * 7 * 1 * 0 * 0 * 0
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ") {
            println("As sequence custom ====== $it")
         }
// Output: 0 * 0 * 0 * 0 * 0 * 5 * 3 * 6 * . . . 6 * 4 * 7 * 1 * 0 * 0 * 0
```  

### • Get amplitudes from audio as new line sequence (String):  

#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, newLineSeq -> {
            System.out.println("As sequence new line ====== " + newLineSeq);
         });
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
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT) {
            println("As sequence new line ====== $it")
         }
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

### • Get amplitudes per second
#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesPerSecond(5, list -> {
            System.out.println(Arrays.toString(list.toArray()));
         });
/* Output: 
[0, 0, 5, 3, . . . 6, 1, 0, 0]
*/
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
         .amplitudesPerSecond(5) {
            println(Arrays.toString(list.toArray()))
         }
/* Output: 
[0, 0, 5, 3, . . . 6, 1, 0, 0]
*/
```

### • Get duration from input file
#### Java
``` java
Amplituda amplituda = new Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3");
System.out.println(amplituda.getDuration(Amplituda.SECONDS));
System.out.println(amplituda.getDuration(Amplituda.MILLIS));
/* Output: 
210
210000
*/
```
#### Kotlin
``` kotlin
val amplituda = Amplituda(context);
. . .
amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
println(amplituda.getDuration(Amplituda.SECONDS))
println(amplituda.getDuration(Amplituda.MILLIS))
/* Output: 
210
210000
*/
```

#### <a href="https://github.com/lincollincol/Amplituda/tree/master/example">Example app here</a>

# Download
## Gradle
``` groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
``` groovy
dependencies {
  implementation 'com.github.lincollincol:Amplituda:1.7'
}
```

## Maven
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
  <version>1.7</version>
</dependency>
```

## WARNING
### Amplituda process audio in the main thread !  You can run Amplituda with RxJava, Kotlin coroutines and Java Threads to process audio in the background therad.
Amplituda don't process audio in the background thread because of :
* You can use your own approach to work in the background thread. It makes Amplituda library more flexible.
* Reduce library size. Third-party library uses a lot of space and Amplituda delegates this task to user.

## Feedback
<a href="https://mail.google.com">linc.apps.sup@gmail.com</a>

# License
```
   Copyright 2020 lincollincol

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
