package me.ezlikespie.miningworld.main.generation;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import me.ezlikespie.miningworld.main.MiningWorld;

public class ChunkPopulator extends BlockPopulator {

	public void populate(World w, Random r, Chunk c) {
		
		GCRandom gcRandom = new GCRandom(c);
		
		for(int x = 0; x<16; x++) {
			for(int z = 0; z<16; z++) {
				for(int y = 60; y>=6; y--) {
					if (gcRandom.isInGiantCave(x, y, z)) {
						try {
						Block block = c.getBlock(x, y, z);
						block.setType(Material.AIR);
						Block top = c.getBlock(x, 70, z);
						if(!top.getType().equals(Material.STONE))
							continue;
						top.setData((byte)5);
						}
						catch(Exception ex) {}
					}
				}
			}
		}
		
	}
	
}
