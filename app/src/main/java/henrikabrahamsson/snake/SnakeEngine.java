package henrikabrahamsson.snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Random;

public class SnakeEngine extends Activity {

    // gameView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    GameView gameView;
    LinearLayout layout;

    // hej

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize gameView and set it as the view
        gameView = new GameView(this);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(getScreenWidth(),getScreenHeight() * 2 / 3));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.addView(gameView);
        setContentView(layout);

    }


    // Notice we implement runnable so we have
    // A thread and can override the run method.
    class GameView extends SurfaceView implements Runnable {


        // General stuff
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        Random random;


        // Determines if the game is running or not
        volatile boolean playing;


        // A Canvas and a Paint object
        Canvas canvas;
        Paint paint;


        // Frame rate
        private final long FPS = 10;
        private long MILLIS_PER_SEC = 2500;
        private long nextFrameTime;


        // Actually game related
        int blobx;
        int bloby;
        int score;
        int scorelastframe;
        int highscore;
        int snakeLength;
        volatile String direction = "";
        ArrayList<Integer> snakeX = new ArrayList<>();
        ArrayList<Integer> snakeY = new ArrayList<>();


        // Screen and input related
        float touchX;
        float touchY;
        int SCREEN_WIDTH;
        int SCREEN_HEIGHT;
        int blocksize;
        int paddingVertical;
        int paddingHorizontal;

        boolean calculateDimensions = true;


        public GameView(Context context) {

            // Super context to the SurfaceView
            super(context);

            // Initialize ourHolder, paint and random objects
            ourHolder = getHolder();
            paint = new Paint();
            random = new Random();

            nextFrameTime = System.currentTimeMillis();

            blocksize = 50;


            // Set up a new game
            newGame();

            // Start the game
            playing = true;


        }

        @Override
        public void run() {

            while (playing) {
                if (UpdateRequired()) {
                    update();
                    draw();
                }

            }

        }


        /** Starts a new game, resetting the score and the snake. Also stores the highscore. */
        public void newGame() {

            spawnBlob();

            snakeX.clear();
            snakeY.clear();
            snakeX.add(16 / 2);
            snakeY.add(16 / 2);

            direction = "";

            SharedPreferences preferences = getContext().getSharedPreferences("Preferences", 0);

            if(preferences.getInt("Highscore", -1) < score) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("Highscore", score);
                editor.commit();

            }


            highscore = preferences.getInt("Highscore", -1);

            snakeLength = 1;
            score = 0;
            scorelastframe = 0;

        }

        /** Updates the game, such as detecting if the snake eats a blob,
         *  death and movement. */
        public void update() {

            if (snakeX.get(0) == blobx && snakeY.get(0) == bloby) {
                eatBlob();
            }

            if (detectDeath()) {
                newGame();
            }

            if(score != scorelastframe) MILLIS_PER_SEC = 2500 - 50 * score;

            if (playing) {
                moveSnake();
            }

            scorelastframe = score;

        }

        /** Returns true if the snake should now be dead, otherwise false. */
        public boolean detectDeath() {

            if (snakeX.get(0) < 0) return true;
            if (snakeX.get(0) > 15) return true;
            if (snakeY.get(0) < 0) return true;
            if (snakeY.get(0) > 15) return true;

            if (snakeLength > 4) {
                for (int i = 4; i < snakeLength; i++) {
                    if (snakeX.get(0).equals(snakeX.get(i)) && snakeY.get(0).equals(snakeY.get(i))) {
                        return true;
                    }
                }
            }

            return false;

        }

        /** Resets the blob by removing it, updating the snake and spawning a new blob. */
        public void eatBlob() {
            //blockPlayable(blobx, bloby, Colors.BACKGROUNDPLAYAREA);
            snakeX.add(snakeLength, snakeX.get(snakeLength - 1));
            snakeY.add(snakeLength, snakeY.get(snakeLength - 1));
            snakeLength++;
            spawnBlob();
            score++;

        }

        /** Updates the blob position to a new one, that's not inside the snake. */
        public void spawnBlob() {
            boolean blobReCalc;
            do {

                blobx = random.nextInt(14) + 1;
                bloby = random.nextInt(14) + 1;

                blobReCalc = false;

                switch (direction) {
                    case "up": if(snakeX.get(0) == blobx && snakeY.get(0) > bloby) blobReCalc = true;
                        break;
                    case "down": if(snakeX.get(0) == blobx && snakeY.get(0) < bloby) blobReCalc = true;
                        break;
                    case "left": if(snakeY.get(0) == bloby && snakeX.get(0) > blobx) blobReCalc = true;
                        break;
                    case "right": if(snakeY.get(0) == bloby && snakeX.get(0) < blobx) blobReCalc = true;
                        break;
                }

                if(!blobReCalc) {
                    for (int i = 0; i < snakeLength; i++) {
                        if (snakeX.get(i) == blobx && snakeY.get(i) == bloby) {
                            blobReCalc = true;
                        }
                    }
                }


            } while (blobReCalc);

        }

        /** Moves the snake one step forward in the right direction. */
        public void moveSnake() {

            for (int i = snakeLength - 1; i > 0; i--) {
                snakeX.set(i, snakeX.get(i - 1));
                snakeY.set(i, snakeY.get(i - 1));

            }

            switch (direction) {
                case "up":
                    snakeY.set(0, snakeY.get(0) - 1);
                    break;
                case "down":
                    snakeY.set(0, snakeY.get(0) + 1);
                    break;
                case "right":
                    snakeX.set(0, snakeX.get(0) + 1);
                    break;
                case "left":
                    snakeX.set(0, snakeX.get(0) - 1);

            }


        }

        /** Determines if it is time to update the frame. */
        public boolean UpdateRequired() {

            // Are we due to update the frame
            if (nextFrameTime <= System.currentTimeMillis()) {
                // Tenth of a second has passed

                // Setup when the next update will be triggered
                nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SEC / FPS;

                // Return true so that the update and draw
                // functions are executed
                return true;
            }

            return false;
        }


        public void draw() {

            // Make sure our drawing surface is valid or we crash
            if (ourHolder.getSurface().isValid()) {

                // Setup
                canvas = ourHolder.lockCanvas(); // Lock canvas

                if(calculateDimensions) {
                    SCREEN_HEIGHT = ourHolder.getSurfaceFrame().height();
                    SCREEN_WIDTH = ourHolder.getSurfaceFrame().width();

                    paddingHorizontal = (SCREEN_WIDTH - Constants.PLAYSIZE) / 2;
                    paddingVertical = (SCREEN_HEIGHT - Constants.PLAYSIZE) / 2;

                    calculateDimensions = false;
                }

                canvas.drawColor(Colors.BACKGROUNDNONPLAYAREA);

                paint.setColor(Color.RED);
                paint.setTextSize(50);
                canvas.drawText("Score: " + score, 20, 80, paint);
                canvas.drawText("High Score: " + highscore, 20, 160, paint);

                drawPlayArea();

                blockPlayable(blobx, bloby, Colors.BLOB);

                if (!detectDeath()) {
                    for (int i = 0; i < snakeLength; i++) {

                        blockPlayable(snakeX.get(i), snakeY.get(i), Colors.SNAKE);
                    }
                }


                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        private void drawPlayArea() {

            paint.setColor(Color.WHITE);
            canvas.drawRect(new Rect(paddingHorizontal, paddingVertical, paddingHorizontal + Constants.PLAYSIZE, paddingVertical + Constants.PLAYSIZE), paint);

        }

        private void blockPlayable(int x, int y, int color) {
            paint.setColor(color);
            canvas.drawRect(new Rect(paddingHorizontal + x * blocksize, paddingVertical + y * blocksize,
                    paddingHorizontal + x * blocksize + blocksize,
                    paddingVertical + y * blocksize + blocksize), paint);
        }

        // If SnakeEngine Activity is paused/stopped
        // shutdown our thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }

        }

        // If SnakeEngine Activity is started then
        // start our thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:

                    touchX = motionEvent.getX();
                    touchY = motionEvent.getY();
                    break;

                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:

                    // Get the X value when the user released his/her finger
                    float currentX = motionEvent.getX();
                    float currentY = motionEvent.getY();
                    // check if horizontal or vertical movement was bigger


                    if (Math.abs(touchX - currentX) > Math.abs(touchY - currentY)) {
                        Log.v("", "x");
                        // going backwards: pushing stuff to the right
                        if (touchX < currentX) {

                            if (!direction.equals("left")) {
                                direction = "right";
                            }
                        }

                        // going forwards: pushing stuff to the left
                        if (touchX > currentX) {
                            if (!direction.equals("right")) {
                                direction = "left";
                            }

                        }

                    } else {
                        Log.v("", "y ");

                        if (touchY < currentY) {
                            if (!direction.equals("up")) {
                                direction = "down";
                            }

                        }
                        if (touchY > currentY) {
                            if (!direction.equals("down")) {
                                direction = "up";
                            }

                        }
                    }
                    break;


            }
            return true;
        }


    }


    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        gameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        gameView.pause();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }




}