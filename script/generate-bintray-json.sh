#!/bin/bash
#
# Script to generate descriptor.json file needed by Travis for bintray deployment
#

set -e

populate_descriptor_file() {
	local package_type=$1

	local tmpl_filename="descriptor-${package_type}.json.tmpl"
	local input_file="${ROOT_DIR}/script/${tmpl_filename}"
	local output_file="${DEST_DIR}//${tmpl_filename%.tmpl}"

	sed -e "s/@DATE@/${DATE}/g" \
	    -e "s/@PKG_VERSION@/${TRAVIS_TAG}/g" \
	    -e "s/@BINTRAY_USER@/${BINTRAY_USER}/g" \
	    ${input_file} > ${output_file}
}

export ROOT_DIR=$(cd $(dirname $0)/..; pwd)
export DEST_DIR="${ROOT_DIR}/bintray"
export DATE=$(date +%Y-%m-%d)

[[ -d "${DEST_DIR}" ]] || mkdir -p "${DEST_DIR}"

populate_descriptor_file deb
populate_descriptor_file rpm
populate_descriptor_file tar
