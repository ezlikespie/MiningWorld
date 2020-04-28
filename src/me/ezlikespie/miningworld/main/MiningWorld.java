package me.ezlikespie.miningworld.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.ezlikespie.miningworld.main.generation.ChunkPopulator;
import me.ezlikespie.miningworld.main.generation.CustomChunkGenerator;
import me.ezlikespie.miningworld.utility.Command;
import me.ezlikespie.miningworld.utility.FileHandler;
import me.ezlikespie.miningworld.utility.Message;
import me.ezlikespie.miningworld.utility.Snapshot;
import me.ezlikespie.miningworld.utility.TimeParse;
import me.ezlikespie.miningworld.utility.WorldHandler;

public class MiningWorld extends Command {

	public MiningWorld (JavaPlugin plugin) {
		
		//==================== Command Setup ================
		setup(plugin);
		setName("mining", "mine", "mineworld", "miningworld");
		
		if(!FileHandler.exists(Main.getInstance().getDataFolder()))
    		FileHandler.newDir(Main.getInstance().getDataFolder());
    	if(!FileHandler.exists(new File(Main.getInstance().getDataFolder(),"generator.yml")))
    		FileHandler.newFile(new File(Main.getInstance().getDataFolder(),"generator.yml"));
		
		//Copy World from Template
    	if(Bukkit.getServer().getWorld("MiningWorld")!=null)
			WorldHandler.unloadWorld(Bukkit.getServer().getWorld("MiningWorld"));
		if(FileHandler.exists(new File("MiningWorld")))
			WorldHandler.deleteWorld(new File("MiningWorld"));
		WorldHandler.copyWorld(new File("templates/MiningWorld"), new File("MiningWorld"));
		
		//Load World and Setup Generator
		if(Bukkit.getServer().getWorld("MiningWorld")==null) {
			WorldCreator wc = new WorldCreator("MiningWorld");
			wc.generator(new CustomChunkGenerator());
			wc.createWorld();
			Bukkit.getServer().getWorld("MiningWorld").getPopulators().add(new ChunkPopulator());
		}
		
		//Regenerate Old Chunks
		
		world = Bukkit.getServer().getWorld("MiningWorld");
		
		if(world.getPlayers().size()>0)
			return;
		int dist = Bukkit.getServer().getViewDistance()+4;
		for(int i = -dist; i<=dist; i++) {
			for(int x = -dist; x<=dist; x++) {
				try {
				world.regenerateChunk(i, x);
				}
				catch(Exception ex) {}
			}
		}
		
		//=================== Day Timer ================
		new BukkitRunnable() {
			public void run() {
				if(world==null)
					return;
				world.setTime(5000);
			}
		}.runTaskTimer(plugin, 200, 200);
		
	}
	
	private World world;
	
	//-----------------------------------------
	//				Run Command
	//-----------------------------------------
	public void execute(Player p, String[] args) {
		
		//==================== Leave World =========================
		if(args.length>0) {
			if(args[0].equalsIgnoreCase("leave")||args[0].equalsIgnoreCase("stop")) {
				//Not in World
				if(!getPlayers().containsKey(p.getUniqueId())) {
					Message.send(p, "&f[Mining World] &cYou are not in a mining world");
					return;
				}
				removePlayer(p);
				return;
			}
			else if(args[0].equalsIgnoreCase("time")||args[0].equalsIgnoreCase("wait")) {
				//Not in World
				if(getTicketHolders().contains(p.getUniqueId())) {
					String timeMessage = (startTime+930000-(new Date().getTime())<=0) ? "&f[Mining World] &6The mining session will start in less than 30 seconds":"&f[Mining World] &6"+TimeParse.toString(startTime+930000-(new Date().getTime())).trim()+" remaining until your mining session starts";
					Message.send(p, timeMessage);
					return;
				}
				else if(getPlayers().containsKey(p.getUniqueId())) {
					String timeMessage = "&f[Mining World] &6"+TimeParse.toString(startTime+930000-(new Date().getTime())).trim()+" remaining until your mining session ends";
					Message.send(p, timeMessage);
					return;
				}
				else {
					Message.send(p, "&f[Mining World] &6You are not in a mining world");
					return;
				}
				
			}
		}
		p.openInventory(createGUI());
		
	}
	
	//-----------------------------------------------
	//		  		 Mining World Open
	//-----------------------------------------------
	private Boolean open = true;
	public Boolean isOpen() {
		return open;
	}
	public void setOpen(Boolean val) {
		open = val;
	}
	
	private static long startTime = 0;
	public static long getStartTime() {
		return startTime;
	}
	
	//-------------------------------------------------
	//					Loaded Chunks
	//-------------------------------------------------
	private List<Map<Integer, Integer>> loadedChunks = new ArrayList<Map<Integer, Integer>>();
	
	public void loadChunk(int x, int z) {
		Map<Integer,Integer> dataMap = new HashMap<Integer,Integer>(0);
		dataMap.put(x, z);
		loadedChunks.add(dataMap);
	}
	
	//-------------------------------------------------
	//					World Timer
	//-------------------------------------------------
	private BukkitRunnable timer;
	
	public BukkitRunnable getTimer() {
		return timer;
	}
	public void setTimer(BukkitRunnable run) {
		timer = run;
	}
	
	//------------------------------------------
	//		   	 	Player List
	//------------------------------------------
	private Map<UUID, MiningPlayer> players = new HashMap<UUID, MiningPlayer>();
	
	//====================== Get Player ================
	public Map<UUID, MiningPlayer> getPlayers(){
		return players;
	}
	public MiningPlayer getPlayer(UUID id) {
		return players.get(id);
	}
	public MiningPlayer getPlayer(Player p) {
		return getPlayer(p.getUniqueId());
	}
	
	//======================= Add Player =====================
	public void addPlayer(Player p) {
		players.put(p.getUniqueId(), new MiningPlayer(p));
	}
	
	//======================= Remove Player =====================
	public void removePlayer(Player p) {
		if(!getPlayers().containsKey(p.getUniqueId()))
			return;
		try {
			Message.send(p, "&f[Mining World] &6You made $"+Integer.toString((int)Main.getEconomy().getBalance(p)));
			Snapshot.apply(p, getPlayer(p).getOldSnapshot());
			getPlayers().remove(p.getUniqueId());
			if(getPlayers().size()==0) {
				stopGame();
			}
		}catch(Exception ex) {};
	}
	//------------------------------------
	//			 Ticket Holders
	//------------------------------------
	private List<UUID> ticketHolders = new ArrayList<UUID>();
	
	public List<UUID> getTicketHolders(){
		return ticketHolders;
	}
	public void addTicketHolder(UUID id) {
		
		Player p = Bukkit.getServer().getPlayer(id);
		if(getTicketHolders().contains(id)) {
			Message.send(p, "&f[Mining World] &cYou already have a ticket");
			return;
		}
		ticketHolders.add(id);
		Message.send(p, "&aSuccessfully Purchased Ticket for $15000");
		String timeMessage = (startTime+930000-(new Date().getTime())<=0) ? "&f[Mining World] &6The mining session will start in less than 30 seconds":"&f[Mining World] &6"+TimeParse.toString(startTime+930000-(new Date().getTime())).trim()+" remaining until your mining session starts";
		Message.send(p, timeMessage);
		if(p==null)
			return;
		Boolean didStart = startGame();
		if(didStart==true) {
			Message.send(p, "&f[Mining World] &aJoining mining world...");
		}
	}
	public void addTicketHolder(Player p) {
		addTicketHolder(p.getUniqueId());
	}
	
	private Boolean gameDelay = false;
	private Boolean runnableGoing = false;
	
	//------------------------------------------
	//				Start Game
	//------------------------------------------
	public Boolean startGame() {
		
		//Test if world is open or people are waiting to join
		if(getPlayers().size()>0)
			return false;
		if(getTicketHolders().size()==0)
			return false;
		
		if(gameDelay==false) {
			if(runnableGoing==true)
				return
			runnableGoing = true;
			new BukkitRunnable() {
				public void run() {
					runnableGoing = false;
					gameDelay = true;
					startGame();
					this.cancel();
				}
			}.runTaskTimer(Main.getInstance(), 600, 0);
			return false;
		}
		
		gameDelay = false;
		
		//================ Setup Players in Mining World ===================
		Iterator<UUID> it = getTicketHolders().iterator();
		while(it.hasNext()) {
			UUID id = it.next();
			Player p = Bukkit.getServer().getPlayer(id);
			if(p==null)
				continue;
			addPlayer(p);
			Message.send(p, "&f[Mining World]");
			Message.send(p, "&6-To find caves, look under andesite");
			Message.send(p, "&6-To leave your mining session, type /mine leave");
			Message.send(p, "&6-To get a ticket for the next mining session, type /mine");
			setupInventory(p);
			p.teleport(new Location(world,Math.random()*((Math.random()>0.5)?-1:1)*50,71,Math.random()*((Math.random()>0.5)?-1:1)*50));
			
		}
		ticketHolders = new ArrayList<UUID>();
		
		//=============== Setup Delay to Stop World ====================
		MiningWorld m = Main.getMiningWorld();
		startTime = new Date().getTime();
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
					
				if(startTime+900000<new Date().getTime()) {
					this.cancel();
					m.stopGame();
				}
				else if(startTime+899800<new Date().getTime())
					m.warnPlayers();
				
			}
		};
		runnable.runTaskTimer(Main.getInstance(), 20, 0);
		
		setTimer(runnable);
		
		return true;
		
	}
	
	//------------------------------------------
	//				  Warn Players
	//------------------------------------------
	public void warnPlayers() {
		//========================= Current Player Warning =================
		for(Entry<UUID, MiningPlayer> e : getPlayers().entrySet()) {
			UUID id = e.getKey();
			Player p = Bukkit.getServer().getPlayer(id);
			if(p==null)
				continue;
			Message.send(p, "&f[Mining World] &cYou will be teleported out of the mining world in &l10 seconds");
		}
		//======================= Ticket Holder Warning ======================
		Iterator<UUID> it = getTicketHolders().iterator();
		while(it.hasNext()) {
			UUID id = it.next();
			Player p = Bukkit.getServer().getPlayer(id);
			if(p==null)
				continue;
			Message.send(p, "&f[Mining World] &cYou will be teleported to the mining world in in 40 seconds");
		}
		
		
	}
	//------------------------------------------
	//				  Stop Game
	//------------------------------------------
	public void stopGame() {
		
		startTime = 0l;
		//================= Remove Each Player =================
		for(Map.Entry<UUID,MiningPlayer> e : getPlayers().entrySet()) {
			Player p = Bukkit.getServer().getPlayer(e.getKey());
			if(p==null)
				continue;
			removePlayer(p);
		}
		
		if(world==null)
			return;
		
		if(Main.stopping==true)
			return;
		
		loadedChunks = new ArrayList<Map<Integer,Integer>>();
		
		new BukkitRunnable() {
			public void run() {
				if(world==null)
					return;
				startGame();
				if(world.getPlayers().size()>0)
					return;
				if(Bukkit.getServer().getWorld("MiningWorld")!=null)
					WorldHandler.unloadWorld(Bukkit.getServer().getWorld("MiningWorld"));
				if(FileHandler.exists(new File("MiningWorld")))
					WorldHandler.deleteWorld(new File("MiningWorld"));
				WorldHandler.copyWorld(new File("templates/MiningWorld"), new File("MiningWorld"));
				CustomChunkGenerator.resetGen();
				int dist = Bukkit.getServer().getViewDistance()+4;
				for(int i = -dist; i<=dist; i++) {
					for(int x = -dist; x<=dist; x++) {
						try {
						world.regenerateChunk(i, x);
						}
						catch(Exception ex) {}
					}
				}
				cancel();
			}
		}.runTaskTimer(Main.getInstance(), 20, 1);
	}
	
	//------------------------------------------
	//				Create GUI
	//------------------------------------------
	public static Inventory createGUI() {
		
		Inventory i = Bukkit.createInventory(null, 27, Message.trans("&8Mining World Ticket Shop"));
		ItemStack grayPanel = new ItemStack(Material.STAINED_GLASS_PANE,1, (byte) 7);
		ItemMeta grayPanelMeta = grayPanel.getItemMeta();
		grayPanelMeta.setDisplayName(" ");
		grayPanel.setItemMeta(grayPanelMeta);
		
		//=============== Row One ===============
		i.setItem(0, grayPanel.clone());
		i.setItem(1, grayPanel.clone());
		i.setItem(2, grayPanel.clone());
		i.setItem(3, grayPanel.clone());
		i.setItem(4, grayPanel.clone());
		i.setItem(5, grayPanel.clone());
		i.setItem(6, grayPanel.clone());
		i.setItem(7, grayPanel.clone());
		i.setItem(8, grayPanel.clone());
		
		//=============== Row Three ===============
		i.setItem(18, grayPanel.clone());
		i.setItem(19, grayPanel.clone());
		i.setItem(20, grayPanel.clone());
		i.setItem(21, grayPanel.clone());
		i.setItem(22, grayPanel.clone());
		i.setItem(23, grayPanel.clone());
		i.setItem(24, grayPanel.clone());
		i.setItem(25, grayPanel.clone());
		i.setItem(26, grayPanel.clone());
		
		//============= Row Two ==============
		//Gray Panels
		i.setItem(9, grayPanel.clone());
		i.setItem(10, grayPanel.clone());
		i.setItem(11, grayPanel.clone());
		i.setItem(13, grayPanel.clone());
		i.setItem(15, grayPanel.clone());
		i.setItem(16, grayPanel.clone());
		i.setItem(17, grayPanel.clone());
		ItemStack confirmButton = new ItemStack(Material.EMERALD_BLOCK,1);
		ItemMeta confirmButtonMeta = confirmButton.getItemMeta();
		confirmButtonMeta.setDisplayName(Message.trans("&a&lPurchase Ticket"));
		String timeMessage = (startTime+930000-(new Date().getTime())<=0) ? "&7Wait Time: less than 30 Seconds":"&7Wait Time: "+TimeParse.toString(startTime+930000-(new Date().getTime()));
		confirmButtonMeta.setLore(Arrays.asList(
			new String [] {
				"",
				Message.trans("&6Cost: $15000"),
				Message.trans("&7Duration: 15 Minutes"),
				Message.trans(timeMessage)
				
			}
		));
		confirmButton.setItemMeta(confirmButtonMeta);
		i.setItem(12, confirmButton);
		ItemStack denyButton = new ItemStack(Material.REDSTONE_BLOCK,1);
		ItemMeta denyButtonMeta = denyButton.getItemMeta();
		denyButtonMeta.setDisplayName(Message.trans("&c&lCancel Purchase"));
		denyButton.setItemMeta(denyButtonMeta);
		i.setItem(14, denyButton);
		
		
		return i;
		
	}
	
	//-------------------------------------------------
	//					Setup Inventory
	//-------------------------------------------------
	public static void setupInventory(Player p) {
		
		//Clear Inventory
		PlayerInventory i = p.getInventory();
		i.setHelmet(null);
		i.setChestplate(null);
		i.setLeggings(null);
		i.setBoots(null);
		i.clear();
		
		//Add Pickaxe, Stone, and Torches
		ItemStack dp = new ItemStack(Material.IRON_PICKAXE,1);
		ItemMeta dpMeta = dp.getItemMeta();
		dpMeta.setDisplayName(Message.trans("&bBig Bertha"));
		dpMeta.addItemFlags(new ItemFlag[] {ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES});
		dpMeta.addEnchant(Enchantment.DIG_SPEED, 3, true);
		dpMeta.addEnchant(Enchantment.DURABILITY, 3, true);
		dp.setItemMeta(dpMeta);
		i.addItem(dp);
		ItemStack stone = new ItemStack(Material.STONE, 64);
		i.addItem(stone);
		
		//Set Health, Food, Flying, Fire, and GameMode
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFlying(false);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);
		p.setGameMode(GameMode.SURVIVAL);
		p.updateInventory();
		
		//Clear Balance
		try {
			Main.getEconomy().withdrawPlayer(p, Main.getEconomy().getBalance(p));
		}
		catch(Exception ex) {}
	}
	
	//----------------------------------------------
	//				 Purchase Ticket
	//----------------------------------------------
	public void purchaseTicket(Player p) {
		
		addTicketHolder(p);
		
	}
	
	//---------------------------------------------
	//				 Inventory Click
	//---------------------------------------------
	@EventHandler
	public static void inventoryClick(InventoryClickEvent e) {
		
		Inventory i = e.getClickedInventory();
		if(i==null)
			return;
		String title = i.getTitle();
		if(title==null)
			return;
		if(!title.equals(Message.trans("&8Mining World Ticket Shop")))
			return;
		e.setCancelled(true);
		
		ItemStack is = e.getCurrentItem();
		if(!is.hasItemMeta())
			return;
		ItemMeta isMeta = is.getItemMeta();
		if(!isMeta.hasDisplayName())
			return;
		if(isMeta.getDisplayName()==null)
			return;
		if(isMeta.getDisplayName().equals(Message.trans("&c&lCancel Purchase")))
			((Player)e.getWhoClicked()).closeInventory();
		if(!isMeta.getDisplayName().equals(Message.trans("&a&lPurchase Ticket")))
			return;
		
		//Close Inventory, Purchase Ticket
		((Player)e.getWhoClicked()).closeInventory();
		if(Main.getEconomy().getBalance((Player)e.getWhoClicked())>=15000)
			Main.getMiningWorld().purchaseTicket((Player)e.getWhoClicked());
		else
			Message.send((Player)e.getWhoClicked(), "&f[Mining World] &cYou don't have enough money to purchase a ticket");
		
	}
	
	//------------------------------------------
	//				Item Pickup
	//------------------------------------------
	@EventHandler
	public static void itemPickup(InventoryPickupItemEvent e) {
		
		Inventory i = e.getInventory();
		String title = i.getTitle();
		if(title==null)
			return;
		if(!title.equals(Message.trans("&8Mining World Ticket Shop")))
			return;
		e.setCancelled(true);
		
	}
	
	@EventHandler
	public static void foodLoss(FoodLevelChangeEvent e) {
		if(!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if(!p.getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!Main.getMiningWorld().getPlayers().containsKey(p.getUniqueId()))
			return;
		e.setCancelled(true);
		
	}
	
	//---------------------------------------
	//			Get Block Touching
	//---------------------------------------
	public static Boolean isTouching(Material[][][] grid, int _x, int _y, int _z, Material m) {
		
		for(int x = -1; x<2; x++) {
			for(int y = -1; y<2; y++) {
				for(int z = -1; z<2; z++) {
					if(grid==null)
						continue;
					if(x+_x<0||x+_x>15)
						continue;
					if(grid[x+_x]==null)
						continue;
					if(y+_y<0||y+_y>255)
						continue;
					if(grid[x+_x][y+_y]==null)
						continue;
					if(z+_z<0||z+_z>15)
						continue;
					if(grid[x+_x][y+_y][z+_z]==null)
						continue;
					//Don't count corners
					int numCorn = 0;
					if((x==1||x==-1))
						numCorn++;
					if((y==1||y==-1))
						numCorn++;
					if((x==1||x==-1))
						numCorn++;
					if(numCorn>1)
						continue;
					Material _m = grid[x+_x][y+_y][z+_z];
					if(_m.equals(m)) {
						return true;
					}
				}
			}
		}
		
		return false;
		
	}
	
	@EventHandler
	public static void monsterSpawn(CreatureSpawnEvent e) {
		World w = e.getLocation().getWorld();
		if(w.getName().equals("MiningWorld"))
			e.setCancelled(true);
	}
	
	@EventHandler
	public static void weatherChange(WeatherChangeEvent e) {
		World w = e.getWorld();
		if(!w.getName().equals("MiningWorld"))
			return;
		
		new BukkitRunnable() {
			public void run() {
				w.setStorm(false);
				this.cancel();
			}
		}.runTaskTimer(Main.getInstance(), 1, 0);
	
	}
	
	@EventHandler
	public static void blockPlace(BlockPlaceEvent e) {
		if(!e.getBlock().getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!Main.getMiningWorld().getPlayers().containsKey(e.getPlayer().getUniqueId()))
			return;

		if(e.getBlock().getType().equals(Material.STONE)) {
			new BukkitRunnable() {
				public void run() {
					e.getPlayer().getInventory().addItem(new ItemStack(Material.STONE,1));
					cancel();
				}
			}.runTaskTimer(Main.getInstance(), 1, 0);
		}
		if(e.getBlock().getType().equals(Material.TORCH)) {
			new BukkitRunnable() {
				public void run() {
					e.getPlayer().getInventory().addItem(new ItemStack(Material.TORCH,1));
					cancel();
				}
			}.runTaskTimer(Main.getInstance(), 1, 0);
		}
	}
	
	@EventHandler
	public static void blockBreak(BlockBreakEvent e) {
		World w = e.getBlock().getWorld();
		if(!w.getName().equals("MiningWorld"))
			return;
		if(!Main.getMiningWorld().getPlayers().containsKey(e.getPlayer().getUniqueId()))
			return;
		e.setDropItems(false);
		Material type = e.getBlock().getType();
		int cost = 0;
		if(type.equals(Material.IRON_ORE))
			cost=100;
		else if(type.equals(Material.GOLD_ORE))
			cost=200;
		else if(type.equals(Material.DIAMOND_ORE))
			cost=500;
		else if(type.equals(Material.EMERALD_ORE))
			cost=1000;
		Player p = e.getPlayer();
		Main.getEconomy().depositPlayer(p, cost);
	}
	
	@EventHandler
	public static void enemyHit(EntityDamageByEntityEvent e) {
		if(!e.getEntity().getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if(!Main.getMiningWorld().getPlayers().containsKey(p.getUniqueId()))
			return;
		e.setCancelled(true);
	}
	
	@EventHandler
	public static void enemyDamage(EntityDamageEvent e) {
		if(!e.getEntity().getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if(!Main.getMiningWorld().getPlayers().containsKey(p.getUniqueId()))
			return;
		e.setCancelled(true);
	}
	
	@EventHandler
	public static void playerFall(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(!p.getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!Main.getMiningWorld().getPlayers().containsKey(p.getUniqueId()))
			return;
		if(e.getTo().getY()<=0) {
			Location newL = e.getTo();
			newL.setY(258);
			e.setTo(newL);
			Message.send(p, "&f[Mining World] &cYou fell out of the world!");
		}
	}
	
	@EventHandler
	public static void itemDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if(!p.getLocation().getWorld().getName().equals("MiningWorld"))
			return;
		if(!Main.getMiningWorld().getPlayers().containsKey(p.getUniqueId()))
			return;
		e.setCancelled(true);
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent e) {
		removePlayer(e.getPlayer());
	}
	
}
