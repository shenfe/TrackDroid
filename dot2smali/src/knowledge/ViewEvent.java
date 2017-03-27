package knowledge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewEvent {
	public String fileName;
	public HashMap<String, Integer> ViewEventIndex = new HashMap<String, Integer>();
	public ArrayList<String> ViewEventList = new ArrayList<String>();
	public ViewEvent() {
		this.loadConfig("../config/view_event_handler_collection.txt");
	}
	public ViewEvent(String filePath) {
		this.loadConfig(filePath);
	}
	public void loadConfig(String filePath) {
		this.fileName = filePath;
		try {
            File fr = new File(filePath);
    		
            if(fr.isFile() && fr.exists()){
            	
            	String encoding = "UTF-8";
            	
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fr), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
    			
    			int lineNum = 0;
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                	String curApiStr = line.trim();
                	String curApi = null;
                	if(!curApiStr.equals("")) {
                    	if(curApiStr.indexOf('#') > 0) {
                    		curApi = curApiStr.substring(0, curApiStr.indexOf('#'));
                    	} else {
                    		curApi = curApiStr;
                    	}
                    	
                		if(this.ViewEventIndex.containsKey(curApi)) continue;
                		lineNum++;
                		this.ViewEventIndex.put(curApi, lineNum);
                		this.ViewEventList.add(curApi);
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
}
