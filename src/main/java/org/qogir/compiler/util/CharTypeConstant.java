package org.qogir.compiler.util;

public class CharTypeConstant {

    //0-basic；1-concatenation；2-union； 3-kleene closure; 4-leftParenthesis; 5-rightParenthesis
    // 普通字母(例如'a', 'b')以及空符号'ε'
    public static final Integer CHAR_BASIC = 0;
    // 连接符号‘-’
    public static final Integer CHAR_CONCATENATION = 1;
    // 并集符号‘|’
    public static final Integer CHAR_UNION = 2;
    // 克林闭包符号‘*’
    public static final Integer CHAR_KLEENE_CLOSURE = 3;
    // 左括号’(‘
    public static final Integer CHAR_LEFT_PARENTHESIS = 4;
    // 右括号')'
    public static final Integer CHAR_RIGHT_PARENTHESIS = 5;
}
