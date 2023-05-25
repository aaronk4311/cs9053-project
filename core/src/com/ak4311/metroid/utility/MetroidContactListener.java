package com.ak4311.metroid.utility;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import java.util.Map;
import java.util.HashMap;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.entity.ArmCannonProjectile;
import com.ak4311.metroid.entity.NPC;
import com.ak4311.metroid.entity.Samus;
import com.ak4311.metroid.item.Item;
import com.ak4311.metroid.screen.GameScreen;

public class MetroidContactListener implements ContactListener {
	GameScreen gameScreen;
	
	// an edge is defined by two vertices. a ChainShape is a shape made up of edges.
	// when a collision happens involving one of the edges, beginContact is called twice, one for each vertex.
	// so, hold a list of contacts to avoid double processing.
	Map<Fixture, Integer> collisionCounter = new HashMap<>();
	
	public MetroidContactListener(GameScreen gameScreen) {
		this.gameScreen = gameScreen;
	}
	
	private Fixture getFixture(Fixture fixA, Fixture fixB, short bits) {
		return fixA.getFilterData().categoryBits == bits ? fixA : fixB;
	}
	
	private void incrementFixtureCounter(Fixture fix) {
		Integer count = collisionCounter.get(fix);
		if (count == null) count = 0;
		collisionCounter.put(fix, count + 1);	
	}
	
	private boolean stillHasCollisions(Fixture fix) {
		if (collisionCounter.get(fix) > 1) {
			Integer count = collisionCounter.get(fix);
			collisionCounter.put(fix, count - 1);
			return true;
		}		
		collisionCounter.remove(fix);
		return false;
	}
	
	@Override
	public void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int collisionType = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		Samus samus;
		NPC npc;
		ArmCannonProjectile projectile;
		switch (collisionType) {
		case Metroid.SAMUS_BIT | Metroid.ENEMY_BIT:
			samus = (Samus)getFixture(fixA, fixB, Metroid.SAMUS_BIT).getUserData();
			if (samus.isImmune()) return;
			
			npc = (NPC)getFixture(fixA, fixB, Metroid.ENEMY_BIT).getUserData();
			int dmg = npc.getDamageDealt();
			samus.takeDamage(dmg);
//			samus.getBody().applyLinearImpulse(new Vector2(-105, 0), samus.getBody().getWorldCenter(), true);
			
			// TODO: look for a better way to push player on contact with enemies.
			Vector2 samusPos = samus.getBody().getPosition();
            Vector2 npcPos = npc.getBody().getPosition();
            Vector2 force = samusPos.sub(npcPos).nor().scl(1000); // calculate force vector
            samus.getBody().applyForceToCenter(Math.max(-500, Math.min(force.x, 500)), Math.min(300, Math.abs(force.y)), true);
			break;
		case Metroid.SAMUS_BIT | Metroid.ITEM_BIT:
			samus = (Samus)getFixture(fixA, fixB, Metroid.SAMUS_BIT).getUserData();
			Item item = (Item)getFixture(fixA, fixB, Metroid.ITEM_BIT).getUserData();
			item.effect(samus);
			break;
		case Metroid.SAMUS_BIT | Metroid.WORLD_BIT:
			Fixture samusFix = getFixture(fixA, fixB, Metroid.SAMUS_BIT);
			if (samusFix.isSensor()) {
				((SensorData)samusFix.getUserData()).inContact = true;
			}
			
			incrementFixtureCounter(samusFix);
			break;
		case Metroid.ENEMY_BIT | Metroid.WORLD_BIT:	// TODO: create beginContact and endContact methods in Entity superclass and call those instead so we can handle behaviors of different npcs.
			// TODO: not working when npc starts on ceilings.
			Fixture npcFix = getFixture(fixA, fixB, Metroid.ENEMY_BIT);
			npc = (NPC)npcFix.getUserData();
			Vector2 normal = contact.getWorldManifold().getNormal();
			if (normal.x > 0) {
				npc.setGravity(-22, 22);
				npc.setVelocity(0, -3);
				npc.setRotation(270);
			}
			else if (normal.x < 0) {
				npc.setGravity(22, 22);
				npc.setVelocity(0, 3);
				npc.setRotation(90);
			}
			else if (normal.y > 0) {
				npc.setGravity(0, 0);
				npc.setVelocity(3, 0);
				npc.setRotation(0);
			}
			else if (normal.y < 0) {
				npc.setGravity(0, 44);
				npc.setVelocity(-3, 0);
				npc.setRotation(180);
			}
			
			incrementFixtureCounter(npcFix);
			break;
		case Metroid.PROJECTILE_BIT | Metroid.WORLD_BIT:
			projectile = (ArmCannonProjectile)(getFixture(fixA, fixB, Metroid.PROJECTILE_BIT).getUserData());
			projectile.setToDestroy();
			break;
		case Metroid.PROJECTILE_BIT | Metroid.ENEMY_BIT:
			projectile = (ArmCannonProjectile)(getFixture(fixA, fixB, Metroid.PROJECTILE_BIT).getUserData());
			npc = (NPC)(getFixture(fixA, fixB, Metroid.ENEMY_BIT).getUserData());
			npc.takeDamage(projectile.getDamageDealt());
			projectile.setToDestroy();
			break;
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int collisionType = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		Fixture samusFix;
		switch (collisionType) {
		case Metroid.SAMUS_BIT | Metroid.ITEM_BIT:
			
			break;
		case Metroid.SAMUS_BIT | Metroid.WORLD_BIT:
			samusFix = getFixture(fixA, fixB, Metroid.SAMUS_BIT);
			if (stillHasCollisions(samusFix)) return;
			
			if (samusFix.isSensor()) {				
				((SensorData)samusFix.getUserData()).inContact = false;
			}
			break;
		case Metroid.ENEMY_BIT | Metroid.WORLD_BIT:
			// TODO: clean up logic
			Fixture npcFix = getFixture(fixA, fixB, Metroid.ENEMY_BIT);
			if (stillHasCollisions(npcFix)) return;
			
			Vector2 oldVelocity = npcFix.getBody().getLinearVelocity();
			NPC npc = (NPC)(npcFix.getUserData());
			if (oldVelocity.x > 0) {
				npc.setGravity(-22, 22);
				npc.setVelocity(0, -3);
				npc.setRotation(270);
			}
			else if (oldVelocity.x < 0) {
				npc.setGravity(22, 22);
				npc.setVelocity(0, 3);
				npc.setRotation(90);
			}
			else if (oldVelocity.y > 0) {
				npc.setGravity(0, 0);
				npc.setVelocity(3, 0);
				npc.setRotation(0);
			}
			else if (oldVelocity.y < 0) {
				npc.setGravity(0, 44);
				npc.setVelocity(-3, 0);
				npc.setRotation(180);
			}
			break;
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		int collisionType = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;
		switch (collisionType) {
		case Metroid.SAMUS_BIT | Metroid.WORLD_BIT:
			Fixture samusFix = getFixture(fixA, fixB, Metroid.SAMUS_BIT);
			Samus samus = (Samus)samusFix.getUserData();	
			if (samus.inContactWithWall()) {				
				contact.setFriction(0);
			}
			break;
		case Metroid.SAMUS_BIT | Metroid.ENEMY_BIT:
			contact.setEnabled(false);
			break;
		case Metroid.PROJECTILE_BIT | Metroid.ENEMY_BIT:
			contact.setEnabled(false);
			break;
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
}
