package NetworkModel;

public enum Conditions {
	Susceptible (1.0),
	Exposed (2.0), 
	Infectious (3.0), 
	Recovered (4.0), 
	Dead (5.0);
	
	private final double id;
	Conditions(double id){
		this.id =id;
	}
	
	public double id() {
		return id;
	}
}
