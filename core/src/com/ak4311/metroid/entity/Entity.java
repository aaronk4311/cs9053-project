package com.ak4311.metroid.entity;

import com.ak4311.metroid.Metroid;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.Animation;

public abstract class Entity {
	protected World world;
	protected Body body;
	
	protected Texture texture;
	protected Sprite sprite;

	protected Array<Animation<TextureRegion>> animations = new Array<>();
	protected Vector2 velocity = new Vector2(0, 0);
	
	protected boolean toBeDestroyed = false;
	
	public Entity(World world, Texture texture) {
		this.world = world;
		this.texture = texture;
	}
	
	// TODO: see if there's a better way to set regions and animations without manually setting values in arrays.
	public void addAnimation(int srcX, int srcY, int srcW, int srcH, int frameCount, float animTime) {
		Array<TextureRegion> animFrames = new Array<>();
		for (int i = 0; i < frameCount; i++) {
			animFrames.add(new TextureRegion(this.texture, srcX + (i * srcW), srcY, srcW, srcH));
		}
		
		this.animations.add(new Animation<>(animTime, animFrames));
	}

	public void setToDestroy() {
		toBeDestroyed = true;
	}
	
	public boolean isToBeDestroyed() {
		return toBeDestroyed;
	}
	
	/**
	 * Position body so that bottom-left corner is the origin point, rather than the center of body.
	 * Useful for setting position of an entity parsed from Tiled map editor since the position of
	 * an object read from Tiled starts on bottom left.
	 * @param x
	 * @param y 
	 * @param factorPPM if true, position will be divided by PPM. */
	public void setPosition(Vector2 realPos, boolean factorPPM) {
		setPosition(realPos.x, realPos.y, factorPPM);
	}
	
	/**
	 * Position body so that bottom-left corner is the origin point, rather than the center of body.
	 * Useful for setting position of an entity parsed from Tiled map editor since the position of
	 * an object read from Tiled starts on bottom left.
	 * @param x
	 * @param y 
	 * @param factorPPM if true, position will be divided by PPM. */
	public void setPosition(float x, float y, boolean factorPPM) {
		if (factorPPM) {
			x /= Metroid.PPM;
			y /= Metroid.PPM;
		}
		body.setTransform(x + sprite.getWidth() / 2, y + sprite.getHeight() / 2, body.getAngle());
	}
	
	/**
	 * Center the body on (x, y).
	 * @param x
	 * @param y 
	 * @param factorPPM if true, position will be divided by PPM. */
	public void setPositionCentered(float x, float y, boolean factorPPM) {
		if (factorPPM) {
			x /= Metroid.PPM;
			y /= Metroid.PPM;
		}
		body.setTransform(x, y, body.getAngle());
	}
	
	/**
	 * Adjust entity x-position.
	 * @param x
	 * @param factorPPM if true, position will be divided by PPM. */
	public void addPositionX(float x, boolean factorPPM) {
		if (factorPPM) x /= Metroid.PPM;
		body.setTransform(getBodyPosition().x + x, getBodyPosition().y, body.getAngle());
	}
	
	/**
	 * Adjust entity y-position.
	 * @param y
	 * @param factorPPM if true, position will be divided by PPM. */
	public void addPositionY(float y, boolean factorPPM) {
		if (factorPPM) y /= Metroid.PPM;
		body.setTransform(getBodyPosition().x, getBodyPosition().y + y, body.getAngle());
	}
	
	public void setVelocity(float velocityX, float velocityY) {
		this.velocity.x = velocityX;
		this.velocity.y = velocityY;
	}
	
	public void setVelocityX(float velocityX) {
		this.velocity.x = velocityX;
	}
	
	public void setVelocityY(float velocityY) {
		this.velocity.y = velocityY;
	}
	
	public void setVelocityXY(float velocityXY) {
		this.velocity.x = velocityXY;
		this.velocity.y = velocityXY;
	}
	
	public void setRotation(float degrees) {
		/**
		 * TODO: ALL setTransform calls should be moved to after world.step() otherwise we get an error. use a dirty bit or something
		 * to signal that we want to update transform, but do the actual updating in update() so we're sure it happens after world.step().
		 * so far, only rotation is changed in ContactListener, which happens during world.step() from what i understand, so i just commented it here.
		 */
//		body.setTransform(getBodyPosition(), (float)Math.toRadians(degrees));	
		sprite.setRotation(degrees);
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
	
	public Body getBody() {
		return body;
	}
	
	public Vector2 getBodyPosition() {
		return body.getPosition();
	}
	
	/** @return position of entity without PPM factor. */
	public Vector2 getRealPosition() {
		return new Vector2(getBodyPosition().x * Metroid.PPM, getBodyPosition().y * Metroid.PPM);
	}
	
	// TODO: currently, only sprite is rotated. rotate body as well.
	/** @return rotation of entity body in degrees. */
	public float getRotationDegrees() {
		return (float)Math.toDegrees(body.getAngle());
	}
	
	protected abstract void initializeBody();
	protected abstract void initializeSprite();
	
	public abstract void update(float dt);
	public abstract void draw(Batch batch);
}
