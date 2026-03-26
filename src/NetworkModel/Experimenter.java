package NetworkModel;

import observer.Observer;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sweep.ParameterSweeper;
import sweep.SimStateSweep;

public class Experimenter extends Observer {
	public Environment eState;
	//vars
	public int numSusceptible;
	public int numExposed;
	public int numInfected;
	public int numRecovered;
	public int numDead;
	
	public int numWearMask;
	public int numExpFamG;
	public int numExpNoneG;
	
	public int meanR0;
	public int meanNeighbors;


	public Experimenter(String fileName, String folderName, SimStateSweep state, ParameterSweeper sweeper,
			String precision, String[] headers) {
		super(fileName, folderName, state, sweeper, precision, headers);
		this.eState = (Environment)state;
	}

	
	
	public void countAgents(Environment state) {
		numSusceptible = state.numSusceptible;
		numExposed = state.numExposed;
		numInfected= state.numInfected;
		numRecovered= state.numRecovered;
		numDead = state.numDead;
		numWearMask = countMask(state);
		numExpFamG = state.numExpFam;
		numExpNoneG= state.numExpNoneG;
//		meanR0 = countMeanR0(state);
//		meanNeighbors = countMeanNeighbors(state);
	}


	public void addData(Environment state) {
		double total = state.continuousSpace.getAllObjects().numObjs;
		data.add(total);
		data.add(numSusceptible/total);
		data.add(numExposed/total);
		data.add(numInfected/total);
		data.add(numRecovered/total);
		data.add(numDead/total);
		data.add(numWearMask/total);
		data.add(numExpFamG/total);
		data.add(numExpNoneG/total);
//		data.add(meanR0/total);
//		data.add(meanNeighbors/total);
	}

	public void reSet(Environment state) {
		numSusceptible = state.numSusceptible= 0;
		numExposed = state.numExposed=0;
		numInfected= state.numInfected=0;
		numRecovered= state.numRecovered=0;
		numDead = state.numDead=0;
		numExpFamG = state.numExpFam=0;
		numExpNoneG= state.numExpNoneG=0;
		numWearMask= 0; 
//		meanR0= 0;
//		meanNeighbors= 0;
	}

	public void setEstate(Environment state) {
		this.eState = state;
	}

	
	public int countMask(Environment state) {
		int numMaskWearer= 0;
		Bag agents = state.continuousSpace.getAllObjects();
		for(int i=0;i<agents.numObjs;i++) {
			Agent a = (Agent)agents.objs[i];
			if(a.mask) 
				numMaskWearer++;
		}
		return numMaskWearer;
	}
	
	public int countMeanR0(Environment state) {
		Bag agents = state.continuousSpace.getAllObjects();
		int totalTransmittedInfection= 0;
		int casesOfTransmission = 0;
		int MeanR0= 0;
		for(int i=0; i<agents.numObjs; i++) {
			Agent a = (Agent)agents.objs[i];
			if(a.infectiousDate > (state.schedule.getTime()-state.timeFrameR0) && a.infectiousEnd) {
				totalTransmittedInfection += a.transmittedInfection;
				casesOfTransmission++;
			}
		}
		if(casesOfTransmission > 0) {
			MeanR0 = totalTransmittedInfection/casesOfTransmission;
		}
		return MeanR0;
	}
	
	public int countMeanNeighbors(Environment state) {
		Bag agents = state.continuousSpace.getAllObjects();
		int sumNeighbors= 0;
		int MeanNeighbors= 0;
		for(int i=0; i<agents.numObjs; i++) {
			Agent a = (Agent)agents.objs[i];
			Double2D position = new Double2D(a.x,a.y);
			Bag neighbors = state.continuousSpace.getNeighborsExactlyWithinDistance(position, state.distance, true);
			sumNeighbors += (double)(neighbors.numObjs - 1);
		}
		MeanNeighbors = sumNeighbors/agents.numObjs;
		return MeanNeighbors;
	}


	public void step (SimState state) {
		super.step(state);
		final long step = (long)state.schedule.getTime();
		if(step % eState.dataSamplingInterval == 0) {
			countAgents(eState);
			addData(eState);
			reSet(eState);
		}
	}
}
