package com.ak4311.metroid.entity;

import com.ak4311.metroid.Metroid;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

public class Zoomer extends NPC {
	public Zoomer(World world, Texture texture) {
		super(world, texture);
		addAnimation(0, 0, 16, 16, 2, 0.1f);
	}

	@Override
	public void update(float dt) {
		elapsedTime += dt;	// TODO: mod with number of frames to avoid overflow.
		freezeTimer = Math.max(freezeTimer - dt, 0);
		
		body.applyForceToCenter(gravity, true);	// TODO: maybe don't need to do this in update. use setters to update? or does force need to be applied every frame?
		if (freezeTimer == 0) {
			body.setLinearVelocity(velocity);
		}
		
		if (toBeDestroyed) {
			world.destroyBody(body);
			Metroid.SESSION_SCORE += 1;
			// TODO: spawn ammo and health for player
		}
	}
}
