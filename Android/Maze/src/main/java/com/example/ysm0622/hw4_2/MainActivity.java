package com.example.ysm0622.hw4_2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private static final String TAG = "MainActivity";
    private EditText mEditText[];
    private Button mButton;
    private MazeSurfaceView mSurfaceView;
    private MazeSurfaceView.MazeThread mMazeThread;

    private int mRow;
    private int mCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Array allocation
        mEditText = new EditText[2];

        // View allocation
        mEditText[0] = (EditText) findViewById(R.id.EditText0);
        mEditText[1] = (EditText) findViewById(R.id.EditText1);
        mButton = (Button) findViewById(R.id.Button0);
        mSurfaceView = (MazeSurfaceView) findViewById(R.id.MazeSurfaceView);

        // Default setting
        for (int i = 0; i < 2; i++) {
            mEditText[i].addTextChangedListener(this);
        }
        mButton.setOnClickListener(this);
        mButton.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mButton)) { // Generate button click
            mRow = Integer.parseInt(mEditText[0].getText().toString());
            mCol = Integer.parseInt(mEditText[1].getText().toString());
            if (mRow % 2 == 0) mRow++;
            if (mCol % 2 == 0) mCol++;
            mMazeThread = mSurfaceView.getThread();
            mMazeThread.interrupt();
            mSurfaceView.drawMaze = false;
            mSurfaceView.setMaze(mRow, mCol);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { // Set enable button when both edittext are filled
        if (!mEditText[0].getText().toString().isEmpty() && !mEditText[1].getText().toString().isEmpty()) {
            mButton.setEnabled(true);
        } else {
            mButton.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
