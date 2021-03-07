#!/bin/bash

cd $(dirname "$0")
cd ..

#Build the project
mvn -f ./pom.xml clean package