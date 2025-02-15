package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;

import java.security.KeyPair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The subset construction Algorithm for converting an NFA to a DFA.
 * The subset construction Algorithm takes an NFA N as input and output a DFA D accepting the same language as N.
 * The main mission is to eliminate ε-transitions and multi-transitions in NFA and construct a transition table for D.
 * The algorithm can be referred to {@see }
 */
public class SubsetConstruction {

    /**
     * Eliminate all ε-transitions reachable from a single state in NFA through the epsilon closure operation.
     * @param s a single state of NFA
     * @param tb the transition table of NFA
     * @return a set of state reachable from the state s on ε-transition
     * @author xuyang
     */
    private HashMap<Integer, State> epsilonClosures(State s, LabeledDirectedGraph<State> tb){

        //System.out.println(s);
        //System.out.println(tb.vertexSet());
        if (!tb.vertexSet().contains(s)) { //if vertex s not in the transition table
            System.out.println("错误");
            return null;
        }

        HashMap<Integer,State> nfaStates = new HashMap<>();
        nfaStates.put(s.getId(),s);
        for(LabelEdge edge : tb.edgeSet()){
            State target = (State) edge.getTarget();
            if(s.equals((State) edge.getSource())&&edge.getLabel()=='ε'&&!nfaStates.containsValue(target)){
                //System.out.println(111);
                nfaStates.put(target.getId(),target);//把s通过空变迁可以到达的状态放到hashmap里面
                HashMap<Integer,State> newnfaStates = epsilonClosures(target,tb);
                nfaStates.putAll(newnfaStates);

            }
        }//对单个状态找出他的空变迁闭包，这里可以考虑改用arraydeque来bfs

        //Add your implementation



        return nfaStates;
    }

    /**
     * Eliminate all ε-transitions reachable from a  state set in NFA through the epsilon closure operation
     * @param ss a state set of NFA
     * @param tb the transition table of NFA
     * @return a set of state reachable from the state set on ε-transition
     * @author xuyang
     */

    public HashMap<Integer, State> epsilonClosure(HashMap<Integer, State> ss, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            nfaStates.putAll(epsilonClosures(s,tb));
        }
        return nfaStates;
    }

    /**
     *
     * @param s
     * @param ch
     * @param tb
     * @return
     */
    private HashMap<Integer,State> moves(State s, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();

        //System.out.println(ch);
        //Add your implementation
        for(LabelEdge edge : tb.edgeSet()){
            State target = (State) edge.getTarget();

            if(s.equals((State) edge.getSource()) && edge.getLabel()==ch && !nfaStates.containsValue(target)){
                //System.out.println(target);
                nfaStates.put(target.getId(),target);//把s通过 ch 变迁可以到达的状态放到hashmap里面
            }
        }

        return nfaStates;
    }

    public HashMap<Integer,State> move(HashMap<Integer, State> ss, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> nfaStates = new HashMap<>();
        for(State s : ss.values()){
            nfaStates.putAll(moves(s,ch,tb));
        }
        //System.out.println(nfaStates);
        return nfaStates;
    }//把整个集合中的ch转化对应状态全部加到hashmap里面

    public HashMap<Integer,State> epsilonClosureWithMove(HashMap<Integer, State> sSet, Character ch, LabeledDirectedGraph<State> tb){
        HashMap<Integer,State> states = new HashMap<>();
        states.putAll(epsilonClosure(move(sSet, ch, tb),tb));
        return states;
    }//返回的是sSet里面所有状态通过ch转化到达的状态再通过空变迁转化到的状态的集合

    public RDFA subSetConstruct(TNFA tnfa){

        // Add your implementation
        if(tnfa == null){
            return null;
        }

        ArrayDeque<HashMap<Integer,State>> sSetDeque = new ArrayDeque<>();//存放新产生的集合等待遍历
        ArrayDeque<State> stateDeque = new ArrayDeque<>();//存放上面集合对应在dfa里面的状态

        RDFA rdfa = new RDFA();

        rdfa.setAlphabet(tnfa.getAlphabet());

        LabeledDirectedGraph<State> tb = tnfa.getTransitTable();//tnfa的状态转化表

        State dfaBeginState = rdfa.getStartState();

        State nfaBeginState = tnfa.getStartState();
        State nfaEndState = tnfa.getAcceptingState();

        HashMap<Integer,State> dfaBeginSet = epsilonClosures(nfaBeginState,tb);

        //System.out.println(dfaBeginSet);

        if(dfaBeginSet.containsValue(nfaEndState)){
            dfaBeginState.setType(State.ACCEPT);
        }

        rdfa.setStateMappingBetweenDFAAndNFA(dfaBeginState,dfaBeginSet);

        //System.out.println(dfaBeginState + " " + dfaBeginSet);

        //接下来通过遍历入队bfs的方式创造新状态
        sSetDeque.addLast(dfaBeginSet);
        stateDeque.addLast(dfaBeginState);

        while(!sSetDeque.isEmpty()){
            HashMap<Integer,State> firstSet = sSetDeque.pollFirst();//取出第一个
            State firstState = stateDeque.pollFirst();

            //System.out.println(firstSet + " " + firstState);
            //System.out.println(firstSet);

            for(char ch : rdfa.getAlphabet()){
                HashMap<Integer,State> newSet = epsilonClosureWithMove(firstSet,ch,tb);
                //System.out.println(newSet);


                if(!newSet.isEmpty()&&!rdfa.getStateMappingBetweenDFAAndNFA().containsValue(newSet)){//假如该集合不存在，则需要创建新状态

                    //System.out.println(newSet + " " + ch);
                    State newDfaState = new State();
                    if(newSet.containsValue(nfaEndState)){
                        newDfaState.setType(State.ACCEPT);
                    }

                    rdfa.setStateMappingBetweenDFAAndNFA(newDfaState,newSet);//加入对应的集合表里面并创建一个新的状态作为对应dfa的状态
                    //System.out.println(newDfaState + " " + newSet);


                    sSetDeque.addLast(newSet);
                    stateDeque.addLast(newDfaState);//加入到队列里面等待扫描

                    rdfa.getTransitTable().addVertex(newDfaState);
                    rdfa.getTransitTable().addEdge(firstState,newDfaState,ch);
                }
                else if(!newSet.isEmpty()&&rdfa.getStateMappingBetweenDFAAndNFA().containsValue(newSet)){//假如该集合已经存在，则不需要新建，只需要把边加上
                    //接下来获取这个集合对应的状态，要怎么获取？
                    for(State s : rdfa.getStateMappingBetweenDFAAndNFA().keySet()){
                        if(rdfa.getStateMappingBetweenDFAAndNFA().get(s).equals(newSet)){
                            rdfa.getTransitTable().addEdge(firstState,s,ch);
                        }
                    }
                }
            }

        }

        return rdfa;
    }
}
