package org.qogir.compiler.util.graph;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;

public class BreadthFirstIterator<V> implements Iterator<V> {

    private ArrayDeque<V> queue = new ArrayDeque<>(); //用来干嘛的？？？？？？？？？

    private LabeledDirectedGraph<V> graph;
    private V startVertex; //起点

    public BreadthFirstIterator(LabeledDirectedGraph<V> g, V startVertex){
        this.graph = g;
        this.startVertex = startVertex;
        breadthFirstTraverse();
    }

    private void breadthFirstTraverse(){
        if(this.startVertex == null)
            return;

        HashMap<V,Integer> visited = new HashMap<>();

        for(V v : graph.vertexSet()){ //添加点集合到visited里面（所有的点）
            visited.put(v,0);
        }
        ArrayDeque<V> traverseQueue = new ArrayDeque<>();

        traverseQueue.add(this.startVertex);//开始点先进
        V temp;

        while(!traverseQueue.isEmpty()){//用队列的话，应该是用BFS
            temp = traverseQueue.poll();//poll：返回的同时出队
            if(visited.get(temp) == 0){//为0代表没有访问过
                visited.put(temp,1);//标记为访问过
                queue.add(temp);//入队

                for(LabelEdge le : graph.edgeSet()){//遍历边集合，找每条边的起点
                    if(le.getSource().equals(temp)){
                        if(visited.get(le.getTarget()) == 0){//终点未访问过
                            traverseQueue.add((V)le.getTarget());//目标点入队列
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        if(queue.isEmpty())
            return false;
        return true;
    }

    @Override
    public V next() {
        if(!hasNext())
            return null;
        return queue.poll();
    }
}
