SuperDiffuse_MasterControlGUI {

	var m_numControls, m_controlFaders;

	*new { | numControls |
		^super.new.init(numControls);
	}

	init { | numControls |
		MIDIIn.connectAll;
		m_numControls = numControls;
		m_controlFaders = List();
		this.display;
	}

	display {
		var win, layout;

		win = Window("SuperDiffuse | Master Control");
		layout = HLayout();

		m_numControls.do({ | i |
			layout.add(SuperDiffuse_ControlFader("/SuperDiffuse/Master/" ++ (i+1)).gui);
		});

		win.layout_(layout);

		win.onClose_({m_controlFaders.do(_.clear)});

		win.front;
	}
}