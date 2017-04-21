/* Convenience wrapper */
SuperDiffuse {
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
			concert.addPiece(SuperDiffuse_Piece(pieceInfo[0]).name_(pieceInfo[1]).matrixInd_(pieceInfo[2]).masterLevel_(pieceInfo[3]));
		});

		// get rid of any existing matrices
		concert.matrices.clear;

		dic[\matrices].do({|matrixInfo|
			concert.addMatrix(matrixInfo[0]);
			concert.matrices.last.matrix = matrixInfo[1];
		});

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

SuperDiffuse_Concert : SuperDiffuse_Subject {
	var m_pieces, m_matrixMaster, m_matrices, m_numIns, m_numOuts, m_numControls;
	var m_masterControl, m_outFaders;
	var m_inBus, m_outBus, m_controlBus;
	var m_patchers;
	var m_inGroup, m_patcherGroup, m_outGroup;
	var m_concertGUI;

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

		m_masterControl = SuperDiffuse_MasterControl(numControls);
		m_numOuts.do({ | i |
			m_outFaders.add(SuperDiffuse_OutFader(m_masterControl.fader(i%numControls), m_controlBus.subBus(i)));
		});
		m_concertGUI = SuperDiffuse_ConcertGUI(this);

		("\n\n*** Welcome to SuperDiffuse ***\nCopyright(c) James Surgenor, 2016\nDeveloped at the University of Sheffield Sound Studios\n\n").postln;

		Synth(\sd_outsynth,[\in, m_outBus, \control, m_controlBus], m_outGroup);

		m_concertGUI.update;
	}

	registerSynthDefs {
		SynthDef(\sd_patcher,{| in=0, out=0, gain=1 |
			var sig = In.ar(in);
			Out.ar(out,sig * gain);
		}).add;

		SynthDef(\sd_outsynth,{ | in=0, control=0, masterLevel=0 |
			var sig, amps;

			sig = In.ar(in,m_numIns);
			amps = In.kr(control, m_numOuts);

			Out.ar(0, sig * Lag.kr(amps) * masterLevel);
		}).add;
	}

	initBuses { | numIns, numOuts |
		m_inBus = Bus.audio(Server.default, numIns);
		m_outBus = Bus.audio(Server.default, numOuts);
		m_controlBus = Bus.control(Server.default, numOuts);
	}

	initGroups {
		m_inGroup = Group();
		m_patcherGroup = Group.after(m_inGroup);
		m_outGroup = Group.after(m_patcherGroup);
	}

	initMatrix { | numIns, numOuts |
		m_matrixMaster = SuperDiffuse_Matrix(numIns, numOuts, "master");
		m_matrices.add(SuperDiffuse_Matrix.newFrom(m_matrixMaster, "Default"));
	}

	addPiece { | piece |
		if(piece.isKindOf(SuperDiffuse_Piece) && (m_pieces.includesEqual(piece) != true))
		{
			m_pieces.add(piece);
			this.notify;
		}
	}

	addMatrix { | name |
		m_matrices.add(SuperDiffuse_Matrix.newFrom(m_matrixMaster, name));
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

	controls {
		^m_masterControl;
	}

	configureOutFaders {
		var win, layout;
		var scrollView, scrollCanvas;

		win = Window("SuperDiffuse | Control Faders Configuration");

		scrollView = ScrollView();
		scrollCanvas = View();

		scrollView.canvas_(scrollCanvas);

		layout = GridLayout();

		layout.add(StaticText().string_("Listens to:"), 0, 1);
		m_outFaders.do({ | outFader, i |
			layout.add(StaticText().string_("Out%".format(i+1)),i+1,0);
			layout.add(PopUpMenu().items_((0..m_numControls-1)+1).action_({| caller |
				m_outFaders[i].changeSubject(m_masterControl.fader(caller.value));
			}).value_(m_masterControl.indexOf(outFader.subject)), i+1, 1);
		});

		scrollCanvas.layout_(layout);

		win.layout_(VLayout(scrollView, Button().states_([["OK"]]).action_({win.close;})));
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

		win.layout_(VLayout(scrollView, Button().states_([["OK"]]).action_({
			config.do({ | info, ind |
				m_masterControl.fader(ind).assignMIDI(info[0], info[1]);
			});
			win.close;
		})));

		win.front;

	}

	assignControl { | controlInd, faderInd |
		m_outFaders[faderInd].changeSubject(m_masterControl.fader(controlInd));
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
		dic.add(\pieces -> m_pieces.collect({|piece| [piece.path, piece.name, piece.matrixInd, piece.masterLevel] }));
		dic.add(\matrices -> m_matrices.collect({|matrix| [matrix.name, matrix.matrix] }));
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