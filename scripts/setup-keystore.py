#!/usr/bin/python3

print("Setting up keystore file...")

build_gradle_file = "./app/build.gradle"
build_gradle_code = open(build_gradle_file, "r", encoding="utf-8-sig").read()
build_gradle_code = build_gradle_code.replace(r"""    buildTypes {""", r"""    signingConfigs {
        release {
            storeFile file("/content/Infinity.jks")
            storePassword "Infinity"
            keyAlias "Infinity"
            keyPassword "Infinity"
        }
    }
    buildTypes {""")
build_gradle_code = build_gradle_code.replace(r"""    buildTypes {
        release {""", r"""    buildTypes {
        release {
            signingConfig signingConfigs.release""")

with open(build_gradle_file, "w", encoding="utf-8") as f:
  f.write(build_gradle_code)
