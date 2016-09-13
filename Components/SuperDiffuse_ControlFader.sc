/*
**
**  ControlFader
**
**  ControlFader is an Observer Pattern wrapper around
**  a Slider (with a MIDI learn button).
**
**  The idea is to allow any number of OutFaders to subscribe to this ControlFader's
**  value - the GroupControlGUI will control which OutFader listens to which
**  ControlFader
**
**  The OutFader sends its value to a Control bus, owned by the Concert
**
*/

SuperDiffuse_ControlFader : SuperDiffuse_Subject {

	var m_layout, m_slider, m_midiLearnButton, m_midiFunc, m_oscFunc;

	*new { | oscAddressPattern |
		^super.new.ninit(oscAddressPattern)
	}

	ninit { | oscAddressPattern |
		m_layout = VLayout();
		m_slider = Slider().action_({this.notify;});
		m_midiFunc = MIDIFunc.cc({|val|  {m_slider.valueAction_(val/127)}.defer(0) });
		m_midiLearnButton = Button().states_([["Learn"]]).action_({ m_midiFunc.learn; });

		("Creating OSC Listener for: " + oscAddressPattern).postln;
		m_oscFunc = OSCFunc({|msg, time, addr, recvPort |
			{m_slider.valueAction_(msg[1])}.defer(0);
		}, oscAddressPattern.asSymbol);

		m_layout.add(m_slider);
		m_layout.add(m_midiLearnButton);
	}

	value {
		^m_slider.value;
	}
	value_{|v|
		m_slider.value(v);
	}
	valueAction_{|v|
		m_slider.valueAction_(v);
	}

	gui {
		^m_layout;
	}

	clear {
		m_midiFunc.clear.free;
		m_oscFunc.clear.free;
	}

}