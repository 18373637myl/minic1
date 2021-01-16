package SymbolTable;

import java.util.*;

public class SymbolTable {
	int fatherTable_id;
	int id;
	List<Symbol> table = new ArrayList<>();
	
	
	
	public SymbolTable() {}
	
	public int size() {
		return table.size();
	}
	
	public Symbol get(int index) {
		return table.get(index);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addSymbol(Symbol s) {
		this.table.add(s);
	}
	
	public Symbol searchByName(String name) {
		for(int i=0; i<this.table.size(); i++) {
			if(table.get(i).name.equals(name)) {
				return table.get(i);
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("\nthis is a table////// \n");
		for(int i=0; i<table.size(); i++) {
			s.append("name:"+table.get(i).name+" type:"+table.get(i).Type.toString()+'\n');
		}
		s.append("this table is over/// \n");
		return s.toString();
	}
	
}
