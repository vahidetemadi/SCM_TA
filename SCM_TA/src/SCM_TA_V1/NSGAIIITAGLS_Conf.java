package SCM_TA_V1;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
	public class NSGAIIITAGLS_Conf extends AbstractProblem{
		static Bug[] bugs=GA_Problem_Parameter.bugs;
		HashMap<Integer,Developer> developers=GA_Problem_Parameter.developers;
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP;
		TopologicalOrderIterator<Bug,DefaultEdge> tso;
		ArrayList<Zone> genes=new ArrayList<Zone>();
		ArrayList<ArrayList<Integer>> schedules=new ArrayList<ArrayList<Integer>>();
		HashMap<Integer,Bug> varToBug=new HashMap<Integer, Bug>(); //supposed to be used for mapping the item in chromosome to a particular bug
		/*ArrayList<ArrayList<ArrayList<Integer>>> variables=new ArrayList<ArrayList<ArrayList<Integer>>>();
		//ArrayList<ArrayList<Integer>> variable=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> encodedSolution= new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> encodedSolutions= new ArrayList<ArrayList<Integer>>();*/
		ArrayList<Integer> assignment=new ArrayList<Integer>();
		TopologicalOrderIterator<Zone,DefaultEdge> tso_zones;
		AllDirectedPaths<Bug, DefaultEdge> paths;
		DefaultDirectedGraph<Bug, DefaultEdge> DEP_scheduling;
		ArrayList<Triplet<Bug, Zone, Integer>> zoneAssignee=new ArrayList<Triplet<Bug,Zone,Integer>>();
	public NSGAIIITAGLS_Conf(){
		super(GA_Problem_Parameter.setNum_of_Variables(GA_Problem_Parameter.bugs),GA_Problem_Parameter.Num_of_functions_Multi);
	}
	
	
	public ArrayList<ArrayList<Integer>> getSchedules(Solution solution){
		int indexTotal=0;
			//clear the list holds mapping solution element index to bugs
			varToBug.clear();
			assignment.clear();
			schedules.clear();
			for(Integer devID:EncodingUtils.getInt(solution)){
				assignment.add(devID);
			}
			
			int index=0;
			for(Bug b:GA_Problem_Parameter.tasks){
				b.setZoneDEP();
				varToBug.put(index, b);
				index++;
			}
			
			for(int i=0;i<GA_Problem_Parameter.numOfEvaluationLocalSearch;i++)
				schedules.add(generateSchedule_newDesign());
			
			//schedules.add(null);
			
		return schedules;
	}
		
	

	public void init(){
		bugs=GA_Problem_Parameter.bugs;
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_ID;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
			}
		}
	}
	
	public void preInit(){
		bugs=GA_Problem_Parameter.bugs;
		DEP=GA_Problem_Parameter.DEP;
		tso=GA_Problem_Parameter.tso_competenceMulti2;
		/*
		//generate DAG for arrival Bugs
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
		//topologically sort the graph
		tso=GA_Problem_Parameter.getTopologicalSorted(DEP);*/
		while(tso.hasNext()){
			Bug b=tso.next();
			b.setZoneDEP();
			TopologicalOrderIterator<Zone,DefaultEdge> tso_zones=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
			while(tso_zones.hasNext()){
				genes.add(tso_zones.next());
			}
		}
	}
	
	
	@Override
	public Solution newSolution(){
		if(GA_Problem_Parameter.flag==1){
			preInit();
			GA_Problem_Parameter.flag=0;
		}
		Solution solution=new Solution(genes.size(),GA_Problem_Parameter.Num_of_functions_Multi);
		int min=GA_Problem_Parameter.getMinIdofDeveloper();
		int max=GA_Problem_Parameter.getMaxIdofDeveloper();
		int j=0;
		for(Zone z:genes){
			//RealVariable r=new RealVariable(GA_Problem_Parameter.getMinIdofDeveloper(), GA_Problem_Parameter.getMaxIdofDeveloper());
			//r.randomize();
			solution.setVariable(j,EncodingUtils.newInt(min, max));
			j++;
		}
		return solution;
	}
		
	
	@Override 	
	public void evaluate(Solution solution){
		long st3=System.nanoTime();
		HashMap<Integer, int[]> listOfBugAssignee=new HashMap<Integer, int[]>();
		@SuppressWarnings("unchecked")
		TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone = null;
		//copy the TDG graph into another object
		DirectedAcyclicGraph<Bug, DefaultEdge>  DEP_evaluation=(DirectedAcyclicGraph<Bug, DefaultEdge>) DEP.clone();
		TopologicalOrderIterator<Bug, DefaultEdge> tso_sch_evaluate;
		ArrayList<Integer> schedule=new ArrayList<Integer>();
		
		for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++)
			schedule.add(0);
		//reset all the associate time for the bugs and their zones
		GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
		//assign Devs to zone
		zoneAssignee.clear();
		GA_Problem_Parameter.assignZoneDev(zoneAssignee,GA_Problem_Parameter.tasks, solution);
		//if((GA_Problem_Parameter.algorithm.getNumberOfEvaluations()/500)%100==0)
		double[] forward=new double[2];
		double[] backward=new double[2];
		String[] twoSidedLabel={"forward","backward"};
		String selected="";
		double[] selectedDirection=new double[2];
		int test=0;
		if(true)
		{
			AllDirectedPaths<Bug, DefaultEdge> allPaths=new AllDirectedPaths<Bug, DefaultEdge>(DEP_evaluation);
			TopologicalOrderIterator<Bug, DefaultEdge> tso_sortedBugList=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);
			ArrayList<Bug> sortedBugList=new ArrayList<Bug>();
			ArrayList<Bug> sortedBugListToBeUpdated=new ArrayList<Bug>();
			int c=0;
			int[] Devs;
			int numOfDevs=GA_Problem_Parameter.numOfDevs;
			while(tso_sortedBugList.hasNext()){
				sortedBugList.add(tso_sortedBugList.next());
				sortedBugListToBeUpdated.add(sortedBugList.get(c));
				int c2=0;
				int[] tempListToKeep=new int[sortedBugList.get(c).Zone_DEP.vertexSet().size()];
				int i=0;
				for(Triplet<Bug, Zone, Integer> item:zoneAssignee){
					if(item.getFirst().ID==sortedBugList.get(c).ID){
						tempListToKeep[i]=item.getThird();
						i++;
					}
				}
				listOfBugAssignee.put(sortedBugList.get(c).ID,tempListToKeep );
				c++;
				
			}
			//Collections.shuffle(sortedBugList);
			boolean isParallel=false;
			int numOfDevsInCommon=0;
			int counter=0;
			int sizeOfSortedBugList=sortedBugList.size();
			int firstBugID=0;
			Random r=new Random();
			for(int i=0; i<sizeOfSortedBugList-1;i++){
				firstBugID=sortedBugList.get(i).ID;
				for(int j=i+1;j<sizeOfSortedBugList;j++){
					if(r.nextDouble()>0.0){
						numOfDevsInCommon=getNumOfDeveloperInCommon_enhanced(listOfBugAssignee.get(firstBugID),listOfBugAssignee.get(sortedBugList.get(j).ID), numOfDevs);
						/*int[] devs=new int[numOfDevs];
						int[] b1=listOfBugAssignee.get(firstBugID);
						int[] b2=listOfBugAssignee.get(sortedBugList.get(j).ID);
						for(int k=0;k<b1.length;k++)
							devs[b1[k]-1]++;
						
						for(int m=0;m<b2.length;m++)
							if(devs[b2[m]-1]>0){
								numOfDevsInCommon++;
								devs[b2[m]-1]--;
							}*/
						
						if(numOfDevsInCommon>1){
							isParallel=allPaths.getAllPaths(sortedBugList.get(i), sortedBugList.get(j), true, 100000000).isEmpty();
							isParallel= isParallel & allPaths.getAllPaths(sortedBugList.get(j), sortedBugList.get(i), true, 100000000).isEmpty();
							if(isParallel){
								test++;
								for(String edgeLabel:twoSidedLabel){
									if(edgeLabel.equals("forward"))
										DEP_evaluation.addEdge(sortedBugList.get(i), sortedBugList.get(j));
									else{
										DEP_evaluation.removeEdge(sortedBugList.get(i), sortedBugList.get(j));
										DEP_evaluation.addEdge(sortedBugList.get(j), sortedBugList.get(i));
									}
									GA_Problem_Parameter.resetParameters(DEP_evaluation,solution, developers);
									tso_sch_evaluate=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);
								
									double totalTime=0.0;
									double totalCost=0.0;
									double totalDevCost=0.0;
									double totalDelayTime=0.0;
									double totalDelayCost=0.0;
									double totalStartTime=0.0;
									double totalEndTime=0.0;
									double totalExecutionTime=0.0;
									int index=0;
									int test2=0;
									while(tso_sch_evaluate.hasNext()){
										Bug b=tso_sch_evaluate.next();
										//set Bug startTime
										b.startTime_evaluate=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
										GA_Problem_Parameter.resetParameters_ZoneAndDevelopers(b,solution,developers);
										for(Zone zone:b.sortedZoneList){
											test2++;
											double compeletionTime=0.0;
											Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
											compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(EncodingUtils.getInt(solution.getVariable(index))));
											totalExecutionTime+=compeletionTime;
											//need to be changed????///
											totalDevCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(index))).hourlyWage;
											zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(EncodingUtils.getInt(solution.getVariable(index))), zone.DZ);
											zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
											/*developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=Math.max(developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour,
											zone.zoneStartTime_evaluate)+compeletionTime;*/
											developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=zone.zoneEndTime_evaluate;
											b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
											index++;
										}
										totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
										totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
										//pay for those 
										totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
										if(totalDelayTime>0)
											totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
									}
									
									totalTime=totalEndTime-totalStartTime;
									totalCost=totalDevCost+totalDelayCost;
									if(edgeLabel.equals("forward")){
										forward[0]=totalTime;
										forward[1]=totalCost;
									}
									else{
										backward[0]=totalTime;
										backward[1]=totalCost;
									}
								}
								DEP_evaluation.removeEdge(sortedBugList.get(j), sortedBugList.get(i));
								
								if(compare(forward, backward)==1){
									selected="forward";
									selectedDirection=forward;
								}
								else{
									selected="backward";
									selectedDirection=backward;
								}
									
								if(counter==0){
									solution.setObjective(0, selectedDirection[0]);
									solution.setObjective(1, selectedDirection[1]);
									if(selected.equals("forward")){
										schedule.set(GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(sortedBugList.get(i), 
											sortedBugList.get(j))), 1);
										DEP_evaluation.addEdge(sortedBugList.get(i), sortedBugList.get(j));
									}
									else{
										schedule.set(GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(sortedBugList.get(j), 
												sortedBugList.get(i))), 1);
											DEP_evaluation.addEdge(sortedBugList.get(j), sortedBugList.get(i));
									}
									counter++;
								}
								else if(compare(selectedDirection[0], selectedDirection[1], solution)==1){
									solution.setObjective(0, selectedDirection[0]);
									solution.setObjective(1, selectedDirection[1]);
									if(selected.equals("forward")){
										schedule.set(GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(sortedBugList.get(i), 
											sortedBugList.get(j))), 1);
										DEP_evaluation.addEdge(sortedBugList.get(i), sortedBugList.get(j));
									}
									else{
										schedule.set(GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(sortedBugList.get(j), 
												sortedBugList.get(i))), 1);
											DEP_evaluation.addEdge(sortedBugList.get(j), sortedBugList.get(i));
									}
								}
							}
						}
					}
				}
			}
			solution.setSchedule(schedule);
			//System.out.println("total time:"+ sT2);
			//System.out.println();
		}
		else{
			tso_sch_evaluate=new TopologicalOrderIterator<Bug, DefaultEdge>(DEP_evaluation);				
			double totalTime=0.0;
			double totalCost=0.0;
			double totalDevCost=0.0;
			double totalDelayTime=0.0;
			double totalDelayCost=0.0;
			double totalStartTime=0.0;
			double totalEndTime=0.0;
			double totalExecutionTime=0.0;
			int index=0;
			while(tso_sch_evaluate.hasNext()){
				Bug b=tso_sch_evaluate.next();
				//set Bug startTime
				b.startTime_evaluate=fitnessCalc.getMaxEndTimes(b, DEP_evaluation);
				GA_Problem_Parameter.resetParameters_ZoneAndDevelopers(b,solution,developers);
				//TopologicalOrderIterator<Zone, DefaultEdge> tso_Zone=new TopologicalOrderIterator<Zone, DefaultEdge>(b.Zone_DEP);
				//iterate by the order provided in y graph
				while(tso_Zone.hasNext()){
					Zone zone=tso_Zone.next();
					double compeletionTime=0.0;
					Entry<Zone, Double> zone_bug=new AbstractMap.SimpleEntry<Zone, Double>(zone,b.BZone_Coefficient.get(zone));
					compeletionTime=fitnessCalc.compeletionTime(b,zone_bug, developers.get(EncodingUtils.getInt(solution.getVariable(index))));
					totalExecutionTime+=compeletionTime;
					//need to be changed????///
					totalDevCost+=compeletionTime*developers.get(EncodingUtils.getInt(solution.getVariable(index))).hourlyWage;
					zone.zoneStartTime_evaluate=b.startTime_evaluate+fitnessCalc.getZoneStartTime(developers.get(EncodingUtils.getInt(solution.getVariable(index))), zone.DZ);
					zone.zoneEndTime_evaluate=zone.zoneStartTime_evaluate+compeletionTime;
					/*developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=Math.max(developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour,
					zone.zoneStartTime_evaluate)+compeletionTime;*/
					developers.get(EncodingUtils.getInt(solution.getVariable(index))).developerNextAvailableHour=zone.zoneEndTime_evaluate;
					b.endTime_evaluate=Math.max(b.endTime_evaluate, zone.zoneEndTime_evaluate);
					index++;
				}
				totalStartTime=Math.min(totalStartTime, b.startTime_evaluate);
				totalEndTime=Math.max(totalEndTime, b.endTime_evaluate);
				//pay for those 
				totalDelayTime+=b.endTime_evaluate-(2.5*totalExecutionTime+totalExecutionTime);
				if(totalDelayTime>0)
					totalDelayCost+=totalDelayTime*GA_Problem_Parameter.priorities.get(b.priority);
			}
			totalTime=totalEndTime-totalStartTime;
			totalCost=totalDevCost+totalDelayCost;
			solution.setObjective(0, totalTime);
			solution.setObjective(1, totalCost);
		}
		long st4=System.nanoTime()-st3;
		//System.out.println(st4);
		if(GA_Problem_Parameter.algorithm.getNumberOfEvaluations()%50000==0)
			System.out.println(GA_Problem_Parameter.algorithm.getNumberOfEvaluations());
			
	}
		
	
	public ArrayList<Integer> generateSchedule(){
		//schedule has the same size of pEdges (potential edges)
		ArrayList<Integer> schedule=new ArrayList<Integer>();
		
		
		//initialization
		for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++)
			schedule.add(0);
		
		/* generate valid schedules*/
		//m: startIndex, n:endIndex, 
		int m,n,p,q;
		int[] indexes=new int[2];
		//create DEP_scheduling graph to add potential links used for a new schedule 
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) GA_Problem_Parameter.DEP.clone();
		//DEP_scheduling=GA_Problem_Parameter.convertToDirectedGraph(GA_Problem_Parameter.DEP, DEP_scheduling);
		ArrayList<Integer> indices=new ArrayList<Integer>();
		
		//shuffling the bug list to provide randomness in the scheduling
		GA_Problem_Parameter.shuffledTasks=(ArrayList<Bug>) GA_Problem_Parameter.tasks.clone();
		Collections.shuffle(GA_Problem_Parameter.shuffledTasks);
		int vertexSetSize=GA_Problem_Parameter.tasks.size()-1;
		vertexSetSize/=20;
		/*if(vertexSetSize>40)
			vertexSetSize/=3;*/
		for(int i=0;i<vertexSetSize;i++){
			indexes=getIndex(i);
			m=indexes[0];
			n=indexes[1];
			for(int j=i+1;j<vertexSetSize+1;j++){
				indexes=getIndex(j);
				p=indexes[0];
				q=indexes[1];
				if(compareSubtasksAssignee(m,n,p,q,assignment)){
					paths=new AllDirectedPaths<Bug, DefaultEdge>(DEP_scheduling);
					try{
						if(paths.getAllPaths(varToBug.get(i),varToBug.get(j), true, 10000).isEmpty() && paths.getAllPaths(varToBug.get(j), varToBug.get(i), true, 10000).isEmpty()){
							int t=-1;
							//get the information for potential link-- index t determines if the edge already exists there
							//DDG_1 is the fully connected graph of all bugs 
							t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(i), varToBug.get(j)));
							if(t<0){
								t=GA_Problem_Parameter.pEdges.indexOf(GA_Problem_Parameter.DDG_1.getEdge(varToBug.get(j), varToBug.get(i)));
							}
							// in case a link exits we add that link to DEP_scheduling (the graph of a specific schedule)
							DEP_scheduling.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
									GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
							//check if added link does not result in a cycle otherwise the added link gets removed
							boolean b=new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles();
							if(!b)
								schedule.set(t, 1);
							else
								DEP_scheduling.removeEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.pEdges.get(t)),
										GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.pEdges.get(t)));
						}
					}
					catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}
		DEP_scheduling=null;
		paths=null;
		return schedule;
	}

	
	
	/*
	 * generate the schedule- new design
	 */
	public ArrayList<Integer> generateSchedule_newDesign(){
		ArrayList<Integer> schedule=new ArrayList<Integer>();
		
		for(int i=0;i<GA_Problem_Parameter.pEdges.size();i++)
			schedule.add(0);
		//create DEP_scheduling graph to add potential links used for a new schedule 
		DirectedAcyclicGraph<Bug, DefaultEdge> DEP_scheduling=(DirectedAcyclicGraph<Bug, DefaultEdge>) GA_Problem_Parameter.DEP.clone();
		//shuffling the bug list to provide randomness in the scheduling
		GA_Problem_Parameter.shuffledTasks=(ArrayList<Bug>) GA_Problem_Parameter.tasks.clone();
		Collections.shuffle(GA_Problem_Parameter.shuffledTasks);
		GA_Problem_Parameter.shuffledPEdges=(ArrayList<DefaultEdge>) GA_Problem_Parameter.pEdges.clone();
		Collections.shuffle(GA_Problem_Parameter.shuffledPEdges);
		int numOfBugs=GA_Problem_Parameter.DEP.vertexSet().size();
		int numOfIteraration=GA_Problem_Parameter.shuffledPEdges.size()/numOfBugs;
		int i=0;
		while(numOfIteraration>0 && i<numOfIteraration){
			if(checkHavingDeveloperInCommon(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.shuffledPEdges.get(i)),
					GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.shuffledPEdges.get(i)))){
				try{
					schedule.set(GA_Problem_Parameter.pEdges.lastIndexOf(GA_Problem_Parameter.shuffledPEdges.get(i)), 1);
					int pIndex=GA_Problem_Parameter.shuffledPEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.shuffledPEdges.get(i)),
							GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.shuffledPEdges.get(i))));
					GA_Problem_Parameter.shuffledPEdges.remove(pIndex);
					DEP_scheduling.addEdge(GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.shuffledPEdges.get(i)),
					GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.shuffledPEdges.get(i)));
				}
				catch(Exception e){
					schedule.set(GA_Problem_Parameter.pEdges.lastIndexOf(GA_Problem_Parameter.shuffledPEdges.get(i)), 0);
					//e.printStackTrace();
				}
			}
			else{
				int pIndex=GA_Problem_Parameter.shuffledPEdges.indexOf(GA_Problem_Parameter.DDG.getEdge(GA_Problem_Parameter.DDG.getEdgeTarget(GA_Problem_Parameter.shuffledPEdges.get(i)),
						GA_Problem_Parameter.DDG.getEdgeSource(GA_Problem_Parameter.shuffledPEdges.get(i))));
				GA_Problem_Parameter.shuffledPEdges.remove(pIndex);
			}
			i++;
		}
		/*System.out.println(new CycleDetector<Bug, DefaultEdge>(DEP_scheduling).detectCycles());
		System.out.println(DEP_scheduling.edgeSet().size());
		
		graphVisulaization gV=new graphVisulaization();
		gV.init(DEP_scheduling);
		
		JFrame frame = new JFrame();
        frame.getContentPane().add(gV);
        frame.setTitle("JGraphT Adapter to JGraphX Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);*/
		
		return schedule;
	}
	
	
	//get the start and end indices of each bug in the choromosome
	public int[] getIndex(int index){
		int[] indexes=new int[2];
		int sIndex=0;
		for(int i=0;i<index;i++){
			sIndex+=GA_Problem_Parameter.tasks.get(i).BZone_Coefficient.size();
		}
		indexes[0]=sIndex;
		indexes[1]=sIndex+GA_Problem_Parameter.tasks.get(index).BZone_Coefficient.size();
		return indexes;
	}
	
	public static Boolean compareSubtasksAssignee(int i, int j,int p, int k, ArrayList<Integer> assignment){
		Boolean b=false;
		outerloop:
		for(int r=i;r<j;r++){
			for(int s=p;s<k;s++)
				if(assignment.get(r)==assignment.get(s)){
					b=true;
					break outerloop;
				}
		}
		
		return b;
	}
	
	public void generateDAG(){
		DEP=GA_Problem_Parameter.getDAGModel(bugs);
	}

	public Boolean checkHavingDeveloperInCommon(Bug b1, Bug b2){
		Collection<Integer> assigneeForB1=new ArrayList<Integer>();
		Collection<Integer> assigneeForB2=new ArrayList<Integer>();
		
		for(Triplet<Bug, Zone, Integer> item:zoneAssignee){
			if(item.getFirst().ID==b1.ID)
				assigneeForB1.add(item.getThird());
			else if(item.getFirst().ID==b2.ID)
				assigneeForB2.add(item.getThird());
		}
		assigneeForB1.retainAll(assigneeForB2);
		
		return assigneeForB1.size()>0?true:false;
	}
	
	public int getNumOfDeveloperInCommon(Bug b1, Bug b2){
		Collection<Integer> assigneeForB1=new ArrayList<Integer>();
		Collection<Integer> assigneeForB2=new ArrayList<Integer>();
		
		for(Triplet<Bug, Zone, Integer> item:zoneAssignee){
			if(item.getFirst().ID==b1.ID)
				assigneeForB1.add(item.getThird());
			else if(item.getFirst().ID==b2.ID)
				assigneeForB2.add(item.getThird());
		}
		assigneeForB1.retainAll(assigneeForB2);
		
		return assigneeForB1.size();
	}
	public int getNumOfDeveloperInCommon_enhanced(int[] b1, int[] b2, int numOfDevs){
		int numOfDevsInCommon=0;
		int[] devs=new int[numOfDevs];
		
		for(int i=0;i<b1.length;i++)
			devs[b1[i]-1]++;
		
		for(int i=0;i<b2.length;i++)
			if(devs[b2[i]-1]>0){
				numOfDevsInCommon++;
				devs[b2[i]-1]--;
			}
		
		return numOfDevsInCommon;
	}
	
	
	public int compare(double totalTime, double totalCost, Solution solution){
		if(solution.getObjective(0)<totalTime && solution.getObjective(1)<totalCost)
			return -1;
		else if(solution.getObjective(0)<totalTime && solution.getObjective(1)==totalCost)
			return -1;
		else if(solution.getObjective(0)==totalTime && solution.getObjective(1)<totalCost)
			return -1;
		else if(solution.getObjective(0)==totalTime && solution.getObjective(1)==totalCost)
			return 0;
		else if(solution.getObjective(0)<totalTime && solution.getObjective(1)>totalCost)
			return 0;
		else if(solution.getObjective(0)>totalTime && solution.getObjective(1)<totalCost)
			return 0;
		else 
			return 1;
	}
	
	public int compare(double[] forward, double[] backward){
		if(backward[0]<forward[0] && backward[1]<forward[1])
			return -1;
		else if(backward[0]<forward[0] && backward[1]==forward[1])
			return -1;
		else if(backward[0]==forward[0] && backward[1]<forward[1])
			return -1;
		else if(backward[0]==forward[0] && backward[1]==forward[1])
			return 0;
		else if(backward[0]<forward[0] && backward[1]>forward[1])
			return 0;
		else if(backward[0]>forward[0] && backward[1]<forward[1])
			return 0;
		else 
			return 1;
	}
}
	

