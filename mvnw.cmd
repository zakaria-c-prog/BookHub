@echo off
setlocal
set MAVEN_VERSION=3.9.9
set DIST_DIR=%~dp0.mvn\dist
set MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Downloading Apache Maven %MAVEN_VERSION% ...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force -Path '%DIST_DIR%' | Out-Null; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%DIST_DIR%\maven.zip'; Expand-Archive -Path '%DIST_DIR%\maven.zip' -DestinationPath '%DIST_DIR%' -Force; Remove-Item '%DIST_DIR%\maven.zip'"
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
