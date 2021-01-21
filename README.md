<p align="center">
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/amplituda_preview.png" width="700" height="400">
</p>  

![GitHub release (latest by date)](https://img.shields.io/github/v/release/lincollincol/Amplituda)
![GitHub](https://img.shields.io/github/license/lincollincol/Amplituda)

![GitHub followers](https://img.shields.io/github/followers/lincollincol?style=social)
![GitHub stars](https://img.shields.io/github/stars/lincollincol/Amplituda?style=social)
![GitHub forks](https://img.shields.io/github/forks/lincollincol/Amplituda?style=social)


### This library using ffmpeg source. If you want to calculate amplitudes from and draw waveform - use Amplituda library
#### Used pre-build libraries: libavutil.so, libavcodec.so, libavformat.so, libavresampe.so

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
  implementation 'com.github.lincollincol:Amplituda:1.4'
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
  <version>1.4</version>
</dependency>
```

# Usage

#### <a href="https://github.com/lincollincol/Amplituda/tree/master/example">Example app here</a> 

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

### Supported formats (tested):
* mp3
* wav
* opus
* oga
* ogg 
* test more formats and contact me.

## WARNING
### Amplituda process audio in the main thread !  You can run Amplituda with RxJava, Kotlin coroutines and Java Threads to process audio in the background therad.
Amplituda don't process audio in the background thread because of :
* You can use your own approach to work in the background thread. It makes Amplituda library more flexible.
* Reduce library size. Third-party library uses a lot of space and Amplituda delegates this task to user.

## How to draw waveform
* You can use my <a href="https://github.com/lincollincol/waveformSeekBar">WaveformSeekBar library</a> fork in which Amplituda implemented. Example screenshot below: 
<p align="center">
  <img src="https://github.com/lincollincol/Amplituda/blob/master/img/waveform.jpg" width="400" height="100">
</p>  

* Use <a href="https://stackoverflow.com/questions/38744579/show-waveform-of-audio">PlayerVisualizerView</a> from this StackOverflow answer in which you should pass Amplituda data to ``` updateVisualizer() ``` as a parameter.
* Use another third-party library to draw waveform or create cutsom view in which this waveform view use processed audio data byAmplituda to draw every line. 

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
