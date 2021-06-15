package com.redmoon.oa.test;
public class FlowAllPath {
    static private int startI, startJ;  // 入口
    static private int endI, endJ;  // 出口
    static private int maxI = 0; // 行数
    static private int maxJ = 0; // 列数

    public static void main(String[] args) {

      int action[][] = {{0,  0,  0,  2,  2,  2,  2},
                        {2,  0,  2,  0,  2,  2,  2},
                        {2,  2,  0,  0,  2,  2,  2},
                        {2,  2,  2,  0,  0,  0,  2},
                        {2,  2,  2,  2,  0,  2,  0},
                        {2,  2,  2,  2,  2,  0,  0},
                        {2,  2,  2,  2,  2,  2,  0}};


        FlowAllPath flow = new FlowAllPath();
        maxI = action.length;
        maxJ = action[0].length;

        flow.setStart(0, 0);
        flow.setEnd(6, 6);

        // flow.go(action);
        startI = 1;
        startJ = 1;

        flow.getAllPathStartFromAction(action, startI, startJ);
    }

    public void setStart(int i, int j) {
        this.startI = i;
        this.startJ = j;
    }

    public void setEnd(int i, int j) {
        this.endI = i;
        this.endJ = j;
    }

    public void go(int[][] action) {
        visit(action, startI, startJ);
    }

    /**
     * 深度优先搜索
     * @param action int[][]
     * @param i int
     * @param j int
     */
    private void visit(int[][] action, int i, int j) {
        // 1 表示已遍历
        action[i][j] = 1;
        // System.out.println("i=" + i + " j=" + j);

        if(j == endJ) {
            System.out.println("\n找到目标节点！");
            for(int m = 0; m < action.length; m++) {
                for(int n = 0; n < action[0].length; n++) {
                    if(action[m][n] == 2)
                        System.out.print("■");
                    else if(action[m][n] == 1)
                        System.out.print("◇"); // 按列从左到右，找出此符号，可得出此次遍历得来的路径
                    else
                        System.out.print("  ");
                }
                System.out.println();
            }
            // 打印出实际路径
            String str = "";
            for(int m = 0; m < action[0].length; m++) {
                for(int n = 0; n < action.length; n++) {
                    if (action[m][n]==1) {
                        str += m + ",";
                        break;
                    }
                }
            }

            System.out.println("路径为：" + str + j);

        }

        // 此处注意i与j在流程图节点中是相等的
        for (int k=0; k<=maxJ-1; k++) {
            if (k!=j) {
                if (action[j][k]==0) // 查找(i,j)节点与其它节点是否相连接
                    visit(action, j, k);
            }
        }

        // 找不到从i,j开始往下的其它通路，则将其恢复为原来的i,j以便下一轮寻找
        action[i][j] = 0;
    }

    // 找出自i,j开始的所有路径，注意图中不得有环，否则会陷入死循环
    private void getAllPathStartFromAction(int[][] action, int i, int j) {
        action[i][j] = 1;
        boolean found = false;
        for (int k=0; k<=maxJ-1; k++) {
            if (k!=j) {
                if (action[j][k]==0) {
                    found = true;
                    getAllPathStartFromAction(action, j, k);
                }
            }
        }

        // found为false时表示找到了路径中的最后一个节点，然后运行到此处，完了以后退栈
        if (!found) {
            String str = "";
            for (int m = 0; m < action[0].length; m++) {
                for (int n = 0; n < action.length; n++) {
                    if (action[m][n] == 1) {
                        str += m + ",";
                        break;
                    }
                }
            }
            // if (j != startJ)
                System.out.println(str + j);
        }

        action[i][j] = 0;
    }
 }
