#!/bin/bash

CHANGELOG_FILE="CHANGELOG.md"
VERSION_FILE="app/version.properties"
# Load the two properties
# shellcheck source=app/version.properties
source "$VERSION_FILE"

source tools/_functions.sh


function incrementVersionCode() {
  NEW_VERSION_CODE=$((versionCode + 1))
}

function updateVersionName() {
  NEW_YYYY_MM=$(date +"%Y.%m")
  OLD_YYYY_MM=$(echo "$versionName" | cut -d '.' -f 1-2)
  OLD_COUNT=$(echo "$versionName" | cut -d '.' -f 3)
  COUNT_PLUS_ONE=$((OLD_COUNT + 1))

  if [[ "$NEW_YYYY_MM" != "$OLD_YYYY_MM" ]]; then
    NEW_VERSION_NAME=$(printf "%s.1" "$NEW_YYYY_MM")
  else
    NEW_VERSION_NAME=$(printf "%s.%s" "$NEW_YYYY_MM" "$COUNT_PLUS_ONE")
  fi
}

function writeFastlaneChangelog() {
  getCurrentChangelog > "fastlane/metadata/android/en-US/changelogs/$NEW_VERSION_CODE.txt"
}

# Moves all notes under the unreleased header into the new release
function updateChangelog() {
  sed -i "1s/.*/# Unreleased\n\n/; 2s/.*/# $NEW_VERSION_NAME\n/" $CHANGELOG_FILE
}

echo "Incrementing the version number…"
incrementVersionCode
updateVersionName
printf "versionCode=%s\nversionName=%s" "$NEW_VERSION_CODE" "$NEW_VERSION_NAME" > $VERSION_FILE

echo "Updating Changelog…"
writeFastlaneChangelog
updateChangelog

echo "Done!"
echo "To create a new release, commit the changes, create a new tag, and push both changes and tag."
