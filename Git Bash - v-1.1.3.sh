#!/bin/bash

# Get current branch name first
branch=$(git rev-parse --abbrev-ref HEAD)
echo "Current Branch: $branch"

# Loop until the user provides a nonempty input
while true; do
    read -p "Enter Commit Text: " input

    # Check if input is empty
    if [[ -z "$input" ]]; then
        echo "You must enter a commit message!"
    else
        break
    fi
done

# Get the current date and time
dateTime=$(date "+%Y%m%d %H:%M:%S")

# Prepare the commit message
message="$input  $dateTime"

# Show the commit message
echo "Commit message: $message"

# Perform the git operations
git add .
git commit -m "$message"
git pull --all
git push --all

# Completion message
echo "Process Complete on branch: $branch"
read -p "Press Enter to continue..."