package edu.neu.madcourse.jameshardy.Boggle;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.anim;
import edu.neu.madcourse.jameshardy.R.color;
import edu.neu.madcourse.jameshardy.Boggle.BoggleGame;
//import edu.neu.madcourse.jameshardy.Boggle.DatabaseSingleton;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class BoardView extends View {

	private static final String TAG = "Boggle";

	private static final String SELX = "selX";
	private static final String SELY = "selY";
	private static final String VIEW_STATE = "viewState";
	private static final int ID = 42;

	private int grid_size = 0;
	private float width; // width of one tile
	private float height; // height of one tile
	private int selX; // X index of selection
	private int selY; // Y index of selection
	private final Rect selRect = new Rect();

	private final BoggleGame game;
	private Path mPath;
	
	public BoardView(Context context) {

		super(context);
		this.game = (BoggleGame) context;
		setFocusable(true);
		setFocusableInTouchMode(true);

		// Get the grid_size from the user select, since theres three
		// selections 0,1,2 add four to each one to get grid size
		grid_size = this.game.getIntent().getIntExtra(game.GRID_SIZE,
				game.GRID_FOUR);
		grid_size += 4;
		
		mPath = new Path();
		// ...
		//DatabaseSingleton ds = DatabaseSingleton.getInstance();
		//Cursor c = ds.mDB.getWordMatches("hello", null);
		//Log.d(TAG, "CURSOR: " + c.toString());
		
		setId(ID);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Log.d(TAG, "onSaveInstanceState");
		Bundle bundle = new Bundle();
		bundle.putInt(SELX, selX);
		bundle.putInt(SELY, selY);
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState");
		Bundle bundle = (Bundle) state;
		select(bundle.getInt(SELX), bundle.getInt(SELY));
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
	}

	// sets the width and height from screen size/orientation
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// width = w / 9f;
		// height = h / 9f;
		// SET THE BOARD TO THE SAME FRACTION OF WIDTH SINCE ALWAYS VERTICAL
		// ORIENTATION. MAKES EASY TO ADD BUTTONS AT BOTTOM
		// width = w / 4f;
		// height = w / 4f;
		width = w / grid_size;
		height = w / grid_size;
		getRect(selX, selY, selRect);
		Log.d(TAG, "onSizeChanged: width " + width + ", height " + height);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the background...
		Paint background = new Paint();
		background.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(0, 0, getWidth(), getWidth(), background);

		// draw grid lines
		Paint dark = new Paint();
		dark.setColor(getResources().getColor(R.color.puzzle_dark));

		// Log.d(TAG, "grid size " + grid_size);
		// Draw the minor grid lines
		for (int i = 0; i < grid_size; i++) {
			canvas.drawLine(0, i * height, getWidth(), i * height, dark);
			canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, dark);
			canvas.drawLine(i * width, 0, i * width, getWidth(), dark);
			canvas.drawLine(i * width + 1, 0, i * width + 1, getWidth(), dark);
		}

		// Draw the numbers...
		// Define color and style for numbers
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);

		// Draw the number in the center of the tile
		FontMetrics fm = foreground.getFontMetrics();
		// Centering in X: use alignment (and X at midpoint)
		float x = width / 2;
		// Centering in Y: measure ascent/descent first
		float y = height / 2 - (fm.ascent + fm.descent) / 2;
		for (int i = 0; i < grid_size; i++) {
			for (int j = 0; j < grid_size; j++) {
				canvas.drawText(this.game.getTileString(i, j), i * width + x, j
						* height + y, foreground);
			}
		}
	}

	private void select(int x, int y) {
		invalidate(selRect);
		selX = Math.min(Math.max(x, 0), 8);
		selY = Math.min(Math.max(y, 0), 8);
		getRect(selX, selY, selRect);
		invalidate(selRect);
	}

	private void getRect(int x, int y, Rect rect) {
		rect.set((int) (x * width), (int) (y * height),
				(int) (x * width + width), (int) (y * height + height));
	}

	
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        select((int) (mX / width),
        		(int) (mY / height));
        Log.d(TAG, "onTouchEvent START: x " + selX + ", y " + selY);
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            select((int) (mX / width),
            		(int) (mY / height));
            Log.d(TAG, "onTouchEvent MOVE: x " + selX + ", y " + selY);
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        //TODO acknowledge word and call search/check
        
        //reset path 
        mPath.reset();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

	
}