package com.ak4311.metroid.item;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.entity.Samus;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Item {
	protected World world;
	protected Vector2 pos;
	
	protected Texture texture;
	protected Sprite sprite;
	protected Body body;
	
	protected boolean destroyed;
	
	public Item(World world, Texture texture, int srcX, int srcY, int srcW, int srcH, float animTime, int posX, int posY) {
		this(world, texture, srcX, srcY, srcW, srcH, animTime, new Vector2(posX, posY));
	}
	
	public Item(World world, Texture texture, int srcX, int srcY, int srcW, int srcH, float animTime, Vector2 pos) {
		this.world = world;
		this.texture = texture;
		this.pos = pos;
		this.destroyed = false;
		
		this.sprite = new Sprite(texture, srcX, srcY, srcW, srcH);
		this.sprite.setSize(srcW / Metroid.PPM, srcH / Metroid.PPM);
		BodyDef bdef = new BodyDef();
		bdef.position.set((pos.x + srcW / 2) / Metroid.PPM, (pos.y + srcH / 2) / Metroid.PPM);
		bdef.type = BodyDef.BodyType.StaticBody;
		
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(srcW / 2 / Metroid.PPM, srcH / 2 / Metroid.PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = Metroid.ITEM_BIT;
		fdef.filter.maskBits = Metroid.SAMUS_BIT;
		fdef.isSensor = true;
				
		this.body = this.world.createBody(bdef);
		this.body.createFixture(fdef).setUserData(this);
		shape.dispose();
	}
	
	public void update(float dt) {
		// TODO: play animations for items.
		if (destroyed) {			
			this.world.destroyBody(this.body);	// TODO: have to destroy here rather than destroy(), otherwise assertion failed occurs. why?
		}
	}
	
	public void draw(Batch batch) {
		if (!this.destroyed) {
			this.sprite.setPosition(this.body.getPosition().x - this.sprite.getWidth() / 2, this.body.getPosition().y - this.sprite.getHeight() / 2);
			this.sprite.draw(batch);
		}
	}
	
	public void destroy() {
		this.destroyed = true;
	}
	
	public boolean isDestroyed() {
		return this.destroyed;
	}
	
	/*
	 * Subclass must override this method to define 
	 * what sort of effect(s) the item has.
	 */
	public abstract void effect(Samus samus);
}
