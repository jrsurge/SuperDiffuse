SuperDiffuse_ConcertGUI : SuperDiffuse_Observer {
	var m_parent;

	var m_win, m_layout;

	var m_meterBridge;

	var m_firstPiece;

	var m_piecesListView, m_pieceEditFunc, m_piecesUpButton, m_piecesDownButton, m_piecesAddButton, m_piecesRemoveButton;

	var m_matricesListView, m_matrixEditFunc, m_matrixAddButton, m_matrixRemoveButton;

	var m_controlsConfigButton, m_saveButton, m_midiConfigButton, m_filterConfigButton;

	var m_importButton;

	var m_masterVolumeSlider, m_masterVolumeNumberBox;

	var m_clock;

	var m_sfView;
	var m_playheadRoutine;

	var m_locked, m_sfViewHidden;

	*new { | parent |
		^super.new(parent).ninit(parent);
	}

	ninit { | parent |
		m_parent = parent;
		m_firstPiece = true;
		m_locked = false;
		m_sfViewHidden = false;

		this.initWindow;
		this.update;

		if(m_parent.pieces.size > 0)
		{
			this.loadFirstPiece;
		};
	}

	initWindow {
		var screenWidth, screenHeight, winWidth, winHeight, winX, winY;

		screenWidth = Window.screenBounds.width;
		screenHeight = Window.screenBounds.height;
		winWidth = screenWidth * 0.8;
		winHeight = screenHeight * 0.8;
		winX = (screenWidth / 2) - (winWidth / 2);
		winY = (screenHeight / 2 ) - (winHeight / 2);


		m_win = Window("SuperDiffuse | v" ++ SuperDiffuse.version, Rect(winX,winY,winWidth,winHeight));

		m_layout = GridLayout();

		this.prCreatePiecesView;
		this.prCreateMatricesView;
		this.prCreateControlsView;
		this.prCreateSfView;

		m_layout.setColumnStretch(0, 0);
		m_layout.setColumnStretch(1, 0);
		m_layout.setColumnStretch(2, 1);

		m_layout.setRowStretch(0, 0);
		m_layout.setRowStretch(1, 0);
		m_layout.setRowStretch(2, 0);
		m_layout.setRowStretch(3, 1);

		m_win.view.keyDownAction_({ | caller, modifiers, unicode, keycode |
			case
			{keycode == 32} { if(m_parent.isPlaying) { this.stop } { this.play }; }
			{keycode == 13} { m_sfView.timeCursorPosition_(0); m_clock.reset; m_sfView.setSelection(0,[0,0]); }
		});

		m_win.onClose_({
			this.stop;
			m_parent.clear;

			if(m_meterBridge.notNil)
			{
				m_meterBridge.free;
			};
		});

		m_win.layout_(m_layout);

		m_win.front;
	}

	prCreatePiecesView {
		m_pieceEditFunc = { | caller, char, modifiers, unicode, keycode, key |
			if(caller.hasFocus)
			{
				if( (caller.selection[0] != nil) && (key == 0x45) && (modifiers.isCtrl || modifiers.isCmd) && (m_locked == false))
				{
					var win, layout, nameLayout, textEdit, buttonLayout, okButton, cancelButton, matrixLayout, matrixMenu, filterLayout, filterMenu;
					var sel, piece;

					sel = caller.selection[0];
					piece = m_parent.pieces[sel];

					win = Window("SuperDiffuse | Edit Piece Info");
					layout = VLayout();
					nameLayout = HLayout();
					matrixLayout = HLayout();
					filterLayout = HLayout();
					buttonLayout = HLayout();

					nameLayout.add(StaticText().string_("Name: "));
					textEdit = TextField().string_( piece.name );
					nameLayout.add(textEdit);

					matrixLayout.add(StaticText().string_("Matrix:"));
					matrixMenu = PopUpMenu().items_(m_parent.matrices.collect({|matrix| matrix.name; })).value_(piece.matrixInd);

					matrixLayout.add(matrixMenu);

					filterLayout.add(StaticText().string_("Filter Set:"));
					filterMenu = PopUpMenu().items_(m_parent.filterManager.names).value_(piece.filterInd);
					filterLayout.add(filterMenu);

					cancelButton = Button().states_([["Cancel"]]).action_({
						win.close;
					});
					okButton = Button().states_([["OK"]]).action_({
						piece.name_(textEdit.string);
						piece.matrixInd_(matrixMenu.value);
						piece.filterInd_(filterMenu.value);
						m_parent.filterManager.load(piece.filterInd);
						this.updatePieces;
						caller.value_(sel);
						m_matricesListView.valueAction_(piece.matrixInd);
						this.updateMatrices;
						win.close;
						m_parent.createSaveFile(m_parent.saveFileLoc);
					});

					buttonLayout.add(cancelButton);
					buttonLayout.add(okButton);

					layout.add(nameLayout);
					layout.add(matrixLayout);
					layout.add(filterLayout);
					layout.add(buttonLayout);

					win.layout_(layout);
					win.front;
				}
				{
					if( (caller.selection[0] != nil) && (key == 0x20) )
					{
						if(m_parent.isPlaying)
						{
							this.stop;
						}
						{
							this.play(caller.selection[0]);
						};
					}
				}
			};
		};

		m_piecesListView = ListView().action_({ | lv |
			this.stop;
			m_matricesListView.valueAction_(m_parent.pieces[lv.selection[0]].matrixInd);

			if(lv.selection[0] != nil)
			{
				m_masterVolumeNumberBox.valueAction_(m_parent.pieces[lv.selection[0]].masterLevel);
				m_parent.filterManager.load(m_parent.pieces[lv.selection[0]].filterInd);
			};

			this.updateSFView;
			m_sfView.setSelection(0, [0,0]);
			m_sfView.timeCursorPosition_(0);
			m_clock.reset;
			lv.focus;
		})
		.keyDownAction_(m_pieceEditFunc);

		m_piecesAddButton = Button().minWidth_(10).states_([["+"]]).action_({
			Dialog.openPanel({|v|
				var sizeBefore, sizeAfter;

				sizeBefore = m_parent.pieces.size;

				v.do({ | path |
					m_parent.addPiece(SuperDiffuse_Piece(path));

				});

				sizeAfter = m_parent.pieces.size;

				if((sizeBefore == 0) && (sizeAfter > 0))
				{
					m_piecesListView.valueAction_(0);
				};
			},multipleSelection:true);
			m_parent.createSaveFile(m_parent.saveFileLoc);

			m_piecesListView.focus;
		});

		m_piecesRemoveButton = Button().minWidth_(10).states_([["-"]]).action_({
			var sel;
			sel = m_piecesListView.selection[0];

			if(sel != nil)
			{
				m_parent.removePiece(m_parent.pieces[sel]);

				if(sel != 0)
				{
					m_piecesListView.valueAction_(sel - 1);
				};

				m_parent.createSaveFile(m_parent.saveFileLoc);
			};

			m_piecesListView.focus;
		});

		m_piecesUpButton = Button().minWidth_(10).states_([["^"]]).action_({ | btn |
			var sel;
			sel = m_piecesListView.selection[0];
			if( (sel != 0) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel-1);
				this.updatePieces;
				m_piecesListView.selection = sel - 1;
				m_parent.createSaveFile(m_parent.saveFileLoc);
			};

			m_piecesListView.focus;
		});

		m_piecesDownButton = Button().minWidth_(10).states_([["v"]]).action_({ | btn |
			var sel;
			sel = m_piecesListView.selection[0];
			if( (sel != (m_parent.pieces.size-1)) && (sel != nil) )
			{
				m_parent.pieces.swap(sel,sel + 1);
				this.updatePieces;
				m_piecesListView.value = sel + 1;
				m_parent.createSaveFile(m_parent.saveFileLoc);
			};

			m_piecesListView.focus;
		});

		m_layout.addSpanning(
			VLayout(
				StaticText().string_("Pieces").align_(\center),
				m_piecesListView,
				HLayout(
					m_piecesAddButton,
					m_piecesRemoveButton,
					m_piecesUpButton,
					m_piecesDownButton,
				)
			),
			row:0,
			column:0,
			rowSpan:4,
			columnSpan:1
		);
	}

	prCreateMatricesView {
		m_matrixEditFunc = { | caller, char, modifiers, unicode, keycode, key |
			if(caller.hasFocus)
			{
				var selectedMatrix = caller.selection[0];

				if(selectedMatrix != nil)
				{
					case
					{ key == 0x20 } {
						if(m_parent.isPlaying)
						{
							this.stop;
						}
						{
							if(m_piecesListView.selection[0] != nil)
							{
								this.play(m_piecesListView.selection[0]);
							}
						};
					}
					{ (key == 0x45) && (modifiers.isCtrl || modifiers.isCmd) && (m_locked == false) } {
						var win, layout, fieldLayout, textEdit, buttonLayout, matrixLayout, okButton, cancelButton, matrixScrollView, matrixScrollCanvas;
						var sel, matrix;
						var tmpMatrix;
						var setMouseWheelAction = false;

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

							m_parent.createSaveFile(m_parent.saveFileLoc);
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

						if(Main.scVersionMajor >= 3)
						{
							if(Main.scVersionMinor >= 10)
							{
								var patch = Main.scVersionPostfix.drop(1).asInteger;
								if(Main.scVersionMinor == 10)
								{
									if(patch >= 3)
									{
										setMouseWheelAction = true;
									}
								}
								{
									setMouseWheelAction = true;
								};
							};
						};

						matrix.matrix.size.do({ | in |
							matrixLayout.add(StaticText().string_("In%".format(in+1)).align_(\center), in+1, 0);
							matrix.matrix[0].size.do({ | out |
								var numBox;

								if(in == 0)
								{
									matrixLayout.add(StaticText().string_("Out%".format(out+1)).align_(\center), 0, out+1);
								};

								numBox = NumberBox()
								.align_(\center)
								.clipLo_(0)
								.value_(tmpMatrix.matrix[in][out])
								.action_({|caller|
									tmpMatrix.matrix[in][out] = caller.value;

									if(caller.value > 1)
									{
										caller.background_(Color.red);
									}
									{
										if(caller.value > 0)
										{
											caller.background_(Color.green(1, caller.value));
										}
										{
											caller.background_(Color.white);
										};
									};
								})
								.toolTip_("%:%".format(in+1, out+1));

								if(setMouseWheelAction)
								{
									numBox.mouseWheelAction_({ | caller, x, y, mod, xDelta, yDelta |
										var retVal = false;

										// only respond to yDelta - xDelta props
										if(yDelta > 0)
										{
											caller.valueAction_(caller.value + 1);
											retVal = true;
										};

										if(yDelta < 0)
										{
											caller.valueAction_(caller.value - 1);
											retVal = true;
										};


										retVal;
									});
								};

								if(tmpMatrix.matrix[in][out] > 1)
								{
									numBox.background_(Color.red);
								}
								{
									if(tmpMatrix.matrix[in][out] > 0)
									{
										numBox.background_(Color.green(1,tmpMatrix.matrix[in][out]));
									}
									{
										numBox.background_(Color.white);
									};
								};

								matrixLayout.add(numBox,in+1,out+1);
							});
						});

						layout.add(matrixScrollView);

						layout.add(buttonLayout);

						win.layout_(layout);
						win.front;
					}
					{ (key == 0x44) && (modifiers.isCtrl || modifiers.isCmd) } {
						m_parent.addMatrix("copy", selectedMatrix);
					};
				};
			};
		};

		m_matricesListView = ListView().action_({ | caller |
			var sel;

			sel =  caller.selection[0];
			m_parent.loadMatrix(m_parent.matrix(sel));
		})
		.keyDownAction_(m_matrixEditFunc);

		m_matrixAddButton = Button().minWidth_(10).states_([["+"]]).action_({
			var sel;
			sel = m_matricesListView.selection[0];
			m_parent.addMatrix("Untitled");
			m_matricesListView.value_(sel);
			m_parent.createSaveFile(m_parent.saveFileLoc);
			m_matricesListView.focus;
		});

		m_matrixRemoveButton = Button().minWidth_(10).states_([["-"]]).action_({
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
					});
					m_parent.createSaveFile(m_parent.saveFileLoc);
				}
				{
					"Unable to remove initial matrix".warn;
				}
			};

			m_matricesListView.focus;
		});

		m_layout.addSpanning(
			VLayout(
				StaticText().string_("Matrices").align_(\center),
				m_matricesListView,
				HLayout(
					m_matrixAddButton,
					m_matrixRemoveButton
				)
			),
			row: 0,
			column: 1,
			rowSpan: 4,
			columnSpan: 1
		);
	}

	prCreateControlsView {
		m_masterVolumeNumberBox = NumberBox().maxWidth_(50).action_({ | caller |
			m_parent.setMasterLevel(caller.value ** 2);
			m_masterVolumeSlider.value_(caller.value);

			if(m_piecesListView.selection[0] != nil)
			{
				m_parent.pieces[m_piecesListView.selection[0]].masterLevel_(caller.value);
			};

			if(caller.hasFocus)
			{
				m_parent.createSaveFile(m_parent.saveFileLoc);
			};

			m_sfView.focus;
		});

		m_masterVolumeSlider = Slider().orientation_(\horizontal).action_({ | caller |
			m_masterVolumeNumberBox.valueAction_(caller.value);
		}).mouseUpAction_({
			m_parent.createSaveFile(m_parent.saveFileLoc);
		});

		if(Main.versionAtLeast(3,9))
		{
			m_meterBridge = SuperDiffuse_LevelMeters(m_parent.numOuts);
		}
		{
			"The MeterBridge requires SuperCollider >= 3.9.3".warn;

			m_meterBridge = StaticText().string_("MeterBridge (requires SuperCollider version >= 3.9.3)").align_(\center);
		};


		m_controlsConfigButton = Button().states_([["Configure Control Faders"]]).action_({m_parent.configureOutFaders(); m_sfView.focus;});
		m_saveButton = Button()
		.states_([["Save Concert Configuration"]])
		.mouseDownAction_({ | caller, x, y, modifiers, buttonNumber, clickCount |
			if((m_parent.saveFileLoc == "") || (clickCount > 1) )
			{
				Dialog.savePanel({ | path |
					m_parent.createSaveFile(path);
				});
				m_sfView.focus;
			}
			{
				m_parent.createSaveFile(m_parent.saveFileLoc);
			};
		})
		.mouseUpAction_({m_sfView.focus;});

		m_importButton = Button()
		.states_([["Import.."]])
		.mouseDownAction_({
			FileDialog({ | paths |
				SuperDiffuse_Import(m_parent, paths[0]);
			},{}, path: Platform.userHomeDir);

			m_sfView.focus;
		});


		m_midiConfigButton = Button().states_([["Configure MIDI"]]).action_({m_parent.configureMIDI; m_sfView.focus;});

		m_filterConfigButton = Button().states_([["Configure Filters"]]).action_({m_parent.configureFilters; m_sfView.focus; });

		m_clock = SuperDiffuse_Clock();

		m_layout.addSpanning(
			m_parent.controls.gui,
			row: 0,
			column: 2,
			rowSpan: 1,
			columnSpan: 1
		);

		m_layout.addSpanning(
			if(Main.versionAtLeast(3,9))
			{
				m_meterBridge.view
			}
			{
				m_meterBridge;
			},
			row: 1,
			column: 2,
			rowSpan: 1,
			columnSpan: 1
		);

		m_layout.addSpanning(
			HLayout(
				m_controlsConfigButton,
				m_midiConfigButton,
				m_filterConfigButton,
				m_saveButton,
				m_importButton,
				Button().states_([["Lock Interface"],["Unlock Interface"], ["Unlock Interface"]])
				.action_({ | caller |
					this.lockInterface(caller.value > 0); m_sfView.focus;
				}),
				Button().states_([["Hide Waveform"], ["Show Waveform"]]).action_({| caller |
					var tglState = (1 - caller.value).asBoolean;

					m_sfViewHidden = caller.value.asBoolean;

					m_sfView.drawsWaveForm_(tglState);
					m_sfView.timeCursorOn_(tglState);

					if(m_locked == false)
					{
						m_sfView.acceptsMouse_(tglState);
					};

					m_sfView.focus;

				})
			),
			row: 2,
			column: 2,
			rowSpan: 1,
			columnSpan: 1
		);

		m_layout.addSpanning(
			HLayout(
				nil,
				m_clock.gui,
				nil,
				StaticText().string_("Master:"),
				m_masterVolumeSlider,
				m_masterVolumeNumberBox,
			).margins_([0,15,0,15]),
			row: 3,
			column: 2,
			rowSpan: 1,
			columnSpan: 1
		);
	}

	prCreateSfView {
		m_sfView = SoundFileView()
		.minWidth_(500)
		.gridOn_(false)
		.timeCursorOn_(true)
		.timeCursorColor_(Color.fromHexString("#00C853"))
		.rmsColor_(Color.fromHexString("#FF9800"))
		.peakColor_(Color.fromHexString("#FF6D00"))
		.setSelectionColor(0,Color.fromHexString("#3F51B5"))
		.mouseUpAction_({ | caller, x, y, modifiers, buttonNumber |
			if(buttonNumber == 0)
			{
				if(m_parent.isPlaying)
				{
					this.stop;
					this.play;
				}
				{
					m_clock.setTimeInSamples(caller.timeCursorPosition);
				};
			}
		});

		m_layout.addSpanning(
			m_sfView,
			row: 4,
			column: 0,
			rowSpan: 1,
			columnSpan: 3
		);
	}

	lockInterface { | state |
		var invState = state.not;

		m_locked = state.asBoolean;

		m_matricesListView.enabled_(invState);
		m_matrixAddButton.enabled_(invState);
		m_matrixRemoveButton.enabled_(invState);
		m_piecesUpButton.enabled_(invState);
		m_piecesDownButton.enabled_(invState);
		m_piecesAddButton.enabled_(invState);
		m_piecesRemoveButton.enabled_(invState);
		m_controlsConfigButton.enabled_(invState);
		m_filterConfigButton.enabled_(invState);
		m_midiConfigButton.enabled_(invState);
		m_saveButton.enabled_(invState);
		m_importButton.enabled_(invState);

		if(state.asBoolean)
		{
			m_sfView.acceptsMouse_(false);

		}
		{
			if(m_sfViewHidden == false)
			{
				m_sfView.acceptsMouse_(true);
			};
		};

		m_sfView.focus;
	}

	updatePieces {
		m_piecesListView.items = m_parent.pieces.collect({|piece| piece.name});
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

	updateWindowTitle {
		var winTitle = "SuperDiffuse | v" ++ SuperDiffuse.version;

		if(m_parent.saveFileLoc != "")
		{
			winTitle = winTitle ++ " - " ++ PathName(m_parent.saveFileLoc).fileNameWithoutExtension;
		};

		m_win.name = winTitle;
	}

	update { | notifyType |
		switch(notifyType)
		{\pieceAdded}{
			this.updatePieces;
			if(m_firstPiece)
			{
				this.loadFirstPiece;
			}
		}
		{\pieceRemoved}{ this.updatePieces; this.updateSFView; }
		{\pieceChanged}{ this.updateSFView; }
		{\matrixAdded}{ this.updateMatrices; }
		{\matrixRemoved}{ this.updateMatrices; }
		{\saveFileLocChanged}{ this.updateWindowTitle; }
		// default:
		{
			this.updatePieces;
			this.updateMatrices;
			this.updateSFView;
			this.updateWindowTitle;
		}
	}

	updatePlayhead { | start, end, sampleRate |
		var playheadPos;

		if(m_playheadRoutine != nil)
		{
			m_playheadRoutine.stop;
		};

		m_sfView.timeCursorPosition_(start);
		playheadPos = start;

		m_playheadRoutine = Routine({
			var numFrames = end - start;

			Server.default.latency.wait;

			m_clock.setSampleRate(sampleRate);

			(numFrames/10).do({
				(10 / sampleRate).wait;
				if(m_sfView.isClosed) { thisThread.stop; };
				{m_sfView.timeCursorPosition_(playheadPos)}.defer;
				{m_clock.setTimeInSamples(m_sfView.timeCursorPosition)}.defer;
				playheadPos = playheadPos + 10;
			});
			{m_sfView.timeCursorPosition_(start); m_clock.setTimeInSamples(start); }.defer;
		});
		m_playheadRoutine.play(SystemClock);
	}

	loadFirstPiece {
		if(m_piecesListView.items.size > 0)
		{
			m_piecesListView.valueAction_(0);
			m_firstPiece = false;
		}
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
			this.updatePlayhead(start,end, m_parent.pieces[index].sampleRate);
		};
	}

	stop {
		if(m_parent.isPlaying)
		{
			m_parent.stop;
			m_playheadRoutine.stop;
			SystemClock.clear;
			{
				m_sfView.timeCursorPosition_(m_sfView.selections[0][0]);
				m_clock.setTimeInSamples(m_sfView.timeCursorPosition);
			}.defer(0.01);
		}
	}
}
