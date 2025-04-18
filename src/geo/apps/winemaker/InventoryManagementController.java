package geo.apps.winemaker;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import geo.apps.winemaker.utilities.Constants.*;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

public class InventoryManagementController implements Initializable {

	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;
	private WineMakerInventory selectedInventoryRecord;
	
	private StringProperty itemCurrAmountProperty = new SimpleStringProperty();
	private StringProperty itemRemoveReasonProperty = new SimpleStringProperty();
	private String datePickerOriginal = "";
	
	private String regexNumberWithScale = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([A-BD-EG-bd-eg-z%\\/]*)$";
	private Pattern matchAmountPattern = Pattern.compile(regexNumberWithScale);	
	
	private double inStockReference = 0;
	private double inStockUpdated = 0;
	private double inStockRemoved = 0;
	private int purchaseAmount = 0;
	private String batchIdReference = "";
	
	private boolean debugLogging = true;
	
	private final String NEWASSET = "newAsset";
	private final String REMASSET = "remAsset";
	private final String itemSelectSeparator = "--------------------";
	
//	private HashMap<String, String> itemTypeMap = new HashMap<>();
	private HashMap<String, HashMap<String, String>> resourceCodesFamilyMap = HelperFunctions.getCodeKeyMappings();
	
	private HashMap<String, String> vendorOptionsMap = new HashMap<>();
	private HashMap<String, String> removeReasonOptionsMap = new HashMap<>();
	
	private ArrayList<WineMakerInventory> inventoryExistingSetsQueryList = new ArrayList<WineMakerInventory>(100);
	private ArrayList<WineMakerInventory> inventoryNewRecordsList = new ArrayList<WineMakerInventory>(1);
	private ArrayList<WineMakerInventory> inventoryOldRecordsUpdateList = new ArrayList<WineMakerInventory>(1);
	private ArrayList<WineMakerInventory> inventoryOldRecordsDeleteList = new ArrayList<WineMakerInventory>(1);
	
	private EnumMap<MassAndVolume, String> conversionMap = new EnumMap<MassAndVolume, String>(MassAndVolume.class);

	private BiFunction<String, String, String> buildItemDisplay = 
			(itemName, itemId) -> (itemId.length() > 0) ? 
					String.format("%s (%s)", itemId, itemName) : itemName;

	@FXML GridPane gp;
	
	@FXML Button buttonDisplaySwitch;

	@FXML Label labelItemSelections;
	@FXML Label labelStockOnHand;
	@FXML Label labelRemoveStock;
	@FXML Label labelRemoveReason;
	@FXML Label labelPurchaseType;
	@FXML Label labelPurchaseDate;
	@FXML Label labelItemID;
	@FXML Label labelPurchaseCount;
	@FXML Label labelPurchaseCost;
	@FXML Label labelSize;
	@FXML Label labelSupplier;
	private Label labelBatchId = new Label();
	
	@FXML ComboBox<String> itemExistingSelections;	
	@FXML TextField itemStockOnHand;
	@FXML TextField itemRemoveFromStock;
	@FXML ChoiceBox<String> itemRemovalReason;	
	@FXML ComboBox<String> itemAvailablePurchaseTypes;	
	@FXML DatePicker itemPurchasedDate;
	@FXML TextField itemID;
	@FXML TextField itemPurchaseCount;
	@FXML TextField itemPurchaseCost;
	@FXML TextField itemSize;
	@FXML ComboBox<String> itemSupplierSelections;
	private TextField itemBatchId = new TextField();
	
	@FXML private TextArea statusUpdates;
	
	ObservableList<String> itemExistingSelectionsList = FXCollections.observableArrayList();
	ObservableList<String> itemAvailableTypesList = FXCollections.observableArrayList();
	ObservableList<String> itemRemoveReasonSelectionsList = FXCollections.observableArrayList();
	ObservableList<String> itemSupplierSelectionsList = FXCollections.observableArrayList();
		
	InventoryScene uiSetting;
	
	public InventoryManagementController()
	{
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);	
	}
	
	private ArrayList<WineMakerInventory> getInventoryExistingSetsQueryList()
	{
		return this.inventoryExistingSetsQueryList;
	}
	
	private void setInventoryExistingSetsQueryList(ArrayList<WineMakerInventory> newInventorySet)
	{
		this.inventoryExistingSetsQueryList = newInventorySet;
	}
	
	public WineMakerInventory getSelectedInventoryRecord() {
		return this.selectedInventoryRecord;
	}

	public void setSelectedInventoryRecord(WineMakerInventory selectedInventoryRecord) {
		this.selectedInventoryRecord = selectedInventoryRecord;
	}

	public double getInStockReference() {
		return this.inStockReference;
	}

	public void setInStockReference(double inStockReference) {
		this.inStockReference = inStockReference;
	}

	public double getInStockUpdated() {
		return this.inStockUpdated;
	}

	public void setInStockUpdated(int inStockUpdated) {
		this.inStockUpdated = inStockUpdated;
	}

	public double getInStockRemoved() {
		return this.inStockRemoved;
	}

	public void setInStockRemoved(double inStockRemoved) {
		this.inStockRemoved = inStockRemoved;
	}
	
	public String getBatchIdReference()
	{
		return this.batchIdReference;
	}
	
	public void setBatchIdReference(String batchIdReference)
	{
		this.batchIdReference = batchIdReference;
	}

	public int getPurchaseAmount() {
		return purchaseAmount;
	}

	public void setPurchaseAmount(int purchaseAmount) {
		this.purchaseAmount = purchaseAmount;
	}
	
	/*
	 * Create master asset record.  Contains fields:
	 * 		Asset name and optionally the asset id
	 * 		Stock on hand
	 * 		Associated batch id, if any
	 */
	private WineMakerInventory createMasterRecord()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.createMasterRecord()", debugLogging);

		WineMakerInventory wmi = new WineMakerInventory();

		List<String> itemNameConversion = HelperFunctions.convertAssetItemName(buildItemDisplay.apply(itemAvailablePurchaseTypes.getValue(), itemID.getText()));
		
		wmi.set_itemName(itemNameConversion.get(0));
		if (itemNameConversion.size() > 1)
			wmi.setItemId(itemNameConversion.get(1));
		
		wmi.setItemTaskTime(Timestamp.valueOf(itemPurchasedDate.getValue().atTime(LocalTime.now())));
		inventoryNewRecordsList.add(wmi);
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.createMasterRecord() added to new record list%s%n", wmi), debugLogging);
		return wmi;
	} // end of createMasterRecord()
	
	/*
	 * Create Inventory record logging the updates
	 */
	private void createAssetUpdateRecord(WineMakerInventory wmiMaster)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.createAssetUpdateRecord()"), debugLogging);

		HashMap<String, String> removalCodeMap = resourceCodesFamilyMap.get(FamilyCode.REMOVALFAMILY.getValue());

	    WineMakerInventory wmiUpdate = new WineMakerInventory();
		wmiUpdate.setItemTaskTime(Timestamp.valueOf(LocalDateTime.now()));

		wmiUpdate.set_itemName(wmiMaster.get_itemName());
		wmiUpdate.setItemId(wmiMaster.getItemId());
		wmiUpdate.setItemBatchId(wmiMaster.getItemBatchId());
		
		Optional<String> removeCodeFind = removalCodeMap.keySet()
				.stream()
				.filter(code -> itemRemovalReason.getValue().equals(removalCodeMap.get(code)))
				.findFirst();

		wmiUpdate.setItemTaskId(removeCodeFind.get());
		
		if (itemRemoveFromStock.getText().length() > 0)
		{
			Matcher validateMatcher = HelperFunctions.validateNumberInput(itemRemoveFromStock.getText());
			wmiUpdate.set_itemActivityAmount(Double.parseDouble(validateMatcher.group(1)));

			if (wmiUpdate.getItemId().length() == 0)
				wmiUpdate.set_itemAmountScale(validateMatcher.group(2));
		}
		
		inventoryNewRecordsList.add(wmiUpdate);
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.createAssetUpdateRecord(): Update activity record: %s", wmiUpdate), debugLogging);
	} // end of createRemovalRecord()
	
	/*
	 * Create a new purchase record
	 * The query count indicates the existence of an existing master record
	 */
	private void createAssetPurchaseRecord(WineMakerInventory wmiMaster, int queryCount)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.createPurchaseRecord(): Master record: %s", wmiMaster), debugLogging);
		
		HashMap<String, String> vendorCodeMap = resourceCodesFamilyMap.get(FamilyCode.SUPPLIESFAMILY.getValue());
		
		WineMakerInventory wmiPurchase = new WineMakerInventory();
		
		wmiPurchase.set_itemName(HelperFunctions.convertAssetItemName(buildItemDisplay.apply(itemAvailablePurchaseTypes.getValue(), itemID.getText())).get(0));
		wmiPurchase.setItemId(wmiMaster.getItemId());
		
		Timestamp purchaseTime = null;
		purchaseTime = Timestamp.valueOf(itemPurchasedDate.getValue().atTime(LocalTime.now()));

		if (queryCount == 0)
			purchaseTime.setTime(purchaseTime.getTime() + 1000);

		wmiPurchase.setItemTaskTime(purchaseTime);

		winemakerLogger.writeLog(String.format("   InventoryManagementController.createAssetPurchaseRecord() %nMaster %s%nPurchase %s", wmiMaster, wmiPurchase), debugLogging);		
		
		/*
		 * update the stock-on-hand in the master record
		 */
		double sizeNumber = 1;
		String sizeScale = "";
		
		if (!itemSize.getText().isEmpty())
		{
			Matcher itemSizeMatcher = matchAmountPattern.matcher(itemSize.getText());
			itemSizeMatcher.matches();

			if (!itemSizeMatcher.group(2).equalsIgnoreCase(MassAndVolume.MASSG.getValue()))
			{
				HelperFunctions.convertSizeField(MassAndVolume.MASSG.getValue(), itemSizeMatcher, itemSize);
				
				itemSizeMatcher = matchAmountPattern.matcher(itemSize.getText());
				itemSizeMatcher.matches();
			}
			sizeNumber = Double.parseDouble(itemSizeMatcher.group(1));
			sizeScale = itemSizeMatcher.group(2).toLowerCase();
		}
		
		double purchaseCount = Double.parseDouble(itemPurchaseCount.getText()) * sizeNumber;
		purchaseCount = purchaseCount * Math.pow(10, 4);
		purchaseCount = Math.floor(purchaseCount); 
		purchaseCount = purchaseCount / Math.pow(10, 4); 
		
		wmiMaster.set_itemStockOnHand(wmiMaster.get_itemStockOnHand() + purchaseCount);
		wmiMaster.set_itemAmountScale(sizeScale);
		winemakerLogger.writeLog(String.format("   InventoryManagementController.createAssetPurchaseRecord() updated master's stock-on-hand%n"), debugLogging);
		
		wmiPurchase.setItemTaskId(ActivityName.INVENTORYBUY.getValue());
		wmiPurchase.set_itemActivityAmount(purchaseCount);
		wmiPurchase.set_itemAmountScale(sizeScale);
		wmiPurchase.set_itemPurchaseCost(Double.parseDouble(itemPurchaseCost.getText()) * Double.parseDouble(itemPurchaseCount.getText()));

		Optional<String> vendorFind = vendorCodeMap.keySet()
				.stream()
				.filter(code -> itemSupplierSelections.getValue().equals(vendorCodeMap.get(code)))
				.findFirst();
		wmiPurchase.set_itemPurchaseVendor(vendorFind.get());
		
		inventoryNewRecordsList.add(wmiPurchase);

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.createAssetPurchaseRecord()"), debugLogging);		
	} // end of createPurchaseRecord()
	
	/*
	 * Validation for asset update task:
	 * 		Reduce stock
	 * 			For capital assets with id's, this can only be '1'
	 * 			For consumable assets, this will be a 2-part value like "35 g"
	 * 		Update batch id
	 */
	private ValidationPackage validateUpdateSubmit()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validateUpdateSubmit()", debugLogging);

		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
		
		String errorMsg = (itemExistingSelections.getSelectionModel().isEmpty()) ? 
				"An asset must be selected\n" : "";
		errorMsg += (itemRemoveFromStock.getText().length() == 0 && itemBatchId.getText().length() == 0) ?
				"No update input\n" : "" ;
		
		if (itemRemoveFromStock.getText().length() > 0)
		{
			errorMsg += (itemRemovalReason.getValue().equals("Update Reason")) ?
					"An Update Reason must be supplied\n" : "";
			errorMsg += (itemBatchId.getText().length() > 0) ?
					"Remove from stock and setting the batch id are mutually exclusive\n" : "";
		}
		
		if (errorMsg.length() == 0)
		{
			List<WineMakerInventory> filteredQueryList = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(), itemExistingSelections.getValue());
			WineMakerInventory selectedRecord = filteredQueryList.get(0);
			
			ValidationPackage validateAssetType = new ValidationPackage();
			if (selectedRecord.getItemId().length() > 0)
				validateAssetType = validateCapitalUpdate();				
			else
				validateAssetType = validateConsumableUpdate();

			errorMsg += validateAssetType.getErrorMsg();		
		}

		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validateUpdateSubmit(): '%s'", errorMsg), debugLogging);
		}

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validateUpdateSubmit(): returning %n: '%s'", validatePackage.getErrorMsg()), debugLogging);
		return validatePackage;
	} // end of validateUpdateSubmit()

	private ValidationPackage validateCapitalUpdate()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validateCapitalUpdate()", debugLogging);
	
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
		String errorMsg = "";
		
		if (itemRemoveFromStock.getText().length() > 0)
			errorMsg += (!HelperFunctions.validateInteger(itemRemoveFromStock.getText(), 1)) ?
				String.format("Remove count can only be 1, not '%s'%n", itemRemoveFromStock.getText()) : "";
	
		if (itemBatchId.getText().length() > 0 && !this.getBatchIdReference().equals(itemBatchId.getText()))
		{
			ArrayList<WineMakerLog> batchSets = winemakerModel.queryBatch("", SQLSearch.PARENTBATCH);
			
			Optional<String> batchKeyTest = batchSets
					.stream()
					.filter(wmk -> wmk.get_batchKey().equals(itemBatchId.getText()))
					.map(wmk -> wmk.get_batchKey())
					.findFirst();
			if (batchKeyTest.isEmpty())
				errorMsg += "New batch id is not valid\n";						
		}
	
		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validateUpdateSubmit(): '%s'", errorMsg), debugLogging);
		}
	
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validateCapitalUpdate(): returning %n: '%s'", validatePackage.getErrorMsg()), debugLogging);
		return validatePackage;
	} // end of validateCapitalSubmit()

	private ValidationPackage validateConsumableUpdate()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validateConsumableUpdate()", debugLogging);
	
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
		Matcher inStockMatcher;
		String errorMsg = "";
		
		Matcher validateMatcher = HelperFunctions.validateNumberInput(itemRemoveFromStock.getText());
		errorMsg += (!itemRemoveFromStock.getText().isEmpty() && validateMatcher.groupCount() < 2) ?
				String.format("Item size should be like '48 oz, or 100 g', not '%s'%n", itemSize.getText()) : "";
	
		if (validateMatcher.matches() && validateMatcher.groupCount() == 2)
		{
			inStockMatcher = HelperFunctions.validateNumberInput(itemStockOnHand.getText());
			if (!inStockMatcher.group(2).equalsIgnoreCase(validateMatcher.group(2)))
			{
				HelperFunctions.convertSizeField(inStockMatcher.group(2), validateMatcher, itemRemoveFromStock);
			}
		}
	
		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validateUpdateSubmit(): '%s'", errorMsg), debugLogging);
		}
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validateConsumableUpdate(): returning message  %n'%s'", validatePackage.getErrorMsg()), debugLogging);
		return validatePackage;
	} // end of validateConsumableSubmit()

	private ValidationPackage validateCapitalPurchase()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validateCapitalPurchase()", debugLogging);
		
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
	
		String errorMsg = "";

		List<WineMakerInventory> searchSet = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(), buildItemDisplay.apply(itemAvailablePurchaseTypes.getValue(), itemID.getText()));
		Matcher validateMatcher = HelperFunctions.validateNumberInput(itemPurchaseCount.getText());

		if (itemID.getText().length() > 16)
			errorMsg += String.format("'%s' has a limit of 16 characters%n", itemID.getText());
		if (searchSet.size() > 0)
			errorMsg += "An inventory record for this record already exists\n";
		if (validateMatcher.matches() && (validateMatcher.groupCount() > 1) || (Integer.parseInt(validateMatcher.group(1)) > 1))
			errorMsg += String.format("Supplying an ID implies a purchase count of 1, not '%s'%n", itemPurchaseCount.getText());

		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validateCapitalPurchase(): '%s'", errorMsg), debugLogging);
		}

		winemakerLogger.writeLog(">> InventoryManagementController.validateCapitalPurchase()", debugLogging);
		return validatePackage;		
	} // end of validateCapitalPurchase()
	
	private ValidationPackage validateConsumablePurchase()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validateConsumablePurchase()", debugLogging);
		
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
	
		String errorMsg = "";

		List<WineMakerInventory> searchSet = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(), buildItemDisplay.apply(itemAvailablePurchaseTypes.getValue(), itemID.getText()));
		Matcher validateMatcher = HelperFunctions.validateNumberInput(itemSize.getText());
		errorMsg += (!itemSize.getText().isEmpty() && validateMatcher.groupCount() < 2) ?
				String.format("Item size should be like '48 oz, or 100 g', not '%s'%n", itemSize.getText()) : "";
		
		if (validateMatcher.matches() && (validateMatcher.groupCount() == 2) && (searchSet.size() > 0))
		{
			if (!searchSet.get(0).get_itemAmountScale().equalsIgnoreCase(validateMatcher.group(2)))
				HelperFunctions.convertSizeField(searchSet.get(0).get_itemAmountScale(), validateMatcher, itemSize);
		}

		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validateConsumablePurchase(): '%s'", errorMsg), debugLogging);
		}

		winemakerLogger.writeLog(">> InventoryManagementController.validateConsumablePurchase()", debugLogging);
		return validatePackage;		
	} // end of validateConsumablePurchase()
	
	/*
	 * Validate purchase input. 
	 * If an ID is provided, that indicates a capital asset, with a count of 1 and no sizing.
	 * 
	 */
	private ValidationPackage validatePurchaseSubmit()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validatePurchaseSubmit()", debugLogging);
		
		ValidationPackage validatePackage = validatePurchaseSubmitDefaults();
		
		String errorMsg = validatePackage.getErrorMsg();
		
		if (validatePackage.getTestStatus().equals(Validation.PASSED) && (itemID.getText().length() > 0))
		{
			errorMsg += validateCapitalPurchase().getErrorMsg();
		}

		if (validatePackage.getTestStatus().equals(Validation.PASSED) && (itemID.getText().length() == 0))
		{
			errorMsg += validateConsumablePurchase().getErrorMsg();
		}

		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validatePurchaseSubmit(): '%s'", errorMsg), debugLogging);
		}
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validatePurchaseSubmit(): returning %n'%s'", validatePackage.getErrorMsg()), debugLogging);
		return validatePackage;
	} // end of validateCapitalPurchase()

	private ValidationPackage validatePurchaseSubmitDefaults()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.validatePurchaseSubmitDefaults()", debugLogging);
		
		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
	
		String errorMsg = "";
	
		errorMsg = (itemAvailablePurchaseTypes.getSelectionModel().isEmpty()) ? 
				"An item type must be selected\n" : "";
		errorMsg += (itemPurchasedDate.getValue() == null) ? 
				"A purchase date must be specified\n" : "";
		errorMsg += (itemPurchasedDate.getValue() != null && itemPurchasedDate.getValue().getYear() == 1900) ? 
				String.format("Invalid date string was converted to 1/1/1900 from %s%n", datePickerOriginal) : "";
		errorMsg += (!HelperFunctions.validateInteger(itemPurchaseCount.getText())) ? 
				String.format("Invalid count value '%s' for '%s'%n", itemPurchaseCount.getText(), labelPurchaseCount.getText()): "";
		errorMsg += (!HelperFunctions.validateDouble(itemPurchaseCost.getText())) ? 
				String.format("Invalid cost value '%s' for '%s'%n", itemPurchaseCost.getText(), labelPurchaseCost.getText()) : "";
		errorMsg += (itemSupplierSelections.getValue() == null) ? 
				String.format("'%s' must be selected%n", labelSupplier.getText()) : "";
	
		if (errorMsg.length() > 0)
		{
			validatePackage.setErrorMsg(errorMsg);
			validatePackage.setTestStatus(Validation.FAILED);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.validatePurchaseSubmitDefaults(): '%s'", errorMsg), debugLogging);
		}
	
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validatePurchaseSubmitDefaults(): returning message%n'%s'", validatePackage.getErrorMsg()), debugLogging);
		return validatePackage;
	} // end of validatePurchaseSubmitDefaults()

	private Double processItemRemoval(Double stockOnHand, String removalInput)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.processItemRemoval('%s')", removalInput), debugLogging);		

		Matcher validateMatcher = HelperFunctions.validateNumberInput(removalInput);
		Double newStockOnHand = 0.0;

		if (validateMatcher.matches() && validateMatcher.groupCount() == 2)
		{
			newStockOnHand =  stockOnHand - Double.parseDouble(validateMatcher.group(1));
			newStockOnHand = newStockOnHand * Math.pow(10, 4);
			newStockOnHand = Math.floor(newStockOnHand); 
			newStockOnHand = newStockOnHand / Math.pow(10, 4); 
			
			if (newStockOnHand < 1)
			{
				newStockOnHand = 0.0;

				winemakerLogger.writeLog(String.format("   InventoryManagementController.processItemRemoval(): reduced stock to 0"), debugLogging);
			}
		}

		winemakerLogger.writeLog(String.format(">> InventoryManagementController.processItemRemoval('%s')", removalInput), debugLogging);
		return newStockOnHand;
	} // end of processItemRemoval()
	
	/*
	 * Update the Inventory master record with updates 
	 * The remove count can be 1 for capital assets, or like '34 g' for consumable assets 
	 */
	private ValidationPackage processUpdateSubmit()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.processUpdateSubmit()"), debugLogging);		

		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
		
		List<WineMakerInventory> inventoryQueryList = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(),	itemExistingSelections.getValue());
		Optional<WineMakerInventory> itemMaster = inventoryQueryList.stream()
				.filter(wmi -> wmi.getItemTaskId().length() == 0)
				.findFirst();

		WineMakerInventory updateRecord = itemMaster.get();
		
		if (itemRemoveFromStock.getText().length() > 0)
		{
			Double newStockOnHand = processItemRemoval(updateRecord.get_itemStockOnHand(), itemRemoveFromStock.getText());
			updateRecord.set_itemStockOnHand(newStockOnHand);

			if (newStockOnHand == 0)
				inventoryOldRecordsDeleteList.add(updateRecord);		
			else
				inventoryOldRecordsUpdateList.add(updateRecord);			
		}
		else
		{
			updateRecord.setItemBatchId(itemBatchId.getText());
			inventoryOldRecordsUpdateList.add(updateRecord);			
		}
				
		createAssetUpdateRecord(updateRecord);
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.processUpdateSubmit()"), debugLogging);		
		return validatePackage;
	} // end of processUpdateSubmit()
	
	/*
	 * 
	 */
	private ValidationPackage processPurchaseSubmit()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.processPurchaseSubmit()"), debugLogging);

		ValidationPackage validatePackage = new ValidationPackage();
		validatePackage.setErrorMsg("");
		validatePackage.setTestStatus(Validation.PASSED);
		WineMakerInventory wmiMaster = new WineMakerInventory();

		List<WineMakerInventory> inventoryQueryList = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(),	buildItemDisplay.apply(itemAvailablePurchaseTypes.getValue(), itemID.getText()));
		
		if (inventoryQueryList.size() == 0)
			wmiMaster = createMasterRecord();
		else
		{
			Optional<WineMakerInventory> itemMaster = inventoryQueryList.stream()
					.filter(wmi -> wmi.getItemTaskId().length() == 0)
					.findFirst();
			wmiMaster = itemMaster.get();
			inventoryOldRecordsUpdateList.add(wmiMaster);
			winemakerLogger.writeLog(String.format("   InventoryManagementController.processPurchaseSubmit(): add to update list %s", wmiMaster), debugLogging);
		}
		
		createAssetPurchaseRecord(wmiMaster, inventoryQueryList.size());

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.processPurchaseSubmit()"), debugLogging);
		return validatePackage;
	} // end of processPurchaseSubmit()
	
	/**
	 * 	1. Call the validation routines.
	 * 	2. Depending on whether this is a new item or not, call requisite build method
	 */
	public void submitBatch()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.submitBatch()", debugLogging);
	
		statusUpdates.clear();
		HashMap<String, String> itemDisplay = HelperFunctions.getCodeKeyFamily(FamilyCode.ADDITIVEFAMILY.getValue());
		itemDisplay.putAll(HelperFunctions.getCodeKeyFamily(FamilyCode.YEASTFAMILY.getValue()));
		itemDisplay.putAll(HelperFunctions.getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue()));
	
		if (validationManager().getTestStatus().equals(Validation.FAILED))
		{
			statusUpdates.appendText(validationManager().getErrorMsg());
			return;
		}
	
		if (submissionManager().getTestStatus().equals(Validation.FAILED))
		{
			statusUpdates.appendText(submissionManager().getErrorMsg());
			return;
		}
		
		String itemName = "";
		int updateCount = 0;
		for (WineMakerInventory wmi: inventoryNewRecordsList)
		{
			itemName = itemDisplay.get(wmi.get_itemName());
			itemName += (wmi.getItemId().length() > 0) ?
					String.format(" (%s)", wmi.getItemId()) : "";
			
			if (!winemakerModel.insertInventory(wmi))
				statusUpdates.appendText(String.format("Inventory record '%s' was not saved%n", itemName));
			else
			{
				updateCount++;
				if (updateCount == 1)
					statusUpdates.appendText(String.format("Adjustment asset record '%s' successfully saved%n", itemName));
				winemakerLogger.writeLog(String.format("   InventoryManagementController.submitBatch(): added %s", wmi), debugLogging);
			}
		}
		
		for (WineMakerInventory wmi: inventoryOldRecordsUpdateList)
		{
			if (!winemakerModel.updateInventoryBatch(wmi))
			{
				if (!winemakerModel.insertInventory(wmi))
					statusUpdates.appendText("Inventory record update failure\n");
			}
			else
			{
				statusUpdates.appendText("Parent asset record successfully updated\n");
				winemakerLogger.writeLog(String.format("   InventoryManagementController.submitBatch(): updated %s", wmi), debugLogging);

				for(WineMakerInventory wmiShow: winemakerModel.queryInventory(wmi.get_itemName(), ""))
				{
					if (wmiShow.get_itemStockOnHand() > 0)
						winemakerLogger.writeLog(String.format("   InventoryManagementController.submitBatch(): post-updated %s", wmiShow), debugLogging);					
				}
			}
		}
	
		for (WineMakerInventory wmi: inventoryOldRecordsDeleteList)
		{
			if (!winemakerModel.deleteDateRecord(wmi.getItemTaskTime(), DatabaseTables.INVENTORY.getValue()))
				statusUpdates.appendText("Inventory record delete failure\n");
			else
				statusUpdates.appendText("Inventory record successfully deleted\n");
		}
		
		resetUI();
		
		winemakerLogger.writeLog("<< InventoryManagementController.submitBatch()", debugLogging);
	} // end of submitBatch()

	/*
	 * Call validation method for the current task mode
	 */
	private ValidationPackage validationManager()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.validationManager('%s')", uiSetting), debugLogging);
		ValidationPackage validatePackage = new ValidationPackage();
		
		if (uiSetting.equals(InventoryScene.UPDATE))
			validatePackage = validateUpdateSubmit();
		else
			validatePackage = validatePurchaseSubmit();
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.validationManager('%s')", uiSetting), debugLogging);
		return validatePackage;
	} // end of validationManager()

	/*
	 * Call submission method for the current task mode
	 */
	private ValidationPackage submissionManager()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.submissionManager('%s')", uiSetting), debugLogging);
		ValidationPackage validatePackage = new ValidationPackage();
		
		if (uiSetting.equals(InventoryScene.UPDATE))
			validatePackage = processUpdateSubmit();
		else
			validatePackage = processPurchaseSubmit();
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.submissionManager('%s')", uiSetting), debugLogging);
		return validatePackage;
	} // end of submissionManager()
	
	/*
	 * Collect the set of resource codes
	 */
	private List<String> getResourceValues(HashMap<String, String> resourceCodes)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.getResourceValues('%s')", resourceCodes), debugLogging);

		HashMap<String, String> valueCodeSet = new HashMap<>();
		valueCodeSet.putAll(resourceCodes);
		
		List<String> resourceValueList = valueCodeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.getResourceValues('')"), debugLogging);
		return resourceValueList;
	} // end of getResourceValues()
	
	/*
	 * Retrieve the item's parent record containing stock on hand.  
	 * Save the in-stock value for comparison with any UI changes.
	 */
	private void itemSelectExistingHandler(String itemSelectedExisting)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.itemSelectExistingHandler('%s')", itemSelectedExisting), debugLogging);
				
		List<WineMakerInventory> filteredQueryList = HelperFunctions.findAssetItemRecord(getInventoryExistingSetsQueryList(), itemSelectedExisting);
		if (filteredQueryList.size() > 0)
		{
			WineMakerInventory selectedRecord = filteredQueryList.get(0);
			
			String showDouble = "";		
			BigDecimal testDecimal = new BigDecimal(String.valueOf(selectedRecord.get_itemStockOnHand()));
			
			try
			{
				showDouble = Integer.toString(testDecimal.intValueExact());
			}
			catch (ArithmeticException  e)
			{
				showDouble = testDecimal.toPlainString();
			}
			
			if (filteredQueryList.get(0).get_itemAmountScale().length() > 0)
			{
				showDouble = String.format("%s %s", showDouble, selectedRecord.get_itemAmountScale());
			}

			this.itemStockOnHand.setText(showDouble);
			this.itemBatchId.setText(selectedRecord.getItemBatchId());
			this.setBatchIdReference(selectedRecord.getItemBatchId());
			this.setInStockReference(selectedRecord.get_itemStockOnHand());
			this.setSelectedInventoryRecord(selectedRecord);
			
			winemakerLogger.writeLog(String.format("   InventoryManagementController.itemSelectExistingHandler('%s'): set value %s", itemSelectedExisting, itemStockOnHand.getText()), debugLogging);
		}
		
		statusUpdates.clear();
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.itemSelectExistingHandler('%s')", itemSelectedExisting), debugLogging);
	} // end of itemSelectExistingHandler()

	/*
	 * Verify removal count input and save for subsequent processing.
	 */
	private void itemRemoveStockHandler(String removeAmount)
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.itemRemoveStockHandler('%s')", removeAmount), debugLogging);
	
		if (HelperFunctions.validateDouble(removeAmount, this.getInStockReference()))
			this.setInStockRemoved(Double.parseDouble(removeAmount));
		
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.itemRemoveStockHandler('%s')", removeAmount), debugLogging);
	} // end of itemRemoveStockHandler()

	/*
	 * Verify purchase input and save for subsequent processing.
	 */
	private void itemPurchasedAmountHandler(String purchaseAmount) 
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.itemPurchasedAmountHandler('%s'): inStock = %1.2f", purchaseAmount, this.getInStockReference()), debugLogging);

		if (HelperFunctions.validateInteger(purchaseAmount))
			this.setPurchaseAmount(Integer.parseInt(purchaseAmount));

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.itemPurchasedAmountHandler('%s')", purchaseAmount), debugLogging);
	} // end of itemPurchasedAmountHandler
	
	private void vendorSelectHandler(String vendorSelected)
	{}
	
	/**
	 * Rebuild UI 
	 */
	public void switchFunctionUI()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.switchFunctionUI()", debugLogging);
		
		resetUIFields();

		if (uiSetting.equals(InventoryScene.UPDATE))
			switchToPurchaseUI();
		else
			switchToUpdateUI();
		
		winemakerLogger.writeLog("<< InventoryManagementController.switchFunctionUI()", debugLogging);
	} // end of switchFunctionUI()
	
	/*
	 * Switch UI to process updates to existing inventory
	 */
	private void switchToUpdateUI()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.switchToUpdateUI()", debugLogging);

		buttonDisplaySwitch.setText("+");
		buttonDisplaySwitch.setTooltip(HelperFunctions.buildTooltip(NEWASSET));
		
		uiSetting = InventoryScene.UPDATE;
		
		buildUIForUpdates();
		statusUpdates.clear();
		
		winemakerLogger.writeLog("<< InventoryManagementController.switchToUpdateUI()", debugLogging);		
	} // end of switchToUpdateUI()

	/*
	 * Switch UI to process additions to existing inventory
	 */
	private void switchToPurchaseUI()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.switchToPurchaseUI()", debugLogging);

		buttonDisplaySwitch.setText("^");
		buttonDisplaySwitch.setTooltip(HelperFunctions.buildTooltip(REMASSET));

		uiSetting = InventoryScene.PURCHASE;
		
		buildUIForPurchases();
		statusUpdates.clear();

		winemakerLogger.writeLog("<< InventoryManagementController.switchToPurchaseUI()", debugLogging);		
	} // end of switchToPurchaseUI()
	
	/*
	 * Build the UI for the asset update task
	 */
	private void buildUIForUpdates() 
	{
		winemakerLogger.writeLog(">> InventoryManagementController.buildUIForUpdates()", debugLogging);
		
		resetUIFields();
		uiSetting = InventoryScene.UPDATE;
				
		itemExistingSelectionsList.clear();
		
		this.itemExistingSelectionsList.addAll(getInventoryExistingSetsQueryList()
				.stream()
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> HelperFunctions.getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue()).containsKey(wmi.get_itemName()))
				.map(wmi -> buildItemDisplay.apply(HelperFunctions.getCodeKeyEntry(FamilyCode.CONTAINERFAMILY.getValue(), wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		
		this.itemExistingSelectionsList.add(itemSelectSeparator);
		this.itemExistingSelectionsList.addAll(getInventoryExistingSetsQueryList()
				.stream()
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> HelperFunctions.getCodeKeyFamily(FamilyCode.ADDITIVEFAMILY.getValue()).containsKey(wmi.get_itemName()))
				.map(wmi -> buildItemDisplay.apply(HelperFunctions.getCodeKeyEntry(FamilyCode.ADDITIVEFAMILY.getValue(), wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));

		this.itemExistingSelectionsList.add(itemSelectSeparator);
		this.itemExistingSelectionsList.addAll(getInventoryExistingSetsQueryList()
				.stream()
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> HelperFunctions.getCodeKeyFamily(FamilyCode.YEASTFAMILY.getValue()).containsKey(wmi.get_itemName()))
				.map(wmi -> buildItemDisplay.apply(HelperFunctions.getCodeKeyEntry(FamilyCode.YEASTFAMILY.getValue(), wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		
		this.itemExistingSelections.setItems(this.itemExistingSelectionsList);
		this.itemExistingSelections.setPromptText("Select Item");

		this.itemRemovalReason.setValue(this.itemRemoveReasonSelectionsList.get(0));
		
		gp.add(labelItemSelections, 0, 0);		
		gp.add(labelStockOnHand, 0, 1);
		gp.add(labelRemoveStock, 0, 2);
		gp.add(labelBatchId, 0, 3);		
		gp.add(labelRemoveReason, 0, 4);
		
		gp.add(itemExistingSelections, 1, 0);
		gp.add(itemStockOnHand, 1, 1);
		gp.add(itemRemoveFromStock, 1, 2);
		gp.add(itemBatchId, 1, 3);
		gp.add(itemRemovalReason, 1, 4);
		gp.add(buttonDisplaySwitch, 1, 5);
		
		winemakerLogger.writeLog("<< InventoryManagementController.buildUIForUpdates()", debugLogging);			
	} // end of buildUIForUpdates()
	
	/*
	 * Build the UI for the asset purchase task
	 */
	private void buildUIForPurchases()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.buildUIForPurchases()", debugLogging);

		resetUIFields();
		uiSetting = InventoryScene.PURCHASE;
		
		itemAvailableTypesList.clear();
		
		this.itemAvailableTypesList.addAll(getResourceValues(resourceCodesFamilyMap.get(FamilyCode.LABFAMILY.getValue())));
		this.itemAvailableTypesList.add(itemSelectSeparator);

		this.itemAvailableTypesList.addAll(getResourceValues(resourceCodesFamilyMap.get(FamilyCode.CONTAINERFAMILY.getValue())));
		this.itemAvailableTypesList.add(itemSelectSeparator);

		this.itemAvailableTypesList.addAll(getResourceValues(resourceCodesFamilyMap.get(FamilyCode.ADDITIVEFAMILY.getValue())));
		this.itemAvailableTypesList.add(itemSelectSeparator);

		this.itemAvailableTypesList.addAll(getResourceValues(resourceCodesFamilyMap.get(FamilyCode.YEASTFAMILY.getValue())));

		this.itemAvailablePurchaseTypes.setItems(this.itemAvailableTypesList);
		
		setInventoryExistingSetsQueryList(winemakerModel.queryInventory());

		this.itemAvailablePurchaseTypes.setPromptText("Select type for purchase");
		this.itemPurchasedDate.setValue(null);
		this.itemID.clear();
		this.itemPurchaseCount.clear();
		this.itemPurchaseCost.clear();
		this.itemSize.clear();
		this.itemSupplierSelections.setPromptText("Select item vendor");
		
		gp.add(labelPurchaseType, 0, 0);
		gp.add(labelPurchaseDate, 0,1);
		gp.add(labelItemID, 0, 2);
		gp.add(labelPurchaseCount, 0, 3);
		gp.add(labelPurchaseCost, 0, 4);
		gp.add(labelSize, 0, 5);		
		gp.add(labelSupplier, 0, 6);
		
		gp.add(itemAvailablePurchaseTypes, 1, 0);
		gp.add(itemPurchasedDate, 1,1);
		gp.add(itemID, 1, 2);
		gp.add(itemPurchaseCount, 1, 3);
		gp.add(itemPurchaseCost, 1, 4);
		gp.add(itemSize, 1, 5);
		gp.add(itemSupplierSelections, 1, 6);
		gp.add(buttonDisplaySwitch, 1, 7);
		
		winemakerLogger.writeLog("<< InventoryManagementController.buildUIForPurchases()", debugLogging);			
	} // end of buildUIForPurchases()
	
	/*
	 *	Populate the various selection lists.
	 *	The resource item list is a combination of: inventory-specific items, container items, chemical items,
	 *		and yeast items.   A text string separator is inserted into the selection list, a value which is
	 *		then ignored in the handler code. 
	 */
	private void loadUIDefaults()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.loadUIDefaults()"), debugLogging);

		setInventoryExistingSetsQueryList(winemakerModel.queryInventory());
		
		itemPurchasedDate.setConverter(localDateStringConverter);
		DropShadow dS = new DropShadow();
		
		this.itemExistingSelections.setEffect(dS);
		this.itemExistingSelections.setPrefWidth(300);
		this.itemAvailablePurchaseTypes.setPrefWidth(240);
		this.itemID.setPrefWidth(120);
		itemSupplierSelections.setPrefWidth(200);
		
		this.itemStockOnHand.setEditable(false);

		this.itemPurchasedDate.setEffect(dS);
		this.itemExistingSelections.setEffect(dS);
		this.itemStockOnHand.setEffect(dS);
		this.itemRemoveFromStock.setEffect(dS);
		this.itemRemovalReason.setEffect(dS);
		this.itemBatchId.setEffect(dS);

		this.itemAvailablePurchaseTypes.setEffect(dS);
		this.itemSupplierSelections.setEffect(dS);
		this.itemID.setEffect(dS);
		this.itemPurchaseCount.setEffect(dS);
		this.itemPurchaseCost.setEffect(dS);
		this.itemSize.setEffect(dS);
	
		this.vendorOptionsMap = HelperFunctions.getCodeKeyFamily(FamilyCode.SUPPLIESFAMILY.getValue());
		
		if (this.vendorOptionsMap == null)
			winemakerLogger.displayAlert("No vendors have been added to the inventory.  Go to Admin/Common Tasks/Update Resources to add new hardware vendors.");
		else
		{
			this.itemSupplierSelectionsList.addAll(getResourceValues(HelperFunctions.getCodeKeyFamily(FamilyCode.SUPPLIESFAMILY.getValue())));
			this.itemSupplierSelections.setItems(itemSupplierSelectionsList);
		}
		
		this.removeReasonOptionsMap = HelperFunctions.getCodeKeyFamily(FamilyCode.REMOVALFAMILY.getValue());
		if (removeReasonOptionsMap == null)
			winemakerLogger.displayAlert("No item removal codes have been added to the inventory.  Go to Admin/Common Tasks/Update Resources to add new removal reasons.");
		else
		{
			this.itemRemoveReasonSelectionsList.addAll(getResourceValues(HelperFunctions.getCodeKeyFamily(FamilyCode.REMOVALFAMILY.getValue())));
			this.itemRemoveReasonSelectionsList.add(0, "Update Reason");
			
			this.itemRemovalReason.setItems(itemRemoveReasonSelectionsList);
			
			this.itemBatchId.setPrefWidth(130);
			this.itemBatchId.setMaxWidth(itemBatchId.getPrefWidth());
		}
				
		labelBatchId.setId("labelBatchId");
		labelBatchId.setText("Batch Id");

		itemBatchId.setId("itemBatchId");
		
		buttonDisplaySwitch.setTooltip(HelperFunctions.buildTooltip(NEWASSET));
		conversionMap.putAll(HelperFunctions.getConversionMap());

		winemakerLogger.writeLog(String.format("<< InventoryManagementController.loadUIDefaults()"), debugLogging);
	} // end of loadUIDefaults()

	/*
	 * Note: this technique for resetting the ComboBoxes seems to work.
	 * 		1. Clear TextField selections
	 * 		2. Set new ButtonCell for ComboBoxes (what happened to the last one...?)
	 */
	private void resetUI()
	{
		winemakerLogger.writeLog(String.format(">> InventoryManagementController.resetUI()"), debugLogging);
	
		setInventoryExistingSetsQueryList(winemakerModel.queryInventory());
		
		this.inventoryNewRecordsList.clear();
		this.inventoryOldRecordsUpdateList.clear();
		this.inventoryOldRecordsDeleteList.clear();
		
		if (this.uiSetting.equals(InventoryScene.UPDATE))
			buildUIForUpdates();
		else
			buildUIForPurchases();
	
		winemakerLogger.writeLog(String.format("<< InventoryManagementController.resetUI()"), debugLogging);
	} // end of resetUI()

	/*
	 * Reset the GridPane elements for task switching
	 */
	private void resetUIFields()
	{
		winemakerLogger.writeLog(">> InventoryManagementController.resetUIFields()", debugLogging);
		
		ArrayList<Node> oldNodes = new ArrayList<>();

		ObservableList<Node> childNodes = this.gp.getChildren();

		Predicate<Node> findCustom = node -> node.getId() != null;
		oldNodes.addAll(childNodes
					.stream()
					.filter(findCustom)
					.collect(Collectors.toList()));
		
		for (Node oldNode : oldNodes) 
		{
			if (oldNode instanceof TextField) 
			{
				TextField textNode = (TextField) oldNode;
				gp.getChildren().remove(textNode);
			} 
			else if (oldNode instanceof TextArea)
			{
				TextArea textArea = (TextArea) oldNode;
				gp.getChildren().remove(textArea);
			}
			else if (oldNode instanceof DatePicker)
			{
				DatePicker oldDate = (DatePicker) oldNode;
				gp.getChildren().remove(oldDate);
			}
			else if (oldNode instanceof Label)
			{
				Label labelField = (Label) oldNode;
				gp.getChildren().remove(labelField);
			}
			else if (oldNode instanceof Button)
			{
				Button buttonField = (Button) oldNode;
				gp.getChildren().remove(buttonField);
			}
			else if (oldNode instanceof ComboBox<?>)
			{
				@SuppressWarnings("unchecked")
				ComboBox<String> cmbBox = (ComboBox<String>) oldNode;
				gp.getChildren().remove(cmbBox);
			}
			else if (oldNode instanceof ChoiceBox<?>)
			{
				@SuppressWarnings("unchecked")
				ChoiceBox<String> choiceBox = (ChoiceBox<String>) oldNode;
				gp.getChildren().remove(choiceBox);
			}
		}

		winemakerLogger.writeLog("<< InventoryManagementController.resetUIFields()", debugLogging);
	} // end of resetUIFields()

	/*
	 * Return to home Scene
	 */
	@FXML
	public void returnToMain(ActionEvent e) 
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WineMaker.fxml"));
	
		try {
			WineMakerController winemakerController = new WineMakerController();
			loader.setController(winemakerController);
	
			Parent batchDetailParent = loader.load();
			Scene winemakerScene = new Scene(batchDetailParent);
			winemakerScene.getStylesheets().add(getClass().getResource("modena.css").toExternalForm());
	
			Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
			window.setScene(winemakerScene);
			window.setResizable(false);
	
			window.show();
		} 
		catch (IOException e1) 
		{
			winemakerLogger.displayAlert(e1.getMessage());
			return;
		}
	}
	
	/*
	 * Input string converter for the DatePicker control.  Prevents the class from
	 * throwing an uncatchable exception if an invalid date string is manually entered.
	 */
    LocalDateStringConverter localDateStringConverter = new LocalDateStringConverter() {
        @Override
        public LocalDate fromString(String value) {
            try 
            {
               return super.fromString(value);
            } 
            catch (Exception e) 
            {
                datePickerOriginal = value;
                return LocalDate.of(1900, 1, 1);
            }
        }

        @Override
        public String toString(LocalDate value) {
            return super.toString(value);
        }
    };
    
	/*
	 * Provided for resetting ComboBox button prompts
	 */
	@SuppressWarnings("unused")
	private static class ButtonCell extends ListCell<String> {
		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			setText(item);
		}
	}

	class ValidationPackage {
	
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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		loadUIDefaults();
		buildUIForUpdates();

		itemRemoveReasonProperty.bind(itemRemovalReason.valueProperty());
		
		itemCurrAmountProperty.addListener(new ChangeListener<String>() 
		{
			@SuppressWarnings("rawtypes")
			@Override public void changed(ObservableValue ov, String oldValue, String newValue) { 
			} 
		});
		
		itemExistingSelections.valueProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable arg0) {				
				if ((itemExistingSelections.getValue() != null) && (!itemExistingSelections.getValue().equals(itemSelectSeparator)))
					itemSelectExistingHandler(itemExistingSelections.getValue());
				else
				{
					itemExistingSelections.getSelectionModel().clearSelection();
					itemExistingSelections.setValue(null);
				}
			}
		});

		itemAvailablePurchaseTypes.valueProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable arg0) {				
				if ((itemAvailablePurchaseTypes.getValue() != null) && (!itemAvailablePurchaseTypes.getValue().equals(itemSelectSeparator)))
					itemSelectExistingHandler(itemAvailablePurchaseTypes.getValue());
				else
				{
					itemAvailablePurchaseTypes.getSelectionModel().clearSelection();
					itemAvailablePurchaseTypes.setValue(null);
				}
			}
		});

		itemSupplierSelections.valueProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable arg0) {
				if (itemSupplierSelections.getValue() != null)
					vendorSelectHandler(itemSupplierSelections.getValue());
			}			
		});

		itemRemoveFromStock.textProperty().addListener(new ChangeListener<String>() 
		{
			@SuppressWarnings("rawtypes")
			@Override public void changed(ObservableValue ov, String oldValue, String newValue) { 
				itemRemoveStockHandler(newValue);
			} 
		});

		itemID.textProperty().addListener(new ChangeListener<String>() 
		{
			@SuppressWarnings("rawtypes")
			@Override public void changed(ObservableValue ov, String oldValue, String newValue) { 
				if (newValue.length() > 0)
				{
					itemID.setText(newValue.toUpperCase());
					itemPurchaseCount.setText("1");
					itemPurchaseCount.setEditable(false);
					itemSize.setEditable(false);
				}
				else 
				{
					itemPurchaseCount.setText("");
					itemPurchaseCount.setEditable(true);					
					itemSize.setEditable(true);
				}
			}
		});

		itemPurchaseCount.textProperty().addListener(new ChangeListener<String>() 
		{
			@SuppressWarnings("rawtypes")
			@Override public void changed(ObservableValue ov, String oldValue, String newValue) { 
				itemPurchasedAmountHandler(newValue);
			}
		});
		
	}
}
