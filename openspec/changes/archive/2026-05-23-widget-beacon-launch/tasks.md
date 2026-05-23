## 1. MainActivity — accept a "launch Beacon" intent

- [x] 1.1 Add a public `EXTRA_LAUNCH_BEACON` intent-extra key (companion constant on `MainActivity`)
- [x] 1.2 Set `android:launchMode="singleTop"` on `MainActivity` in `AndroidManifest.xml`
- [x] 1.3 In `onCreate`, read the launch-beacon extra so a cold start opens straight into the Beacon overlay (via a one-shot tick observed by a `LaunchedEffect` that drives the existing `onLaunchBeacon`, rather than seeding `showBeacon` directly — keeps mic gating in one place)
- [x] 1.4 Override `onNewIntent`, call `setIntent(intent)`, and handle the extra so a tap while the app is already running shows the Beacon without spawning a duplicate activity
- [x] 1.5 Route the launch through the existing `onLaunchBeacon` logic (so mic acquisition stays gated on `beaconReactToSound && micGranted`, with the static fallback), and **consume** the flag after handling so rotation / return-from-background does not re-trigger the Beacon

## 2. Widget — become a stateless Beacon launcher

- [x] 2.1 Replace the click `PendingIntent` with `PendingIntent.getActivity` targeting `MainActivity`, carrying `EXTRA_LAUNCH_BEACON` and `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP`
- [x] 2.2 Remove `ACTION_TOGGLE` handling and the `acquire/release(PERSISTENT)` toggle
- [x] 2.3 Remove `ACTION_STATE_CHANGED` handling, `refreshAll`, and the `isRunning`-based running/stopped background + status text; render one static look ("Tap to show")
- [x] 2.4 Update the widget layout/copy and drawables as needed for the stateless launcher look (status text → "Tap to show"; deleted the now-unused `widget_bg_running` drawable)
- [x] 2.5 Verify whether `GlyphSenseService.notifyStateChanged` is still needed; drop the widget-only call sites if they exist solely to refresh the widget (dropped both calls + the companion method + the now-unused import)

## 3. Verify

- [x] 3.1 `./gradlew ktlintCheck :app:assembleDebug` passes
- [x] 3.2 Install on the Phone (3a); tap the widget from the home screen → app opens directly into the Beacon overlay
- [x] 3.3 Confirm the Beacon renders with the last-used settings (set a distinct hue/text, then launch via widget)
- [x] 3.4 Tap the widget while the app is already open → existing instance comes forward into the Beacon, no duplicate activity
- [x] 3.5 Dismiss the Beacon on-screen (back / tap); confirm the widget shows no running state and a second tap re-launches cleanly
- [x] 3.6 Confirm the persistent visualizer Start/Stop still works from the Lights tab and the notification (unaffected by the widget change)
- [x] 3.7 `openspec validate widget-beacon-launch` passes

## Notes

- **Bug found + fixed during on-device verification:** the widget's launch `PendingIntent` and the service notification's content `PendingIntent` shared request code `0` (both `getActivity` → `MainActivity`). Since `PendingIntent` identity uses `Intent.filterEquals` (which ignores extras), they were the same token — so the notification's `FLAG_UPDATE_CURRENT` overwrote the widget's intent and stripped `EXTRA_LAUNCH_BEACON` the first time any foreground session built the notification, silently breaking the launch (it landed on the bare Beacon tab). Fixed by giving the widget `PendingIntent` a distinct request code (`100`). Confirmed via logcat: launched intent showed `(has extras)` before a session and lost it after; resolved after the fix.
