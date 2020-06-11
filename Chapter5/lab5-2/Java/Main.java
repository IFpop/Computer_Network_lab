package network.chapter5.lab2;

import java.util.*;
import java.io.*;

public class Main 
{
	public static final int UNREACHABLE = 99;
	public static final int INF = UNREACHABLE;
	
	private static class Edge
	{
		public int dst, dis;
		Edge(int _dst, int _dis)
		{
			dst = _dst;
			dis = _dis;
		}
	}
	
	static Comparator<Edge> cmpEdge = new Comparator<Edge>() 
	{
		public int compare(Edge lhs, Edge rhs)
		{
			return lhs.dis - rhs.dis;
		}
    };
	
    static void outputRoutingTable (Edge[] edgeArr, int crtNode, int n)
	{
		System.out.println("node " + crtNode + " routing table: (To next dis)");
		for (int i = 0; i < n; i++)
		{
			System.out.print(i + " ");
			if (edgeArr[i].dst == -1)
			{
				System.out.print("- ");
			}
			else
			{
				System.out.print(edgeArr[i].dst + " ");
			}
			
			if (edgeArr[i].dis == INF)
			{
				System.out.println("-");
			}
			else
			{
				System.out.println(edgeArr[i].dis);
			}
		}
	};
	
	
	public static void main(String[] args) throws Exception
	{
		Scanner in = new Scanner(new File("lab5-2.cfg"));
		
		int n = in.nextInt();
		Vector<Edge>[] graph = new Vector[n];
		for (int i = 0; i < n; i++)
		{
			graph[i] = new Vector<Edge>();
		}
		for (int i = 0; i < n; i++)
		{
			System.out.print("node " + i + " link state: ");
			for (int j = 0; j < n; j++)
			{
				int dis = in.nextInt();
				if (dis > 0 && dis != UNREACHABLE)
				{
					graph[i].add(new Edge(j, dis));
					System.out.print("(" + j + " " + dis + ")");
				}
			}
			System.out.println("");
		}
		
		
		
		for (int crtNode = 0; crtNode < n; crtNode++)
		{
			System.out.println("--------------------Update routing table of node " + crtNode + "--------------------");
			
			Queue<Edge> que = new PriorityQueue<>(cmpEdge);
			
			Edge edgeArr[] = new Edge[n];
			for (int i = 0; i < n; i++)
			{
				edgeArr[i] = new Edge(-1, INF);
			}
			edgeArr[crtNode].dis = 0;
			outputRoutingTable(edgeArr, crtNode, n);
			
			que.add(new Edge(crtNode, 0));
			while (!que.isEmpty())
			{
				Edge shortestEdge = que.poll();
				if (edgeArr[shortestEdge.dst].dis != shortestEdge.dis)
				{
					continue;
				}
				System.out.println("Update edge to " + shortestEdge.dst);
				for (Edge edge : graph[shortestEdge.dst])
				{
					if (edge.dis + shortestEdge.dis < edgeArr[edge.dst].dis)
					{
						edgeArr[edge.dst].dis = edge.dis + shortestEdge.dis;
						
						if (shortestEdge.dst == crtNode)
						{
							// The node is neighbor
							edgeArr[edge.dst].dst = edge.dst;
						}
						else
						{
							edgeArr[edge.dst].dst = shortestEdge.dst;
						}
						
						que.add(new Edge(edge.dst, edgeArr[edge.dst].dis));
					}
				}
				outputRoutingTable(edgeArr, crtNode, n);
			}
			
			System.out.println("");
		}
		
		
		in.close();
	}
}
