//package com.ak4311.metroid.entity;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.physics.box2d.World;
//import com.badlogic.gdx.utils.Array;
//
//public class EntityBuilder {
//	private World world;
//	private Texture texture;
//	
//	private Array<Vector2> sourcePositions = new Array<>();
//	private Array<Vector2> sourceSizes = new Array<>();
//	private Array<Integer> frameCounts = new Array<>();
//	private Array<Float> animationTimes = new Array<>();
//	private Vector2 position = new Vector2(0f, 0f);
//	private Vector2 velocity = new Vector2(0f, 0f);
//	
//	public EntityBuilder(World world, Texture texture) {
//		this.world = world;
//		this.texture = texture;
//	}
//	
//	public <T extends Entity> T buildEntity(Class<T> clazz) {
//		try {
//			Constructor<T> constructor = clazz.getConstructor(World.class, Texture.class, Array.class, Array.class, Array.class, Array.class, Vector2.class, Vector2.class);
//			T entity = constructor.newInstance(world, texture, sourcePositions, sourceSizes, frameCounts, animationTimes, position, velocity);
//			return entity;
//		} 
//		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
//	
//	public EntityBuilder sourcePositions(Array<Vector2> sourcePositions) {
//		this.sourcePositions = sourcePositions;
//		return this;
//	}
//	
//	public EntityBuilder sourceSizes(Array<Vector2> sourceSizes) {
//		this.sourceSizes = sourceSizes;
//		return this;
//	}
//	
//	public EntityBuilder frameCounts(Array<Integer> frameCounts) {
//		this.frameCounts = frameCounts;
//		return this;
//	}
//	
//	public EntityBuilder animationTimes(Array<Float> animationTimes) {
//		this.animationTimes = animationTimes;
//		return this;
//	}
//	
//	public EntityBuilder position(Vector2 position) {
//		this.position = position;
//		return this;
//	}
//	
//	public EntityBuilder position(float posX, float posY) {
//		this.position.x = posX;
//		this.position.y = posY;
//		return this;
//	}
//	
//	public EntityBuilder velocityXY(Vector2 velocity) {
//		this.velocity = velocity;
//		return this;
//	}
//	
//	public EntityBuilder velocityXY(float velocityX, float velocityY) {
//		this.velocity.x = velocityX;
//		this.velocity.y = velocityY;
//		return this;
//	}
//}
