package geo.apps.winemaker.activity.fermentation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class FermentActivityBottle implements FermentationActivity {
	
	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{}
	
	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList)
	{}
	
	@Override
	public String apply(WineMakerFerment wmf)
	{
		HashMap<String, String> containerName = HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue());

		String displayLine = String.format("Bottling party %s%n", 
				wmf.get_entry_date().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
		
		displayLine += String.format("%nSource containers:");
		displayLine += (wmf.get_containerType().length() > 0) ?
				String.format("%n\t%s", containerName.get(wmf.get_containerType())) : "";
		displayLine += (wmf.get_containerType2().length() > 0) ?
				String.format("%n\t%s", containerName.get(wmf.get_containerType2())) : "";
		displayLine += (wmf.get_containerType3().length() > 0) ?
				String.format("%n\t%s", containerName.get(wmf.get_containerType3())) : "";

		
		displayLine += String.format("%n%n%d bottles were packaged", wmf.get_bottleCount());
		
		if (wmf.get_fermentNotes().length() > 0)
			displayLine += String.format("%nNotes: %n\t%s", wmf.get_fermentNotes());
		
		return displayLine;
	}
}
