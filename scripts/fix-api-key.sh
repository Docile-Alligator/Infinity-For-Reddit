#!/bin/bash

echo "Custom API key: $CUSTOM_API_KEY"
echo "Custom UA: $REDDIT_USER"

if [ -z "$CUSTOM_API_KEY" ]
then
  echo "No custom API key, just building..."
else
  echo "Custom API key detected, setting it up along the custom UA..."

  sed -i "s/NOe2iKrPPzwscA/$CUSTOM_API_KEY/" app/src/main/java/ml/docilealligator/infinityforreddit/utils/APIUtils.java
  sed -i "s|infinity://localhost|http://127.0.0.1|" app/src/main/java/ml/docilealligator/infinityforreddit/utils/APIUtils.java
  sed -iE "s/public static final String USER_AGENT = \"(.*)\";/public static final String USER_AGENT = \"android:personal-app:0.0.1 (by \/u\/$REDDIT_USER)\";/" app/src/main/java/ml/docilealligator/infinityforreddit/utils/APIUtils.java
fi
