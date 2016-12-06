SuperDiffuse_Piece {
	var m_name, m_path, m_cuedBuffer, m_matrixInd;

	*new { | path |
		^super.new.init(path);
	}

	init { | path |
		m_name = path;
		m_path = path;
		m_matrixInd = 0;
		SoundFile.use(path,{ | sf |
			m_cuedBuffer = Buffer.cueSoundFile(Server.default, sf.path,numChannels:sf.numChannels);
		});
	}

	name {
		^m_name;
	}

	name_ { | name |
		m_name = name;
	}

	path {
		^m_path;
	}

	cuedBuffer {
		^m_cuedBuffer;
	}

	recue {
		m_cuedBuffer.close;
		m_cuedBuffer.cueSoundFile(m_path,m_cuedBuffer.numChannels);
	}

	printOn { | stream |
		stream << m_name;
	}

	storeOn { | stream |
		stream << "SuperDiffuse_Piece.new(" << m_path.asCompileString << ")";
	}

	== { | b |
		^(this.path == b.path);
	}

	matrixInd {
		^m_matrixInd;
	}

	matrixInd_ { | ind |
		if(m_matrixInd != ind)
		{
			m_matrixInd = ind;
		}
	}
}