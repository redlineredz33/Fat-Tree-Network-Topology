// Ivan Khaffaji

// Implementation of Fat-Tree Network Topology

// This implementation uses Breadth-First Search algorithm to list all possible nodes in the Fat-Tree
// and utilizes BFS to calculate the number of hops that is required to traverse any two nodes in a
// undirected graph. Additionally, it also implements an undirected graph to store nodes, and each
// edge on the graph represents a network connection in the Fat-Tree network topology.

// Copyright 2020 Ivan Khaffaji

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class FatTreeDriver {
	
	public static void main(String args[]) 
    { 
        @SuppressWarnings("resource")
		Scanner userInput = new Scanner(System.in);
        @SuppressWarnings("resource")
		Scanner numberInput = new Scanner(System.in);
		
		String input;
		System.out.print("Please enter number of desired ports for each switch (i.e. k). Please ensure that your input is an even number: ");
		input = userInput.nextLine();
		
		while(Integer.parseInt(input)%2 != 0) {
			System.out.println("Please enter an even number for k");
			input = userInput.nextLine();
		}
		
		int k = Integer.parseInt(input);
		
		int numVMPairs = 0;
		System.out.print("Please enter number of VM pairs to place randomly onto PMs (l): ");
		numVMPairs = numberInput.nextInt();
		
		System.out.print("Please enter communication frequency (max) of VMs: ");
		int commFreq = randomInteger(0,numberInput.nextInt());
		System.out.println("Randomly chosen communication frequency is " + commFreq + "\n");
		
		System.out.print("Please enter number of VNFs desired to place randomly onto switches (m): ");
		int numVNF = numberInput.nextInt();
		
		int numOfCoreSwitches = (k/2) *(k/2);
		int numOfPods = k;
		int numOfAggSwitches = (k/2) * numOfPods;
		int numOfEdgeSwitches = (k/2) * numOfPods;
		int numOfSwitchPerPod = (k/2);
		int numOfPhysicalMachines = (k*k*k)/4;

		//calculates total number of nodes for the whole graph
        int totalTreeNodes = (k*k/4) + (k/2*k) + (k/2*k) + (k*k*k/4) + (numVMPairs*2) + numVNF;
  
        ArrayList<ArrayList<Integer>> adj =  new ArrayList<ArrayList<Integer>>(totalTreeNodes); 
        
        //creates the adjacency matrix
        for (int i = 0; i < totalTreeNodes; i++) { 
            adj.add(new ArrayList<Integer>()); 
        } 

        //this loop adds agg switches for first half of core switches
        for(int i=numOfCoreSwitches; i<numOfCoreSwitches+numOfAggSwitches; i++) {
        	for(int j=0; j<numOfCoreSwitches/2; j++) {
        		addEdge(adj, j, i);
        	}
        	i++;
        }
        
        //this loop adds agg switches for second half of core switches
        for(int i=numOfCoreSwitches+1; i<numOfCoreSwitches+numOfAggSwitches; i++) {
        	for(int j=numOfCoreSwitches/2; j<numOfCoreSwitches; j++) {
        		addEdge(adj, j, i);
        	}
        	i++;
        }
        
        
        //this series of loops creates the structure of the graph
        int index = numOfCoreSwitches+numOfEdgeSwitches;
        for(int i=numOfCoreSwitches; i<numOfCoreSwitches+numOfEdgeSwitches; i++) {

        	for(int j=0; j<numOfSwitchPerPod; j++) {        
        		System.out.println(i);
        		System.out.println(index);
                System.out.println();
        		addEdge(adj, i, index);
        		index++;
        	}
        	i++;
        }
        
       index = numOfCoreSwitches+numOfEdgeSwitches;
       for(int i=numOfCoreSwitches+1; i<numOfCoreSwitches+numOfEdgeSwitches; i++) {
        	for(int j=0; j<numOfSwitchPerPod; j++) {
                System.out.println(i);
                System.out.println(index);
                System.out.println();
        		addEdge(adj, i, index);
        		index++;
        	}
        	i++;
        }
       
       //Adds physical machines and connects them to edge switches
       int pmIndex = numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches;
       for(int i=numOfCoreSwitches+numOfEdgeSwitches; i<numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches; i++) {
    	   for(int j=0; j<numOfSwitchPerPod; j++) {
       			addEdge(adj, i, pmIndex);
       			pmIndex++;
       		}
       }
       
       boolean isSource = true;
       String sourceDest;
       // this loop is for virtual machine placement onto random physical machines
       for(int i=totalTreeNodes-(numVMPairs*2)-numVNF; i<totalTreeNodes-numVNF; i++) {
    	   if(isSource == true) {
    		   sourceDest = "source";
    	   }
    	   else
    		   sourceDest = "destination";
    	   int myInt = randomInteger((numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches), 
    			   (numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches+numOfPhysicalMachines-1));
    	   System.out.println("vm" + ((i-(numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches+numOfPhysicalMachines))/2) 
    			  + " " + sourceDest + " is assigned to pm" + (myInt-(numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches)));
    	   addEdge(adj, myInt, i);
    	   isSource = !isSource;
       }
       
       //Prints communication cost of each VM pair
       int totalCost = 0;
       for(int i=0; i<numVMPairs*2; i++) {
    	   int temp = i + (numOfCoreSwitches+numOfEdgeSwitches+numOfAggSwitches+numOfPhysicalMachines);
    	   int temp2 = temp + 1;
    	   int cost = printNodeDistance(adj, temp, temp2, totalTreeNodes, commFreq, numVNF, numVMPairs, k, false, true); 
    	   System.out.println("vm" + (i/2) + " pair's total communication cost is " + cost);
    	   i++;
    	   totalCost += cost;
       }
       
       System.out.println("\nTotal communication cost across all virtual machines combined is " + totalCost + ".\n");
       
       //Creates array of shuffled integers to generate "random non-repeating integer" in range for VNF placement
       ArrayList<Integer> myList = new ArrayList<Integer>();
       for(int i=numOfCoreSwitches; i<(numOfCoreSwitches+numOfAggSwitches+numOfEdgeSwitches); i++) {
    	   myList.add(i);
       }
       
       Collections.shuffle(myList);
       
       //Places VNFs randomly on edge switch + agg switch
       int myListCounter = 0;
       for(int i=(totalTreeNodes-numVNF); i<totalTreeNodes; i++) {
       		addEdge(adj, i, myListCounter);
       		myListCounter++;
       }
       
       int totalCost2 = 0;
       int VMIndex = (totalTreeNodes-numVNF);
       int VNFIndex = VMIndex - (numVMPairs*2);
      
       //Calculates total communication cost routed through randomly placed VNFs on edge switch + agg switch
       for(int i=0; i<numVMPairs; i++) {
    	   int VNFOffset = VNFIndex + 1;
   			int startCost = printNodeDistance(adj, VNFIndex, VMIndex, totalTreeNodes, commFreq, numVNF, numVMPairs, k, false, false); 
       		for(int j=0; j<numVNF; j++) {
       			int temp = j + VMIndex;
       			int temp2 = temp + 1;
       			int cost= printNodeDistance(adj, temp, temp2, totalTreeNodes, commFreq, numVNF, numVMPairs, k, false, false); 
       			totalCost2 += cost;
       		}
   			int endCost = printNodeDistance(adj, totalTreeNodes-1, VNFOffset, totalTreeNodes, commFreq, numVNF, numVMPairs, k, false, false); 

       		VNFIndex += 2;
       		totalCost += startCost;
       		totalCost += endCost;
       }
       totalCost += totalCost2;
       
       System.out.println("Total communication cost across all virtual machines going through VNFs combined is " + totalCost + ".");

       System.out.println();
       	//sets initial and ending node at index 0 in order to print out fat tree
        int source = 0, dest = 0; 
        
        //uses BFS to iterate through the graph and return printout of all nodes of tree
        printNodeDistance(adj, source, dest, totalTreeNodes, commFreq, numVNF, numVMPairs, k, true, true); 
        
        for(int i=0; i<k; i++) {
        	System.out.println("pod" + i);
        }
        
        //grabs user input for first desired node
        System.out.print("\nPlease enter the first node: ");
		input = userInput.nextLine();
		
		if(input.charAt(0)=='c') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				source = chosenNum;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
		if(input.charAt(0)=='p') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				source = chosenNum + numOfCoreSwitches + numOfAggSwitches + numOfEdgeSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");	
			}
		}
		
		if(input.charAt(0)=='v') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				source = chosenNum;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");	
			}
		}
		
		if(input.charAt(0)=='e') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				source = chosenNum + numOfCoreSwitches + numOfAggSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
		if(input.charAt(0)=='a') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				source = chosenNum + numOfCoreSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
        System.out.print("Please enter the second node: ");
		input = userInput.nextLine();
		
		if(input.charAt(0)=='c') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				dest = chosenNum;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
		if(input.charAt(0)=='p') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				dest = chosenNum + numOfCoreSwitches + numOfAggSwitches + numOfEdgeSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
		if(input.charAt(0)=='v') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				dest = chosenNum;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");	
			}
		}
		
		if(input.charAt(0)=='e') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				dest = chosenNum + numOfCoreSwitches + numOfAggSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}
		
		if(input.charAt(0)=='a') {
			try {
				int chosenNum = Integer.parseInt(input.substring(2));
				dest = chosenNum + numOfCoreSwitches;
			}
			catch(Exception e) {
				System.out.println("Please enter proper node name.");
			}
		}

		//prints out total distance between nodes on graph 
		printNodeDistance(adj, source, dest, totalTreeNodes, commFreq, numVNF, numVMPairs, k, false, true); 
		
    } 
	
	
	//method adds edge to graph in both directions (undirected graph)
    private static void addEdge(ArrayList<ArrayList<Integer>> adj, int i, int j) 
    { 
        adj.get(i).add(j); 
        adj.get(j).add(i); 
    } 
  
    //uses bfs to iterate through the graph and return printout of all nodes of tree
    private static int printNodeDistance( ArrayList<ArrayList<Integer>> adj, int s, int dest, int v, int freq, int numVNF, int numVMPairs, int k, boolean displayTree, boolean showOutput) 
    { 
        int pred[] = new int[v]; 
        int dist[] = new int[v]; 
  
        if (BFS(adj, s, dest, v, pred, dist, k, numVNF, numVMPairs, displayTree) == true) { 
        	int distance = dist[dest];
        	if(showOutput) {
        	System.out.println("\nThe number of hops required to traverse between the virtual machine pair's source "
        			+ "and destination physical machines is " + distance + ".");
        	}
            return distance*freq; 
        } 
        
        else
        	return 0;
    } 
    
    private static int randomInteger(int min, int max) {
        Random arrayInt = new Random();
        return arrayInt.nextInt((max - min) + 1) + min;
    }
  
    //BFS algorithm that is slightly modified in order to return distance as well as tree
    private static boolean BFS(ArrayList<ArrayList<Integer>> adj, int src, int dest, int v, int pred[], int hopNum[], int k, int numVNF, int numVMPairs, boolean displayTree) 
    { 
        LinkedList<Integer> queue = new LinkedList<Integer>(); 
        boolean visited[] = new boolean[v]; 
        
        int numOfPods = k;
		int numOfCoreSwitches = (k/2) * (k/2);
		int numOfAggSwitches = (numOfPods/2) * numOfPods;
		int numOfEdgeSwitches = numOfAggSwitches;
		int numOfPhysicalMachines = (k*k*k)/4;
        int totalTreeNodes = (k*k/4) + (k/2*k) + (k/2*k) + (k*k*k/4) + (numVMPairs*2) + numVNF;

        for (int i = 0; i < v; i++) { 
            visited[i] = false; 
            hopNum[i] = Integer.MAX_VALUE; 
            pred[i] = -1; 
        } 
  
        visited[src] = true; 
        hopNum[src] = 0; 
        queue.add(src); 
        
       
        while (!queue.isEmpty()) { 
            int u = queue.remove(); 
            
            for (int i = 0; i < adj.get(u).size(); i++) { 
                if (visited[adj.get(u).get(i)] == false) { 
                    visited[adj.get(u).get(i)] = true; 
                    hopNum[adj.get(u).get(i)] = hopNum[u] + 1; 
                    pred[adj.get(u).get(i)] = u; 
                    queue.add(adj.get(u).get(i)); 
                    
                    if(displayTree) {
                    	
                    	if(adj.get(u).get(i) <= numOfCoreSwitches ) {
                    		System.out.println("cs" + (adj.get(u).get(i)));
                    	}
                    
                    	if(adj.get(u).get(i) > numOfCoreSwitches && adj.get(u).get(i) <= (numOfCoreSwitches + numOfEdgeSwitches)) {
                    		System.out.println("as" + (adj.get(u).get(i)-numOfCoreSwitches));
                    	}
                    
                    	if(adj.get(u).get(i) > (numOfCoreSwitches+numOfEdgeSwitches) && adj.get(u).get(i) <= (numOfCoreSwitches+(2 * numOfEdgeSwitches))) {
                    		System.out.println("es" + (adj.get(u).get(i)-(numOfCoreSwitches + numOfEdgeSwitches)));
                    	}
                    
                    	if(adj.get(u).get(i) > (numOfCoreSwitches+(numOfEdgeSwitches*2)) && adj.get(u).get(i) <= (totalTreeNodes-(numVMPairs*2)-numVNF)) {
                    		System.out.println("pm" + (adj.get(u).get(i)-(numOfCoreSwitches+(2 * numOfEdgeSwitches))));
                    	}

                    	if(adj.get(u).get(i) > (totalTreeNodes-(numVMPairs*2)-numVNF) && adj.get(u).get(i) <= (totalTreeNodes - numVNF)) {
                    		System.out.println("vm" + (adj.get(u).get(i)-(numOfCoreSwitches+(2 * numOfEdgeSwitches)+numOfPhysicalMachines)));
                    	}
         
                    	
                    	if(adj.get(u).get(i) > (totalTreeNodes - numVNF-1)) {
                    		System.out.println("VNF_" + (adj.get(u).get(i)-(totalTreeNodes-numVNF-1)));
                    	}
                    
                    }
                    
                    if (adj.get(u).get(i) == dest) 
                    	return true; 
                } 
            } 
        } 

        return false; 
    } 
} 

