package me.ezlikespie.miningworld.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.ezlikespie.miningworld.main.Main;

public class Snapshot {

	public Snapshot(Player p) {
		
		PlayerInventory i = p.getInventory();
		helmet = i.getHelmet();
		chestplate = i.getChestplate();
		leggings = i.getLeggings();
		boots = i.getBoots();
		
		for(ItemStack is : i.getContents()) {
			slots.add(is);
		}
		health = p.getHealth();
		food = p.getFoodLevel();
		isFlying = p.isFlying();
		gamemode = p.getGameMode();
		location = p.getLocation();
		exp = p.getExp();
		levels = p.getLevel();
		try {
		balance = Main.getEconomy().getBalance(p)-15000;
		}catch(Exception ex) {}
		
	}
	
	
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private double balance;
	private List<ItemStack> slots = new ArrayList<ItemStack>();
	private double health;
	private int food;
	private Location location;
	private GameMode gamemode;
	private Boolean isFlying;
	private float exp;
	private int levels;
	
	
	public double getBalance() {
		return balance;
	}
	public ItemStack getHelmet() {
		return helmet;
	}
	public ItemStack getChestplate() {
		return chestplate;
	}
	public ItemStack getLeggings() {
		return leggings;
	}
	public ItemStack getBoots() {
		return boots;
	}
	public List<ItemStack> getSlots(){
		return slots;
	}
	public ItemStack getSlot(int i) {
		return getSlots().get(i);
	}
	public double getHealth() {
		return health;
	}
	public int getFood() {
		return food;
	}
	public Location getLocation() {
		return location;
	}
	public GameMode getGamemode() {
		return gamemode;
	}
	public Boolean isFlying() {
		return isFlying;
	}
	public float getExp() {
		return exp;
	}
	public int getLevels() {
		return levels;
	}
	
	public static void apply(Player p, Snapshot s) {
		PlayerInventory i = p.getInventory();
		i.setHelmet(s.getHelmet());
		i.setChestplate(s.getChestplate());
		i.setLeggings(s.getLeggings());
		i.setBoots(s.getBoots());
		
		List<ItemStack> slots = s.getSlots();
		for(int x = 0; x<slots.size(); x++) {
			i.setItem(x, slots.get(x));
		}
		
		p.setHealth(s.getHealth());
		p.setFoodLevel(s.getFood());
		p.setGameMode(s.getGamemode());
		p.teleport(s.getLocation());
		p.giveExp((int)s.getExp());
		p.setLevel(p.getLevel()+s.getLevels());
		p.setFlying(s.isFlying().booleanValue());
		
		p.updateInventory();
		
		try {
		Main.getEconomy().depositPlayer(p, s.getBalance());
		}catch(Exception e) {}
		
	}
	
}
