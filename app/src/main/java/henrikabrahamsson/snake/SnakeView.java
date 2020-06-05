package henrikabrahamsson.snake;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends SurfaceView implements Runnable {


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

    MainInterface mainInterface;

    /**
     * Constructor for the view used for the actual snake game.
     * Passing MainInterface to reference textviews.
     */
    public SnakeView(Context context, MainInterface mainInterface) {

        // Super context to the SurfaceView
        super(context);

        // save mainInterface to reference it
        this.mainInterface = mainInterface;
        mainInterface.setGameView(this);

        // Set layoutparams
        this.setLayoutParams(new LinearLayout.LayoutParams(mainInterface.GAME_BLOCK_WIDTH * mainInterface.BLOCKSIZE, mainInterface.GAME_BLOCK_HEIGHT * mainInterface.BLOCKSIZE));

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
            if (updateRequired()) {
                update();
                draw();

                final String lastScore = mainInterface.scoreNumber.getText().toString();

                if (super.getHandler() != null) {
                    super.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mainInterface.scoreNumber.setText(String.valueOf(score));

                            if (lastScore != mainInterface.scoreNumber.getText().toString() && score % 5 == 0) {
                                YoYo.with(Techniques.Pulse).duration(500).repeat(2).playOn(mainInterface.scoreNumber);

                            }


                        }
                    });
                }


            }
        }
    }


    /**
     * Starts a new game, resetting the score and the snake. Also stores the highscore.
     */
    public void newGame() {
        spawnBlob();
        snakeX.clear();
        snakeY.clear();
        snakeX.add(mainInterface.GAME_BLOCK_WIDTH / 2);
        snakeY.add(mainInterface.GAME_BLOCK_HEIGHT / 2);
        direction = "";

        SharedPreferences preferences = getContext().getSharedPreferences("Preferences", 0);

        if (preferences.getInt("Highscore", -1) < score) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt("Highscore", score);
            editor.apply();

        }

        if (preferences.contains("Highscore")) {
            highscore = preferences.getInt("Highscore", -1);
        } else {
            highscore = 0;
        }

        snakeLength = 1;
        score = 0;
        scorelastframe = 0;
        MILLIS_PER_SEC = 2500;

        direction = "none";
        mainInterface.highScoreNumber.setText(String.valueOf(highscore));


    }

    /**
     * Updates the game, such as detecting if the snake eats a blob,
     * death and movement.
     */
    public void update() {

        if (snakeX.get(0) == blobx && snakeY.get(0) == bloby) {
            eatBlob();
        }

        if (detectDeath()) {
            newGame();
        }

        if (score != scorelastframe && score <= 20) {
            MILLIS_PER_SEC = 2500 - 50 * score;
        } else if (score != scorelastframe) {
            MILLIS_PER_SEC = 1500;
        }

        if (playing) {
            moveSnake();
        }

        scorelastframe = score;

    }

    /**
     * Returns true if the snake should now be dead, otherwise false.
     */
    public boolean detectDeath() {

        if (snakeX.get(0) < 0) return true;
        if (snakeX.get(0) >= mainInterface.GAME_BLOCK_WIDTH) return true;
        if (snakeY.get(0) < 0) return true;
        if (snakeY.get(0) >= mainInterface.GAME_BLOCK_HEIGHT) return true;

        if (snakeLength > 4) {
            for (int i = 4; i < snakeLength; i++) {
                if (snakeX.get(0).equals(snakeX.get(i)) && snakeY.get(0).equals(snakeY.get(i))) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * Resets the blob by removing it, updating the snake and spawning a new blob.
     */
    public void eatBlob() {
        snakeX.add(snakeLength, snakeX.get(snakeLength - 1));
        snakeY.add(snakeLength, snakeY.get(snakeLength - 1));
        snakeLength++;
        spawnBlob();
        score++;

        mainInterface.animateEatenApple(snakeX.get(0) * mainInterface.BLOCKSIZE, snakeY.get(0) * mainInterface.BLOCKSIZE + mainInterface.scorefield.getHeight());

    }

    /**
     * Updates the blob position to a new one, that's not inside the snake.
     */
    public void spawnBlob() {
        boolean blobReCalc;
        do {

            blobx = random.nextInt(mainInterface.GAME_BLOCK_WIDTH - 2) + 1;
            bloby = random.nextInt(mainInterface.GAME_BLOCK_HEIGHT - 2) + 1;

            blobReCalc = false;

            switch (direction) {
                case "up":
                    if (snakeX.get(0) == blobx && snakeY.get(0) > bloby) blobReCalc = true;
                    break;
                case "down":
                    if (snakeX.get(0) == blobx && snakeY.get(0) < bloby) blobReCalc = true;
                    break;
                case "left":
                    if (snakeY.get(0) == bloby && snakeX.get(0) > blobx) blobReCalc = true;
                    break;
                case "right":
                    if (snakeY.get(0) == bloby && snakeX.get(0) < blobx) blobReCalc = true;
                    break;
            }

            if (!blobReCalc) {
                for (int i = 0; i < snakeLength; i++) {
                    if (snakeX.get(i) == blobx && snakeY.get(i) == bloby) {
                        blobReCalc = true;
                        break;
                    }
                }
            }


        } while (blobReCalc);

    }

    /**
     * Moves the snake one step forward in the right direction.
     */
    public void moveSnake() {

        if (direction == "none") return;

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

    /**
     * Determines if it is time to update the frame.
     */
    public boolean updateRequired() {

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


    // ------------ DRAWING ----------

    public void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {

            // Setup
            canvas = ourHolder.lockCanvas(); // Lock canvas

            // canvas.drawColor(Colors.BACKGROUNDNONPLAYAREA);

            paint.setColor(Color.RED);
            paint.setTextSize(50);

            drawPlayArea();

            blockPlayable(blobx, bloby, Colors.APPLE);

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

        paint.setColor(Colors.BACKGROUNDPLAYAREA);
        canvas.drawRect(new Rect(paddingHorizontal, paddingVertical, paddingHorizontal + mainInterface.GAME_BLOCK_WIDTH * mainInterface.BLOCKSIZE
                , paddingVertical + mainInterface.GAME_BLOCK_HEIGHT * mainInterface.BLOCKSIZE), paint);

    }

    private void blockPlayable(int x, int y, int color) {
        paint.setColor(color);
        canvas.drawRect(new Rect(paddingHorizontal + x * mainInterface.BLOCKSIZE, paddingVertical + y * mainInterface.BLOCKSIZE,
                paddingHorizontal + x * mainInterface.BLOCKSIZE + mainInterface.BLOCKSIZE,
                paddingVertical + y * mainInterface.BLOCKSIZE + mainInterface.BLOCKSIZE), paint);
    }


    // --------- STATE RELATED ---------

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


    // ----------- TOUCH EVENTS -----------

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        int BUFFER = 60;

        // Ignore MotionEvent if it was not a push on the screen
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }


        // Get the x and y coordinates of the finger when it was removed from the screen
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (direction) {

            case "none":
                direction = "right";
                break;
            case "up":
            case "down":
                if (x < snakeX.get(0) * mainInterface.BLOCKSIZE - BUFFER) {
                    direction = "left";
                } else if (x > snakeX.get(0) * mainInterface.BLOCKSIZE + BUFFER) {
                    direction = "right";
                }
                break;

            case "right":
            case "left":
                if (y < snakeY.get(0) * mainInterface.BLOCKSIZE - BUFFER) {
                    direction = "up";
                } else if (y > snakeY.get(0) * mainInterface.BLOCKSIZE + BUFFER) {
                    direction = "down";
                }
                break;
        }


        return true;
    }


}
