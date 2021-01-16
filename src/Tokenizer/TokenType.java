package Tokenizer;

public enum TokenType {
    /* �� */
    None,
    /* EOF */
    EOF,
    /* �ؼ��� */
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

    /* ��ʶ�� */
    Ident,
    Type,
    
    /* ������ */
    Uint,
    String,
    Double,
    Char,

    /* ������ */
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
    /* ������ */
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
    
    /* ע�� */
    Comment,
}