#!/bin/bash
#
# Script to generate Homebrew formula
#

set -e

ROOT_DIR=$(cd $(dirname $0)/..; pwd)
DEST_DIR="${ROOT_DIR}/homebrew"
[[ -d "${DEST_DIR}" ]] || mkdir -p "${DEST_DIR}"

INPUT_FILE="${ROOT_DIR}/script/homebrew-formula.tmpl"
OUTPUT_FILE="${DEST_DIR}/strongbox.rb"

ARCHIVE_FILE=${ROOT_DIR}/cli/build/distributions/strongbox-cli-${TRAVIS_TAG}.tar.gz
if [ ! -f ${ARCHIVE_FILE} ]; then
    echo "Archive file '${ARCHIVE_FILE}' not found"
    exit 1
fi
SHA256SUM=$(shasum -a 256 ${ARCHIVE_FILE} | cut -d " " -f1)

sed -e "s/@PKG_VERSION@/${TRAVIS_TAG}/g" \
	    -e "s/@SHA_256_SUM@/${SHA256SUM}/g" \
	    ${INPUT_FILE} > ${OUTPUT_FILE}
