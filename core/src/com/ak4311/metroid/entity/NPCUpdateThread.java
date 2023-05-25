package com.ak4311.metroid.entity;

import com.badlogic.gdx.utils.Array;

public class NPCUpdateThread implements Runnable {
	private Array<NPC> allNpcs;
	private Array<NPC> npcs;
	private float dt;
	
	public NPCUpdateThread(Array<NPC> allNpcs, Array<NPC> npcs, float dt) {
		this.allNpcs = allNpcs;
		this.npcs = npcs;
		this.dt = dt;
	}
	
	@Override
	public void run() {
//		System.out.println(this.npcs.size + " " + System.currentTimeMillis());
		for (NPC npc : this.npcs) {			
			npc.update(this.dt);
			if (npc.isToBeDestroyed()) {
				allNpcs.removeValue(npc, true);
			}
		}
	}
}
