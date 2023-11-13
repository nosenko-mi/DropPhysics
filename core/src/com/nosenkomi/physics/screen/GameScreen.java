package com.nosenkomi.physics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.nosenkomi.physics.B2DBodyConstructor;
import com.nosenkomi.physics.BodyType;
import com.nosenkomi.physics.Constants;
import com.nosenkomi.physics.MyGdxGame;

public class GameScreen implements Screen {

    Sprite bucketSprite, raindropSprite;
    Texture bucketImg, raindropImg;

    Integer points = 0;
    boolean isGameOver = false;
    private MyGdxGame game;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private OrthographicCamera guiCamera;

    private Long lastDropTime = 0L;
    private Long dropIntervalTime = 1000000000L; // nano
    private int dropsGathered = 0;

    // gui
    private Stage stage;
    private TextButton buttonLeft, buttonRight;
    private Label labelPoints;

    // World
    // manages all physics entities just like Stage. World -> Stage
    private World world;
    private Box2DDebugRenderer b2dbr;

    // Game objects
    private Body bucket;

    private Array<Body> raindrops, raindropsToRemove;
    private Body bottomSensor;

    public GameScreen(final MyGdxGame game) {
        this.game = game;

        bucketImg = new Texture("bucket.png");
        bucketSprite = new Sprite(bucketImg);

        raindropImg = new Texture("drop.png");
        raindropSprite = new Sprite(raindropImg);

        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);

        guiCamera = new OrthographicCamera();
        guiCamera.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);
//        guiCamera = new OrthographicCamera(Constants.WIDTH, Constants.HUD_HEIGHT);

        b2dbr = new Box2DDebugRenderer();
    }

    @Override
    public void show() {
        world = new World(new Vector2(-3f, -9.8f), false);

        ExtendViewport viewport = new ExtendViewport(Constants.WIDTH, Constants.HEIGHT, guiCamera);
        // Don't specify batch ->
        // The stage will use its own Batch which will be disposed when the stage is disposed.
        stage = new Stage(viewport);

        initStage();
        initGameLevel();
        setContactListener();

        game.batch.setProjectionMatrix(camera.combined);
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        update(Gdx.graphics.getDeltaTime());
        ScreenUtils.clear(0, 0, 0.2f, 1);

        b2dbr.render(world, camera.combined.scl(Constants.PPM));

        stage.draw();
        game.batch.begin();
        game.batch.draw(bucketSprite, bucketSprite.getX(), bucketSprite.getY());
        for (Body raindrop : raindrops) {
            game.batch.draw(raindropSprite,
                    raindrop.getPosition().x * Constants.PPM - Constants.RAINDROP_WIDTH,
                    raindrop.getPosition().y * Constants.PPM - Constants.RAINDROP_HEIGHT);
        }
        game.batch.end();

        labelPoints.setText(points);

    }

    @Override
    public void resize(int width, int height) {
//        camera.setToOrtho(false, width, height);
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
        System.out.println("dispose");
        dropSound.dispose();
        rainMusic.dispose();
        bucketImg.dispose();
        raindropImg.dispose();
        stage.dispose();
        world.dispose();
        b2dbr.dispose();
    }

    private void initGameLevel() {
        createWalls();

        raindrops = new Array<>();
        raindropsToRemove = new Array<>();

        bucket = B2DBodyConstructor.createBox(
                world,
                camera.viewportWidth / 2,
                Constants.HUD_HEIGHT + 1,
                Constants.BUCKET_WIDTH,
                Constants.BUCKET_HEIGHT,
                false
        );
        bucket.setUserData(BodyType.BUCKET);

        bucketSprite.setPosition(
                bucket.getPosition().x * Constants.PPM - Constants.BUCKET_WIDTH / 2,
                bucket.getPosition().y * Constants.PPM - Constants.BUCKET_HEIGHT / 2);

        bottomSensor = B2DBodyConstructor.createSensor(
                world,
                0,
                (Constants.HUD_HEIGHT + Constants.BUCKET_HEIGHT - 4),
                (int) (camera.viewportWidth * Constants.PPM),
                1,
                true
        );
        bottomSensor.setUserData(BodyType.SENSOR);

        spawnRaindrop();
    }

    private void update(float delta) {

        removeCollidedRaindrops();

        world.step(1 / 60f, 6, 2);
        stage.act();

        inputUpdate(delta);
        bucketSprite.setPosition(
                bucket.getPosition().x * Constants.PPM - Constants.BUCKET_WIDTH / 2,
                bucket.getPosition().y * Constants.PPM - Constants.BUCKET_HEIGHT / 2);

        if (TimeUtils.nanoTime() - lastDropTime > dropIntervalTime) spawnRaindrop();

        cameraUpdate(delta);
        checkIfGameOver();
    }

    private void checkIfGameOver() {
        if(isGameOver)
        {
            Array<Body> bodies = new Array<Body>();
            world.getBodies(bodies);
            for(int i = 0; i < bodies.size; i++)
            {
                if(!world.isLocked())
                    world.destroyBody(bodies.get(i));
            }
            isGameOver = false;
            dispose();
        }
    }


    private void setContactListener() {

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                Body a = contact.getFixtureA().getBody();
                Body b = contact.getFixtureB().getBody();

                if((a.getUserData() == BodyType.BUCKET && b.getUserData() == BodyType.RAINDROP )) {
                    removeRaindrop(b);
                    dropSound.play();
                    points++;
                }
                if((b.getUserData() == BodyType.BUCKET && a.getUserData() == BodyType.RAINDROP )) {
                    removeRaindrop(a);
                    dropSound.play();
                    points++;
                }
                if(
                        (a.getUserData() == BodyType.SENSOR && b.getUserData() == BodyType.RAINDROP ) ||
                        (b.getUserData() == BodyType.SENSOR && a.getUserData() == BodyType.RAINDROP )
                ) {
                    stopGame();
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private void cameraUpdate(float delta) {
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    private void inputUpdate(float delta){
        int horizontalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || buttonLeft.isPressed()){
            horizontalForce -= 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || buttonRight.isPressed()){
            horizontalForce += 2;
        }

        bucket.setLinearVelocity(horizontalForce * 7, bucket.getLinearVelocity().y);
    }

    private void checkBucketPosition(){
        if (bucket.getPosition().x < 0) {
            bucket.getPosition().set(0 + Constants.BUCKET_WIDTH / 2, bucket.getPosition().y);
        }
        if (bucket.getPosition().x > Constants.WIDTH - Constants.BUCKET_WIDTH) {
            bucket.getPosition().set(Constants.WIDTH - Constants.BUCKET_WIDTH / 2, bucket.getPosition().y);
        }
    }

    private void createWalls(){

        Body body;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        body = world.createBody(bodyDef);

        ChainShape shape = new ChainShape();
        Vector2[] verts = new Vector2[5];

        verts[0] = new Vector2(1 / Constants.PPM, Constants.HUD_HEIGHT / Constants.PPM); // bottom left
        verts[1] = new Vector2(camera.viewportWidth / Constants.PPM,Constants.HUD_HEIGHT / Constants.PPM); // bottom right
        verts[2] = new Vector2(camera.viewportWidth / Constants.PPM, (camera.viewportHeight - Constants.HUD_HEIGHT / 2) / Constants.PPM); // top right
        verts[3] = new Vector2(1 / Constants.PPM, (camera.viewportHeight - Constants.HUD_HEIGHT / 2) / Constants.PPM); // top left
        verts[4] = new Vector2(1 / Constants.PPM, Constants.HUD_HEIGHT / Constants.PPM); // close bottom left

        shape.createChain(verts);

        body.createFixture(shape, 1f);
        shape.dispose();

    }

    private void initStage() {

        // used to style ui
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        labelPoints = new Label( points.toString(), skin, "default");
        labelPoints.setAlignment(Align.center);
        labelPoints.setSize(500, 30);
        labelPoints.setFontScale(2f);
        labelPoints.setPosition((stage.getWidth() / 2 - labelPoints.getWidth() / 2), stage.getHeight() - (Constants.HUD_HEIGHT / 2));
        stage.addActor(labelPoints);

        buttonLeft = new TextButton("<", skin);
        buttonLeft.setSize(Constants.BUCKET_WIDTH, Constants.BUTTON_HEIGHT);
        buttonLeft.getLabel().setFontScale(2.5f);
        buttonLeft.setPosition(
                stage.getWidth() * 0.25f - Constants.BUTTON_WIDTH / 2,
                (Constants.HUD_HEIGHT / 2) - (Constants.BUTTON_HEIGHT / 4));

        buttonRight = new TextButton(">", skin);
        buttonRight.setSize(Constants.BUCKET_WIDTH, Constants.BUTTON_HEIGHT);
        buttonRight.getLabel().setFontScale(2.5f);
        buttonRight.setPosition(
                stage.getWidth() * 0.75f - Constants.BUTTON_WIDTH / 2,
                (Constants.HUD_HEIGHT / 2) - (Constants.BUTTON_HEIGHT / 4));

        stage.addActor(buttonLeft);
        stage.addActor(buttonRight);

        Gdx.input.setInputProcessor(stage);
    }

    private void removeRaindrop(Body body){
        raindropsToRemove.add(body);
    }

    private void removeCollidedRaindrops(){
        for(Body body : raindropsToRemove){
            raindrops.removeValue(body,true);
            world.destroyBody(body);
        }
        raindropsToRemove.clear();
    }

    private void spawnRaindrop(){
        float x = MathUtils.random(0f, camera.viewportWidth - (Constants.BUCKET_WIDTH * 2) / Constants.PPM);
        float y = camera.viewportHeight - Constants.HUD_HEIGHT;

        Body raindrop = B2DBodyConstructor.createBox(world, x, y, Constants.RAINDROP_WIDTH, Constants.RAINDROP_HEIGHT, false);
        raindrop.setUserData(BodyType.RAINDROP);
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();

        if (dropIntervalTime > 400000000){
            dropIntervalTime -= 10000000;
        }
    }

    private void stopGame() {
        isGameOver = true;
        game.setScreen(new GameOverScreen(game, points));
    }
}
