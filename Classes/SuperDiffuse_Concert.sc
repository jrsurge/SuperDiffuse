/* Convenience wrapper */
SuperDiffuse {
	classvar <version = "1.4.0";

	*new { | numIns, numOuts, numControls |
		if(numOuts > Server.default.options.numOutputBusChannels)
		{
			Error("Server doesn't have enough output channels - update Server.default.options.numOutputBusChannels").throw;
		}
		{
			^SuperDiffuse_Concert(numIns, numOuts, numControls);
		}
	}

	*load { | pathToSaveFile |
		var dic, concert;

		File.use(pathToSaveFile, "r", { | file |
			dic = interpret(file.readAllString);
		});

		concert = SuperDiffuse_Concert(dic[\setupInfo][0], dic[\setupInfo][1], dic[\setupInfo][2]);

		dic[\pieces].do({|pieceInfo|
			var piece = SuperDiffuse_Piece(pieceInfo[0]);

			piece.name_(pieceInfo[1]);
			piece.matrixInd_(pieceInfo[2]);
			piece.masterLevel_(pieceInfo[3]);

			// older save files won't have filter indices
			if(pieceInfo.size == 5)
			{
				piece.filterInd_(pieceInfo[4]);
			};

			concert.addPiece(piece);
		});

		// get rid of any existing matrices
		concert.matrices.clear;

		dic[\matrices].do({|matrixInfo|
			concert.addMatrix(matrixInfo[0]);
			concert.matrices.last.matrix = matrixInfo[1];
		});

		// old save files won't have filterSets
		if(dic[\filterSets] != nil)
		{
			// get rid of any existing filterSets
			concert.filterManager.clear; // doing this here ensures we have the Default filterSet even if it's an old save

			dic[\filterSets].do({| filterSetInfo |
				var fs = concert.filterManager.createFilterSet;

				fs.name_(filterSetInfo[0]);

				// inFilters
				filterSetInfo[1].do({ | info, ind |
					fs.inFilters[ind]
					.active_(info[0])
					.hpOn_(info[1])
					.bpOn_(info[2])
					.lpOn_(info[3])
					.hpFreq_(info[4])
					.bpFreq_(info[5])
					.bpRq_(info[6])
					.bpGain_(info[7])
					.lpFreq_(info[8])
					;
				});

				// outFilters
				filterSetInfo[2].do({ | info, ind |
					fs.outFilters[ind]
					.active_(info[0])
					.hpOn_(info[1])
					.bpOn_(info[2])
					.lpOn_(info[3])
					.hpFreq_(info[4])
					.bpFreq_(info[5])
					.bpRq_(info[6])
					.bpGain_(info[7])
					.lpFreq_(info[8])
					;
				});

				concert.filterManager.addFilterSet(fs);
			})
		};

		dic[\controlsConfig].do({| controlAssignment, ind |
			concert.assignControl(controlAssignment, ind);
		});

		dic[\midiConfig].do({| conf, ind |
			concert.assignMIDI(ind, conf[0], conf[1]);
		});

		concert.loaded;

		^concert;
	}
}

/*

inGroup -> inFxGroup -> patcherGroup -> outFxGroup -> outGroup

- inGroup has playbackSynth with nChannels, writes to inBus[numIns] : nChannels <= numIns;
- inFxGroup has filterSynths for each input channel, overwrites inBus
- patcherGroup has patcherSynths for each routing, routes inBus[n] to outBus[n]
- outFxGroup has filterSynth for each output channel, overwrites outBus
- outGroup has outputSynth, multiplies outBus by controlBus, sends to DAC

*/

SuperDiffuse_Concert : SuperDiffuse_Subject {
	var m_pieces, m_matrixMaster, m_matrices, m_numIns, m_numOuts, m_numControls;
	var m_masterControl, m_outFaders;
	var m_inBus, m_outBus, m_controlBus;
	var m_patchers;
	var m_inGroup, m_inFxGroup, m_patcherGroup, m_outFxGroup, m_outGroup;
	var m_concertGUI;
	var m_filterManager;

	var m_playingPiece;

	*new { | numIns, numOuts, numControls |
		^super.new.ninit(numIns,numOuts,numControls);
	}

	ninit { | numIns, numOuts, numControls |
		m_pieces = List();
		m_outFaders = List();

		m_matrices = List();

		// We'll need to be able to talk to our patchers if we ever want to do proportional level sending
		// i.e. multiple ins to single out
		m_patchers = Array.fill(numIns, { Array.fill(numOuts, { nil } ) });

		m_numIns = numIns;
		m_numOuts = numOuts;
		m_numControls = numControls;

		this.registerSynthDefs;

		this.initBuses(numIns, numOuts);
		this.initGroups;
		this.initMatrix(numIns, numOuts);
		this.initFilters;

		m_masterControl = SuperDiffuse_MasterControl(numControls);
		m_numOuts.do({ | i |
			m_outFaders.add(SuperDiffuse_OutFader(m_masterControl.fader(i%numControls), m_controlBus.subBus(i)));
		});
		m_concertGUI = SuperDiffuse_ConcertGUI(this);

		Post << "*** Welcome to SuperDiffuse ***" "\nVersion " << SuperDiffuse.version << "\nCopyright(c) James Surgenor, 2016-2019\nDeveloped at the University of Sheffield Sound Studios\n\n";

		Synth(\sd_outsynth,[\in, m_outBus, \control, m_controlBus], m_outGroup);

		m_concertGUI.update;
	}

	registerSynthDefs {
		"Adding sd_patcher".inform;
		SynthDef(\sd_patcher,{| in=0, out=0, gain=1 |
			var sig = In.ar(in);
			Out.ar(out,sig * gain);
		}).add;

		"Adding sd_outsynth".inform;
		SynthDef(\sd_outsynth,{ | in=0, control=0, masterLevel=0 |
			var sig, amps;

			sig = In.ar(in,m_numOuts);
			amps = In.kr(control, m_numOuts);

			Out.ar(0, sig * Lag.kr(amps) * masterLevel);
		}).add;

		"Adding sd_filterSynth".inform;
		SynthDef(\sd_filterSynth, { | in = 0, hpOn = 0, bpOn = 0, lpOn = 0, hpFreq = 5, bpFreq = 1000, bpRq = 1, bpGain = 0, lpFreq = 15000 |
			var sig = In.ar(in);

			sig = Select.ar(hpOn, [ sig, HPF.ar(sig, hpFreq) ]);
			sig = Select.ar(bpOn, [ sig, MidEQ.ar(sig, bpFreq, bpRq, bpGain * 24) ]);

			sig = Select.ar(lpOn, [ sig, LPF.ar(sig, lpFreq) ]);

			ReplaceOut.ar(in, sig);
		}).add;
	}

	initBuses { | numIns, numOuts |
		m_inBus = Bus.audio(Server.default, numIns);
		m_outBus = Bus.audio(Server.default, numOuts);
		m_controlBus = Bus.control(Server.default, numOuts);
	}

	initGroups {
		m_inGroup = Group();
		m_inFxGroup = Group.after(m_inGroup);
		m_patcherGroup = Group.after(m_inFxGroup);
		m_outFxGroup = Group.after(m_patcherGroup);
		m_outGroup = Group.after(m_outFxGroup);
	}

	initMatrix { | numIns, numOuts |
		m_matrixMaster = SuperDiffuse_Matrix(numIns, numOuts, "master");
		m_matrices.add(SuperDiffuse_Matrix.newFrom(m_matrixMaster, "Default"));
	}

	initFilters {
		m_filterManager = SuperDiffuse_FilterSetManager(m_numIns, m_numOuts, m_inBus, m_outBus, m_inFxGroup, m_outFxGroup);

		m_filterManager.addFilterSet(m_filterManager.createFilterSet().name_("Default"));
	}

	addPiece { | piece |
		if(piece.isKindOf(SuperDiffuse_Piece) && (m_pieces.includesEqual(piece) != true))
		{
			m_pieces.add(piece);
			this.notify;
		}
	}

	addMatrix { | name, refIndex=nil |
		var refMatrix = m_matrixMaster;

		if(refIndex != nil)
		{
			refMatrix = m_matrices[refIndex];
			name = refMatrix.name + "Copy";
		};

		m_matrices.add(SuperDiffuse_Matrix.newFrom(refMatrix, name));

		m_observers.do(_.updateMatrices);
	}

	removePiece { | piece |
		m_pieces.remove(piece);
		this.notify;
	}

	removeMatrix { | matrix |
		m_matrices.remove(matrix);
		m_observers.do(_.updateMatrices);
	}

	loadMatrix { | matrix |
		this.clearPatchers;

		m_numIns.do({ | in |
			m_numOuts.do({ | out |
				var amp = matrix.matrix[in][out];
				if(amp > 0)
				{
					m_patchers[in][out] = Synth(\sd_patcher, [\in, m_inBus.subBus(in), \out, m_outBus.subBus(out), \gain, amp],m_patcherGroup);
				};
			});
		});
	}

	pieces {
		^m_pieces;
	}

	matrixMaster {
		^m_matrixMaster;
	}

	matrix { | index |
		^m_matrices[index];
	}

	matrices {
		^m_matrices;
	}

	filterManager {
		^m_filterManager;
	}

	controls {
		^m_masterControl;
	}

	configureOutFaders {
		var win, layout;
		var scrollView, scrollCanvas;
		var toggles = List();
		var ctrlConfig = m_outFaders.collect({|outFader| m_masterControl.indexOf(outFader.subject); });

		win = Window("SuperDiffuse | Control Faders Configuration");

		scrollView = ScrollView();
		scrollCanvas = View();

		scrollView.canvas_(scrollCanvas);

		layout = GridLayout();

		m_numControls.do({ | ctrlInd |
			var togglesPerControl = List();

			layout.add(StaticText().string_("Control: " ++ (ctrlInd+1)).align_(\right), ctrlInd + 1, 0, \center);

			m_numOuts.do({ | outInd |
				var pt;

				pt = SuperDiffuse_PatchToggle().toolTip_("%:%".format(ctrlInd + 1, outInd + 1)).action_({ | caller |
					var val = caller.value;
					// update graphical view
					toggles.do({| ctrl | ctrl[outInd].value_(0) });

					toggles[ctrlInd][outInd].value_(val);

					// actually change the value in the config
					if(val != 0)
					{
						ctrlConfig[outInd] = ctrlInd;
					}
					{
						ctrlConfig[outInd] = nil;
					}
				});

				togglesPerControl.add(pt);

				layout.add(StaticText().string_("Out" ++ (outInd + 1)).align_(\center), 0, outInd+1, \center);
				layout.add(pt, ctrlInd + 1, outInd + 1);
			});
			toggles.add(togglesPerControl);
		});

		// now that we've got out widgets, grab the current setup
		ctrlConfig.do({| ctrl, out |
			if(ctrl != nil)
			{
				toggles[ctrl][out].valueAction_(1);
			}
			{
				toggles[out][out].valueAction_(0);
			};
		});


		scrollCanvas.layout_(layout);

		win.layout_(VLayout(scrollView,
			HLayout(
				Button().states_([["Cancel"]]).action_({win.close;}),
				Button().states_([["OK"]]).action_({
					ctrlConfig.do({| ctrl, out |
						this.assignControl(ctrl, out);
					});
					win.close;
				})
			)
		));
		win.front;
	}

	configureMIDI {
		var win, layout;
		var scrollView, scrollCanvas;
		var config;

		win = Window("SuperDiffuse | Configure MIDI");

		// [[chan, cc], ...]
		config = Array.fill(m_numControls, { Array.fill(2, { 0 } ) });

		scrollView = ScrollView();
		scrollCanvas = View();

		scrollView.canvas_(scrollCanvas);

		layout = GridLayout();

		layout.add(StaticText().string_("MIDI Chan"), 0, 1);
		layout.add(StaticText().string_("MIDI CC"), 0, 2);

		m_numControls.do({ | ind |
			layout.add(StaticText().string_("Control Fader: " + (ind + 1)).align_(\right), ind + 1, 0);
			layout.add(NumberBox().clipLo_(0).clipHi_(127).action_({ | caller | config[ind][0] = caller.value; }).valueAction_(m_masterControl.fader(ind).midiChan), ind + 1, 1);
			layout.add(NumberBox().clipLo_(0).clipHi_(127).action_({ | caller | config[ind][1] = caller.value; }).valueAction_(m_masterControl.fader(ind).midiCC), ind + 1, 2);
		});

		scrollCanvas.layout_(layout);

		win.layout_(
			VLayout(scrollView,
				HLayout(
					Button().states_([["Cancel"]]).action_({win.close;}),
					Button().states_([["OK"]]).action_({
						config.do({ | info, ind |
							m_masterControl.fader(ind).assignMIDI(info[0], info[1]);
						});
						win.close;
					})
				)
			)
		);
		win.front;
	}

	configureFilters {
		var win, layout;
		var filterList;
		var filterAddButton, filterRemoveButton;

		win = Window("SuperDiffuse | Configure Filters");
		layout = VLayout();

		layout.add(StaticText().string_("Filter Sets"));

		filterList = ListView().items_(m_filterManager.names).keyDownAction_({ | caller, char, modifiers, unicode, keycode, key |
			if(caller.hasFocus)
			{
				if( (caller.selection[0] != nil) && (key == 0x45) && (modifiers.isCtrl || modifiers.isCmd))
				{
					m_filterManager[caller.selection[0]].gui({caller.items_(m_filterManager.names); m_filterManager.reload; });
				}
			}
		});

		filterAddButton = Button().states_([["+"]]).action_({
			m_filterManager.addFilterSet(SuperDiffuse_FilterSet(m_numIns, m_numOuts, m_inBus, m_outBus, m_inFxGroup, m_outFxGroup));
			filterList.items_(m_filterManager.names);
		});

		layout.add(VLayout(filterList, HLayout(filterAddButton)));

		win.layout_(layout);
		win.front;
	}

	assignControl { | controlInd, faderInd |
		if(controlInd != nil)
		{
			m_outFaders[faderInd].changeSubject(m_masterControl.fader(controlInd));
		}
		{
			m_outFaders[faderInd].changeSubject(SuperDiffuse_OutFader.dummySubject);
		};
	}

	assignMIDI { | ind, chan, cc |
		m_masterControl.fader(ind).assignMIDI(chan, cc);
	}

	clear {
		m_inBus.free;
		m_outBus.free;
		m_controlBus.free;
		m_masterControl.clear;
		m_inGroup.free;
		m_outGroup.free;

		m_patcherGroup.free;
		m_patcherGroup = nil;
		this.clearPatchers;

		m_filterManager.unload;
		m_inFxGroup.free;
		m_outFxGroup.free;
	}

	clearPatchers
	{
		if(m_patcherGroup != nil)
		{
			m_patcherGroup.freeAll;
		};
		m_patchers.size.do({ | in |
			m_patchers[0].size.do({ | out |
				m_patchers[in][out] = nil;
			});
		});
	}

	dumpControlBus {
		m_controlBus.get.postln;
	}

	createSaveFile { | path |
		var dic;

		dic = Dictionary();

		dic.add(\setupInfo -> [m_numIns, m_numOuts, m_numControls]);
		dic.add(\pieces -> m_pieces.collect({|piece| [piece.path, piece.name, piece.matrixInd, piece.masterLevel, piece.filterInd] }));
		dic.add(\matrices -> m_matrices.collect({|matrix| [matrix.name, matrix.matrix] }));

		dic.add(
			\filterSets ->
			m_filterManager.filterSets.collect({| filterSet |
				[filterSet.name, filterSet.inFilters.collect({|fu| fu.saveInfo; }), filterSet.outFilters.collect({|fu| fu.saveInfo; })];
			})
		);


		dic.add(\controlsConfig -> m_outFaders.collect({|outFader| m_masterControl.indexOf(outFader.subject)}));
		dic.add(\midiConfig -> m_masterControl.faders.collect({|fader| [fader.midiChan, fader.midiCC]}));

		File.use(path, "w", { | file |
			file.write(dic.asCompileString);
		});
	}

	loaded {
		// should only be called from SuperDiffuse.load function - indicates we have loaded from a file and the first piece needs loading up..
		m_concertGUI.ready;
	}

	play { | index, start, end |
		m_playingPiece = m_pieces[index];
		m_playingPiece.play(start, end, m_inBus, m_inGroup);
	}

	stop {
		m_playingPiece.stop;
	}

	isPlaying {
		if( m_playingPiece == nil)
		{
			^false;
		}
		{
			^m_playingPiece.isPlaying;
		};
	}

	setMasterLevel { | level |
		m_outGroup.set(\masterLevel, level);
	}

}