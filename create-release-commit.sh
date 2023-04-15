#!/bin/bash

set -e

print_usage() {
  echo "Usage of conventional-commit-version"
  echo "Prints a version obtained from this project's git commit subjects. Expects conventional commit messages."
  echo "Options:"
  echo "  -p    Appends the current nanosecond precision timestamp as the 'prerelease' section."
  echo "  -c    Inserts the new version to all relevant files. Commits these file changes to git."
  echo "        Only applicable if the current git working tree is clean."
  echo "  -t    Adds the computed version as git tag. Only applies if 'c' is set."
}

get_version() {
  major_regex="BREAKING\wCHANGE|\!:"
  minor_regex="^(feat).*:"
  patch_regex="^(fix|perf).*:"

  previous_version=$(git describe | cut -d "-" -f1)
  commit_offset=$(git describe | cut -d "-" -f2)

  major=$(echo "${previous_version}" | cut -d "." -f1)
  minor=$(echo "${previous_version}" | cut -d "." -f2)
  patch=$(echo "${previous_version}" | cut -d "." -f3)

  log_since_offset=$(git log -n "${commit_offset}" --format=%s)
  major_steps=$(echo "$log_since_offset" | grep -E "${major_regex}" | wc -l)
  minor_steps=$(echo "$log_since_offset" | grep -E "${minor_regex}" | wc -l)
  patch_steps=$(echo "$log_since_offset" | grep -E "${patch_regex}" | wc -l)
  version_suffix="${commit_offset}-$(git rev-parse --short=8 HEAD)-SNAPSHOT"

  if [[ ${major_steps} -gt 0 ]]; then
    major=$(( major+major_steps ))
    minor=0
    patch=0
  elif [[ ${minor_steps} -gt 0 ]]; then
    minor=$(( minor+minor_steps ))
    patch=0
  else
    patch=$(( patch+patch_steps ))
  fi

  if [[ ${prerelease} ]]; then
    echo "${major}.${minor}.$(( patch+1 ))-${version_suffix}"
  else
    echo "${major}.${minor}.${patch}"
  fi
}

# get options
prerelease=''
commit=''
tag=''
while getopts 'pct' flag; do
  case "${flag}" in
    p) prerelease=1;;
    c) commit=1;;
    t) tag=1;;
    *) print_usage && exit 1;;
  esac
done

# work
version=$(get_version)
echo "${version:?Could determine version}"

if [[ ${commit} ]]; then
  # only commence if working dir is clean
  git diff-index HEAD --quiet --exit-code || (echo "Git working tree is not clean. Exiting..." && exit 1)

  mvn test --quiet
  # Only change version in docs for non-prerelease version
  if ! [[ ${prerelease} ]]; then
    sed "s/^.*:version:.*$/:version: ${version}/" doc.adoc -i
    mvn generate-resources --quiet
    cp target/generated-docs/doc.html doc.html
    git add doc.html
  fi

  mvn versions:set -DnewVersion="${version}" --quiet

  git add pom.xml doc.adoc
  git commit -m "chore: Release ${version}"
  if [[ ${tag} ]]; then
    git tag -a "${version}" -m "Release ${version}"
  fi
else
  echo "Dry run. Use option -c to commit changes."
fi