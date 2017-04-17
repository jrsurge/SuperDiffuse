SuperDiffuse_PlayStopToggle : SuperDiffuse_Toggle {

	*new { | parent, bounds |
		^super.new(parent,bounds);
	}

	drawGraphic { | graphicWidth, graphicHeight |
		if(this.value == 1)
		{
			Pen.fillColor = Color.red(0.7);
			Pen.addRect(Rect((this.bounds.width / 2)-(graphicWidth / 2),(this.bounds.height / 2) - (graphicHeight / 2),graphicWidth,graphicHeight));
		}
		{
			Pen.strokeColor = Color.black;
			Pen.fillColor = Color.green(0.7);
			Pen.moveTo(Point((this.bounds.width / 2)-(graphicWidth / 2),(this.bounds.height / 2) - (graphicHeight / 2)));
			Pen.lineTo(Point((this.bounds.width / 2)-(graphicWidth / 2) + graphicWidth,(this.bounds.height / 2) - (graphicHeight / 2) + (graphicHeight * 0.5)));
			Pen.lineTo(Point((this.bounds.width / 2)-(graphicWidth / 2),(this.bounds.height / 2) + (graphicHeight * 0.5)));
			Pen.lineTo(Point((this.bounds.width / 2)-(graphicWidth / 2),(this.bounds.height / 2) - (graphicHeight / 2)));
		};
		Pen.draw(3);
	}
}