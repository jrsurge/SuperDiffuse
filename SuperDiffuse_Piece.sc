SuperDiffuse_Piece {
	var m_name, m_path, m_cuedBuffer;

	*new { | path |
		^super.new.init(path);
	}

	init { | path |
		m_name = path;
		m_path = path;
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

	== { | b |
		^(this.path == b.path);
	}
}