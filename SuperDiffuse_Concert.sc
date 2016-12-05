/* Convenience wrapper */
SuperDiffuse {
	*new { | numIns, numOuts, numControls |
		^SuperDiffuse_Concert(numIns,numOuts, numControls);
	}

	/* Will eventually have a Builder to recreate concerts from saves
	Pseudo-code:
	load{ | save |
		var concert;
		concert = SuperDiffuse_Concert(save[\numIns], save[\numOuts]);
		save[\pieces].do({|piece|
			concert.addPiece(piece);
		});
	}
	*/
}

SuperDiffuse_Concert : SuperDiffuse_Subject {
	var m_pieces, m_matrixMaster, m_matrices, m_numIns, m_numOuts, m_numControls;
	var m_masterControl, m_outFaders;
	var m_inBus, m_outBus, m_controlBus;
	var m_inGroup, m_patcherGroup, m_outGroup;
	var m_concertGUI;

	*new { | numIns, numOuts, numControls |
		^super.new.ninit(numIns,numOuts,numControls);
	}

	ninit { | numIns, numOuts, numControls |
		m_pieces = List();
		m_outFaders = List();

		m_matrices = List();

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

		Synth(\sd_outsynth,[\in, m_inBus, \control, m_controlBus], m_outGroup);
	}

	registerSynthDefs {
		SynthDef(\sd_patcher,{| in=0, out=0, gain=1 |
			var sig = In.ar(in);
			Out.ar(out,sig * gain);
		}).add;

		SynthDef(\sd_outsynth,{ | in=0, control=0 |
			var sig, amps;

			sig = In.ar(in,m_numIns);
			amps = In.kr(control, m_numOuts);

			Out.ar(0, sig * amps);
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
		"Loading matrix: %".format(matrix.name).postln;
		m_patcherGroup.freeAll;
		m_numIns.do({ | in |
			m_numOuts.do({ | out |
				if(matrix.matrix[in][out] == 1)
				{
					Synth(\sd_patcher, [\in, m_inBus.subBus(in), \out, m_outBus.subBus(out)],m_patcherGroup);
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

		win.layout_(VLayout().add(scrollView));
		win.front;
	}

	clear {
		m_inBus.free;
		m_outBus.free;
		m_controlBus.free;
		m_masterControl.clear;
		m_inGroup.free;
		m_outGroup.free;
		m_patcherGroup.free;
	}

	dumpControlBus {
		m_controlBus.get.postln;
	}

}