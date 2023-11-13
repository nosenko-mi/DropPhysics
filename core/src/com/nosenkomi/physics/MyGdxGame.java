package com.nosenkomi.physics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.nosenkomi.physics.screen.MainMenuScreen;

public class MyGdxGame extends Game {
	public SpriteBatch batch;
	public BitmapFont font;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont(); // use libGDX's default Arial font
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render() {
		//  Without this call, the Screen that you set in the create() method will not be rendered
		//  if you override the render method in your Game class!
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		this.getScreen().dispose();
	}
}
