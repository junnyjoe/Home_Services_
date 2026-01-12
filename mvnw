#!/usr/bin/env sh
# ----------------------------------------------------------------------------
# Maven wrapper script (lightweight)
# ----------------------------------------------------------------------------
MAVEN_WRAPPER_DIR=".mvn/wrapper"
DEFAULT_JAR="$MAVEN_WRAPPER_DIR/maven-wrapper.jar"
if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME=""
fi

if [ -f "$DEFAULT_JAR" ]; then
  java -jar "$DEFAULT_JAR" "$@"
else
  echo "maven-wrapper.jar introuvable. Exécutez 'scripts/setup-maven-wrapper.ps1' sous Windows ou téléchargez le jar manuellement." >&2
  exit 1
fi
