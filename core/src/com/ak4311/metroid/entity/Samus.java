package com.ak4311.metroid.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.utility.AttributeListener;
import com.ak4311.metroid.utility.SensorData;

public class Samus extends Entity {
	public enum FaceDirection { LEFT, RIGHT, CENTER, UP };
	
	public static final short STANDING_BIT = 0x0001;
	public static final short WALK_BIT = 0x0002;
	public static final short MORPH_BIT = 0x0004;
	public static final short SHOOT_BIT = 0x0008;
	public static final short JUMP_BIT = 0x0010;
	public static final short DEAD_BIT = 0x0020;
	
	public static final short MORPH_BALL_UPGRADE_BIT = 0x0001;
	public static final short MISSILE_UPGRADE_BIT = 0x0002;
	public static final short LONG_BEAM_UPGRADE_BIT = 0x0004;
	public static final short BOMB_UPGRADE_BIT = 0x0008;
	public static final short ICE_BEAM_UPGRADE_BIT = 0x0010;
	public static final short HIGH_JUMP_UPGRADE_BIT = 0x0020;
	public static final short VARIA_SUIT_UPGRADE_BIT = 0x0040;
	public static final short SCREW_ATTACK_UPGRADE_BIT = 0x0080;
	public static final short WAVE_BEAM_UPGRADE_BIT = 0x0100;
	
	public static final int HITBOX_WIDTH = 6;
	public static final int HITBOX_HEIGHT = 15;
	
	private short stateBits = STANDING_BIT;
	private short upgradeBits = 0;
	
	private FaceDirection direction = FaceDirection.CENTER;
	
	private Array<AttributeListener<Integer>> listeners;
	private Array<ArmCannonProjectile> projectiles;
	
	private TextureRegion[] idleRegions;	// texture regions when player velocity is zero (both x and y velocities).
	private TextureRegion[] jumpRegions;	// texture regions when player jumps straight up or falls down.
	
	private int hp = 25;
	private int missileCount = 0;
	private int energyTankCount = 0;
	private int damageDealt = 3;
	private float beamLiveTime = 0.25f;
	private float beamSpeed = 20f;
	private float elapsedTime = 0f;
	private float immuneTimer = 0f;
	private boolean spriteReversed = false;
	
	public Samus(World world, String textureFileName) {
		super(world, new Texture(textureFileName));
		listeners = new Array<>();
		projectiles = new Array<>();
		
		initializeSprite();
		initializeBody();
		addAnimation(32, 32, 32, 32, 3, 0.06f);		// walk
		addAnimation(32, 64, 32, 32, 3, 0.06f);		// walk + shoot
		addAnimation(32, 96, 32, 48, 3, 0.06f);		// walk + look up
		addAnimation(32, 144, 32, 48, 3, 0.06f);	// walk + look up + shoot
		addAnimation(0, 240, 32, 32, 4, 0.06f);		// spinjump
		addAnimation(64, 272, 32, 32, 4, 0.06f);	// morph ball		
	}
	
	public void addListener(AttributeListener<Integer> listener) {
		listeners.add(listener);
	}
	
	public void makeImmune(float time) {
		immuneTimer = time;
	}
	
	public boolean isImmune() {
		return immuneTimer > 0;
	}
	
	public void takeDamage(int dmg) {
		if (immuneTimer > 0) return;
		
		makeImmune(1);
		setHP(Math.max(hp - dmg, 0));
	}
	
	public void setHP(int hp) {
		this.hp = hp;
		if (this.hp == 0) {
			// TODO: player dies
		}
		
		for (AttributeListener<Integer> listener : listeners) {
			listener.onValueChanged(this.hp);
		}
	}
	
	public void setStateBits(short bits) {
		stateBits |= bits;
	}
	
	public void unsetStateBits(short bits) {
		stateBits &= ~bits;
	}
	
	public boolean areStatesActive(short bits) {
		return (stateBits & bits) == bits;
	}
	
	public void setUpgradeBits(short bits) {
		upgradeBits |= bits;
	}
	
	public boolean hasUpgrades(short bits) {
		return (upgradeBits & bits) == bits;
	}
	
	public void setDirection(FaceDirection direction) {
		this.direction = direction;
	}
	
	public FaceDirection getDirection() {
		return direction;
	}
	
	public short getStateBits() {
		return stateBits;
	}
	
	public short getUpgradeBits() {
		return upgradeBits;
	}
	
	public int getHP() {
		return hp;
	}
	
	public boolean inMorphBall() {
		return (stateBits & MORPH_BIT) > 0;
	}
	
	public boolean isDead() {
		return (stateBits & DEAD_BIT) > 0;
	}
	
	public void setPlayerFriction(float friction) {
		Fixture hitboxFix = body.getFixtureList().get(0);
		hitboxFix.setFriction(friction);
	}
	
	public boolean inContactWithWall() {
		// TODO: optimize later to not use for loops when checking inContactWithWall and inContactWithGround. maybe just declare predefined variables for each sensor.
		for (Fixture fix : body.getFixtureList()) {
			if (fix.isSensor()) {				
				Object userData = fix.getUserData();
				if (userData instanceof SensorData) {
					SensorData sensor = (SensorData)userData;
					if (sensor.type == SensorData.Type.WALL && sensor.inContact) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean inContactWithGround() {
		for (Fixture fix : body.getFixtureList()) {
			if (fix.isSensor()) {				
				Object userData = fix.getUserData();
				if (userData instanceof SensorData) {
					SensorData sensor = (SensorData)userData;
					if (sensor.type == SensorData.Type.FOOT && sensor.inContact) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private void morph() {
		if (!hasUpgrades(MORPH_BALL_UPGRADE_BIT) || areStatesActive(MORPH_BIT)) return;
		
		FixtureDef fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(HITBOX_WIDTH / Metroid.PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = Metroid.SAMUS_BIT;
		fdef.restitution = 0.1f;
		
		FixtureDef fdefSensorLeft = new FixtureDef();
		PolygonShape shapeSensorLeft = new PolygonShape();
		shapeSensorLeft.setAsBox(2 / Metroid.PPM, HITBOX_WIDTH / Metroid.PPM, new Vector2((-HITBOX_HEIGHT - 1) / 2 / Metroid.PPM, 0), 0);
		fdefSensorLeft.shape = shapeSensorLeft;
		fdefSensorLeft.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorLeft.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorLeft.isSensor = true;
		
		FixtureDef fdefSensorRight = new FixtureDef();
		PolygonShape shapeSensorRight = new PolygonShape();
		shapeSensorRight.setAsBox(2 / Metroid.PPM, HITBOX_WIDTH / Metroid.PPM, new Vector2((HITBOX_HEIGHT + 1) / 2 / Metroid.PPM, 0), 0);
		fdefSensorRight.shape = shapeSensorRight;
		fdefSensorRight.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorRight.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorRight.isSensor = true;
		
		FixtureDef fdefSensorFoot = new FixtureDef();
		PolygonShape shapeSensorFoot = new PolygonShape();
		shapeSensorFoot.setAsBox((HITBOX_WIDTH - 1) / Metroid.PPM, 2 / Metroid.PPM, new Vector2(0, (-HITBOX_WIDTH - 2) / Metroid.PPM), 0);
		fdefSensorFoot.shape = shapeSensorFoot;
		fdefSensorFoot.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorFoot.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorFoot.isSensor = true;
		
		destroyOldFixtures();
		
		body.createFixture(fdef).setUserData(this);
		body.createFixture(fdefSensorLeft).setUserData(new SensorData());
		body.createFixture(fdefSensorRight).setUserData(new SensorData());
		body.createFixture(fdefSensorFoot).setUserData(new SensorData(SensorData.Type.FOOT));
		shape.dispose();
		shapeSensorLeft.dispose();
		shapeSensorRight.dispose();
		shapeSensorFoot.dispose();
		
		setStateBits(MORPH_BIT);
	}
	
	private void unmorph() {
		destroyOldFixtures();
		
		FixtureDef fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(HITBOX_WIDTH / Metroid.PPM, HITBOX_HEIGHT / Metroid.PPM);
		fdef.shape = shape;
		fdef.filter.categoryBits = Metroid.SAMUS_BIT;
		
		FixtureDef fdefSensorLeft = new FixtureDef();
		PolygonShape shapeSensorLeft = new PolygonShape();
		shapeSensorLeft.setAsBox(2 / Metroid.PPM, (HITBOX_HEIGHT - 0.5f) / Metroid.PPM, new Vector2((-HITBOX_HEIGHT - 1) / 2 / Metroid.PPM, 0), 0);
		fdefSensorLeft.shape = shapeSensorLeft;
		fdefSensorLeft.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorLeft.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorLeft.isSensor = true;
		
		FixtureDef fdefSensorRight = new FixtureDef();
		PolygonShape shapeSensorRight = new PolygonShape();
		shapeSensorRight.setAsBox(2 / Metroid.PPM, (HITBOX_HEIGHT - 0.5f) / Metroid.PPM, new Vector2((HITBOX_HEIGHT + 1) / 2 / Metroid.PPM, 0), 0);
		fdefSensorRight.shape = shapeSensorRight;
		fdefSensorRight.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorRight.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorRight.isSensor = true;
		
		FixtureDef fdefSensorFoot = new FixtureDef();
		PolygonShape shapeSensorFoot = new PolygonShape();
		shapeSensorFoot.setAsBox((HITBOX_WIDTH - 1) / Metroid.PPM, 2 / Metroid.PPM, new Vector2(0, (-HITBOX_HEIGHT - 2) / Metroid.PPM), 0);
		fdefSensorFoot.shape = shapeSensorFoot;
		fdefSensorFoot.filter.categoryBits = Metroid.SAMUS_BIT;
		fdefSensorFoot.filter.maskBits = Metroid.WORLD_BIT;
		fdefSensorFoot.isSensor = true;
		
		body.createFixture(fdef).setUserData(this);
		body.createFixture(fdefSensorLeft).setUserData(new SensorData());
		body.createFixture(fdefSensorRight).setUserData(new SensorData());
		body.createFixture(fdefSensorFoot).setUserData(new SensorData(SensorData.Type.FOOT));
		shape.dispose();
		shapeSensorLeft.dispose();
		shapeSensorRight.dispose();
		shapeSensorFoot.dispose();
		
		addPositionY(16, true);
		unsetStateBits(MORPH_BIT);
	}
	
	private void fireBeam() {
		Vector2 beamVelocity = new Vector2(0, 0);
		if (direction == FaceDirection.UP) {
			beamVelocity.y = beamSpeed;
		}
		else {
			beamVelocity.x = direction == FaceDirection.LEFT ? -beamSpeed : beamSpeed;
		}
		projectiles.add(new ArmCannonProjectile(world, texture, 0, 304, beamVelocity));
		Vector2 spawnPos = getRealPosition();
		if (direction == FaceDirection.UP) {
			spawnPos.y += 22;
		}
		else {
			spawnPos.x += spriteReversed ? -14 : 14;
			spawnPos.y += 6;
		}
		
		ArmCannonProjectile projectile = projectiles.get(projectiles.size - 1);
		projectile.setDamageDealt(damageDealt);
		projectile.setLiveTime(beamLiveTime);
		projectile.setPositionCentered(spawnPos.x, spawnPos.y, true);
	}
	
	private void handleInputs() {
		if (immuneTimer > 0) return;	// prevent player from using force applied after taking damage to boost to unreachable places.
		
		// TODO: check if player is alive to handle inputs below
		if (Gdx.input.isKeyJustPressed(Input.Keys.W) && body.getLinearVelocity().y == 0) {
			if (!inMorphBall()) {				
				body.applyLinearImpulse(new Vector2(0, 16), body.getWorldCenter(), true);	// TODO: jump distance depending on how long key is pressed
			}
			else {
				unmorph();	// TODO: only unmorph if there is enough vertical height.
			}
		}
//		else {
//			body.applyLinearImpulse(new Vector2(0, -body.getLinearVelocity().y), body.getWorldCenter(), true);
//		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			body.applyLinearImpulse(new Vector2(body.getMass() * (-8 - body.getLinearVelocity().x), 0), body.getWorldCenter(), true);
			direction = FaceDirection.LEFT;
			spriteReversed = true;
		}
		else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			body.applyLinearImpulse(new Vector2(body.getMass() * (8 - body.getLinearVelocity().x), 0), body.getWorldCenter(), true);
			direction = FaceDirection.RIGHT;
			spriteReversed = false;
		}
		else {	// stop player immediately if not pressing A or D so controls feel "tight".
			body.applyLinearImpulse(new Vector2(-body.getLinearVelocity().x, 0), body.getWorldCenter(), true);
		}
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.S) && !areStatesActive(JUMP_BIT)) {		// morph ball
			morph();
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.K)) {			// look up
//			lookUp();
			direction = FaceDirection.UP;
		}
		else if (direction != FaceDirection.CENTER) {
//			lookForward();
			direction = spriteReversed ? FaceDirection.LEFT : FaceDirection.RIGHT;
		}
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.J) && !inMorphBall()) {
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {	// missile shoot
//				fireMissile();
			}
			else {	// beam shoot
				fireBeam();
			}
			
			setStateBits(SHOOT_BIT);
		}
		else {
			unsetStateBits(SHOOT_BIT);
		}
		
		Vector2 velocity = body.getLinearVelocity();
		if (velocity.isZero()) {
			unsetStateBits((short)(WALK_BIT | JUMP_BIT));
			setStateBits(STANDING_BIT);
		}
		else {
			unsetStateBits(STANDING_BIT);
			if (velocity.x != 0) {
//				direction = velocity.x < 0 ? FaceDirection.LEFT : FaceDirection.RIGHT;	
				if (velocity.y == 0) {
					setStateBits(WALK_BIT);
					unsetStateBits(JUMP_BIT);
				}
			}
			if (velocity.y != 0) {
				setStateBits(JUMP_BIT);
			}
		}
	}
	
	// TODO: maybe no need to destroy fixtures, just update fixturedef's shape.
	private void destroyOldFixtures() {
		while (body.getFixtureList().size > 0) {
			body.destroyFixture(body.getFixtureList().pop());
		}
	}
	
	@Override
	protected void initializeBody() {
		BodyDef bdef = new BodyDef();
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = world.createBody(bdef);

		unmorph();		
	}
	
	@Override
	protected void initializeSprite() {
		sprite = new Sprite();
		sprite.setSize(16 / Metroid.PPM, 16 / Metroid.PPM);
		sprite.setOriginCenter();
		
		// idle regions
		idleRegions = new TextureRegion[5];
		idleRegions[0] = new TextureRegion(texture, 0, 0, 32, 32);
		idleRegions[1] = new TextureRegion(texture, 0, 32, 32, 32);
		idleRegions[2] = new TextureRegion(texture, 0, 64, 32, 32);
		idleRegions[3] = new TextureRegion(texture, 0, 96, 32, 48);
		idleRegions[4] = new TextureRegion(texture, 0, 144, 32, 48);
		
		// jump regions
		jumpRegions = new TextureRegion[4];
		jumpRegions[0] = new TextureRegion(texture, 0, 192, 32, 32);
		jumpRegions[1] = new TextureRegion(texture, 32, 192, 32, 32);
		jumpRegions[2] = new TextureRegion(texture, 64, 192, 32, 48);
		jumpRegions[3] = new TextureRegion(texture, 96, 192, 32, 48);
	}
	
	@Override
	public void update(float dt) {
		immuneTimer = Math.max(immuneTimer - dt, 0);
		handleInputs();
		
		TextureRegion region = idleRegions[0];
		switch (stateBits) {
		case STANDING_BIT:
			if (direction != FaceDirection.CENTER) region = direction == FaceDirection.UP ? idleRegions[3] : idleRegions[1];
			break;
		case STANDING_BIT | SHOOT_BIT:
			region = direction == FaceDirection.UP ? idleRegions[4] : idleRegions[2];
			break;
		case WALK_BIT:
			region = direction == FaceDirection.UP ? animations.get(2).getKeyFrame(elapsedTime, true) : animations.get(0).getKeyFrame(elapsedTime, true);
			break;
		case WALK_BIT | SHOOT_BIT:
			region = direction == FaceDirection.UP ? animations.get(3).getKeyFrame(elapsedTime, true) : animations.get(1).getKeyFrame(elapsedTime, true);
			break;
		case JUMP_BIT:
			region = direction == FaceDirection.UP ? jumpRegions[2] : jumpRegions[0];
			break;
		case JUMP_BIT | WALK_BIT:
			region = animations.get(4).getKeyFrame(elapsedTime, true);
			break;
		case JUMP_BIT | SHOOT_BIT:
			region = direction == FaceDirection.UP ? jumpRegions[3] : jumpRegions[1]; 
			break;
		}
		
		if (inMorphBall()) {
			region = animations.get(5).getKeyFrame(elapsedTime, true);
		}
		
//		System.out.println(this.elapsedTime);
		// TODO: use mod to wrap elapsedTime around based on animation time of each frame so we don't overflow
		elapsedTime += dt;
		if (spriteReversed && !region.isFlipX()) {	// TODO: this could be cleaner... maybe combine with above linear velocity check somehow
			region.flip(true, false);
		}
		else if (!spriteReversed && region.isFlipX()) {
			region.flip(true, false);
		}
		
		sprite.setSize(region.getRegionWidth() / Metroid.PPM, region.getRegionHeight() / Metroid.PPM);
		sprite.setRegion(region);
		
		for (ArmCannonProjectile projectile : projectiles) {
			projectile.update(dt);
			if (projectile.isToBeDestroyed()) {
				projectiles.removeValue(projectile, true);
			}
		}
	}

	@Override
	public void draw(Batch batch) {
		sprite.setPosition(getBodyPosition().x - sprite.getWidth() / 2, getBodyPosition().y - sprite.getHeight() / 2);
		sprite.draw(batch);
		
		for (ArmCannonProjectile projectile : projectiles) {
			projectile.draw(batch);
		}
	}
}
