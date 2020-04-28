package me.ezlikespie.miningworld.utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Message {

	//--------------------------------
	//		 		Log
	//--------------------------------
	public static void log(String... args) {
		Bukkit.getServer().getConsoleSender().sendMessage(Message.trans(args));
	}
	
    //--------------------------------
    //         Translate Message
    //--------------------------------
    public static String trans(String... args){

        String msg = "";
        for(int i = 0; i<args.length; i++){
            msg+=args[i];
        }
        msg = ChatColor.translateAlternateColorCodes('&', msg);

        return msg;

    }

    //--------------------------------
    //      Send Message (Trans)
    //--------------------------------
    public static void send(Player p, String... args){

        Message.send(p,true,args);

    }

    //--------------------------------
    //     Send Message (Options)
    //--------------------------------
    public static void send(Player p, Boolean trans, String... args){

        if(trans){
            p.sendMessage(Message.trans(args));
        }
        else {
            String msg = "";
            for(int i = 0; i<args.length; i++){
                msg+=args[i];
            }
            p.sendMessage(msg);
        }

    }
    
    //---------------------------------
    //			  Combine
    //---------------------------------
    public static String combine(String... args) {
    	String msg = "";
    	for(int i = 0; i<args.length; i++) {
    		msg+=args[i];
    	}
    	return msg;
    }
    
    //-----------------------------------------
    //				Broadcast
    //-----------------------------------------
    public static void broadcast(String... args) {
    	String msg = trans(args);
    	for(Player p : Bukkit.getServer().getOnlinePlayers()) {
    		Message.send(p, msg);
    	}
    }

}
