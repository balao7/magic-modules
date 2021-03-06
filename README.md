# Magic Modules
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/) ![Language](https://img.shields.io/github/languages/top/dotanuki-labs/magic-modules?color=blue&logo=kotlin) [![Maintainability](https://api.codeclimate.com/v1/badges/ee4523261a7d56910bd6/maintainability)](https://codeclimate.com/github/dotanuki-labs/magic-modules/maintainability) [![codebeat badge](https://codebeat.co/badges/e8dc9d35-8fff-40a5-9568-eec7a33342c8)](https://codebeat.co/projects/github-com-dotanuki-labs-magic-modules-master) ![Main](https://github.com/dotanuki-labs/magic-modules/workflows/Main/badge.svg) [![](https://jitpack.io/v/dotanuki-labs/magic-modules.svg)](https://jitpack.io/#dotanuki-labs/magic-modules) ![License](https://img.shields.io/github/license/dotanuki-labs/magic-modules.svg)

![](.github/assets/magicmodules-demo.gif)

## What is this?

> *Read more in the [blog post](https://ubiratansoares.dev/post/an-experiment-around-gradle-modules-and-settings/)*

For large Android projects hosted in mono repos, management for module names might be a real pain, specially when we have lots of moving parts under a structure driven by nested Gradle subprojects.

This experimental plugin attemps to solve that. It parses a project tree like this

```
.
├── app
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           └── res
├── build.gradle
├── buildSrc
│   ├── build.gradle.kts
│   └── src
│       └── main
│           └── kotlin
├── common
│   ├── core
│   │   ├── build.gradle
│   │   └── src
│   │       └── main
│   └── utils
│       ├── build.gradle.kts
│       └── src
│           └── main
├── features
│   ├── home
│   │   ├── build.gradle
│   │   └── src
│   │       └── main
│   └── login
│       ├── build.gradle
│       └── src
│           └── main
|
|
└── settings.gradle

```

and

- it automatically includes all founded modules in `settings.gradle`
- it writes 2 Kotlin files under your `buildSrc/src/main/kotlin` : 

`Libraries.kt`

```kotlin
// Generated by MagicModules plugin. Mind your Linters!
import kotlin.String
import kotlin.collections.List

object Libraries {
    const val FEATURES_HOME: String = ":features:home"

    const val FEATURES_LOGIN: String = ":features:login"

    const val COMMON_CORE: String = ":common:core"

    const val COMMON_UTILS: String = ":common:utils"

    val allAvailable: List<String> = 
            listOf(
                FEATURES_HOME,
                FEATURES_LOGIN,
                COMMON_CORE,
                COMMON_UTILS
            )
}
```

`Applications.kt`

```kotlin
// Generated by MagicModules plugin. Mind your Linters!
import kotlin.String
import kotlin.collections.List

object Applications {
    const val APP: String = ":app"

    val allAvailable: List<String> = 
            listOf(
                APP
            )
}
```
In this way, refactors around the project structure will become a bit easier, since `build.gradle` configuration

```groovy
dependencies {
    implementation project(Libraries.COMMON_UTILS)
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    implementation ...
}
```

will break if `common/utils` moves around. The new constant under `buildSrc` will be mapped and will be ready to use.

We can also add all the libraries to an monotlithic-like `app` easily

```groovy
dependencies {
    Libraries.allAvailable.each { implementation project(it) }
}
```

## Setup

To try this plugin out, you can grab a snapshot build from Jitpack. Add this snippet in your `settings.gradle` file

```groovy
buildscript {
    repositories {
        mavenCentral()	
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        classpath 'com.github.dotanuki-labs:magic-modules:<plugin-version>'
    }
}

apply plugin: "io.labs.dotanuki.magicmodules"

```

and remove all `include` statements

```groovy
include 'app'
include 'featureA'
include 'featureB'
include 'featureC'
include ...

```
They are not needed anymore.

If your project uses a multi-application layout, with standalone apps for your features/screens, you can opt-in to not include all `com.android.application` modules in order to reduce configuration times locally and eventually build times on CI.

```groovy
rootProject.name='awesome-project'

apply plugin: "io.labs.dotanuki.magicmodules"

magicModules {
    includeApps = false
}

include ':app'

```

## Matching Gradle build files

This plugin walks your project tree and inspect all the `build.gradle` and `build.gradle.kts` files in order to learn if each founded module matches an Android library, a JVM library or an Android application. This means that `Magic Modules` is sensitive on how you apply plugins in your Gradle build scripts, for instance using

```groovy
apply plugin: 'com.android.library'
```

or 

```kotlin
plugins {
    kotlin("jvm")
}
```

This plugin does a best-effort attempt in order to catch all the common cases, but it might not work at all if you 

- (1) have some strategy to share build logic accross Gradle modules and 
- (2) applied the `application` or `library` plugin using such shared build logic for your modules

## Building and testing

To build this plugin and publish it locally for testing

```bash
./gradlew publishToMavenLocal
```

To run all the checks, including integration tests

```bash
./gradlew ktlintCheck test
```

To check logs generated by this plugin and learn how this plugin works, we have a sample project available

```bash
cd sample
./gradlew clean app:assembleDebug --info | grep MagicModulesPlugin
```

## Limitations

The main limitation I've found with this approach is that - right now - the plugin generates the `Libraries.kt` and `Applications.kt` under the main source set of `buildSrc`, which means eventually issues with linters that run for `buildSrc` files.

I need more time in order to figure out if we can have such generated files under `buildSrc/build` somehow.

## Further work

I realised that

- It might be useful to configure the output folder/package for `Libraries.kt` and `Applications.kt`
- It might be useful to grab more Gradle build script matchers using the plugin configuration

## Author

Coded by Ubiratan Soares (follow me on [Twitter](https://twitter.com/ubiratanfsoares))

## License

```
The MIT License (MIT)

Copyright (c) 2020 Dotanuki Labs

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```