#!/bin/bash

cd $(dirname "$0")/..

#Build the project
./gradlew clean installDist