package henrikabrahamsson.snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class SnakeEngine extends Activity {

    GameView gameView;
    LinearLayout mainLayout;
    LinearLayout topLayout;
    TextView HighScoreTextView;
    TextView ScoreTextView;
    TextView HS;
    TextView S;
    Button startButton;

    int GAME_PX_PRELIMINARY_HEIGHT;
    int GAME_PX_WIDTH;
    int BLOCKSIZE;
    int GAME_BLOCK_WIDTH;
    int GAME_BLOCK_HEIGHT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MAIN LAYOUT
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);

        // TOP LAYOUT
        topLayout = new LinearLayout(this);
        // Pure text
        HighScoreTextView = new TextView(this);
        ScoreTextView = new TextView(this);
        // Actual score
        HS = new TextView(this);
        S = new TextView(this);

        LinearLayout.LayoutParams scoreLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams scoreTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        scoreLayoutParams.setMargins(5, 5, 5, 20);
        scoreTextParams.setMargins(20, 20, 20, 20);
        scoreParams.setMargins(20, 20, 20, 20);

        topLayout.setLayoutParams(scoreLayoutParams);
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLayout.setGravity(Gravity.CENTER);
        HighScoreTextView.setLayoutParams(scoreTextParams);
        ScoreTextView.setLayoutParams(scoreTextParams);
        HS.setLayoutParams(scoreParams);
        S.setLayoutParams(scoreParams);

        Typeface scoreTextTypeFace = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        Typeface scoreTypeFace = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);

        HighScoreTextView.setTypeface(scoreTextTypeFace);
        HighScoreTextView.setTextColor(Color.BLACK);
        HighScoreTextView.setText("High score:");

        ScoreTextView.setTypeface(scoreTextTypeFace);
        ScoreTextView.setTextColor(Color.BLACK);
        ScoreTextView.setText("Score:");

        HS.setTypeface(scoreTypeFace);
        HS.setTextColor(Color.YELLOW);
        HS.setGravity(Gravity.CENTER);

        S.setTypeface(scoreTypeFace);
        S.setTextColor(Color.YELLOW);
        S.setGravity(Gravity.CENTER);

        topLayout.addView(HighScoreTextView);
        topLayout.addView(HS);
        topLayout.addView(ScoreTextView);
        topLayout.addView(S);


        // Screen and play area related calculations
        GAME_PX_WIDTH = getScreenWidth();
        GAME_PX_PRELIMINARY_HEIGHT = getScreenHeight() * 2 / 3;
        GAME_BLOCK_WIDTH = 20;
        BLOCKSIZE = GAME_PX_WIDTH / GAME_BLOCK_WIDTH;
        GAME_BLOCK_HEIGHT = GAME_PX_PRELIMINARY_HEIGHT / BLOCKSIZE;

        startButton = new Button(this);
        startButton.setLayoutParams(scoreTextParams);
        startButton.setText("Click here to start playing!");

        // Initialize the view for the game
        gameView = new GameView(this);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(GAME_BLOCK_WIDTH * BLOCKSIZE, GAME_BLOCK_HEIGHT * BLOCKSIZE));


        mainLayout.addView(topLayout);
        mainLayout.addView(gameView);
        mainLayout.addView(startButton);
        setContentView(mainLayout);

        mainLayout.setBackgroundColor(Color.BLUE);



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

        int paddingVertical;
        int paddingHorizontal;

        boolean calculateDimensions = true;

        /** Constructor for the view used for the actual snake game. */
        public GameView(Context context) {

            // Super context to the SurfaceView
            super(context);

            // Initialize ourHolder, paint and random objects
            ourHolder = getHolder();
            paint = new Paint();
            random = new Random();

            nextFrameTime = System.currentTimeMillis();

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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            S.setText(String.valueOf(score));
                        }
                    });

                }
            }
        }


        /** Starts a new game, resetting the score and the snake. Also stores the highscore. */
        public void newGame() {
            spawnBlob();
            snakeX.clear();
            snakeY.clear();
            snakeX.add(GAME_BLOCK_WIDTH / 2);
            snakeY.add(GAME_BLOCK_HEIGHT / 2);
            direction = "";

            SharedPreferences preferences = getContext().getSharedPreferences("Preferences", 0);

            if(preferences.getInt("Highscore", -1) < score) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("Highscore", score);
                editor.apply();

            }

            if(preferences.contains("Highscore")) {
                highscore = preferences.getInt("Highscore", -1);
            } else {
                highscore = 0;
            }

            snakeLength = 1;
            score = 0;
            scorelastframe = 0;
            MILLIS_PER_SEC = 2500;


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HS.setText(String.valueOf(highscore));
                    S.setText(String.valueOf(score));
                    startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            direction = "right";
                        }
                    });
                }
            });


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

            if(score != scorelastframe && score <= 20) {
                MILLIS_PER_SEC = 2500 - 50*score;
            } else if(score != scorelastframe) {
                MILLIS_PER_SEC = 1500;
            }

            if (playing) {
                moveSnake();
            }

            scorelastframe = score;

        }

        /** Returns true if the snake should now be dead, otherwise false. */
        public boolean detectDeath() {

            if (snakeX.get(0) < 0) return true;
            if (snakeX.get(0) >= GAME_BLOCK_WIDTH) return true;
            if (snakeY.get(0) < 0) return true;
            if (snakeY.get(0) >= GAME_BLOCK_HEIGHT) return true;

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

                blobx = random.nextInt(GAME_BLOCK_WIDTH - 2) + 1;
                bloby = random.nextInt(GAME_BLOCK_HEIGHT - 2) + 1;

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
                            break;
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

                // canvas.drawColor(Colors.BACKGROUNDNONPLAYAREA);

                paint.setColor(Color.RED);
                paint.setTextSize(50);

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
            canvas.drawRect(new Rect(paddingHorizontal, paddingVertical, paddingHorizontal + GAME_BLOCK_WIDTH * BLOCKSIZE
                    , paddingVertical + GAME_BLOCK_HEIGHT * BLOCKSIZE), paint);

        }

        private void blockPlayable(int x, int y, int color) {
            paint.setColor(color);
            canvas.drawRect(new Rect(paddingHorizontal + x * BLOCKSIZE, paddingVertical + y * BLOCKSIZE,
                    paddingHorizontal + x * BLOCKSIZE + BLOCKSIZE,
                    paddingVertical + y * BLOCKSIZE + BLOCKSIZE), paint);
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

        // TODO write new method for handling movement control


        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {

            int BUFFER = 60;

            // Ignore MotionEvent if it was not a push on the screen
            if(motionEvent.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }


            // Get the x and y coordinates of the finger when it was removed from the screen
            float x = motionEvent.getX();
            float y = motionEvent.getY();

            switch (direction) {

                case "up":
                case "down":
                   if (x < snakeX.get(0) * BLOCKSIZE - BUFFER) {
                       direction = "left";
                   } else if (x > snakeX.get(0) * BLOCKSIZE + BUFFER) {
                       direction = "right";
                   }
                   break;

                case "right":
                case "left":
                   if (y < snakeY.get(0) * BLOCKSIZE - BUFFER) {
                       direction = "up";
                   } else if (y > snakeY.get(0) * BLOCKSIZE + BUFFER) {
                       direction = "down";
                   }
                   break;
            }



            return true;
        }



        /*
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

        */


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