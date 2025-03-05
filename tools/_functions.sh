#!/bin/bash

function getCurrentChangelog() {
    # Separate the changelog into chunks of double newlines and only keep the second chunk
    # (Which contains the unreleased release notes)
  awk -v RS='\n\n' 'NR == 2' CHANGELOG.md
}