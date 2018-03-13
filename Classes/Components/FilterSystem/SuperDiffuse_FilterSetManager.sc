/*

FilterSetManager

This manages all the filter sets available to pieces.
Each piece should remember an index into this array, and on loading a piece,
this manager should be requested to load that filter set.

*/

SuperDiffuse_FilterSetManager
{
	var m_filterSets;
	var m_currentSet;

	*new {
		^super.new.init;
	}

	init {
		m_filterSets = List();
		m_currentSet = nil;
	}

	addFilterSet { | filterSet |
		m_filterSets.add(filterSet);
	}

	removeFilterSet { | ind |
		// if we're removing the current set, unload it first
		if(m_currentSet != nil)
		{
			if(m_filterSets[ind] === m_currentSet)
			{
				this.unload;
			};
		};

		m_filterSets.removeAt(ind);
	}

	at { | ind |
		^m_filterSets[ind];
	}

	load { | ind |
		if(m_currentSet != nil)
		{
			// make sure we only do something if it's a different filter set
			// (if two pieces use the same set, we can't afford a reload)
			if((m_currentSet === m_filterSets[ind]) == false)
			{
				this.unload;
				m_currentSet = m_filterSets[ind].load;
			}
		}
		{
			m_currentSet = m_filterSets[ind].load;
		};
	}

	unload {
		if(m_currentSet != nil)
		{
			m_currentSet.unload;
			m_currentSet = nil;
		}
	}

	reload {
		if(m_currentSet != nil)
		{
			m_currentSet.reload;
		}
	}

	names {
		^m_filterSets.collect({ | set | set.name; });
	}
}