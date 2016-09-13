/*
**
**  OutFader
**
**  This is part of the output level control system.
**  The magic that actually sets the
**
**  OutFader is an Observer Pattern wrapper around
**  a Control bus.
**
**  The idea is that the value of the observered
**  ControlFader is used to set the control bus
**  owned by the Concert.
**
*/

SuperDiffuse_OutFader : SuperDiffuse_Observer {
	var m_controlBus;

	*new { | controlFader, controlBus |
		^super.new(controlFader).ninit(controlBus);
	}

	ninit { | controlBus |
		m_controlBus = controlBus;
	}

	update { | subject |
		if(subject === m_subject)
		{
			m_subject.value.postln;
			// controlBus.set(m_subject.value);
		};
	}
}