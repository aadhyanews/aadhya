# AADHYA NEWS — తెలుగు వార్తా మొబైల్ అప్లికేషన్ (Android)

**AADHYA NEWS** (ఆధ్య న్యూస్) is a native Android application built with Kotlin, Jetpack Compose, Material 3, and Firebase Firestore & Authentication. Designed specifically for Telugu news consumers with broadcast-inspired styling, real-time breaking news ticker, Live TV streaming via AndroidX Media3 ExoPlayer, video news bulletins, Telugu category and district filters, and an administrative news publishing portal.

---

## 1. Application Highlights & Features

- **Telugu-First Identity**: Complete UI, typography, navigation, alerts, and settings in Telugu using bundled `Noto Sans Telugu` font.
- **Brand Palette**: News Red (`#D71920`), Dark Navy Blue (`#101C35`), and Gold accents.
- **Top App Bar & Live Indicator**: Brand logo, Live TV quick access with animated pulse indicator, Telugu search, and admin quick portal.
- **Red Breaking News Ticker**: Instant cycling of breaking stories with Telugu "బ్రేకింగ్" badge.
- **Featured News Carousel**: Rich hero news cards with high-definition imagery and headline overlay.
- **Live TV 24x7 Player**: Powered by AndroidX Media3 ExoPlayer with support for HLS (`.m3u8`) and MP4 video streams, live badge, and network error recovery.
- **Video News Bulletins**: Video news grid with duration stamps and in-app Media3 playback.
- **Category & District Filters**:
  - Categories: తాజా వార్తలు, బ్రేకింగ్ న్యూస్, తెలంగాణ, ఆంధ్రప్రదేశ్, హైదరాబాద్, నారాయణపేట, మహబూబ్ నగర్, కొడంగల్, రాజకీయాలు, జాతీయ వార్తలు, అంతర్జాతీయ వార్తలు, క్రైమ్, విద్య, ఉద్యోగాలు, వ్యవసాయం, వ్యాపారం, క్రీడలు, సినిమా, ఆరోగ్యం, టెక్నాలజీ.
  - Districts: నారాయణపేట, మహబూబ్ నగర్, కొడంగల్, హైదరాబాద్, రంగారెడ్డి, వరంగల్, కరీంనగర్, ఖమ్మం, నిజామాబాద్, విజయవాడ, విశాఖపట్నం, తిరుపతి.
- **Article Details**:
  - Cover photo & caption
  - Reporter attribution & publication date
  - Text-to-Speech (TTS) audio narration in Telugu
  - Font scaling (A- / A+) for readability
  - Native Android Sharesheet (WhatsApp, Telegram, etc.)
  - Bookmark / save offline
  - Related articles recommendation
- **Saved News (Bookmarks)**:
  - Local device storage for guest users
  - Instant Cloud Firestore sync for authenticated users
- **Search**: Fast keyword search across headlines, content, and reporters in Telugu.
- **Admin Publishing Portal**:
  - Secure Google Sign-In with Credential Manager
  - Create, edit, draft, publish, and delete news articles
  - Toggle breaking news and featured carousel
  - Configure Live TV stream URL and broadcast status
  - Add recorded video news entries
  - View real-time portal view analytics
  - One-tap initial database seeding

---

## 2. Architecture & Tech Stack

- **Platform**: Android 8.0+ (API 24 to 36)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material 3 (M3)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: MVVM + StateFlow + Kotlin Coroutines
- **Media Playback**: AndroidX Media3 ExoPlayer 1.5.1 (with HLS support)
- **Image Loading**: Coil Compose 2.7.0
- **Database & Auth**:
  - Cloud Firestore (Enterprise edition via Firebase BoM 34.17.0)
  - Firebase Authentication with Google Sign-In via Jetpack Credential Manager
- **Zero-Trust Security**: Hardened `firestore.rules` and `firebase-blueprint.json` schema

---

## 3. Assigning the First Administrator Securely

For production security, administrative privileges are verified at the backend database and rule levels.

### Pre-Configured Initial Administrator:
- The owner account `srisahasramedicals@gmail.com` is pre-authorized in `firestore.rules` and `AuthRepository`.
- Simply sign in with this Google account via **మరిన్ని (More) -> అడ్మిన్ నిర్వహణ -> Google తో సైన్ ఇన్ చేయండి**.
- Once signed in, the **"అడ్మిన్ డాష్‌బోర్డ్"** button will be unlocked.

### Adding Additional Administrators in Firestore:
To authorize additional reporters or editors, add a document in the `/admins` collection:
1. Open the [Firebase Console](https://console.firebase.google.com).
2. Navigate to **Firestore Database** -> `admins` collection.
3. Click **Add document**:
   - Document ID: The user's Firebase Auth `UID`.
   - Fields:
     - `email`: (string) e.g., `"reporter@aadhyanews.com"`
     - `role`: (string) `"admin"` or `"editor"`
     - `displayName`: (string) e.g., `"రమేష్ గౌడ్"`
     - `createdAt`: (timestamp) Current timestamp.

---

## 4. Local Testing & Verification

1. **JavaScript Rules Unit Tests**:
   ```bash
   FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
   ```

2. **Kotlin Robolectric Integration Tests**:
   ```bash
   gradle :app:testDebugUnitTest --tests '*Rule*'
   ```

3. **Building the APK**:
   ```bash
   gradle :app:assembleDebug
   ```
