/*

FilterSet

A filter set is a set of input filters and output filters.
These are instances of FilterUnit, and can be loaded and unloaded
on request.

Designed to be managed by a FilterSetManager

*/

SuperDiffuse_FilterSet
{
	var m_name;
	var m_inFilters;
	var m_outFilters;

	*new { | numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup |
		^super.new.init(numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup);
	}

	init { | numIns, numOuts, inBus, outBus, inFxGroup, outFxGroup |
		m_inFilters = Array.fill(numIns, { | ind | SuperDiffuse_FilterUnit(inBus.subBus(ind), inFxGroup ); });
		m_outFilters = Array.fill(numOuts, { | ind | SuperDiffuse_FilterUnit(outBus.subBus(ind), outFxGroup); });
		m_name = "New Filter Set";
	}

	inFilterAt { | ind |
		^m_inFilters[ind];
	}

	outFilterAt { | ind |
		^m_outFilters[ind];
	}

	inFilters {
		^m_inFilters;
	}

	outFilters {
		^m_outFilters;
	}

	name {
		^m_name;
	}

	name_ { | n |
		m_name = n;
	}

	load {
		this.unload;

		m_inFilters.do(_.load);
		m_outFilters.do(_.load);
	}

	unload {
		m_inFilters.do(_.unload);
		m_outFilters.do(_.unload);
	}

	reload {
		this.unload;
		this.load;
	}

	gui { | onClose |
		var win;
		var layout;
		var name;
		var inFilterLayout, outFilterLayout;
		var inFilterList, outFilterList;
		var inFilterView, outFilterView;

		inFilterView = nil;
		outFilterView = nil;

		layout = VLayout();

		inFilterLayout = VLayout();
		outFilterLayout = VLayout();

		win = Window("SuperDiffuse | Edit Filter Set");

		name = TextField().string_(m_name).action_({| caller | m_name = caller.value;});

		layout.add(HLayout(StaticText().string_("Name: "), name));

		inFilterList = ListView().items_(m_inFilters.collect({| f, i | "In " + (i + 1);})).action_({ | caller |
			var sel = caller.selection[0];

			if(sel != nil)
			{
				if(inFilterView != nil)
				{
					inFilterView.remove;
				};

				inFilterLayout.add(inFilterView = m_inFilters[sel].getView);
			}
		});

		outFilterList = ListView().items_(m_outFilters.collect({| f, i | "Out " + (i + 1);})).action_({ | caller |
			var sel = caller.selection[0];

			if(sel != nil)
			{
				if(outFilterView != nil)
				{
					outFilterView.remove;
				};

				outFilterLayout.add(outFilterView = m_outFilters[sel].getView);
			}
		});

		inFilterLayout.add(StaticText().string_("Input Filters")).add(inFilterList).add(inFilterView);
		outFilterLayout.add(StaticText().string_("Output Filters")).add(outFilterList).add(outFilterView);

		inFilterList.valueAction_(0);
		outFilterList.valueAction_(0);

		layout.add(HLayout(inFilterLayout,outFilterLayout));

		win.layout_(layout);
		win.front;
		win.onClose_(onClose);
	}
}