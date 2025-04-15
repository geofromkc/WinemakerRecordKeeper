package geo.apps.winemaker.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.WineMakerLog;
import geo.apps.winemaker.utilities.Constants.*;

public class DisplayFormatter {
	
	/*
	 * HelperFunctions test
	 */
//	private HashMap<String, HashMap<String, String>> codeMapping = WineMakerModel.codeFamilies;
	private HashMap<String, HashMap<String, String>> codeMapping = HelperFunctions.getCodeKeyMappings();

	public DisplayFormatter()
	{}
	
	public String displayFirstLineGrapes(WineMakerLog sourceRecord)
	{
		return String.format("Batch grapes were sourced from %s. %n", 
				codeMapping.get(FamilyCode.VINEYARDFAMILY.getValue()).get(sourceRecord.get_batchVineyard())
				);
	}
	
	public String displayFirstLineJuice(WineMakerLog sourceRecord)
	{
		String returnText = (sourceRecord.get_batchVineyard() != null) ? 
				String.format("Batch juice was sourced from %s. %n", 
				codeMapping.get(FamilyCode.VINEYARDFAMILY.getValue()).get(sourceRecord.get_batchVineyard())) : "";
		
		return returnText;
	}
	
	public String displayFirstBlendLine(WineMakerLog sourceRecord)
	{
		String sourceText = (sourceRecord.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue())) ? "Batch Grapes were" : "Batch Juice was";
		return String.format("The following grape is part of this blend:%n\t%s%n\t\t%s sourced from %s.%n", 
				codeMapping.get(FamilyCode.GRAPEFAMILY.getValue()).get(sourceRecord.get_batchGrape()),
				sourceText,
				codeMapping.get(FamilyCode.VINEYARDFAMILY.getValue()).get(sourceRecord.get_batchVineyard()));
	}
	
	public String displaySecondLineGrapes(WineMakerLog sourceRecord, String indent)
	{
		String pluralContainers = (sourceRecord.get_sourceItemCount() > 1 || sourceRecord.get_sourceItemCount() == 0) ? "s" : "";
		String juiceOrGrapes = (sourceRecord.get_sourceScale().equals(WeightsAndMeasures.USVOLUME.getValue())) ? "volume" : "weight";
		String sourceScale = codeMapping.get(FamilyCode.MEASURESFAMILY.getValue()).get(sourceRecord.get_sourceScale());

		return String.format(indent + "Batch contains %d %d-%s unit%s, with a total %s of %d %s.%n", 
				sourceRecord.get_sourceItemCount(), 
				sourceRecord.get_sourceItemMeasure(),
				sourceScale.replace("s", ""),
				pluralContainers,
				juiceOrGrapes,
				sourceRecord.get_sourceItemCount() * sourceRecord.get_sourceItemMeasure(), 
				sourceScale
				);
	}
	
	public String displayThirdLineGrapes(WineMakerLog sourceRecord, String indent)
	{
		String returnText = (sourceRecord.get_sourceItemPrice() > 0) ? 
			String.format(indent + "The cost was $%.2f per %s, with a total cost of $%.2f%n", 
				sourceRecord.get_sourceItemPrice() / sourceRecord.get_sourceItemMeasure(), 
				codeMapping.get(FamilyCode.MEASURESFAMILY.getValue()).get(sourceRecord.get_sourceScale()).replace("s", ""), 
				sourceRecord.get_sourceItemPrice() * sourceRecord.get_sourceItemCount()) : "";
		
		return returnText;
	}
	
	public String displaySecondLineJuice(WineMakerLog sourceRecord)
	{
		return displaySecondLineJuice(sourceRecord, "");
	}
	
	public String displaySecondLineJuice(WineMakerLog sourceRecord, String indent)
	{
		String returnText = (sourceRecord.get_sourceItemPrice() > 0) ? 
			String.format("%sBatch contains %d pails, each being %d %s per pail.%n",
				indent,
				sourceRecord.get_sourceItemCount(), 
				sourceRecord.get_sourceItemMeasure(), 
				codeMapping.get(FamilyCode.MEASURESFAMILY.getValue()).get(sourceRecord.get_sourceScale())) : 
			String.format("%sBatch consists of %d %s.%n",
				indent,
				sourceRecord.get_sourceItemMeasure(), 
				codeMapping.get(FamilyCode.MEASURESFAMILY.getValue()).get(sourceRecord.get_sourceScale()));
		
		return returnText;
	}

	public String displayThirdLineJuice(WineMakerLog sourceRecord)
	{
		return displayThirdLineJuice(sourceRecord, "");
	}
	
	public String displayThirdLineJuice(WineMakerLog sourceRecord, String indent)
	{
		String returnText = (sourceRecord.get_sourceItemPrice() > 0) ? 
				String.format("%sThe cost was $%.2f per pail, $%.2f per %s, with a total cost of $%.2f%n",
				indent,
				sourceRecord.get_sourceItemPrice(),
				sourceRecord.get_sourceItemPrice() / sourceRecord.get_sourceItemMeasure(), 
				codeMapping.get(FamilyCode.MEASURESFAMILY.getValue()).get(sourceRecord.get_sourceScale()), 
				sourceRecord.get_sourceItemPrice() * sourceRecord.get_sourceItemCount()) : "";
		
		return returnText;
	}
	
	public String displayContainersInUse(WineMakerLog sourceRecord, ArrayList<WineMakerInventory> wmiContainers)
	{
		ArrayList<WineMakerInventory> batchInventory = wmiContainers
				.stream()
				.filter(batchWmi -> batchWmi.get_itemStockOnHand() > 0)
				.collect(Collectors.toCollection(ArrayList::new));

		String displayLine = "";
		for (WineMakerInventory invRecord: batchInventory)
		{
			displayLine += String.format("\t%s%n", invRecord.getItemId());
		}
		displayLine = (displayLine.length() > 0) ? "\nContainers in use:\n" + displayLine : "";

		return displayLine;
	}
	
	public String displayVendorLine(WineMakerLog sourceRecord, String indent)
	{
		return String.format(indent + "The batch was purchased from %s%n", 
				codeMapping.get(FamilyCode.GRAPESUPPLYFAMILY.getValue()).get(sourceRecord.get_sourceVendor()));
	}
	
	public String displayVendorNotesLine(WineMakerLog sourceRecord)
	{
		return (sourceRecord.get_sourceVendorNotes().length() > 0) ? 
				String.format("%nBatch notes: %n\t%s %n", sourceRecord.get_sourceVendorNotes()) : "";
	}


}
