package henrikabrahamsson.snake;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class MainInterface extends LinearLayout {

    LinearLayout scorefield;
    TextView highScoreText;
    TextView scoreText;
    TextView highScoreNumber;
    TextView scoreNumber;
    Button startButton;
    SnakeView snakeView;
    View apple;
    int GAME_PX_HEIGHT;
    int GAME_PX_WIDTH;
    int BLOCKSIZE;
    int GAME_BLOCK_WIDTH;
    int GAME_BLOCK_HEIGHT;
    Handler mainHandler;



    public MainInterface(Context context, int width, int height) {
        super(context);

        // SnakeView area calculations
        GAME_PX_WIDTH = width;
        GAME_BLOCK_WIDTH = 20;
        BLOCKSIZE = GAME_PX_WIDTH / GAME_BLOCK_WIDTH;
        GAME_PX_HEIGHT = height * 2 / 3;
        GAME_BLOCK_HEIGHT = GAME_PX_HEIGHT / BLOCKSIZE;

        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER);


        // TOP LAYOUT
        scorefield = new LinearLayout(context);

        // Pure text
        highScoreText = new TextView(context);
        scoreText = new TextView(context);

        // Actual score
        highScoreNumber = new TextView(context);
        scoreNumber = new TextView(context);

        LinearLayout.LayoutParams scoreFieldParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams textparams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                150,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        scoreFieldParams.setMargins(5, 5, 5, 20);
        textparams.setMargins(20, 20, 20, 20);
        scoreParams.setMargins(20, 20, 20, 20);

        scorefield.setLayoutParams(scoreFieldParams);
        scorefield.setOrientation(LinearLayout.HORIZONTAL);
        scorefield.setGravity(Gravity.CENTER);
        highScoreText.setLayoutParams(textparams);
        scoreText.setLayoutParams(textparams);
        highScoreNumber.setLayoutParams(scoreParams);
        scoreNumber.setLayoutParams(scoreParams);

        Typeface scoreTextTypeFace = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        Typeface scoreTypeFace = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);

        highScoreText.setTypeface(scoreTextTypeFace);
        highScoreText.setTextColor(Color.WHITE);
        highScoreText.setText("High score:");

        scoreText.setTypeface(scoreTextTypeFace);
        scoreText.setTextColor(Color.WHITE);
        scoreText.setText("Score:");

        highScoreNumber.setTypeface(scoreTypeFace);
        highScoreNumber.setTextColor(Color.YELLOW);
        highScoreNumber.setTextSize(30);
        highScoreNumber.setGravity(Gravity.CENTER);

        scoreNumber.setTypeface(scoreTypeFace);
        scoreNumber.setTextColor(Color.YELLOW);
        scoreNumber.setTextSize(30);
        scoreNumber.setGravity(Gravity.CENTER);
        scoreNumber.setText("0");

        scorefield.addView(highScoreText);
        scorefield.addView(highScoreNumber);
        scorefield.addView(scoreText);
        scorefield.addView(scoreNumber);

        LinearLayout buttonField = new LinearLayout(context);
        buttonField.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttonField.setOrientation(HORIZONTAL);
        buttonField.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(240, 240);
        buttonParams.setMargins(30, 70, 30, 0);

        startButton = new Button(context);
        startButton.setLayoutParams(buttonParams);
        startButton.setBackgroundResource(R.drawable.ic_play_circle_outline_white_24dp);



        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(snakeView.playing) {
                    snakeView.pause();
                    startButton.setBackgroundResource(R.drawable.ic_play_circle_outline_white_24dp);
                } else {
                    snakeView.resume();
                    startButton.setBackgroundResource(R.drawable.ic_pause_circle_outline_white_24dp);
                }
            }
        });


        this.addView(scorefield);
        this.addView(startButton);

        this.setBackgroundColor(Colors.BACKGROUNDNONPLAYAREA);

        mainHandler = new Handler(Looper.getMainLooper());

        apple = new View(context);
        apple.setBackgroundColor(Colors.APPLE);
        apple.setVisibility(INVISIBLE);
        getOverlay().add(apple);


    }

    public void setGameView(SnakeView snakeView) {
        this.snakeView = snakeView;
        this.addView(snakeView, 1);
    }

    public void animateEatenApple(final int x, int y) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {

                int x = snakeView.snakeX.get(0);
                int y = snakeView.snakeY.get(0);

                apple.layout(x, y, x + BLOCKSIZE, y + BLOCKSIZE);

                apple.setX(x * BLOCKSIZE);
                apple.setY(y * BLOCKSIZE + scorefield.getHeight());

                apple.setVisibility(VISIBLE);

                Log.d("x", "" + apple.getX());

                apple.animate().x(scoreNumber.getX() + (float) scoreNumber.getWidth() / 2).y(scoreNumber.getY() + (float) scoreNumber.getHeight());
                apple.animate().setDuration(1000);
                apple.animate().start();

                apple.animate().setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        apple.setVisibility(INVISIBLE);

                        scoreNumber.setText(Integer.toString(snakeView.score));
                        if(snakeView.score % 5 == 0) {
                            YoYo.with(Techniques.Pulse).duration(500).repeat(2).playOn(scoreNumber);
                        }


                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });



            }
        });


    }


    public void setButtonToPlayingState() {
        startButton.setBackgroundResource(R.drawable.ic_pause_circle_outline_white_24dp);
    }

    public void resetScore() {
        scoreNumber.setText(Integer.toString(snakeView.score));
    }

}
