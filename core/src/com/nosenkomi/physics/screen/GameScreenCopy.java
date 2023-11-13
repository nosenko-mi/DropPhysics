package com.nosenkomi.physics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.nosenkomi.physics.Constants;
import com.nosenkomi.physics.MyGdxGame;

public class GameScreenCopy implements Screen {

    private MyGdxGame game;
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;

    private Rectangle bucket;

    private Array<Rectangle> raindrops;

    private Long lastDropTime = 0L;
    private Long dropIntervalTime = 1000000000L; // nano
    private int dropsGathered = 0;

    // manages all physics entities just like Stage. World -> Stage
    private World world;

    // Body -> Actor
    private Body bucketB;
    private Box2DDebugRenderer b2dbr;

    public GameScreenCopy(final MyGdxGame game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("drop.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);
//        Vector3 position = camera.position;
//        position.x = (int) (Constants.HEIGHT / 2 - 64 / 2) * Constants.PPM;
//        position.y = 20 * Constants.PPM;
//        camera.position.set(position);
//        System.out.println("[constructor] Camera position: x=" + position.x + " y=" + position.y + " z=" + position.z); // x=23552.0 y=1280.0
//        camera.update();


        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = (int) (Constants.HEIGHT / 2 - 64 / 2); // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
        // the bottom screen edge
        bucket.width = Constants.BUCKET_WIDTH;
        bucket.height = Constants.BUCKET_HEIGHT;

        // create the raindrops array and spawn the first raindrop
        raindrops = new Array<Rectangle>();
        spawnRaindrop();

        // World setup

        world = new World(new Vector2(0, 0), false);
//        world = new World(new Vector2(0, -9.8f), false);
        b2dbr = new Box2DDebugRenderer();
        bucketB = createBox();
    }

    private Body createBox() {
        Body pBody;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
                (int) (Constants.HEIGHT / 2 - 64 / 2),
                20);
        bodyDef.fixedRotation = true; // prevent from rotation

        pBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(Constants.BUCKET_WIDTH / 2 / Constants.PPM, Constants.HEIGHT / 2 / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.shape = shape;
//        fixtureDef.friction = 0.5f;
//        fixtureDef.restitution = 0.3f;

        pBody.createFixture(fixtureDef);

        shape.dispose();
        return pBody;

    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = (int) MathUtils.random(0f, Constants.WIDTH - 128f);
        raindrop.y = Constants.HEIGHT;
        raindrop.width = Constants.RAINDROP_WIDTH;
        raindrop.height = Constants.RAINDROP_HEIGHT;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();

        if (dropIntervalTime > 400000000) {
            dropIntervalTime -= 10000000;
        }

    }

    private void handleKeyPressed() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
    }

    private void handleTouch() {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = (int) (touchPos.x - 64 / 2);
        }
    }

    private void checkBucketPosition() {
        if (bucket.x < 0) bucket.x = 0;

        if (bucket.x > Constants.HEIGHT - Constants.BUCKET_HEIGHT) {
            bucket.x = (Constants.HEIGHT - Constants.BUCKET_HEIGHT);
        }
    }

    private void stopGame() {
//        game.screen = GameOverScreen(game, dropsGathered)
//        game.setScreen(new GameOverScreen());
        dispose();
    }

    private void update(float delta) {
        world.step(1 / 60f, 6, 2);

        updateCamera();
    }

    private void updateCamera() {
        Vector3 position = camera.position;
        position.x = bucketB.getPosition().x * Constants.PPM;
        position.y = bucketB.getPosition().y * Constants.PPM;
        camera.position.set(position);
        System.out.println("[updateCamera] Camera position: x=" + position.x + " y=" + position.y + " z=" + position.z); // x=23552.0 y=1280.0
        camera.update();
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        update(Gdx.graphics.getDeltaTime());
        ScreenUtils.clear(0, 0, 0.2f, 1);

        b2dbr.render(world, camera.combined.scl(Constants.PPM));

//        // tell the camera to update its matrices.
//        camera.update();
//
//        // tell the SpriteBatch to render in the
//        // coordinate system specified by the camera.
//        game.batch.setProjectionMatrix(camera.combined);
//
//        // begin a new batch and draw the bucket and
//        // all drops
//        game.batch.begin();
//        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
//        game.batch.draw(bucketImage, bucket.x, bucket.y, bucket.width, bucket.height);
//        for (Rectangle raindrop : raindrops) {
//            game.batch.draw(dropImage, raindrop.x, raindrop.y);
//        }
//        game.batch.end();
//
//        // process user input
//        handleTouch();
//        handleKeyPressed();
//
//        // make sure the bucket stays within the screen bounds
//        checkBucketPosition();
//
//        // check if we need to create a new raindrop
//        if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
//            spawnRaindrop();
//
//        // move the raindrops, remove any that are beneath the bottom edge of
//        // the screen or that hit the bucket. In the later case we increase the
//        // value our drops counter and add a sound effect.
//        Iterator<Rectangle> iter = raindrops.iterator();
//        while (iter.hasNext()) {
//            Rectangle raindrop = iter.next();
//            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
//            if (raindrop.y + 64 < 0)
//                iter.remove();
//            if (raindrop.overlaps(bucket)) {
//                dropsGathered++;
//                dropSound.play();
//                iter.remove();
//            }
//        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();

        world.dispose();
        b2dbr.dispose();
    }
}
