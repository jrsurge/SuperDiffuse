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
	var m_pieces, m_matrix, m_numIns, m_numOuts, m_numControls;
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

		m_numIns = numIns;
		m_numOuts = numOuts;
		m_numControls = numControls;

		this.initBuses(numIns, numOuts);
		this.initGroups;
		this.initMatrix(numIns, numOuts);

		m_masterControl = SuperDiffuse_MasterControl(numControls);
		m_numControls.do({ | i |
			m_outFaders.add(SuperDiffuse_OutFader(m_masterControl.fader(i), m_controlBus.subBus(i)));
		});
		m_concertGUI = SuperDiffuse_ConcertGUI(this);

		("\n\n*** Welcome to SuperDiffuse ***\nCopyright(c) James Surgenor, 2016\nDeveloped at the University of Sheffield Sound Studios\n\n").postln;
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
		m_matrix = Array.fill(numIns, {Array.fill(numOuts, 0)});
	}

	addPiece { | piece |
		if(piece.isKindOf(SuperDiffuse_Piece) && (m_pieces.includesEqual(piece) != true))
		{
			m_pieces.add(piece);
			this.notify;
		}
	}

	removePiece { | piece |
		m_pieces.remove(piece);
		this.notify;
	}

	pieces {
		^m_pieces;
	}

	matrix {
		^m_matrix;
	}

	controls {
		^m_masterControl;
	}
}