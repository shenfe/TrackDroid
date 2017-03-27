package smali;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import knowledge.SystemApi;
import knowledge.ViewEvent;
import log.MethodCreator;

import dot.*;

public class ClassData {

	private String inputSmaliPath;
	private String crackSmaliPath;
	private String classID;
	private String dirPath;
	private String[] vars;
	private String[] methods;
	private boolean debug = false;
	private boolean logMethod = false; // if need to log every method in this class
	private int logDegree = 1; // 0: log every code-block; 1: log every method
	
	private HashSet<String> targetApis;
	private SystemApi sysapis;
	private ViewEvent viewevents; // if specificApis is null, instrument with viewEvents; else instrument without viewEvents.
	private boolean nonDotMode;
	
	public ClassData(String fileName, dot.ClassData classdt, HashSet<String> specificApis, SystemApi thoseapis, ViewEvent thoseevents, String crackSmaliFile){
		nonDotMode = (classdt == null);
		
		crackSmaliPath = crackSmaliFile;
		
		targetApis = specificApis;
		sysapis = thoseapis;
		viewevents = thoseevents;
		
		if(targetApis != null) logMethod = targetApis.contains("self");
		
		if(!nonDotMode) classID = classdt.getID();
		else {
			classID = "L" + fileName.substring(fileName.lastIndexOf("smali/") + 6, fileName.indexOf(".smali"));
		}
		if(debug) System.out.println("classID: " + classID);
		inputSmaliPath = fileName;
		vars = null;//classdt.getVarNames();//20160104
		if(!nonDotMode) methods = classdt.getMethodNames();
		else methods = new String[0];
		
		findCodeBlocks(classdt);
	}
	
	/*
	 * 扫描一个smali文件时，根据其classID从dot文件中得到对应classdata；
	 * 遇到# instance fields，则认为是<init>-BB@0x0代码块；
	 * 遇到.method p时，得到其methodID，得到classdata中对应methoddata，从前往后，顺序对应methoddata中codeblock的ID
	 */
	public void findCodeBlocks(dot.ClassData classdt){		
		try {
            File fr = new File(inputSmaliPath);
    		
            if(fr.isFile() && fr.exists()){
            	
            	String encoding = "UTF-8";
	    		String newSmaliName = inputSmaliPath.substring(0, inputSmaliPath.indexOf(".smali")) + "[new].smali";
            	//String newSmaliName = inputSmaliPath.substring(0, inputSmaliPath.indexOf(".smali")) + ".smali";
	    		
	    		//MethodData curMethoddt = new MethodData();//20160104
	    		String methodnm = null;
	    		String methodnm_simple = null;
	    		int codeblockIndex = 0;
	    		int codeblockLineIndex = 0;
	    		int writeState = 0;//1表示正常在method体内
	    		boolean virtualMethod = false;
	    		boolean methodLogged = false;
	    		int dotParas = 0;//how many parameters the current method has. Important!
	    		int dotLocals = 0;//how many non-parameter registers the current method will use. Important!
	    		int registerTooMany = 0;//if the number of registers is larger than 16
	    		ArrayList<String> codeblockLines = new ArrayList<String>();
	    		
	    		boolean annotation = false;
            	
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fr), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
    			FileWriter writer = new FileWriter(newSmaliName, true);
    			
    			FileWriter writerCracker = new FileWriter(crackSmaliPath, true);
    			
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                	String lineStr = line.trim();
                	
                	if(lineStr.equals("# virtual methods")) {
                		writer.write(line + "\n");
                		virtualMethod = true;
                		continue;
                	}
                	
                	//packed-switch属于代码块中可能出现的dalvik指令，前面带有'.'，其他类似情况还有待枚举！
                	if(lineStr.equals("") || lineStr.charAt(0) == ':' || 
                			(lineStr.charAt(0) == '.' && !lineStr.startsWith(".method") && !lineStr.startsWith(".end method") &&
                					!lineStr.startsWith(".locals ") && !lineStr.startsWith(".packed-switch"))){
                		writer.write(line + "\n");
                		if(lineStr.startsWith(".annotation")) {
                			annotation = true;
                		} else if(lineStr.startsWith(".end annotation")) {
                			annotation = false;
                		}
                		continue;
                	}
                	
                	//<init>
                	if(lineStr.indexOf("# instance fields") >= 0){
                		writer.write(line + "\n");
                		//writer.write(insertLogCB("<init>-BB@0x0", true));//init不需要log输出代码块信息！
                		continue;
                	}
                	
                	//.method
                	if(lineStr.startsWith(".method ")){
                		if(debug) System.out.print("\n.method ");
                		methodnm = containMethod(lineStr, this.methods);//classdt.getMethodNames());//20160104
                		methodnm_simple = lineStr.substring(lineStr.lastIndexOf(' ') + 1);
                		if(debug) System.out.println("methodnm: ");
                		if(!methodnm.equals("")){
                			//dotParas = countMethodParas(lineStr); // parse the number of parameters this method requires//20160215
                			if(debug) System.out.print(methodnm + "\n");
                			codeblockIndex = 0;
                			codeblockLineIndex = 0;
                			//curMethoddt = classdt.getMethodDataByName(methodnm);//20160104
                			//if(curMethoddt.getCodeblocks().length > 0)//20160104
                				writeState = 1;
                		}
                		writer.write(line + "\n");
                		continue;
                	}
                	
                	//.end method
                	if(lineStr.equals(".end method")){
                		writeState = 0;
                		methodLogged = false;
                		virtualMethod = false;
                		registerTooMany = 0;
                		writer.write(line + "\n");
                		continue;
                	}
                	
                	//.registers
                	if(lineStr.startsWith(".locals ") && writeState == 1){
            			dotLocals = Integer.parseInt(lineStr.substring(8)) + 2;
            			//if(dotLocals + dotParas >= (virtualMethod ? 16 : 16)){ // sometimes "this" is an implicit parameter
            			//	registerTooMany = 1;
            			//	writer.write(line + "\n");
            			//} else {
            				registerTooMany = 0;
            				//writer.write(line.substring(0, line.indexOf(".locals ") + 8) + Integer.toString(dotLocals) + "\n");//20160215
            				writer.write(line + "\n");//20160215
            			//}
            			continue;
            		}
                	
                	//现在的比对算法还存在很大问题！不够鲁棒，默认为代码块的指令谓词完全相等
                	//在method体内，且该行开头的指令与当前代码块的当前行指令相同
					if(writeState == 1 && registerTooMany == 0 && !lineStr.equals("")){
                		//if(debug) System.out.println(curMethoddt.getCodeblocks()[codeblockIndex].getID());//20160104
                		//codeblockLines.add(line);
                		
                		if(!annotation) {
                		
	                		codeblockLineIndex++;
	                		if(codeblockLineIndex == 1){// onCreate不需要log输出代码块信息！
	                			//if(logDegree == 0) writer.write(insertLogCB(curMethoddt.getCodeblocks()[codeblockIndex].getID(), false, dotLocals));//20160104
	                			//else if(!methodLogged && targetApis.contains("self")) writer.write(insertLogM(curMethoddt.getID(), false, dotLocals));//20160104
	                			if(!methodLogged) {
	                				if(logMethod) {
	                					if(!viewevents.ViewEventIndex.containsKey(methodnm_simple)) {
			                				//writer.write(insertLogM(methodnm, false, dotLocals));
			                				writerCracker.write(MethodCreator.methodEntry(methodnm, 0, 0));
			                				writer.write(insertLogMethod(false, MethodCreator.methodCounter));
	                					}
	                				} else if(targetApis == null && viewevents.ViewEventIndex.containsKey(methodnm_simple)) {
	                					writerCracker.write(MethodCreator.methodEntry(methodnm, -1, 0));
		                				writer.write(insertLogMethod(false, MethodCreator.methodCounter));
	                				}
	                			}
	                			methodLogged = true;
	                		}
                		
	                		// consider the API:
	                		if(targetApis != null && lineStr.startsWith("invoke-")) {
	                			int invokePos0 = lineStr.indexOf("}, ");
	                			if(invokePos0 > 0) {
            						
            						//if(lineStr.indexOf("Ldalvik") >= 0) System.out.println("this is an api: " + lineStr);
            						
	                				int invokePos1 = lineStr.indexOf('(', invokePos0);
	                				if(invokePos1 > 0) {
	                					String invokeApi = lineStr.substring(invokePos0 + 3, invokePos1).trim();
	                					if(!targetApis.contains(invokeApi)) {
	                						invokeApi = lineStr.substring(invokePos0 + 3, lineStr.indexOf(')', invokePos1) + 1).trim();
	                					}
	                					if(targetApis.contains(invokeApi)) {
	                						//writer.write(insertLogApi(invokeApi, false, dotLocals));
	                						writer.write(insertLogApi(false, sysapis.SystemApiIndex.get(invokeApi)));

	                						//System.out.println("line: " + line);
	                						//System.out.println("targetApis contains: " + invokeApi + ", " + Integer.toString(sysapis.SystemApiIndex.get(invokeApi)));
	                						
	                						if(sysapis.SystemApiParaPosition.containsKey(invokeApi)) {
		                						
		                						//System.out.println("sysapis.SystemApiParaPosition contains: " + invokeApi);
		                						
	                							invokePos0 = lineStr.indexOf('{');
	                							if(invokePos0 > 0) {
		                							invokePos1 = lineStr.indexOf('}', invokePos0 + 1);
		                							if(invokePos1 > 0) {
			                							String apiParaPattern = sysapis.SystemApiParaPosition.get(invokeApi);
			                							
			                							String paraRegs = lineStr.substring(invokePos0 + 1, invokePos1);
			                							// now `paraRegs` is the reg string in "{}"
			                							
		                								invokePos0 = paraRegs.lastIndexOf(' ');
		                								String invokeApiPara = null;
		                								boolean invokeRange = false;
			                							if(invokePos0 > 0) {
			                								invokeApiPara = paraRegs.substring(invokePos0 + 1);
			                								String[] paraRegArray = null;
			                								if(paraRegs.indexOf('.') > 0) {
			                									invokeRange = true;
			                									int regNumFrom = Integer.parseInt(paraRegs.substring(1, paraRegs.indexOf(' ')));
			                									int regNumTo = Integer.parseInt(invokeApiPara.substring(1));
			                									String regType = (paraRegs.charAt(0) == 'v' ? "v" : "p");
			                									paraRegArray = new String[regNumTo - regNumFrom + 1];
			                									for(int ii = 0; ii < regNumTo - regNumFrom + 1; ii++) {
			                										paraRegArray[ii] = regType + Integer.toString(ii + regNumFrom);
			                									}
			                								} else {
			                									paraRegArray = paraRegs.split(", ");
			                								}
		                									String[] targetParaRegs = apiParaPattern.split(",");
		                									for(int ii = 0, nn = targetParaRegs.length; ii < nn; ii++) {
		                										String tmpParaPosAndType = targetParaRegs[ii].trim();
		                										if(tmpParaPosAndType.length() == 0) continue;
		                										int jj = 0, mm = tmpParaPosAndType.length();
		                										for(; jj < mm; jj++) {
		                											if(tmpParaPosAndType.charAt(jj) < '0' || tmpParaPosAndType.charAt(jj) > '9') break;
		                										}
		                										int tmpParaPos = Integer.parseInt(tmpParaPosAndType.substring(0, jj));
		                										if(jj == mm) { // String
		                											writer.write(insertLogStr(false, paraRegArray[paraRegArray.length - tmpParaPos], invokeRange));
		                										} else if(jj < mm && tmpParaPosAndType.charAt(jj) == 'I') { // Integer
		                											writer.write(insertLogInt(false, paraRegArray[paraRegArray.length - tmpParaPos], invokeRange));
		                										}
		                									}
		                									
			                								//int vregNum = Integer.parseInt(invokeApiPara.substring(1));
			                								//int targetVreg = vregNum + 1 - sysapis.SystemApiParaPosition.get(invokeApi);
			                								//if(targetVreg >= 0) {
			                								//	writer.write(insertLogPara(false, dotLocals, (invokeApiPara.charAt(0) == 'v' ? "v" : "p") + Integer.toString(targetVreg)));
			                								//}
			                							} else if(apiParaPattern.charAt(0) == '1' && paraRegs.length() > 0) {
			                								if(apiParaPattern.equals("1I")) {
			                									writer.write(insertLogInt(false, paraRegs, false));
			                								} else if(apiParaPattern.equals("1")) {
			                									writer.write(insertLogStr(false, paraRegs, false));
			                								}
			                							}
			                							//writer.write(insertLogTrace(false, dotLocals));
		                							}
	                							}
	                						}
	                					}
	                				}
	                			}
	                		}

                		
                		}
                		writer.write(line + "\n");
                		continue;
                	}
                	
                	writer.write(line + "\n");
                }
                reader.close();
                writer.close();
                
                writerCracker.close();
                
                fr.delete();
                fr = new File(newSmaliName);
                fr.renameTo(new File(inputSmaliPath));
	        }
            else{
            	if(debug) System.out.println("No such a file: " + inputSmaliPath);
	        }
        }
        catch(IOException e){
        	if(debug) System.out.println("Failed to read or write.");
            e.printStackTrace();
        }
	}
	
	private int countMethodParas(String smlLine){
		// todo: count parameters of a method given its smali-style declaration
		
		// get string in "()"
		int pos0 = smlLine.indexOf('(');
		if(pos0 < 0) return 0;
		int pos1 = smlLine.indexOf(')', pos0);
		if(pos1 < 0) return 0;
		String paras = smlLine.substring(pos0 + 1, pos1);
		
		return countRigistersFromString(paras);
	}
	
	private static int countRigistersFromString(String s){
		// a type is like "Z", "B", "S", "C", "I", "J", "F", "D", "L...;", "[...".
        if(s.length() == 0) return 0;
        char head = s.charAt(0);

        if(head == '[') return countRigistersFromString(s.substring(1));

        String basicTypes = "ZBSCIJFD";
        if(basicTypes.indexOf(head) >= 0) return ((head == 'J' || head == 'D') ? 2 : 1) + countRigistersFromString(s.substring(1));

        if(head == 'L') {
            int pos = s.indexOf(';');
            if(pos < 0) return 0;
            return 1 + countRigistersFromString(s.substring(pos + 1));
        }

        return 1;
	}
	
	private String containMethod(String smlLine, String[] methods){
		if(nonDotMode) return classID + "->" + smlLine.substring(smlLine.lastIndexOf(" ") + 1);
		for(String s : methods){
			//System.out.println(s);
			if((classID + "->" + smlLine.substring(smlLine.lastIndexOf(" ") + 1).replace('(', '-').replace(')', '-').replace('/', '_').replaceAll(";", "")).
					indexOf(s) == 0) {
				if(debug) System.out.println(s);
				return s;
			}
		}
		return "";
	}
	
	public String insertLogCB(String cbID, boolean isfield, int regNum){
		if(isfield){
			return "\nconst-string/jumbo v" + Integer.toString(regNum - 2) + ", \"code_block\"\n" + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + cbID + "\"\n" +
				"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
		}
		return "\n    const-string/jumbo v" + Integer.toString(regNum - 2) + ", \"code_block\"\n    " + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + cbID + "\"\n    " +
			"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
	}
	
	public String insertLogM(String methodID, boolean isfield, int regNum){
		if(isfield){
			return "\nconst-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n" + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + methodID + "\"\n" +
				"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
		}
		return "\n    const-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n    " + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + methodID + "\"\n    " +
			"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
	}
	
	public String insertLogApi(String apiString, boolean isfield, int regNum){
		if(isfield){
			return "\nconst-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n" + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + apiString + "\"\n" +
				"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
		}
		return "\n    const-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n    " + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"" + apiString + "\"\n    " +
			"invoke-static {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
	}

	public String insertLogPara(boolean isfield, int regNum, String reg){
		if(isfield){
			return "\nconst-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n" +
				"invoke-static {v" + Integer.toString(regNum - 2) + ", " + reg + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
		}
		return "\n    const-string/jumbo v" + Integer.toString(regNum - 2) + ", \"method\"\n    " +
			"invoke-static {v" + Integer.toString(regNum - 2) + ", " + reg + "}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n\n";
	}

	public String insertLogTrace(boolean isfield, int regNum){
		String tabSpace = isfield ? "" : "    ";
		return "\n" + tabSpace + "new-instance v" + Integer.toString(regNum - 2) + ", Ljava/lang/Exception;" + 
			"\n" + tabSpace + "const-string/jumbo v" + Integer.toString(regNum - 1) + ", \"print trace\"" + 
			"\n" + tabSpace + "invoke-direct {v" + Integer.toString(regNum - 2) + ", v" + Integer.toString(regNum - 1) + "}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V" + 
			"\n" + tabSpace + "invoke-virtual {v" + Integer.toString(regNum - 2) + "}, Ljava/lang/Exception;->printStackTrace()V\n\n";
	}
	
	public String insertLogStr(boolean isfield, String reg, boolean invokeRange) {
		if(!invokeRange)
			return "\n" + (isfield ? "" : "    ") + "invoke-static {" + reg + "}, Lcrack;->logStr(Ljava/lang/String;)V\n\n";
		else {
			return "\n" + (isfield ? "" : "    ") + "invoke-static/range {" + reg + " .. " + reg + "}, Lcrack;->logStr(Ljava/lang/String;)V\n\n";
		}
	}
	
	public String insertLogInt(boolean isfield, String reg, boolean invokeRange) {
		if(!invokeRange)
			return "\n" + (isfield ? "" : "    ") + "invoke-static {" + reg + "}, Lcrack;->logInt(I)V\n\n";
		else {
			return "\n" + (isfield ? "" : "    ") + "invoke-static/range {" + reg + " .. " + reg + "}, Lcrack;->logInt(I)V\n\n";
		}
	}
	
	public String insertLogMethod(boolean isfield, int methodIndex) {
		return "\n" + (isfield ? "" : "    ") + "invoke-static {}, Lcrack;->logMethod" + Integer.toString(methodIndex) + "()V\n\n";
	}

	public String insertLogApi(boolean isfield, int apiIndex) {
		return "\n" + (isfield ? "" : "    ") + "invoke-static {}, Lcrack;->logApi" + Integer.toString(apiIndex) + "()V\n\n";
	}
}
