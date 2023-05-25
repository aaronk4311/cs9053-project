package com.ak4311.metroid.entity;

import com.ak4311.metroid.Metroid;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ArmCannonProjectile extends Entity {
	private Vector2 speed;
	
	private TextureRegion projectileRegion;
//	private TextureRegion impactRegion;
	
	private int damageDealt = 0;
	private float liveTime = 0;
	private float timeAlive = 0;
	
	public ArmCannonProjectile(World world, Texture texture, int srcX, int srcY, Vector2 speed) {
		super(world, texture);
		this.speed = speed;
		
		projectileRegion = new TextureRegion(texture, srcX, srcY, 16, 16);
		
		initializeSprite();
		initializeBody();
	}
	
	public void setDamageDealt(int damageDealt) {
		this.damageDealt = damageDealt;
	}
	
	public int getDamageDealt() {
		return damageDealt;
	}
	
	public void setLiveTime(float liveTime) {
		this.liveTime = liveTime;
	}
	
	public float getLiveTime() {
		return liveTime;
	}
	
	public void reverseSpeedX() {
		this.speed.x *= -1;
	}
	
	public void reverseSpeedY() {
		this.speed.y *= -1;
	}
	
	@Override
	public void update(float dt) {
		timeAlive += dt;
		if (timeAlive >= liveTime) {
			setToDestroy();
		}
		
		if (toBeDestroyed) {
			this.world.destroyBody(this.body);
		}
	}
	
	@Override
	public void draw(Batch batch) {
		if (!toBeDestroyed) {			
			this.sprite.setPosition(this.body.getPosition().x - this.sprite.getWidth() / 2, this.body.getPosition().y - this.sprite.getHeight() / 2);
			this.sprite.draw(batch);
		}
	}
	
//	public void setImpactTextureRegion() {
//		this.impactRegion = new TextureRegion();
//	}
	
	@Override
	protected void initializeBody() {
		BodyDef bdef = new BodyDef();
//		bdef.position.set(this.pos.x / Metroid.PPM + this.sprite.getWidth() / 2, this.pos.y / Metroid.PPM + this.sprite.getHeight() / 2);
		bdef.type = BodyDef.BodyType.DynamicBody;
		
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
//		shape.setAsBox(this.sprite.getWidth() / 2 - 1 / Metroid.PPM, this.sprite.getHeight() / 2 - 1 / Metroid.PPM);
		shape.setAsBox(4 / Metroid.PPM, 4 / Metroid.PPM);	// TODO: variables for different projectile sizes (wave beam, ice beam, missile)
		fdef.shape = shape;
		fdef.filter.categoryBits = Metroid.PROJECTILE_BIT;
		fdef.filter.maskBits = Metroid.ENEMY_BIT | Metroid.WORLD_BIT;
				
		this.body = this.world.createBody(bdef);
		this.body.createFixture(fdef).setUserData(this);
		this.body.setGravityScale(0);
		this.body.setLinearVelocity(speed);
		shape.dispose();
	}

	@Override
	protected void initializeSprite() {
		sprite = new Sprite(projectileRegion);
		sprite.setSize(16 / Metroid.PPM, 16 / Metroid.PPM);
		sprite.setOriginCenter();
	}
}
