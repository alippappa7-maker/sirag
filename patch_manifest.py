import sys

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

permissions = """    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />"""

content = content.replace('    <uses-permission android:name="android.permission.VIBRATE" />', permissions)

service_decl = """        <service
            android:name=".core.audio.SirajAudioService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService"/>
            </intent-filter>
        </service>
    </application>"""

content = content.replace('    </application>', service_decl)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
