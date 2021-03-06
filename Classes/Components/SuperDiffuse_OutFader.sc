/*
**
**  OutFader
**
**  This is part of the output level control system.
**
**  OutFader is an Observer Pattern wrapper around
**  a Control bus - as such, it is an 'invisible'
**  fader.
**
**  The idea is that the value of the observed
**  ControlFader is used to set the control bus
**  owned by the Concert.
**
*/

SuperDiffuse_OutFader : SuperDiffuse_Observer {
	classvar <dummySubject;
	var m_controlBus;

	*new { | controlFader, controlBus |
		^super.new(controlFader).ninit(controlBus);
	}

	*initClass {
		dummySubject = SuperDiffuse_Subject();
	}

	ninit { | controlBus |
		m_controlBus = controlBus;
	}

	update { | subject |
		if(subject === m_subject)
		{
			m_controlBus.value_(m_subject.value);
		};
	}

	index {
		^ m_controlBus.index;
	}
}