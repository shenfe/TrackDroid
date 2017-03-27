package statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import util.Trie;
//import util.Trie.TrieNode;

public class PackagePaths {
	private String path;
	private int nodeCountLimit = 10;
	private Trie tree;
/*	public PackagePaths(String rootPath) {
		this.path = rootPath;
		String[] commands = new String[]{"/bin/bash", "-c", "cd " + this.path + " && find . -type d -links 2"};
		List<String> paths = new ArrayList<String>();
	    try {
	        Process proc = new ProcessBuilder(commands).start();
	        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

	        String s = null;
	        while ((s = stdInput.readLine()) != null) {
	        	paths.add(this.clean(s));
	        }

	        while ((s = stdError.readLine()) != null) {
	        	paths.add(this.clean(s));
	        }
	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	    
	    this.tree = new Trie();
	    for(String pkg : paths) {
	    	this.tree.addPackage(pkg);
	    }
	    
	    this.tree.outputPackages(3, true);
	}*/
	public PackagePaths(String[] rootPaths, int outputDepth, int nodeLimit) {
		this.tree = new Trie();
		this.nodeCountLimit = nodeLimit;
		for(int i = 0, n = rootPaths.length; i < n; i++) {
			this.path = rootPaths[i];
			System.out.println(Integer.toString(i) + ": " + this.path);
			if(!new File(this.path).isDirectory()) continue;
			String[] commands = new String[]{"/bin/bash", "-c", "cd " + this.path + " && find . -type d -links 2"};
			List<String> paths = new ArrayList<String>();
		    try {
		        Process proc = new ProcessBuilder(commands).start();
		        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	
		        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	
		        String s = null;
		        while ((s = stdInput.readLine()) != null) {
		        	paths.add(this.clean(s));
		        }
	
		        while ((s = stdError.readLine()) != null) {
		        	paths.add(this.clean(s));
		        }
		    } catch (IOException e) {
		        System.out.println(e.getMessage());
		    }
		    
		    Trie temptree = new Trie();
		    for(String pkg : paths) {
		    	temptree.addPackage(pkg);
		    }
		    this.tree.union(temptree);
		}
	    
	    this.tree.outputPackages(outputDepth, true, this.nodeCountLimit);
	}
	
	private String clean(String s) {
		String ss = s.replace('/', '.');
		List<String> list = new ArrayList<String>();
		String[] parts = ss.split("\\.");
		for(String part : parts) {
			if(part.equals("")) continue;
			list.add(part);
		}
		return String.join(".", list.toArray(new String[list.size()]));
	}
	
	private static final class myFileFilter implements FileFilter {

		//public String rootPath;
		public String prefix;
		
		public myFileFilter(String p) {//, String r) {
			//this.rootPath = r;
			System.out.println(p);
			this.prefix = p;
		}
		
		@Override
		public boolean accept(File pathname) {
			
			if(!pathname.isDirectory()) {
				//System.out.println("Not dir!");
				return false;
			}
			String filename = pathname.getName().toLowerCase();  
            if(this.prefix.equals("") || filename.startsWith(this.prefix)) {
            	//System.out.println("Name matched!");
                return true;  
            } else {  
            	//System.out.println("Name not matched!");
                return false;  
            }
		}
		
	}
	
	/**
	 * args[0]: root dir, like "/home/santoku/Workspace/Proj201404/dot2smali/input/smali"
	 * args[1]: depth of wanted nodes in the Trie tree, like 3
	 * args[2]: limitation of node's count, like 10
	 * args[3]: prefix of an app folder name at root, like "googleplay_"
	 * args[4]: the path from an app folder to the smali folder, like "smali"
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*
        Scanner in = new Scanner(System.in);
        PackagePaths pp = new PackagePaths(in.next());
        
        String[] paths = new String[2];
        paths[0] = in.next();
        paths[1] = in.next();
        pp = new PackagePaths(paths);
        */
		if(args.length < 3) return;
		File root = new File(args[0]);
		//System.out.println(root.getCanonicalPath());
		if(root.exists() && root.isDirectory()) {
			File[] appFolders = root.listFiles(new myFileFilter(args.length > 3 ? args[3] : ""));
			int appCount = appFolders.length;
			//System.out.println("count: " + Integer.toString(appCount));
			String[] smaliFolders = new String[appCount];
			for(int i = 0; i < appCount; i++) {
				smaliFolders[i] = appFolders[i].getCanonicalPath() + "/" + (args.length > 4 ? args[4] : "smali");
				//System.out.println(smaliFolders[i]);
			}
			int depth = Integer.parseInt(args[1]);
			int limit = Integer.parseInt(args[2]);
			//System.out.println("depth: " + Integer.toString(depth));
			PackagePaths pp = new PackagePaths(smaliFolders, depth, limit);
		}
    }
}
