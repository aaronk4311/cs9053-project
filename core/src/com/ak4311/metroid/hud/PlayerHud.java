package com.ak4311.metroid.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.entity.Samus;
import com.ak4311.metroid.utility.AttributeListener;

public class PlayerHud implements Disposable {
	private Samus samus;
	private float x;
	private float y;
	
	private Stage stage;
	private Viewport viewport;
	
	private Texture texture;
	private Actor healthTextActor;
	private Actor missilesTextActor;
	
	/**
	 * Construct the player's HUD. 
	 * @param Samus	the player object, containing information about the player's status.
	 * @param x 	the x-coordinate of where the HUD will be.
	 * @param y		the y-coordinate of where the HUD will be.
	 */
	public PlayerHud(SpriteBatch batch, Samus samus, float x, float y, String fileName) {
		this.samus = samus;
		this.texture = new Texture(Gdx.files.internal(fileName));
		this.x = x;
		this.y = y;
		
		this.viewport = new FitViewport(Metroid.VIEWPORT_WIDTH, Metroid.VIEWPORT_HEIGHT);
		this.stage = new Stage(this.viewport, batch);
		
		Table table = new Table();
		table.left();
		table.top();
		table.setFillParent(true);
		
		Drawable drawable = new TextureRegionDrawable(new TextureRegion(this.texture, 0, 0, 32, 16));
		Image energyIcon = new Image(drawable);
		
		this.healthTextActor = new HudCountActor(this.texture, Integer.toString(this.samus.getHP()));
		
		table.add(energyIcon).padLeft(20).padTop(25);
		table.add(this.healthTextActor).padTop(25 + 8);	// TODO: why are Actors higher by the sprite region's number of pixels? is the origin of Actors different than Images?
		
		stage.addActor(table);
	}
	
	/** Returns the HUD's stage. */
	public Stage getStage() {
		return this.stage;
	}
	
	public Actor getHealthActor() {
		return this.healthTextActor;
	}
	
	public AttributeListener<Integer> getHealthActorAsInterface() {
		return (AttributeListener<Integer>)this.healthTextActor;
	}
	
	public Actor getMissilesActor() {
		return this.healthTextActor;
	}
	
	public AttributeListener<Integer> getMissilesActorAsInterface() {
		return (AttributeListener<Integer>)this.missilesTextActor;
	}

	@Override
	public void dispose() {
		this.stage.dispose();
	}
}
