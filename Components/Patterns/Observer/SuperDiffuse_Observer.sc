SuperDiffuse_Observer {
	var m_subject;

	*new { | subject |
		^super.new.init(subject)
	}

	init { | subject |
		if(subject.isKindOf(SuperDiffuse_Subject))
		{
			m_subject = subject;
			m_subject.attach(this);
		};
	}

	update { | caller |
		if(caller === m_subject)
		{
			"UPDATE CALLED".postln;
		};
	}
}