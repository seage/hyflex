#!/bin/bash

cd $(dirname "$0")

#Build the project
mvn -f ../pom.xml clean package