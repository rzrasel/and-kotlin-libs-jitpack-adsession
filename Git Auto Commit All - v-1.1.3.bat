@echo off
setlocal enabledelayedexpansion

REM Get current branch name
for /f "delims=" %%b in ('git rev-parse --abbrev-ref HEAD') do set branchName=%%b

:loop
echo Current Branch: [%branchName%]
set /p input=Enter Commit Text: 
if "%input%"=="" (
    echo.
    echo You must enter a commit message!
    echo.
    goto loop
)

REM Get timestamp
set date=%DATE%
set time=%TIME%

REM Format time as H:mm:ss
REM Extract hour, minutes, and seconds, handling the hour being a single digit
set hour=%time:~0,2%
set minute=%time:~3,2%
set second=%time:~6,2%

REM If hour is a single digit, remove the leading space
if "%hour:~0,1%"==" " set hour=0%hour:~1,1%

set hms=%hour%:%minute%:%second%

set message=%input% - %date% %hms%

echo.
echo Staging files...
git add .

echo Committing to branch [%branchName%]: "%message%"
git commit -m "%message%"

echo Pulling latest changes...
git pull --all

echo Pushing to all remotes...
git push --all

echo.
echo Commit complete on branch [%branchName%]
echo Press Enter to exit...
pause >nul