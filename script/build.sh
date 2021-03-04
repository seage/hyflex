#!/bin/bash

#Pull the latest code from origin
git pull origin master

#Build the project
mvn clean package