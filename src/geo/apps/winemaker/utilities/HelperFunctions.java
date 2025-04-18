package geo.apps.winemaker.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.WineMakerLog;
import geo.apps.winemaker.conversions.ConversionFactory;
import geo.apps.winemaker.conversions.ConversionTemplate;
import geo.apps.winemaker.utilities.Constants.*;
//import geo.apps.winemaker.utilities.Constants.FamilyCode;
//import geo.apps.winemaker.utilities.Constants.MassAndVolume;
//import geo.apps.winemaker.utilities.Constants.RegistryKeys;
//import geo.apps.winemaker.utilities.Constants.Validation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;

/**
 * Provide helper functions to the application modules:
 * 		> Stage and return Key and Value mappings for the resource definitions
 * 		> Return a ToolTip from the resource file
 * 		> Validate Integers and Doubles
 * 		> Validate Temperature and Volume strings (ie '61F' or '30gal')
 * 		> Expand and compress batch keys
 * 
 * @author geo
 * @version 1.0
 */
public final class HelperFunctions {
	
	private static Properties prop = new Properties();
	
	private static String regexDecimal = "^-?\\d*\\.\\d+$";
	private static String regexInteger = "^-?\\d+$";
	private static String regexDoubleInteger = "(" + regexDecimal + "|" + regexInteger + ")";
	private static Pattern matchSimpleAmountPattern = Pattern.compile(regexDoubleInteger);
	private static String regexNumber = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([A-BD-EG-bd-eg-z%\\/]*)$";
	private static Pattern matchAmountPattern = Pattern.compile(regexNumber);
	private static String regexTemp = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([cCfF]*)$";
	private static Pattern matchTempPattern = Pattern.compile(regexTemp);
	private static Pattern matchDefaultPattern = Pattern.compile("^DEFAULT$");

	private static ArrayList<String> codeRecords = new ArrayList<String>(100);
	
	private static HashMap<String, HashMap<String, String>> codeFamilies = new HashMap<String, HashMap<String, String>>(100);
	private static HashMap<String, HashMap<String, String>> valueFamilies = new HashMap<String, HashMap<String, String>>(100);
	private static HashMap<String, String> assetTypeMap = new HashMap<String, String>(100);
	
	private static Registry appRegistry = Registry.getInstance();
//	private static WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);

	private static EnumMap<MassAndVolume, String> conversionMap = new EnumMap<MassAndVolume, String>(MassAndVolume.class);

	private HelperFunctions()
	{}
	
	static {
		conversionMap.put(MassAndVolume.MASSKG, MassAndVolume.MASSKG.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.MASSG, MassAndVolume.MASSG.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.MASSMG, MassAndVolume.MASSMG.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.MASSLBS, MassAndVolume.MASSLBS.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.MASSOZ, MassAndVolume.MASSOZ.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.VOLUMEL, MassAndVolume.VOLUMEL.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.VOLUMEML, MassAndVolume.VOLUMEML.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.VOLUMEHL, MassAndVolume.VOLUMEHL.getValue().toUpperCase());
		conversionMap.put(MassAndVolume.VOLUMEGAL, MassAndVolume.VOLUMEGAL.getValue().toUpperCase());	
	}
	
	public static Registry getRegistry()
	{
		return appRegistry;
	}
	
	public static EnumMap<MassAndVolume, String> getConversionMap()
	{
		return conversionMap;
	}
	
	public static boolean loadCodeRecords(ArrayList<String> resourceCodes)
	{
		boolean loadTest = true;
		if (resourceCodes.size() > 0)
		{
			codeRecords = resourceCodes;		
			stageCodes();
		}
		else
			loadTest = false;
		
		return loadTest;
	}
	
	/*
	 * Load the ToolTip resources file
	 */
	public static void loadPropertiesFile()
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.loadPropertiesFile()"), true);
	
		InputStream ins = HelperFunctions.class.getClassLoader().getResourceAsStream("tooltips.properties");
		if (ins == null)
		{
			winemakerLogger.writeLog(String.format("   HelperFunctions.loadPropertiesFile(): Could not find Tooltip properties file"), true);
			winemakerLogger.displayAlert(String.format("HelperFunctions.loadPropertiesFile(): Could not find Tooltip properties file"));
		}
		else
		{
			try 
			{
				prop.load(ins);
				if (prop.isEmpty())
				{
					winemakerLogger.writeLog(String.format("   HelperFunctions.loadPropertiesFile(): The Tooltip properties file is empty"), true);
					winemakerLogger.displayAlert(String.format("HelperFunctions.loadPropertiesFile(): The Tooltip properties file is empty"));
				}
			} 
			catch (IOException e2) 
			{
				winemakerLogger.writeLog(String.format("   HelperFunctions.loadPropertiesFile(): I/O exception '%s' loading the Tooltip properties file", e2.getMessage()), true);
				winemakerLogger.displayAlert(String.format("HelperFunctions.loadPropertiesFile(): I/O exception '%s' loading the Tooltip properties file", e2.getMessage()));
			}
		}
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.loadPropertiesFile()"), true);
	} // end of loadPropertiesFile()
    
	/*
	 * Initialize the resource definition mappings
	 * 
	 * codeFamilies: labsupplies = {glbkr50ml=50ml Glass Beaker, glbkr100ml=100ml Glass Beaker}
	 * valueFamilies: labsupplies = {100ml Glass Beaker=glbkr100ml, 50ml Glass Beaker=glbkr50ml}
	 */
	private static void stageCodes() 
	{
		String[] codeTokens = null;
		String codeFamily, codeKey, codeValue;
		HashMap<String, String> codeMap = null;
		HashMap<String, String> valueMap = null;
	
		for (String codeEntry : codeRecords) {
			codeTokens = codeEntry.split(",");
			codeFamily = codeTokens[0];
			codeKey = codeTokens[1];
			codeValue = codeTokens[2];
	
			if (codeFamilies.containsKey(codeFamily))
				codeMap = codeFamilies.get(codeFamily);
			else
				codeMap = new HashMap<String, String>(50);
	
			if (valueFamilies.containsKey(codeFamily))
				valueMap = valueFamilies.get(codeFamily);
			else
				valueMap = new HashMap<String, String>(50);
	
			codeMap.put(codeKey, codeValue);
			valueMap.put(codeValue, codeKey);
	
			codeFamilies.put(codeFamily, codeMap);
			valueFamilies.put(codeFamily, valueMap);
		}
		
		assetTypeMap.putAll(codeFamilies.get(FamilyCode.LABFAMILY.getValue()));
		assetTypeMap.putAll(codeFamilies.get(FamilyCode.CONTAINERFAMILY.getValue()));
		assetTypeMap.putAll(codeFamilies.get(FamilyCode.ADDITIVEFAMILY.getValue()));
		assetTypeMap.putAll(codeFamilies.get(FamilyCode.YEASTFAMILY.getValue()));	
	}

	public static HashMap<String, HashMap<String, String>> getCodeKeyMappings()
	{
		return codeFamilies;
	}

	public static HashMap<String, HashMap<String, String>> getCodeValueMappings()
	{
		return valueFamilies;
	}
	
	public static HashMap<String, String> getCodeKeyFamily(String familyKey)
	{
		HashMap<String, String> newKeyMap = new HashMap<>();
		newKeyMap.putAll(codeFamilies.get(familyKey));
		return newKeyMap;
	}
	
	public static HashMap<String, String> getCodeValueFamily(String familyKey)
	{
		HashMap<String, String> newKeyMap = new HashMap<>();
		newKeyMap.putAll(valueFamilies.get(familyKey));
		return newKeyMap;
	}

	public static String getCodeKeyEntry(String familyKey, String entryKey)
	{
		return codeFamilies.get(familyKey).get(entryKey);
	}

	public static String getCodeValueEntry(String familyKey, String entryKey)
	{
		return valueFamilies.get(familyKey).get(entryKey);
	}
	
	/*
	 * Convert "2021-09-12-2021 Pinot Noir" to "210912pinot"
	 */
	public static String batchKeyCompress(String displayBatchName)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.batchKeyCompress(%s)", displayBatchName), true);


		String regexBatchName = "(\\d{4}-\\d{2}-\\d{2}) (.*)";
		Pattern batchNameMatcher = Pattern.compile(regexBatchName);
	 	Matcher batchMatcher = batchNameMatcher.matcher(displayBatchName);
	 	batchMatcher.find();
	 	
		String tempKey = (valueFamilies.get(FamilyCode.GRAPEFAMILY.getValue()).containsKey(batchMatcher.group(2))) ?
				valueFamilies.get(FamilyCode.GRAPEFAMILY.getValue()).get(batchMatcher.group(2)) : 
				valueFamilies.get(FamilyCode.BLENDFAMILY.getValue()).get(batchMatcher.group(2));
		
		String compressedKey = displayBatchName.replace(batchMatcher.group(2), tempKey);
		compressedKey = compressedKey.replaceAll("[\\s-]", "");
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.batchKeyCompress(): Return %s", compressedKey.substring(2)), true);
		return compressedKey.substring(2);
	} // end of batchKeyCompress()
	
	/**
	 * Expand the batch key into its display version.
	 * Display format "YYMMDDgrapecode" is expanded to "YYYY-MM-DD Grape Name"
	 * @param batchKey String batch key value from the database
	 * @return String display version of batch key
	 */
	public static String batchKeyExpand(String batchKey) 
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.batchKeyExpand(%s)", batchKey), true);

		String grapeName = (codeFamilies.get(FamilyCode.GRAPEFAMILY.getValue()).containsKey(batchKey.substring(6))) ?
				codeFamilies.get(FamilyCode.GRAPEFAMILY.getValue()).get(batchKey.substring(6)) : 
				codeFamilies.get(FamilyCode.BLENDFAMILY.getValue()).get(batchKey.substring(6));

		String returnKey = String.format("20%s-%s-%s %s", batchKey.substring(0, 2), batchKey.substring(2, 4),
				batchKey.substring(4, 6), grapeName);

		winemakerLogger.writeLog(String.format("<< HelperFunctions.batchKeyExpand(): Return %s", returnKey), true);
		return returnKey;
	} // end of batchKeyExpand()

	public static String batchKeyExpand(WineMakerLog wmk) 
	{
		return batchKeyExpand(wmk.get_batchKey());
	} // end of batchKeyExpand()
	
	public static boolean validateInteger(String testValue) 
	{
		boolean rc = true;
		
		try 
		{	
			Integer.parseInt(testValue);
		} 
		catch (NumberFormatException e) 
		{
			rc = false;
		}
		
		return rc;
	} // end of validateInteger()
	
	public static boolean validateInteger(String testValue, Integer upperLimit) 
	{
		boolean rc = validateInteger(testValue);
		if (rc && Integer.parseInt(testValue) > upperLimit)
		{
			rc = false;
		}
		
		return rc;
	} // end of validateInteger()

	public static boolean validateDouble(String testValue) 
	{
		boolean rc = true;
		
		try 
		{
			Double.parseDouble(testValue);
		} 
		catch (NumberFormatException e) 
		{
			rc = false;
		}
		
		return rc;
	} // end of validateDouble()
	
	public static boolean validateDouble(String testValue, Double upperLimit) 
	{
		boolean rc = validateDouble(testValue);
		if (rc && Double.parseDouble(testValue) > upperLimit)
			rc = false;
		
		return rc;
	} // end of validateDouble()
	
	/*
	 * Test input string against three patterns:
	 * 		Simple number like '1' or '10.5'
	 * 		Size amount like '14.8 g'
	 * 		Temperature like '67 f'
	 */
	public static Matcher validateNumberInput(String fieldValue)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.validateNumberInput(%s)", fieldValue), true);

		Matcher amountMatcher = matchAmountPattern.matcher(fieldValue);
		Matcher simpleAmountMatcher = matchSimpleAmountPattern.matcher(fieldValue);
		Matcher tempMatcher = matchTempPattern.matcher(fieldValue);
		Matcher returnMatcher = matchDefaultPattern.matcher("DEFAULT");

		if (simpleAmountMatcher.matches())
		{
			returnMatcher =  simpleAmountMatcher;
			winemakerLogger.writeLog(String.format("   HelperFunctions.validateNumberInput(): matched simple number"), true);
		}

		if (amountMatcher.matches())
		{
			if (validateVolumeWeightTemp(amountMatcher))
				returnMatcher =  amountMatcher;
			winemakerLogger.writeLog(String.format("   HelperFunctions.validateNumberInput(): matched number with scale"), true);
		}

		if (tempMatcher.matches())
		{
			if (validateVolumeWeightTemp(tempMatcher))
				returnMatcher = tempMatcher;
			winemakerLogger.writeLog(String.format("   HelperFunctions.validateNumberInput(): matched temperature with scale"), true);
		}
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.validateNumberInput(%s): return '%s' (%d)", fieldValue, returnMatcher.group(), returnMatcher.groupCount()), true);
		return returnMatcher;
	}
	/**
	 * Validate three types of input:
	 * 		volume
	 * 		mass
	 * 		temperature
	 * @param String value from the TextField
	 * @return boolean with match result
	 */
	public static boolean validateVolumeWeightTemp(String fieldValue)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.validateVolumeWeightTemp(%s)", fieldValue), true);

		Matcher amountMatcher = matchAmountPattern.matcher(fieldValue);
		Matcher tempMatcher = matchTempPattern.matcher(fieldValue);
		
		if (amountMatcher.matches())
		{
			winemakerLogger.writeLog(String.format("<< HelperFunctions.validateVolumeWeightTemp(%s): %s", fieldValue, amountMatcher.pattern().toString()), true);
			return validateVolumeWeightTemp(amountMatcher);
		}
		if (tempMatcher.matches())
		{
			winemakerLogger.writeLog(String.format("<< HelperFunctions.validateVolumeWeightTemp(%s): %s", fieldValue, tempMatcher.pattern().toString()), true);
			return validateVolumeWeightTemp(tempMatcher);
		}
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.validateVolumeWeightTemp(%s)", fieldValue), true);
		return false;
	} // end of validateVolumeWeightTemp()
	
	/*
	 * Validate a weight|volume|temperature input, like "34.2 g" or "4.7mg" or "64f" 
	 */
	private static boolean validateVolumeWeightTemp(Matcher valueMatched)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.validateVolumeWeightTemp(%s)", valueMatched.pattern().toString()), true);

		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		
		boolean returnValue = validateDouble(valueMatched.group(1));

		if (returnValue)
		{
			winemakerLogger.writeLog(String.format("   HelperFunctions.validateVolumeWeightTemp(): '%s' '%s'", valueMatched.group(1), valueMatched.group(2)), true);

			Optional<String> valueLookup = codeSet.values().stream()
					.filter(codeValue -> valueMatched.group(2).equalsIgnoreCase(codeValue))
					.findFirst();
			if (valueLookup.isEmpty())
			{
				returnValue = false;
			}
		}

		winemakerLogger.writeLog(String.format("<< HelperFunctions.validateVolumeWeightTemp(%s)", valueMatched.pattern().toString()), true);
		return returnValue;
	} // end of validateVolumeWeightTemp()
	
	/*
	 * Build list of inventory container objects in this batch
	 */
	public static ObservableList<String> buildSourceContainerSet(ArrayList<WineMakerLog> batchSet, ArrayList<WineMakerInventory> currentInventory)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(">> HelperFunctions.buildSourceContainerSet()", true);

		BiFunction<String, String, String> buildItemDisplay = (itemName, itemId) -> (itemId.length() > 0) ? 
				String.format("%s (%s)", itemId, itemName) : itemName;
		
		HashMap<String, String> codeSet = getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue());
		ObservableList<String> containerList = FXCollections.observableArrayList();
		List<String> batchIds = batchSet
				.stream()
				.map(wmk -> wmk.get_batchKey())
				.collect(Collectors.toList());

		containerList.addAll(currentInventory
				.stream()
				.filter(wmi -> codeSet.get(wmi.get_itemName()) != null)
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> batchIds.contains(wmi.getItemBatchId()))
				.map(wmi -> buildItemDisplay.apply(codeSet.get(wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog("<< HelperFunctions.buildSourceContainerSet()", true);
		return containerList;
	} // end of buildSourceContainerSet(<batch id>)
	
	/*
	 * Define the new Tooltip to be added to a UI element.  The appearance is set in the local CSS file.
	 */
	public static Tooltip buildTooltip(String tipText)
	{
		Tooltip toolTipText = new Tooltip(prop.getProperty(tipText));
		toolTipText.setTextAlignment(TextAlignment.LEFT);
		toolTipText.setWrapText(true);
		toolTipText.setMaxWidth(400);
		toolTipText.setStyle("-fx-background-color: gray;");

		return toolTipText;
	}
	
	public static void activityDefaultLayoutContainerSetup(ComboBox<String> uiComboBox, ObservableList<String> containerContent, String promptText)
	{
		uiComboBox.setItems(containerContent);
		uiComboBox.setPromptText(promptText);
		uiComboBox.setButtonCell(new ButtonCell());
	}
	
	/*
	 * Convert the inventory display value back into its key value(s).
	 * Assets with ids return the internal name and the id.
	 * Consumable assets return the internal name.
	 * 
	 * 		ie. "BRT10GAL01 (Brute 10 gal Bucket)" -> ["BRT10GAL01", "brute10"]
	 * 			"Ascorbic Acid" -> ["ascorbic"]
	 */
	public static List<String> convertAssetItemName(String selectedValue)
	{	
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.convertAssetItemName(%s)", selectedValue), true);

		String regexTypeNameWithId = "^(\\w*\\d{2})\\s*\\((.*)\\)$";
		Pattern matchTypeNameWithIdPattern = Pattern.compile(regexTypeNameWithId);
		
		ArrayList<String> returnValues = new ArrayList<String>();
		boolean matchedThePattern = false;

		Matcher matchItemName = matchTypeNameWithIdPattern.matcher(selectedValue);
		matchedThePattern = matchItemName.matches();
		
		if (matchedThePattern)
		{
			returnValues.add(matchItemName.group(2));
			returnValues.add(matchItemName.group(1));
		}
		else
		{
			returnValues.add(selectedValue);
		}

		Optional<String> itemKey = assetTypeMap.keySet()
				.stream()
				.filter(code -> returnValues.get(0).equals(assetTypeMap.get(code)))
				.findFirst();
		if (!itemKey.isEmpty())
		{
			returnValues.set(0, itemKey.get());
		}

		winemakerLogger.writeLog(String.format("<< HelperFunctions.convertAssetItemName(): return %s", returnValues), true);
		return returnValues;
	} // end of convertAssetItemName()	
	
	/**
	 * Collect contents of provided file
	 * @param inputFile File object for target file
	 * @return String array of file lines
	 */
	public static String[] returnFileContents(File inputFile)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);

		List<String> lines = new ArrayList<String>();
		String oneLine = null;

		try {
			FileReader fileReader = new FileReader(inputFile.getPath());
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((oneLine = bufferedReader.readLine()) != null) {
				lines.add(oneLine);
			}

			bufferedReader.close();
		} 
		catch (IOException e2) 
		{
			winemakerLogger.showIOException(e2, String.format("Failure reading file '%s'", inputFile.getPath()));
		}
		
		return lines.toArray(new String[lines.size()]);
	} // end of readSelectedFile(File inputFile)
	
	/*
	 * When removing or adding stock, convert sizing mismatch between values
	 * i.e.; remove 14mg from 100g, change '14mg' to '.014g'  
	 */
	public static ValidationPackage convertSizeField(String targetType, Matcher matchUiField, TextField uiField)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.convertSizeField('%s', '%s', '%s')", targetType, matchUiField.group(), uiField.getText()), true);
		
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setTestStatus(Validation.PASSED);
		validatePackage.setErrorMsg(String.format("Amount '%s' has been converted to ", uiField.getText()));
	
		String removeCount = matchUiField.group(1);
	
		String oops = "Undefined converter";
		ConversionTemplate conversionTemplate = ConversionFactory.getConverter(matchUiField.group(2).toUpperCase())
				.orElseThrow(() -> new IllegalArgumentException(oops));
		
		String numberScale = targetType;
		Optional<MassAndVolume> conversionType = conversionMap.keySet()
				.stream()
				.filter(enumKey -> conversionMap.get(enumKey).equalsIgnoreCase(numberScale))
				.findFirst();
	
		double convertedAdjustmentAmount = conversionTemplate.apply(conversionType.get(), Double.parseDouble(removeCount));
	    double truncated = Math.floor(convertedAdjustmentAmount * 10000) / 10000;
	    
		uiField.setText(Double.toString(truncated) + " " + targetType);
		validatePackage.setErrorMsg(validatePackage.getErrorMsg() + String.format("'%s'", uiField.getText()));
		winemakerLogger.displayAlert(validatePackage.getErrorMsg());
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.convertSizeField('%s', '%s', '%s')", targetType, matchUiField.group(), uiField.getText()), true);
		return validatePackage;
	} // end of convertSizeField()

	static class ValidationPackage {
		
		String errorMsg;
		Validation testStatus;
		
		void setErrorMsg(String localErrorMsg)
		{
			this.errorMsg = localErrorMsg;
		}
		
		String getErrorMsg()
		{
			return this.errorMsg;
		}
		
		void setTestStatus(Validation localTestStatus)
		{
			this.testStatus = localTestStatus;
		}
		
		Validation getTestStatus()
		{
			return this.testStatus;
		}
	}
	
	/*
	 * Find the matching inventory asset record 
	 */
	public static List<WineMakerInventory> findAssetItemRecord(ArrayList<WineMakerInventory> inventorySet, String itemSelectedExisting)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.findAssetItemRecord(..., '%s')", itemSelectedExisting), true);
		
		List<String> itemNameTokens = HelperFunctions.convertAssetItemName(itemSelectedExisting);

		Predicate<WineMakerInventory> matchItemName = wmi -> wmi.get_itemName().equals(itemNameTokens.get(0));
		Predicate<WineMakerInventory> matchItemId = wmi -> wmi.getItemId().equals(itemNameTokens.get(1));
		Predicate<WineMakerInventory> noTaskId = wmi -> wmi.getItemTaskId().length() == 0;
		
		List<WineMakerInventory> filteredQueryList = inventorySet
				.stream()
				.filter(noTaskId)
				.filter(matchItemName)
				.collect(Collectors.toList());

		if (itemNameTokens.size() > 1)
			filteredQueryList = filteredQueryList
					.stream()
					.filter(matchItemId)
					.collect(Collectors.toList());

		winemakerLogger.writeLog(String.format("<< HelperFunctions.findAssetItemRecord('%s'): returning %s", itemSelectedExisting, filteredQueryList), true);
		return filteredQueryList;
	} // end of findAssetItemRecord()

	/*
	 * Increment an existing TimeStamp with the provided increment
	 */
	public static Timestamp buildTimeStamp(Timestamp parentEntryDate, int secondIncrement)
	{
		Instant newTimeInstant = parentEntryDate.toInstant().plusSeconds(secondIncrement);

		return Timestamp.from(newTimeInstant);
	} // end of buildTimeStamp()
	
	public static Timestamp buildTimeStamp(DatePicker uiDate, String uiTime, int secIncrement)
	{
		LocalTime logEntryTime = parseTimeString(uiTime).plusNanos(secIncrement * 100);
		Timestamp newDateTime = Timestamp.valueOf(uiDate.getValue().atTime(logEntryTime));
		
		return newDateTime;		
	}
	
	/*
	 * Validate and parse a time string.  The string "now" is a shortcut to the current time
	 */
	public static LocalTime parseTimeString(String uiTime)
	{
		WineMakerLogging winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		winemakerLogger.writeLog(String.format(">> HelperFunctions.parseTimeString('%s')", uiTime), true);

		String timeColonPattern = "h:mm a";
		LocalTime parsedTime = LocalTime.now();
		DateTimeFormatter timeParser = DateTimeFormatter.ofPattern(timeColonPattern);
		
		try
		{
			if (uiTime.equalsIgnoreCase("now"))
				parsedTime = LocalTime.now();
			else
				parsedTime = LocalTime.parse(uiTime.toUpperCase(), timeParser);
		}
		catch (DateTimeParseException de)
		{
			parsedTime = null;
		}
		
		winemakerLogger.writeLog(String.format("<< HelperFunctions.parseTimeString('%s')", uiTime), true);
		return parsedTime;
	} // end of parseTimeString()
	
	/*
	 * Provided for resetting ComboBox button prompts
	 */
	private static class ButtonCell extends ListCell<String> {
		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			setText(item);
		}
	}
}
