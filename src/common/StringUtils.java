package common;
import java.lang.reflect.Array;
import java.util.*;

public class StringUtils {
	public static boolean containsIgnoringCase(String source,String pattern){
		return toUpperCase(source).contains(toUpperCase(pattern));
	}
	public static boolean isEqualIgnoringCase(String str1,String str2){
		str1 = toUpperCase(str1);
		str2 = toUpperCase(str2);
		return str1.equals(str2);
	}
	public static boolean isBlank(String str){
		str = trim(str);
		return str.isEmpty();
	}
	public static boolean isNotBlank(String str){
		return !isBlank(str);
	}
	public static String toUpperCase(String str){
		str = trim(str);
		return str.toUpperCase();
	}
	public static boolean isEqual(String str1,String str2){
		if(str1 == null || str2 == null){
			return false;
		}
		return str1.equals(str2);
	}
	public static String trim(String str){
		if(str==null){
			return "";
		}
		return str.trim();
	}
	public static String getCommaSeparatedString(Collection<? extends Object> collection){
		StringBuilder stringBuilder = new StringBuilder("(");
		int index = 0;
		for(Iterator<? extends Object> iterator = collection.iterator(); iterator.hasNext();index ++ ){
			if(index > 0){
				stringBuilder.append(",");
			}
			stringBuilder.append(iterator.next());
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
	
	public static Integer[] toIntArray(String[] param){
		return toArray(param, Integer.class);
	}

	public static Double[] toDoubleArray(String[] parameterValues) {
		return toArray(parameterValues, Double.class);
	}
	
	@SuppressWarnings("unchecked")
	private static<T extends Number> T[] toArray(String[] params,Class<T> clazz){
		
		T[] resultList = (T[]) Array.newInstance(clazz, params.length);
		
		
		for(int i=0;i<params.length;i++){
			try {
				resultList[i] = clazz.getConstructor(String.class).newInstance(params[i]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return resultList;
	}
	
	public static List<Long> getLongListFromCommaSeperatedString(String additionalIPs) {
		String[] splittedString = additionalIPs.split(",");
		List<Long> longList = new ArrayList<Long>();
		for(String ip : splittedString) {
			longList.add(Long.parseLong(ip));
		}
		return longList;
	} 
	
	
}
