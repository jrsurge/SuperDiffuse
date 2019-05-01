/*
**
**  Clock
**
**  This is a wrapper around a number of samples that can return a StaticText in hh::mm::ss
**  format for embedding in a Window
**
**  It can be ticked every sample, or multiple samples, but SuperDiffuse_ConcertGUI uses
**  setTimeInSamples to synchronise with the SoundFileView timeCursor.
**
*/

SuperDiffuse_Clock
{
	var samples, sampleRate;
	var display;

	*new {
		^super.new.init;
	}

	init {
		samples = 0;
		sampleRate = Server.default.sampleRate;
		this.initDisplay;
	}

	setSampleRate { | sr |
		sampleRate = sr;
	}

	initDisplay {
		display = StaticText().font_(Font(Font.defaultMonoFace,32)).string_(this.getString).align_(\center);
	}

	setTimeInSamples { | sampleOffset |
		samples = sampleOffset;
		this.updateGUI;
	}

	tick { | sampleOffset=1 |
		samples = samples + sampleOffset;

		this.updateGUI;
	}

	getString {
		var h, m, s;
		var totalSeconds = samples / sampleRate;

		s = (totalSeconds % 60).floor;
		m = (totalSeconds / 60 % 60).floor;
		h = (totalSeconds / 60 / 60).floor;

		^"%:%:%".format(h.asInt.asString.padLeft(2,"0"),m.asInt.asString.padLeft(2,"0"),s.asInt.asString.padLeft(2,"0"));
	}

	reset {
		samples = 0;

		this.updateGUI;
	}

	gui {
		if(display.isClosed)
		{
			this.initDisplay;
		}
		^display;
	}

	updateGUI {
		{display.string_(this.getString)}.defer;
	}
}