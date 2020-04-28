package me.ezlikespie.miningworld.main.generation;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public class GCRandom
{
	public Chunk chunk;
	private final double f1xz;
	private final double f1y;
	private final int amplitude1 = 100;
  
	private final double subtractForLessThanCutoff;
	private final double f2xz = 0.25D;
	private final double f2y = 0.05D;
	private final int amplitude2 = 2;
  
	private final double f3xz = 0.025D;
	private final double f3y = 0.005D;
	private final int amplitude3 = 20;
  
	private final int caveBandBuffer;
  
	private final NoiseGenerator noiseGen1;
	private final NoiseGenerator noiseGen2;
	private final NoiseGenerator noiseGen3;
	
	private final int caveBandMax = 60;
	private final int caveBandMin = 6;
	private final int sxz = 200;
	private final int sy = 100;
	private final int cutoff = 25;
	
	@SuppressWarnings("unused")
	public GCRandom(Chunk chunk) {
		this.chunk = chunk;
		subtractForLessThanCutoff = (100 - cutoff);
		f1xz = (1.0D / sxz);
		f1y = (1.0D / sy);
		
		if (caveBandMax - caveBandMin > 128) {
			caveBandBuffer = 32;
		} 
		else {
			caveBandBuffer = 16;
		}
		noiseGen1 = new SimplexNoiseGenerator(chunk.getWorld());
		noiseGen2 = new SimplexNoiseGenerator((long)noiseGen1.noise(chunk.getX(), chunk.getZ()));
		noiseGen3 = new SimplexNoiseGenerator((long)noiseGen1.noise(chunk.getX(), chunk.getZ()));
	}
  
	public boolean isInGiantCave(int x, int y, int z) {
		boolean isInCave = false;
		try {
		double xx = chunk.getX() << 4 | x & 0xF;
		double yy = y;
		double zz = chunk.getZ() << 4 | z & 0xF;
		
		double n1 = noiseGen1.noise(xx * f1xz, yy * f1y, zz * f1xz) * 100.0D;
		double n2 = noiseGen2.noise(xx * 0.25D, yy * 0.05D, zz * 0.25D) * 2.0D;
		double n3 = noiseGen3.noise(xx * 0.025D, yy * 0.005D, zz * 0.025D) * 20.0D;
		double lc = linearCutoffCoefficient(y);
		isInCave = n1 + n2 - n3 - lc > cutoff;
		}
		catch(Exception ex) {};
		return isInCave;
	}
  
	private double linearCutoffCoefficient(int y) {
		if ((y < caveBandMin) || (y > caveBandMax)) {
			return subtractForLessThanCutoff;
		}
		if ((y >= caveBandMin) && (y <= caveBandMin + caveBandBuffer)) {
			double yy = y - caveBandMin;
			return -subtractForLessThanCutoff / caveBandBuffer * yy + subtractForLessThanCutoff;
		}
		if ((y <= caveBandMax) && (y >= caveBandMax - caveBandBuffer)) {
			double yy = y - caveBandMax + caveBandBuffer;
			return subtractForLessThanCutoff / caveBandBuffer * yy;
		}
    
		return 0.0D;
	}
}