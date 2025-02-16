package org.qogir.compiler.util;

public class ExceptionConstant {
    public static final String NULL_ERROR = "：表达式为空！";
    public static final String BEGIN_CHAR_ERROR = "：非法输入：表达式第一位必须为字母、左括号或者ε";
    public static final String PRE_KLEENE_CHAR_ERROR = "：非法输入：符号*前面必须为字母或右括号";
    public static final String LEFT_PARENTHESIS_MISSING_ERROR = "：非法输入：左括号缺失";
    public static final String UNION_RIGHT_PARENTHESIS_ERROR = "：非法输入：|)不能连接在一起";
    public static final String UNION_RIGHT_MISSING_ERROR = "：非法输入：|右侧无内容";
    public static final String UNION_LEFT_ERROR = "：非法输入：(|、||不能连接在一起";
    public static final String UNION_LEFT_MISSING_ERROR = "：非法输入：|左侧无内容";
    public static final String ILLEGAL_CHAR = "：非法输入：输入了非法字符：";
}
