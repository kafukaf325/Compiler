package org.qogir.compiler.grammar.regularGrammar;

import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;

public class ThompsonConstruction {

    //0-basic；1-concatenation；2-union； 3-kleene closure;
    public TNFA translate(RegexTreeNode node) {
        if (node == null)
            return null;

        TNFA tnfa = new TNFA();

        State beginState = tnfa.getStartState();
        State endState = tnfa.getAcceptingState();

        // System.out.println(beginState.getId() +" "+ endState.getId() +" "+ node.getValue());

        if(node.getType() == 0) {//代表传入了一个基本符号
            //node的type是符号类型，state的type是状态类型

            tnfa.getTransitTable().addEdge(beginState,endState,node.getValue());
            tnfa.getAlphabet().add(node.getValue());
            //返回一个有头尾，经过转化的NFA

        }
        else if(node.getType() == 1) {//符号是-，即子结点串联。但是串联的前后状态如何连接呢？

            RegexTreeNode thisNode = (RegexTreeNode) node.getFirstChild();//用来遍历的

            State lastEndState = null;

            while(thisNode != null){
                TNFA perNFA = translate(thisNode);


                if(thisNode.equals(node.getFirstChild())){
                    tnfa.setStartState(perNFA.getStartState());//开始状态就是这个
                    beginState = tnfa.getStartState();
                }

                tnfa.getAlphabet().addAll(perNFA.getAlphabet());//合并所有的点
                tnfa.getTransitTable().merge(perNFA.getTransitTable());//拿到字母表和转化表

                if(lastEndState != null) {
                    lastEndState.setType(State.MIDDLE);
                    perNFA.getStartState().setType(State.MIDDLE);
                    tnfa.getTransitTable().addEdge(lastEndState,perNFA.getStartState(),'ε');
                }

                lastEndState = new State(perNFA.getAcceptingState());
                //lastEndState = perNFA.getAcceptingState();

                if(thisNode.equals(node.getLastChild())) {
                    tnfa.setAcceptingState(perNFA.getAcceptingState());//结束状态是整一个行最后的结束状态
                    endState = tnfa.getAcceptingState();
                }

                thisNode = (RegexTreeNode) thisNode.getNextSibling();
            }
        }

        else if(node.getType() == 2) {//符号是|，即或
            RegexTreeNode thisNode = (RegexTreeNode) node.getFirstChild();//用来遍历的

            while(thisNode != null){
                TNFA perNFA = translate(thisNode);

                tnfa.getAlphabet().addAll(perNFA.getAlphabet());//合并所有的点
                tnfa.getTransitTable().merge(perNFA.getTransitTable());//拿到字母表和转化表

                tnfa.getTransitTable().addEdge(beginState,perNFA.getStartState(),'ε');
                tnfa.getTransitTable().addEdge(perNFA.getAcceptingState(),endState,'ε');

                perNFA.getStartState().setType(State.MIDDLE);
                perNFA.getAcceptingState().setType(State.MIDDLE);

                thisNode = (RegexTreeNode) thisNode.getNextSibling();
            }
        }
        else if(node.getType() == 3) {//符号是*，即kleene闭包
            RegexTreeNode child = (RegexTreeNode) node.getFirstChild();

            TNFA childNFA = translate(child);

            tnfa.getAlphabet().addAll(childNFA.getAlphabet());//合并所有的点
            tnfa.getTransitTable().merge(childNFA.getTransitTable());//拿到字母表和转化表

            childNFA.getStartState().setType(State.MIDDLE);
            childNFA.getAcceptingState().setType(State.MIDDLE);

            tnfa.getTransitTable().addEdge(beginState,childNFA.getStartState(),'ε');
            tnfa.getTransitTable().addEdge(childNFA.getAcceptingState(),endState,'ε');
            tnfa.getTransitTable().addEdge(beginState,endState,'ε');
            tnfa.getTransitTable().addEdge(childNFA.getAcceptingState(),childNFA.getStartState(),'ε');

        }
//        else{
//            System.out.println("符号有问题喵");
//        }

        for(State perState : tnfa.getTransitTable().vertexSet()){
            if(!perState.equals(beginState) && !perState.equals(endState)){
                perState.setType(State.MIDDLE);
            }
        }

        for(LabelEdge perEdge : tnfa.getTransitTable().edgeSet()){
            State source = (State) perEdge.getSource();
            State target = (State) perEdge.getTarget();

            if(!source.equals(beginState) && !source.equals(endState)){
                source.setType(State.MIDDLE);
                //System.out.println("source:" + source.getId());
            }
            if(!target.equals(beginState) && !target.equals(endState)){
                target.setType(State.MIDDLE);
                //System.out.println("target:" + target.getId());
            }
        }

        return tnfa;
    }



}
