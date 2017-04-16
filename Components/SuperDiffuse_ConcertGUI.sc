SuperDiffuse_ConcertGUI : SuperDiffuse_Observer {
	var m_parent;

	var m_win, m_mainLayout, m_leftLayout, m_piecesLayout, m_piecesButtonLayout, m_matricesLayout, m_matricesButtonLayout, m_rightLayout, m_playbackControlsLayout;

	var m_piecesListView, m_piecesUpButton, m_piecesDownButton, m_piecesAddButton, m_piecesRemoveButton;

	var m_matricesListView, m_matrixAddButton, m_matrixRemoveButton;

	var m_sfView;
	var m_backButton, m_playStopButton, m_forwardButton;

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
		m_leftLayout = HLayout();
		m_piecesLayout = VLayout();
		m_matricesLayout = VLayout();
		m_rightLayout = VLayout();

		m_piecesButtonLayout = HLayout();
		m_matricesButtonLayout = HLayout();
		m_playbackControlsLayout = HLayout();

		m_win.layout_(m_mainLayout);

		m_piecesListView = ListView().action_({ | lv |
			this.updateSFView;
			m_matricesListView.valueAction_(m_parent.pieces[lv.selection[0]].matrixInd);
		})
		.keyDownAction_({ | caller, modifiers, unicode, keycode |
			if(caller.hasFocus)
			{
				if( (caller.selection[0] != nil) && (keycode == 101) )
				{
					var win, layout, nameLayout, textEdit, buttonLayout, okButton, cancelButton, matrixLayout, matrixMenu;
					var sel, piece;

					sel = caller.selection[0];
					piece = m_parent.pieces[sel];

					win = Window("SuperDiffuse | Edit Piece Info");
					layout = VLayout();
					nameLayout = HLayout();
					matrixLayout = HLayout();
					buttonLayout = HLayout();

					nameLayout.add(StaticText().string_("Name: "));
					textEdit = TextField().string_( piece.name );
					nameLayout.add(textEdit);

					matrixLayout.add(StaticText().string_("Matrix:"));
					matrixMenu = PopUpMenu().items_(m_parent.matrices.collect({|matrix| matrix.name; })).value_(piece.matrixInd);

					matrixLayout.add(matrixMenu);

					cancelButton = Button().states_([["Cancel"]]).action_({
						win.close;
					});
					okButton = Button().states_([["OK"]]).action_({
						piece.name_(textEdit.string);
						piece.matrixInd_(matrixMenu.value);
						this.updatePieces;
						caller.value_(sel);
						m_matricesListView.valueAction_(piece.matrixInd);
						this.updateMatrices;
						win.close;
					});

					buttonLayout.add(cancelButton);
					buttonLayout.add(okButton);

					layout.add(nameLayout);
					layout.add(matrixLayout);
					layout.add(buttonLayout);

					win.layout_(layout);
					win.front;
				};
				if( (caller.selection[0] != nil) && (keycode == 32) )
				{
					if(m_parent.isPlaying)
					{
						this.stop;
					}
					{
						this.play(caller.selection[0]);
					};
				}
			};
		});

		m_piecesLayout.add(StaticText().string_("Pieces"),align:\center);
		m_piecesLayout.add(m_piecesListView);

		m_piecesUpButton = Button().states_([["^"]]).action_({ | btn |
			var sel;
			sel = m_piecesListView.selection[0];
			if( (sel != 0) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel-1);
				this.updatePieces;
				m_piecesListView.selection = sel - 1;
			};
		});

		m_piecesDownButton = Button().states_([["v"]]).action_({ | btn |
			var sel;
			sel = m_piecesListView.selection[0];
			if( (sel != (m_parent.pieces.size-1)) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel + 1);
				this.updatePieces;
				m_piecesListView.selection = sel + 1;
			};
		});

		m_piecesAddButton = Button().states_([["+"]]).action_({
			Dialog.openPanel({|v|
				v.do({ | path |
					m_parent.addPiece(SuperDiffuse_Piece(path));
				});
			},multipleSelection:true)
		});
		m_piecesRemoveButton = Button().states_([["-"]]).action_({
			var sel;
			sel = m_piecesListView.selection[0];

			if(sel != nil)
			{
				m_parent.removePiece(m_parent.pieces[sel]);
				if(sel != 0)
				{
					m_piecesListView.valueAction_(sel - 1);
				}
			};
		});

		m_piecesButtonLayout.add(m_piecesUpButton);
		m_piecesButtonLayout.add(m_piecesDownButton);
		m_piecesButtonLayout.add(m_piecesAddButton);
		m_piecesButtonLayout.add(m_piecesRemoveButton);

		m_piecesLayout.add(m_piecesButtonLayout);

		m_leftLayout.add(m_piecesLayout);

		m_matricesListView = ListView().action_({ | caller |
			var sel;

			sel =  caller.selection[0];
			m_parent.loadMatrix(m_parent.matrix(sel));
		})
		.keyDownAction_({ | caller, modifiers, unicode, keycode |
			if(caller.hasFocus)
			{
				if( (caller.selection[0] != nil) && (keycode == 101) )
				{
					var win, layout, fieldLayout, textEdit, buttonLayout, matrixLayout, okButton, cancelButton, matrixScrollView, matrixScrollCanvas;
					var sel, matrix;
					var tmpMatrix;

					sel = caller.selection[0];
					matrix = m_parent.matrix(sel);
					tmpMatrix = SuperDiffuse_Matrix.newFrom(matrix,"tmp");

					win = Window("SuperDiffuse | Edit Matrix");
					layout = VLayout();
					fieldLayout = HLayout();
					buttonLayout = HLayout();


					fieldLayout.add(StaticText().string_("Name: "));
					textEdit = TextField().string_(matrix.name);
					fieldLayout.add(textEdit);

					cancelButton = Button().states_([["Cancel"]]).action_({
						win.close;
					});
					okButton = Button().states_([["OK"]]).action_({
						if(textEdit.string != matrix.name)
						{
							matrix.name = textEdit.string;

							caller.value_(sel);

						};
						matrix.matrix = tmpMatrix.matrix;
						m_parent.loadMatrix(matrix);

						this.updateMatrices;

						win.close;
					});

					buttonLayout.add(cancelButton);
					buttonLayout.add(okButton);

					layout.add(fieldLayout);


					matrixScrollView = ScrollView();
					matrixScrollCanvas = View();
					matrixScrollView.canvas_(matrixScrollCanvas);

					matrixLayout = GridLayout();
					matrixScrollCanvas.layout_(matrixLayout);

					matrix.matrix.size.do({ | in |
						matrix.matrix[0].size.do({ | out |
							matrixLayout.add(StaticText().string_("In%".format(in+1)).align_(\center), in+1, 0);
							matrixLayout.add(StaticText().string_("Out%".format(out+1)).align_(\center), 0, out+1);
							matrixLayout.add(SuperDiffuse_PatchToggle().value_(tmpMatrix.matrix[in][out]).action_({|caller| tmpMatrix.matrix[in][out] = caller.value; }).toolTip_("%:%".format(in+1, out+1)),in+1,out+1);
						});
					});

					layout.add(matrixScrollView);

					layout.add(buttonLayout);

					win.layout_(layout);
					win.front;
				};
			};
		});

		m_matricesLayout.add(StaticText().string_("Matrices"),align:\center);
		m_matricesLayout.add(m_matricesListView);

		m_matrixAddButton = Button().states_([["+"]]).action_({
			var sel;
			sel = m_matricesListView.selection[0];
			m_parent.addMatrix("Untitled");
			m_matricesListView.value_(sel);
		});

		m_matricesButtonLayout.add(m_matrixAddButton);

		m_matrixRemoveButton = Button().states_([["-"]]).action_({
			var sel;

			sel = m_matricesListView.selection[0];

			if(sel != nil)
			{
				if(sel != 0)
				{
					m_parent.removeMatrix(m_parent.matrix(sel));
					m_matricesListView.valueAction_(sel - 1);
					m_parent.pieces.do({ | piece |
						if(piece.matrixInd == sel)
						{
							piece.matrixInd = 0;
						}
					})
				}
				{
					"Unable to remove initial matrix".warn;
				}
			};

		});

		m_matricesButtonLayout.add(m_matrixRemoveButton);

		m_matricesLayout.add(m_matricesButtonLayout);

		m_leftLayout.add(m_matricesLayout);


		m_mainLayout.add(m_leftLayout);
		m_mainLayout.setStretch(m_leftLayout,0);

		m_sfView = SoundFileView()
		.minWidth_(500)
		.gridOn_(false)
		.timeCursorOn_(true)
		.timeCursorColor_(Color.fromHexString("#00C853"))
		.rmsColor_(Color.fromHexString("#FF9800"))
		.peakColor_(Color.fromHexString("#FF6D00"))
		.setSelectionColor(0,Color.fromHexString("#3F51B5"))
		.keyDownAction_({ | caller, modifiers, unicode, keycode|
			case
			{keycode == 32} { if(m_parent.isPlaying) { this.stop } { this.play }; }
			{keycode == 13} { caller.timeCursorPosition_(0); caller.setSelection(0,[0,0]); }
			;
		});

		m_rightLayout.add(m_parent.controls.gui,1);

		m_rightLayout.add(Button().states_([["Configure Control Faders"]]).action_({m_parent.configureOutFaders()}));
		m_rightLayout.add(Button().states_([["Save Concert Configuration"]]).action_({
			Dialog.savePanel({ | path |
				m_parent.createSaveFile(path);
			});
		}));

		m_rightLayout.add(m_sfView,3);

		m_backButton = Button().states_([["<"]]);
		m_playStopButton = SuperDiffuse_PlayStopToggle();
		m_forwardButton = Button().states_([[">"]]);

		m_playbackControlsLayout.add(m_backButton);
		m_playbackControlsLayout.add(m_playStopButton);
		m_playbackControlsLayout.add(m_forwardButton);

		//m_rightLayout.add(m_playbackControlsLayout);

		m_mainLayout.add(m_rightLayout);
		m_mainLayout.setStretch(m_rightLayout,1);

		m_win.onClose_({ m_parent.clear; });

		m_win.front;
	}

	updatePieces {
		var sel;
		sel = m_piecesListView.selection[0];
		m_piecesListView.items = m_parent.pieces.collect({|piece| piece.name});
		if(sel != nil)
		{
			m_piecesListView.value_(sel);
		};
	}

	updateMatrices {
		var sel;
		sel = m_matricesListView.selection[0];
		m_matricesListView.items = m_parent.matrices.collect({|matrix| matrix.name});
		m_matricesListView.value_(sel);
	}

	updateSFView {
		if(m_piecesListView.selection[0] != nil)
		{
			m_sfView.timeCursorPosition_(0);
			SoundFile.use(m_parent.pieces[m_piecesListView.selection[0]].path,{ | sf |
				m_sfView.readFileWithTask(sf);
			});
		}
		{
			m_sfView.data_([0]);
		}
	}

	update {
		this.updatePieces;
		this.updateMatrices;
		this.updateSFView;
	}

	ready {
		m_piecesListView.valueAction_(0);
	}

	play { | index |
		var start, range, end;

		// if we don't provide an index, play whatever the listview is pointing at
		if(index == nil)
		{
			index = m_piecesListView.value;
		};

		// only try to play something if there is something
		if(index != nil)
		{
			start = m_sfView.selections[0][0];
			if(m_sfView.selections[0][1] == 0) { range =  m_sfView.numFrames - start; } { range = m_sfView.selections[0][1]; };
			end = start + range;

			m_parent.play(index, start, end);
		}
	}

	stop {
		m_parent.stop;
	}
}