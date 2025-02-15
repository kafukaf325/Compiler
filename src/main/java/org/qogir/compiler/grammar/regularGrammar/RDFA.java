package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.FiniteAutomaton;
import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;

import java.util.HashMap;

public class RDFA extends FiniteAutomaton {

    /**
     * holds the maps between DFA states and NFA state sets
     */
    private HashMap<State, HashMap<Integer,State>> StateMappingBetweenDFAAndNFA = new HashMap<>();
    //hashmap第一个元是状态，第二个元是对应NFA中的状态组成的hashmap
    public RDFA(){
        super();
        this.StateMappingBetweenDFAAndNFA = new HashMap<>();
        //this.transitTable = new LabeledDirectedGraph<>();
        //挠缠代码，不知道怎么想的
    }

    public RDFA(State startState){
        this.startState = startState;
        this.StateMappingBetweenDFAAndNFA = new HashMap<>();
        this.transitTable = new LabeledDirectedGraph<>();
        this.getTransitTable().addVertex(this.startState);
    }

    public void setStateMappingBetweenDFAAndNFA(State s, HashMap<Integer,State> nfaStates){
        this.StateMappingBetweenDFAAndNFA.put(s,nfaStates);
    }

    public HashMap<State, HashMap<Integer, State>> getStateMappingBetweenDFAAndNFA() {
        return StateMappingBetweenDFAAndNFA;
    }

    public String StateMappingBetweenDFAAndNFAToString() {//输出状态转化表

        String str = "";
        for(State s : this.getStateMappingBetweenDFAAndNFA().keySet()){
            //System.out.println(s);
            String mapping = "";
            for(State ns : this.getStateMappingBetweenDFAAndNFA().get(s).values()){
                //System.out.println("   " + ns);
                mapping += ns.toString() + ",";
            }
            mapping = mapping.substring(0,mapping.length()-1);

            mapping = "DFA State:" + s.toString() + "\tNFA State set:\t{"+ mapping + "}" + "\n";
            //System.out.println(mapping);
            str = str + mapping;
            //System.out.println(str);
        }
        return str;
    }
}
