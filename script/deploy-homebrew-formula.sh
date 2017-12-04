#!/bin/bash
#
# Script to deploy Homebrew formula
#

set -e

WORKING_DIR=$(cd $(dirname $0)/..; pwd)/homebrew
GIT_DIR=${WORKING_DIR}/git
HOMEBREW_FORMULA=${WORKING_DIR}/strongbox.rb

[[ -d "${GIT_DIR}" ]] || mkdir -p "${GIT_DIR}"

GIT_REPO=https://${GITHUB_TOKEN}@github.com/schibsted/homebrew-strongbox.git

cd ${GIT_DIR}
git init

echo "Working dir:"
pwd

git pull ${GIT_REPO}

if [ ! -f ${HOMEBREW_FORMULA} ]; then
    echo "Homebrew formula not present in '${HOMEBREW_FORMULA}'"
    exit 1
fi

cp ${HOMEBREW_FORMULA} .
git add strongbox.rb

echo "Working dir:"
pwd

git commit -m "Upgraded to version ${TRAVIS_TAG}"
git push ${GIT_REPO} master