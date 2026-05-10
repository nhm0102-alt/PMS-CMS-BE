#!/usr/bin/env sh
set -eu

APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"

if [ -z "${JAVA_HOME:-}" ]; then
  echo "ERROR: JAVA_HOME is not set" >&2
  exit 1
fi

JAVA_EXE="$JAVA_HOME/bin/java"
if [ ! -x "$JAVA_EXE" ]; then
  echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
  exit 1
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: Gradle Wrapper JAR not found: $CLASSPATH" >&2
  exit 1
fi

exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
