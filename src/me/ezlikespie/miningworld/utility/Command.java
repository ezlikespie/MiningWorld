package me.ezlikespie.miningworld.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.ezlikespie.miningworld.main.Main;

public abstract class Command implements Listener {

    //============ Execute Method ==============
    public abstract void execute(Player p, String[] args);

    //============ Setup Events ==============
    public void setup(JavaPlugin plugin){

        Bukkit.getServer().getPluginManager().registerEvents(this,plugin);

    }

    
    //Names and Aliases for Command
    public List<String> names = new ArrayList<String>();

    //Permissions
    public List<List<String>> permissions = new ArrayList<List<String>>();
    
    //Error Message
    public String error = Message.trans("&cYou do not have permission to execute that command!");

    //=========== Set Permission Error Message ===========
    public void setMessage(String msg) {
    	
    	error = msg;
    	
    }
    
    //=========== Add Permissions ==========
    public void setPermissions(String... args){

        //====== How Permissions Work ======
            //Step 1: Player Sends Command
            //Step 2: Server Checks if Player Has Permissions
                //Step 2a: Each ArrayList<String> (let's call them "sublists") in the permissions ArrayList<ArrayList<String>> can have multiple permissions
                //         A sublist will return true if the player has all of the permissions in the sublist
                //         If any of the sublists return true then the command will run.
            //Step 3: Command Runs

        List<String> newPermArray = new ArrayList<String>();
        for(int i = 0; i<args.length; i++){
            newPermArray.add(args[i]);
        }
        permissions.add(newPermArray);

    }
    
    public void setPermission(String... args) {
    	setPermissions(args);
    }

    //============ Set Name ============
    public void setName(String... args){

        for(int i = 0; i<args.length; i++){

            names.add(args[i]);

        }

    }

    private static List<String> allowedCommands;
    
    public static void commandConfig() {
    	if(!FileHandler.exists(Main.getInstance().getDataFolder()))
    		FileHandler.newDir(Main.getInstance().getDataFolder());
    	if(!FileHandler.exists(new File(Main.getInstance().getDataFolder(),"allowed-commands.yml")))
    		FileHandler.newFile(new File(Main.getInstance().getDataFolder(),"allowed-commands.yml"));
    	String[] commands = FileHandler.readLines(new File(Main.getInstance().getDataFolder(),"allowed-commands.yml"));
    	allowedCommands = Arrays.asList(commands);
    	
    }
    
    //=========== Command Event ==========
    @EventHandler
    public void commandEvent(PlayerCommandPreprocessEvent e){
    	
    	if(allowedCommands==null)
    		commandConfig();
    	if(Main.getMiningWorld().getPlayers().containsKey(e.getPlayer().getUniqueId())) {
    		if(!allowedCommands.contains(e.getMessage().replaceFirst("/", "").split(" ")[0])) {
    			e.setCancelled(true);
    			return;
    		}
    	}
    	
        //Correct Command
        if(!names.contains(e.getMessage().replaceFirst("/", "").split(" ")[0])) {
        	return;
        }
        e.setCancelled(true);
        
        //============ Permissions ============
        Boolean isTrue = false;
        Iterator<List<String>> it = permissions.iterator();
        if(it.hasNext()==false)
            isTrue = true;
        while(it.hasNext()){
            Iterator<String> it2 = it.next().iterator();
            while(it2.hasNext()){
            	String perm = it2.next();
                if((!e.getPlayer().hasPermission(perm))&&!(perm.equals("op")&&e.getPlayer().isOp())){
                    break;
                }
                if(!it2.hasNext()){
                    isTrue = true;
                    break;
                }
                if(isTrue)
                    break;
            }
        }

        if(!isTrue) {
        	if(error==null)
        		return;
        	Message.send(e.getPlayer(), false, error);
            return;
        }

        String[] _args = e.getMessage().split(" ");
        String[] args = new String[_args.length-1];
        for(int i = 1; i<_args.length; i++) {
            args[i-1] = _args[i];
        }

        //Run Command
        execute(e.getPlayer(), args);
    }

}
