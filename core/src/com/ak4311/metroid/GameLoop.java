package com.ak4311.metroid;

import com.ak4311.metroid.screen.GameScreen;
import com.badlogic.gdx.Screen;

public class GameLoop {
	private Metroid game;
	private Screen screen;
	
	public GameLoop(Metroid game) {
		this.game = game;
		this.screen = new GameScreen(this.game);
		
//		loop();
	}
	
	public Metroid getGame() {
		return this.game;
	}
	
	public Screen getScreen() {
		return this.screen;
	}
	
//	private void loop() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Gdx.app.postRunnable(new Runnable() {
//					@Override
//					public void run() {
//						System.out.println("TEST2");	
//					}
//				});
//			}
//		}).start();
//	}
}
