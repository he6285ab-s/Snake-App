package henrikabrahamsson.snake;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainInterface extends LinearLayout {

    LinearLayout scorefield;
    TextView highScoreText;
    TextView scoreText;
    TextView highScoreNumber;
    TextView scoreNumber;
    Button startButton;
    SnakeView snakeView;

    int GAME_PX_HEIGHT;
    int GAME_PX_WIDTH;
    int BLOCKSIZE;
    int GAME_BLOCK_WIDTH;
    int GAME_BLOCK_HEIGHT;


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


    }

    public void setGameView(SnakeView snakeView) {
        this.snakeView = snakeView;
        this.addView(snakeView, 1);
    }

    public void animateEatenApple(int x, int y) {

        View apple = new View(getContext());
        apple.setLayoutParams(new LayoutParams(BLOCKSIZE, BLOCKSIZE));
        apple.setBackgroundColor(Colors.APPLE);
        apple.setX(x);
        apple.setY(y);

        Path path = new Path();
        path.lineTo(scoreNumber.getX(), scoreNumber.getY());

        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(apple, View.X, View.Y, path);
        objectAnimator.setDuration(1000);

        this.addView(apple);

        super.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "running animation", Toast.LENGTH_LONG).show();
                objectAnimator.start();
            }
        });

        this.removeView(apple);





    }


}
