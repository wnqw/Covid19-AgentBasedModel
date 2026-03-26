package NetworkModel;

import java.awt.Color;
import java.awt.Paint;
import sim.display.Controller;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sweep.GUIStateSweep;
import sweep.SimStateSweep;

public class GUICN extends GUIStateSweep {
	NetworkPortrayal2D edgePortrayal = new NetworkPortrayal2D();


	public GUICN(SimStateSweep state, int gridWidth, int gridHeight, Color backdrop, Color agentDefaultColor,
			boolean agentPortrayal) {
		super(state, gridWidth, gridHeight, backdrop, agentDefaultColor, agentPortrayal);
	}

	public void setupPortrayals()
	{ 
		display.repaint();
		super.setupPortrayals();


		if(agentPortrayal){
			OvalPortrayal2D o = new OvalPortrayal2D(agentDefaultColor); 
			agentsPortrayalContnuous.setPortrayalForClass(Agent.class, o);
		}

		if(((Environment)state).showNetworks) {
			edgePortrayal.setField( new SpatialNetwork2D(sweepState.continuousSpace, ((Environment)state).familyG ) );

			agentsPortrayalContnuous.setField(sweepState.continuousSpace);  /** <---- Change to your extended SimState class   */

			SimpleEdgePortrayal2D p = new SimpleEdgePortrayal2D(Color.BLACK, Color.BLACK,Color.BLACK);
			p.setLabelScaling(1);
			p.setShape(SimpleEdgePortrayal2D.SHAPE_THIN_LINE);
			
			edgePortrayal.setPortrayalForClass(Agent.class, p);// only set edge portryals for agents
		}


		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);

		// redraw the display
		display.repaint();
	}

	public void init(Controller c){
		super.init(c);  // use the predefined method to initialize the
		display.attach( edgePortrayal, "Edges" );
	}


	public static void main(String[] args) {
//		GUICN.initializeTimeSeriesChart( "R naught", "Time (days)", "R0");
//		GUICN.initializeHistogramChart( "Phases", "Phases", "# Agents", 6);
		GUICN.initialize(Environment.class, Experimenter.class, GUICN.class, 600, 600, Color.WHITE, Color.BLUE, false, spaces.CONTINUOUS);

	}
}