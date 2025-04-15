
package geo.apps.winemaker.activity.fermentation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class FermentActivityRack implements FermentationActivity {
	
	private ArrayList<WineMakerInventory> wmiSet;

	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{}
	
	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList) 
	{
		this.wmiSet = objList;
	}

	@Override
	public String apply(WineMakerFerment wmf)
	{
		HashMap<String, String> amountMeasure = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		
		String displayLine = String.format("Rack performed %s%n", 
				wmf.get_entry_date().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

		displayLine += String.format("%nApproximate output volume was %d %s", wmf.get_outputJuiceVol(), amountMeasure.get(wmf.get_outputJuiceScale()));
		
		if (wmf.get_fermentNotes().length() > 0)
		{
			displayLine += String.format("%n%nNotes on this activity:");
			
			String[] splitNotes = wmf.get_fermentNotes().split("\n");
			for (String noteLine: splitNotes)
			{
				displayLine += String.format("%n\t%s", noteLine);
			}
		}
		
		return displayLine;
	}

}