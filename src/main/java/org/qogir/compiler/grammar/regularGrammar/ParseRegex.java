package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.util.CharTypeConstant;
import org.qogir.compiler.util.ExceptionConstant;

import java.util.ArrayDeque;
import java.util.Stack;

/**
 * An implementation of converting a regex to a regex tree.
 * @author xuyang
 */

public class ParseRegex {//这个类用于把表达式转树
    private ArrayDeque<Character> queue = new ArrayDeque<>();

    private String name = "";
    /**
     * Construct a ParseRegex object with a regex.
     * The input string of regex is divided into a sequence of char
     * For convenience, the char '%' indicating the end of the input is appended to the sequence
     * @param regex a regular expression
     */
    public ParseRegex(Regex regex){
        char[] ch = regex.getRegex().toCharArray();
        for(char c : ch){
            this.queue.add(c);
        }

        //%-end of the regex
        this.queue.add('%');

        name = regex.getName();
    }

    private boolean kleeneClosureMerge(Stack<RegexTreeNode> stack) {
        RegexTreeNode knode;
        int peekType = stack.peek().getType();
        if(peekType == CharTypeConstant.CHAR_BASIC){// is basic before *
            knode = new RegexTreeNode('*',CharTypeConstant.CHAR_KLEENE_CLOSURE,stack.pop(),null);
        }
        else if(peekType == CharTypeConstant.CHAR_RIGHT_PARENTHESIS){ //is a ')' before *
            stack.pop(); //pop ')'
            knode = new RegexTreeNode('*',CharTypeConstant.CHAR_KLEENE_CLOSURE,stack.pop(),null);
            stack.pop(); //pop '('
        }else{ //is other char before *, not legal
            return false;
        }
        stack.push(knode);
        return true;
    }
    private Stack<RegexTreeNode> parenthesis2Stack(Stack<RegexTreeNode> stack) {
        Stack<RegexTreeNode> rstack = new Stack<>();
        while(!stack.isEmpty() && stack.peek().getType() != CharTypeConstant.CHAR_LEFT_PARENTHESIS){
            if( stack.peek().getType() == CharTypeConstant.CHAR_BASIC
                    || stack.peek().getType() == CharTypeConstant.CHAR_CONCATENATION
                    || stack.peek().getType() == CharTypeConstant.CHAR_KLEENE_CLOSURE){
                //basic,kleene or concatenation(the case of conca exist?)
                rstack.push(stack.pop());
            }
            else if(stack.peek().getType() == CharTypeConstant.CHAR_UNION){//union, case (stack|?)
                if(rstack.isEmpty()){ //case (stack|)
                    return null;
                }
                // case (?|...)
                RegexTreeNode unode = stack.pop();
                if(rstack.size() > 1){ //case (stack|...)
                    RegexTreeNode cnode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
                    cnode = mergeStackAsOneChild(cnode,rstack);//
                    unode.getLastChild().setNextSibling(cnode);
                }else{ //size = 1, case (stack|.)
                    unode.getLastChild().setNextSibling(rstack.pop());
                }
                rstack.push(unode);

            }
            else if(stack.peek().getType() == CharTypeConstant.CHAR_RIGHT_PARENTHESIS){ //case ...(stack))
                stack.pop(); //pop ')'
                rstack.push(stack.pop());
                stack.pop(); // pop '('
            }
        }
        return rstack;
    }
    private boolean unionMerge(Stack<RegexTreeNode> stack) {
        RegexTreeNode unode;
        Stack<RegexTreeNode> ustack = new Stack<>();
        while(!stack.isEmpty()
                && stack.peek().getType() != CharTypeConstant.CHAR_UNION
                && stack.peek().getType() != CharTypeConstant.CHAR_LEFT_PARENTHESIS ){
            ustack.push(stack.pop());
        }

        if(stack.isEmpty() || stack.peek().getType() == CharTypeConstant.CHAR_LEFT_PARENTHESIS){
            unode = new RegexTreeNode('|',CharTypeConstant.CHAR_UNION,null,null);
            RegexTreeNode firstChildNode;
            if(ustack.size() == 1){
                firstChildNode = ustack.pop();
            }
            else if(ustack.size() > 1){
                RegexTreeNode cnode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
                cnode = mergeStackAsOneChild(cnode,ustack);
                firstChildNode = cnode;
            }
            else{
                return false;
            }
            unode.setFirstChild(firstChildNode);
        }
        else{ //type=2
            unode = stack.pop();
            RegexTreeNode lastNode = unode.getLastChild();
            if(ustack.size() == 1){
                lastNode.setNextSibling(ustack.pop());
            }
            else{
                RegexTreeNode cnode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
                cnode = mergeStackAsOneChild(cnode,ustack);
                lastNode.setNextSibling(cnode);
            }
        }
        stack.push(unode);
        return true;
    }
    private void parenthesisMerge(Stack<RegexTreeNode> stack, Stack<RegexTreeNode> rstack) {
        // 1) convert the nodes in rstack into one node
        // 2) push the converted node and the right-parenthesis node
        if(!rstack.isEmpty()){
            if(rstack.size() > 1){
                RegexTreeNode cnode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
                cnode = mergeStackAsOneChild(cnode,rstack);
                stack.push(cnode);

            }else{ //rstack.size = 1
                stack.push(rstack.pop());
            }
            RegexTreeNode rnode = new RegexTreeNode(')',CharTypeConstant.CHAR_RIGHT_PARENTHESIS,null,null);
            stack.push(rnode);
        }
        else {
            stack.pop();
        }
    }

    private void removeParenthesis(Stack<RegexTreeNode> stack) {
        // 下一步：处理括号
        Stack<RegexTreeNode> pstack = new Stack<>();
        while(!stack.isEmpty() && stack.peek().getType() != CharTypeConstant.CHAR_UNION){
            pstack.push(stack.pop());
        }

        if(stack.isEmpty()){
            while(!pstack.isEmpty())
                stack.push(pstack.pop());
        }
        else if(stack.peek().getType() == CharTypeConstant.CHAR_UNION){
            if(pstack.peek().getType() == CharTypeConstant.CHAR_RIGHT_PARENTHESIS){
                while(!pstack.isEmpty()){
                    stack.push(pstack.pop());
                }
            }
            else{
                RegexTreeNode unode;
                unode = stack.pop();
                if(pstack.size() == 1){
                    unode.getLastChild().setNextSibling(pstack.pop());
                }
                else{
                    RegexTreeNode cnode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
                    cnode = mergeStackAsOneChild(cnode,pstack);
                    unode.getLastChild().setNextSibling(cnode);
                }
                stack.push(unode);
            }
        }
    }

    /**
     * Converting the regex into a regex tree
     * 1) Set a stack to hold the nodes of regex tree
     * 2) The variable "look" hold the next input char
     * 3) According to the type of look, a new node is
     *    created when the type of the "look" is basic case (type = letter or ε),
     *    or a new node is created by popping several nodes in the stack, merging
     *    them as a concatenation node, taking the conca node as its child when
     *    the type of the "look" is operator (type = * ,(, ) or |)，
     *    Then the new node is pushed into the stack.
     * @return a regex tree
     * @author xuyang
     */
    public RegexTree parse() {//做出一棵树的方法，返回的是RegexTree对象
        //System.out.println("\t2.1) Convert the regex into a tree.");
        if(this.queue.isEmpty())
            return null;

        Stack<RegexTreeNode> stack = new Stack<>();
        //lookahead char
        char look = this.queue.poll();

        if(look == '%'){
            System.out.println(name + ExceptionConstant.NULL_ERROR);
            return null;
        }
        else if(!Character.isLetter(look) &&  look != '('){ //look != 'ε' &&
            //The first char must be a letter, ε or '('
            System.out.println(name + ExceptionConstant.BEGIN_CHAR_ERROR);
            return null;
        }

        // 手动取出第一个符号，进行后续的判断
        int t;
        if(Character.isLetter(look) || look == 'ε')
            t= CharTypeConstant.CHAR_BASIC;
        else
            t=CharTypeConstant.CHAR_LEFT_PARENTHESIS;

        RegexTreeNode node = new RegexTreeNode(look, t, null, null);
        stack.push(node);

        look = this.queue.poll();
        while(look != '%'){
            if(look == '*'){
                boolean closureMergeResult = kleeneClosureMerge(stack);
                if(!closureMergeResult) {
                    System.out.println(name + ExceptionConstant.PRE_KLEENE_CHAR_ERROR);
                    return null;
                }
            }
            else if(look == '(') {
                RegexTreeNode lnode = new RegexTreeNode('(', CharTypeConstant.CHAR_LEFT_PARENTHESIS, null, null);
                stack.push(lnode);
                //how about the case of "...(..." (right parenthesis is missing)这里是不是没有做？
            }
            else if(look == ')'){
                if(stack.isEmpty()){
                    System.out.println(name + ExceptionConstant.LEFT_PARENTHESIS_MISSING_ERROR);
                    return null;
                }

                if(stack.peek().getType() == CharTypeConstant.CHAR_LEFT_PARENTHESIS){ // case: ()
                    stack.pop();
                }
                else{
                    Stack<RegexTreeNode> rstack = parenthesis2Stack(stack);
                    if(rstack == null) {
                        System.out.println(name + ExceptionConstant.UNION_RIGHT_PARENTHESIS_ERROR);
                        return null;
                    }
                    if(stack.isEmpty()){
                        System.out.println(name + ExceptionConstant.LEFT_PARENTHESIS_MISSING_ERROR);
                        return null;
                    }
                    else if(stack.peek().getType() == CharTypeConstant.CHAR_LEFT_PARENTHESIS){ //case (rstack)
                        parenthesisMerge(stack, rstack);
                    }
                }
            }
            //0-basic；1-concatenation；2-union； 3-kleene closure; 4-leftParenthesis; 5-rightParenthesis
            else if(look == '|'){
                if(this.queue.peek() == '%'){
                    System.out.println(name + ExceptionConstant.UNION_RIGHT_MISSING_ERROR);
                    return null;
                }
                t = stack.peek().getType();
                if(t == CharTypeConstant.CHAR_LEFT_PARENTHESIS || t == CharTypeConstant.CHAR_UNION){
                    System.out.println(name + ExceptionConstant.UNION_LEFT_ERROR);
                    return null;
                }
                boolean unionMergeResult = unionMerge(stack);
                if(!unionMergeResult) {
                    System.out.println(name + ExceptionConstant.UNION_LEFT_MISSING_ERROR);
                    return null;
                }
            }
            else if(Character.isLetter(look) || look == 'ε'){
                RegexTreeNode bnode = new RegexTreeNode(look,CharTypeConstant.CHAR_BASIC,null,null);
                stack.push(bnode);
            }
            else{
                System.out.println(name + ExceptionConstant.ILLEGAL_CHAR + look);
                return null;
            }

            look = this.queue.poll();
        }

        //if look == '%'
        if(stack.isEmpty()){
            return null;
        }
        removeParenthesis(stack);

        // merge the nodes in stack as one node
        Stack<RegexTreeNode> treeStack = new Stack<>();
        while(!stack.isEmpty()){
            if(stack.peek().getType() == CharTypeConstant.CHAR_LEFT_PARENTHESIS || stack.peek().getType() == CharTypeConstant.CHAR_RIGHT_PARENTHESIS){
                stack.pop();
            }
            else
                treeStack.push(stack.pop());
        }

        if(treeStack.isEmpty()){
            return null;
        }

        RegexTree tree = new RegexTree();
        if(treeStack.size() > 1){
            RegexTreeNode treeNode = new RegexTreeNode('-',CharTypeConstant.CHAR_CONCATENATION,null,null);
            treeNode = mergeStackAsOneChild(treeNode,treeStack);
            tree.setRoot(treeNode);
        }
        else{
            tree.setRoot(treeStack.pop());
        }

        return tree;
    }

    /**
     * Make nodes in a stack as children of one node.
     * @param stack holds the nodes that will be merged.
     */
    public RegexTreeNode mergeStackAsOneChild(RegexTreeNode pt, Stack<RegexTreeNode> stack){
        RegexTreeNode temp1 = stack.pop();
        RegexTreeNode firstChild = temp1;
        while(!stack.isEmpty()){
            RegexTreeNode temp2 = stack.pop();
            temp1.setNextSibling(temp2);
            temp1 = (RegexTreeNode) temp1.getNextSibling();
        }
        if(pt.getFirstChild() == null) {
            pt.setFirstChild(firstChild);
        }
        else {
            RegexTreeNode temp = pt.getLastChild();
            temp.setNextSibling(firstChild);
        }
        return pt;
    }
}
