package utility;

public class Log {

	private static String seperator = ",";

	public static void log(Object obj) {
		System.out.println(obj);
	}

	public static void log(Object src, Object obj) {
		System.out.println(src + seperator + obj);
	}

	public static void log(Object src, Object obj, Object obj1) {
		System.out.println(src + seperator + obj + seperator + obj1);
	}

	public static void error(Object src, Object obj) {
		System.err.println(src + seperator + obj);
	}

	public static void error(Object src, Object obj0, Object obj1) {
		System.err.println(src + seperator + obj0 + seperator + obj1);
	}
}
