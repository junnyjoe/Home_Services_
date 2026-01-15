@echo off
setlocal
set MAVEN_WRAPPER_DIR=%~dp0.mvn\wrapper
set MAVEN_WRAPPER_JAR=%MAVEN_WRAPPER_DIR%\maven-wrapper.jar
if exist "%MAVEN_WRAPPER_JAR%" (
  java -Dmaven.multiModuleProjectDirectory="%~dp0." -cp "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
) else (
  echo maven-wrapper.jar introuvable. Exécutez 'scripts\setup-maven-wrapper.ps1' pour le télécharger.
  exit /b 1
)
