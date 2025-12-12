# Android Library JitPack AdSession

[![](https://jitpack.io/v/rzrasel/and-kotlin-libs-jitpack-adsession.svg)](https://jitpack.io/#rzrasel/and-kotlin-libs-jitpack-adsession)

## Setup: and-kotlin-libs-jitpack-adsession

### Settings File (settings.gradle.kts):
```setup_settings_file
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
    }
}
```
```setup_settings_file
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
### Settings File - App Module (build.gradle.kts):
```setup_settings_file
dependencies {
    implementation("com.github.rzrasel:and-kotlin-libs-jitpack-adsession:VERSION")
}
```
```setup_settings_file
dependencies {
    implementation 'com.github.rzrasel:and-kotlin-libs-jitpack-adsession:VERSION'
}
```

### Code Setup:
```setup_AdSessionConfigModel
val adSessionConfig = AdSessionConfigModel(
    minTime1 = (2.0 * 60 * 1000).toLong(),
    minTime2 = (2.5 * 60 * 1000).toLong(),
    maxTime1 = (4.0 * 60 * 1000).toLong(),
    maxTime2 = (4.5 * 60 * 1000).toLong(),
    minEventCount1 = 10,
    minEventCount2 = 14
)

AppAdPreferences.init(
    context = this,
    prefName = "ad_mob_session_pref"
)

val adSessionViewModel: AdSessionViewModel = AdSessionViewModel()
AdMobSharedViewModel(adSessionViewModel, adSessionConfig)
```

### GIT Command
```git_command
git init
git remote add origin https://github.com/rzrasel/and-kotlin-libs-jitpack-adsession.git
git remote -v
git fetch && git checkout master
git add .
git commit -m "Add Readme & Git Commit File"
git pull
git push --all
git status
git status
```

### Git Rebase Squash Interactive
```git_command_rebase_squash_interactive
git rebase -i HEAD~2
i
-- squash/s
esc:
wq↵

i
esc:
wq↵

git rebase -i 4daac6b7
i
esc:
wq↵

i
esc:
wq↵

git push --force

git rebase -i --root
i
esc:
wq↵

i
esc:
wq↵

git push --force

//git push -f --set-upstream origin master
```

```PHP_DATE_TIME
echo date("D", (time() + 6 * 60 * 60)) . "day " . date("F j, Y, G:i:s", (time() + 6 * 60 * 60));
```

➕❌

[Learn Git Squash in 3 minutes // explained with live animations!](https://youtu.be/V5KrD7CmO4o)