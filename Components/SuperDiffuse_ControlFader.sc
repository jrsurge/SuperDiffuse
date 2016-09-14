/*
**
**  ControlFader
**
**  ControlFader is an Observer Pattern wrapper around
**  a Slider (with a MIDI learn button).
**
**  This may be misleading as a ControlFader isn't dependant on its GUI.
**  It's designed to function like a Slider, and can return a GUI if requested.
**
**  The idea is to allow any number of OutFaders to subscribe to this ControlFader's
**  value - the GroupControlGUI will control which OutFader listens to which
**  ControlFader
**
**  The OutFader sends its value to a Control bus, owned by the Concert
**
*/


SuperDiffuse_ControlFader : SuperDiffuse_Subject {

	var  m_value, m_layout, m_slider, m_midiLearnButton, m_midiFunc, m_oscFunc;

	*new { | oscAddressPattern |
		^super.new.ninit(oscAddressPattern)
	}

	ninit { | oscAddressPattern |

		m_value = 0;

		this.prInitGUI;
		this.changeOSC(oscAddressPattern);

	}

	value {
		^m_value;
	}

	value_{|v|
		m_value = v;
		AppClock.sched(0,{m_slider.value_(v)});
	}

	valueAction_{|v|
		this.value_(v);
		this.action;
	}

	learn {
		m_midiFunc.free;
		m_midiFunc = MIDIFunc.cc({|val|  this.valueAction_(val/127); });
		m_midiFunc.learn;
	}

	changeOSC { | oscAddressPattern |
		m_oscFunc.free;
		("Creating OSC Listener for: " + oscAddressPattern).postln;
		m_oscFunc = OSCFunc({|msg, time, addr, recvPort |
			this.valueAction_(msg[1]);
		}, oscAddressPattern.asSymbol);
	}

	action {
		this.notify;
	}

	prInitGUI {
		m_layout.free;
		m_slider.free;
		m_midiLearnButton.free;

		m_layout = VLayout();
		m_slider = Slider().action_({|v| this.valueAction_(v.value)}).value_(this.value);
		m_midiLearnButton = Button().states_([["Learn"]]).action_({ this.learn; });
		m_layout.add(m_slider);
		m_layout.add(m_midiLearnButton);
	}

	gui {
		/*
		**  GUI parenting leads to deletion if parent View destroyed
		**  If we've been deleted, reinit
		*/
		if(m_slider.isClosed == true)
		{
			this.prInitGUI;
		};
		^m_layout;
	}

	clear {
		m_midiFunc.clear.free;
		m_oscFunc.clear.free;
	}

}