// Base class for all custom GUI toggles
SuperDiffuse_Toggle : QUserView {
	var value;

	*new { | parent, bounds |
		^super.new(parent,bounds).init;
	}

	init {
		this.drawFunc_({ this.draw; });
		value = 0;
		this.minSize_(Size(30,30));
	}

	draw {
		var graphicWidth, graphicHeight;

		graphicWidth = this.bounds.width * 0.7;
		graphicHeight = this.bounds.height * 0.7;

		// Keeps graphic a square that fits in the bounds regardless of Widget size
		if(graphicWidth > graphicHeight) { graphicWidth = graphicHeight } { graphicHeight = graphicWidth};

		// Draw background
		Pen.fillColor = Color.grey(0.7);
		Pen.addRoundedRect(Rect(0,0, this.bounds.width,this.bounds.height),5,5);
		Pen.fill;

		// Draw graphic
		this.drawGraphic(graphicWidth, graphicHeight);

		// Draw widget outline
		Pen.width = 1;
		Pen.strokeColor = Color.black;
		Pen.addRoundedRect(Rect(0,0, this.bounds.width,this.bounds.height),5,5);
		Pen.stroke;
	}

	drawGraphic { | graphicWidth, graphicHeight |
		// Draw nothing - override in subclasses
	}

	valueAction_{ | val |
		this.value=val;
		this.doAction;
	}

	value {
		^value;
	}

	value_{ |val|
		value=val;
		this.refresh;
	}

	mouseUp{ | x, y, modifiers, buttonNumber, clickCount |
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		if( this.value == 0 ) { this.value = 1 } { this.value = 0 };
		this.doAction(this);
	}

	defaultKeyDownAction { | char, modifiers, unicode,keycode |
		^nil
	}

	defaultGetDrag {^value}
	defaultCanReceiveDrag  {^currentDrag.isNumber}
	defaultReceiveDrag { this.valueAction = currentDrag;}
}