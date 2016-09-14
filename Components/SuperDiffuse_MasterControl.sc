SuperDiffuse_MasterControl {

	var m_numControls, m_controlFaders;
	var m_layout;

	*new { | numControls |
		^super.new.init(numControls);
	}

	init { | numControls |
		MIDIIn.connectAll;
		m_numControls = numControls;
		m_controlFaders = List();
		this.prInitControlFaders;
	}

	prInitControlFaders {
		m_numControls.do({ | i |
			var cf;
			cf = SuperDiffuse_ControlFader("/SuperDiffuse/Master/" ++ (i+1));
			m_controlFaders.add(cf);
		});
	}

	fader{ | i |
		^m_controlFaders[i];
	}

	gui {
		m_layout.free;
		m_layout = HLayout();

		m_controlFaders.do({| cf | m_layout.add(cf.gui); });

		^m_layout;
	}

	clear {
		m_controlFaders.do(_.clear);
	}
}