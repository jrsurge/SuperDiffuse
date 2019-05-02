# SuperDiffuse Change Log

## Known issues
[1] Moving control faders (MIDI) while a piece is loading will lock the interface, forcing a restart of SuperDiffuse. This appears to be a SuperCollider/Qt issue that can't really be addressed

## Version 1.4.0
### New features
* Added meter bridge to GUI (post-master fader)
* Added Ctrl+D for duplicating Matrices
* Added Ctrl+D for duplicating FilterSets
* Added auto-save. If you've saved the concert once (or loaded it), any changes that would require a save now automatically trigger a resave:
    * Adding/Removing/Swapping/Editing Pieces
    * Adding/Removing/Editing Matrices
    * Adding/Removing/Editing Filter Sets
    * Setting Master Fader
    * Setting Control Faders
    * Setting MIDI Config

### Modifications
* Changed Master Fader to behave exponentially (like control faders). The numberbox values are linear, but they apply exponentially.

### Fixes
* Fixed a bug where selecting a Matrix and pressing play would start the piece at the matrix index
* Fixed an issue with the Clock displaying decimals on newer SuperCollider versions (backwards compatible)
* Reduced the number of calls to ControlFader GUI updates on value changes (might help with Known Issue [1])

## Version 1.3.0
### New Features
* Added configurable input (pre-routing matrix) and output (post-routing matrix) filters

## Version 1.2.0
### Modifications
* Changed faders to behave uniformly (regardless of control from MIDI, OSC or GUI)
* Added GPLv3 license

## Version 1.1.1
### Fixes
* Minor patch to fix issues with machine specific keyboard shortcuts (now uses Qt keycodes)

## Version 1.1.0
* First public release. Thanks to Adrian Moore, Adam Stanovic, Jonty Harrison, Denis Smalley, and the composers at USSS for their support and invaluable thoughts
