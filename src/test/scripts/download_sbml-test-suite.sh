#!/bin/bash
#################################################
# Download sbml-test-suite for testing
#
# usage:
#  source ./download_sbml-test-suite.sh
#################################################

set -eo pipefail

# This URL gives the updated SBML test cases from SBML Test Suite repository
SBML_TEST_SUITE_LINK="https://github.com/sbmlteam/sbml-test-suite/branches/develop/cases"

_CWD="$PWD"
TEST_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# download and extract the bigg models for testing
rm -rf $TEST_DIR/../resources/sbml-test-suite
mkdir -p $TEST_DIR/../resources/sbml-test-suite
cd $TEST_DIR/../resources/sbml-test-suite
svn checkout $SBML_TEST_SUITE_LINK
cd cases
rm NEWS.md

# set environment variable
export SBML_TEST_SUITE_PATH=${TEST_DIR}/../resources/sbml-test-suite/cases/semantic/

echo $SBML_TEST_SUITE_PATH

# move back
cd $_CWD
