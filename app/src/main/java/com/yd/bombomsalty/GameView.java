package com.yd.bombomsalty;

/**
 * Created on 31.01.2017
 @author Yury.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;

public class GameView extends SurfaceView
    //implements Runnable
{

    private static final String LOG_TAG = "GameView";

    /** simple PI number */
    private static final double PI = Math.PI;
    /** multiply to convert to radians */
    private static final double RAD = PI / 180;

    private int time = 0;
    private int score = 0;
    private int health = 3;

    /** screen stuff */
    private float screenWidth;
    private float screenHeight;

    private Player player;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Boom> booms = new ArrayList<>();

    Paint paint = new Paint();

    float controlR = 0;
    float enemyX = 0;
    float enemyY = 0;
    float xSpeed = 0;
    float ySpeed = 0;

    //bitmaps
    private Bitmap playerBM;
    private Bitmap turretBM;
    private Bitmap bgBM;
    private Bitmap projBM;
    private Bitmap rulerBM;
    private Bitmap enemyBM;
    private Bitmap bladeBM;

    private SurfaceHolder holder;

   //game looper
    private GameLooper gameLoopThread;

    //coordinates and angles
    private float x = 0;
    private float y = 0;
    private int playerA = 0;
    private int turretA = 0;

    private int speed = 0;

    public GameView(Context context)
    {
        super(context);
        gameLoopThread = new GameLooper(this);
        player = new Player();

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            // Destroying surface
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                gameLoopThread.setRunning(false);
                while (true) {
                    try {
                        gameLoopThread.join();
                        return;
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "surfaceDestroyed" + e.getMessage());
                    }
                }
            }

            // Creating surface
            public void surfaceCreated(SurfaceHolder holder)
            {
                if (!gameLoopThread.getRunning()){
                    gameLoopThread.setRunning(true);
                    gameLoopThread.start();
                }
            }

            // Changing surface
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
            }
        });

        playerBM = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        turretBM = BitmapFactory.decodeResource(getResources(), R.drawable.turret);
        bgBM = BitmapFactory.decodeResource(getResources(), R.drawable.back);
        projBM = BitmapFactory.decodeResource(getResources(), R.drawable.projectile);
        rulerBM = BitmapFactory.decodeResource(getResources(), R.drawable.ruler);
        enemyBM = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
        bladeBM = BitmapFactory.decodeResource(getResources(), R.drawable.blade);
    }

    public GameLooper getGameLooper(){
        if(gameLoopThread != null){
            return gameLoopThread;
        }
        return null;
    }

    protected void updateProjectiles(){
        //Update all projectiles
        ArrayList projectiles = player.getProjectiles();
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = (Projectile) projectiles.get(i);
            if ((p.getX() > 0) || (p.getY() > 0)) {
                p.update();
            }
        }
    }

    protected void updatePlayer(){
        player.update(screenWidth / 2, screenHeight / 2, turretA, playerA);
    }

    protected void updateBackground(){

        float screenWidthHalf = screenWidth / 2;
        float screenHeightHalf = screenHeight / 2;

        if (((x > bgBM.getWidth() * screenWidth / 1600 + screenWidthHalf) && (((float) Math.cos((float) playerA * RAD)) < 0))
                ||
                ((x < -bgBM.getWidth() * screenWidth / 1600 + screenWidthHalf) && (((float) Math.cos((float) playerA * RAD)) > 0))) {
            xSpeed = 0;
        } else {
            x = x - speed * (float) Math.cos((float) playerA * RAD);
            xSpeed = speed * (float) Math.cos((float) playerA * RAD);
        }

        if (((y > bgBM.getHeight() * screenWidth / 1600 + screenHeightHalf) && (((float) Math.sin((float) playerA * RAD)) < 0))
                ||
                ((y < -bgBM.getHeight() * screenWidth / 1600 + screenHeightHalf) && (((float) Math.sin((float) playerA * RAD)) > 0))) {
            ySpeed = 0;
        } else {
            y = y - speed * (float) Math.sin((float) playerA * RAD);
            ySpeed = speed * (float) Math.sin((float) playerA * RAD);
        }
    }

    protected void updateEnemies(){

        float screenWidthHalf = screenWidth/ 2;
        float screenHeightHalf = screenHeight / 2;

        ArrayList projectiles = player.getProjectiles();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemyX = enemy.getX();
            enemyY = enemy.getY();

            boolean killed = false;

            for (int j = 0; j < projectiles.size(); j++) {
                Projectile p = (Projectile) projectiles.get(j);

                //if projectile near enemy - kill them both
                if ((p.getX() - enemyX) * (p.getX() - enemyX) + (p.getY() - enemyY) * (p.getY() - enemyY) < 10000) {
                    //drawImage(canvas, projBM, enemyX, enemyY, enemy.getA(), screenWidth / 700);
                    enemies.remove(i);
                    projectiles.remove(j);

                    //big bada-BOOM
                    Boom b = new Boom(enemyX, enemyY);
                    booms.add(b);
                    score++;

                    killed = true;
                    if (Math.random() > 0.3) {
                        enemyY = screenHeight * (float) Math.random();
                        enemyX = screenWidth * (float) Math.random();
                        if (((enemyX - screenWidthHalf) * (enemyX - screenWidthHalf) + (enemyY - screenHeightHalf) * (enemyY - screenHeightHalf)) > 8000) {
                            Enemy ne = new Enemy(enemyX, enemyY, 40, 0, player);
                            enemies.add(ne);
                        }
                    }
                }
            }
            //if not killed -> update position
            if (!killed) {
                enemy.update(-xSpeed, -ySpeed, screenWidthHalf, screenHeightHalf);
            }
        }
    }

    protected void updateBooms(){
        //update booms data
        for (int i = 0; i < booms.size(); i++) {
            Boom b = booms.get(i);
            float speedBoomX = -speed * (float) Math.cos((float) playerA * RAD);
            float speedBoomY = -speed * (float) Math.sin((float) playerA * RAD);
            b.update(speedBoomX, speedBoomY);
        }
    }

    protected void updateBlades(){

        float screenWidthHalf = screenWidth/ 2;
        float screenHeightHalf = screenHeight / 2;

        //update blades data
        ArrayList blades = player.getBlades();
        for (int i = 0; i < blades.size(); i++) {
            Blade blade = (Blade) blades.get(i);
            float bX = blade.getX();
            float bY = blade.getY();

            //if we've been hit by blade
            if (((bX - screenWidthHalf) * (bX - screenWidthHalf) + (bY - screenHeightHalf) * (bY - screenHeightHalf)) < 1000) {
                blades.remove(i);
                health -= 1;
            } else if (bX > 0 || bY > 0) {
                blade.update((-speed * (float) Math.cos((float) playerA * RAD)), -speed * (float) Math.sin((float) playerA * RAD));
            } else {
                blades.remove(i);
            }
        }
    }

    protected void createEnemy(){

        float screenWidthHalf = screenWidth/ 2;
        float screenHeightHalf = screenHeight / 2;

        //new enemy
        if (Math.random() > 0.95) {
            enemyY = screenHeight * (float) Math.random();
            enemyX = screenWidth * (float) Math.random();
            if (((enemyX - screenWidthHalf) * (enemyX - screenWidthHalf) + (enemyY - screenHeightHalf) * (enemyY - screenHeightHalf)) > 8000) {
                Enemy e = new Enemy(enemyX, enemyY, 40, 0, player);
                enemies.add(e);
            }
        }
    }

    protected void onMainDraw(Canvas canvas)
    {
        float screenWidthHalf = screenWidth/ 2;
        float screenHeightHalf = screenHeight / 2;

        //it's alive...
        if (health > 0) {

            updateProjectiles();

            updatePlayer();

            updateBackground();

            updateEnemies();

            updateBooms();

            updateBlades();

            createEnemy();

            ArrayList projectiles = player.getProjectiles();
            ArrayList blades = player.getBlades();

            screenWidth = getWidth();
            screenHeight = getHeight();
            screenWidthHalf = screenWidth/ 2;   //update values
            screenHeightHalf = screenHeight / 2;    //update values
            controlR = rulerBM.getWidth() * screenWidth / 8000;

            time++;

            //drawing
            canvas.drawColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            canvas.drawPaint(paint);
            drawImage(canvas, bgBM, x, y, 0, screenWidth / 800);

            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                drawImage(canvas, enemyBM, e.getX(), e.getY(), e.getA(), screenWidth / 7000);
            }

            drawImage(canvas, rulerBM, screenWidth - 100, screenHeight - 100, 0, screenWidth / 3000);
            //drawImage(canvas, rulerBM, 100, screenHeight - 100, 0, screenWidth / 3000);
            drawImage(canvas, playerBM, screenWidthHalf, screenHeightHalf, playerA, screenWidth / 5000);

            for (int i = 0; i < projectiles.size(); i++) {
                Projectile p = (Projectile) projectiles.get(i);
                drawImage(canvas, projBM, p.getX(), p.getY(), 0, screenWidth / 7000);
            }

            for (int i = 0; i < booms.size(); i++) {
                Boom b = booms.get(i);
                if (b.mBeen < 10) {
                    float scalingFactor = (float) b.mBeen / 4 + (screenWidth / 4000);
                    drawImage(canvas, projBM, b.getX(), b.getY(), 0, scalingFactor);
                } else {
                    booms.remove(i);
                }
            }

            for (int i = 0; i < blades.size(); i++) {
                Blade b = (Blade) blades.get(i);
                drawImage(canvas, bladeBM, b.getX(), b.getY(), 0, screenWidth / 2600);
            }


            drawImage(canvas, turretBM, screenWidthHalf, screenHeightHalf, turretA, screenWidth / 5000);
            paint.setTextSize(40);
            paint.setColor(Color.WHITE);

            String yourScoreText = getResources().getString(R.string.score);
            canvas.drawText(yourScoreText + score, screenWidthHalf - paint.measureText(yourScoreText)/2, 40, paint);

            //draw circles of lives
            for (int i = health; i > 0; i--){
                paint.setColor(Color.RED);
                canvas.drawCircle(200 - i * 40, screenHeight - 100, 20, paint);
            }

        } else {
            //you died
            canvas.drawColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(100);

            String youDied = getResources().getString(R.string.dead);
            canvas.drawText(youDied, screenWidthHalf - paint.measureText(youDied) / 2, screenHeightHalf, paint);
        }
    }

    public void drawImage(final Canvas canvas, final Bitmap bitmap, final float x, final float y, final int rotationAngle, final float scalingFactor){
        Matrix matrix = new Matrix();
        int bitmapWidthHalf = bitmap.getWidth() / 2;
        int bitmapHeightHalf = bitmap.getHeight() / 2;

        matrix.postRotate(rotationAngle, bitmapWidthHalf, bitmapHeightHalf);
        matrix.postScale(scalingFactor, scalingFactor, bitmapWidthHalf, bitmapHeightHalf);
        matrix.postTranslate(x - bitmapWidthHalf, y - bitmapHeightHalf);

        canvas.drawBitmap(bitmap, matrix, null);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        float eventX = event.getX(),
                eventY = event.getY();

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (health <= 0) {
                    enemies.clear();
                    player.clear();
                    booms.clear();
                    health = 3;
                    score = 0;
                }
                break;
            default:
                break;
        }

        float tmpPos = (eventX - screenWidth + 100) * (eventX - screenWidth + 100) + (eventY - screenHeight + 100) * (eventY - screenHeight + 100);
        if (tmpPos < controlR * controlR + 4000) {
            switch (eventAction) {
                case MotionEvent.ACTION_DOWN:
                    speed = 30;
                    break;

                case MotionEvent.ACTION_UP:
                    speed = 0;
                    break;

                default:
                    break;
            }

            playerA = (int) (Math.atan2(eventY - screenHeight + 100, eventX - screenWidth + 100) / RAD);
        }
        /*
        if (((event.getX() - 100)*(event.getX() - 100) + (event.getY() - screenHeight + 100)*(event.getY() - screenHeight + 100)) < controlR*controlR + 4000) {
            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:

                    player.setFiring(true);
                    break;

                case MotionEvent.ACTION_UP:
                    //player.setFiring(false);
                    break;
            }
        */
        turretA = playerA;
        //turretA = (int)(Math.atan2(event.getY()  - screenHeight + 100, event.getX() - 100) / RAD);

        return true;
    }


    /** resume game, new thread */
    public void resume(){

        if (gameLoopThread == null) {
            gameLoopThread = new GameLooper(this);
        }
        gameLoopThread.setRunning(true);
        gameLoopThread.start();
    }

    /** simply terminates the rendering/main loop */
    public void pause(){

        gameLoopThread.setRunning(false);

        while (true){
            try{
                gameLoopThread.join();
                return;
            } catch (InterruptedException e){
                //retry until we win
            }
        }
    }
}