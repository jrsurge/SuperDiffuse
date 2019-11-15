SuperDiffuse_Import {
	*new { | destination, sourcePath |
		var dic;
		var win, layout;
		var importMIDI, importControl;
		var midiConfig, controlConfig, matrices, filterSets;
		var iRow = 0;

		File.use(sourcePath, "r", { | file |
			dic = interpret(file.readAllString);
		});

		if(dic[\setupInfo][0] != destination.numIns)
		{
			Error("SuperDiffuse: Cannot import from concert of different size").throw;
		};

		if(dic[\setupInfo][1] != destination.numOuts)
		{
			Error("SuperDiffuse: Cannot import from concert of different size").throw;
		};

		if(dic[\setupInfo][2] != destination.numControls)
		{
			Error("SuperDiffuse: Cannot import from concert of different size").throw;
		};

		win = Window("SuperDiffuse | Import");
		layout = GridLayout();

		importMIDI = Button().states_([["[   ]"],["[ X ]"]]);
		importControl = Button().states_([["[   ]"],["[ X ]"]]);

		layout.add(importMIDI, 0, 0);
		layout.add(StaticText().string_("MIDI Configuration"), 0, 1);
		layout.add(importControl, 1, 0);
		layout.add(StaticText().string_("Control Configuration"), 1, 1);

		iRow = iRow + 2;

		matrices = ListView().items_(dic[\matrices].collect({|matrix| matrix[0]}))
		.selectionMode_(\multi)
		.selection_(nil);

		filterSets = ListView().items_(dic[\filterSets].collect({|filterSet| filterSet[0]}))
		.selectionMode_(\multi)
		.selection_(nil);

		layout.spacing_(10);

		layout.addSpanning(VLayout(StaticText().string_("Matrices:"), matrices), iRow, 0);
		layout.addSpanning(VLayout(StaticText().string_("Filter Sets:"), filterSets), iRow, 1);

		iRow = iRow + 1;

		layout.add(Button().states_([["Cancel"]]).action_({ win.close }), iRow, 0);
		layout.add(Button().states_([["OK"]]).action_({
			"SuperDiffuse: importing..".inform;

			if(importMIDI.value == 1)
			{
				"..MIDI Config".inform;
				destination.importMIDIConfig(dic[\midiConfig]);
			};

			if(importControl.value == 1)
			{
				"..Control Config".inform;
				destination.importControlConfig(dic[\controlsConfig]);
			};

			matrices.selection.do({ | ind |
				("..Matrix -" + dic[\matrices][ind][0]).inform;
				destination.importMatrix(dic[\matrices][ind]);
			});

			filterSets.selection.do({ | ind |
				("..Filter Set -" + dic[\filterSets][ind][0]).inform;
				destination.importFilterSet(dic[\filterSets][ind]);
			});

			"..done".inform;

			win.close;
		}), iRow, 1);

		win.layout = layout;
		win.front;
	}
}