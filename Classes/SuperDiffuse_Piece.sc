

SuperDiffuse_Piece {
	var m_name, m_path, m_soundFile, m_matrixInd, m_masterLevel;
	var m_filterInd;
	var m_playbackEv;

	*new { | path |
		^super.new.init(path);
	}

	init { | path |
		m_name = path;
		m_path = path;
		m_matrixInd = 0;
		m_filterInd = 0;
		m_masterLevel = 0.5;
		m_soundFile = SoundFile(path);
		m_soundFile.openRead();
	}

	name {
		^m_name;
	}

	name_ { | name |
		m_name = name;
	}

	sampleRate {
		^m_soundFile.sampleRate;
	}

	path {
		^m_path;
	}

	play { | startPos, endPos, out, group |
		if(m_playbackEv != nil)
		{
			if(m_playbackEv.isRunning)
			{
				m_playbackEv.stop;
			};
		};

		m_playbackEv = m_soundFile.cue((firstFrame: startPos, lastFrame: endPos, out: out, group: group),true);
	}

	isPlaying
	{
		^m_playbackEv.isRunning;
	}

	stop {
		m_playbackEv.stop;
	}

	printOn { | stream |
		stream << m_name;
	}

	storeOn { | stream |
		stream << "SuperDiffuse_Piece.new(" << m_path.asCompileString << ")";
	}

	== { | b |
		^this.compareObject(b, #[\m_path]);
	}

	hash {
		^this.instVarHash(#[\m_path]);
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

	filterInd {
		^m_filterInd;
	}

	filterInd_ { | ind |
		if(m_filterInd != ind)
		{
			m_filterInd = ind;
		}
	}

	masterLevel {
		^m_masterLevel;
	}

	masterLevel_ { | level |
		m_masterLevel = level;
	}
}