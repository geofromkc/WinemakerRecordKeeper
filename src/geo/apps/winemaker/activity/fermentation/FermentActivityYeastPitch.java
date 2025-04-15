package geo.apps.winemaker.activity.fermentation;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class FermentActivityYeastPitch implements FermentationActivity {

	private ArrayList<WineMakerFerment> wmfSets;
	
	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{
		this.wmfSets = objList;
	}

	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList) 
	{}

	@Override
	public String apply(WineMakerFerment wmf)
	{
		String yeastName = HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue()).get(wmf.get_yeastStrain());
		
		String displayLine = String.format("Yeast pitch used %.1fg of %s.  Yeast was pitched %s into %d %s of must at %d %s%n", 
				wmf.get_starterYeastAmt(),
				yeastName, 
				wmf.get_entry_date().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")), 
				wmf.get_outputMustVolume(),
				HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue()).get(wmf.get_outputJuiceScale()),
				wmf.get_currentTemp(),
				HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue()).get(wmf.get_tempScale()));

		displayLine += String.format("%nThe following chemical adjustments were made:%n");

		Instant beginTimeInterval = wmf.get_entry_date().toInstant();
		Instant endTimeInterval = wmf.get_entry_date().toInstant().plusSeconds(10);
		Instant thisTime = null;
		
		for (WineMakerFerment wmfChem : wmfSets)
		{
			thisTime = wmfChem.get_entry_date().toInstant();
			if (thisTime.isAfter(beginTimeInterval) && thisTime.isBefore(endTimeInterval))
				displayLine += (wmfChem.get_chemAmount() > 0.0) ? 
					String.format("\t%s, %1.2f %s%n", HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue()).get(wmfChem.get_chemAdded()), wmfChem.get_chemAmount(), HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue()).get(wmfChem.get_chemScale())) : "";
		}
		
		if (wmf.get_fermentNotes().length() > 0)
		{
			displayLine += String.format("%nNotes on this activity:");
			
			String[] splitNotes = wmf.get_fermentNotes().split("\n");
			for (String noteLine: splitNotes)
			{
				displayLine += String.format("%n\t%s", noteLine);
			}
		}
		
		return displayLine;
	}
}