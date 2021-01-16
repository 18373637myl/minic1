package SymbolTable;

import java.util.*;

public class SymbolTableStack {
	List<SymbolTable> list = new ArrayList<>();
	
	public SymbolTableStack() {
		SymbolTable Global_table = new SymbolTable();
		SymbolTable fn_table = new SymbolTable();
		list.add(fn_table);
		list.add(Global_table);
		
		
	}
	
	public SymbolTable top() {
		if(list.isEmpty()) return null;
		else return list.get(list.size()-1);
	}
	
	public int size() {
		return list.size();
	}
	
	public SymbolTable get(int index) {
		return list.get(index);
	}
	
	public void pop() {
		list.remove(list.size()-1);
	}
	
	public void push(SymbolTable a) {
		a.fatherTable_id = this.top().id;
		list.add(a);
	}
	
	public SymbolTable getTable(int index) {
		return list.get(index);
	}
	
	public boolean checkSymbolExist(String name) {
		for(int i = list.size()-1 ; i>=1; i--) {
			if(list.get(i).searchByName(name)!=null) return true;
		}
		return false;
	}
	
	public boolean checkFnExist(String name) {
		if(list.get(0).searchByName(name)!=null) return true;
		return false;
	}
	
	public SymbolVar getSymbolByName(String name) {
		for(int i = list.size()-1 ; i>=1; i--) {
			if(list.get(i).searchByName(name)!=null) return (SymbolVar)list.get(i).searchByName(name);
		}
		return null;
	}
}
