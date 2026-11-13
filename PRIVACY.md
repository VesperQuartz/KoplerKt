# Privacy

KoplerKt reads a screen only when you tap the floating eye. It does not capture anything in the background.

## What the app accesses

- The visible text tree of the active window.
- A screenshot of the current display on Android 11 and newer.
- The package name of the foreground app, when it is not KoplerKt itself.

## What stays on the phone

- On-device explanations are produced with Gemini Nano through Firebase AI Logic. The captured screen and the answer are not sent to a server.
- The screenshot is held in memory only long enough to build the explanation request. It is not written to storage.
- Password and editable fields are replaced with a masked label before the text is used.

## What can be sent to the cloud

If the on-device model is unavailable or cannot answer, and you have allowed cloud fallback, the app sends the captured screen text and the screenshot to Google through Firebase AI Logic. The cloud model is Gemini 3.5 Flash Lite.

Cloud fallback is off until you allow it. The first explanation asks for permission, and you can change the choice in setup.

## What the app does not collect

- No accounts.
- No app analytics.
- No advertising identifiers.
- No stored screen captures or answers.
- No background screen reading.

Firebase and Google process requests under their own terms and privacy policies. App Check may process installation identifiers to protect the Firebase project.

## Contact

Open an issue at https://github.com/VesperQuartz/KoplerKt/issues.
