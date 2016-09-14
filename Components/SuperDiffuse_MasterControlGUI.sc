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

	fader{ | i |
		^m_controlFaders[i];
	}

	display {
		var win, layout;

		win = Window("SuperDiffuse | Master Control");
		layout = HLayout();

		if(m_controlFaders.size == 0)
		{
			m_numControls.do({ | i |
				var cf;
				cf = SuperDiffuse_ControlFader("/SuperDiffuse/Master/" ++ (i+1));
				m_controlFaders.add(cf);
				layout.add(cf.gui);
			});
		}
		{
			m_controlFaders.do({| cf | layout.add(cf.gui); });
		};

		win.layout_(layout);

		win.onClose_({
			/*
			**  NOTE: Not sure we want to do this anymore - depending on the GUI existing isn't the best
			*/
			//m_controlFaders.do(_.clear);
			//MIDIIn.disconnectAll;
		});

		win.front;
	}
}