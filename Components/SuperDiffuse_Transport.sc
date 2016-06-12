SuperDiffuse_Transport : SuperDiffuse_Observer {
	var sfView;
	*new { | subject |
		^super.new(subject).ninit;
	}

	ninit {
		this.display;
	}

	display {
		var win, layout, buttonLayout, backButton, playButton, forwardButton;
		var xPos, yPos, width, height, screenWidth, screenHeight;

		screenWidth = Window.screenBounds.width;
		screenHeight = Window.screenBounds.height;

		width = screenWidth * 0.75 - 60;
		height = screenHeight * 0.5;
		xPos = (screenWidth - width) - 20;
		yPos = (screenHeight - height) / 2;

		win = Window("SuperDiffuse | Transport", Rect(xPos,yPos,width,height));
		layout = VLayout();

		sfView = SoundFileView();
		sfView.gridOn_(false);
		sfView.timeCursorOn_(true);
		sfView.keyDownAction_({ | caller, modifiers, unicode, keycode|
			var start = caller.selections[0][0];
			var range = caller.selections[0][1];

			if(range == 0)
			{
				range = caller.numFrames - start;
			};

			case
			{keycode == 32} { this.play(start, range); }
			{keycode == 13} { caller.timeCursorPosition_(0); caller.setSelection(0,[0,0]); }
			;
		});

		layout.add(sfView);

		buttonLayout = HLayout();
		backButton = Button().states_([["<|"]]);

		playButton = SuperDiffuse_PlayStopButton().action_({| v |
			var start = sfView.selections[0][0];
			var range = sfView.selections[0][1];
			if(v.value == 1)
			{
				if(range == 0)
				{
					range = sfView.numFrames - start;
				};

				this.play(start,range);
			}
		});
		forwardButton = Button().states_([["|>"]]);

		buttonLayout.add(backButton);
		buttonLayout.add(playButton);
		buttonLayout.add(forwardButton);

		layout.add(buttonLayout);

		win.layout_(layout);
		win.front;

	}

	play { | start, range |
		var updateRes = 0.01;
		var updateRatio = 44100 * updateRes;
		var numUpdates, remainder;

		sfView.timeCursorPosition_(start);

		numUpdates = range/updateRatio;
		remainder = numUpdates - numUpdates.floor;

		{
			(numUpdates).do({
				(updateRes).wait;
				sfView.timeCursorPosition_(sfView.timeCursorPosition + updateRatio);
			});
			sfView.timeCursorPosition_(start);
		}.fork(AppClock);
	}

	update{ | caller |
		if(caller === m_subject)
		{
			SoundFile.use(caller.selectedPath,{ | sf |
				sfView.readFileWithTask(sf,showProgress:true);
			})
		}
	}
}