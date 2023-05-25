package com.ak4311.metroid.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.ak4311.metroid.Metroid;
import com.ak4311.metroid.entity.NPC;
import com.ak4311.metroid.entity.NPCUpdateThread;
import com.ak4311.metroid.entity.Samus;
import com.ak4311.metroid.hud.PlayerHud;
import com.ak4311.metroid.item.Item;
import com.ak4311.metroid.utility.TiledMapParser;
import com.ak4311.metroid.utility.MetroidContactListener;

public class GameScreen implements Screen {
	private TiledMapParser mapParser;
	private OrthogonalTiledMapRenderer renderer;
	private Box2DDebugRenderer debugRenderer;
	
	private OrthographicCamera gameCamera;
	private Viewport viewport;
	private PlayerHud hud;
	
	private Metroid game;
	private Samus samus;
	private Array<NPC> npcs;
	private Array<Item> items;
	private World world;
	
	public GameScreen(Metroid game) {
		this.game = game;
		this.npcs = new Array<>();
		this.items = new Array<>();
		this.world = new World(new Vector2(0, -22), true);
		this.world.setContactListener(new MetroidContactListener(this));
		this.samus = new Samus(this.world, "Spritesheets/samus.png");
		this.hud = new PlayerHud(this.game.batch, this.samus, 0, 0, "Spritesheets/player_hud.png");
		
		this.samus.addListener(this.hud.getHealthActorAsInterface());
		initializeMap();
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public Samus getPlayer() {
		return this.samus;
	}
	
	public void update(float dt) {
		this.world.step(1 / 60f, 6, 2);
		
		this.samus.update(dt);
//		for (NPC npc : npcs) {
//			npc.update(dt);
//		}
		
		// TODO: calling this.npcs.items to pass as NPC[] in chunks.add(...) below gave ClassCastException. not sure why creating NPC[] separately fixed it.
		NPC[] npcsArray = new NPC[this.npcs.size];
	    for (int i = 0; i < this.npcs.size; i++) {
	        npcsArray[i] = this.npcs.get(i);
	    }
	    
	    // we use multithreading to update all NPCs.
	    // TODO: look into if thread pools can help so we don't have to recreate new threads every update().
		int chunkSize = (int)Math.ceil(this.npcs.size / (float)Metroid.THREAD_COUNT);
		int iterations = Math.min(Metroid.THREAD_COUNT, (int)Math.ceil(this.npcs.size / (float)chunkSize));
		Array<Array<NPC>> chunks = new Array<>();
		for (int i = 0; i < iterations; i++) {
			int startIdx = i * chunkSize;
			int endIdx = Math.min(startIdx + chunkSize - 1, npcs.size - 1);
			chunks.add(new Array<>(false, npcsArray, startIdx, endIdx - startIdx + 1));
		}
		
		Array<Thread> threads = new Array<>();
		for (int i = 0; i < iterations; i++) {
			threads.add(new Thread(new NPCUpdateThread(npcs, chunks.get(i), dt)));
			threads.get(i).start();
		}
		
		for (int i = 0; i < iterations; i++) {
            try {
                threads.get(i).join();
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		
		for (Item item : items) {
			item.update(dt);
			if (item.isDestroyed()) {
				items.removeValue(item, true);
			}
		}
		
		this.gameCamera.position.x = Math.min(this.mapParser.getMapWidth() / Metroid.PPM - this.viewport.getWorldWidth() / 2, 
			Math.max((this.viewport.getWorldWidth() - (1 / Metroid.PPM)) / 2, this.samus.getBody().getPosition().x));
		this.gameCamera.position.y = Math.min(this.mapParser.getMapHeight() / Metroid.PPM - this.viewport.getWorldHeight() / 2, 
			Math.max(this.viewport.getWorldHeight() / 2, this.samus.getBody().getPosition().y));
		
		this.gameCamera.update();
		this.renderer.setView(this.gameCamera);
	}
	
	private void initializeMap() {
		this.mapParser = new TiledMapParser(this, this.world, "brinstar1.tmx");
		this.renderer = new OrthogonalTiledMapRenderer(this.mapParser.getMap(), 1 / Metroid.PPM);
		this.debugRenderer = new Box2DDebugRenderer();
		
		this.gameCamera = new OrthographicCamera();
		this.viewport = new FitViewport(Metroid.VIEWPORT_WIDTH / Metroid.PPM, Metroid.VIEWPORT_HEIGHT / Metroid.PPM, gameCamera);
//		this.gameCamera.position.set(this.gamePort.getWorldWidth() / 2, this.gamePort.getWorldHeight() / 2, 0);
	}
	
	public void addNPC(NPC npc) {
		this.npcs.add(npc);
	}
	
	public void removeNPC(NPC npc) {
		this.npcs.removeValue(npc, true);
	}
	
	public void addItem(Item item) {
		this.items.add(item);
	}
	
	public void removeItem(Item item) {
		this.items.removeValue(item, true);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	// TODO: currently, samus draws on top of everything. we want priority of drawing to be foreground, samus and other sprites, and finally the background.
	// introduce z-indexes later to decide what should draw in front of others.
	@Override
	public void render(float dt) {
		update(dt);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		this.renderer.render();
		
		// TODO: ideally for a bigger game, we would want an entity manager or something similar to process all entities neatly.
		// TODO: use multithreading to handle game logic of different entities below, e.g. input processing, AI logic.
		this.game.batch.setProjectionMatrix(this.gameCamera.combined);
		this.game.batch.begin();
		this.samus.draw(this.game.batch);
		for (NPC npc : this.npcs) {
			npc.draw(this.game.batch);
		}
		for (Item item : this.items) {
			item.draw(this.game.batch);
		}
		this.game.batch.end();
		
		if (Metroid.DEBUG) {
			this.debugRenderer.render(this.world, this.gameCamera.combined);
		}
		
		this.game.batch.setProjectionMatrix(this.hud.getStage().getCamera().combined);
		this.hud.getStage().act(dt);
		this.hud.getStage().draw();
	}

	@Override
	public void resize(int width, int height) {
		this.viewport.update(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}