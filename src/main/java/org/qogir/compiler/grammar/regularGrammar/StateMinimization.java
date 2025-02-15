package org.qogir.compiler.grammar.regularGrammar;


import org.qogir.compiler.FA.State;
import org.qogir.compiler.util.graph.LabelEdge;
import org.qogir.compiler.util.graph.LabeledDirectedGraph;

import java.sql.SQLOutput;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class StateMinimization {

    /**
     * Distinguish non-equivalent states in the given DFA.
     * @param dfa the original dfa.
     * @return distinguished equivalent state groups
     */
    private HashMap<Integer,HashMap<Integer, State>> distinguishEquivalentState(RDFA dfa){
        //Add your implementation
        //传入参数是dfa的话证明整个等价关系的判断要在一次之内完成

        HashMap<Integer,HashMap<Integer, State>> groupSet = new HashMap<>(); // group set
        HashMap<Integer,HashMap<Integer, State>> lastGroupSet;

        //第一步：遍历点集合，根据状态类型分成两个集合
        HashMap<Integer,State> acceptState = new HashMap<>();
        HashMap<Integer,State> otherState = new HashMap<>();

        for(State s : dfa.getTransitTable().vertexSet()){
            if(s.getType()==State.ACCEPT){
                acceptState.put(s.getId(),s);
            }
            else{
                otherState.put(s.getId(),s);
            }
        }

        groupSet.put(0, otherState);
        groupSet.put(1, acceptState);//初始状态：1代表的是结束，0代表非结束
        System.out.println(GroupSetToString(groupSet) + '\t' + ":initial split");

        for(char ch : dfa.getAlphabet()){
            lastGroupSet = groupSet;
            groupSet = separate(ch, lastGroupSet, dfa);//迭代更新状态集合

            System.out.println(GroupSetToString(groupSet) + '\t' + ":end of_" + ch);
        }

        return groupSet;
    }

    /*
    这个函数的目标是对于一个给定的集合划分和一个符号，遍历每个集合，并尝试找出新的划分
     */
    private HashMap<Integer,HashMap<Integer, State>> separate(char ch, HashMap<Integer,HashMap<Integer, State>> lastGroupSet, RDFA rdfa){
        State.STATE_ID = 0;//重置状态表
        int setID = 0;//用于标识每个新集合的状态

        HashMap<Integer,HashMap<Integer, State>> finalGroupSet = new HashMap<>();
        HashMap<Integer,HashMap<Integer, State>> thisGroupSet = lastGroupSet;

        ArrayDeque<HashMap<Integer,State>> queue = new ArrayDeque<>();
        for(HashMap<Integer,State> set : lastGroupSet.values()){
            queue.addLast(set);
        }

        while(!queue.isEmpty()){
            HashMap<Integer,State> thisSet = queue.pollFirst();//获取第一个
            //System.out.println(thisSet);

            //对于当前所获取的集合，按照他们的指向划分新的集合
            HashMap<Integer,HashMap<Integer, State>> map = new HashMap<>();
            for(State s : thisSet.values()){//对于当前集合中的所有状态
                boolean hasLabel = false;

                for(LabelEdge edge : rdfa.getTransitTable().edgeSet()){//对于所有的边
                    //如果某条边的开始是他且以对应的字符转化
                    if(s.equals((State) edge.getSource())&&edge.getLabel()==ch) {
                        State target = (State) edge.getTarget();
                        for(Integer setNum : thisGroupSet.keySet()){
                            //找到这个目标状态当前在哪个集合
                            if(thisGroupSet.get(setNum).containsValue(target)){
                                if(!map.containsKey(setNum)){
                                    map.put(setNum,new HashMap<>());
                                }
                                map.get(setNum).put(s.getId(),s);
                                //在字符为ch的条件下，s转化到组号为setNum的组里面
                            }
                        }

                        hasLabel = true;
                    }
                }

                if(!hasLabel){
                    if(!map.containsKey(-1)){
                        map.put(-1,new HashMap<>());
                    }
                    map.get(-1).put(s.getId(),s);
                }
            }

            if(map.size()==1){
                finalGroupSet.put(State.STATE_ID++, thisSet);
            }
            else{
                for(Integer i : thisGroupSet.keySet()){
                    if(thisGroupSet.get(i).equals(thisSet)){
                        thisGroupSet.remove(i);
                        break;
                    }
                }//把旧的集合从列表中去掉

                for(HashMap<Integer,State> hashMap : map.values()){
                    thisGroupSet.put(setID++, hashMap);
                    queue.addLast(hashMap);
                }//把所有新的放到列表和队列里面
            }
        }

        return finalGroupSet;
    }


    public RDFA minimize(RDFA dfa){

        //Add your implementation
        //这里依照上面判断等价之后返回的集合直接构建新的

        HashMap<Integer,HashMap<Integer,State>> groupSet = distinguishEquivalentState(dfa);

        int stateNum = State.STATE_ID;

        //下一步是创建新的dfa,谁是开始状态？
        RDFA minimizeDFA = new RDFA();
        minimizeDFA.getAlphabet().addAll(dfa.getAlphabet());//合并字母表

        State.STATE_ID = 0;

        for(int i=0;i<stateNum;i++){
            //System.out.println(i);
            State newState = new State();
            if(groupSet.get(i).containsValue(dfa.getStartState())){//如果这个集合里面包含了原dfa的开始状态
                newState.setType(dfa.getStartState().getType());//获取对应的开始状态的类型
                minimizeDFA.setStartState(newState);
            }

            //由于已经对所有的状态分好了组，在其中所有的状态以相同符号转化到的状态必定在同一个集合内，所以只要任取一个状态
            State typeState = (State) groupSet.get(i).values().toArray()[0];//在这个集合里面取出一个状态
            if(typeState.getType() == State.ACCEPT){//如果是接受状态，代表对应的集合也要设为接受状态
                newState.setType(State.ACCEPT);//设为接受状态
            }

            minimizeDFA.getTransitTable().addVertex(newState);//合并点集合
            minimizeDFA.setStateMappingBetweenDFAAndNFA(newState,groupSet.get(i));//现在最小DFA里面的集合对应的是原DFA里面的状态
        }//至此完成所有状态的创建

        for(int i=0;i<stateNum;i++){
            State minBeginState = null;
            for(State s : minimizeDFA.getStateMappingBetweenDFAAndNFA().keySet()){
                if(s.getId()==i){
                    minBeginState = s;
                    break;
                }
            }//找到这个编号对应的状态
            if(minBeginState == null){
                System.out.println("状态信息错误！");
                break;
            }

            State sampleState = (State) groupSet.get(i).values().toArray()[0];//在这个集合里面取出一个状态

            for(LabelEdge edge : dfa.getTransitTable().edgeSet()){//开始遍历原集合的每条边
                State source = (State) edge.getSource();//原dfa的起点
                if(source.equals(sampleState)){//找到这个状态在原集合里面作为起点的边
                    State target = (State) edge.getTarget();//原dfa的终点
                    for(State s : minimizeDFA.getStateMappingBetweenDFAAndNFA().keySet()){
                        if(minimizeDFA.getStateMappingBetweenDFAAndNFA().get(s).containsValue(target)){//终点
                            //s是现dfa集合的终点
                            minimizeDFA.getTransitTable().addEdge(minBeginState,s,edge.getLabel());//把边连起来
                            break;
                        }
                    }
                }
            }
        }


        return minimizeDFA;
    }

//    /**
//     * Used for showing the distinguishing process of state miminization algorithm
//     * @param stepQueue holds all distinguishing steps
//     * @param GroupSet is the set of equivalent state groups
//     * @param memo  remarks
//     */
//    private void recordDistinguishSteps(ArrayDeque<String> stepQueue, HashMap<Integer,HashMap<Integer, State>> GroupSet, String memo){
//        String str = "";
//        str = GroupSetToString(GroupSet);
//        str += ":" + memo;
//        stepQueue.add(str);
//    }
//
//    /**
//     * Display the equivalent state groups
//     * @param stepQueue
//     */
//    private void showDistinguishSteps(ArrayDeque<String> stepQueue){
//        int step = 0;
//        String str = "";
//        while(!stepQueue.isEmpty()){
//            str = stepQueue.poll();
//            System.out.println("Step" + step++ + ":\t" + str +"\r");
//        }
//    }

    private String GroupSetToString(HashMap<Integer,HashMap<Integer, State>> GroupSet){//输出合并后的集合的函数
        String str = "";
        for( Integer g: GroupSet.keySet()){
            String tmp = GroupToString(GroupSet.get(g));
            str += g +  ":" + tmp + "\t" ;
        }
        return str;
    }

    private String GroupToString(HashMap<Integer, State> group){
        String str = "";
        for(Integer k : group.keySet()){
            str += group.get(k).getId() + ":" + group.get(k).getType() + ",";
        }
        if(str.length()!=0) str = str.substring(0,str.length()-1);
        str = "{" + str + "}";
        return str;
    }
}
