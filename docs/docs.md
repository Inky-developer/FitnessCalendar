# Documentation

## Public API

The public API allows third party apps to interact with Fitness Calendar and create activities and
recordings.

Apps cannot interact with the API unless it has been enabled in the settings.

To interact with the api, start an intent against one of the endpoints.

### Public API Endpoints

| Class name                                           | Flags                                                               | Description                                                                                                                                                  |
|------------------------------------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `com.inky.fitnesscalendar.publicApi.CreateActivity`  | `ACTIVITY_TYPE` (String?), `START_TIME` (Long?), `END_TIME` (Long?) | Opens the dialog to create a new activity, with the activity type, start time and end time set to the values of the specified flags. Every flag is optional. |
| `com.inky.fitnesscalendar.publicApi.CreateRecording` | none                                                                | Opens the dialog to create a new recording                                                                                                                   |
| `com.inky.fitnesscalendar.publicApi.StopRecording`   | `ACTIVITY_TYPE` (String)                                            | Stops all recordings of the given activity type. Fails if no activity type is specified.                                                                     |

For example, a call to create a new activity could be done like this in kotlin:

```kotlin
val intent = Intent().apply {
    component = ComponentName("com.inky.fitnesscalendar", "com.inky.fitnesscalendar.publicApi.CreateActivity")
    putExtra("ACTIVITY_TYPE", "Running")
}
startActivity(intent)
```