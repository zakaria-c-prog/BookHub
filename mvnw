#!/usr/bin/env bash
set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAVEN_VERSION=3.9.9
DIST_DIR="$DIR/.mvn/dist"
MAVEN_HOME="$DIST_DIR/apache-maven-$MAVEN_VERSION"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$DIST_DIR"
  echo "Downloading Apache Maven $MAVEN_VERSION ..."
  curl -sS -o "$DIST_DIR/maven.zip" "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
  unzip -q -o "$DIST_DIR/maven.zip" -d "$DIST_DIR"
  rm "$DIST_DIR/maven.zip"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
