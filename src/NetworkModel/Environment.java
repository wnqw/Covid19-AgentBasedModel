package NetworkModel;

import java.awt.Color;
import java.awt.Paint;

import groups.NetworkGroup;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import spaces.Spaces;
import sweep.SimStateSweep;
import java.util.ArrayList;


/**
 * Basic epidemiological model implementing SEIR (susceptible, exposed, infectious, recovered) modeling approach.
 * @author jeffreyschank
 *
 */

public class Environment extends SimStateSweep {
	//general settings
	public int susceptible = 7000; // davis 69,289 
	public int infected = 5; 
	boolean gaussian = false;
	double rotation = 1; 
	double gaussanStandardDeviation = 1.0;
	double stepSize = 1; 
	double meanK = 1;
	double active = 1.0; 
	double distance = 6;  
	public double explosedPhase = 2;//2-14
	public double infectiousPhase = 10;//10-20
	public double recoveredPhase = 180; //6months reinfection
	public double timeFrameR0 = 14; //14, This is the number of time step back to caculate a dynamic R0 based on how many agents
	public int lineGraph = 2; 


	//P expose
	public double youngerP = 0.62; 
	public double elderP = 0.88; 
	public double maskP = 0.7;
	public double supplementP = 0.12;  

	public int age; 
	public double ageSD= 0.1;
	public int averageAge = 150;
	public int maxAge = 230; //120/79*150=230
	public int elderAgeDefine = 123; //65/79*150=123
	public double deathRate = 0.097;  //0.00001
	public double elderDeathRate = 0.807; //0.0008

	//testing, quarantine
	public double countdownTest = 7;
	public double countdownQuarantine= 14;

	//groups
	NetworkGroup familyG = null; 
	//P(exposed) by groups.
	public double pExpFamilyG = 0.17;
	public double pExpNoneG = 0.09;

	//data
	public int numSusceptible = 0;
	public int numExposed= 0;
	public int numInfected= 0;
	public int numRecovered= 0;
	public int numDead= 0;

	public int numExpFam= 0;
	public int numExpNoneG= 0;

	//soc approval
	public double approvalBaseline= 70;
	public double approval= 100;
	public double socPenalty= 10;

	//controls
	public boolean SocApproval= false;
	public boolean mutation= false;
	public boolean vaccine = false;
	public boolean testing = false;

	public boolean charts = false;
	public boolean showDeadAgents= false;
	public boolean showNetworks = false;

	//virus
	public double mutationLevel = 0.5;
	public double mutationRate = 0.001;

	//vaccine
	public double vaccineCoverage = 0.04;
	public double countdownVaccinationInterval = 30;
	public int numGotTheDose = 0;
	public double countdownVacTakesEffect = 14;
	public boolean elderFirst= false;

	//others
	public Experimenter experimenter;




	public Environment(long seed, Class observer) {//constructor
		super(seed, observer);
	}






	public int getSusceptible() {
		return susceptible;
	}



	public void setSusceptible(int susceptible) {
		this.susceptible = susceptible;
	}





	public int getInfected() {
		return infected;
	}








	public void setInfected(int infected) {
		this.infected = infected;
	}



	public boolean isGaussian() {
		return gaussian;
	}






	public void setGaussian(boolean gaussian) {
		this.gaussian = gaussian;
	}





	public boolean isCharts() {
		return charts;
	}






	public void setCharts(boolean charts) {
		this.charts = charts;
	}






	public boolean isShowDeadAgents() {
		return showDeadAgents;
	}



	public void setShowDeadAgents(boolean showDeadAgents) {
		this.showDeadAgents = showDeadAgents;
	}






	public boolean isShowNetworks() {
		return showNetworks;
	}






	public void setShowNetworks(boolean showNetworks) {
		this.showNetworks = showNetworks;
	}






	public boolean isSocApproval() {
		return SocApproval;
	}






	public void setSocApproval(boolean socApproval) {
		SocApproval = socApproval;
	}




	public boolean isMutation() {
		return mutation;
	}






	public void setMutation(boolean mutation) {
		this.mutation = mutation;
	}





	public boolean isVaccine() {
		return vaccine;
	}






	public void setVaccine(boolean vaccine) {
		this.vaccine = vaccine;
	}







	public boolean isTesting() {
		return testing;
	}






	public void setTesting(boolean testing) {
		this.testing = testing;
	}




	public double getCountdownTest() {
		return countdownTest;
	}






	public void setCountdownTest(double countdownTest) {
		this.countdownTest = countdownTest;
	}






	public double getCountdownQuarantine() {
		return countdownQuarantine;
	}






	public void setCountdownQuarantine(double countdownQuarantine) {
		this.countdownQuarantine = countdownQuarantine;
	}






	public double getSocPenalty() {
		return socPenalty;
	}






	public void setSocPenalty(double socPenalty) {
		this.socPenalty = socPenalty;
	}



	public int getLineGraph() {
		return lineGraph;
	}




	public void setLineGraph(int lineGraph) {
		this.lineGraph = lineGraph;
		Graph_Types key = Graph_Types.values()[lineGraph];
		switch(key) {
		case R0:
			gui.chartTimeSeries.setTitle("R naught");
			gui.chartTimeSeries.setYAxisLabel("R0");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case DISTANCE:
			gui.chartTimeSeries.setTitle("Average Number of Agents within Infectious Radius");
			gui.chartTimeSeries.setYAxisLabel("Average Distance");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case PHASE:
			gui.chartTimeSeries.setTitle("Phases");
			gui.chartTimeSeries.setYAxisLabel("Phases");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case NUMMASK:
			gui.chartTimeSeries.setTitle("Number of Mask Wearers");
			gui.chartTimeSeries.setYAxisLabel("# Agents");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case INFGROUPS:
			gui.chartTimeSeries.setTitle("Distribution of Infected Agents in NWGroup");
			gui.chartTimeSeries.setYAxisLabel("# Agents");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case INFDIST:
			gui.chartTimeSeries.setTitle("Infection Distance");
			gui.chartTimeSeries.setYAxisLabel("Distance");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		case VIRUSLEVEL:
			gui.chartTimeSeries.setTitle("Virus Level");
			gui.chartTimeSeries.setYAxisLabel("Level");
			gui.chartTimeSeries.setXAxisLabel("Time (days)");
			break;
		}
	}



	public boolean decideWearMask() {
		if(this.random.nextBoolean(0.5)) {
			return true;
		}
		return false;
	}


	public Groups decideGroup() {
		if(this.random.nextBoolean(0.5)) {
			return Groups.FAMILY;
		}
		return null;
	}



	public void makeAgents() {
		for(int i=0;i<susceptible;i++) {
			double x = random.nextDouble()*gridWidth;
			double y = random.nextDouble()*gridHeight;
			Agent a = new Agent(this,x,y,stepSize, Conditions.Susceptible, decideGroup(), decideWearMask());//decideGroup()
			this.continuousSpace.setObjectLocation(a, new Double2D(x,y));
			a.event = schedule.scheduleRepeating(1.0,a, scheduleTimeInterval); //this allows us to schedule for explicit time intervals
		}
		for(int i=0;i<infected;i++) {
			double x = random.nextDouble()*gridWidth;
			double y = random.nextDouble()*gridHeight;
			Agent a = new Agent(this,x,y,stepSize, Conditions.Infectious, decideGroup(), decideWearMask());
			this.continuousSpace.setObjectLocation(a, new Double2D(x,y));
			a.event = schedule.scheduleRepeating(1.0,a, scheduleTimeInterval); //this allows us to schedule for explicit time intervals
		}
	}




	//make network group
	public void makeNWG() {
		familyG = new NetworkGroup();

		Bag agents = this.continuousSpace.getAllObjects();//everything in space, hubs, agents

		for(int i=0; i<agents.numObjs;i++) {
			Agent a= (Agent)agents.objs[i];
			if(a.group == Groups.FAMILY) {
				familyG.addNode(a);
			}
		}

		familyG.randomNetworkMeanK(this, familyG.allNodes, meanK, null);
	}



	public void start() {
		super.start();
		setGridWidth(200);
		setGridHeight(200);
		spaces = Spaces.CONTINUOUS; 
		this.make2DSpace(Spaces.CONTINUOUS, 1.0, gridWidth, gridHeight); 

		makeAgents(); 

		if(showNetworks) {
			makeNWG();
		}

		if(observer != null){
			observer.initialize(this.continuousSpace, spaces,scheduleTimeInterval);
			experimenter = (Experimenter)observer;
			experimenter.reset();
			experimenter.setEstate(this);
		}
	}

}
