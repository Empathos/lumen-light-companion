# Agent 1 Draft Packet: Android Build Scaffold

## Scope

Prepare a conventional native Android build scaffold for Lumen Light Companion:
root Gradle configuration, Android app module, Kotlin/Compose plugin setup, and
a public-safe manifest. This packet intentionally avoids runtime pairing,
transport, media capture, signing, deployment, and private configuration.

## Proposed Files

```text
settings.gradle.kts
build.gradle.kts
gradle.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/org/lumen/lightcompanion/MainActivity.kt
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
```

`MainActivity.kt`, `strings.xml`, and `themes.xml` are included only because a
debug build will need a minimal launch target. Agent 2 can replace the activity
body with the actual Compose shell.

## Content Outline

### `settings.gradle.kts`

- Set `pluginManagement` and `dependencyResolutionManagement`.
- Use `google()`, `mavenCentral()`, and `gradlePluginPortal()`.
- Set `rootProject.name = "LumenLightCompanion"`.
- Include only `:app`.

### Root `build.gradle.kts`

- Declare Android application, Kotlin Android, and Compose compiler plugins with
  `apply false`.
- Keep versions centralized in the plugin declarations for the initial scaffold.
- Do not add publication, signing, release, or deployment configuration.

Suggested plugin shape:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

### `gradle.properties`

- Enable AndroidX.
- Enable Kotlin official code style.
- Use non-private, generic build settings only.

Suggested content:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
```

### `app/build.gradle.kts`

- Apply `com.android.application`, `org.jetbrains.kotlin.android`, and
  `org.jetbrains.kotlin.plugin.compose`.
- Use public-safe package/application ID:
  `org.lumen.lightcompanion`.
- Set a conservative Android baseline:
  - `namespace = "org.lumen.lightcompanion"`
  - `compileSdk = 35`
  - `minSdk = 26`
  - `targetSdk = 35`
- Enable Compose through `buildFeatures { compose = true }`.
- Add only local UI dependencies needed for a minimal Compose app:
  `androidx.activity:activity-compose`, Compose BOM, Material 3, UI tooling, and
  AndroidX test runner/JUnit defaults.
- Avoid network, camera, microphone, haptic, QR, WebSocket, WebRTC, or
  serialization dependencies in this packet.
- Do not define signing configs or release deployment settings.

Dependency outline:

```kotlin
dependencies {
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### `app/src/main/AndroidManifest.xml`

- Declare a single exported launcher activity.
- Do not request permissions yet.
- Do not add host URLs, deep links, network security config, device metadata, or
  private identifiers.

Suggested content:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.LumenLightCompanion">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Integrator note: either add default launcher icon resources or remove
`android:icon` until generated icons exist. The latter is smaller for the first
scaffold.

### Minimal Launch Stub

`MainActivity.kt` can contain a temporary Compose surface with generic status
text only:

```kotlin
package org.lumen.lightcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Text("Lumen Light Companion")
                }
            }
        }
    }
}
```

Agent 2 should replace this with the operational app shell.

## Acceptance Criteria

- The repo has a conventional single-module Android project structure.
- The application ID and namespace are generic and public-safe:
  `org.lumen.lightcompanion`.
- `./gradlew :app:assembleDebug` can be attempted on a machine with a compatible
  Android SDK/JDK.
- Build files do not contain private hosts, IP addresses, credentials, signing
  configs, deployment configuration, session IDs, device IDs, transcripts, logs,
  or screenshots.
- The manifest requests no microphone, camera, network, vibration, notification,
  or storage permissions in this packet.
- Android-specific decisions remain scaffold-level and do not define live host
  protocol behavior.

## Verification Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
python3 scripts/validate_companion_event.py examples/companion-event.example.json
rg -n "http://|https://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|password|secret|token|credential|session_[A-Za-z0-9_-]+|device_[A-Za-z0-9_-]+" .
```

Expected schema validation output remains:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## Dependencies

- Android Gradle Plugin `8.7.3`
- Kotlin `2.0.21`
- Kotlin Compose compiler plugin `2.0.21`
- Compose BOM `2024.10.01`
- AndroidX Activity Compose `1.9.3`
- Material 3 via Compose BOM
- JDK compatible with Android Gradle Plugin 8.x
- Local Android SDK with API 35 installed

These versions are intentionally ordinary, current-enough scaffold defaults. The
integrator may bump them if the local Android toolchain already standardizes on
newer versions.

## Risks And Open Questions

- Local verification may fail if Android SDK API 35 or the required JDK is not
  installed.
- Compose/Kotlin/AGP version alignment should be checked during integration.
- The roadmap says Android implementation follows protocol stabilization, while
  `docs/AGENT_PACKET_PLAN.md` explicitly authorizes this scaffold packet. Keep
  the implementation limited to build runway until contract/model agents land.
- Adding permissions now would imply product behavior not owned by this packet;
  microphone, camera, vibration, network, and QR/pairing permissions should be
  added only with their dedicated packets.
- The launcher icon decision should be resolved by the integrator: omit the icon
  initially or add generated default resources.
