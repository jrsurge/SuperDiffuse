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
	var m_numIns, m_numOuts, m_inBus, m_outBus, m_inFxGroup, m_outFxGroup;

	*new { | numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup |
		^super.new.init(numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup);
	}

	init { | numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup |
		m_filterSets = List();
		m_currentSet = nil;

		m_numIns = numIns;
		m_numOuts = numOuts;
		m_inBus = inBus;
		m_outBus = outBus;
		m_inFxGroup = inFxGroup;
		m_outFxGroup = outFxGroup;
	}

	clear {
		this.unload;

		m_filterSets.clear;
	}

	addFilterSet { | filterSet |
		m_filterSets.add(filterSet);
	}

	createFilterSet {
		^SuperDiffuse_FilterSet(m_numIns, m_numOuts, m_inBus, m_outBus, m_inFxGroup, m_outFxGroup);
	}

	removeFilterSet { | ind |
		var removed = false; // assume we didn't remove anything

		if(ind > 0) // don't remove the default filterset
		{
			// if we're removing the current set, unload it first
			if(m_currentSet != nil)
			{
				if(m_filterSets[ind] === m_currentSet)
				{
					this.unload;
				};
			};

			m_filterSets.removeAt(ind);
			removed = true;
		}

		^removed;
	}

	at { | ind |
		^m_filterSets[ind];
	}

	filterSets {
		^m_filterSets;
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