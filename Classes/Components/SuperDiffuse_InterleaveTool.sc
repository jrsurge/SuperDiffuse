SuperDiffuse_InterleaveToolGUI
{
	var parent;
	var window, layout;
 	var listView;
	var addButton, removeButton, upButton, downButton;

	*new { | parent |
		^super.new.init(parent);
	}

	init { | p |
		parent = p;
		window = Window("SuperDiffuse | Interleave Tool");

		layout = VLayout();

		listView = ListView();
		layout.add(listView);

		addButton = Button().states_([["+"]]).action_({
			Dialog.openPanel({ | files |
				files.do({ | file |
					parent.addPath(file);
				});
			},{}, true);
		});

		removeButton = Button().states_([["-"]]).action_({
			var sel = listView.selection[0];

			if(sel != nil)
			{
				parent.removeAt(sel);
				if(sel != 0)
				{
					listView.selection = [sel - 1];
				}
				{
					listView.selection = [0];
				};
			}
		});

		upButton = Button().states_([["^"]]).action_({
			var sel = listView.selection[0];
			if(sel != nil)
			{
				if(sel > 0)
				{
					parent.swapAt(sel, sel - 1);
					listView.selection = [sel - 1];
				};
			};
		});

		downButton = Button().states_([["v"]]).action_({
			var sel = listView.selection[0];
			if(sel != nil)
			{
				if(sel < (listView.items.size - 1))
				{
					parent.swapAt(sel, sel + 1);
					listView.selection = [sel + 1];
				};
			};
		});

		layout.add(HLayout(addButton, removeButton, upButton, downButton));

		layout.add(Button().states_([["Interleave"]]).action_({ Dialog.savePanel({ | outPath | parent.interleave(outPath) }); }));

		window.layout_(layout);

		this.update;
	}

	display {
		if(window.isClosed)
		{
			this.init;
		};

		window.front;
	}

	close {
		window.close;
	}

	update {
		if(listView.isClosed != true)
		{
			listView.items_(parent.paths.asArray);
		}
	}
}

SuperDiffuse_InterleaveTool
{
	var m_paths;
	var m_gui;
	var <>onInterleave;

	*new {
		^super.new.init;
	}

	init {
		m_paths = List();

		m_gui = SuperDiffuse_InterleaveToolGUI(this);
	}

	addPath { | path |
		m_paths.add(path);
		m_gui.update;
	}

	removeAt { | ind |
		m_paths.removeAt(ind);
		m_gui.update;
	}

	// Swap at index
	swapAt { | indA, indB |
		m_paths.swap(indA,indB);
		m_gui.update;
	}

	paths {
		^m_paths;
	}

	// Perform the interleave
	interleave { | outPath |
		outPath = outPath.dirname +/+ PathName(outPath).fileNameWithoutExtension ++ ".caf";

		// if we have any files
		if(m_paths.size > 0)
		{
			var cmdString = SuperDiffuse.helpersDir +/+ "sd-interleave" + outPath.quote;
			m_paths.do({|p| cmdString = cmdString + p.quote; });

			cmdString.unixCmd({ {onInterleave.value(outPath)}.defer; m_gui.close; });
		}
	}

	gui {
		m_gui.display;
	}
}