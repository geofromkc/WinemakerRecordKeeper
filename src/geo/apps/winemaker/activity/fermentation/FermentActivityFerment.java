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

public class FermentActivityFerment implements FermentationActivity {

	private ArrayList<WineMakerInventory> wmiRackSet;
	
	@Override
	public void setRecordList(ArrayList<WineMakerFerment> objList)
	{}

	@Override
	public void setInventoryList(ArrayList<WineMakerInventory> objList)
	{
		this.wmiRackSet = objList;
	}

	@Override
	public String apply(WineMakerFerment wmf)
	{
		HashMap<String, String> amountScale = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		
		String containerDisplay = "";
		String endDate = (wmf.get_startDate().compareTo(wmf.get_endDate()) < 0) ? String.format(", ended %s", wmf.get_endDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))) : "";
		
		String displayLine = String.format("Fermentation stage %d, started %s%s", 
				wmf.get_stageCycle(), 
				wmf.get_startDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss")), 
				endDate);

		displayLine += (wmf.get_outputMustVolume() > 0) ?
				String.format("%nOriginal volume of must = %d %s %s", 
						wmf.get_outputMustVolume(), 
						amountScale.get(wmf.get_outputJuiceScale()), 
						containerDisplay) : "";
		displayLine += (wmf.get_outputJuiceVol() > 0) ?
				String.format("%nOriginal volume of juice = %d %s %s", 
						wmf.get_outputJuiceVol(), 
						amountScale.get(wmf.get_outputJuiceScale()),
						containerDisplay) : "";
		displayLine += (wmf.get_currentStageJuiceVol() > 0) ?
				String.format("%nCurrent volume of juice = %d %s %s", 
						wmf.get_currentStageJuiceVol(), 
						amountScale.get(wmf.get_outputJuiceScale()),
						containerDisplay) : "";
		
		if (wmiRackSet != null)
		{
			wmiRackSet.stream().forEach(wmi -> System.out.printf("%nFermentActivityFerment.apply() asset record", wmi.getItemId()));
			
			LocalDateTime wmfDate = wmf.get_entry_date().toLocalDateTime().truncatedTo(ChronoUnit.DAYS);

			ArrayList<WineMakerInventory> batchInventory = this.wmiRackSet
					.stream()
					.filter(batchWmi -> wmfDate.equals(batchWmi.getItemTaskTime().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)))
					.collect(Collectors.toCollection(ArrayList::new));
			
			if (batchInventory.size() > 0)
			{
				displayLine += "\nTarget containers used in activity:\n";
				for (WineMakerInventory wmi : batchInventory)
				{
					displayLine += String.format("\t%s%n", wmi.getItemId());
				}
			}
		}
					
		if (wmf.get_fermentNotes().length() > 0)
			displayLine += String.format("%nNotes: %n\t%s", wmf.get_fermentNotes());
		
		return displayLine;
	}
}