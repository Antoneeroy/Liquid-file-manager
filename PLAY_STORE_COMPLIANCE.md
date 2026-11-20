# Liquid Files — Public Release, Google Play & Uptodown Compliance Guide

**Package ID:** `com.aistudio.liquidfiles.vxtrnq`  
**Current Version:** `versionCode = 3`, `versionName = "1.2.0"`  
**Min SDK:** 26 (Android 8.0) | **Target / Compile SDK:** 36

---

## 1. AdMob Production Configuration Checklist

Development builds default to official Google Test Ad Unit IDs so clicks/renders during development never violate AdMob policy. **Before building for Google Play or Uptodown public distribution**, supply your live AdMob IDs via environment variables or GitHub Actions Secrets:

- `ADMOB_APP_ID` (injected into `AndroidManifest.xml` `${ADMOB_APP_ID}`)
- `ADMOB_BANNER_UNIT_ID` (injected into `BuildConfig.ADMOB_BANNER_UNIT_ID`)
- `ADMOB_NATIVE_UNIT_ID` (injected into `BuildConfig.ADMOB_NATIVE_UNIT_ID`)
- `ADMOB_INTERSTITIAL_UNIT_ID` (injected into `BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID`)
- `PRIVACY_POLICY_URL` (defaults to `https://antoneeroy.github.io/Liquid-file-manager/privacy-policy.html`)

### Placement Rules Verified in Code
- Banner ads appear only at the bottom of `DashboardScreen` and `FileExplorerScreen` (automatically hidden while multi-selecting files, using clipboard paste bars, or opening context menus).
- Native ad card appears only below analyzer/cleaner cards in `StorageOptimizerScreen` (hidden until loaded—zero blank placeholder boxes).
- Interstitial ads are preloaded only after UMP consent, rate-limited with a 3-minute minimum cooldown, and shown strictly when exiting Storage Optimizer—never inside Safe Vault or during file operations.
- **Safe Vault (`SafeVaultScreen`) and all file/media viewers contain ZERO ads.**

---

## 2. Google Play Data Safety Form Answers

Use these exact responses in **Play Console → App content → Data safety**:

| Question | Answer | Notes |
|---|---|---|
| Does your app collect or share any of the required user data types? | **Yes** | Via embedded **Google Mobile Ads SDK (AdMob)** & **UMP**; user files/Vault data are never collected or shared. |
| Is all user data collected by your app encrypted in transit? | **Yes** | Google Mobile Ads / UMP network requests use HTTPS/TLS. |
| Do you provide a way for users to request that their data is deleted? | **Optional / No user account** | No user accounts exist; local app data/Vault are deleted on uninstall or in-app Trash/Restore. |

### Exact Data Types Collected by Third-Party SDK (Google Mobile Ads)
Check **Collected** (and **Ephemeral: No**; **User can choose whether data is collected: Yes via UMP / OS Ad ID opt-out**, **Shared: May be marked Yes under Advertising or marketing depending on Google Ads mediation settings**):
1. **Location → Approximate location:** Collected (Ad targeting, Analytics, Fraud prevention).
2. **App activity → App interactions:** Collected (Analytics, Advertising or marketing).
3. **App info and performance → Crash logs & Diagnostics:** Collected (Analytics, Fraud prevention, App functionality).
4. **Device or other IDs → Advertising ID / Device ID:** Collected (Advertising or marketing, Analytics, Fraud prevention).

> **Important:** User personal files, photos, videos, audio, documents, PIN hashes, and encrypted Vault items are processed **strictly on-device** and are **NOT** collected or shared.

---

## 3. `MANAGE_EXTERNAL_STORAGE` (All Files Access) Declaration

Because Liquid Files declares `android.permission.MANAGE_EXTERNAL_STORAGE` for its core file-management workflow, **Play Console → App content → App access / All files access** requires explicit justification:

### Core Functionality Classification
- **Selected Category:** **File management** (Managing files across device storage and removable SD cards, including non-media documents, archives, downloads, bulk copy/move/rename/delete, duplicate scanning, large-file analysis, and local AES-256-GCM encryption in Safe Vault).

### Reviewer Walkthrough Video Checklist
Record a short (30–60 second) unedited screen recording on Android 11+ demonstrating:
1. Launching **Liquid Files** and tapping the **Grant All Files Access** banner/prompt leading to Android Settings (`ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`).
2. Browsing `/storage/emulated/0` folders containing mixed non-media files (PDF, TXT, ZIP, APK) and media folders.
3. Creating a folder, copying/moving/renaming a file, or analyzing storage / encrypting a file in Safe Vault.
4. Showing that denying All Files Access gracefully falls back to Scoped Media / Storage Access Framework (`Pick File`) without crashing.

---

## 4. Permanent Release Signing & Dual-Store Consistency (Google Play + Uptodown)

1. **Keystore Identity:**
   - Upload/signing keystore resides at `my-upload-key.jks` (`keyAlias = "upload"`).
   - **Never commit** `my-upload-key.jks`, `.env`, or plaintext passwords to Git.
   - `/.gitignore` already excludes `*.jks`, `*.keystore` (except debug templates if tracked separately), `.env`, and signing secrets.
2. **Google Play App Signing:**
   - Enable **Play App Signing** in Play Console.
   - If you let Google Play manage a separate App Signing Key while using `my-upload-key.jks` as the **Play Upload Key**, note that:
     - **Google Play** distributes APKs signed with Google's **App Signing Key**.
     - **Uptodown** distributes your directly signed `app-release.apk`.
     - If you want users to be able to update interchangeably between **Uptodown** and **Google Play** with the *exact same certificate*, export/enroll the same certificate in Play App Signing (using PEPK tool during Play App Signing setup) or keep Uptodown and Play channels separate while preserving each channel's permanent key across every update.
3. **Every Future Release Checklist:**
   - Keep `applicationId = "com.aistudio.liquidfiles.vxtrnq"` unchanged forever.
   - Increment `versionCode` by at least `+1` (currently `3`) and bump `versionName` (currently `"1.2.0"`).
   - Never use `RoomDatabase.Builder.fallbackToDestructiveMigration()`—always provide explicit `Migration(old, new)` steps if schema changes.

---

## 5. Pre-Submission Physical Device QA Checklist

Before uploading the AAB to Google Play Production or APK to Uptodown, run this manual smoke checklist on real Android 8.0, Android 11/12, and Android 14/15/16 hardware:
- [ ] First launch → Grant / Deny runtime permissions → Verify `"Pick File"` (SAF) and Scoped Storage work smoothly when `MANAGE_EXTERNAL_STORAGE` is denied.
- [ ] Image Viewer: open JPG, PNG, WEBP, GIF, and `content://` SAF image; verify pinch-to-zoom, double-tap zoom, and horizontal swipe.
- [ ] Video Player: play MP4/MKV/MOV + test corrupted/unsupported file error card + fullscreen & volume controls.
- [ ] Audio Player: play MP3/M4A/WAV/AAC + verify ID3 title/artist, seekbar, Next/Prev track buttons, and background continuity.
- [ ] PDF Viewer & Text Viewer: open multi-page PDF and `.txt`/`.json` files; save edited `.txt` file.
- [ ] Safe Vault: first-time 4-digit PIN setup, recovery passphrase setup, biometric unlock, 10-attempt lockout cooldown, move file to Vault & restore file back cleanly.
- [ ] Upgrade test: `adb install -r app-release.apk` over previous version and confirm database Favorites, Trash, and Safe Vault items remain intact.
- [ ] AdMob & UMP: verify test/consent flow in EEA-simulated settings, `"Manage Ad Privacy Choices"` in Settings, and verify zero ads appear in Safe Vault.
