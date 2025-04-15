package geo.apps.winemaker.activity.fermentation;

import java.util.ArrayList;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;

/**
 * Produce the display output for a specific fermentation activity
 * @author geo
 *
 */
public interface FermentationActivity 
{	
	WineMakerLogging winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);

	void setRecordList(ArrayList<WineMakerFerment> objList);
	void setInventoryList(ArrayList<WineMakerInventory> objList);
		
	String apply(WineMakerFerment wmf);
}
