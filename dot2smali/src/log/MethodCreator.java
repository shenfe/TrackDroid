package log;

public class MethodCreator {
	
	public static int methodCounter = 0;
	
	public MethodCreator() {
		methodCounter = 0;
	}

	/**
	 * string of the method to be inserted into crack.smali
	 * @param name: name of the method or api.
	 * @param type: 0 is method, 1 is api.
	 * @param num
	 * @return
	 */
	public static String methodEntry(String name, int type, int num) {
		if(type <= 0) {
			methodCounter++;
		}
		String methodName = (type <= 0 ? ("logMethod" + Integer.toString(methodCounter)) : ("logApi" + Integer.toString(num)));
		String callName = "Call " + (type <= 0 ? (type == 0 ? "Method" : "Event") : "Api") + ": " + name;
		return ".method public static " + methodName + "()V\n" +
			"    .locals 2\n" +
			"    .prologue\n" +
			"\n" +
			"    const-string/jumbo v0, \"trackdroid\"\n" +
			"    const-string/jumbo v1, \"" + callName + "\"\n" +
			"    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I\n" +
			"    return-void\n" +
			".end method\n\n";
	}
	
}
