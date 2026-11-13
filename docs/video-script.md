# Demo video script

Target length: **2:45**. Hard limit: 3:00. Only the first 3 minutes are judged.

Requirements:

- Publicly visible on YouTube
- English voiceover or English subtitles
- Footage of the app functioning on Android
- Shows the on-device path on a supported phone
- Link added to the Devpost submission form

## Before recording

- Use the supported phone for the on-device segment.
- Confirm the setup screen shows **Ready** for the on-device model.
- Pre-allow cloud fallback if you want to avoid the consent dialog on camera, or show the dialog deliberately.
- Clear notifications and set the phone to a clean home screen.
- Record at 1080p or higher. Capture the phone screen with the built-in recorder or `adb shell screenrecord`.
- Record the voiceover separately and edit to the picture.

## Shot list

### 0:00 – 0:15 — Hook

**Picture:** The fake SecureBank alert. Slow push-in on the urgency line and the password field.

**Voiceover:**
> "This looks like a bank alert. It is actually a phishing screen designed to make you panic and type your password. Millions of people see screens like this every day and have no easy way to know what is real."

### 0:15 – 0:35 — What KoplerKt is

**Picture:** Home screen with the floating eye. Tap the eye; the explanation card opens.

**Voiceover:**
> "KoplerKt is a floating eye for any Android screen. Tap it once and it explains what you are looking at, what it wants from you, and what to watch out for. It reads a screen only when you tap. Nothing is captured in the background."

### 0:35 – 1:00 — Setup and on-device model

**Picture:** KoplerKt setup. Highlight accessibility **On**, the green **Ready** pill, and the cloud fallback switch.

**Voiceover:**
> "Setup takes one accessibility permission. On a supported phone, Gemini Nano runs through Firebase AI Logic. The green pill means the model is ready on this device. The app tries this local model first, so the screen never has to leave the phone."

### 1:00 – 1:40 — On-device scam explanation

**Picture:** Open the demo bank screen. Tap the eye. Wait for the green **Answered on this phone** badge. Scroll through the three sections.

**Voiceover:**
> "Here is the phishing screen. One tap, and the answer comes from this phone. It calls the screen a phishing attempt and a pressure tactic. It explains that the screen wants a banking username and password, and it warns about the 24-hour threat. No text and no screenshot left the device."

### 1:40 – 1:55 — Show what I read

**Picture:** Tap **Show what I read**. Highlight the masked password field and the masked text field.

**Voiceover:**
> "The app shows exactly what it read. Password and editable fields are replaced before anything is sent anywhere. That is the transparency layer."

### 1:55 – 2:10 — Offline proof

**Picture:** Turn on airplane mode. Tap the eye again. Green badge. Explanation appears.

**Voiceover:**
> "Turn off the network and it still works. The on-device model is fast, private, and available offline. That is the reason to run local first."

### 2:10 – 2:30 — Cloud fallback

**Picture:** On a screen the local model cannot handle, or on an unsupported device, tap the eye. Show the consent dialog. Allow cloud. Show the amber **Cloud answer — text and screenshot sent to Google** badge.

**Voiceover:**
> "When the local model is unavailable or cannot answer, KoplerKt can use Gemini in the cloud. The first time, it asks. If you allow it, the screen text and a screenshot go to Google through Firebase AI Logic. The amber badge tells you exactly which path answered."

### 2:30 – 2:45 — Impact and close

**Picture:** Return to the setup screen, then a final shot of the floating eye over the demo screen. Overlay the repo URL.

**Voiceover:**
> "KoplerKt makes the hybrid promise visible: private by default, cloud only when needed and allowed. It is built for older adults, non-native speakers, and anyone facing a screen they cannot afford to guess about. The code is open source under the MIT license."

## End card

- KoplerKt
- github.com/VesperQuartz/KoplerKt
- Built with Firebase AI Logic, Gemini Nano, and Gemini 3.5 Flash Lite
- #BuildHybridAI 2026

## Editing notes

- Keep the on-device badge visible long enough to read.
- Do not speed up the explanation so much that the text is unreadable.
- Add English captions even with a voiceover.
- Cut any dead time waiting for the model; a short trim is fine, but do not fake the result.
- Export at 1080p, H.264, and upload as public, not unlisted.
