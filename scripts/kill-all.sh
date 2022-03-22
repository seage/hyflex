#!/bin/bash

ps xau | grep bash | grep run-all | awk '{print $2}' | xargs kill -9
ps xau | grep java | grep hyflex  | awk '{print $2}' | xargs kill -9
