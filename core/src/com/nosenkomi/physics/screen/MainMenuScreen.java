package com.nosenkomi.physics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.nosenkomi.physics.Constants;
import com.nosenkomi.physics.MyGdxGame;

public class MainMenuScreen implements Screen {

    final OrthographicCamera camera = new OrthographicCamera();
    Stage stage;

    MyGdxGame game;

    public MainMenuScreen(MyGdxGame game) {
        this.game = game;
        camera.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);
        ScreenViewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.batch);

        initStage();

    }

    private void initStage() {

        // used to style ui
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        Label label = new Label("Drop Physics", skin, "default");
        label.setAlignment(Align.center);
        label.setSize(500, 60);
        label.setFontScale(3f);
        label.setPosition((stage.getWidth() / 2 - label.getWidth() / 2), stage.getHeight() * 0.7f);
        stage.addActor(label);

        TextButton startGameButton = new TextButton("Start", skin);
        startGameButton.setSize(500, 60);
        startGameButton.getLabel().setFontScale(2.5f);
        startGameButton.setPosition((stage.getWidth() / 2 - startGameButton.getWidth() / 2), stage.getHeight() * 0.4f);
        stage.addActor(startGameButton);

        startGameButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.log("MainMenuScreen", "Touch detected");
                game.setScreen(new GameScreen(game));
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        ScreenUtils.clear(0f, 0f, 0.2f, 1f);

        stage.act(delta);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {

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

    }
}
