# CammentSDK for Android
**current version: 1.0.1**

To get started with the Camment Mobile SDK for Android you can set up the SDK and build a new project, or you can integrate the SDK in an existing project. 

The instructions were written for the following configuration:
- Android Studio 2.3.3
- Java 1.8.0_112
- Gradle 2.3.3 (distribution gradle-3.3-all.zip)

## Technical specification
**SDK version**
SDK is built with the following configuration:
```gradle
minSdkVersion 19
targetSdkVersion 26
compileSdkVersion 26
buildToolsVersion "26.0.1"
```
*Note:* If your application supports also lower versions, you have to handle enabling/disabling of the CammentSDK by yourself.

**Dependencies**
CammentSDK relies on following dependencies: 
- Amazon AWS SDK (v2.4.5)
- Facebook SDK (v4.25.0)
- Google Exoplayer (v2.4.4)
- Glide library (v4.0.0)
- EasyPermissions library (v0.4.2)
- Greenrobot EventBus (v3.0.0)
- Android Support AppCompat-v7 (v26.0.1)
- Android Support Design Library (v26.0.1)
- Android Support RecyclerView (v26.0.1)
- Android Support ConstraintLayout (v1.0.2)

*Note:* If you use some of these dependencies in your application too, you can remove them from your app gradle file. In case you want to override some dependencies, you can do it using gradle, e.g.:
```gradle
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        def requested = details.requested
        if (requested.group == 'com.android.support') {
            if (!requested.name.startsWith("multidex")) {
                details.useVersion '26.0.1'
            }
        }
    }
}
```
## Add CammentSDK to your project
SDK is available on the github in a maven structure, containing 2 important files: 
- ```cammentsdk-<sdk_version>.aar```
- ```cammentsdk-<sdk_version>.pom```

Add following repository url into your **project level** ```build.gradle``` file:
```gradle
allprojects {
    repositories {
        ... // your other repositories
        maven {
            url 'https://raw.githubusercontent.com/camment/sdk-android/master/sdk/'
        }
    }
}
```
Add following dependency into your **application level** ```build.gradle``` file:
```gradle
dependencies {
    ... //your other dependencies
    compile ('tv.camment.cammentsdk:cammentsdk:<sdk_version>@aar') {
        transitive true
    }
}
```
*Note*: ```transitive true``` means that gradle will download also the SDK dependencies

Now **sync the project with your gradle files** and **clean the project**. 

## Initialize CammentSDK
Open your application class extending ```Application``` and add following into the ```onCreate()``` method:
```java
public class YourApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ...
        CammentSDK.getInstance().init(this);
    }
}
```
*Note:* Make sure you specified this class also in the ```AndroidManifest.xml``` if you didn't use it before:
```xml
<application
        android:name=".YourApp"
        ...>
```
## Modify AndroidManifest.xml
Open your application ```AndroidManifest.xml``` and add:
1. specify your API key (security of the API key you have to handle by yourself):
```xml
<application
    ...>
    
    <meta-data
        android:name="tv.camment.cammentsdk.ApiKey"
        android:value="YOUR API KEY" />
       
</application>            
```
2. Specify ```ContentProvider``` used by CammentSDK as it has to have unique authority:
```xml
<application
    ...>
    
    <provider
            android:name="tv.camment.cammentsdk.data.DataProvider"
            android:authorities="${applicationId}.cammentsdk"
            android:enabled="true"
            android:exported="false" />
       
</application>            
```
```${applicationId}``` has to be specified in the **application level** ```build.gradle``` file.
```gradle
android {
    ...
    defaultConfig {
        applicationId "com.yourapp.yourapp" //replace with your package name
        ...
    }
}
```
## Add CammentSDK overlay on top of your video player
CammentSDK overlay should be included in the activity which shows video streaming.
```xml
<tv.camment.cammentsdk.views.CammentOverlay
        android:id="@+id/camment_overlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
CammentSDK overlay should be on top of every other view in the layout (usually defined as last in the layout). 
Example of layout:
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black"
    tools:context="tv.camment.cammentdemo.CammentMainActivity">

    <FrameLayout
        android:id="@+id/fl_parent"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <VideoView
            android:id="@+id/show_player"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_gravity="center" />
    </FrameLayout>

    <android.support.v4.widget.ContentLoadingProgressBar
        android:id="@+id/cl_progressbar"
        style="?android:attr/progressBarStyleLarge"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:visibility="gone"
        android:layout_gravity="center" />

    <tv.camment.cammentsdk.views.CammentOverlay
        android:id="@+id/camment_overlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```
As CammentSDK overlay intercepts all touch events, it has to know to which ```ViewGroup``` to pass the touch events in order not to disable underlying components. In the layout above ```VideoView``` is wrapped in ```FrameLayout``` to enable this.
Then in the activity where CammentOverlay is used, call ```setParentViewGroup(ViewGroup parentViewGroup)``` method.

CammentSDK also needs to know unique identifier of the currently watched show. Use ```setShowUuid(String showUuid)``` method.
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.<your_layout>);
    ...
    CammentSDK.getInstance().setShowUuid("<Any unique String identifier of your show>");
        
    FrameLayout parentViewGroup = (FrameLayout) findViewById(R.id.fl_parent);
    CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);
    cammentOverlay.setParentViewGroup(parentViewGroup);
}
```
## Pass onActivityResult and onRequestPermissionsResult to CammentSDK
CammentSDK handles permissions which it needs as well as Facebook Login. In order to complete the flow correctly, pass results of ```onActivityResult``` and ```onRequestPermissionsResult``` to CammentSDK. 

```onActivityResult``` should be overriden in all activities (use your BaseActivity or similar parent activity) where CammentSDK is used as Facebook Login may be performed e.g. when invitation request is received. 
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
}
```
```onRequestPermissionsResult``` should be overriden in the activity where CammentOverlay is used.
```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```
## React to Camment interactions 
For better user experience we recommend muting a video player when a user starts recording camment and decrease the volume at least by half when a user plays camment. In order to do it - implement ```CammentAudioListener``` interface and set it using method ```setCammentAudioListener(CammentAudioListener cammentAudioListener)```. Again this should be done in the activity where CammentSDK Overlay is used.
```java
public interface CammentAudioListener {

    void onCammentPlaybackStarted(); // Decrease player volume

    void onCammentPlaybackEnded(); // Restore normal volume

    void onCammentRecordingStarted(); // Mute your player here

    void onCammentRecordingEnded(); // Restore normal volume

}
```
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.<your_layout>);
    ...
    CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);
    cammentOverlay.setCammentAudioListener(this);
}
```
## DeepLinking
CammentSDK takes care of the invitation deeplinking. It specifies in its manifest ```CammentDeeplinkActivity``` which takes care of the deeplinks starting with ```camment://``` scheme.
Deeplinks are currently used to join group after an invitation. After the deeplink is opened, dialog is opened and user can decide if he wants to join the group or not. 
According to your app's activities and flow, you can decide where not to show this dialog, e.g. in the splash screen. To ignore the dialog on given ```Activity``` you just need to implement ```DeeplinkIgnore``` interface.
```java
public class SplashActivity extends AppCompatActivity 
        implements DeeplinkIgnore
```
In such case ```Activity``` (which doesn't implement ```DeeplinkIgnore``` interface)  following the ```SplashActivity``` will present the invitation dialog.

Deeplinks contain information about a show (show uuid). User should be navigated to the show no matter if he was inviter or invitee. As CammentSDK can't do this for you, implement ```OnDeeplinkOpenShowListener``` interface where it is suitable for your code and pass the listener to CammentSDK (in your Application object):
```java
public interface OnDeeplinkOpenShowListener {

    void onOpenShowWithUuid(String showUuid);

}
```
```java
public class CammentApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ...
        CammentSDK.getInstance().init(this);
        CammentSDK.getInstance().setOnDeeplinkOpenShowListener(new YourObject());
    }
}
```
```onOpenShowWithUuid``` will be called by CammentSDK when user reacts positively to any invitation request. You should open your activity with video playback for the given showUuid.
