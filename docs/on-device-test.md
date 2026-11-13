# On-device validation checklist

Use this checklist on the borrowed Gemini Nano-capable phone before recording the final demo.

- Device:
- Android version:
- AICore version:
- Build: `KoplerKt-v0.1.0.apk`
- Date:

## 1. Install

- Install the signed APK from [Releases](https://github.com/VesperQuartz/KoplerKt/releases/latest) or with `adb install -r KoplerKt-v0.1.0.apk`.
- On Android 13+, if the accessibility toggle is grayed out, open **Settings → Apps → KoplerKt → ⋮ → Allow restricted settings**.

## 2. Enable the eye

- Open KoplerKt → **Open accessibility settings** → **KoplerKt** → On.
- Return to KoplerKt. The Accessibility pill should be green and say **On**.
- The floating eye should appear over other apps.

## 3. Check the on-device model

Open the setup screen and read the **On-device model** row.

- **Ready** — proceed to step 4.
- **Download** — keep the setup screen open and note the progress. Wait until it changes to Ready.
- **Unavailable** — stop. This phone cannot run the local model; the on-device demo cannot be recorded on it.

Useful log:

```bash
adb logcat -s MainActivity
```

## 4. On-device demo

- Open **Open demo bank screen**.
- Tap the floating eye.
- On the first run, choose **On-device only** so the test proves the local path. Cloud stays off.
- Expected:
  - green **Answered on this phone** badge
  - three sections: what this is, what it wants from you, things to watch out for
  - a phishing warning
  - no truncation at the end of the answer
- Tap **Show what I read**. Verify:
  - `[password field — content hidden]`
  - `[text field — content hidden] (Online banking username)`
- Tap **Done**. The demo screen should return and the eye should come back.

## 5. Offline proof

- Turn on airplane mode.
- Tap the eye again on the demo screen.
- Expected: green badge and a complete answer, with no network.

## 6. Capture evidence

Take phone screenshots of:

- setup with **Ready**
- green badge explanation
- airplane-mode explanation
- raw text with masked fields

Record a clean screen capture for the demo video. Avoid showing personal notifications or real accounts.

## 7. Notes

- Time to first on-device answer:
- Answer length: complete / cut off
- Model status after reboot:
- Any crash or ANR:
- Accessibility toggle behavior:
- Other observations:

## 8. Cloud path

The cloud path is already verified on the emulator. To re-check it after the phone test:

- Turn off airplane mode.
- In setup, turn on **Cloud fallback**.
- The emulator or an unsupported phone will use the cloud model.
- Expected: amber **Cloud answer — text and screenshot sent to Google** badge.
