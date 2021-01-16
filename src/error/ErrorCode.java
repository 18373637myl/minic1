package error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError, EOF, InvalidString, InvalidIdentifier, IntegerOverflow, // int32_t overflow.
    NoMain, NoEnd, NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    NotDeclared, AssignToConstant, DuplicateDeclaration, NotInitialized, InvalidAssignment, InvalidPrint, ExpectedToken,
    InvalidOperator, ProgramError, OverDeclared, TypeError
}
