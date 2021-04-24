#!/bin/bash

git describe --tags --abbrev=0 --match *-rc > last-version.txt
major=$(cut -d . -f 1 last-version.txt)
minor=$(cut -d . -f 2 last-version.txt)
patch=$(cut -d . -f 3 last-version.txt | cut -d - -f 1)
next_patch=$((patch + 1))
next_version="$major.$minor.$next_patch-rc"
echo "$next_version" | xargs git tag
git push --tags
printf "api:\n\timage:\n\t\ttag: %s\n\n" "$next_version" > doer-values.yaml
