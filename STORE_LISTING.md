# Liquid Files — Google Play & Uptodown Store Listing Kit

**App Name (Play Policy Compliant <= 30 chars, Title Case):** `Liquid Files`  
**Package ID (Permanent):** `com.aistudio.liquidfiles.vxtrnq`  
**Version:** `1.2.0` (`versionCode = 3`)  
**Min SDK:** Android 8.0 (API 26) | **Target SDK:** 36  
**Developer Email:** `antoneeroy30@gmail.com`  
**Privacy Policy HTTPS URL:** `https://antoneeroy.github.io/Liquid-file-manager/privacy-policy.html`  
*(Enable GitHub Pages in `Antoneeroy/Liquid-file-manager` → Settings → Pages → Source: `Deploy from branch`, Branch: `main`, Folder: `/docs` so this HTTPS URL serves live `docs/privacy-policy.html`.)*

---

## 1. Google Play Store Listing Copy

### Short Description (78 / 80 characters max)
```text
Fast glass file manager, media & PDF viewer, storage cleaner, and AES-256 vault.
```

### Full Description
```text
Liquid Files is a fast, modern file manager for Android 8.0+ designed to organize internal storage, removable SD cards, media collections, and encrypted private files with clarity and speed.

CORE FILE MANAGEMENT
• Browse, search, and sort files by name, date, size, or file type in list or grid view
• Copy, move, rename, favorite, share, hide/unhide, and delete files with Recycle Bin recovery
• Real-time internal storage & removable SD card usage analytics powered by Android StorageStatsManager
• Full support for Android Storage Access Framework (SAF) and Scoped Storage

BUILT-IN MEDIA, PDF & DOCUMENT VIEWERS
• Image Viewer: Open JPG, JPEG, PNG, WEBP, and GIF files with pinch-to-zoom, double-tap zoom, and gallery swiping
• Video Player: Hardware-accelerated AndroidX Media3 playback for MP4, MKV, MOV, and standard formats with fullscreen, seekbar, and volume controls
• Audio / MP3 Player: Play MP3, M4A, WAV, and AAC tracks with ID3 title/artist metadata, queue controls, and seekbar
• Document Viewers: Native Android PdfRenderer multi-page PDF reader plus fast text/code viewer & editor

AES-256-GCM SAFE VAULT & PRIVACY
• Encrypt sensitive personal files locally with AES-256-GCM wrapped by Android Keystore hardware security
• Salted PBKDF2-SHA256 4-digit PIN protection with brute-force cooldown and optional BiometricPrompt fingerprint/face unlock
• Your personal files and Safe Vault contents stay strictly on your device and are never uploaded or backed up to external servers

STORAGE OPTIMIZER & LOCAL WI-FI SHARE
• Scan and clean temporary files, caches, and empty directories
• Detect duplicate files via SHA-256 hashing and locate large files quickly
• Share accessible folders over your local Wi-Fi network (LAN) via a browser portal isolated from Safe Vault
```

---

## 2. Advertising, Content Rating & Permissions Declarations

- **App Category:** Tools / Productivity
- **Advertising Declaration (Google Play & Uptodown):** **Yes, contains ads** (Google AdMob Banner, Native, and rate-limited Interstitial ads gated behind Google UMP consent).
- **Content Rating (IARC):**
  - Violence / Sexual Content / Profanity / Gambling / Controlled Substances: **No**
  - User Interaction / Personal Info Sharing / Location Sharing by App: **No** (no social/chat or user-to-user uploads)
  - Digital Purchases: **No**
  - Expected Rating: **Everyone / PEGI 3**
- **Target Audience:** Ages 13+ / General Utility Audience (not directed at children)
- **Permissions Declaration:** `MANAGE_EXTERNAL_STORAGE` (Core File Manager — see `PLAY_STORE_COMPLIANCE.md` Section 3).

---

## 3. Uptodown Publication Summary

- **Package Artifact:** `app-release.apk` signed with permanent `upload` certificate (`my-upload-key.jks`)
- **App Title:** `Liquid Files`
- **Package Name:** `com.aistudio.liquidfiles.vxtrnq`
- **Version:** `1.2.0` (`versionCode 3`)
- **Minimum OS:** Android 8.0+
- **Developer Contact:** `antoneeroy30@gmail.com`
- **Privacy Policy:** `https://antoneeroy.github.io/Liquid-file-manager/privacy-policy.html`
- **Advertising Disclosure:** Contains Google AdMob ads (respects UMP consent; no ads inside Safe Vault or file operations).

---

## 4. App Icon & Store Graphics Checklist

- **In-App Adaptive Launcher Icon (Verified):** `res/mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml` referencing custom vector layers `res/drawable/ic_launcher_foreground.xml` and `res/drawable/ic_launcher_background.xml` (custom Liquid Glass cyan/violet folder emblem — never the default Android green icon).
- **Play Console Graphic Exports Needed on Workstation / Design Tool:**
  - `512 x 512 px` 32-bit PNG store icon (export/render from `ic_launcher`)
  - `1024 x 500 px` Feature Graphic PNG
  - `4–8` real phone screenshots (`1080 x 2400` or `9:16`) capturing:
    1. Dashboard real storage overview & categories
    2. File Explorer with search, sort & grid/list views
    3. Image Viewer / Video Player / MP3 Player
    4. PDF Viewer
    5. Safe Vault PIN / Biometric unlock
    6. Storage Optimizer & Trash
