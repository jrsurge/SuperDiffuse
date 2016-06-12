SuperDiffuse_FilePool : SuperDiffuse_Subject {

	classvar files, selectedFileInd;

	*new {
		^super.new.ninit;
	}

	*initClass {
		files = List();
	}

	ninit {
		selectedFileInd = nil;
		this.display;
	}

	selectedPath {
		^files[selectedFileInd];
	}

	display {
		var win, layout, listView, buttonsLayout, upButton, downButton, addButton, removeButton;
		var xPos, yPos, width, height, screenWidth, screenHeight;

		screenWidth = Window.screenBounds.width;
		screenHeight = Window.screenBounds.height;

		width = screenWidth * 0.25;
		height = screenHeight * 0.8;
		xPos = 20;
		yPos = (screenHeight - height) / 2;

		win = Window("SuperDiffuse | FilePool",Rect(xPos,yPos,width,height));
		layout = VLayout();

		listView = ListView().action_({ | lv |
			var sel;
			sel = lv.selection[0];
			selectedFileInd = sel;
			this.notify;
		});
		listView.items = files.collect({|f| f.asString});
		layout.add(listView);

		buttonsLayout = HLayout();

		upButton = Button().states_([["^"]]).action_({
			var sel;
			sel = listView.selection[0];
			if( (sel != 0) && (sel != nil) )
			{
				files.swap(sel,sel-1);
				listView.items = files.asArray;
				listView.selection = sel - 1;
			};
		});
		downButton = Button().states_([["v"]]).action_({
			var sel;
			sel = listView.selection[0];
			if( (sel != (files.size-1)) && (sel != nil) )
			{
				files.swap(sel,sel+1);
				listView.items = files.asArray;
				listView.selection = sel + 1;
			};
		});
		addButton = Button().states_([["+"]]).action_({
			Dialog.openPanel({|v|
				v.do({ | path |
					if(files.includesEqual(path))
					{
						"Duplicate".postln
					}
					{
						files.add(path);
						listView.items = files.asArray;
					};
				});
			},multipleSelection:true)
		});
		removeButton = Button().states_([["-"]]).action_({
			var sel;
			sel = listView.selection[0];
			if(sel != nil)
			{
				files.removeAt(sel);
				listView.items = files.asArray;
				if(sel != 0)
				{
					listView.selection = sel - 1;
				}
			}
		});

		buttonsLayout.add(upButton);
		buttonsLayout.add(downButton);
		buttonsLayout.add(addButton);
		buttonsLayout.add(removeButton);

		buttonsLayout.add(SuperDiffuse_PatchToggle());

		layout.add(buttonsLayout);
		win.layout_(layout);
		win.front;
	}
}