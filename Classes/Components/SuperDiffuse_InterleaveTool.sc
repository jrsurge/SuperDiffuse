SuperDiffuse_InterleaveToolGUI
{
	var parent;
	var window, layout;
 	var listView;
	var addButton, removeButton, upButton, downButton;

	*new { | parent |
		^super.new.init(parent);
	}

	init { | p |
		parent = p;
		window = Window("SuperDiffuse | Interleave Tool");

		layout = VLayout();

		listView = ListView();
		layout.add(listView);

		addButton = Button().states_([["+"]]).action_({
			Dialog.openPanel({ | files |
				files.do({ | file |
					parent.addPath(file);
				});
			},{}, true);
		});

		removeButton = Button().states_([["-"]]).action_({
			var sel = listView.selection[0];

			if(sel != nil)
			{
				parent.removeAt(sel);
				if(sel != 0)
				{
					listView.selection = [sel - 1];
				}
				{
					listView.selection = [0];
				};
			}
		});

		upButton = Button().states_([["^"]]).action_({
			var sel = listView.selection[0];
			if(sel != nil)
			{
				if(sel > 0)
				{
					parent.swapAt(sel, sel - 1);
					listView.selection = [sel - 1];
				};
			};
		});

		downButton = Button().states_([["v"]]).action_({
			var sel = listView.selection[0];
			if(sel != nil)
			{
				if(sel < (listView.items.size - 1))
				{
					parent.swapAt(sel, sel + 1);
					listView.selection = [sel + 1];
				};
			};
		});

		layout.add(HLayout(addButton, removeButton, upButton, downButton));

		layout.add(Button().states_([["Interleave"]]).action_({ parent.interleave; }));

		window.layout_(layout);

		this.update;
	}

	display {
		if(window.isClosed)
		{
			this.init;
		};

		window.front;
	}

	close {
		window.close;
	}

	update {
		if(listView.isClosed != true)
		{
			listView.items_(parent.paths.asArray);
		}
	}
}

SuperDiffuse_InterleaveTool
{
	var m_paths;
	var m_gui;
	var <>onInterleave;
	var m_outPath;

	*new {
		^super.new.init;
	}

	init {
		m_paths = List();

		m_gui = SuperDiffuse_InterleaveToolGUI(this);
	}

	addPath { | path |
		m_paths.add(path);
		m_gui.update;
	}

	removeAt { | ind |
		m_paths.removeAt(ind);
		m_gui.update;
	}

	// Swap at index
	swapAt { | indA, indB |
		m_paths.swap(indA,indB);
		m_gui.update;
	}

	paths {
		^m_paths;
	}

	// Perform the interleave
	interleave {
		var blockSize, numBlocks, tailBlockSize;
		var writeBuffer;
		var frameCheck;
		var inFiles, outPath, outFile;
		var headerFormat, sampleFormat;

		var processBlock = { | blockSize |
			var tmpBuffer = FloatArray.newClear(blockSize);
			inFiles.do({ | sf, chan |
				sf.readData(tmpBuffer);
				blockSize.do({ | frame |
					writeBuffer[frame * inFiles.size + chan] = tmpBuffer[frame];
				});
			});
			outFile.writeData(writeBuffer);
		};

		blockSize = 512;
		numBlocks = 0;
		tailBlockSize = 0;

		inFiles = List();

		// if we have any files
		if(m_paths.size > 0)
		{
			Dialog.savePanel({ | file |
				outPath = file;
				m_outPath = outPath;

				// make sure they all have the same numFrames and numChannels
				SoundFile.use(m_paths[0], { | sf |
					frameCheck = sf.numFrames;
					headerFormat = sf.headerFormat;
					sampleFormat = sf.sampleFormat;

					numBlocks = (frameCheck / blockSize).floor;
					tailBlockSize = frameCheck - (numBlocks * blockSize);
				});

				m_paths.do({ | path |
					var sf = SoundFile();

					sf.openRead(path);

					if(sf.numChannels != 1)
					{
						Error("Cannot interleave with multichannel file").throw;
					};
					if(sf.numFrames != frameCheck)
					{
						Error("Frame count mismatch - cannot interleave").throw;
					};

					// if we succeed, add to list
					inFiles.add(sf);
				});

				writeBuffer = FloatArray.newClear(blockSize * m_paths.size);

				outFile = SoundFile();
				outFile.headerFormat_(headerFormat);
				outFile.sampleFormat_(sampleFormat);
				outFile.numChannels_(m_paths.size);
				outFile.openWrite(outPath);

				numBlocks.do({
					processBlock.(blockSize);
				});

				// process final block
				processBlock.(tailBlockSize);

				inFiles.do(_.close); // close all the inFiles
				outFile.close; // close the outFile

				onInterleave.(this);

				m_gui.close;
			});
		}
	}

	outPath {
		^m_outPath;
	}

	gui {
		m_gui.display;
	}
}