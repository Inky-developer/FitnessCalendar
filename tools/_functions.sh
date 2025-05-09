#!/bin/bash

export CHANGELOG_FILE="CHANGELOG.md"
export VERSION_FILE="app/version.properties"


# Load the two properties (versionCode and versionName)
# shellcheck source=app/version.properties
source "$VERSION_FILE"

function getUnreleasedChangelog() {
    # Separate the changelog into chunks of double newlines and only keep the second chunk
    # (Which contains the unreleased release notes)
  awk -v RS='\n\n' 'NR == 2' CHANGELOG.md
}

# Returns the content of the changelog for the version code passed as the first parameter
function getChangelog() {
  cat "fastlane/metadata/android/en-US/changelogs/$1.txt"
}