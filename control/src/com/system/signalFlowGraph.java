package com.system;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.*;



import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class signalFlowGraph {

    private JPanel panel1;
    private JButton insertNodeButton;
    private JButton calculateTransferFunctionButton;
    private JButton insertBranchButton;
    private JButton enterNumberOfNodesButton;
    public String sourcevertex="";
    public String targetvertex="";
    public int n;
    Graph<String, DefaultWeightedEdge> g=new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

    double transferFunction(){


        AllDirectedPaths<String,DefaultWeightedEdge> d=new AllDirectedPaths<>(g);
        List<GraphPath<String,DefaultWeightedEdge>> allpaths=new LinkedList<>();
        allpaths=d.getAllPaths(sourcevertex,targetvertex,true,100);

        SzwarcfiterLauerSimpleCycles<String,DefaultWeightedEdge> a=new SzwarcfiterLauerSimpleCycles<>(g);
        List<List<String>> l=a.findSimpleCycles();

        double gain;
        List<DefaultWeightedEdge> pathsedges=new LinkedList<>();
        double[] pathsgains=new double[allpaths.size()];
        for(int i=0;i<allpaths.size();i++){
            gain=1;
            pathsedges=allpaths.get(i).getEdgeList();
            for(int j=0;j<allpaths.get(i).getLength();j++)
                gain=gain*(g.getEdgeWeight(pathsedges.get(j)));
            pathsgains[i]=gain;
        }


        double[] loopsgains=new double[l.size()];
        for(int i=0;i<l.size();i++){
            gain=1;
            for(int j=0;j<l.get(i).size();j++){
                if(j<l.get(i).size()-1)
                    gain=gain*(g.getEdgeWeight(g.getEdge(l.get(i).get(j),l.get(i).get(j+1))));
                else
                    gain=gain*(g.getEdgeWeight(g.getEdge(l.get(i).get(j),l.get(i).get(0))));
            }
            loopsgains[i]=gain;
        }

        List<String> vertices=new LinkedList<>();
        List<List<List<String>>> loopsoffpaths=new LinkedList<>();
        List<List<String>>[] loops=new LinkedList[l.size()];
        int flag;
        for(int i=0;i<allpaths.size();i++) {
            vertices=allpaths.get(i).getVertexList();
            loops[i]=new LinkedList<>();
            for (int j = 0; j < l.size(); j++) {
                flag=0;
                for(int t=0;t<vertices.size();t++)
                    if(l.get(j).contains(vertices.get(t))){
                        flag=1;
                        break;}
                if(flag==0)
                    loops[i].add(l.get(j));
            }
            loopsoffpaths.add(loops[i]);
        }

        double sop=0;


        for(int i=0;i<l.size()-1;i++){
            for(int t=i+1;t<l.size();t++){

                flag=0;
                for(int j=0;j<l.get(i).size();j++){
                    if(l.get(t).contains(l.get(i).get(j))) {
                        flag=1;
                        break;
                    }
                }
                if(flag==0){
                    sop=sop+loopsgains[i]*loopsgains[t];
                }

            }

        }

        List<Double> soppaths= new LinkedList<>();
        for(int i=0;i<loopsoffpaths.size();i++){
            soppaths.add(0.0);
            for(int j=0;j<loopsoffpaths.get(i).size()-1;j++){
                flag=0;
                for(int t=0;t<loopsoffpaths.get(i).get(j).size();t++){
                    if(loopsoffpaths.get(i).get(j+1).contains(loopsoffpaths.get(i).get(j).get(t))){
                        flag=1;
                        break;
                    }
                }
                if(flag==0){
                    soppaths.set(i,soppaths.get(i)+loopsgains[l.indexOf(loopsoffpaths.get(i).get(j))]*loopsgains[l.indexOf(loopsoffpaths.get(i).get(j+1))]);
                }
            }
        }



        double sum=0;
        for(int i=0;i<loopsgains.length;i++){
            sum=sum+loopsgains[i];
        }

        double delta=1-sum+sop;

        List<Double> sumloopsoffpaths = new LinkedList<>();
        for(int i=0;i<loopsoffpaths.size();i++){
            sumloopsoffpaths.add(0.0);
            for(int j=0;j<loopsoffpaths.get(i).size();j++){
                sumloopsoffpaths.set(i,sumloopsoffpaths.get(i)+loopsgains[l.indexOf(loopsoffpaths.get(i).get(j))]);
            }
        }


        sum=0;
        for(int i=0;i<pathsgains.length;i++){
            sum=sum+pathsgains[i]*(1-sumloopsoffpaths.get(i)+soppaths.get(i));
        }

        double tf=sum/delta;

        return tf ;
    }



    public signalFlowGraph() {
        enterNumberOfNodesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                n= Integer.parseInt (JOptionPane.showInputDialog("number of nodes ="));
            }
        });
        insertNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                    for(int i=0;i<n-1;i++) {
                        if(i==0){sourcevertex=JOptionPane.showInputDialog("enter source node =");
                            g.addVertex(sourcevertex);
                        }
                        else
                        g.addVertex(JOptionPane.showInputDialog("enter node name ="));
                    }
                    targetvertex= JOptionPane.showInputDialog("end node =");
                    g.addVertex(targetvertex);
            }
        });
        insertBranchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                   g.setEdgeWeight(g.addEdge(JOptionPane.showInputDialog("from node ="),JOptionPane.showInputDialog("to node =")),Double.parseDouble(JOptionPane.showInputDialog("branch weight =")));
            }
        });
        calculateTransferFunctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null,transferFunction());
            }
        });
    }



    public static void main(String[] args) {
        JFrame frame=new JFrame("signalFlowGraph");
        frame.setContentPane(new signalFlowGraph().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
