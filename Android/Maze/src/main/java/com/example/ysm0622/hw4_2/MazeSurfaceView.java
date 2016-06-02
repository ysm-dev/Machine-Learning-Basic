package com.example.ysm0622.hw4_2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by ysm0622 on 2016-05-23.
 */
public class MazeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MazeSurfaceView";
    private Context mContext;
    private SurfaceHolder mHolder;
    private MazeThread mMazeThread;

    private Maze mMaze;
    private float mWidth;
    private float mHeight;
    private Bitmap mBuffer;
    private Canvas mCanvas;
    private Paint mazePaint;
    private Paint drawPaint;
    private int mRow;
    private int mCol;
    public boolean drawMaze = false;
    boolean M[][];
    private Point pBall;
    private float mRadius;
    private boolean startInside = false;

    public MazeSurfaceView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public MazeSurfaceView(Context context, AttributeSet attr) {
        super(context, attr);
        this.mContext = context;
        init();
    }

    public void init() { // Constructor initializer
        this.mHolder = getHolder();
        mHolder.setKeepScreenOn(true);
        getHolder().addCallback(this);
        setFocusable(true);
        mMazeThread = new MazeThread();
    }

    public void setMaze(int row, int col) { // Create Maze and draw
        mMaze = new Maze(row, col);
        pBall = new Point(0, 0);
        mRadius = mWidth / col * 1 / 3;
        mRow = row;
        mCol = col;
        drawPaint.setStrokeWidth(5);
        mMaze.start();
        try {
            mMaze.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        M = new boolean[row][col];
        M = mMaze.getM();
        drawMaze();
        drawBall();
    }

    public void drawMaze() { // Draw maze
        mCanvas.drawColor(Color.BLACK);
        for (int i = 0; i < mRow; i++) {
            for (int j = 0; j < mCol; j++) {
                if (!M[i][j]) {
                    mazePaint.setColor(Color.WHITE);
                    mCanvas.drawRect(mWidth / mCol * j, mHeight / mRow * i, mWidth / mCol * (j + 1), mHeight / mRow * (i + 1), mazePaint);
                }
            }
        }
        mazePaint.setColor(getResources().getColor(R.color.colorAccent));
        mCanvas.drawRect(mWidth / mCol * (mCol - 2), mHeight / mRow * (mRow - 2), mWidth / mCol * (mCol - 1), mHeight / mRow * (mRow - 1), mazePaint);
        mazePaint.setColor(Color.GREEN);
    }

    public void drawBall() { // Draw ball at default postion
        pBall.X = (int) (mWidth / mCol + mWidth / mCol / 2);
        pBall.Y = (int) (mHeight / mRow + mHeight / mRow / 2);
        mCanvas.drawCircle(pBall.X, pBall.Y, mRadius, mazePaint);
    }

    public void drawBall(int x, int y) { // Draw ball
        pBall.X = x;
        pBall.Y = y;
        mCanvas.drawCircle(x, y, mRadius, mazePaint);
    }

    int lastX, lastY, currX, currY;
    boolean isDeleting;

    @Override
    public void onMeasure(int width, int height) {
        super.onMeasure(width, width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        boolean del = false;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                // Check validation that inputX,Y are inside of ball
                if (Math.pow(pBall.X - lastX, 2) + Math.pow(pBall.Y - lastY, 2) <= mRadius * mRadius)
                    startInside = true;
                else
                    startInside = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDeleting) break;
                if (startInside) {
                    currX = (int) event.getX();
                    currY = (int) event.getY();
                    mCanvas.drawLine(lastX, lastY, currX, currY, drawPaint);
                    del = false;
                    for (int i = 0; i < mRow; i++) {
                        for (int j = 0; j < mCol; j++) {
                            if (M[i][j]) { // Check current X,Y are in wall(black) or not
                                if (currX >= mWidth / mCol * j && currX <= mWidth / mCol * (j + 1) && currY >= mHeight / mRow * i && currY <= mHeight / mRow * (i + 1)) {
                                    drawMaze();
                                    drawBall();
                                    del = true;
                                    startInside = false;
                                    isDeleting = true;
                                    break;
                                }
                            }
                        }
                        if (del) break;
                    }
                    lastX = currX;
                    lastY = currY;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDeleting) isDeleting = false;
                if (startInside) {
                    drawMaze();
                    if (!del) drawBall(lastX, lastY);
                    else drawBall(); // If user clear the game, level up the maze
                    if (lastX >= mWidth / mCol * (mCol - 2) && lastX <= mWidth / mCol * (mCol - 1) && lastY >= mHeight / mRow * (mRow - 2) && lastY <= mHeight / mRow * (mRow - 1)) {
                        Toast.makeText(mContext, "Clear! Much harder~!", Toast.LENGTH_LONG).show();
                        setMaze(mRow + 10, mCol + 10);
                    }
                }

                break;
        }
        return true;
    }

    public MazeThread getThread() {
        return mMazeThread;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { // Initialize Default value when created
        mMazeThread.setRunning(true);
        mMazeThread.start();
        mCanvas = new Canvas();

        mWidth = getWidth();
        mHeight = getHeight();

        mBuffer = Bitmap.createBitmap((int) mWidth, (int) mHeight, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBuffer);
        mCanvas.drawColor(Color.WHITE);

        mazePaint = new Paint();
        drawPaint = new Paint();
        drawPaint.setColor(getResources().getColor(R.color.colorAccent));
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public class MazeThread extends Thread {

        private static final String TAG = "MazeThread";
        private boolean mRunning = false;

        public void setRunning(boolean v) {
            mRunning = v;
        }

        @Override
        public void run() {
            // super.run();
            while (mRunning) {
                Canvas Canvas = null; // Swap buffer
                try {
                    Canvas = mHolder.lockCanvas();
                    synchronized (mHolder) {
                        Canvas.drawBitmap(mBuffer, 0, 0, drawPaint);
                    }
                } finally {
                    if (Canvas != null) {
                        mHolder.unlockCanvasAndPost(Canvas);
                    }
                }
            }
        }
    }
}
