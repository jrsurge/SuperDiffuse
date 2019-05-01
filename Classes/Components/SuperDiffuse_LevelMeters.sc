SuperDiffuse_LevelMeters
{
	classvar cl_responder;

	var m_numOuts;
	var m_levelMeters;

	*new { | numOuts |
		^super.new.init(numOuts);
	}

	*initClass {
		cl_responder = nil; // lazy init
	}

	init { | numOuts |
		m_numOuts = numOuts;

		m_levelMeters = numOuts.collect({
			LevelIndicator()
			.warning_(0.6)
			.critical_(0.95)
			.drawsPeak_(true)
			.style_(\led)
			.maxHeight_(100)
			.numSteps_(32)
		});

		if(cl_responder.isNil)
		{
			cl_responder = OSCFunc({ | msg |
				m_levelMeters.do({ | meter, index |
					var peakVal = msg[2 * index + 3];
					var rmsVal = msg[2 * index + 4];

					AppClock.sched(0, {
						meter.peakLevel_(peakVal.ampdb.linlin(-80, 0, 0, 1, \min));
						meter.value_(rmsVal.ampdb.linlin(-80, 0, 0, 1));
					});
				});
			}, '/SuperDiffuse/OutLevels');
		};
	}

	view {
		var view = View();
		var layout = HLayout();

		view.onResize_({
			m_levelMeters.do(_.numSteps_(32));
		});

		m_levelMeters.do({ | meter |
			layout.add(meter);
		});

		view.layout_(layout);
		^view;
	}

	free {
		cl_responder.free;
		cl_responder = nil;
	}
}