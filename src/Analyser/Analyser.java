package Analyser;

import error.AnalyzeError;
import error.CompileError;
import error.ErrorCode;
import error.ExpectedTokenError;
import error.TokenizeError;
import instruction.Instruction;
import instruction.Operation;
import Tokenizer.Token;
import Tokenizer.TokenType;
import Tokenizer.Tokenizer;
import Tokenizer.Pos;
import SymbolTable.Symbol;
import SymbolTable.SymbolFn;
import SymbolTable.SymbolTable;
import SymbolTable.SymbolTableStack;
import SymbolTable.SymbolVar;
import SymbolTable.Type;
import instruction.InstructionList;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;
    int expr_num = 0;
    int fn_level_num = 0;
    SymbolFn func;
    int loc_num = 0;
    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    SymbolTableStack stack = new SymbolTableStack();
    int block_id = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            System.out.println(token.getTokenType()+"  values:"+token.getValueString());
            expr_num++;
            peekedToken = null;
            return token;
        }else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

/*************************************函数与程序******************************************/
    public void analyseProgram() throws CompileError {
    	var peeked = peek().getTokenType();
    	boolean flag_s = true, flag_main_exit = false;
    	
    	while(flag_s) {
	    	switch (peeked) {
	    		case Let :
	    			analyseVariableDeclaration(true,null);
	    			break;
	    		case Const :
	    			analyseConstantDeclaration(true,null);
	    			break;
	    		case Fn :
	    			if(analyseFunction())
	    				flag_main_exit = true;
	    			break;
	    		case Comment :
	    			next();
	    			break;
	    		case EOF :
	    			System.out.println(stack.get(0).toString());
	    			System.out.println(stack.get(1).toString());
	    			for(int i=0; i<stack.get(0).size(); i++) {
	    				SymbolFn s = (SymbolFn)stack.get(0).get(i);
	    				System.out.println(s.toString());
	    			}
	    			System.out.println("Success!!");
	    			flag_s = false;
	    			break;
	    		default :
	    			throw new AnalyzeError(ErrorCode.ProgramError, peek().getStartPos());
	    	}
	    	peeked = peek().getTokenType();
    	}
    	if(flag_main_exit == false) throw new AnalyzeError(ErrorCode.NoMain, peek().getStartPos());
    		
    }
    
    private boolean analyseFunction() throws CompileError {
    	loc_num = 0;
    	next();
    	String name = expect(TokenType.Ident).getValueString();
    	
    	if(stack.getTable(0).searchByName(name)!=null) throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
    	if(stack.getTable(1).searchByName(name)!=null) throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
    	SymbolFn fn = new SymbolFn(name);
    	stack.getTable(0).addSymbol(fn);
    	func = fn;
    	expect(TokenType.L_paren);
    	
    	var peeked = peek().getTokenType();
    	if(peeked == TokenType.Const || peeked == TokenType.Ident)
    		analyseParam(fn);
    	peeked = peek().getTokenType();
    	
    	while(peeked == TokenType.Comma) {
    		next();
    		analyseParam(fn);
    		peeked = peek().getTokenType();
    	}
    	
    	expect(TokenType.R_paren);
    	expect(TokenType.Arrow);
    	String type = expect(TokenType.Type).getValueString();
    	switch(type) {
    	case "int" : 
    		fn.setRet(Type.Int);
    		break;
    	case "double" :
    		fn.setRet(Type.Double);
    		break;
    	case "void" :
    		fn.setRet(Type.Void);
    		break;
    	}
    	fn_level_num = 0;
    	analyseBlockStatement(fn);
    	
    	if(name.equals("main")) return true;
    	else return false;
    }

    private void analyseParam(SymbolFn Fn) throws CompileError {
    	var peeked = peek().getTokenType();
    	if(nextIf(TokenType.Const)!=null) {
    		String name = expect(TokenType.Ident).getValueString();
    		expect(TokenType.Colon);
    		String Type = expect(TokenType.Type).getValueString();
    		
    		if(Fn.searchParam(name)!=null) throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
    		SymbolVar param = new SymbolVar(name,Type);
    		param.setConstant(true);
    		Fn.param.add(param);
    	}
    	else if(peeked == TokenType.Ident) {
    		String name = expect(TokenType.Ident).getValueString();
    		expect(TokenType.Colon);
    		String Type = expect(TokenType.Type).getValueString();
    		
    		if(Fn.searchParam(name)!=null) throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
    		SymbolVar param = new SymbolVar(name,Type);
    		Fn.param.add(param);
    	}
    }
/***************************************语句*********************************************/
    private void analyseStatement(SymbolFn fn) throws CompileError {
    	var peeked = peek();
    	
        switch (peeked.getTokenType()) {
        	case Let :
        		analyseVariableDeclaration(false,fn);
        		break;
        	case Const :
        		analyseConstantDeclaration(false,fn);
        		break;
        	case If :
        		analyseConditionalStatement(fn);
        		break;
        	case While :
        		analyseLoopStatement(fn);
        		break;
        	case Return :
        		analyseReturnStatement(fn);
        		break;
        	case Semicolon :
        		expect(TokenType.Semicolon);
        		break;
        	case L_brace :
        		analyseBlockStatement(fn);
        		break;
        	default :
        		analyseExpression(fn);
        	
        		
        }
    }

    private void analyseConstantDeclaration(boolean isGlobal, SymbolFn fn) throws CompileError {
         	next();
            var nameToken = expect(TokenType.Ident);

            String name =nameToken.getValueString();         

            expect(TokenType.Colon);
            String t = expect(TokenType.Type).getValueString();
            
            //加入符号表
            
            SymbolVar s = new SymbolVar(name,t);
            s.setConstant(true);
            s.setInitialized(true);
            
            if(fn!=null) fn.table.addSymbol(s);
            if(isGlobal) {
            	if(stack.getTable(1).searchByName(name)!=null) 
                	throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
            	s.setGlobal(true);
            	stack.getTable(1).addSymbol(s);
            	s.setLoc_num(stack.getTable(1).size()-1);
            }
            else {
            	if(stack.top().searchByName(name)!=null) 
                	throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
            	stack.top().addSymbol(s);
            	s.setLoc_num(loc_num);
            	loc_num++;
            }
            ////////////
            
            expect(TokenType.Assign);
            analyseExpression(fn);
            
            expect(TokenType.Semicolon);

            // 这里把常量值直接放进栈里，位置和符号表记录的一样。
            // 更高级的程序还可以把常量的值记录下来，遇到相应的变量直接替换成这个常数值，
            // 我们这里就先不这么干了。
            //instructions.add(new Instruction(Operation.LIT, value));
            if(stack.size()>2) System.out.println(stack.top().toString());
    }

    private void analyseVariableDeclaration(boolean isGlobal, SymbolFn fn) throws CompileError {
    		next();
            var nameToken = expect(TokenType.Ident);
            String name = nameToken.getValueString();
            // 变量初始化了吗
            boolean initialized = false;
            
            expect(TokenType.Colon);
            String t = expect(TokenType.Type).getValueString();
            // 下个 token 是等于号吗？如果是的话分析初始化
            if(nextIf(TokenType.Assign)!=null){
                analyseExpression(fn);
                
                initialized = true;
            }
            expect(TokenType.Semicolon);

            // 加入符号表，请填写名字和当前位置（报错用）
            SymbolVar s = new SymbolVar(name,t);
            s.setInitialized(initialized);
            if(fn!=null) fn.table.addSymbol(s);
            if(isGlobal) {
            	if(stack.getTable(1).searchByName(name)!=null) 
                	throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
            	s.setGlobal(isGlobal);
            	stack.getTable(1).addSymbol(s);
            	s.setLoc_num(stack.getTable(1).size()-1);
            }
            else {
            	if(stack.top().searchByName(name)!=null) 
                	throw new AnalyzeError(ErrorCode.OverDeclared, peek().getStartPos());
            	stack.top().addSymbol(s);
            	s.setLoc_num(loc_num);
            	loc_num++;
            }
            ////////////

            /* 如果没有初始化的话在栈里推入一个初始值
            if (!initialized) {
                instructions.add(new Instruction(Operation.LIT, 0));
            }*/if(stack.size()>2) System.out.println(stack.top().toString());
    }

    private void analyseConditionalStatement(SymbolFn fn) throws CompileError {
    	expect(TokenType.If);
       	analyseExpression(fn);

    	analyseBlockStatement(fn);
    	
    	if(nextIf(TokenType.Else)!=null) {
    		if(peek().getTokenType()==TokenType.If)
    			analyseConditionalStatement(fn);
    		
    		else if(peek().getTokenType()==TokenType.L_brace)
    			analyseBlockStatement(fn);
    	}
    	
    		
    }
    
    private void analyseLoopStatement(SymbolFn fn) throws CompileError {
    	expect(TokenType.While);
    	analyseExpression(fn);
    	
    	analyseBlockStatement(fn);
    }
    
	private void analyseReturnStatement(SymbolFn fn) throws CompileError {
	    expect(TokenType.Return);
	    if(nextIf(TokenType.Semicolon)!=null) {}
	    else {
	    	analyseExpression(fn);
	    	expect(TokenType.Semicolon);
	    }
	}
	
	private void analyseBlockStatement(SymbolFn fn) throws CompileError {
    	SymbolTable table = new SymbolTable();
    	stack.push(table);
    	if(fn!=null && fn_level_num == 0) {
    		for(int i=0; i<fn.param.size(); i++)
    			table.addSymbol(fn.param.get(i));
    		fn_level_num++;
    	}
		expect(TokenType.L_brace);
		boolean flag = true;
		while(flag) {
			switch (peek().getTokenType()) {
		    	case Let :
		    		analyseVariableDeclaration(false,fn);
		    		break;
		    	case Const :
		    		analyseConstantDeclaration(false,fn);
		    		break;
		    	case If :
		    		analyseConditionalStatement(fn);
		    		break;
		    	case While :
		    		analyseLoopStatement(fn);
		    		break;
		    	case Return :
		    		analyseReturnStatement(fn);
		    		break;
		    	case Semicolon :
		    		next();
		    		break;
		    	case L_brace :
		    		analyseBlockStatement(fn);
		    		break;
		    	case R_brace :
		    		next();
		    		flag = false;
		    		stack.pop();
		    		if(stack.size()>2) System.out.println(stack.top().toString());
		    		break;
		    	default :
		    		analyseExpression(fn);
		    		expect(TokenType.Semicolon);
			}
		}
	}
    
/******************************************表达式******************************************/
	
	/**
	 * 分析表达式
	 * @throws CompileError
	 */
    private void analyseExpression(SymbolFn fn) throws CompileError {
    	var peekfir = peek();
    	expr_num = 0;
    	analyseB(fn);
    	var peeked = peek();
    	if(peeked.getTokenType() == TokenType.Assign && expr_num == 1 && peekfir.getTokenType() == TokenType.Ident) {
    		if(!stack.checkSymbolExist(peekfir.getValueString()))
    			throw new AnalyzeError(ErrorCode.NotDeclared, peek().getStartPos());
    		
    		func.add(new Instruction(Operation.pop));
    		
    		SymbolVar s = stack.getSymbolByName(peekfir.getValueString());
    		if(s.isGlobal()) func.add(new Instruction(Operation.globA,true,s.getLoc_num()));
    		else func.add(new Instruction(Operation.locA,true,s.getLoc_num()));
    		
    		next();
    		
    		analyseB(fn);
    		func.add(new Instruction(Operation.store64));
    	}
    }
    
    private Type analyseB(SymbolFn fn) throws CompileError {
    	Type left = analyseC(fn);
    	Type right = null;
    	var peeked = peek().getTokenType();
    	switch(peeked) {
    		case Eq:
    		case Neq:
    		case Lt:
    		case Le:
    		case Gt:
    		case Ge:
    			next();
    			right = analyseC(fn);
    			break;
    		default:
    			return left;
    	}
    	if(left != right) 
    		throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    	else {
    		switch(left){
            case Int:
                func.list.add(new Instruction(Operation.cmpI));
                break;
            case Double:
                func.list.add(new Instruction(Operation.cmpF));
                break;
            default:
                throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
                
        }
       
        switch (peeked){
	        case Eq:
	            func.list.add(new Instruction(Operation.not));
	            break;
	        case Neq:
	            break;
	        case Lt:
	            func.list.add(new Instruction(Operation.setLt));
	            break;
	        case Gt:
	            func.list.add(new Instruction(Operation.setGt));
	            break;
	        case Le:
	            func.list.add(new Instruction(Operation.setGt));
	            func.list.add(new Instruction(Operation.not));
	            break;
	        case Ge:
	            func.list.add(new Instruction(Operation.setLt));
	            func.list.add(new Instruction(Operation.not));
	            break;
	
	        default:
        	}
        return Type.Int;
    	}
    }
    
    private Type analyseC(SymbolFn fn) throws CompileError {
    	Type left = analyseD(fn);
    	var peeked = peek().getTokenType();
    	Type right = null;
    	switch(peeked) {
    		case Plus:
    		case Minus:
    			next();
    			right = analyseD(fn);
    			if(left != right) 
    				throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    			break;
    		default:
    	}
    	
    	if(left==Type.Double) {
    		if(peeked == TokenType.Plus)
                func.list.add(new Instruction(Operation.addF));
            else
                func.list.add(new Instruction(Operation.subF));
    	}
    	else if(left==Type.Int) {
    		if(peeked == TokenType.Plus)
                func.list.add(new Instruction(Operation.addI));
            else
                func.list.add(new Instruction(Operation.subI));
    	}
    	else throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    	return left;
    }
    
    private Type analyseD(SymbolFn fn) throws CompileError {
    	Type left = analyseE(fn);
    	var peeked = peek().getTokenType();
    	Type right = null;
    	switch(peeked) {
    		case Mul:
    		case Div:
    			next();
    			right = analyseE(fn);
    			if(left != right) 
    				throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    			break;
    	}
    	if(left==Type.Double) {
    		if(peeked == TokenType.Mul)
                func.list.add(new Instruction(Operation.mulF));
            else
                func.list.add(new Instruction(Operation.divF));
    	}
    	else if(left==Type.Int) {
    		if(peeked == TokenType.Mul)
                func.list.add(new Instruction(Operation.mulI));
            else
                func.list.add(new Instruction(Operation.divI));
    	}
    	else throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    	return left;
    }
    
    private Type analyseE(SymbolFn fn) throws CompileError {
    	Type t = analyseF(fn);
    	
    	while(nextIf(TokenType.As)!=null) {
    		
    		if(t!=Type.Int||t!=Type.Double)
    			throw new AnalyzeError(ErrorCode.TypeError,peek().getStartPos());
    		
    		TokenType trans = peek().getTokenType();
            switch(trans){
                case Uint:
                    if(t == Type.Double)
                        func.list.add(new Instruction(Operation.ftoi));
                        t = Type.Int;
                    break;
                case Double:
                    if(t == Type.Int)
                        func.list.add(new Instruction(Operation.itof));
                        t = Type.Double;
                    break;
                default:
                    throw new AnalyzeError(ErrorCode.ExpectedToken, peek().getStartPos());

    		}
            next();
    	}
    	return t;
    	
    }
    
    private Type analyseF(SymbolFn fn) throws CompileError {
    	Type t;
    	boolean ne = false;
    	if(nextIf(TokenType.Minus)!=null) ne = true;
    	t = analyseG(fn);
    	if(ne) {
    		if(t == Type.Double) {
    			func.add(new Instruction(Operation.negF));
    			return t;
    		}
    		else if(t == Type.Int) {
    			func.add(new Instruction(Operation.negI));
    			return t;
    		}
    		else throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
    	}
    	return t;
    }
    
    private Type analyseG(SymbolFn fn) throws CompileError {
    	var peeked = peek();
    	Type t = null;
    	switch(peeked.getTokenType()) {
    		case L_paren:
    			next();
    			t = analyseB(fn);
    			expect(TokenType.R_paren);
    			break;
    		case String:
    			t = Type.String;
    			next();
    			break;
    		case Uint:
    			t = Type.Int;
    			long value = (long)(peeked.getValue());
                func.add(new Instruction(Operation.pushI, false, value));
    			next();
    			break;
    		case Double:
    			t = Type.Double;
    			double value_d = (double)(peeked.getValue());
                func.add(new Instruction(Operation.pushF, value_d));
    			next();
    			break;
    		case Char:
    			t = Type.Char;
                int ch = ((int)(char)peeked.getValue()) ;
                func.add(new Instruction(Operation.pushI, false, (long)ch));
                next();
                break;
    		case Ident:
    			Type paramtype;
    			int param_num = 0;
    			String name = next().getValueString();
    			SymbolVar th = stack.getSymbolByName(name);
    			
    			Symbol sym = stack.getTable(0).searchByName(name);
    			SymbolFn fn_ = (SymbolFn)sym;
    			if(th!=null)
    				t = th.Type;
    			if(fn_!=null) {
    				if(nextIf(TokenType.L_paren)!=null) {
    				if(!stack.checkFnExist(name))
    	    			throw new AnalyzeError(ErrorCode.NotDeclared, peek().getStartPos());
 
    				func.add(new Instruction(Operation.stackAlloc, true, fn.ret_slot()));
    				
    				paramtype = analyseB(fn);
    				if(paramtype != fn.param.get(param_num).Type)
    					throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
    				param_num++;
    				
    				while(nextIf(TokenType.Comma)!=null) {
    					paramtype = analyseB(fn);
	    				if(paramtype != fn.param.get(param_num).Type)
	    					throw new AnalyzeError(ErrorCode.TypeError, peek().getStartPos());
	    				param_num++;
    				}
    				
    				expect(TokenType.R_paren);
    				if(fn.getIndex_global() <=8){
                        ins = new Instruction(InstructionType.callName, true, fn.getIndex_global());
                        ins.setNeedRelocation(true);
                        func.instructionList.add(ins);
                    }
    				
    				t = fn_.getRet();
    				}
    			}
    			
    			break;
    		default:
    			throw new AnalyzeError(ErrorCode.IncompleteExpression,peek().getEndPos());	
    	}
    	return t;
    }
}
    