SuperDiffuse_ConcertGUI : SuperDiffuse_Observer {
	var m_parent;
	var m_win, m_mainLayout, m_leftLayout, m_buttonLayout, m_rightLayout, m_playbackControlsLayout;
	var m_listView, m_sfView, m_upButton, m_downButton, m_addButton, m_removeButton, m_backButton, m_playStopButton, m_forwardButton;

	*new { | parent |
		^super.new(parent).ninit(parent);
	}

	ninit { | parent |
		m_parent = parent;
		this.initWindow;
		this.update;
	}

	initWindow {
		var screenWidth, screenHeight, winWidth, winHeight, winX, winY;

		screenWidth = Window.screenBounds.width;
		screenHeight = Window.screenBounds.height;
		winWidth = screenWidth * 0.8;
		winHeight = screenHeight * 0.8;
		winX = (screenWidth / 2) - (winWidth / 2);
		winY = (screenHeight / 2 ) - (winHeight / 2);


		m_win = Window("SuperDiffuse | v.1", Rect(winX,winY,winWidth,winHeight));
		m_mainLayout = HLayout();
		m_leftLayout = VLayout();
		m_rightLayout = VLayout();

		m_buttonLayout = HLayout();
		m_playbackControlsLayout = HLayout();

		m_win.layout_(m_mainLayout);

		m_listView = ListView().action_({ | lv |
			this.updateSFView;
		})
		.keyDownAction_({ | caller, modifiers, unicode, keycode |
			if( (caller.selection[0] != nil) && (keycode == 101) )
			{
				var win, layout, fieldLayout, textEdit, buttonLayout, okButton, cancelButton;
				var sel, piece;

				sel = caller.selection[0];
				piece = m_parent.pieces[sel];

				win = Window("SuperDiffuse | Edit Piece Info");
				layout = VLayout();
				fieldLayout = HLayout();
				buttonLayout = HLayout();

				fieldLayout.add(StaticText().string_("Name: "));
				textEdit = TextField().string_( piece.name );
				fieldLayout.add(textEdit);

				cancelButton = Button().states_([["Cancel"]]).action_({
					win.close;
				});
				okButton = Button().states_([["OK"]]).action_({
					piece.name_(textEdit.string);
					this.updatePieces;
					caller.valueAction_(sel);
					win.close;
				});

				buttonLayout.add(cancelButton);
				buttonLayout.add(okButton);

				layout.add(fieldLayout);
				layout.add(buttonLayout);

				win.layout_(layout);
				win.front;
			}
		});

		m_leftLayout.add(m_listView);

		m_upButton = Button().states_([["^"]]).action_({ | btn |
			var sel;
			sel = m_listView.selection[0];
			if( (sel != 0) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel-1);
				this.updatePieces;
				m_listView.selection = sel - 1;
			}
		});

		m_downButton = Button().states_([["v"]]).action_({ | btn |
			var sel;
			sel = m_listView.selection[0];
			if( (sel != (m_parent.pieces.size-1)) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel + 1);
				this.updatePieces;
				m_listView.selection = sel + 1;
			}
		});

		m_addButton = Button().states_([["+"]]).action_({
			Dialog.openPanel({|v|
				v.do({ | path |
					m_parent.addPiece(SuperDiffuse_Piece(path));
				});
			},multipleSelection:true)
		});
		m_removeButton = Button().states_([["-"]]).action_({
			var sel;
			sel = m_listView.selection[0];

			if(sel != nil)
			{
				"Removing: %".format(m_parent.pieces[sel]).postln;
				m_parent.removePiece(m_parent.pieces[sel]);
				if(sel != 0)
				{
					m_listView.valueAction_(sel - 1);
				}
			}
		});

		m_buttonLayout.add(m_upButton);
		m_buttonLayout.add(m_downButton);
		m_buttonLayout.add(m_addButton);
		m_buttonLayout.add(m_removeButton);

		m_leftLayout.add(m_buttonLayout);

		m_mainLayout.add(m_leftLayout);
		m_mainLayout.setStretch(m_leftLayout,0);

		m_sfView = SoundFileView().minWidth_(500).gridOn_(false).timeCursorOn_(true).rmsColor_(Color.fromHexString("#63B76C")).peakColor_(Color.fromHexString("#4D8C57")).setSelectionColor(0,Color.fromHexString("#00CCCC"));
		m_rightLayout.add(m_sfView);

		m_backButton = Button().states_([["<"]]);
		m_playStopButton = SuperDiffuse_PlayStopToggle();
		m_forwardButton = Button().states_([[">"]]);

		m_playbackControlsLayout.add(m_backButton);
		m_playbackControlsLayout.add(m_playStopButton);
		m_playbackControlsLayout.add(m_forwardButton);

		m_rightLayout.add(m_playbackControlsLayout);

		m_mainLayout.add(m_rightLayout);
		m_mainLayout.setStretch(m_rightLayout,1);

		m_win.front;
	}

	updatePieces {
		m_listView.items = m_parent.pieces.collect({|piece| piece.name});
	}

	updateSFView {
		if(m_listView.selection[0] != nil)
		{
			m_sfView.timeCursorPosition_(0);
			SoundFile.use(m_parent.pieces[m_listView.selection[0]].path,{ | sf |
				m_sfView.readFileWithTask(sf,showProgress:true);
			});
		}
		{
			m_sfView.clear;
		}
	}

	update {
		this.updatePieces;
		this.updateSFView;
	}
}