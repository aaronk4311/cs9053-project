package com.ak4311.metroid.hud;

import com.ak4311.metroid.utility.AttributeListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class HudCountActor extends Actor implements AttributeListener<Integer> {
	private Texture texture;
	private String text;
	
	public HudCountActor(Texture texture, String text) {
		this.texture = texture;
		this.text = text;
	}
	
	public void updateText(String text) {
		this.text = text;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		for (int i = 0; i < this.text.length(); i++) {
			char chr = text.charAt(i);
			TextureRegion region = new TextureRegion(this.texture, 8 * (chr - '0'), 16, 8, 8);
			batch.draw(region, getX() + i * 8, getY(), 8, 8);
		}
	}

	@Override
	public void onValueChanged(Integer newVal) {
		updateText(String.format("%02d", newVal));
	}
}
