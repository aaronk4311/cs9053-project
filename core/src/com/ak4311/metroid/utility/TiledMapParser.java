package com.ak4311.metroid.utility;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.entity.NPC;
import com.ak4311.metroid.entity.Zoomer;
import com.ak4311.metroid.item.Item;
import com.ak4311.metroid.item.MorphBall;
import com.ak4311.metroid.screen.GameScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.ChainShape;

public class TiledMapParser {
	private GameScreen gameScreen;
	private TiledMap map;
	private World world;
	
	private int mapWidth = 0, mapHeight = 0;
	
	public TiledMapParser(GameScreen gameScreen, World world, String fileName) {
		this.gameScreen = gameScreen;
		this.world = world;
		
		TmxMapLoader mapLoader = new TmxMapLoader();
		this.map = mapLoader.load(fileName);
		
		MapProperties properties = this.map.getProperties();
		this.mapWidth = properties.get("width", Integer.class) * properties.get("tilewidth", Integer.class);
		this.mapHeight = properties.get("height", Integer.class) * properties.get("tileheight", Integer.class);
		
		// TODO: try to multithread parising layers
		MapLayers mapLayers = this.map.getLayers();
		parseMapCollisionObjects(mapLayers.get("Collisions").getObjects());
		parseMapItemObjects(mapLayers.get("Items").getObjects());
		parseMapSpriteObjects(mapLayers.get("Sprites").getObjects());
		parseMapEventObjects(mapLayers.get("Events").getObjects());
	}
	
	public TiledMap getMap() {
		return this.map;
	}
	
	public int getMapWidth() {
		return this.mapWidth;
	}
	
	public int getMapHeight() {
		return this.mapHeight;
	}
	
	private void parseMapCollisionObjects(MapObjects mapObjs) {
		BodyDef bodyDef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		Body body;
		
		for (MapObject mapObj : mapObjs) {
			if (mapObj instanceof RectangleMapObject) {
				ChainShape shape = new ChainShape();
				Rectangle rect = ((RectangleMapObject)mapObj).getRectangle();
				
				float rectX = rect.x, rectY = rect.y;
				float rectW = rect.getWidth(), rectH = rect.getHeight();
				
				Vector2[] vertices = new Vector2[5];
				vertices[0] = new Vector2(rectX / Metroid.PPM, (rectY + rectH) / Metroid.PPM);
				vertices[1] = new Vector2((rectX + rectW) / Metroid.PPM, (rectY + rectH) / Metroid.PPM);
				vertices[2] = new Vector2((rectX + rectW) / Metroid.PPM, rectY / Metroid.PPM);
				vertices[3] = new Vector2(rectX / Metroid.PPM, rectY / Metroid.PPM);
				vertices[4] = new Vector2(rectX / Metroid.PPM, (rectY + rectH) / Metroid.PPM);
				shape.createChain(vertices);
				
				body = this.gameScreen.getWorld().createBody(bodyDef);
				fdef.shape = shape;
				fdef.friction = 10;
				fdef.filter.categoryBits = Metroid.WORLD_BIT;
				body.createFixture(fdef);
				shape.dispose();
			}
		}
	}
	
	private void parseMapItemObjects(MapObjects mapObjs) {
		for (MapObject mapObj : mapObjs) {
			if (mapObj instanceof RectangleMapObject) {	
				String itemName = (String)mapObj.getProperties().get("Item");
				Rectangle rect = ((RectangleMapObject)mapObj).getRectangle();
				Vector2 itemPos = new Vector2(rect.x, rect.y);
				
				Texture texture = new Texture("Spritesheets/items.png");
				TextureRegion region = null;
				Item item = null;
				switch (itemName) {
				case "Morph Ball":
					item = new MorphBall(this.world, texture, 0, 0, 16, 16, 0.06f, itemPos);
					region = new TextureRegion(texture, 0, 0, 16, 16);
					break;
				case "Missile Tank":
					
					break;
				case "Energy Tank":
					
					break;
				}
				
				this.gameScreen.addItem(item);
			}
		}
	}
	
	private void parseMapSpriteObjects(MapObjects mapObjs) {
		for (MapObject mapObj : mapObjs) {
			if (mapObj instanceof RectangleMapObject) {
				String spriteName = (String)mapObj.getProperties().get("Sprite");
				Rectangle rect = ((RectangleMapObject)mapObj).getRectangle();
				Vector2 spritePos = new Vector2(rect.x, rect.y);
				
				if (spriteName.equals("Samus")) {
					this.gameScreen.getPlayer().setPosition(spritePos, true);
				}
				else {
					Texture texture;
					TextureRegion[] region;
					NPC npc = null;
					switch (spriteName) {
					case "Zoomer1":
						texture = new Texture("Spritesheets/zoomer.png");
						npc = new Zoomer(this.world, texture);
						npc.setHP(6);
						npc.setDamageDealt(5);
						npc.setPosition(spritePos, true);
						break;
					case "Skree1":
						
						break;
					case "Ripper1":
						
						break;
					}
					
					if (npc != null) this.gameScreen.addNPC(npc);	// TODO: temporary conditional check. there should be no invalid npcs in the world.
				}
			}
		}
	}
	
	private void parseMapEventObjects(MapObjects mapObjs) {
		
	}
}