package henrikabrahamsson.snake;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainInterface extends LinearLayout {

    LinearLayout topLayout;
    TextView HighScoreTextView;
    TextView ScoreTextView;
    TextView HS;
    TextView S;
    Button startButton;

    int GAME_PX_HEIGHT;
    int GAME_PX_WIDTH;
    int BLOCKSIZE;
    int GAME_BLOCK_WIDTH;
    int GAME_BLOCK_HEIGHT;


    public MainInterface(Context context, int width, int height)  {
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
        topLayout = new LinearLayout(context);

        // Pure text
        HighScoreTextView = new TextView(context);
        ScoreTextView = new TextView(context);

        // Actual score
        HS = new TextView(context);
        S = new TextView(context);

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

        startButton = new Button(context);
        startButton.setLayoutParams(scoreTextParams);
        startButton.setText("Click here to start playing!");

        this.addView(topLayout);
        this.addView(startButton);

        this.setBackgroundColor(Color.BLUE);


    }

    public void setGameView(SnakeView snakeView) {
        this.addView(snakeView, 1);
    }





}
