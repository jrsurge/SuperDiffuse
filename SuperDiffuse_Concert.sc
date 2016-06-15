/* Convenience wrapper */
SuperDiffuse {
	*new { | numIns, numOuts |
		^SuperDiffuse_Concert(numIns,numOuts);
	}
}

SuperDiffuse_Concert : SuperDiffuse_Subject {
	var m_pieces, m_matrix;
	var m_concertGUI, m_matrixGUI;

	*new { | numIns, numOuts |
		^super.new.ninit(numIns,numOuts);
	}

	ninit { | numIns, numOuts |
		m_pieces = List();
		this.initMatrix(numIns,numOuts);
		m_concertGUI = SuperDiffuse_ConcertGUI(this);
		//m_matrixGUI = SuperDiffuse_MatrixGUI(this);
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