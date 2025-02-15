package org.qogir.simulation.scanner;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.grammar.regularGrammar.*;
import org.qogir.compiler.util.graph.LabelEdge;

import java.util.HashMap;

/**
 * An implementation of a regular grammar for:
 * 1)
 */
public class Scanner {

    /**
     * A regular grammar {@see }
     */
    private final RegularGrammar rg;//表达式类

    /**
     * Hold regex trees for each regex
     */
    private HashMap<Regex, RegexTree> regexToRegexTree = new HashMap<>();

    /**
     * Hold NFAs for each regex
     */
    private HashMap<Regex,TNFA> RegexToNFA = new HashMap<>();

    /**
     * Hold NFA sets for each DFA State
     */
    private HashMap<State, HashMap<Integer,State>> DFAToNFAs = new HashMap<>();

    /**
     * Hold equivalent state sets for minimized DFA
     */
    private HashMap<State,HashMap<Integer,State>> MinDFAToDFAs = new HashMap<>();

    public Scanner(RegularGrammar rg){
        this.rg = rg;
    }

    public Scanner(String[] regexes){
        rg = new RegularGrammar(regexes);
    }

    public HashMap<Regex, RegexTree> getRegexToRegexTree() {
        return regexToRegexTree;
    }

    public HashMap<Regex, TNFA> getRegexToNFA() {
        return RegexToNFA;
    }

    public HashMap<State, HashMap<Integer, State>> getDFAToNFAs() {
        return DFAToNFAs;
    }

    public HashMap<State, HashMap<Integer, State>> getMinDFAToDFAs() {
        return MinDFAToDFAs;
    }

    /**
     * Build a regex tree by ParseRegex{@link ParseRegex#parse}
     * @param r a regex
     * @return a regex tree {@link RegexTree}
     */
    public RegexTree constructRegexTree(Regex r){//这里进来个正则表达式，转个树
        ParseRegex parser = new ParseRegex(r);
        RegexTree tree = parser.parse();
        regexToRegexTree.put(r,tree);
        return tree;
    }

    /**
     * Build regex trees for a Regular grammar with more regexes.
     * The method calls {@see constructRegexTree} and can be used for a Regular grammar with only one regex.
     * @return a collection of regex trees each for one regex in the regular grammar.
     */

    public HashMap<Regex,RegexTree> constructRegexTrees(){//先从外部拷贝入rg，然后在内部用递归转化成树
        if(rg == null)
            return null;
        for(Regex r: rg.getPatterns()){
            regexToRegexTree.put(r,constructRegexTree(r));
        }
        return regexToRegexTree;
    }

    /**
     * This private method is used to construct an NFA for a regex.
     * The construction is based on McNaughton-Yamada-Thompson algorithm {@link ThompsonConstruction#translate}.
     * @param r a regex
     * @return An NFA
     */
    public TNFA constructRegexNFA(Regex r){
        RegexTree tree = constructRegexTree(r);//调转树的函数

        if(tree == null)return null;

        ThompsonConstruction thompsonConstruction = new ThompsonConstruction();
        TNFA nfa = thompsonConstruction.translate(tree.getRoot());//调转TNFA的函数
        for(Character ch : r.getRegex().toCharArray()){
            if(Character.isLetter(ch) && ch != 'ε' && !nfa.getAlphabet().contains(ch)){
                nfa.getAlphabet().add(ch);//往里加点
            }
        }
        return nfa;
    }

    /**
     * This private method is used to construct an DFA for a regex.
     * The construction is based on subset construction algorithm {@link SubsetConstruction#subSetConstruct}.
     * @param r a regex
     * @return An DFA
     */
    public RDFA constructRegexDFA(Regex r){
        TNFA nfa = constructRegexNFA(r);
        SubsetConstruction subsetConstruction = new SubsetConstruction();
        RDFA dfa = subsetConstruction.subSetConstruct(nfa);
        dfa.setAlphabet(nfa.getAlphabet());
        return dfa;
    }

    /**
     * Construct NFAs for all regexes in a regular grammar.
     * @return An NFA set
     */
    public HashMap<Regex,TNFA> constructAllNFA(){
        HashMap<Regex,TNFA> rtonfa = new HashMap<>();
        for(Regex r : rg.getPatterns()){
            rtonfa.put(r,constructRegexNFA(r));
        }
        return rtonfa;
    }

    /**
     *  Construct DFAs for all regexes in a regular grammar.
     * @return a DFA set
     */
    public HashMap<Regex,RDFA> constructAllDFA(){
        HashMap<Regex,RDFA> rtodfa = new HashMap<>();
        for(Regex r : rg.getPatterns()){
            rtodfa.put(r,constructRegexDFA(r));
        }
        return rtodfa;
    }

    /**
     * construct an DFA with an NFA
     * @param nfa an NFA
     * @return an DFA
     */
    public RDFA constructDFA(TNFA nfa){
        SubsetConstruction subsetConstruction = new SubsetConstruction();
        RDFA dfa = subsetConstruction.subSetConstruct(nfa);
        dfa.setAlphabet(nfa.getAlphabet());
        return dfa;
    }

    /**
     * Minimize an DFA by State Minimization algorithm {@link StateMinimization#minimize}
     * @param dfa an DFA
     * @return an DFA
     */
    public RDFA minimizeDFA(RDFA dfa){
        StateMinimization stateMinimization = new StateMinimization();
        RDFA miniDFA = stateMinimization.minimize(dfa);
        miniDFA.setAlphabet(dfa.getAlphabet());
        return miniDFA;
    }

    /**
     * Construct an NFA for a regular grammar.
     * @return An NFA
     */
    public TNFA constructNFA(){//下一步最主要的函数在这里
        if(rg.getPatterns().size() == 1){//只有一个就直接构造完然后返回
            Regex r = rg.getPatterns().get(0);
            TNFA nfa = constructRegexNFA(r);//转NFA
            if(nfa != null){
                this.RegexToNFA.put(r, nfa);
                nfa.setAlphabet(rg.getSymbols());
                return nfa;
            }
            return new TNFA();
        }
        else if(rg.getPatterns().size() > 1) {//有多个就分别构造然后合并返回
            for (Regex r : rg.getPatterns()) {
                TNFA nfa = constructRegexNFA(r);
                if (nfa != null)
                    this.RegexToNFA.put(r, nfa);
            }
            TNFA nfa = new TNFA();
            for (TNFA tn : this.RegexToNFA.values()) {
                if(tn.getStartState().getType() != State.ACCEPTANDSTART){
                    tn.getStartState().setType(State.MIDDLE);
                }
                else{
                    tn.getStartState().setType(State.ACCEPT);//这是个什么东西？
                }
                nfa.getTransitTable().merge(tn.getTransitTable());

                nfa.getTransitTable().addEdge(new LabelEdge(nfa.getStartState(), tn.getStartState(), 'ε'));

                tn.getAcceptingState().setType(State.MIDDLE);

                nfa.getTransitTable().addEdge(new LabelEdge(tn.getAcceptingState(),nfa.getAcceptingState(),'ε'));
            }


            /*自己修改：把合并后表内的状态类型做了修改*/
            for(State perState : nfa.getTransitTable().vertexSet()){
                if(!perState.equals(nfa.getStartState())&&perState.getType()==State.START){
                    perState.setType(State.MIDDLE);
                }
            }
            for(LabelEdge perEdge : nfa.getTransitTable().edgeSet()){
                State source = (State) perEdge.getSource();
                State target = (State) perEdge.getTarget();
                if(!source.equals(nfa.getStartState())&&source.getType()==State.START){
                    source.setType(State.MIDDLE);
                }
                if(!target.equals(nfa.getStartState())&&target.getType()==State.START){
                    target.setType(State.MIDDLE);
                }
            }

            nfa.setAlphabet(rg.getSymbols());
            return nfa;
        }
        return null;
    }

    /**
     * construct DFA for a regular grammar
     * @return a DFA
     */
    public RDFA constructDFA(){
        TNFA nfa = constructNFA();
        SubsetConstruction subsetConstruction = new SubsetConstruction();
        RDFA dfa = subsetConstruction.subSetConstruct(nfa);
        dfa.setAlphabet(rg.symbols);
        return dfa;
    }
}
