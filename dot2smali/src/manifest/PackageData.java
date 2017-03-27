package manifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PackageData {
	public String fileName;
	
	public String packageName;
	private String packageNamePattern0 = "<manifest ";
	private String packageNamePattern1 = " package=\"";
	
	public String mainActivity;
	private String mainActivityPattern0 = "<action ";
	private String mainActivityPattern1 = " android:name=\"android.intent.action.MAIN\"";
	private String mainActivityPattern2 = " android:name=\"android.intent.category.LAUNCHER\"";
	
	public ArrayList<String> activities = new ArrayList<String>();
	private String activityPattern0 = "<activity ";
	private String activityPattern1 = " android:name=\"";
	
	public ArrayList<String> services = new ArrayList<String>();
	private String servicePattern0 = "<service ";
	private String servicePattern1 = " android:name=\"";
	
	public ArrayList<String> receivers = new ArrayList<String>();
	private String receiverPattern0 = "<receiver ";
	private String receiverPattern1 = " android:name=\"";
	
	public ArrayList<String> providers = new ArrayList<String>();
	private String providerPattern0 = "<provider ";
	private String providerPattern1 = " android:name=\"";
	
	public ArrayList<String> permissions = new ArrayList<String>();
	private String permissionPattern0 = "<uses-permission ";
	private String permissionPattern1 = " android:name=\"";
	
	public PackageData(String filePath) {
		this.fileName = filePath;
		try {
            File fr = new File(filePath);
    		
            if(fr.isFile() && fr.exists()){
            	
            	String encoding = "UTF-8";
            	
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fr), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
    			
    			int mainActivityFound = 0;
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                	if(line.indexOf("<intent-filter>") >= 0) {
                		if(mainActivityFound >= 11) {
                			while((line = bufferedReader.readLine()) != null) {
	                			if(line.indexOf("</intent-filter>") >= 0) break;
	                		}
                		} else {
	                		while((line = bufferedReader.readLine()) != null) {
	                			if(line.indexOf("</intent-filter>") >= 0) break;
	                			if(line.indexOf(this.mainActivityPattern1) >= 0) {
	                				mainActivityFound += 1;
	                			} else if(line.indexOf(this.mainActivityPattern2) >= 0) {
	                				mainActivityFound += 10;
	                			}
	                		}
	                		if(mainActivityFound >= 11) {
	                			this.mainActivity = this.activities.get(this.activities.size() - 1);
	                		}
                		}
                	} else {
                		this.getValueInLine(line);
                	}
                }
                
                reader.close();
	        }
            else{
            	System.out.println("No such a file: " + filePath);
	        }
        }
        catch(IOException e){
            e.printStackTrace();
        }
	}
	
	private boolean getValueInLine(String line) {
		String p1 = null;
		int flag = -1;
		if(line.indexOf(this.packageNamePattern0) >= 0) {
			flag = 0;
			p1 = packageNamePattern1;
		} else if(line.indexOf(this.activityPattern0) >= 0) {
			flag = 1;
			p1 = activityPattern1;
		} else if(line.indexOf(this.servicePattern0) >= 0) {
			flag = 2;
			p1 = servicePattern1;
		} else if(line.indexOf(this.receiverPattern0) >= 0) {
			flag = 3;
			p1 = receiverPattern1;
		} else if(line.indexOf(this.providerPattern0) >= 0) {
			flag = 4;
			p1 = providerPattern1;
		} else if(line.indexOf(this.permissionPattern0) >= 0) {
			flag = 5;
			p1 = permissionPattern1;
		} else {
			return false;
		}
		
		int pos0 = line.indexOf(p1);
		if(pos0 < 0) return false;
		pos0 += p1.length();
		int pos1 = line.indexOf('"', pos0);
		if(pos1 < 0) return false;
		String originVal = line.substring(pos0, pos1);
		String val = null;
		if(flag != 0) {
			if(originVal.charAt(0) == '.') {
				val = this.packageName + originVal;
			} else if(originVal.indexOf('.') > 0) {
				val = originVal;
			} else {
				val = this.packageName + "." + originVal;
			}
		}
		
		if(flag == 0) {
			val = originVal;
			this.packageName = val;
		} else if(flag == 1) {
			this.activities.add(val);
		} else if(flag == 2) {
			this.services.add(val);
		} else if(flag == 3) {
			this.receivers.add(val);
		} else if(flag == 4) {
			this.providers.add(val);
		} else if(flag == 5) {
			this.permissions.add(val);
		} else {
			return false;
		}
		return true;
	}
	
	private String arrayListToString(ArrayList<String> strs) {
		StringBuilder sb = new StringBuilder("");
		for(String s : strs) {
			sb.append(s + "  ");
		}
		return sb.toString();
	}
	
	public String getActivities() {
		return this.arrayListToString(this.activities);
	}
	public String getServices() {
		return this.arrayListToString(this.services);
	}
	public String getPermissions() {
		return this.arrayListToString(this.permissions);
	}
	
	public void addPermission(String permission) {
		String filePath = this.fileName;
		try {
            File fr = new File(filePath);
    		
            if(fr.isFile() && fr.exists()){
            	
            	String encoding = "UTF-8";
	    		String newFilePath = filePath.substring(0, filePath.indexOf(".xml")) + "[new].xml";
            	
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fr), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
    			FileWriter writer = new FileWriter(newFilePath, true);
    			
    			String newPermission = "<user-permission android:name=\"android.permission." + permission + "\" />";
    			boolean flag = false;
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                	String lineStr = line.trim();
                	
                	if(lineStr.indexOf(permission) >= 0) flag = true;
                	
                	if(!flag && lineStr.indexOf("<user-permission ") >= 0){
                		writer.write(line + "\n");
                		writer.write(newPermission + "\n");
                		flag = true;
                		continue;
                	}
                	
                	writer.write(line + "\n");
                }
                
                reader.close();
                writer.close();
                
                fr.delete();
                fr = new File(newFilePath);
                fr.renameTo(new File(filePath));
	        }
            else{
            	System.out.println("No such a file: " + filePath);
	        }
        }
        catch(IOException e){
            e.printStackTrace();
        }
	}
	
	public boolean dumpToFile(String apkPath, String apkName) {
		String filePath = "../" + apkPath + (apkPath.endsWith("/") ? "" : "/") + apkName + ".ini";
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write("[BASE]");
			writer.write("\npkgname = " + this.packageName);
			writer.write("\nmainactivity = " + this.mainActivity);
			// todo: add other items
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
