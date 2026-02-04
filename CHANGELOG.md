# Unreleased

# 2026.02.1

- Correctly distribute actity duration in statistics (#68)
- Add Russian translation
- Some bug & crash fixes

# 2025.11.2

- Allow adding multiple images to an activity
- Allow selecting a single video frame as image for an activity
- The minimum supported android sdk version is now 28
- Rework the view of activities with a track
- Allow inputting the duration of an activity

# 2025.11.1

- Fix bug where setting the place to none was not possible
- Slightly improve predictions for vehicle and place when starting a recording or adding a new
  activity

# 2025.07.2

- Add setting for initial statistic
- Allow overlaying graphs in the track view
- Add color scheme toggle to the share view
- PlaceListView: Only suggest used colors for filtering

# 2025.07.1

- Feature: Share activity summary as image
- Round vertical ascend to meters, but not kilometers

# 2025.05.1

- Remove "Store location information" preference
- Don't dismiss dialogs on click outside

# 2025.04.1

- Add a public api that allows third party apps to create activities

# 2025.03.1

- Let mosaic chart always end at the current date
- Implement backup restore functionality

# 2025.02.2

- Allow limiting suggested places for an activity type by their color
- Add dedicated records section to the summary view
- Don't show statistics if they are 0
- Use dynamic y-range for heart rate graphs, so that it will not always start at 0

# 2025.02.1

- Add longest time to summary 
- Add a description field to places 
- Add share button for tracks

# 2025.01.1

- Add a summary view for a detailed summary of the current filter
- The app will now automatically import tracks from configurable folders
- Fix y-range for elevation graphs
- Display total ascent and total descent
- Add average heart rate, total heart rate, average ascent and total ascent to statistics
- Fix average moving speed for slow activities

# 2024.12.1

- Fix quick settings dialog background color 
- Improve place list design
- Add ability to add images to places 
- Fix date picker background color 
- Allow setting a custom start time when starting a recording
- (2024.11.1) Fix bug where saving an activity was not possible in some cases if the activity type had no duration
- (2024.11.1) Creating a new activity from the day view now defaults it to the correct day

# 2024.10.3

- Add a filter button to the statistics view
- Smooth the per-activity statistics graphs to make it easier to look at them
- Round values to 0 digits in the track graph view for the HeartRate and Elevation charts
