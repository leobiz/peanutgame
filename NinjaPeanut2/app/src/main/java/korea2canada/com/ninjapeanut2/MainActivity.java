package korea2canada.com.ninjapeanut2;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
implements Peanut.PeanutListener{
    public static final int MIN_ANIMATION_DELAY = 500;
    public static final int MAX_ANIMATION_DELAY = 1500;
    public static final int MIN_ANIMATION_DURATION = 1000;
    public static final int MAX_ANIMATION_DURATION = 8000;
    public static final int NUMBER_OF_PINS = 5;
    private static final int PEANUTS_PER_LEVEL = 3;

    private ViewGroup mContentView;
    private int[] mPeanutColors = new int[3];
    private int mNextColor, mScreenWidth, mScreenHeight;
    private int mLevel;
    private int mScore, mPinsUsed;
    TextView mScoreDisplay, mLevelDisplay;
    private List<ImageView> mPinImages = new ArrayList<>();
    private List<Peanut> mPeanuts = new ArrayList<>();
    private Button mGoButton;
    private boolean mPlaying;
    private boolean mGameStopped = true;
    private int mPeanutsPoped;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPeanutColors[0] = Color.argb(255, 255, 0, 0);
        mPeanutColors[1] = Color.argb(255, 0, 255, 0);
        mPeanutColors[2] = Color.argb(255, 0, 0, 255);

        //3. 이미지 등록 : 리사이즈 프로그램 사용 -> 백그라운드 적용
        getWindow().setBackgroundDrawableResource(R.drawable.seabackground);

//1. 전체화면
        mContentView = (ViewGroup) findViewById(R.id.activity_main);

        //for mScreenWidth, mScreenHeight
        setToFullScreen();

        // ViewTreeObserver 리스너 만듬 for 땅콩 appear
        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }


        //2. 버튼
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFullScreen();
            }
        });

        //31. Score
        mScoreDisplay = (TextView) findViewById(R.id.score_display);
        mLevelDisplay = (TextView) findViewById(R.id.level_display);

        //35. 핀 이미지를 리스트에 추가 : 피넛이 천장에 닿을때 적용
        mPinImages.add((ImageView) findViewById(R.id.pushpin1));
        mPinImages.add((ImageView) findViewById(R.id.pushpin2));
        mPinImages.add((ImageView) findViewById(R.id.pushpin3));
        mPinImages.add((ImageView) findViewById(R.id.pushpin4));
        mPinImages.add((ImageView) findViewById(R.id.pushpin5));
        mGoButton = (Button) findViewById(R.id.go_button);

        updateDisplay();

        //터치 테스트용으로 이미지 설치 후 지움..
//        mContentView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP ) {
//                    Peanut b = new Peanut(MainActivity.this, mPeanutColors[mNextColor], 100);
//                    b.setX(event.getX());
////                    b.setY(event.getY());
//                    b.setY(mScreenHeight);
//                    mContentView.addView(b);
//                    b.releasePeanut(mScreenHeight, 3000);
//
//                    if (mNextColor + 1 == mPeanutColors.length) {
//                        mNextColor = 0;
//                    } else {
//                        mNextColor++;
//                    }
//                }
//
//                return false;
//            }
//       });


    }
//    1.
    private void setToFullScreen() {
        //전체화면 템플릿에서 복사해둔 것 사용
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }
//맨뒤에 추가
    private void startGame() {
        setToFullScreen();
        mScore = 0;
        mLevel = 0;
        mPinsUsed =0;
        for (ImageView pin :
             mPinImages) {
            pin.setImageResource(R.drawable.pin);
        }
        mGameStopped = false;
        startLevel();
    }

    private void startLevel() {
        mLevel++;
        updateDisplay();
        PeanutLauncher launcher = new PeanutLauncher();
        launcher.execute(mLevel);
        mPlaying = true;
        mPeanutsPoped = 0;
        mGoButton.setText("Stop game");
    }

    private void finishLevel() {
        Toast.makeText(this, String.format("You finished level %d", mLevel), Toast.LENGTH_SHORT).show();
        mPlaying = false;
        mGoButton.setText(String.format("Start level %d", mLevel + 1));
    }

    public void goButtonClickHandler(View view) {

        if (mPlaying) {
            gameOver(false);
        } else if (mGameStopped){
            startGame();
        } else {
            startLevel();
        }

    }

    @Override
    public void popPeanut(Peanut peanut, boolean userTouch) {

        mPeanutsPoped++;

        mContentView.removeView(peanut);
        mPeanuts.remove(peanut);
        if (userTouch) {
            mScore++;
        } else {
            mPinsUsed++;
            if (mPinsUsed <= mPinImages.size()) {
                mPinImages.get(mPinsUsed -1)
                        .setImageResource(R.drawable.pin_off);
            }
            if (mPinsUsed == NUMBER_OF_PINS) {
                gameOver(true);
                return;
            } else {
                Toast.makeText(this, "Missed that one", Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();
        
        if (mPeanutsPoped == PEANUTS_PER_LEVEL) {
            finishLevel();
        }
    }

    private void gameOver(boolean b) {
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show();

        for (Peanut peanut :
             mPeanuts) {
            mContentView.removeView(peanut);  //스크린에서 땅콩 제, 애니메이션 중지
            peanut.setPopped(true);
        }
        mPeanuts.clear();
        mPlaying = false;
        mGameStopped = true;
        mGoButton.setText(R.string.start_game);
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }

    //깃에서 복사
    private class PeanutLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY,
                    (MAX_ANIMATION_DELAY - ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

            int peautsLaunched = 0;
            while (mPlaying && peautsLaunched < PEANUTS_PER_LEVEL) {

//              Get a random horizontal position for the next balloon
                Random random = new Random(new Date().getTime());

                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                peautsLaunched++;

//              Wait a random number of milliseconds before looping
                int delay = random.nextInt(minDelay) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchPeanut(xPosition);
        }

    }

    private void launchPeanut(int x) {

        Peanut peanut = new Peanut(this, mPeanutColors[mNextColor], 150);
        mPeanuts.add(peanut);

        if (mNextColor + 1 == mPeanutColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set peanut vertical position and dimensions, add to container
        peanut.setX(x);
        peanut.setY(mScreenHeight + peanut.getHeight());
        mContentView.addView(peanut);

//      Let 'er fly
        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));
        peanut.releasePeanut(mScreenHeight, duration);

    }
}
