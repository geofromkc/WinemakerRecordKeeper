package geo.apps.winemaker.activity.fermentation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class FermentActivityCheckpoint implements FermentationActivity {
	
	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{}
	
	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList) 
	{}

	@Override
	public String apply(WineMakerFerment wmf)
	{
		HashMap<String, String> codesetMeasure = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());

		int currTemp = (wmf.get_currentTemp() == 0) ? 
				wmf.get_startTemp() : wmf.get_currentTemp();
		String batchComposition = "weight";
		if (wmf.get_stageCycle() > 0)
			batchComposition = (wmf.get_stageCycle() == 1) ? "must" : "juice";
		int testedVolume = (wmf.get_stageCycle() > 1) ? wmf.get_outputJuiceVol() : wmf.get_outputMustVolume();
		String testedVolumeDisplay = (testedVolume > 0) ? String.format(" for %d %s of %s, ", testedVolume, codesetMeasure.get(wmf.get_outputJuiceScale()), batchComposition) : " ";
		
		String displayLine = String.format("Checkpoint taken%sat about %s", 
				testedVolumeDisplay,
				wmf.get_entry_date().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss")));
		
		if ((wmf.get_currBrix() > 0 || wmf.get_currpH() > 0 || wmf.get_currTA() > 0 || currTemp > 0))
		{
			displayLine += String.format("%nCurrent checkpoint values: %n");
			displayLine += (wmf.get_currBrix() > 0) ? String.format("\tBrix = %.1f%n", wmf.get_currBrix()) : "";  
			displayLine += (wmf.get_currpH() > 0) ? String.format("\tpH = %.1f%n", wmf.get_currpH()) : "";  
			displayLine += (wmf.get_currTA() > 0) ? String.format("\tTA = %.1f%n", wmf.get_currTA()) : "";  
			displayLine += (currTemp > 0) ? String.format("\tTemp = %d %s%n", currTemp, codesetMeasure.get(wmf.get_tempScale())) : "";
		}
		
		//displayLine += String.format("%nTotal %s for this stage = %d %s", batchComposition, wmf.get_currentStageJuiceVol(), codesetMeasure.get(wmf.get_currentStageJuiceScale()));

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
