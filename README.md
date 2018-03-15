# SuperDiffuse
SuperDiffuse is an n-channel diffusion system, written in SuperCollider.

It was developed as part of research at The University of Sheffield Sound Studios, and to facilitate Sound Junction: a weekend festival of electroacoustic music at The University of Sheffield.

Based on the idea of concerts, SuperDiffuse allows pieces to be added, removed, and swapped. As concerts can often contain a variety of playback formats (Stereo, 5.1, 7.1, 8, 16, 24, 32...), SuperDiffuse allows pieces to be associated with their own routing, using a patchbay-style system.

Starting it is a matter of describing how big your system is:

```supercollider
// 8 playback channels, 8 output channels, 8 control faders
SuperDiffuse(8,8,8);
```

# Installation

__SuperDiffuse uses SuperCollider, so you must have it installed first:__
https://supercollider.github.io

## Automatic:
SuperDiffuse is written as a Quark, so __if you have Git installed__ (Windows users will need to manually install Git), run the following in SuperCollider:

```supercollider
Quarks.install("https://github.com/jrsurge/SuperDiffuse")
```

Then restart SuperCollider (or just recompile the class library with `Ctrl/Cmd + Shift + L`).

## Manual:
* Download a release from Github:
https://github.com/jrsurge/SuperDiffuse/releases

* Unzip into a folder called `SuperDiffuse` (doesn't matter where, we'll move it soon)

* In SuperCollider, go to: __File > Open user support directory__

* If it doesn't already exist, make a folder called `Extensions`

* Move the `SuperDiffuse` folder into the `Extensions` folder

Then restart SuperCollider (or just recompile the class library with `Ctrl/Cmd + Shift + L`)

# Running

## Pre-flight checks
Make sure your SuperCollider is setup to use the number of output channels you need, so something like this in your startup file:
```supercollider
// connect to your soundcard
//  all connected devices are listed on booting
//  the server if you need to know what goes here
s.options.device_("ASIO : ASIO Fireface");

// set the number of output channels on the card:
s.options.numOutputBusChannels_(8);

// boot the server
s.boot;
```

## Takeoff
SuperDiffuse needs to know how big your system is, but that's it.

* The maximum number of channels for playback into the system
* Number of channels to output
* Number of control faders (MIDI or OSC)

So, if the biggest piece you're playing back is 8 channels, then your number of input channels is 8:

```supercollider
// numIns, numOuts, numControls
SuperDiffuse(8,8,8);
```

This will fail if you don't have the number of output channels you're requesting.

From there, it's all in the GUI - see the tutorial series in the documentation for more information (accessible from the SuperCollider Help system, Browse > Quarks > SuperDiffuse)
