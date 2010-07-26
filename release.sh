#!/bin/bash

# This script releases a new version of ExpectJ
set -e
set -x

# Hello Sourceforge shell services
ssh johanwalles,expectj@shell.sf.net create

# The "-P release" is required because of MRELEASE-459
mvn -P release release:prepare
mvn -P release release:perform
