/* Convenience wrapper */
SuperDiffuse {
	*new { | numIns, numOuts |
		^SuperDiffuse_Concert(numIns,numOuts);
	}
}

SuperDiffuse_Concert : SuperDiffuse_Subject {
	var m_pieces, m_matrix;
	var m_inBus, m_outBus, m_controlBus;
	var m_inGroup, m_patcherGroup, m_outGroup;
	var m_concertGUI, m_matrixGUI;

	*new { | numIns, numOuts |
		^super.new.ninit(numIns,numOuts);
	}

	ninit { | numIns, numOuts |
		m_pieces = List();

		this.initBuses(numIns, numOuts);
		this.initGroups;
		this.initMatrix(numIns,numOuts);

		m_concertGUI = SuperDiffuse_ConcertGUI(this);
		//m_matrixGUI = SuperDiffuse_MatrixGUI(this);
	}

	initBuses { | numIns, numOuts |
		m_inBus = Bus.audio(Server.default, numIns);
		m_outBus = Bus.audio(Server.default, numOuts);
		m_controlBus = Bus.control(Server.default, numOuts);
	}

	initGroups {
		m_inGroup = Group();
		m_patcherGroup = Group.after(m_inGroup);
		m_outGroup = Group.after(m_outGroup);
	}

	initMatrix { | numIns, numOuts |
		m_matrix = Array.fill(numIns, Array.fill(numOuts, 0));
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

}