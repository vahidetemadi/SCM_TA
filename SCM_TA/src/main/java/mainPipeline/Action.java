package main.java.mainPipeline;

public enum Action {
	COST,
	DIFFUSION;
	
	public Action getOpposite() {
		switch (this) {
		case COST:
			return DIFFUSION;
		case DIFFUSION:
			return COST;
		default:
			return null;
		}
	}
}
