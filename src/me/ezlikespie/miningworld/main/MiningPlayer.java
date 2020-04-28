package me.ezlikespie.miningworld.main;

import java.util.UUID;

import org.bukkit.entity.Player;

import me.ezlikespie.miningworld.utility.Snapshot;

public class MiningPlayer {

	public MiningPlayer(Player p) {
		
		id = p.getUniqueId();
		oldSnapshot = new Snapshot(p);
		
	}
	
	private UUID id;
	private Snapshot oldSnapshot;
	private Snapshot newSnapshot;
	
	public Snapshot getOldSnapshot() {
		return oldSnapshot;
	}
	
	public Snapshot getNewSnapshot() {
		return newSnapshot;
	}
	
}
