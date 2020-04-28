package me.ezlikespie.miningworld.main;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.ezlikespie.miningworld.utility.FileHandler;
import me.ezlikespie.miningworld.utility.Message;
import me.ezlikespie.miningworld.utility.WorldHandler;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private static Economy economy;
	
	private static JavaPlugin instance;
	
	private static MiningWorld miningWorld;
	
	private static Connection connection;
	public static String host, database, username, password;
	public static int port;
	
	@Override
	public void onEnable() {
		
		instance = this;
		setupEconomy();
		
		//================ Command Setup ===============
		miningWorld = new MiningWorld(this);
		
	}
	
	public static Boolean stopping = false;
	
	@Override
	public void onDisable() {
		MiningWorld mw = getMiningWorld();
		if(mw!=null) {
			stopping = true;
			mw.stopGame();
		}
	}
	
	//================ Get Mining Game ================
	public static MiningWorld getMiningWorld() {
		return miningWorld;
	}
	
	//================ Get Instance ===============
	public static JavaPlugin getInstance() {
		return instance;
	}
	
	//================ Get Economy ===============
	public static Economy getEconomy() {
		return economy;
	}
	
	//================ Setup Economy =================
	private static void setupEconomy() {
		
		if(Main.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(Message.trans("&c[Warning] &7&l[Mining World] &c&lVault dependency not found!"));
			return;
		}
		
		RegisteredServiceProvider<Economy> rsp = getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
        
        Main.getInstance().getServer().getConsoleSender().sendMessage(Message.trans("&7&l[Mining World] &a&lSetup Successful!"));
		
	}
	
	public void setupDB() {
		
		host = "old.mysql.anvilnode.com";
		port = 3306;
		username = "mc_4188";
		database = "mc_4188";
		password = "175c1fd1f5";
		
		synchronized (this){
			try {
				
				synchronized(this) {
					if(getConnection()!=null && !getConnection().isClosed()) {
						return;
					}
					Class.forName("com.mysql.jdbc.Driver");
					setConnection(DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database,username,password));
				}
				
			} catch (SQLException e) {
			} catch (ClassNotFoundException e) {
			}
		}
		
	}
	
	public static Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection _connection) {
		connection = _connection;
	}
	
}
