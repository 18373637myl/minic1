package SymbolTable;
import java.util.*;
import instruction.*;
public class SymbolFn extends Symbol {
	
	public List<Symbol> param = new ArrayList<>();
	public SymbolTable table = new SymbolTable();
	public Type ret;
	public InstructionList list = new InstructionList();
	
	public int ret_slot() {
		if(ret == Type.Void) return 0;
		else return 1;
	}
	
	public SymbolFn(String name) {
		super(name, "fn");
	}
	
	public Symbol searchParam(String name) {
		for(int i=0; i<param.size() ; i++) {
			if(param.get(i).name.equals(name)) return param.get(i);
		}
		return null;
	}

	public Type getRet() {
		return ret;
	}

	public void setRet(Type ret) {
		this.ret = ret;
	}
	
	public void add(Instruction opt) {
		list.add(opt);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("this is a fn////// \n");
		for(int i=0; i<param.size(); i++) {
			s.append("name:"+param.get(i).name+" type:"+param.get(i).Type.toString()+'\n');
		}
		for(int i=0; i<table.size(); i++) {
			s.append("name:"+table.get(i).name+" type:"+table.get(i).Type.toString()+'\n');
		}
		s.append("this fn is over/// \n");
		return s.toString();
	}
}
