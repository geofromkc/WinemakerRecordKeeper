package geo.apps.winemaker.activity.fermentation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class FermentActivityAmeliorate implements FermentationActivity {

	@Override 
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{
	}
	
	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList) 
	{}

	@Override
	public String apply(WineMakerFerment wmf)
	{	
		winemakerLogger.writeLog(">> FermentActivityAmeliorate.apply()", true);

		HashMap<String, String> codesetChem = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		HashMap<String, String> codesetMeasure = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());

		String showTemp = (wmf.get_currentTemp() > 0) ? 
				String.format(", with juice at %d %s%n", wmf.get_currentTemp(), codesetMeasure.get(wmf.get_tempScale())) : "\n";
		
		String displayLine = String.format("Chemical adjustments to %d %s were made %s%s",
				wmf.get_outputMustVolume(),
				codesetMeasure.get(wmf.get_outputJuiceScale()),
				wmf.get_entry_date().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")),
				showTemp);

		displayLine += (wmf.get_chemAmount() > 0.0) ? 
				String.format("\t%s, %1.2f %s%n", codesetChem.get(wmf.get_chemAdded()), wmf.get_chemAmount(), codesetMeasure.get(wmf.get_chemScale())) : "";			
		
		if (wmf.get_fermentNotes().length() > 0)
		{
			displayLine += String.format("%nNotes on this activity:");
			
			String[] splitNotes = wmf.get_fermentNotes().split("\n");
			for (String noteLine: splitNotes)
			{
				displayLine += String.format("%n\t%s", noteLine);
			}
		}
		
		winemakerLogger.writeLog("<< FermentActivityAmeliorate.apply()", true);
		return displayLine;
	}
}
