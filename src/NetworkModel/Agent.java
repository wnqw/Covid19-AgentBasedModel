package NetworkModel;


import randomWalker.RandomWalkerContinuousAbstract;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.util.Bag;
import sim.util.Double2D;
import java.lang.Math;
import groups.NetworkGroup;




public class Agent extends RandomWalkerContinuousAbstract {
	public Environment state;//The environment of the simulation
	public Stoppable event;//This allows you to remove an agent from a simulation

	//phase coutdowns
	public double countDownExposed;//determines how long the the exposed state lasts
	public double countDownInfectious;//determines how long the infectious state lasts
	public double countDownRecovered; //determines how long the recovered state lasts before an agent enters the susceptible state again

	//enums
	public Groups group;
	public Conditions condition;


	// Transmission info
	public double infectiousDate = -1;//records the first date this agent infected another agent.  Used to calculate R0
	public boolean infectiousEnd = false;//determines when the infectious state is over, used to calculate R0
	public int transmittedInfection = 0;//Keeps track of how many agents were infected by this agent.  Used to calculate R0

	//agent var
	public boolean mask;
	public boolean takeSupplements;
	public int age; 

	//vaccine
	public boolean vaccinated= false;
	public double countdownVaccination;
	public int doses= 0;
	public double countdownVacTakesEffect;


	//quarantine
	public boolean quarantine = false;
	public boolean tested= false;
	public double countdownTesting;
	public double countdownQuarantine;

	//soc approval
	public double approval;



	public Agent(Environment state, double x, double y, double stepSize, Conditions condition, Groups group, boolean mask) {
		this.x = x;
		this.y = y;
		this.stepSize = stepSize;
		this.state = state;
		this.condition = condition;
		this.group = group;
		this.mask = mask;
		this.age = state.random.nextInt((int)state.averageAge + (int)(state.random.nextGaussian()*state.ageSD*state.averageAge));

		double randomAngle = state.random.nextDouble() * ((state.random.nextBoolean()) ? PI : -PI); //360 random angle
		xdir = Math.cos(randomAngle) * stepSize;
		ydir = Math.sin(randomAngle) * stepSize;

		takeSupplements = state.random.nextBoolean(0.5);
		countdownVaccination = state.countdownVaccinationInterval;
		countdownQuarantine= state.countdownQuarantine;
		countdownTesting = state.countdownTest;
		countdownVacTakesEffect = state.countdownVacTakesEffect;
		approval= state.approval;


		// colors
		switch (condition) {
		case Susceptible://blue
			setColor(state, (float)0,(float)0, (float)1, (float)1);
			break;
		case Exposed://purple
			countDownExposed = state.explosedPhase;
			setColor(state, (float)1,(float)0, (float)1, (float)1);
			break;
		case Infectious://red
			countDownInfectious = state.infectiousPhase;
			setColor(state, (float)1,(float)0, (float)0, (float)1);
			break;
		case Recovered://green
			countDownRecovered = state.recoveredPhase;
			setColor(state, (float)0,(float)1, (float)0, (float)1);
			break;
		case Dead://black
			setColor(state, (float)0,(float)0, (float)0, (float)1);
			break;
		}

	}



	/**
	 * Method used to keep track of how many agents were infected by this agent.
	 */
	public void addTransmissionDate() {
		transmittedInfection++;
	}

	/**
	 * Records first date infected another agent.
	 *
	 * @param state
	 */
	public void setInfectionsDate(Environment state) {
		if (infectiousDate < 0) {
			infectiousDate = state.schedule.getTime();//give current timestep
		}
	}

	/**
	 * Called to end the infectious state, used for calculating R0.
	 */
	public void setInfectiousEnd() {
		infectiousEnd = true;
	}

	/**
	 * Controls how an agent transitions from one state to the next.
	 *
	 * @param
	 */



	public NetworkGroup getNetworkGroup(Agent a) {
		if(a.group == Groups.FAMILY) {
			return state.familyG;
		}
		return null;
	}




	public void checkCondition(Environment state) {
		switch(condition) {
		case Susceptible:
			Bag neighbors = state.continuousSpace.getNeighborsExactlyWithinDistance(new Double2D(x,y), state.distance, true);
			neighbors.remove(this);
			if(neighbors.numObjs == 0) {
				return;  
			}
			if(state.testing && this.quarantine) {//cant get exposed
				return;
			}
			for(int i=0;i< neighbors.numObjs;i++) {
				Agent a = (Agent)neighbors.objs[i];

				if(state.testing && a.quarantine) { //cant expose others
					return;
				}
				if(state.SocApproval) {
					if(!a.mask && this.mask) a.approval -= state.socPenalty; //soc approval penalty  
					if(!this.mask && a.mask) this.approval -= state.socPenalty;
				}
				if(a.condition == Conditions.Infectious) {
					if(this.group != a.group) {
						if(state.random.nextBoolean(agentFactors(state.pExpNoneG))) {
							state.numExposed++;
							state.numExpNoneG++;
							a.addTransmissionDate();
							setInfectionsDate(state);
							condition = Conditions.Exposed;
							countDownExposed = state.explosedPhase;
							setColor(state, (float)1,(float)0, (float)1, (float)1);
						}
					}
					else {
						if(this.group == Groups.FAMILY) {
							if(state.random.nextBoolean(agentFactors(state.pExpFamilyG))) {
								state.numExposed++;
								state.numExpFam++;
								a.addTransmissionDate();
								setInfectionsDate(state);
								condition = Conditions.Exposed;
								countDownExposed = state.explosedPhase;
								setColor(state, (float)1,(float)0, (float)1, (float)1);
							}
						}

					}

				}
			}
			return;
		case Exposed:
			if(countDownExposed > 0) {
				countDownExposed-=state.scheduleTimeInterval;
			}
			else {
				state.numInfected++;
				condition = Conditions.Infectious;
				countDownInfectious = state.infectiousPhase;
				setColor(state, (float)1,(float)0, (float)0, (float)1);
			}
			return;
		case Infectious:
			if(countDownInfectious > 0) {
				countDownInfectious -= state.scheduleTimeInterval;
			}
			else {
				//old people 
				if(age >= state.elderAgeDefine) {
					if(state.random.nextBoolean(state.elderDeathRate)) { //die
						state.numDead++;
						condition = Conditions.Dead;
						setColor(state, (float)0,(float)0, (float)0, (float)1);
						if(state.showNetworks) {
							NetworkGroup removeG = getNetworkGroup(this);
							removeG.removeNode(this); 
						}
						if(!state.showDeadAgents) {
							state.continuousSpace.remove(this);
							event.stop();
						}

					}
					else {//recover 
						state.numRecovered++;
						condition = Conditions.Recovered;
						countDownRecovered = state.recoveredPhase;
						setColor(state, (float)0,(float)1, (float)0, (float)1);
					}
				}
				//younger people
				else {
					if(state.random.nextBoolean(state.deathRate)) {	//die
						state.numDead++;
						condition = Conditions.Dead;
						setColor(state, (float)0,(float)0, (float)0, (float)1);
						if(state.showNetworks) {
							NetworkGroup removeG = getNetworkGroup(this);
							removeG.removeNode(this); 
						}
						if(!state.showDeadAgents) {
							state.continuousSpace.remove(this);
							event.stop();
						}
					}
					else {	//recover
						state.numRecovered++;
						condition = Conditions.Recovered;
						countDownRecovered = state.recoveredPhase;
						setColor(state, (float)0,(float)1, (float)0, (float)1);
					}
				}
			}
			return;
		case Recovered:
			if(countDownRecovered > 0) {
				countDownRecovered -= state.scheduleTimeInterval;
			}
			else {
				state.numSusceptible++;
				condition = Conditions.Susceptible;
				setColor(state, (float)0,(float)0, (float)1, (float)1);
				setInfectiousEnd();
			}
			return;
		case Dead:
			//counted in case infec
			setInfectiousEnd();
			return;
		}
	}




	public void socApproval() {
		if(this.approval < state.approvalBaseline) {
			if(!this.mask) this.mask= true;
			approval= state.approval;
		}
	}




	public void testing() {
		if(countdownTesting > 0) {
			tested= false;
			countdownTesting -= state.scheduleTimeInterval; 
		}

		else {
			if(condition==Conditions.Infectious) {
				quarantine= true;
			}
			tested= true;
			countdownTesting= state.countdownTest;
		}

	}



	public void quarantine() {
		if(countdownQuarantine > 0) {
			this.state.continuousSpace.setObjectLocation(this, new Double2D(x,y));
			this.countdownQuarantine -= state.scheduleTimeInterval; 
		}

		else {
			this.quarantine= false;
			this.countdownQuarantine = state.countdownQuarantine;
		}
	}





	public void vaccinate() {
		if(state.elderFirst && age < state.elderAgeDefine) return;
		
		if(doses == 2) return;
		
		if(doses == 0) {
			int total = state.continuousSpace.getAllObjects().numObjs;

			if((state.numGotTheDose/total) <= state.vaccineCoverage) {
				doses++;
				state.numGotTheDose++;
			}
			else {
				state.numGotTheDose = 0; //reset
			}
			return;
		}
		
		if(doses == 1) {
			if(countdownVaccination > 0) countdownVaccination -= state.scheduleTimeInterval; 

			else {
				int total = state.continuousSpace.getAllObjects().numObjs;

				if((state.numGotTheDose/total) <= state.vaccineCoverage) {
					doses++;
					vaccinated = true;
					state.numGotTheDose++;
				}
				else {
					state.numGotTheDose = 0; //reset
				}

				countdownVaccination = state.countdownVaccinationInterval;
			}
			return;
		}
	}




	public double agentFactors(double curExpP) {//p of get exposed
		double realExpP = 0;
		//age
		if(age < state.elderAgeDefine) { 
			realExpP += curExpP*(state.youngerP);
		}
		else realExpP += curExpP*(state.elderP);
		//supplements
		if(takeSupplements) { 
			realExpP *= (1-state.supplementP);
		}
		//mask
		if(mask) realExpP *= (1-state.maskP);

		//mutation
		if(state.mutation) {
			if(state.random.nextBoolean(state.mutationRate)) {
				if(realExpP*(1+state.mutationLevel) <= 1) {
					realExpP *= (1+state.mutationLevel);
				}
			}
			 
		}
		//vaccine
		if(state.vaccine) {
			if(vaccinated) {
				if(countdownVacTakesEffect > 0) countdownVacTakesEffect -= state.scheduleTimeInterval;
				else {
					realExpP = 0;
				}
				if(countdownVacTakesEffect==0) {
					realExpP = 0;
				}
			}
		}

		return realExpP;
	}




	public void die(Environment state) {
		if(state.showNetworks) {
			NetworkGroup removeG = getNetworkGroup(this);
			removeG.removeNode(this); 
		}
		if(!state.showDeadAgents) {
			state.continuousSpace.remove(this);
			event.stop();
		}
	}





	public void GaussianStep() {
		if(state.testing) {
			if(this.quarantine) {
				quarantine();
				return;
			}
		}
		randomOrientedGaussianStep(state,this.state.gaussanStandardDeviation,this.state.rotation);
		x=  this.state.continuousSpace.stx(x+xdir); 
		y = this.state.continuousSpace.sty(y+ydir);
		this.state.continuousSpace.setObjectLocation(this, new Double2D(x,y));

	}



	public void UniformStep() {
		if(state.testing) {
			if(this.quarantine) {
				quarantine();
				return;
			}
		}
		randomOrientedUniformStep(state, this.state.rotation);
		x=  this.state.continuousSpace.stx(x+xdir); 
		y = this.state.continuousSpace.sty(y+ydir);
		this.state.continuousSpace.setObjectLocation(this, new Double2D(x,y));

	}

	public void aging() {
		double time = state.schedule.getTime();
		if((time % 365) == 0) {
			this.age++;
		}
	}


	public void step(SimState state) {
		Environment eState = (Environment)state;
		if(this.condition == Conditions.Dead) {
			die(eState);
			return;  
		}
		if(this.age > eState.maxAge) {
			die(eState);
			return;
		}

		//movements
		if(state.random.nextBoolean(eState.active)) {
			if(this.state.gaussian) {
				GaussianStep();
			}
			else {
				UniformStep();
			}
		}
		if(eState.SocApproval) socApproval();
		if(eState.testing) testing();
		if(eState.vaccine) vaccinate();
		checkCondition(eState); 

		this.aging();
	}
}
