# CammentSDK for Android
**current version: 2.1.8**

To get started with the Camment Mobile SDK for Android you can set up the SDK and build a new project, or you can integrate the SDK into your existing project. 

The instructions were written for the following configuration:
- Android Studio 3.1.1
- Java 1.8.0_144
- Gradle 2.3.3 (distribution gradle-3.5-all.zip)

## Technical specification
**SDK version**
SDK is built with the following configuration:
```gradle
minSdkVersion 19
targetSdkVersion 26
compileSdkVersion 26
buildToolsVersion "26.0.2"
supportLibVersion "26.0.2"
```
*Note:* If your application supports also lower SDK versions, you have to handle enabling/disabling of the CammentSDK by yourself.

**Dependencies**
CammentSDK relies on following dependencies: 
- Amazon AWS SDK (v2.6.13)
- Facebook SDK (v4.25.0) (part of CammentAuth)
- Google Exoplayer (v2.7.3)
- Glide library (v4.3.0)
- EasyPermissions library (v0.4.2)
- Greenrobot EventBus (v3.0.0)
- Android Support v4 (v26.0.2)
- Android Support Design Library (v26.0.2)
- Android Support RecyclerView (v26.0.2)
- Android Support ConstraintLayout (v1.0.2)
- DanielMartinus Konfetti (v1.1.0)
- Sannies Mp4Parser (v1.1.22)

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
            url 'https://raw.githubusercontent.com/camment/sdk-android/rtve/sdk/'
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
*Note*: ```<sdk_version>``` is currently **2.1.8**
*Note*: ```transitive true``` means that gradle will download also the SDK dependencies

Now **sync the project with your gradle files** and **clean the project**. 

## CammentAuth - Facebook SDK
CammentSDK requires authentication via **Facebook SDK**. CammentSDK provides the functionality for you with our Facebook App Id.
Nevertheless you have to provide us **development and release key hashes**. How to retrieve them is described in official FB documentation in sections: “Running Sample Apps”, “Create a Development Key Hash”, “Setting a Release Key Hash” (https://developers.facebook.com/docs/android/getting-started#samples, https://developers.facebook.com/docs/android/getting-started#create_hash)

## Pass onActivityResult to CammentSDK
CammentSDK handles Facebook Login. In order to complete the flow correctly, pass results of ```onActivityResult``` to CammentSDK. 

```onActivityResult``` should be overridden in **ALL activities (use your BaseActivity or similar parent activity)** where CammentSDK is used as Facebook Login may be performed e.g. when invitation request is received and there has to be an activity ready to receive onActivityResult from Facebook SDK. To avoid mistakes, pass it to CammentSDK from all your activities.
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ...
    CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
}
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
2. Specify ```ContentProvider``` used by CammentSDK as it has to have unique authority (copy, don't change anything):
```xml
<application
    ...>
    ...
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
3. Specify ```FileProvider``` used by CammentSDK to enable sharing of internal video files (copy, don't change anything):
```xml
<application
    ...>
    ...
    <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.cammentsdk.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">

            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/cmmsdk_file_provider_paths" />

    </provider>
</application>            
```

## (OPTIONAL) Camment audio recording volume
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
Camment audio volume adjustment levels are (**default value is ```NO_ADJUSTMENT```**):
```java
public enum CammentAudioVolume {

    NO_ADJUSTMENT, // original Android recording audio level is used
    MILD_ADJUSTMENT, // original Android recording audio level is increased slightly
    FULL_ADJUSTMENT // original Android recording audio level is increased significantly

}
```
## Opening Activity with list of karaoke videos
Call ```EurovisionShowsActivity.start(Context context)``` to open activity from your code. Opening of the player (```EurovisionPlayerActivity```) and karaoke functionality is handled by CammentSDK so you're not required to do any further actions.

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
                handleOpenShowWithUuid(showUuid);
            }
        });
    }
    
    private void handleOpenShowWithUuid(String showUuid) {
        // EurovisionShowsActivity will handle opening activity with karaoke player too
        EurovisionShowsActivity.startFromDeeplink(this, showUuid); 
    }
}
```
```onOpenShowWithUuid``` will be called by CammentSDK, e.g. when user opens an invitation to join a chat group. You should open your activity with video playback for the given showUuid.
