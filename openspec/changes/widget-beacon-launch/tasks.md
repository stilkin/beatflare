## 1. MainActivity — accept a "launch Beacon" intent

- [ ] 1.1 Add a public `EXTRA_LAUNCH_BEACON` intent-extra key (companion constant on `MainActivity`)
- [ ] 1.2 Set `android:launchMode="singleTop"` on `MainActivity` in `AndroidManifest.xml`
- [ ] 1.3 In `onCreate`, read the launch-beacon extra and seed the initial `showBeacon` state so a cold start opens straight into the Beacon overlay
- [ ] 1.4 Override `onNewIntent`, call `setIntent(intent)`, and handle the extra so a tap while the app is already running shows the Beacon without spawning a duplicate activity
- [ ] 1.5 Route the launch through the existing `onLaunchBeacon` logic (so mic acquisition stays gated on `beaconReactToSound && micGranted`, with the static fallback), and **consume** the flag after handling so rotation / return-from-background does not re-trigger the Beacon

## 2. Widget — become a stateless Beacon launcher

- [ ] 2.1 Replace the click `PendingIntent` with `PendingIntent.getActivity` targeting `MainActivity`, carrying `EXTRA_LAUNCH_BEACON` and `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP`
- [ ] 2.2 Remove `ACTION_TOGGLE` handling and the `acquire/release(PERSISTENT)` toggle
- [ ] 2.3 Remove `ACTION_STATE_CHANGED` handling, `refreshAll`, and the `isRunning`-based running/stopped background + status text; render one static look ("Beacon — tap to show")
- [ ] 2.4 Update the widget layout/copy and drawables as needed for the stateless launcher look
- [ ] 2.5 Verify whether `GlyphSenseService.notifyStateChanged` is still needed; drop the widget-only call sites if they exist solely to refresh the widget

## 3. Verify

- [ ] 3.1 `./gradlew ktlintCheck :app:assembleDebug` passes
- [ ] 3.2 Install on the Phone (3a); tap the widget from the home screen → app opens directly into the Beacon overlay
- [ ] 3.3 Confirm the Beacon renders with the last-used settings (set a distinct hue/text, then launch via widget)
- [ ] 3.4 Tap the widget while the app is already open → existing instance comes forward into the Beacon, no duplicate activity
- [ ] 3.5 Dismiss the Beacon on-screen (back / tap); confirm the widget shows no running state and a second tap re-launches cleanly
- [ ] 3.6 Confirm the persistent visualizer Start/Stop still works from the Lights tab and the notification (unaffected by the widget change)
- [ ] 3.7 `openspec validate widget-beacon-launch` passes
