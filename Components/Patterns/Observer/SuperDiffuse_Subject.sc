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
		};
	}

	detach { | observer |
		m_observers.remove(observer);
	}

	notify {
		m_observers.do(_.update(this));
	}
}