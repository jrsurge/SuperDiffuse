// SDPK File header:
//
// Identification (char[4]): SDPK
// numFrames (int32)
// numChannels (int32)
// compressionRatio (int32)
// data (float[numFrames * numChannels])
//

SuperDiffuse_PeakFile
{
	var <data;
	var <numFrames, <numChannels, <compressionRatio;
	var <>path;

	*new {
		^super.new;
	}

	*write { | inPath, outPath, compressionRatio = 4 |

		var inFile = SoundFile.openRead(inPath);
		var outFile = File(outPath, "wb");

		var numFrames  = inFile.numFrames;
		var numChannels = inFile.numChannels;

		var blockSize = min(1024, numFrames);
		var numBlocks = (numFrames / blockSize).floor;
		var finalBlockSize = numFrames - (numBlocks * blockSize);

		var inData = FloatArray.newClear(blockSize * numChannels);

		if(inPath.notNil && outPath.notNil)
		{
			// If we've got everything we need:


			// Write the header
			"SDPK".do({ | c |
				outFile.putChar(c);
			});

			outFile.putInt32(numFrames / compressionRatio);
			outFile.putInt32(numChannels);
			outFile.putInt32(compressionRatio);

			// Write the data
			numBlocks.do({ | block |
				inFile.readData(inData);

				(blockSize / compressionRatio).do({ | frame |
					numChannels.do({ |channel |
						outFile.putFloat(inData[frame * compressionRatio * numChannels + channel]);
					});
				});
			});

			inFile.readData(inData); // inData should now be the right size
			(finalBlockSize / compressionRatio).do({ | frame |
				numChannels.do({ |channel |
					outFile.putFloat(inData[frame * compressionRatio * numChannels + channel]);
				});
			});

			// close all the files
			inFile.close;
			outFile.close;
		}
	}

	read { | path |
		var file = File(path, "rb");

		"SDPK".do({ | c |
			if(file.getChar != c)
			{
				Error("Not an SDPK file").throw;
			}
		});

		numFrames = file.getInt32;
		numChannels = file.getInt32;
		compressionRatio = file.getInt32;

		numFrames.postln;
		numChannels.postln;
		compressionRatio.postln;

		data = FloatArray.newClear(numFrames * numChannels);

		data.size.do({ | sample |
			data[sample] = file.getFloat(data);
		});

		file.close;
	}
}