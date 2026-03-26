package NetworkModel;

public enum Groups {
	FAMILY (1.0);
	
	private final double id;
	Groups(double id){
		this.id =id;
	}
	
	public double id() {
		return id;
	}
}
