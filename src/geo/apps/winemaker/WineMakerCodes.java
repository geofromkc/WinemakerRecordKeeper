package geo.apps.winemaker;

import java.util.HashMap;

public class WineMakerCodes {

	private HashMap<String, String> codeMap = new HashMap<String, String>(50);
	
	public WineMakerCodes() {
		// TODO Auto-generated constructor stub
	}
	
	public void saveCode(String key, String value)
	{
		codeMap.put(key, value);		
	}

	public String getCode(String key)
	{
		return codeMap.get(key);
	}
}
