SuperDiffuse_PatchToggle : SuperDiffuse_Toggle {

	*new { | parent, bounds |
		^super.new(parent,bounds);
	}

	drawGraphic { | graphicWidth, graphicHeight |
		if(this.value == 1)
		{
			Pen.fillColor = Color.red;
			Pen.addOval(Rect((this.bounds.width / 2)-(graphicWidth / 2),(this.bounds.height / 2) - (graphicHeight / 2),graphicWidth,graphicHeight));
		};
		Pen.draw(3);
	}
}