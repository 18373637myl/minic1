package Tokenizer;
import error.TokenizeError;
import error.ErrorCode;

public class Tokenizer {
	private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }
	
	private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
	
	public Token nextToken(){
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        try {
        	char peek = it.peekChar();
	        if (Character.isDigit(peek)) {
	            return lexUIntOrDouble();
	        } else if (Character.isLetter(peek)||peek=='_') {
	            return lexIdentOrKeyword();
	        } else if (peek=='\"') {
	        	return StringType();
	        } else {
	            return lexOperatorOrUnknown();
	        }
        }catch(Exception e){
        	System.out.println(e.toString());
        }
        return null;
    }

    private Token lexUIntOrDouble() {

        Pos startpos = it.currentPos();
        int sum      = 0;
        double sum_d = 0.0;

        while(Character.isDigit(it.peekChar())){
            sum  = sum*10;
            sum += it.nextChar()-'0';
        }
        
        if(it.peekChar() == '.') {
        	it.nextChar();
        	double a = 1.0;
        	while(Character.isDigit(it.peekChar())){
        		sum_d = sum_d *10 + it.nextChar()-'0';
                a *= 0.1;
            }
        	sum_d *= a;
        	sum_d += sum;
        	return new Token(TokenType.Double, sum_d, startpos, it.previousPos());
        }
        if(it.peekChar() == 'e' || it.peekChar() == 'E') {
        	
        }
        
        return new Token(TokenType.Uint, sum, startpos, it.previousPos());

    }

    private Token StringType() throws TokenizeError{
    	Pos startpos = it.currentPos();
    	StringBuilder s = new StringBuilder();
    	
    	while(it.peekChar() != '\"') {
    		if(it.peekChar() == '\\') {
    			it.nextChar();
    			if(it.peekChar() == '\"')      s.append('\"');
    			else if(it.peekChar() == '\'') s.append('\'');
    			else if(it.peekChar() == 'n')  s.append('\n');
    			else if(it.peekChar() == 't')  s.append('\t');
    			else if(it.peekChar() == 'r')  s.append('\r');
    			else if(it.peekChar() == '\\') s.append('\\');
    			else throw new TokenizeError(ErrorCode.InvalidString,it.previousPos());
    			it.nextChar();
    		}
    		else {
    			s.append(it.nextChar());
    		}
    	}
    	it.nextChar();
    	return new Token(TokenType.String, s.toString(), startpos, it.previousPos());
    }
    
    private Token lexIdentOrKeyword() {

        Pos startpos = it.currentPos();
        
        StringBuilder nowToken = new StringBuilder("");
        nowToken.append(it.nextChar());
        while(Character.isLetterOrDigit(it.peekChar())||it.peekChar()=='_'){
            nowToken.append(it.nextChar());
        }

        String nowToken_s = nowToken.toString();
        if(nowToken_s.equals("fn")) return new Token(TokenType.Fn, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("let")) return new Token(TokenType.Let, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("const")) return new Token(TokenType.Const, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("as")) return new Token(TokenType.As, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("while")) return new Token(TokenType.While, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("if")) return new Token(TokenType.If, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("else")) return new Token(TokenType.Else, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("return")) return new Token(TokenType.Return, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("break")) return new Token(TokenType.Break, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("continue")) return new Token(TokenType.Continue, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("int")) return new Token(TokenType.Type, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("void")) return new Token(TokenType.Type, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("double")) return new Token(TokenType.Type, nowToken_s, startpos, it.previousPos());
        else if(nowToken_s.equals("char")) return new Token(TokenType.Type, nowToken_s, startpos, it.previousPos());
        
        // -- 如果是关键字，则返回关键字类型的 token
        else return new Token(TokenType.Ident, nowToken_s, startpos, it.previousPos());
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
    }

    private Token lexOperatorOrUnknown() throws TokenizeError{
    	var peeked = it.peekChar();
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.Plus, '+', it.previousPos(), it.currentPos());
            case '-':
            	if(it.peekChar()=='>')
            	{
            		it.nextChar();
            		return new Token(TokenType.Arrow, "->", it.previousPos(), it.currentPos());
            	}
                return new Token(TokenType.Minus, '-', it.previousPos(), it.currentPos());
            case '*':
                return new Token(TokenType.Mul, '*', it.previousPos(), it.currentPos());
            case '/':
            	if(it.peekChar()=='/') {
            		it.nextChar();
            		StringBuilder nowToken = new StringBuilder("");
                    while(it.peekChar()!='\n' && !it.isEOF())
                    	nowToken.append(it.nextChar());
                    return nextToken();
            	}
                return new Token(TokenType.Div, '/', it.previousPos(), it.currentPos());           
            case '=':
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.Eq, "==", it.previousPos(), it.currentPos());
            	}
            	return new Token(TokenType.Assign, '=', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.L_paren, '(', it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_paren, ')', it.previousPos(), it.currentPos()); 
            case '{':
            	return new Token(TokenType.L_brace, '{', it.previousPos(), it.currentPos());  	
            case '}':
            	return new Token(TokenType.R_brace, '}', it.previousPos(), it.currentPos());
            case ':':
            	return new Token(TokenType.Colon, ':', it.previousPos(), it.currentPos());
            case ',':
            	return new Token(TokenType.Comma, ',', it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.Semicolon, ';', it.previousPos(), it.currentPos());
            case '!':
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.Neq, "!=", it.previousPos(), it.currentPos());
            	}
            	else throw new TokenizeError(ErrorCode.InvalidOperator, it.previousPos());
            case '<':
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.Le, "<=", it.previousPos(), it.currentPos());
            	}
            	return new Token(TokenType.Lt, '<', it.previousPos(), it.currentPos());
            case '>':
            	if(it.peekChar()=='=') {
            		it.nextChar();
            		return new Token(TokenType.Ge, ">=", it.previousPos(), it.currentPos());
            	}
            	return new Token(TokenType.Gt, '>', it.previousPos(), it.currentPos());     	
            default:
            	System.out.println(peeked);
                throw new TokenizeError(ErrorCode.InvalidOperator, it.previousPos());
        }
    }
}
