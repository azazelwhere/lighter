# Lighter Browser

Lightweight anonymous browser for Android 10+ with **complete device identity spoofing**.

Built with Kotlin + Jetpack Compose, Android System WebView, and an AOSP Holo dark UI.

---

## Key Features

### Identity Spoofing (Core)
All spoofing is applied via JavaScript override that runs *before* page scripts,
using `Object.defineProperty` with frozen descriptors. Detection scripts cannot
re-assign or read the real underlying value.

| Field                  | What we spoof                                                |
|------------------------|--------------------------------------------------------------|
| `navigator.userAgent`  | Real UA string (Chrome / Safari / Firefox / custom)          |
| `navigator.platform`   | `Linux armv8l` / `Win32` / `MacIntel` / `iPhone` / `Linux x86_64` |
| `navigator.vendor`     | `Google Inc.` / `Apple Computer, Inc.` / empty               |
| `navigator.language`   | `en-US` / any custom                                         |
| `navigator.languages`  | Array of languages                                           |
| `navigator.hardwareConcurrency` | CPU core count (4/6/8/12/16)                       |
| `navigator.deviceMemory` | Device RAM in GB (4/8/16)                                  |
| `navigator.plugins`    | Fake PluginArray                                             |
| `screen.width/height`  | Full screen resolution                                       |
| `screen.colorDepth`    | 24/30/48 bit                                                 |
| `window.devicePixelRatio` | DPR (1.0 / 2.0 / 2.625 / 3.0)                            |
| `window.outerWidth/Height` | Spoofed to match screen                                   |
| `Intl.DateTimeFormat`  | Always returns spoofed timezone                              |
| `Date.getTimezoneOffset` | Spoofed offset (minutes from UTC)                         |
| WebGL `getParameter`   | UNMASKED_VENDOR_WEBGL + UNMASKED_RENDERER_WEBGL spoofed      |
| `HTMLCanvasElement.toDataURL/toBlob` | Tiny per-pixel noise defeats canvas fingerprint |
| `navigator.getBattery` | Returns spoofed level / charging / chargingTime              |
| Device sensors         | All sensor constructors replaced with throwing fakes         |
| `document.fonts.check` | Lies about installed font families                           |
| `navigator.connection` | Spoofed to `4g / 50ms / 10Mbps`                              |
| `navigator.doNotTrack` | Always `1`                                                   |

### Built-in Profiles
- Android 13 / Pixel 7 (default)
- Windows 11 / Chrome 120
- macOS 14 / Safari 17
- iOS 17 / iPhone 15 Safari
- Linux / Firefox 121
- **Random rotation** - new randomized identity per page request
- Custom profiles - create, edit, save, export/import (JSON)

### Privacy & Anonymity
- **Ad & tracker blocker** - bundled StevenBlack hosts list (~70 entries),
  swappable for full EasyList. O(1) lookup, applied in `shouldInterceptRequest`.
- **DNS over HTTPS** - Cloudflare / Google / Quad9 / AdGuard / NextDNS / Mullvad
- **Proxy support** - HTTP and SOCKS5 (system-level via JVM properties)
- **Tor via Orbot** - send START_TOR intent to Orbot, route WebView via 127.0.0.1:9050
- **Clear data on exit** - WebView cache, cookies, WebStorage, form data, history DB
- **Do Not Track** header always sent
- **Mixed content blocked** - never load HTTP resources on HTTPS pages
- **Third-party cookies blocked** globally
- **JavaScript alert/confirm/prompt blocked** (anti-fingerprint)

### Browser Features
- Multi-tab (regular + incognito)
- Bookmarks manager (Room database)
- Browsing history (Room database, with search & time-based cleanup)
- Built-in download manager (system DownloadManager + WorkManager fallback for spoofed UA)
- JS / Cookie / Image / DOM storage toggles (per-app)
- Full-page screenshot
- Reader mode (strips images, inverts colors)
- URL bar with search-or-URL resolution
- AOSP Holo UI (dark blue + cyan accent, like the old stock Android Browser)

---

## Project Structure

```
LighterBrowser/
├── build.gradle.kts                  # Root Gradle config
├── settings.gradle.kts
├── gradle.properties
├── build.sh                          # CLI build helper (downloads wrapper jar if missing)
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   └── gradle-wrapper.properties     # Gradle 8.5
└── app/
    ├── build.gradle.kts              # App module: Compose, Room, OkHttp, WorkManager
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                       # AOSP Holo colors, strings, themes, vector icons
        └── java/com/lighter/browser/
            ├── LighterApp.kt          # Manual DI container
            ├── MainActivity.kt        # NavHost, Orbot receiver, lifecycle
            ├── data/                  # Room entities, DAOs, AppDatabase, Prefs (DataStore)
            ├── spoofing/              # ProfileManager, ProfilePresets, JsInjector, SpoofingEngine
            ├── privacy/               # AdBlocker, DnsOverHttpsResolver, ProxyManager, TorManager, DataCleaner
            ├── browser/               # TabManager, WebViewClients, BrowserDownloadManager, DownloadService
            ├── ui/
            │   ├── theme/             # Holo color scheme + typography
            │   ├── components/        # UrlBar, TopToolbar, BottomBar, ProgressBar
            │   └── screens/           # Browser, Tabs, Bookmarks, History, Settings, Spoofing
            └── util/
```

---

## Building

### Option A: Android Studio (recommended)
1. Open Android Studio → **Open** → select the `LighterBrowser` folder.
2. Wait for Gradle sync to complete (downloads Gradle 8.5 + AGP 8.2.2).
3. Connect an Android 10+ device with USB debugging enabled.
4. Click **Run** (or Build → Build APK).

### Option B: CLI
```bash
export ANDROID_HOME=$HOME/Android/Sdk   # adjust path
cd LighterBrowser
./build.sh
# APK: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If `gradle-wrapper.jar` is missing (it is a binary file we cannot ship in a text bundle),
`build.sh` downloads it automatically from the Gradle GitHub repo.

### Requirements
- JDK 17
- Android SDK Platform 34 + Build Tools 34
- Kotlin 1.9.22 (auto-downloaded by Gradle)
- ~500 MB disk for Gradle cache + dependencies

---

## Using the Spoofing Features

1. Open the app → tap the **⋮** menu → **Spoofing identitas**.
2. Pick one of the 6 built-in profiles, or tap **+** to create a custom one.
3. (Optional) Pick **Acak** to rotate fingerprint per page request.
4. Tap **Terapkan ke semua tab** to reload all open tabs with the new identity.
5. Verify the spoofing:
   - Visit `https://browserleaks.com/javascript` to check UA / platform / screen / TZ.
   - Visit `https://browserleaks.com/canvas` to check canvas fingerprint noise.
   - Visit `https://browserleaks.com/webgl` to check GPU vendor/renderer.
   - Visit `https://coveryourtracks.eff.org` for a full fingerprint audit.

### Custom Profile Tips
- The UA, platform, vendor, screen size, and WebGL renderer should be **mutually
  consistent** — e.g. don't pair an iPhone UA with a `Win32` platform or with a
  `NVIDIA RTX` GPU, or detectors will flag you.
- Keep `canvasNoise` between `0.0001` and `0.001` — too high breaks legitimate
  Canvas usage, too low leaves the fingerprint intact.
- Blocking the Battery API entirely (`blockBattery = true`) is safer than spoofing
  it — fingerprinters expect *some* battery object to exist on mobile.

---

## Privacy Setup (for maximum anonymity)

1. Settings → **Tor (via Orbot)** → install Orbot → enable toggle → tap **Start Orbot**.
   Wait for status to become `ON`. All WebView traffic now routes via 127.0.0.1:9050.
2. Settings → **DNS over HTTPS** → enable → pick `AdGuard Family` or `Quad9`.
3. Settings → **Ad & Tracker Blocker** → enable.
4. Settings → **Bersihkan data saat keluar** → enable.
5. Settings → **Cookies** → disable (or use only Incognito tabs).
6. Spoofing → **Acak (rotasi per request)** profile.
7. Spoofing → block sensors + block battery API.

With all of the above active, your real device fingerprint is never exposed.

---

## Limitations & Known Issues

- **System WebView fingerprint**: Lighter uses the user's installed System WebView
  (Chromium). Sites that probe for Chromium-specific quirks (e.g. `chrome.runtime`,
  `chrome.csi`) can still detect that you're running a Chromium-based engine. For
  stronger engine spoofing, you would need GeckoView (Firefox) or a patched
  WebView like Bromite.
- **HTTP/3 (QUIC)**: WebView uses QUIC by default. QUIC bypasses the system proxy
  in some Android versions. If you rely on Tor or SOCKS5, disable QUIC in
  WebView flags (`chrome://flags/#enable-quic`) — not exposed in the UI yet.
- **Canvas noise on DRM content**: Spoofed canvas breaks some video sites that
  use Canvas for DRM. Disable canvas noise in your profile if video doesn't play.
- **WebView proxy**: SOCKS5 support requires Android 10+. On older versions,
  only HTTP proxy works through WebView.
- **Tor without Orbot**: Lighter does not ship its own Tor binary. Orbot must be
  installed separately.
- **Gradle wrapper jar**: Binary file not included in text bundle. Run `build.sh`
  once to auto-download it.

---

## License

MIT-style - use it, fork it, modify it. No warranty.
Built for privacy research and anonymous browsing.

---

## Roadmap (ideas for v1.1)

- Per-site spoofing profiles (auto-switch based on URL).
- Full EasyList download + auto-update.
- MediaStore-based screenshot save.
- In-page find UI.
- Reader mode with content extraction (not just CSS inversion).
- Cookie jar per profile (each profile has its own cookie store).
- HTTP/3 disable toggle in Settings.
- Bookmark folders + import from HTML.
- History auto-purge after N days.

Pull requests welcome.
