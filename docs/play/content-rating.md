# Play Console — Content rating questionnaire (IARC) (`docs/play/content-rating.md`)

Source for **App content > Content ratings** in Play Console. Category:
Utility / Productivity (closest IARC category to an offline gradebook tool;
Play Console's questionnaire calls this category "Utility, Productivity,
Communication, or Other").

| Question | Answer | Justification |
|---|---|---|
| Violence — does the app contain realistic or cartoon violence? | No | Gema is a gradebook and attendance tool; it has no game content of any kind. |
| Blood and gore | No | Not applicable — no such content exists in the app. |
| Sexuality / nudity | No | Not applicable. |
| Profanity or crude humor | No | The app's text is limited to educational and administrative vocabulary (attendance states, CNEB competencies, achievement levels). |
| References to drugs, alcohol, or tobacco | No | Not applicable. |
| Fear or horror themes | No | Not applicable. |
| Gambling — does the app contain simulated gambling? | No | Not applicable. |
| Does the app allow the purchase of simulated gambling chips or currency? | No | Gema has no in-app purchases of any kind. |
| User-generated content — can users share content with other users (text, images, video) inside the app? | No | Gema has no sharing, messaging, or social feature between users; the only "sharing" is the Android system share sheet the teacher uses to move their own backup or exported file to another app of their choosing. |
| Does the app share the user's physical location with other users or allow the user to share it? | No | Gema does not access or use location services. |
| Does the app allow users to interact or exchange content, including with unknown persons? | No | Gema is single-device, single-user; there is no multiplayer, chat, or account system. |
| Digital purchases — does the app allow purchasing digital goods or in-app currency? | No | Gema is free with no in-app purchases and no in-app currency. |
| Does the app contain ads? | No | Gema does not display any advertising. |
| Does the app share personal or device information with third parties for advertising or marketing? | No | No third-party SDK is included; no data leaves the device. |

## Notes for the person filling the Play Console form

- Select **Utility, Productivity, Communication, or Other** as the app category when the questionnaire asks for it.
- Answer every "does the app contain..." question with **No**; none apply to an offline single-user gradebook.
- The expected resulting rating is the lowest tier in every region (e.g. **PEGI 3 / ESRB Everyone / IARC "3+"**), consistent with a productivity tool with no objectionable content.
