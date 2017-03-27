package knowledge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SystemApi {
	public String fileName;
	public HashMap<String, Integer> SystemApiIndex = new HashMap<String, Integer>();
	public HashMap<String, String> SystemApiParaPosition = new HashMap<String, String>();
	public ArrayList<String> SystemApiList = new ArrayList<String>();
	public SystemApi() {
		this.loadConfig("../config/api_collection.txt");
	}
	public SystemApi(String filePath) {
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
                	String apiCoreValue = null;
                	if(!curApiStr.equals("")) {
                    	if(curApiStr.indexOf('#') > 0) {
                    		curApi = curApiStr.substring(0, curApiStr.indexOf('#'));
                    		apiCoreValue = curApiStr.substring(curApiStr.indexOf('#') + 1).trim();
                    	} else {
                    		curApi = curApiStr;
                    		apiCoreValue = "";
                    	}
                    	
                		if(this.SystemApiIndex.containsKey(curApi)) continue;
                		lineNum++;
                		this.SystemApiIndex.put(curApi, lineNum);
                		if(!apiCoreValue.equals("")) this.SystemApiParaPosition.put(curApi, apiCoreValue);
                		this.SystemApiList.add(curApi);
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
