package me.ezlikespie.miningworld.main.generation;

import java.io.File;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import me.ezlikespie.miningworld.main.Main;
import me.ezlikespie.miningworld.utility.FileHandler;

public class CustomChunkGenerator extends ChunkGenerator {
    int currentHeight = 50;
    
    private static double ironOffset = -200 + 10000 * Math.random();
    private static double goldOffset = 100 + 10000 * Math.random();
    private static double diamondOffset = -100 + 10000 * Math.random();
    private static double emeraldOffset = 200 + 10000 * Math.random();
    
    public static void resetGen() {
    	ironOffset = -200 + 10000 * Math.random();
    	goldOffset = 100 + 10000 * Math.random();
        diamondOffset = -100 + 10000 * Math.random();
        emeraldOffset = 200 + 10000 * Math.random();
    }
    
	private static double ironCoefficient = 0.4;
	private static double ironCutoff = 0.58;
	private static double goldCoefficient = 0.37;
	private static double goldCutoff = 0.60;
	private static double diamondCoefficient = 0.33;
	private static double diamondCutoff = 0.64;
	private static double emeraldCoefficient = 0.3;
	private static double emeraldCutoff = 0.7;
    
    public CustomChunkGenerator() {
    	try {
    	if(!FileHandler.exists(Main.getInstance().getDataFolder()))
    		FileHandler.newDir(Main.getInstance().getDataFolder());
    	if(!FileHandler.exists(new File(Main.getInstance().getDataFolder(),"generator.yml")))
    		FileHandler.newFile(new File(Main.getInstance().getDataFolder(),"generator.yml"));
    	String[] lines = FileHandler.readLines(new File(Main.getInstance().getDataFolder(),"generator.yml"));
    	ironCoefficient = Double.parseDouble(lines[0]);
    	ironCutoff = Double.parseDouble(lines[1]);
    	goldCoefficient = Double.parseDouble(lines[2]);
    	goldCutoff = Double.parseDouble(lines[3]);
    	diamondCoefficient = Double.parseDouble(lines[4]);
    	diamondCutoff= Double.parseDouble(lines[5]);
    	emeraldCoefficient = Double.parseDouble(lines[6]);
    	emeraldCutoff = Double.parseDouble(lines[7]);
    	}
    	catch(Exception ex) {}
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    	
    	
        ChunkData chunk = createChunkData(world);
        
        if(world.getName().equals("MiningWorld")) {
        	if(Main.getMiningWorld()!=null) {
        		Main.getMiningWorld().loadChunk(chunkX, chunkZ);
        	}
        }
        
        PerlinNoiseGenerator gen = new PerlinNoiseGenerator(world);

        //==================== Loop through All Blocks in Chunk =====================
        for(int y = 0; y<=70; y++) {
        	for(int x = 0; x<16; x++) {
        		for(int z = 0; z<16; z++) {
    				if(y==0) {
    					chunk.setBlock(x, y, z, Material.BEDROCK);
    					continue;
    				}
    				if(y==70) {
    					chunk.setBlock(x, y, z, Material.STONE);
    					continue;
    				}
    				double ironVal = gen.noise((x+ironOffset+chunkX*16)*ironCoefficient,(z+ironOffset+chunkZ*16)*ironCoefficient,y*ironCoefficient);
    				double goldVal = gen.noise((x+goldOffset+chunkX*16)*goldCoefficient,(z+goldOffset+chunkZ*16)*goldCoefficient,y*goldCoefficient);
    				double diamondVal = gen.noise((x+diamondOffset+chunkX*16)*diamondCoefficient,(z+diamondOffset+chunkZ*16)*diamondCoefficient,y*diamondCoefficient);
    				double emeraldVal = gen.noise((x+emeraldOffset+chunkX*16)*emeraldCoefficient,(z+emeraldOffset+chunkZ*16)*emeraldCoefficient,y*emeraldCoefficient);
    				if(emeraldCoefficient!=0&&emeraldVal>=emeraldCutoff)
    					chunk.setBlock(x, y, z, Material.EMERALD_ORE);
    				else if(diamondCoefficient!=0&&diamondVal>=diamondCutoff)
    					chunk.setBlock(x, y, z, Material.DIAMOND_ORE);
    				else if(goldCoefficient!=0&&goldVal>=goldCutoff)
    					chunk.setBlock(x, y, z, Material.GOLD_ORE);
    				else if(ironCoefficient!=0&&ironVal>=ironCutoff)
    					chunk.setBlock(x, y, z, Material.IRON_ORE);
    				else
    					chunk.setBlock(x, y, z, Material.STONE);
    				
        		}
        	}
        }
        
        return chunk;
    }
}