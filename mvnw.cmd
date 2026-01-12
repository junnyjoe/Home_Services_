@echo off
setlocal
set MAVEN_WRAPPER_DIR=.mvn\wrapper
set MAVEN_WRAPPER_JAR=%MAVEN_WRAPPER_DIR%\maven-wrapper.jar
if exist "%MAVEN_WRAPPER_JAR%" (
  java -jar "%MAVEN_WRAPPER_JAR%" %*
) else (
  echo maven-wrapper.jar introuvable. Exécutez 'scripts\setup-maven-wrapper.ps1' pour le télécharger.
  exit /b 1
)
