package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Executor {
	private String cmd;
	public Executor(String command) {
		cmd = command;
	    exec(command);
	}
	
	private void exec(String command) {
		String[] commands = new String[]{"/bin/bash", "-c", command};
	    try {
	        Process proc = new ProcessBuilder(commands).start();
	        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

	        String s = null;
	        while ((s = stdInput.readLine()) != null) {
	        	// todo with s:
	        }

	        while ((s = stdError.readLine()) != null) {
	            // todo with s:
	        }
	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	}
}
