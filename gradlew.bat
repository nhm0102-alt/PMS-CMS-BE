@echo off
setlocal

set APP_HOME=%~dp0
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

if not defined JAVA_HOME (
  echo ERROR: JAVA_HOME is not set
  exit /b 1
)

set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_EXE%" (
  echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
  exit /b 1
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
  echo ERROR: Gradle Wrapper JAR not found: %CLASSPATH%
  exit /b 1
)

"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
