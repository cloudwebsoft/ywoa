package com.redmoon.oa.test;
public class MouseAllPath {
    private int startI, startJ;  // 入口
    private int endI, endJ;  // 出口
    static private int maxI = 0;
    static private int maxJ = 0;

    public static void main(String[] args) {
        int maze[][] = {{0, 2, 2, 2, 2, 2, 2, 2, 2},
                        {0, 0, 0, 0, 0, 0, 0, 0, 2},
                        {2, 0, 2, 2, 0, 2, 2, 0, 2},
                        {2, 0, 2, 0, 0, 2, 0, 0, 2},
                        {2, 0, 2, 0, 2, 0, 2, 0, 2},
                        {2, 0, 0, 0, 0, 0, 2, 0, 2},
                        {2, 2, 0, 2, 2, 0, 2, 2, 2},
                        {2, 0, 0, 0, 0, 0, 0, 0, 0},
                        {2, 2, 2, 2, 2, 2, 2, 2, 0}};

        maxI = maze.length;
        maxJ = maze[0].length;

        System.out.println("显示迷宫：");
        for(int i = 0; i < maze.length; i++) {
            for(int j = 0; j < maze[0].length; j++)
                if(maze[i][j] == 2)
                    System.out.print("■");
                else
                    System.out.print("  ");
            System.out.println();
        }

        MouseAllPath mouse = new MouseAllPath();
        mouse.setStart(0, 0);
        mouse.setEnd(8, 8);

        mouse.go(maze);
    }

    public void setStart(int i, int j) {
        this.startI = i;
        this.startJ = j;
    }

    public void setEnd(int i, int j) {
        this.endI = i;
        this.endJ = j;
    }

    public void go(int[][] maze) {
        visit(maze, startI, startJ);
    }

    private void visit(int[][] maze, int i, int j) {
        maze[i][j] = 1;

        if(i == endI && j == endJ) {
            System.out.println("\n找到出口！");
            for(int m = 0; m < maze.length; m++) {
                for(int n = 0; n < maze[0].length; n++) {
                    if(maze[m][n] == 2)
                        System.out.print("■");
                    else if(maze[m][n] == 1)
                        System.out.print("◇");
                    else
                        System.out.print("  ");
                }
                System.out.println();
            }
        }

        if (j+1<=maxJ-1)
            if(maze[i][j+1] == 0)
                visit(maze, i, j+1);
        if (i+1<=maxI-1)
            if(maze[i+1][j] == 0)
                visit(maze, i+1, j);
        if (j-1>=0)
            if(maze[i][j-1] == 0)
                visit(maze, i, j-1);
        if (i-1>=0)
            if(maze[i-1][j] == 0)
                visit(maze, i-1, j);

        maze[i][j] = 0;
    }
 }
