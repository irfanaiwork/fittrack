# Play Console Submission Checklist — FitTrack Daily

Code-side fixes completed 2026-11-04 (GitHub update system removed, restricted permissions removed, App Check debug provider made debug-only, privacy policy added). The items below are the Console-side steps that remain at submission time.

## Before uploading

- [ ] Build the release **AAB** from this code (`bundleRelease`). You need two files that are intentionally NOT in this repo: `my-upload-key.jks` (upload keystore) and `app/google-services.json` (from the Firebase project). Keep a backup of the keystore — if it is lost, the app can never be updated again.
- [ ] Check the Play **pre-launch report** after upload (crashes/ANRs on test devices).

## Store listing

- App name: **FitTrack Daily** (13/30 chars)
- Short description (80 chars max): "Eat. Move. Track. Improve. Diet, workout, water & reminder tracker."
- Category: **Health & Fitness**
- Icon 512×512 PNG, feature graphic 1024×500, minimum 2 phone screenshots (take from the running app)
- Privacy policy URL: **https://irfanaiwork.github.io/fittrack-privacy/** (live page; source in `privacy-policy.html` / `PRIVACY_POLICY.md`)
- Contact email: irfan.aiwork@gmail.com

## App content forms (all mandatory)

- **Data safety:** No data shared with third parties. Tracking/health data is stored on-device only and never transmitted, so it is not "collected" in Google's sense. If the form requires declaring the Firebase App Check traffic, declare *Device or other IDs — collected for fraud prevention/security, processed by Google as service provider*. Data deletion: users can clear data in-app (Settings) and by uninstalling — state this in the form.
- **Health apps declaration:** Fitness/diet tracking only; the app does **not** diagnose, treat, or provide medical advice. Health data stays on the device.
- **Permissions declaration — exact alarms (SCHEDULE_EXACT_ALARM):** Justification to paste: *"FitTrack Daily's core feature is user-scheduled reminders for meals, workouts, water intake and medication-style routines. Users pick an exact time for each reminder and rely on it firing at that exact time, like an alarm clock. Alarms are only ever set for times the user explicitly chose in the app."* Also select the "Alarms & reminders" use case.
- **Content rating (IARC):** Answer honestly; expected outcome **Everyone**. No violence, no user-generated content, no social features.
- **Target audience:** Select **18+** (keeps the app out of the Families programme; a weight/diet tracker should not target children).
- **Ads declaration:** The app contains **no ads**.
- **App access:** No login or restricted areas — "All functionality is available without special access."
- **News / government / COVID declarations:** Not applicable / No.

## Account-level requirement (personal accounts created after 13 Nov 2023)

- Run a **closed test with at least 12 testers opted in continuously for 14 days**, then apply for production access and answer the questionnaire (what feedback testers gave, what was changed).
- Only after production access is granted can the production release go to review (typically 3–7 days).

## Notes

- App updates must go through Google Play only. The old in-app GitHub update checker was removed for this reason — do not re-add it to a Play build.
- The app declares no `USE_EXACT_ALARM` and no `USE_FULL_SCREEN_INTENT` (both restricted). Reminder alerts use a high-priority notification; on Android 14+ full-screen display follows the user's per-app setting.
