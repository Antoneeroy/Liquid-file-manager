# Privacy Policy for Liquid Files

**Effective Date:** 2026-01-01  
**Application Name:** Liquid Files  
**Package ID:** `com.aistudio.liquidfiles.vxtrnq`  
**Minimum Supported OS:** Android 8.0 (API 26)

---

## 1. Overview

Liquid Files is a local file manager, media viewer, storage analyzer, and encrypted file vault for Android. Your personal files, folders, media, and Safe Vault contents stay on your device. Liquid Files does not operate user-file cloud storage servers and never uploads your personal files or Vault contents.

---

## 2. Information Processed on Your Device

### 2.1 Files, Folders & Storage Metadata (On-Device Only)
- Liquid Files accesses local directories, removable SD cards, MediaStore collections (Images, Videos, Audio), and files you explicitly select via the Android Storage Access Framework (SAF) solely to display, copy, move, rename, share, inspect, optimize, or delete files at your direction.
- Favorites, Trash metadata, and Vault file records are stored locally in the app's private SQLite Room database and private internal storage (`allowBackup="false"`).
- **We never upload, sell, or transmit your personal files or folder listings.**

### 2.2 Safe Vault Encryption & Biometric Authentication
- Files moved to **Safe Vault** are encrypted locally using **AES-256-GCM** streaming encryption.
- The Vault encryption key is generated locally with a cryptographically secure random number generator and protected via **Android Keystore** hardware-backed wrapping when supported by your device.
- Your Vault PIN and optional personal recovery answer are stored locally only as salted **PBKDF2-HMAC-SHA256** cryptographic hashes—plaintext PINs are never saved or transmitted.
- Optional fingerprint/face unlock uses Android's official **BiometricPrompt** system API. Biometric templates never leave your device's secure hardware and are never accessible to Liquid Files.
- Safe Vault, database, and shared preference files are excluded from Android cloud and device backups (`android:allowBackup="false"`).

### 2.3 Local Wi-Fi File Portal
- When you manually enable **Wi-Fi Share**, Liquid Files starts a temporary HTTP server (`LocalWebServer`) strictly on your local Wi-Fi network (LAN) so devices connected to the same router can browse and download shared storage folders.
- Access to private Safe Vault, Trash, databases, and preference directories is blocked at the path-resolution layer. No data is routed through external relay servers.

---

## 3. Third-Party Advertising & Consent SDKs

Liquid Files integrates the official **Google Mobile Ads SDK (AdMob)** and **Google User Messaging Platform (UMP)** to support free distribution.

- **Ad Placement Policy:** Ads are displayed strictly as non-intrusive banner or native cards on general browsing/optimizer views. **No ads are ever placed inside Safe Vault or inside destructive/file-transfer confirmation flows.**
- **Data Collected by Google Mobile Ads SDK:** When initialized, Google's SDK may automatically collect and process:
  - **Advertising ID (`AD_ID`)** and device identifiers (subject to your consent and OS ad tracking settings)
  - **Approximate location** (derived from IP address)
  - **App interactions** (ad impressions, taps, and basic engagement signals)
  - **Diagnostics** (crash logs, ad load latency, SDK performance metrics)
- **Consent Management (UMP):** Where required by regional regulations (EEA, UK, Switzerland, and applicable US state privacy laws), Liquid Files presents a UMP consent form before requesting ads. You can review or withdraw ad consent anytime under **Settings → Manage Ad Privacy Choices**.
- Google's data practices are governed by the [Google Privacy Policy](https://policies.google.com/privacy) and [How Google uses information from sites or apps that use our services](https://policies.google.com/technologies/partner-sites).

---

## 4. Android Permissions Used

| Permission | Purpose |
|---|---|
| `MANAGE_EXTERNAL_STORAGE` (Android 11+) | Core file manager capability to browse, move, copy, rename, analyze, and organize all user-accessible folders across internal storage and SD cards. |
| `READ_EXTERNAL_STORAGE` (Android 8–12) / `WRITE_EXTERNAL_STORAGE` (Android 8–10) | Reading and managing device files on Android 8.0 through Android 12. |
| `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`, `READ_MEDIA_VISUAL_USER_SELECTED` | Displaying real photos, videos, and audio files from MediaStore on Android 13–16+ when All Files Access is not granted or partial photo access is selected. |
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Loading Google Mobile Ads / UMP consent forms and determining local Wi-Fi IPv4 address for LAN Wi-Fi Share. |
| `com.google.android.gms.permission.AD_ID` | Required by Google Mobile Ads SDK on Android 13+ (respects UMP consent and user Advertising ID opt-out). |
| `USE_BIOMETRIC` | Optional hardware BiometricPrompt unlock for Safe Vault. |
| `WAKE_LOCK` | Keeping audio/video playback active smoothly during media sessions. |

---

## 5. Data Retention, Deletion & User Controls

- **Uninstalling Liquid Files** permanently deletes the local database (`AppDatabase`), Vault security preferences, and encrypted Safe Vault files inside private app storage (unless restored beforehand).
- You can remove files from **Trash** permanently at any time, restore **Safe Vault** files to public storage, or update your PIN/recovery passphrase inside **Settings**.
- You can opt out of personalized advertising via **Settings → Manage Ad Privacy Choices** (UMP) or Android system **Settings → Privacy → Ads**.

---

## 6. Children's Privacy

Liquid Files is a general-utility file management tool and is not directed at children under 13 (or the minimum age required in your jurisdiction). We do not knowingly collect personal information from children.

---

## 7. Updates & Contact

We may update this Privacy Policy to reflect new OS requirements or SDK disclosures. Before publishing on Google Play or Uptodown, replace the contact placeholder below with your active support email address and publish this document at a stable HTTPS URL:

- **Developer / Support Email:** `<REPLACE_WITH_YOUR_SUPPORT_EMAIL>`
- **Public Policy URL:** `<REPLACE_WITH_YOUR_HOSTED_HTTPS_PRIVACY_URL>`
