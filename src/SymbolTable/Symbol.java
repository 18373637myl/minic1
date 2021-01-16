package SymbolTable;

public class Symbol {
	public String name;
	public Type Type;
	int offset;
	public Symbol(String name, String T) {
		this.name = name;
		if(T.equals("double")) this.Type = Type.Double;
		else if(T.equals("int")) this.Type = Type.Int;
		else if(T.equals("fn")) this.Type = Type.fn;
		else if(T.equals("String")) this.Type = Type.String;
		else if(T.equals("char")) this.Type = Type.Char;
	}
}
