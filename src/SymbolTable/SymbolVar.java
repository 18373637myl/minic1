package SymbolTable;

public class SymbolVar extends Symbol {
	boolean isConstant;
	boolean isInitialized;
	int loc_num ;
	boolean isGlobal;
	public int getLoc_num() {
		return loc_num;
	}

	public void setLoc_num(int loc_num) {
		this.loc_num = loc_num;
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

	public SymbolVar(String name, String Type ) {
		super(name, Type);
		this.isConstant = this.isInitialized = this.isGlobal = false;
	}

	public boolean isConstant() {
		return isConstant;
	}

	public void setConstant(boolean isConstant) {
		this.isConstant = isConstant;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	
}
