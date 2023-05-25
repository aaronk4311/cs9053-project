package com.ak4311.metroid.entity;

import com.ak4311.metroid.Metroid;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class NPC extends Entity {
	// TODO: maybe make enums as part of an interface that all characters implement from?
	public enum State { IDLE, WALK, ATTACK, DEAD };
	public enum Direction { LEFT, RIGHT, UP, DOWN };
	
	// TODO: move to Entity
	protected Vector2 gravity = new Vector2(0, 0);
	
	protected TextureRegion[] region;
	protected Animation<TextureRegion> anim;
	
	protected int hp = 0;
	protected int damageDealt = 0;
	protected float elapsedTime = 0f;
	protected float freezeTimer = 0f;
	protected boolean isInvincible = false;
	
	public NPC(World world, Texture texture) {
		super(world, texture);
		
		initializeSprite();
		initializeBody();
	}
	
	public void freeze(float time) {
		freezeTimer = time;
	}
	
	public void takeDamage(int damage) {
		if (isInvincible) return;
		freeze(0.5f);
		setHP(hp - damage);
	}
	
	public void setHP(int hp) {
		this.hp = hp;
		if (hp <= 0) {
			setToDestroy();
		}
	}
	
	public void setDamageDealt(int damageDealt) {
		this.damageDealt = damageDealt;
	}
	
	public void setGravity(float gravityX, float gravityY) {
		this.gravity.x = gravityX;
		this.gravity.y = gravityY;
	}
	
	public int getHP() {
		return this.hp;
	}
	
	public int getDamageDealt() {
		return this.damageDealt;
	}
	
	@Override
	public void draw(Batch batch) {
		if (!toBeDestroyed) {			
			TextureRegion region = animations.get(0).getKeyFrame(elapsedTime, true);
			sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
			sprite.setRegion(region);
			sprite.draw(batch);
		}
	}
	
	@Override
	protected void initializeBody() {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(8 / Metroid.PPM, (8 - 1) / Metroid.PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = Metroid.ENEMY_BIT;
		fdef.filter.maskBits = Metroid.SAMUS_BIT | Metroid.WORLD_BIT | Metroid.PROJECTILE_BIT;
				
		this.body = this.world.createBody(bdef);
		this.body.createFixture(fdef).setUserData(this);
		shape.dispose();
	}
	
	@Override
	protected void initializeSprite() {
		sprite = new Sprite();
		sprite.setSize(16 / Metroid.PPM, 16 / Metroid.PPM);
		sprite.setOriginCenter();
	}
	
	// TODO: like with many repeated tasks, maybe for bigger games, we should make some sort of manager
	// to handle tasks that are common across multiple classes, e.g. update() is used in several classes.
	@Override
	public abstract void update(float dt);
}
