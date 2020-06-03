package Chapter5.Lab1;

import java.io.*;
import java.util.*;

import static java.lang.Integer.min;

public class Router {
    private static  ArrayList<ArrayList<String>> routerTable;
    private static  int nexthop[][];
    private static  boolean isadjacent[][];
    private static String configFilePath="D:\\GoToCode\\Java\\ComputerNetworkLab\\src\\Chapter5\\Lab1\\config.txt";
    private static int numOfRouter;
    public static void init()
    {
        routerTable=new ArrayList<>();
        nexthop=new int[10][10];
        isadjacent=new boolean[10][10];
    }
    public static int getDistance(int x,int y)
    {
        int dis=Integer.parseInt(routerTable.get(x).get(y));
        return dis;
    }
    public static void getNetworkTopo (String filepath)throws Exception
    {
        File file=new File(filepath);
        BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
        String fileString;
        while((fileString=bufferedReader.readLine())!=null)
        {
            ArrayList<String> strRow= new ArrayList<>();
            String[] info=fileString.split(" ");
            for(int i=0;i<info.length;++i)
                strRow.add(info[i]);
            routerTable.add(strRow);
        }
        int num=routerTable.size();
        for(int i=0;i<num;++i)
            for(int j=0;j<num;++j)
            {
                nexthop[i][j]=j;
                if(getDistance(i,j)<16) isadjacent[i][j]=true;
            }
        bufferedReader.close();
    }
    public static int updatexy(int x,int y)
    {
        int cnt=0;
        for(int i=0;i<numOfRouter;++i)
        {
            if(nexthop[x][i]==y)
            {
                int newDist=Integer.parseInt(routerTable.get(x).get(y))+Integer.parseInt(routerTable.get(y).get(i));
                newDist=min(16,newDist);
                if(newDist!=Integer.parseInt(routerTable.get(x).get(i)))
                {
                    routerTable.get(x).set(i,((Integer)newDist).toString());
                    cnt++;
                    System.out.println("update router"+x+"'s table through router "+y);
                }
            }
            else if(isadjacent[x][y])
            {
                int newDist=Integer.parseInt(routerTable.get(x).get(y))+Integer.parseInt(routerTable.get(y).get(i));
                newDist=min(16,newDist);
                if(newDist<Integer.parseInt(routerTable.get(x).get(i)))
                {
                    routerTable.get(x).set(i,((Integer)newDist).toString());
                    cnt++;
                    nexthop[x][i]=y;
                    System.out.println("update router"+x+"'s table through router "+y);
                }
            }
        }
        return cnt;
    }
    public static void printRouterTable(int id)
    {
        System.out.println("------Router Table----------");
        System.out.println("Router:"+id);
        System.out.println("Des   dis  Nexthop");
        for(int i=0;i<routerTable.get(id).size();i++) {
            System.out.println(i+"     "+routerTable.get(id).get(i)+"      "+nexthop[id][i]);
        }
    }
    public static void update()
    {
        int num=0;
        boolean updatable=true;
        while(updatable)
        {
            updatable=false;
            for(int i=0;i<numOfRouter;++i)
            {
                for(int j=0;j<numOfRouter;++j)
                {
                    if(i!=j)
                    {
                        int cnt=updatexy(i,j);
                        if(cnt!=0)
                        {
                            updatable=true;
                            num+=cnt;
                        }
                    }
//                    if(!updatable) break;
                }
//                if(!updatable) break;;
            }
        }
        System.out.println("All update "+num+" times.");
//        return num;
    }
    public static void printMenu()
    {
        System.out.println("------------------Menu------------------");
        System.out.println("1--modify network topo");
        System.out.println("2--print routertable of specified router");
        System.out.println("3--update all routertable");
        System.out.println("4--print all routertable");
        System.out.println("q--quit the program");
        System.out.print("please input your option:");
    }
    public static void main(String[] args) throws Exception{
        init();
        getNetworkTopo(configFilePath);
        System.out.println("before update the routertable is");
        numOfRouter=routerTable.size();
        for(int i=0;i<numOfRouter;++i)
            printRouterTable(i);
        update();
        System.out.println("after update the routertable is");
        for(int i=0;i<numOfRouter;++i)
            printRouterTable(i);
        boolean run=true;
        while(run)
        {
            printMenu();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));
            char c;
            c=(char)bufferedReader.read();
            bufferedReader.read();
            if(c=='q') run=false;
            else if(c=='1') 
            {
                int src,des,dis;
                System.out.print("please input the source router:");
                src=Integer.parseInt(bufferedReader.readLine());
                System.out.print("please input the destination:");
                des=Integer.parseInt(bufferedReader.readLine());
                System.out.print("please input the new distance:");
                dis=Integer.parseInt(bufferedReader.readLine());
                routerTable.get(src).set(des,((Integer)dis).toString());
                routerTable.get(des).set(src,((Integer)dis).toString());
                System.out.println();
            }
            else if(c=='2')
            {
                int id;
                System.out.print("please input the id of router:");
                id=Integer.parseInt(bufferedReader.readLine());
//                bufferedReader.read();
                System.out.println();
                printRouterTable(id);
            }
            else if(c=='3')
            {
                update();
            }
            else if(c=='4')
            {
                for(int i=0;i<numOfRouter;++i)
                    printRouterTable(i);
            }
        }
    }
}
