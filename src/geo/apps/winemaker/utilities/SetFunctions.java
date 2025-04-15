package geo.apps.winemaker.utilities;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SetFunctions {

	public static String extractMapKey(String refValue, HashMap<String, String> codeMap)
	{
		return codeMap.keySet()
					.stream()
					.filter(code -> codeMap.get(code)
					.equals(refValue))
					.collect(Collectors.toList())
					.get(0);
	}

}
