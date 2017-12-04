#!/bin/bash
#
# Script to deploy Homebrew formula
#

set -e

WORKING_DIR=$(cd $(dirname $0)/..; pwd)/homebrew/git

[[ -d "${WORKING_DIR}" ]] || mkdir -p "${WORKING_DIR}"

GIT_REPO=https://${GITHUB_TOKEN}@github.com/schibsted/homebrew-strongbox.git

cd ${WORKING_DIR}
git init
git pull ${GIT_REPO}
cp ../strongbox.rb .
git add strongbox.rb
git commit -m "Upgraded to version ${TRAVIS_TAG}"
git push ${GIT_REPO} master