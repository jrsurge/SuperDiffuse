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

		s = ((samples / sampleRate) % 60).floor;
		m = ((samples / sampleRate) / 60 % 60).floor;
		h = ((samples / sampleRate) / 60 / 60).floor;

		^"%:%:%".format(h.asString.padLeft(2,"0"),m.asString.padLeft(2,"0"),s.asString.padLeft(2,"0"));
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