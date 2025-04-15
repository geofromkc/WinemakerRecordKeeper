package geo.apps.winemaker.activity.fermentation;

import java.util.ArrayList;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;

public class FermentActivityDefault implements FermentationActivity {

	//private ArrayList<WineMakerFerment> wmfSets;
	
	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{}
	
	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList) 
	{}

	@Override
	public String apply(WineMakerFerment wmf)
	{
		String displayLine = String.format("Fermentation activity display not yet implemented");
		
		return displayLine;
	}

}
