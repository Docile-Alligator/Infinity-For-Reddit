#!/bin/bash

echo "Infinity for Reddit Docker Builder"

./scripts/fix-api-key.sh

wget https://github.com/TanukiAI/Infinity-keystore/raw/main/Infinity.jks

python3 ./scripts/setup-keystore.py

./gradlew assembleRelease
