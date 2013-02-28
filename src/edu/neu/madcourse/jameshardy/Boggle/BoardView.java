package edu.neu.madcourse.jameshardy.Boggle;

import java.util.*;

import edu.neu.madcourse.jameshardy.R;
import edu.neu.madcourse.jameshardy.R.anim;
import edu.neu.madcourse.jameshardy.R.color;
import edu.neu.madcourse.jameshardy.Boggle.BoggleGame;
//import edu.neu.madcourse.jameshardy.Boggle.DatabaseSingleton;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

import android.os.Bundle;
import android.os.CountDownTimer;
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
	// private int touch_tolerance = 0;
	private final Rect selRect = new Rect();
	private List<Rect> recList;

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

		recList = new ArrayList();
		mPath = new Path();
		// ...
		// DatabaseSingleton ds = DatabaseSingleton.getInstance();
		// Cursor c = ds.mDB.getWordMatches("hello", null);
		// Log.d(TAG, "CURSOR: " + c.toString());

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
		// Log.d(TAG, "sizechanged " + w + " " + h);
		width = w / grid_size;
		height = w / grid_size;
		// getRect(selX, selY, selRect);
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

		// Draw the letters...
		// Define color and style for numbers
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextSize(height * 0.75f);
		foreground.setTextScaleX(width / height);
		foreground.setTextAlign(Paint.Align.CENTER);

		// Draw the letter in the center of the tile
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

		// Draw the selection...
		Log.d(TAG, "selRect=" + selRect);
		Paint selected = new Paint();
		selected.setColor(getResources().getColor(R.color.puzzle_selected));

		for (int i = 0; i < recList.size(); i++) {
			canvas.drawRect(recList.get(i), selected);
		}

		// canvas.drawRect(selRect, selected);

		// Draw Timer text
		Paint timerText = new Paint();
		timerText.setColor(Color.BLACK);
		timerText.setTextSize(20);
		canvas.drawText(game.mTimerString, 300, 600, timerText);

		// Draw Score text
		Paint scoreText = new Paint();
		scoreText.setColor(Color.BLACK);
		scoreText.setTextSize(20);
		canvas.drawText("SCORE IS: ", 300, 630, scoreText);

		// Draw Pause and Quit Buttons
		Paint pauseBtn = new Paint();
		pauseBtn.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(50, 700, 200, 800, pauseBtn);

		Paint quitBtn = new Paint();
		quitBtn.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(300, 700, 450, 800, quitBtn);

		// pause button text
		Paint pauseText = new Paint(Paint.ANTI_ALIAS_FLAG);
		pauseText.setColor(getResources().getColor(R.color.puzzle_foreground));
		pauseText.setStyle(Style.FILL);
		pauseText.setTextSize(100 * 0.25f);
		pauseText.setTextScaleX(150 / 100);
		pauseText.setTextAlign(Paint.Align.CENTER);
		canvas.drawText("PAUSE", 125, 750, pauseText);
		// quit button text
		Paint quitText = new Paint(Paint.ANTI_ALIAS_FLAG);
		quitText.setColor(getResources().getColor(R.color.puzzle_foreground));
		quitText.setStyle(Style.FILL);
		quitText.setTextSize(100 * 0.25f);
		quitText.setTextScaleX(150 / 100);
		quitText.setTextAlign(Paint.Align.CENTER);
		canvas.drawText("QUIT", 375, 750, quitText);
	}

	public void setSelectedTile() {
		if (game.setTileIfValid(selX, selY)) {
			getRect(selX, selY, selRect);
			Rect temp = new Rect(selRect);
			recList.add(temp);
			invalidate();// may change hints
		} else {
			// Number is not valid for this tile
			Log.d(TAG, "setSelectedTile: invalid: x " + selX + " " + selY);
			// startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
		}
	}

	private void select(int x, int y) {
		// invalidate(selRect);
		selX = Math.min(Math.max(x, 0), 8);
		selY = Math.min(Math.max(y, 0), 8);
		// getRect(selX, selY, selRect);
		// recList.add(selRect);
		// invalidate(selRect);

		// check letter
		setSelectedTile();
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
		int x_coord = (int) (mX / width);
		int y_coord = (int) (mY / height);
		// check that the choose square is on board
		if ((x_coord < grid_size) && (y_coord < grid_size)) {
			select(x_coord, y_coord);
			Log.d(TAG, "onTouchEvent START: x " + selX + ", y " + selY);
		} else {
			// not on board
		}
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		// touch_tolerance = (int) (.15 * width);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
			float rX = mX % width;
			float rY = mY % height;

			// Eliminate picking wrong square on diagonal drag
			if ((rX >= (.15 * width) && rX <= (.85 * width))
					&& (rY >= (.15 * height) && rY <= (.85 * height))) {
				int x_coord = (int) (mX / width);
				int y_coord = (int) (mY / height);
				// check that the choose square is on board
				if ((x_coord < grid_size) && (y_coord < grid_size)) {
					select(x_coord, y_coord);
					Log.d(TAG, "onTouchEvent MOVE: x " + selX + ", y " + selY);
				} else {
					// touched off board
					Log.d(TAG, "onTouchEvent MOVE: x " + selX + ", y " + selY);
				}

			} else {
				Log.d(TAG, "onTouchEvent MOVE: BAD COORD " + rX + " " + rY);
			}
		}
	}

	private void touch_up() {
		mPath.lineTo(mX, mY);
		// TODO acknowledge word and call search/check

		// clear squares drawn
		recList.clear();
		invalidate();

		// empty used squares
		game.emptyUsedLetters();
		// reset path
		mPath.reset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		// QUIT BUTTON
		if (((int) x > 300 && (int) x < 450) && ((int) y > 700 && (int) y < 800)) {
			game.finish();
		}
		// Log.d(TAG, "ONTOUCHEVENT " + x + " " + y);
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