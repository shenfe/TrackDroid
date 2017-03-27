package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import log.MethodCreator;
import manifest.PackageData;

import dot.ClassData;
import dot.VarOpItem;
import json.Classes2Json;

import knowledge.SystemApi;
import knowledge.ViewEvent;

public class Main {
	
	public static String dotRootPath;
	public static String dotOutputPath;
	public static String smaliRootPath;
	public static String smaliOutputPath;
	public static String crackSmaliFilePath;
	public static String jsonOutputName;
	public static String manifestFilePath;
    private static ArrayList<String> classes = new ArrayList<String>();
    private static ArrayList<dot.ClassData> classesData = new ArrayList<dot.ClassData>();
	public static boolean debuggable = false;
	public static boolean simpleMode = true;
	public static boolean nonDotMode = true;
	public static boolean writeClassData2Json = false;
	public static HashMap<String, HashSet<Integer>> listOfTargetPkgWithApis = new HashMap<String, HashSet<Integer>>();
	public static SystemApi sysapis = new SystemApi();
	public static ViewEvent viewevents = new ViewEvent();
    
    public static void main(String[] args) throws Exception {
    	String appName = args[0];
    	dotRootPath = "input/dot/" + appName + "/";
    	dotOutputPath = "output/dot/" + appName + "/";
    	smaliRootPath = "input/smali/" + appName + "/";
    	smaliOutputPath = "output/smali/" + appName + "/";
    	crackSmaliFilePath = smaliOutputPath + "smali/crack.smali";
    	jsonOutputName = "../shareIO/" + appName + ".json";
    	manifestFilePath = smaliOutputPath + "AndroidManifest.xml";
    	
    	if(!nonDotMode) VarOpItem.initHashMap();

        clearDir(dotOutputPath);//
    	if(simpleMode || nonDotMode) {
    		dotOutputPath = dotRootPath;
    	} else {
	        System.out.println("[log] Copy .dots: " + dotOutputPath);
	        copyDir(dotRootPath, dotOutputPath);
	        //System.out.println("[log] Classes obtained from dots!");//20160104
    	}

        System.out.println("[log] Copy .smalis: " + smaliOutputPath);
        //System.out.println("[log] getLeafPaths(" + smaliRootPath + ");");
        clearDir(smaliOutputPath);//
        copyDir(smaliRootPath, smaliOutputPath);
        
        copy("input/crack.smali", crackSmaliFilePath);

        if(debuggable) System.out.println("[log] getLeafPaths(" + dotRootPath + ");");
        long t = System.currentTimeMillis();
        
        File[] debagFilesUnderRoot = new File(smaliOutputPath).listFiles();
        if(debagFilesUnderRoot.length == 0) {// || !debagFilesUnderRoot[0].isDirectory()) {
        	System.out.println("Fail to find smali generated just now!");
        	return;
        }
        
        if(args.length > 2) parseTargetApis(args[2]);
        else {
        	HashSet<Integer> tmpHashSet = new HashSet<Integer>();
        	tmpHashSet.add(0);
        	listOfTargetPkgWithApis.put("com/", tmpHashSet);
        }
        
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        	FileWriter crackSmaliAppender = new FileWriter(crackSmaliFilePath, true);
        	for(int apiCounter = 1, apiCount = sysapis.SystemApiList.size(); apiCounter <= apiCount; apiCounter++) {
        		crackSmaliAppender.write(MethodCreator.methodEntry(sysapis.SystemApiList.get(apiCounter - 1), 1, apiCounter));
        	}
        	crackSmaliAppender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Iterator iter = listOfTargetPkgWithApis.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			System.out.println("[log] getLeafPaths(" + ((String) key) + ");");
			Object val = entry.getValue();
			HashSet<String> apisOfKey = new HashSet<String>(); 
			for(Iterator iter2 = ((HashSet<Integer>) val).iterator(); iter2.hasNext(); ) {
				int tmpApiIndex = ((Integer) (iter2.next()));
				apisOfKey.add(tmpApiIndex == 0 ? "self" : sysapis.SystemApiList.get(tmpApiIndex - 1));
			}
			String curKey = ((String) key);
	        if(!nonDotMode) getLeafPaths(new File(dotOutputPath).getAbsolutePath() + "/" + (curKey.endsWith(".smali") ? (curKey.substring(0, curKey.indexOf(".smali")) + "/") : curKey), 0, debuggable, null);
			getLeafPaths(new File(smaliOutputPath).getAbsolutePath() + "/smali/" + curKey, 1, debuggable, apisOfKey);//<-- 要插桩的子目录
		}
		
		getLeafPaths(new File(smaliOutputPath).getAbsolutePath() + "/smali/", 1, debuggable, null);
		
        //getLeafPaths(new File(smaliOutputPath).getAbsolutePath() + "/smali/" + (args.length > 2 ? parseRelativePath(args[2]) : "com/"), 1, debuggable);//<-- 要插桩的子目录
        System.out.println(System.currentTimeMillis() - t);
        System.out.println("[log] Classes modified in smalis!");
        
        if(writeClassData2Json) {
        	Classes2Json cjson = new Classes2Json(jsonOutputName, classesData);
            System.out.println("[log] Classes transformed into json!");
        }
        
        PackageData packageInfo = new PackageData(manifestFilePath);
        packageInfo.addPermission("WRITE_EXTERNAL_STORAGE");
        //PermissionData addPermission = new PermissionData(manifestFilePath, "WRITE_EXTERNAL_STORAGE");
        System.out.println("[log] PackageName: " + packageInfo.packageName);
        System.out.println("[log] MainActivity: " + packageInfo.mainActivity);
        System.out.println("[log] Activities: " + packageInfo.getActivities());
        System.out.println("[log] Services: " + packageInfo.getServices());
        System.out.println("[log] Permissions: " + packageInfo.getPermissions());
        System.out.println("[log] Permission \"WRITE_EXTERNAL_STORAGE\" added into AndroidManifest.xml!");
        packageInfo.dumpToFile(args[1], args[0]);
        System.out.println("[log] Package info has been in file: " + args[1] + "/" + args[0] + ".ini");
    }
    
    public static void parseTargetApis(String arg) {
    	String[] targetApis = arg.split(":");
        for(String targetApi : targetApis) {
        	if(targetApi.equals("")) continue;
        	String pkg = null;
        	int apiIndex = -1;
        	if(targetApi.indexOf('@') >= 0) {
        		pkg = parseRelativePath(targetApi.substring(0, targetApi.indexOf('@')));
        		String api = targetApi.substring(targetApi.indexOf('@') + 1);
        		if(api.length() > 4) {
        			if(sysapis.SystemApiIndex.containsKey(api)) {
        				apiIndex = sysapis.SystemApiIndex.get(api);
        			} else {
        				sysapis.SystemApiList.add(api);
        				apiIndex = sysapis.SystemApiList.size();
        				sysapis.SystemApiIndex.put(api, apiIndex);
        			}
        		}
        		else apiIndex = Integer.parseInt(api);
        		if(apiIndex <= 0 || apiIndex > sysapis.SystemApiList.size()) {
        			apiIndex = 0;
        		}
        	} else {
        		pkg = parseRelativePath(targetApi);
        		apiIndex = 0;
        	}
        	
    		if(!listOfTargetPkgWithApis.containsKey(pkg)) listOfTargetPkgWithApis.put(pkg, new HashSet<Integer>());
    		
    		HashSet<Integer> apis = listOfTargetPkgWithApis.get(pkg);
			if(apis == null) apis = new HashSet<Integer>();
			apis.add(apiIndex);
			listOfTargetPkgWithApis.put(pkg, apis);
        }
        
        Iterator iter1 = listOfTargetPkgWithApis.entrySet().iterator();
		while (iter1.hasNext()) {
			Map.Entry entry = (Map.Entry) iter1.next();
			String key = (String) (entry.getKey());
			HashSet<Integer> value = (HashSet<Integer>) (entry.getValue());

			ArrayList<String> pkgPrefix = new ArrayList<String>();
			boolean flag = (key.equals("") || (key.charAt(0) == '/'));
			String tmpApiPkg = (flag ? key : ("/" + key));
			for(int ii = 0, nn = tmpApiPkg.length(); ii < nn; ii++) {
				if(tmpApiPkg.charAt(ii) == '/') {
					String tmpPrefix = tmpApiPkg.substring(0, ii);
					//System.out.println("tmpPrefix: " + tmpPrefix);
					if(tmpPrefix.equals("")) pkgPrefix.add(tmpPrefix);
					else {
						if(tmpPrefix.charAt(0) == '/' && !flag) {
							pkgPrefix.add(tmpPrefix.substring(1));
						} else {
							pkgPrefix.add(tmpPrefix);
						}
					}
				}
			}
			
			Queue<Integer> indexToRemove = new LinkedList<Integer>();
			for(Iterator iter2 = value.iterator(); iter2.hasNext(); ) {
				int tmpApiIndex = ((Integer) (iter2.next()));
				
				for(String prefix : pkgPrefix) {
					if(!listOfTargetPkgWithApis.containsKey(prefix)) continue;
					HashSet<Integer> _value = listOfTargetPkgWithApis.get(prefix);
					if(_value.contains(tmpApiIndex)) {
						indexToRemove.add(tmpApiIndex);
					}
				}
			}
			while(!indexToRemove.isEmpty()) {
				int t = indexToRemove.poll();
				value.remove(t);
			}
			listOfTargetPkgWithApis.put(key, value);
		}
    }
    
    public static String parseRelativePath(String p) {
    	String r = p + ((p.length() == 0 || (p.length() > 0 && p.charAt(p.length() - 1) != '/' && !p.endsWith(".smali"))) ? "/" : "");
    	if(r.charAt(0) == '/') return r.substring(1);
    	return r;
    }
    
    public static void getLeafPaths(String strPath, int choice, boolean debug, HashSet<String> specificApis) {
        File dir = new File(strPath);
        if(dir.isFile()) {
        	dot.ClassData[] clses = classesData.toArray(new dot.ClassData[0]);
    		
			if(dir.getName().indexOf(".smali") < 0) return;
			if(debug) System.out.println("[log] new smaliClassData(" + dir.getAbsolutePath() + ");");
        	try {
        		String dirFilePath = dir.getCanonicalPath();
				smali.ClassData classdt = new smali.ClassData(dirFilePath, 
						nonDotMode ? null : dot.ClassData.getClassDataByID(clses, dirFilePath.substring(dirFilePath.lastIndexOf("smali/") + 6, dirFilePath.indexOf(".smali"))), 
						specificApis, sysapis, viewevents, crackSmaliFilePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	
    		return;
        }
        File[] files = dir.listFiles();
        if (files == null)
            return;
        
        //cunzai wenti! Jiyou wenjian youyou wenjianjia shi, wenjian huibei hulue!
        if(dirIsleafPath(files)){
        	if(choice == 0){
            	if(debug) System.out.println("[log] new dotClassData(" + strPath + ");");
            	dot.ClassData classdt = new dot.ClassData(strPath);//视为leafPath，生成该类下的所有数据
            	classes.add(classdt.getID());
            	classesData.add(classdt);//a class of dot
        	}
        	else{
        		//
        	}
        	//return;
        }
        
        for (File f : files) {
            if (f.isDirectory()) {
            	if(debug) System.out.println("[log] getLeafPaths(" + f.getAbsolutePath() + ");");
            	getLeafPaths(f.getAbsolutePath(), choice, debug, specificApis);
            } else if(choice != 0) {
    			if(f.getName().indexOf(".smali") < 0) continue;
    			if(debug) System.out.println("[log] new smaliClassData(" + f.getAbsolutePath() + ");");
            	try {
            		String smlFilePath = f.getCanonicalPath();
					smali.ClassData classdt = new smali.ClassData(smlFilePath, null, 
							specificApis, sysapis, viewevents, crackSmaliFilePath);
							//sml.getName().substring(0, sml.getName().indexOf(".smali"))));//a class of smali
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
    
    //判断files所在的路径是否是leafPath
    //gaiwei panduan files zhong shifou you wenjian!
    public static boolean dirIsleafPath(File[] files){
    	for (File f : files) {
    		if(!f.isDirectory())
    			return true;
    	}
    	return false;
    }
    
    //copy a folder
    public static void copyDir(String sourcsPath, String targetPath) throws Exception {
    	File sourceFile = new File(sourcsPath);
    	if(sourceFile.isDirectory()){
    		File targetFile = new File(targetPath);
    		if(!targetFile.exists())
    			targetFile.mkdir();
    		File[] sourceFiles = sourceFile.listFiles();
    		for (File f : sourceFiles){
    			copyDir(f.toString(), targetPath + "/" + f.getName());
    		}
    	}
    	else{
    		copy(sourcsPath,targetPath);
    	}
    }

    //拷贝文件
    public static void copy(String path1, String path2) throws IOException {
    	DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path1)));
    	byte[] date = new byte[in.available()];
    	in.read(date);
    	DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path2)));
    	out.write(date);
    	in.close();
    	out.close();
    }
    
    //move a folder
	public static void moveDir(String oldPath, String newPath) {
		try {
			File srcFolder = new File(oldPath);
			File destFolder = new File(newPath);
			File newFile = new File(destFolder.getAbsoluteFile() + "\\"	+ srcFolder.getName());
			srcFolder.renameTo(newFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//delete a folder
	public static void deleteDir(String folderPath) {
		try {
			clearDir(folderPath); //删除完里面所有内容
			File myFilePath = new File(folderPath);
			myFilePath.delete(); //删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//delete files in a folder
	public static boolean clearDir(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				clearDir(path + "/" + tempList[i]);//先删除文件夹里面的文件
				deleteDir(path + "/" + tempList[i]);//再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
    
}