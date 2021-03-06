SuperDiffuse_Observer {
	var m_subject;

	*new { | subject |
		^super.new.init(subject);
	}

	init { | subject |
		if(subject.isKindOf(SuperDiffuse_Subject))
		{
			m_subject = subject;
			m_subject.attach(this);
		};
	}

	update { | subject |
		if(m_subject === subject)
		{
			"UPDATE CALLED".postln;
		}
	}

	changeSubject { | newSubject |
		if(newSubject.isKindOf(SuperDiffuse_Subject))
		{
			if(m_subject != nil)
			{
				m_subject.detach(this);
			};
			m_subject = newSubject;
			m_subject.attach(this);
			this.update(m_subject);
		};
	}

	subject {
		^m_subject;
	}
}

SuperDiffuse_Subject {
	var m_observers;

	*new {
		^super.new.init;
	}

	init {
		m_observers = List();
	}

	attach { | observer |
		if(observer.isKindOf(SuperDiffuse_Observer) && (m_observers.includesEqual(observer) != true))
		{
			m_observers.add(observer);
		}
	}

	detach { | observer |
		m_observers.remove(observer);
	}

	notify {
		m_observers.do(_.update(this));
	}
}