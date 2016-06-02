package com.example.ysm0622.hw4_2;

import java.util.Stack;

/**
 * Created by ysm0622 on 2016-05-25.
 */
public class Maze extends Thread {

    private static final String TAG = "Maze";
    private boolean M[][];
    private int mRow;
    private int mCol;
    private Point C;

    public Maze(int row, int col) {
        this.mRow = row;
        this.mCol = col;
        this.M = new boolean[row][col];
    }

    @Override
    public void run() {

        // Initialize matrix and stack count
        for (int i = 0; i < mRow; i++)
            for (int j = 0; j < mCol; j++)
                M[i][j] = true;

        C = new Point(mRow - 2, mCol - 2);
        int cnt;
        int dr = -1;
        boolean D[] = new boolean[4];
        Stack<Point> S = new Stack<Point>();

        M[C.X][C.Y] = false;
        S.push(C);

        // Generate maze (DFS algorithm)
        while (true) {

            cnt = 0;

            if (dr != 2 && getV(1, 2)) { // UP
                D[0] = true;
                cnt++;
            } else D[0] = false;

            if (dr != 3 && getV(2, 2)) { // LEFT
                D[1] = true;
                cnt++;
            } else D[1] = false;

            if (dr != 0 && getV(3, 2)) { // DOWN
                D[2] = true;
                cnt++;
            } else D[2] = false;

            if (dr != 1 && getV(4, 2)) { // RIGHT
                D[3] = true;
                cnt++;
            } else D[3] = false;

            if (cnt == 0) {
                S.pop();
                if (S.size() > 0) C = S.lastElement();
                else break;
                dr = -1;
                continue;
            }

            do {
                dr = (int) (Math.random() * D.length); // Decide random direction
            } while (!D[dr]);

            if (dr == 0)
                C.Y -= 2;
            if (dr == 1)
                C.X -= 2;
            if (dr == 2)
                C.Y += 2;
            if (dr == 3)
                C.X += 2;

            Point N = new Point(C.X, C.Y);
            S.push(N);
            if (isValid()) {
                M[C.X][C.Y] = false;
                if (dr == 0)
                    M[C.X][C.Y + 1] = false;

                if (dr == 1)
                    M[C.X + 1][C.Y] = false;

                if (dr == 2)
                    M[C.X][C.Y - 1] = false;

                if (dr == 3)
                    M[C.X - 1][C.Y] = false;

            } else {
                S.pop();
                C = S.lastElement();
            }
        }
    }

    private boolean getV(int i, int v) { // Return current position's 8 direction information

        if (i == 0)
            return M[C.X][C.Y];
        if (i == 1 && C.Y - v >= 0)
            return M[C.X][C.Y - v];
        if (i == 2 && C.X - v >= 0)
            return M[C.X - v][C.Y];
        if (i == 3 && C.Y + v < mCol)
            return M[C.X][C.Y + v];
        if (i == 4 && C.X + v < mRow)
            return M[C.X + v][C.Y];
        if (i == 5 && C.X - v >= 0 && C.Y - v >= 0)
            return M[C.X - v][C.Y - v];
        if (i == 6 && C.X - v >= 0 && C.Y + v < mCol)
            return M[C.X - v][C.Y + v];
        if (i == 7 && C.X + v < mRow && C.Y + v < mCol)
            return M[C.X + v][C.Y + v];
        if (i == 8 && C.X + v < mRow && C.Y - v >= 0)
            return M[C.X + v][C.Y - v];

        return false;

    }

    private boolean isValid() { // Check 8 direction has at least one wall
        for (int i = 0; i < 9; i++)
            if (!getV(i, 1))
                return false;

        return true;
    }

    public boolean[][] getM() {
        return M;
    }
}
