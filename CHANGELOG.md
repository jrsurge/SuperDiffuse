# SuperDiffuse Change Log

## Known issues
- [1] Moving control faders (MIDI) while a piece is loading will lock the interface, forcing a restart of SuperDiffuse. This appears to be a SuperCollider/Qt threading issue.
    * This may have been addressed in newer SC versions(?)
- [2] Using the mouse-wheel in the Edit Matrix window when it has scrollbars can cause trouble. The NumberBox doesn't steal the mouse properly on mouse-wheel, so the scroll event propagates to the ScrollArea. __This has been fixed in SuperCollider 3.10.3, when using SuperDiffuse v1.5.0__ .

## Version 1.5.0
### New Features
* Added ability to import from other concerts. Using the "Import.." button, you can now import:
  * MIDI Configuration
  * Control Configuration
  * Matrices
  * Filter Sets

  __NOTE__: Importing MIDI or Control Configurations will replace those in the current concert. Importing cannot be undone.

### Fixes
* Reduced number of autosaves when moving master fader - only saves on mouseUp
* Applied fix for known issue [2] for SC versions >= 3.10.3
  * __NOTE__: Requires SuperCollider >= 3.10.3
* The save button now refocuses on SoundFileView properly
* Fixed an issue with the Edit Matrix and Control Fader Configuration windows that caused multiple labels to be drawn for each item.

## Version 1.4.1
### Fixes
* Patch to fix an issue where removing a FilterSet would result in pieces that used it accessing the deleted FilterSet. Pieces now reset to the default FilterSet when their FilterSet is removed.

## Version 1.4.0
A big usability update. This version __requires SuperCollider >= 3.9__.

Core functionality is the same, but lots of new features to make things easier. Thanks to everyone who has used the system so far and provided invaluable feedback. In particular, this version features additions requested by Adrian Moore, Adam Stanovic, Hans Tutsku, and David Berezan; thank you all.

### New features
* Added meter bridge to GUI (post-master fader)
  * __NOTE__: requires SuperCollider >= 3.9.3 to function
* Added Ctrl+D for duplicating Matrices.
* Added Ctrl+D for duplicating FilterSets.
* Added auto-save. If you've saved the concert once (or loaded it), any changes that would require a save now automatically trigger a resave:
    * Adding/Removing/Swapping/Editing Pieces
    * Adding/Removing/Editing Matrices
    * Adding/Removing/Editing Filter Sets
    * Setting Master Fader
    * Setting Control Faders
    * Setting MIDI Config
* Added 'Hide Waveform' button. This removes the waveform and the playhead from the soundfile view, leaving only a black box. When hidden, the soundfile view doesn't respond to mouse clicks, so you can't change the playback position while hidden (keyboard still responds, so start/stop and returning to beginning remain possible).

### Changes
* Changed Master Fader to behave exponentially (like control faders). The numberbox values are linear, but they scale exponentially - beware values above 1!
* Lock Interface now disables mouse interaction with the Soundfile View, offering a more coherent 'concert mode'.
* Lock Interface now requires two clicks to unlock, providing additional safety for concerts.
* GUI code refactor - the layout is now grid-based and compartmentalised. Apart from being easier to maintain, this means scaling is handled better. Widgets can also now be smaller than before, reducing screen real-estate requirements.
* The Pieces add/remove and up/down buttons have been swapped to mirror usual workflow: adding/removing pieces, _then_ swapping order.
* The main window title now includes the filename of the currently loaded concert.

### Fixes
* Fixed a bug where selecting a Matrix and pressing play would start the piece at the matrix index
* Fixed an issue with the Clock displaying decimals on newer SuperCollider versions (backwards compatible)
* Reduced the number of calls to ControlFader GUI updates on value changes (might help with Known Issue [1])

## Version 1.3.0
### New Features
* Added configurable input (pre-routing matrix) and output (post-routing matrix) filters

## Version 1.2.0
### Changes
* Changed faders to behave uniformly (regardless of control from MIDI, OSC or GUI)
* Added GPLv3 license

## Version 1.1.1
### Fixes
* Minor patch to fix issues with machine specific keyboard shortcuts (now uses Qt keycodes)

## Version 1.1.0
* First public release. Thanks to Adrian Moore, Adam Stanovic, Jonty Harrison, Denis Smalley, and the composers at USSS for their support and invaluable thoughts
