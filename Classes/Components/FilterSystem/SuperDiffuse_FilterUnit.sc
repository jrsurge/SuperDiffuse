/*
FilterUnit

A filter unit describes and manages an in-place filtering synth.
It knows what bus to work on, and what group to add itself to.
It can be loaded and unloaded on request.

Designed to be used as part of a FilterSet

*/

SuperDiffuse_FilterUnit
{
	var in, group, <hpOn, <bpOn, <lpOn, <hpFreq, <bpFreq, <bpRq, <bpGain, <lpFreq;
	var m_active;
	var m_synth;

	*new { | in = 0, group = 0, hpOn = 0, bpOn = 0, lpOn = 0, hpFreq = 5, bpFreq = 1000, bpRq = 1, bpGain = 0, lpFreq = 15000 |
		^super.newCopyArgs(in, group, hpOn, bpOn, lpOn, hpFreq, bpFreq, bpRq, bpGain, lpFreq).init;
	}


	init {
		m_synth = nil;
		m_active = false;
	}

	active {
		^m_active;
	}

	active_ { | val = 0 |
		m_active = val;
	}

	hpOn_ { | val = 0 |
		hpOn = val;

		if(m_synth != nil)
		{
			m_synth.set(\hpOn, val);
		}
	}

	bpOn_ { | val = 0 |
		bpOn = val;

		if(m_synth != nil)
		{
			m_synth.set(\bpOn, val);
		}
	}

	lpOn_ { | val = 0 |
		lpOn = val;

		if(m_synth != nil)
		{
			m_synth.set(\lpOn, val);
		}
	}

	hpFreq_ { | freq = 5 |
		hpFreq = freq;

		if(m_synth != nil)
		{
			m_synth.set(\hpFreq, freq);
		}
	}

	bpFreq_ { | freq = 1000 |
		bpFreq = freq;

		if(m_synth != nil)
		{
			m_synth.set(\bpFreq, freq);
		}
	}

	bpRq_ { | rq = 1 |
		bpRq = rq;

		if(m_synth != nil)
		{
			m_synth.set(\bpRq, rq);
		}
	}

	bpGain_ { | gain = 0 |
		bpGain = gain;

		if(m_synth != nil)
		{
			m_synth.set(\bpGain, gain);
		}
	}

	lpFreq_ { | freq = 15000 |
		lpFreq = freq;

		if(m_synth != nil)
		{
			m_synth.set(\lpFreq, freq);
		}
	}

	storeOn { | stream |
		stream << "SuperDiffuse_FilterUnit("
		<< in << ","
		<< group << ","
		<< hpOn << ","
		<< bpOn << ","
		<< lpOn << ","
		<< hpFreq << ","
		<< bpFreq << ","
		<< bpRq << ","
		<< bpGain << ","
		<< lpFreq << ")";
	}

	load {
		this.unload;

		if(m_active == 1)
		{
			m_synth = Synth(\sd_filterSynth,
				[
					\in, in,
					\hpOn, hpOn,
					\bpOn, bpOn,
					\lpOn, lpOn,
					\hpFreq, hpFreq,
					\bpFreq, bpFreq,
					\bpRq, bpRq,
					\bpGain, bpGain,
					\lpFreq, lpFreq
				], group
			);
		}
	}

	unload {
		if(m_synth != nil)
		{
			m_synth.free;
			m_synth = nil;
		}
	}

	reload {
		this.unload;
		this.load;
	}

	getView {
		var view, layout;
		var bActive;
		var bHpOn, bBpOn, bLpOn;
		var nHpFreq, nBpFreq, nBpRq, nBpGain, nLpFreq;

		view = View();

		bActive = Button().states_([["OFF"], ["ON"]]).action_({|caller| this.active_(caller.value); }).value_(m_active);

		bHpOn = Button().states_([["OFF"], ["ON"]]).action_({|caller| this.hpOn_(caller.value); }).value_(hpOn);
		bBpOn = Button().states_([["OFF"], ["ON"]]).action_({|caller| this.bpOn_(caller.value); }).value_(bpOn);
		bLpOn = Button().states_([["OFF"], ["ON"]]).action_({|caller| this.lpOn_(caller.value); }).value_(lpOn);

		nHpFreq = NumberBox().clipLo_(5).clipHi_(1000).action_({|caller| this.hpFreq_(caller.value); }).value_(hpFreq);

		nBpFreq = NumberBox().clipLo_(100).clipHi_(15000).action_({|caller| this.bpFreq_(caller.value); }).value_(bpFreq);
		nBpRq = NumberBox().decimals_(4).clipLo_(0.001).clipHi_(2).action_({|caller| this.bpRq_(caller.value); }).value_(bpRq);
		nBpGain = NumberBox().clipLo_(-1).clipHi_(1).action_({|caller| this.bpGain_(caller.value); }).value_(bpGain);

		nLpFreq = NumberBox().clipLo_(1000).clipHi_(20000).action_({|caller| this.lpFreq_(caller.value); }).value_(lpFreq);

		layout = VLayout(
			HLayout(StaticText().string_("Filter state:"), bActive),
			HLayout(StaticText().string_("HPF:"), bHpOn, StaticText().string_("Freq:"), nHpFreq),
			HLayout(StaticText().string_("BPF:"), bBpOn, StaticText().string_("Freq:"), nBpFreq, StaticText().string_("1/Q:"), nBpRq, StaticText().string_("Gain:"), nBpGain),
			HLayout(StaticText().string_("LPF:"), bLpOn, StaticText().string_("Freq:"), nLpFreq)
		);

		view.layout_(layout);
		^view;
	}
}