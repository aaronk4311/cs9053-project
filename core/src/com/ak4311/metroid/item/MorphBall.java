package com.ak4311.metroid.item;

import com.ak4311.metroid.entity.Samus;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class MorphBall extends Item {
	public MorphBall(World world, Texture texture, int srcX, int srcY, int srcW, int srcH, float animTime, int posX, int posY) {
		this(world, texture, srcX, srcY, srcW, srcH, animTime, new Vector2(posX, posY));
	}
	
	public MorphBall(World world, Texture texture, int srcX, int srcY, int srcW, int srcH, float animTime, Vector2 pos) {
		super(world, texture, srcX, srcY, srcW, srcH, animTime, pos);
	}
	
	@Override
	public void effect(Samus samus) {
		samus.setUpgradeBits(Samus.MORPH_BALL_UPGRADE_BIT);
		destroy();
	}
}
