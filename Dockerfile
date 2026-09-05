FROM ubuntu:latest

RUN apt update && apt upgrade -y
RUN apt install -y openjdk-11-jdk wget unzip
RUN wget --quiet --output-document=android-sdk.zip https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip
RUN unzip -q android-sdk.zip -d /tmp/android-sdk

ENV ANDROID_SDK_ROOT="/tmp/android-sdk"
ENV JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"
ENV PATH="${PATH}:/tmp/android-sdk/tools/bin:/tmp/android-sdk/platform-tools"

RUN yes | /tmp/android-sdk/cmdline-tools/bin/sdkmanager  --sdk_root=$ANDROID_SDK_ROOT "platforms;android-30" "build-tools;30.0.3"

ENV CUSTOM_API_KEY=""
ENV REDDIT_USER=""

COPY . /content
WORKDIR /content

ENTRYPOINT [ "scripts/start-compilation.sh" ]
