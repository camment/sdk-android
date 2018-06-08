# CammentSDK for Android
**current version: 3.0.0**

To get started with the Camment Mobile SDK for Android you can set up the SDK and build a new project, or you can integrate the SDK into your existing project. 

The instructions were written for the following configuration:
- Android Studio 3.1.1
- Java 1.8.0_144
- Gradle 3.1.1 (distribution gradle-4.6-all.zip)

## Technical specification
**SDK version**
SDK is built with the following configuration:
```gradle
minSdkVersion 19
targetSdkVersion 27
compileSdkVersion 27
buildToolsVersion "27.0.3"
supportLibVersion "27.1.0"
```
*Note:* If your application supports also lower SDK versions, you have to handle enabling/disabling of the CammentSDK by yourself.

**Dependencies**
CammentSDK relies on following dependencies: 
- Amazon AWS SDK (v2.6.18)
- Facebook SDK (v4.32.0) (part of CammentAuth)
- Google Exoplayer (v2.7.3)
- Glide library (v4.7.1)
- EasyPermissions library (v1.2.0)
- Greenrobot EventBus (v3.0.0)
- Android Support v4 (v27.1.0)
- Android Support Design Library (v27.1.0)
- Android Support RecyclerView (v27.1.0)
- Android Support ConstraintLayout (v1.1.0)

*Note:* If you use some of these dependencies in your application too, you can remove them from your app gradle file. In case you want to override some dependencies, you can do it using gradle, e.g.:
```gradle
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        def requested = details.requested
        if (requested.group == 'com.android.support') {
            if (!requested.name.startsWith("multidex")) {
                details.useVersion '26.0.2'
            }
        }
    }
}
```
## Add CammentSDK to your project
SDK is available on the github in a maven structure, containing 4 important files: 
- CammentSDK:
    - ```cammentsdk-<sdk_version>.aar```
    - ```cammentsdk-<sdk_version>.pom```
- CammentAuth:
    - ```cammentauth-<sdk_version>.aar```
    - ```cammentauth-<sdk_version>.pom```

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
Add following dependencies into your **application level** ```build.gradle``` file:
```gradle
dependencies {
    ... //your other dependencies
    compile ('tv.camment.cammentsdk:cammentsdk:<sdk_version>@aar') {
        transitive true
    }
    compile ('tv.camment.cammentauth:cammentauth:<sdk_version>@aar') {
        transitive true
    }
}
```
*Note*: ```transitive true``` means that gradle will download also the SDK dependencies

Now **sync the project with your gradle files** and **clean the project**. 

## CammentAuth - Facebook SDK
CammentSDK requires authentication via **Facebook SDK**. Currently it is the only option but it's planned to support more options in the future.
> If you don't have Facebook SDK set up in your project, you can find instructions how to create and setup Facebook Application here:
https://developers.facebook.com/docs/android/getting-started/

Don't forget to add following into the ```AndroidManifest.xml``` (more into at https://developers.facebook.com/docs/facebook-login/android):
```xml
<application ...>
    ...
    <meta-data
        android:name="com.facebook.sdk.ApplicationId"
        android:value="@string/facebook_app_id" />
    
    <activity
        android:name="com.facebook.FacebookActivity"
        android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
        android:label="@string/app_name" />
        
    <activity
        android:name="com.facebook.CustomTabActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data android:scheme="@string/fb_login_protocol_scheme" /> 
        </intent-filter>
    </activity>
    ...
</application>
```
## CammentAuthIdentityProvider
Generally, CammentSDK handles own login/logout (if not logged in, login is called by trying to invite somebody; logout can be called from the side-panel). 

If you have already Facebook login/logout functionality on some of your existing activities, call ```void notifyLogoutSuccessful()``` explicitly to notify CammentSDK. This way CammentSDK can cleanup data and refresh its UI properly.
```java
CammentSDK.getInstance().getAppAuthIdentityProvider().notifyLogoutSuccessful(); // call when Facebook logout was successful
```

If you want to be notified about Facebook login/logout performed by CammentSDK, register listener ```CammentAuthListener```:
```java
CammentSDK.getInstance().getAppAuthIdentityProvider().addCammentAuthListener(new CammentAuthListener() {
    @Override
    public void onLoggedIn(CammentAuthInfo cammentAuthInfo) {
        // perform your code after login
    }

    @Override
    public void onLoggedOut() {
        // perform your code after logout
    }
});
```
## Initialize CammentSDK
Open your application class extending ```Application``` and add following into the ```onCreate()``` method:
```java
public class YourApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ...
        CammentSDK.getInstance().init(this, new FbAuthIdentityProvider()); // CammentSDK initialization
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
## Camment audio recording volume
As Android decreases volume of recorded video in order to compensate sound of microphone membrane, you may notice that the camments recorded on Android device are significantly more quiet than the camments recorded on iOS device.
You can decide whether you want to artificially increase camment sound recording or not. Test by yourself which setting is the best for you as by increasing the recording sound it may result in the increased volume of other sounds too, e.g. microphone membrane.
To do so use method ```setCammentAudioVolumeAdjustment(CammentAudioVolume cammentAudioVolume)```.
```java
public class YourApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ...
        CammentSDK.getInstance().init(this);
        
        CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.NO_ADJUSTMENT);
    }
}
```
Camment audio volume adjustment levels are (default value is ```NO_ADJUSTMENT```):
```java
public enum CammentAudioVolume {

    NO_ADJUSTMENT, // original Android recording audio level is used
    MILD_ADJUSTMENT, // original Android recording audio level is increased slightly
    FULL_ADJUSTMENT // original Android recording audio level is increased significantly

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


CammentSDK also needs to know unique identifier of the currently watched show. Use ```setShowMetadata(ShowMetadata showMetadata)``` method. The first parameter of ```ShowMetadata``` object is show uuid and the second one is custom invitation text which will be passed together with invitation deeplink (can be null or empty String, in such case default invitation text is used). 
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.<your_layout>);
    ...
    CammentSDK.getInstance().setShowMetadata(new ShowMetadata("<Any unique String identifier of your show>", "<Any custom invitation text>"));
        
    FrameLayout parentViewGroup = (FrameLayout) findViewById(R.id.fl_parent);
    CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);
    cammentOverlay.setParentViewGroup(parentViewGroup);
}
```

Bottom margin of the camment recording button can be increased if needed. Use ```setRecordButtonMarginBottom(int bottomMarginInDp)``` method of CammentOverlay class to add additional bottom margin (in dp unit). It'll be added to the predefined minimum bottom margin.
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.<your_layout>);
    ...
        
    CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);
    cammentOverlay.setRecordButtonMarginBottom(16);
    ...
}
```

## Pass onActivityResult and onRequestPermissionsResult to CammentSDK
CammentSDK handles permissions which it needs as well as Facebook Login. In order to complete the flow correctly, pass results of ```onActivityResult``` and ```onRequestPermissionsResult``` to CammentSDK. 

```onActivityResult``` should be overridden in **all activities (use your BaseActivity or similar parent activity)** where CammentSDK is used as Facebook Login may be performed e.g. when invitation request is received and there has to be an activity ready to receive onActivityResult from Facebook SDK. 
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ...
    CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
}
```
```onRequestPermissionsResult``` should be overridden in the **activity where CammentOverlay is used**.
```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    ...
    CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```
## React to Camment interactions 
For better user experience we recommend muting a video player when a user starts recording camment and decrease the volume at least by half when a user plays camment. In order to do it - implement ```CammentAudioListener``` interface and set it using method ```setCammentAudioListener(CammentAudioListener cammentAudioListener)```. Again this should be done in the activity where CammentSDK Overlay is used.

```java
public interface CammentAudioListener {

    void onCammentPlaybackStarted(); // Decrease player volume

    void onCammentPlaybackEnded(); // Restore normal volume

    void onCammentRecordingStarted(); // Decrease player volume (almost mute)

    void onCammentRecordingEnded(); // Restore normal volume

}
```
> Video Player volume is handled by the host application. Make sure that correct audio volume is used to ensure good user experience, e.g. "onCammentPlaybackStarted" decreases volume to 60% and "onCammentRecordingStarted" decreases volume to 30% of original value.
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
## Enable video stream syncing in groups
In order to support syncing of position in video stream with host user, implement ```CammentPlayerListener``` interface and set it using method ```setCammentPlayerListener(CammentPlayerListener cammentPlayerListener)``` of CammentSDK class.
```java
public interface CammentPlayerListener {

    int getCurrentPosition(); //return current position of video stream player in milliseconds (i.e. 1s = 1000ms)

    boolean isPlaying(); //return true if player is currently playing the stream (false if i.e. paused, stopped)

    void onSyncPosition(int currentPosition, boolean isPlaying); //the method will be called by CammentSDK when position should be adjusted - use "currentPosition" value to seek to given position and "isPlaying" to play(true)/pause(false) video stream

}
``` 
Example of implementation:
```java
CammentSDK.getInstance().setCammentPlayerListener(new CammentPlayerListener() {
            @Override
            public int getCurrentPosition() {
                return videoView != null ? videoView.getCurrentPosition() : 0;
            }

            @Override
            public boolean isPlaying() {
                return videoView != null && videoView.isPlaying();
            }

            @Override
            public void onSyncPosition(int currentPosition, boolean isPlaying) {
                if (videoView != null) {
                    videoView.seekTo(currentPosition);

                    if (videoView.isPlaying() && !isPlaying) {
                        videoView.pause();
                    } else if (!videoView.isPlaying() && isPlaying) {
                        videoView.start();
                    }
                }
            }
        });
```

Notify CammentSDK about your player actions using CammentSDK class methods ```onPlaybackStarted(int currentPositionMillis)```, ```onPlaybackPaused(int currentPositionMillis)``` and ```onPlaybackPositionChanged(int currentPositionMillis, boolean isPlaying)```. 
Example of usage:
```java
public class MyVideoView extends VideoView {

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void start() {
        super.start();

        CammentSDK.getInstance().onPlaybackStarted(getCurrentPosition());
    }

    @Override
    public void pause() {
        super.pause();

        CammentSDK.getInstance().onPlaybackPaused(getCurrentPosition());
    }

    @Override
    public void seekTo(int msec) {
        super.seekTo(msec);

        CammentSDK.getInstance().onPlaybackPositionChanged(getCurrentPosition(), isPlaying());
    }
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
public class YourApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ...
        CammentSDK.getInstance().init(this);
        
        CammentSDK.getInstance().setOnDeeplinkOpenShowListener(new OnDeeplinkOpenShowListener() {
            @Override
            public void onOpenShowWithUuid(String showUuid) {
                // your code to open Activity (where CammentOverlay is added) with given showUuid
            }
        });
    }
}
```
```onOpenShowWithUuid``` will be called by CammentSDK, e.g. when user opens an invitation to join a chat group. You should open your activity with video playback for the given showUuid.
