package me.ezlikespie.miningworld.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

	public static String[] readLines(File f){
		
		List<String> lines = new ArrayList<String>();
        try {
            
            FileReader fileReader = 
                new FileReader(f);

            
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            String currentLine;
            while((currentLine = bufferedReader.readLine())!=null) {
            	lines.add(currentLine);
            }
            
            bufferedReader.close();         
            
            String[] _lines = new String[lines.size()];
            for(int i = 0; i<lines.size(); i++) {
            	_lines[i] = lines.get(i);
            }
            return _lines;
            
        }
        catch(Exception e) {}
		
        return null;
		
	}
	
	public static void newFile(File f) {
		try {
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(f.getAbsolutePath()), "utf-8"));
			writer.close();
			
		}
		catch(Exception e) {}
	}
	
	public static void newDir(File f) {
		f.mkdirs();
	}
	
	//============= See if File Exists ===============
	public static Boolean exists(File f) {
		if(f.exists())
			return true;
		return false;
	}
	
	//============= Write File ==========
	public static void write(File f, String[] lines) {
		
		try {
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(f.getAbsolutePath()), "utf-8"));
			for(int i = 0; i<lines.length; i++) {
				writer.write(lines[i]);
			}
			writer.close();
			
		}
		catch(Exception e) {}
		
	}
	
}
