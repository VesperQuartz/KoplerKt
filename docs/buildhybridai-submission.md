# #BuildHybridAI 2026 submission

## Form fields

| Field | Value |
|---|---|
| Project name | KoplerKt |
| Tagline | Understand any screen. On-device first. Cloud only when you allow it. |
| Platform | Android |
| Built with | Firebase AI Logic, Gemini Nano, Gemini 3.5 Flash Lite, Android Accessibility API, Kotlin, Material Components |
| Repository | https://github.com/VesperQuartz/KoplerKt |
| Demo video | _Add public YouTube URL before submitting_ |
| Project URL | https://github.com/VesperQuartz/KoplerKt/releases/latest |
| Country | _Add your country_ |

## Text description

KoplerKt is an Android app that explains any screen in plain language. It uses Firebase AI Logic for both sides of a hybrid inference loop. On supported devices, the app calls Gemini Nano through the on-device model API, so the screen never leaves the phone. If the local model is unavailable or cannot answer, the app can fall back to Gemini 3.5 Flash Lite in the cloud. The cloud path is opt-in: the first-use dialog explains that the screen's text and a screenshot may be sent to Google, and the explanation card always shows which path produced the answer. App Check is initialized at startup; the demo uses the debug provider, which is accepted for this hackathon.

The app draws a floating eye through an Android accessibility service. It reads the active window only when the eye is tapped, masks password and editable fields, hides the eye, captures a screenshot on Android 11 or newer, and sends the captured content to the model. The explanation is organized into three short sections: what the screen is, what it wants from the user, and what to watch out for. A built-in demo bank screen shows the phishing case, and the "Show what I read" view makes the capture transparent.

The value proposition is privacy-first comprehension for people who are confused, rushed, or targeted by a scam. Cloud assistants are powerful but require sending the screen away. On-device models are private but not available on every device. KoplerKt uses each where it fits, and the user can see and control the handoff.

KoplerKt started as a Kotlin Android template. During the #BuildHybridAI 2026 submission period (November 9–30, 2026), it was significantly updated into the app described here: the accessibility service and floating eye, the text-tree reader and password masking, the on-device and cloud routing through Firebase AI Logic, cloud consent and source badges, screenshot capture, the setup and demo screens, and the explanation card.

## Existing app summary

KoplerKt existed before the submission period as a Kotlin Android template. Everything that makes it a hybrid AI product was built during the submission window:

- the accessibility service and draggable floating eye
- the text-tree reader with password and editable-field masking
- the on-device first routing and cloud fallback through Firebase AI Logic
- the cloud consent dialog, saved preference, and source badges
- screenshot capture with the eye hidden
- the setup screen, on-device model status and download states
- the demo bank screen and the explanation bottom sheet
- the full visual redesign

The project does not have public usage to report before this submission.

## Runtime proof for Firebase AI Logic

The rules require Firebase AI Logic to be imported and actually called at runtime, not just named in documentation.

- [`ExplanationActivity.kt`](../app/src/main/java/ng/mrlectus/koplerkt/ExplanationActivity.kt) imports `com.google.firebase.ai.*` and calls `generateContent()` for both the on-device and cloud models.
- [`KoplerKtApplication.kt`](../app/src/main/java/ng/mrlectus/koplerkt/KoplerKtApplication.kt) initializes Firebase and App Check at app startup.
- [`MainActivity.kt`](../app/src/main/java/ng/mrlectus/koplerkt/MainActivity.kt) uses `model.onDeviceExtension` to check and download the local model.

## Pre-submission checklist

- [x] Android app uses Firebase AI Logic hybrid inference.
- [x] App Check initialized. Debug provider documented; debug tokens accepted by the rules.
- [x] On-device path and cloud fallback implemented.
- [x] User consent for cloud text and screenshot.
- [x] Password masking and tap-only capture.
- [x] Public repository.
- [x] MIT license file. The rules prefer Apache but allow any open-source license.
- [ ] License visible in the GitHub About section.
- [ ] Signed release APK attached to GitHub Releases.
- [ ] Three-minute demo video on public YouTube, in English.
- [ ] Demo video shows the app functioning on Android.
- [ ] Demo video shows an on-device answer on a supported phone.
- [ ] Text description copied into the submission form.
- [ ] Existing app summary copied into the submission form.
- [ ] Every link tested while logged out.
- [ ] Submission sent before November 30, 2:00 PM PT.

## Notes

- The demo Firebase project and `app/google-services.json` are included so the repository builds and runs. The config is a client configuration, not a server secret. Replace it for production.
- The app uses `DebugAppCheckProviderFactory` for the hackathon. Firebase AI Logic debug tokens are accepted in submissions. Enforcement can remain off for the demo.
- The on-device segment of the demo requires a Gemini Nano-capable phone. The emulator shows **Unavailable** and uses the cloud path.
- The demo bank screen is fake. It contains no real bank data.
