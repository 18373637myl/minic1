package Tokenizer;

public enum TokenType {
    /* 空 */
    None,
    /* EOF */
    EOF,
    /* 关键字 */
    Fn,
    Let,
    Const,
    As,
    While,
    If,
    Else,
    Return,
    Break,
    Continue,

    /* 标识符 */
    Ident,
    Type,
    
    /* 字面量 */
    Uint,
    String,
    Double,
    Char,

    /* 操作符 */
    Plus,
    Minus,
    Mul,
    Div,
    /* = */
    Assign,
    /* == */
    Eq,
    /* != */
    Neq,
    /* < */
    Lt,
    /* > */
    Gt,
    /* <= */
    Le,
    /* >= */
    Ge,
    L_paren,
    R_paren,
    /* 大括号 */
    L_brace,
    R_brace,
    /* -> */
    Arrow,
    /* , */
    Comma,
    /* : */
    Colon,
    /* ; */
    Semicolon,
    
    /* 注释 */
    Comment,
}