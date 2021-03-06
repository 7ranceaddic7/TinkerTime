TinkerTime
==========

Sorry, I am no longer supporting Tinker Time.  I stopped playing KSP soon after the initial release of TinkerTime, and I have been finding that the quality of TinkerTime has been suffering greatly as a result of my reduced interest and lack of testing.

The Ultimate Personal Mechanic for your KSP Mods

Master: [![Build Status](https://travis-ci.org/oharaandrew314/TinkerTime.svg?branch=master)](https://travis-ci.org/oharaandrew314/TinkerTime)

### Description
Tinker Time is a Mod Manager for Kerbal Space Program that will allow you to automatically update, enable, and disable all of your mods.  All you have to do is enter the URL to the mod webpage, and Tinker Time will do the rest of the work for you.  It will even manage your ModuleManager Installation for you. :)

### Requirements
- [Java 7](https://java.com/en/download/index.jsp) or better

#### Supported Mod Hosting Sites
- KerbalStuff (reccomended)
- Curse
- Github (not reccomended)




### Downloads
Please download the latest version from [KerbalStuff](https://kerbalstuff.com/mod/243)

### Wiki
The [Tinker Time Wiki](https://github.com/oharaandrew314/TinkerTime/wiki) contains many useful resources with instructions and known issues.

### Special Thanks
- Contributors ([apemanzilla](https://github.com/apemanzilla), [grossws](https://github.com/grossws))

- Beta Testers ([foonix](https://github.com/foonix), [jcsntoll](https://github.com/jcsntoll), and [apemanzilla](https://github.com/apemanzilla))

### Change Log

##### v2.0.1

- User Exceptions will now be logged to file, and the user will be given a link to open them
- Fix OpenJDK compatability issue with image encoding
- Fix issue where local mods could not be saved
- Fix error where user could not select a new mod by right-clicking it after deleting a mod
- Throw user exception at startup if database connection cannot established

##### v2.0

This Major Update converts TinkerTime to use the pure-java H2 Database engine. TinkerTime will now run much faster, as more data can be cached, rather than constantly analyzing the mod zip files.

###### LEGACY BREAKING CHANGES:

You will need to re-install all your mods. Fortunately, you can import your legacy TinkerTime-mods.json files, which are stored in Kerbal Space Program/TinkerTime. This will help make for a smoother transition. In the future, the database will be automatically migrated for changes.

- New Features
  - Faster due to the H2 database engine migration
    - you may notice that mod info in the right panel switches far faster than before
  - Improved KSP Installation Management
    - TinkerTime will now keep track of multiple KSP installations, and can switch them on the fly
  - You can now filter your mods with a text filter
  - New Splash screen on startup
  - New App Icons based on splash screen
  - You can now import mod URLs from a plain-text file
  - You can now export all of your mods, rather than just enabled ones
  - Updated mods will now be automatically re-installed if they were before updating

- Bug Fixes
  - Fix slowdown anbd freezing during long use
    - Reduced Memory Consumption in this case, too
  - Links to KerbalStuff mods will no longer go to the API page

- Other interesting points
  - app is now packaged in io.andrewohara.tinkertime
  - Much deeper dependency injection migration for easier maintenance and development
  - Major refactor in an attempt to increase maintainability
  - Mod Images and readme text is now stored directly in the database

##### v1.4.5
- Fix first-time startup error related to empty GameData Path

##### v1.4.4
- Fix App Auto-Updater

##### v1.4.3
- Fix Mod-List Scrollbar not appearing
- Remove Github Fallback crawler (used when API limit exceeded)
- Fix GameLauncher not including launch arguments
- Fix JenkinsCrawler to get and install correct artifact
- Various refactorings, including migration to Google Guice Framework

##### v1.4.2
- Remove option to use 64-bit KSP on Windows (since it was removed in 1.0)

##### v1.4.1
- Fix error related to mod version parsing
- Fix error when trying to check for updates when local mod is installed

##### v1.4

###### New Features
- You can now drag and drop URL icons from your browser and files into the mod list to add them
- The Config Window has been visually updated
- Task progress will now appear next to their respective mods as spinners
  - New mods will appear in the list as they are being added
  - The Lower Progress bars have been removed
- The "Enter" and "Delete" keys will now toggle and delete mods in the list
- Tooltips have been added to mods in the list, explaining their current state
- The Mod Image View has been moved to the right panel
- UserVoice support will be reitred.  Support is now done through tinkertime at andrewohara dot io

###### Fixes
- Pressing cancel while selecting a github asset will no longer delete the zip
- Fix Regression where user would be asked to select github asset when they are just checking for updates

##### v1.3

This update focuses on fixing annoyances, in order to provide a better general UX.
A major refactor was done, reducing lines of code by an estimated 25%.

###### LEGACY BREAKING CHANGES:
- All configuration and mod data will not carry over to this version
  - You can export your mods from the old version, and import them into the new (but this is not fully tested)

- New Features
  - Add an options field to set KSP Launch Options
  - Updating the options will no longer require an app restart
  - Now scans the GameData directory to see which mods are enabled
  - Now scrapes Github using the API, by default
    - If the API limit is reached, will fallback to the HTML scraper
  - Can now parse versions of mods and use those when checking for updates
  - Progress bars will now appear immediately after launching a task, and their max progress will be set once it has been determined afterwards
  - If an update for a mod is available, the "update" icon will not go away when you restart TinkerTime
- Fixes
  - Mod file caches between KSP installations are now separate
    - This fixes issues when updating or deleting a mod in one installation, which would then affect the other
    - Caches are now stored in a "TinkerTime" directory within your KSP installation
  - Options Window to have correct number of rows
  - Right-click menu working on OSX due to removal of Nimbus UI
  - Jenkins Crawler should always get latest version (pertinent to ModuleManager)
- Other Changes
  - Removed Nimbus UI
  - Tinker Time now updates itself using KerbalStuff, rather than Github
  - ModuleManager has been further integrated as a mod, (as opposed to a special entity)
  - Mod list file no longer needs to save image URLs and enabled state
  - Refactor Unit Test resource loading
  - Major general refactor

##### v1.2
- New Features
  - Automatically check for TinkerTime update, and prompt user if new version is available
  - Mod Page Caching to now persist for 10 minutes (rather than just for a single workflow)
- Fixes
  - Update Github Crawler to match new DOM
  - Github Crawler to skip latest release if it does not contain a user-uploaded asset
  - Fix issue where illegal file characters were causing some mods to fail to download
  - "Update All" button to only update mods which have an update available

##### v1.1.1
- Updated for KerbalStuff content-provider change

##### v1.1
- New Features
  - KSP Launcher (supports Windows, OSX, and Linux)
    - On Windows, will ask user if they wish to use 64-bit, otherwise, automatic
  - Add Mod Import/Export Functionality (to share mod packs)
  - Add option to set windowed mode of KSP to borderless
  - Mod Host and Version is now displayed in Mod View
  - Support for Github Mods with multiple assets per release
- Fixes
  - Overhauled Zip Archive Analysis for more accurate mod installations
  - Set maximum size for Mod Image preview
  - Fix Typo in TinkerTime Options Window
  - Fix highlighting of toolbar buttons after clicking

##### v1.0
- New Features:
  - Module Manager is now considered a (non-removable) mod
  - Can now add local mod zip files (non-updateable)
  - Now fully using glyphicons (license in about)
  - Windows are now centered in the screen
  - Button to export JSON data for enabled mods
  - Will now delete old image caches and mods
  - Tinker Time Updater is simplified
  - When mod update is available, add message to mod description
- Fixes:
  - Fixed issue where variations of supported domains were not supported. e.g. beta.kerbalstuff.com
  - Fixed issue where mod list would not always load on startup
  - Fixed an issue where a module with a dependent mod would not be disabled even
    if the dependent mod was disabled
  - Improve Github mod URL analysis robustness
  - Fix Json caching for Jenkins crawler (less annoyance for [sarbian](https://github.com/sarbian))

##### v0.7
- KerbalStuff Mod Support
- Graphical Toolbar
- New GUI Theme (Nimbus)
- Overhauled Mod Archive Inspector (better mod compatability)
- Supported KSP Version will be disaplyed for each mod
- Automated JAR file generation with Gradle
- Travis-CI integration for automated testing

##### v0.6
- Github Mod Support
- GUI Mod List is to maintain proper order
- on start, update Module manager, and check for Mod updates
  - Can be Disabled through new Options Window
- Improvements to Mod Archive Inspector
- Improvements to Background Task Processor
- Each KSP Installation to have its own separate mods configuration
- Various other under-the-hood improvements

##### v0.5
- Initial Release
- Curse.com support
