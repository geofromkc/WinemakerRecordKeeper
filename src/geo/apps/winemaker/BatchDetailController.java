package geo.apps.winemaker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import geo.apps.winemaker.utilities.Constants.ActivityName;
import geo.apps.winemaker.utilities.Constants.BatchSource;
import geo.apps.winemaker.utilities.Constants.Blend;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;
import geo.apps.winemaker.utilities.Constants.SQLSearch;
import geo.apps.winemaker.utilities.Constants.WeightsAndMeasures;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

/**
 *  Controller class for managing creation of new batches
 * 
 *  @author geo
 *  @version 1.0
 *  @since 2022-03-01
 */

public class BatchDetailController implements Initializable {

	private WineMakerLog updateWmkLog = null;
	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;

	private boolean updateMode = false;
	private boolean debugLogging = true;

	@FXML AnchorPane ap;
	@FXML GridPane gp;
	@FXML VBox vb;
	
	@FXML DatePicker batchDate;
	
	@FXML private TextField itemCount;
	@FXML private TextField itemPrice;
	@FXML private TextField itemUnits;

	@FXML private TextArea vendorNotes;
	@FXML private TextArea statusUpdates;

	@FXML private Button submitBatch;
	@FXML private Button addBlendGrape;
	@FXML private Button doneBlendMix;

	@FXML private Label batchTypeHeader;
	@FXML private Label batchTitle;
	
	@FXML private Label batchDateLabel;
	@FXML private Label blendTypeLabel;
	@FXML private Label batchSourceLabel;
	@FXML private Label batchGrapeLabel;
	@FXML private Label blendGrapeLabel;
	@FXML private Label batchVineyardLabel;
	@FXML private Label unitCountLabel;
	@FXML private Label unitPriceLabel;
	@FXML private Label unitPerItemLabel;
	@FXML private Label unitMeasureLabel;
	@FXML private Label batchVendorLabel;
	@FXML private Label batchNotesLabel;

	@FXML private CheckBox blendTypeCheckBox;

	private	TextArea displayContainerSelections = new TextArea();

	private Label juiceBlendContainerLabel1 = new Label();
	private Label juiceBlendContainerLabel2 = new Label();

	private HBox hBox_1 = new HBox();
	private HBox hBox_2 = new HBox();
	
	/*
	 * Scene HBoxes
	 */
	@FXML private HBox hb_BatchSource;
	@FXML private HBox hb_BatchMeasurement;
	@FXML private HBox hb_BatchVendor;
	@FXML private HBox hb5;
	@FXML private HBox hb6;

	/*
	 * Scene Radio Button Groups
	 */
	@FXML private ToggleGroup sourceTG = new ToggleGroup();
	@FXML private ToggleGroup blendTG = new ToggleGroup();
	@FXML private ToggleGroup measureTG = new ToggleGroup();
	@FXML private ToggleGroup vendorTG = new ToggleGroup();

	/*
	 * Scene ComboBoxes
	 */
	@FXML private ComboBox<String> grapeSelect;
	@FXML private ComboBox<String> grapeSelectBlend;
	@FXML private ComboBox<String> vineyardSelect;
	private ChoiceBox<String> vendorSelect = new ChoiceBox<String>();
	private ChoiceBox<String> batchInputSelect = new ChoiceBox<String>();
	
	private ObservableList<String> existingBatchesList = FXCollections.observableArrayList();
	private	ComboBox<String> usedSourceContainers = new ComboBox<String>();
	private	ComboBox<String> emptyTargetContainers = new ComboBox<String>();

	private Button addSourceContainerButton = new Button();
	private Button addTargetContainerButton = new Button();

	private HashMap<String, String> saveLastBatchVol = new HashMap<>(1);	
	private HashMap<String, String> validationMethods = new HashMap<>(3);
	private HashMap<String, String> buildUIMethods = new HashMap<>(3);
	private HashMap<String, String> inventoryTypes = new HashMap<>(100);	
	
	private ArrayList<WineMakerLog> createBatch = new ArrayList<WineMakerLog>();
	private ArrayList<WineMakerLog> updateBatch = new ArrayList<WineMakerLog>();
	private ArrayList<WineMakerFerment> createFerment = new ArrayList<WineMakerFerment>();
	private ArrayList<WineMakerInventory> updateInventoryActivity = new ArrayList<WineMakerInventory>();
	private ArrayList<WineMakerInventory> insertInventoryActivity = new ArrayList<WineMakerInventory>();
	private ArrayList<WineMakerInventory> inventoryExistingSetsQueryList = new ArrayList<WineMakerInventory>(100);
	private ArrayList<WineMakerLog> selectedBlendBatches = new ArrayList<WineMakerLog>();

	private ArrayList<WineMakerLog> wmkSets = new ArrayList<WineMakerLog>(20);
	
	private Integer sourceContainerCount = 0;
	private Integer targetContainerCount = 0;

	private String datePickerOriginal = "";
	private String regexNumber = "^([0-9]*\\.?[0-9]+)\\s*([A-z%]*)$";
    private String regexTypeNameWithId = "^(\\w*\\d{2})\\s*\\((.*)\\)$";
	private Pattern matchTypeNameWithIdPattern = Pattern.compile(regexTypeNameWithId);
	private Pattern regexAmtAndScalePattern = Pattern.compile(regexNumber);
	
	private boolean batchSubmitted = false;

	Object classInstance;
	Class<?> classRef;
	
	private Blend blendSetting;

	/**
	 * Construct new controller object with links to Model and Log objects
	 * Retrieve the set of parent batch objects, and populate each with their set of Ferment records
	 * 
	 * @param Reference to Model object for accessing constants and utility methods
	 * @param Reference to Logging object for accessing debug log
	 * @param Blend object for setting CREATE/UPDATE mode
	 */
	public BatchDetailController(Blend blendSetting) 
	{
		//WineMakerModel model, WineMakerLogging logger, 
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);

		this.blendSetting = blendSetting;

		winemakerLogger.writeLog(String.format(">> BatchDetailController.constructor(., ., %s)", blendSetting.toString()), debugLogging);
		
		this.wmkSets = winemakerModel.queryBatch("", SQLSearch.PARENTBATCH);

		this.wmkSets
			.stream()
			.forEach(wmk -> wmk.setWmkFerments(winemakerModel.queryFermentData(wmk.get_batchKey())));
		
		loadMethodMaps();
		setupClassInstance();
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.constructor()"), debugLogging);
	}

	private ArrayList<WineMakerLog> getBatchCreateSet()
	{
		return createBatch;
	}
	
	@SuppressWarnings("unused")
	private ArrayList<WineMakerLog> getBatchUpdateSet()
	{
		return updateBatch;
	}
	
	private ArrayList<WineMakerLog> getExistingBatchesSet()
	{
		return wmkSets;
	}

	private ArrayList<WineMakerLog> getSelectedBlendBatchesSet()
	{
		return selectedBlendBatches;
	}
	
	private void setSelectedBlendBatch(WineMakerLog blendBatch)
	{
		selectedBlendBatches.add(blendBatch);
	}
	
	private ArrayList<WineMakerInventory> getLocalInventorySet()
	{
		return inventoryExistingSetsQueryList;
	}
	
	private void setLocalInventorySet(ArrayList<WineMakerInventory> newInventorySet)
	{
		inventoryExistingSetsQueryList = newInventorySet;
	}
	
	private HashMap<String, String> getInventoryTypes()
	{
		return inventoryTypes;
	}
	private void setInventoryTypes(HashMap<String, String> newInventoryTypes)
	{
		inventoryTypes = newInventoryTypes;
	}
	
	/**
	 * Called from main controller to set the update log object
	 * 
	 * @param wineMakerLog Existing batch object selected in primary UI
	 */
	public void updateBatchScene(WineMakerLog wineMakerLog) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.updateBatchScene(%s)", wineMakerLog.get_batchKey()), debugLogging);

		this.updateWmkLog = wineMakerLog;
		updateMode = true;

		loadUpdateObject();
		batchTitle.setText("For batch " + HelperFunctions.batchKeyExpand(this.updateWmkLog));

		winemakerLogger.writeLog(String.format("<< BatchDetailController.updateBatchScene(%s) return", wineMakerLog.get_batchKey()), debugLogging);
	} // end of updateBatchScene()

	/*
	 * Load the class reference object for dynamically calling activity methods
	 */
	private void setupClassInstance()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.setupClassInstance()"), debugLogging);

		try 
		{
			this.classRef = Class.forName("geo.apps.winemaker.FermentDataDetailController");
		}
		catch (ClassNotFoundException | IllegalArgumentException | SecurityException e) 
		{
			winemakerLogger.showIOException(e, "Failure to set FermentDataDetailController class reference");
		}
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.setupClassInstance()"), debugLogging);
	} // end of setupClassInstance()

	/*
	 * Load maps with the names of methods for UI build and validation.
	 * Methods are dynamically called by validateInput.
	 */
	private void loadMethodMaps()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.loadMethodMaps()"), debugLogging);

		validationMethods.put(Blend.NOTBLEND.toString(), "validateVarietalBatch");
		buildUIMethods.put(Blend.NOTBLEND.toString(), "buildVarietalBatchUI");

		validationMethods.put(Blend.JUICEBLEND.toString(), "validateJuiceBlend");
		buildUIMethods.put(Blend.JUICEBLEND.toString(), "buildBlendJuiceUI");
		
		validationMethods.put(Blend.FIELDBLEND.toString(), "validateFieldBlend");
		buildUIMethods.put(Blend.FIELDBLEND.toString(), "buildBlendFieldUI");

		winemakerLogger.writeLog(String.format("<< BatchDetailController.loadMethodMaps()"), debugLogging);
	} // end of loadMethodMaps()

	/*
	 * In UPDATE mode, populate UI with instance values for potential modification
	 */
	private void loadUpdateObject() 
	{
		winemakerLogger.writeLog(">> BatchDetailController.loadUpdateObject()", debugLogging);
	
		RadioButton setButton = null;
	
		/*
		 * Set source selection
		 */
		for (Node buttonNode : hb_BatchSource.getChildren()) {
			setButton = (RadioButton) buttonNode;
			if ((buttonNode.getUserData()).equals(updateWmkLog.get_batchSource()))
				setButton.setSelected(true);
			else
				setButton.setDisable(true);
		}
	
		/*
		 * Populate the grapes list and set the selected grape
		 */
		ObservableList<String> comboboxSelections = FXCollections.observableArrayList();
		comboboxSelections.add(HelperFunctions.getCodeKeyEntry(FamilyCode.GRAPEFAMILY.getValue(), updateWmkLog.get_batchGrape()));
		
		grapeSelect.setItems(comboboxSelections);
		grapeSelect.setPromptText(comboboxSelections.get(0));
		grapeSelect.setValue(comboboxSelections.get(0));
		grapeSelect.setButtonCell(new ButtonCell());	
		vineyardSelect.setValue(HelperFunctions.getCodeKeyEntry(FamilyCode.VINEYARDFAMILY.getValue(), updateWmkLog.get_batchVineyard()));
	
		itemCount.setText(Integer.toString(updateWmkLog.get_sourceItemCount()));
		itemPrice.setText(Double.toString(updateWmkLog.get_sourceItemPrice()));
		itemUnits.setText(Integer.toString(updateWmkLog.get_sourceItemMeasure()));
	
		for (Node buttonNode : hb_BatchVendor.getChildren()) 
		{
			if ((buttonNode.getUserData()).equals(updateWmkLog.get_sourceVendor())) 
			{
				setButton = (RadioButton) buttonNode;
				setButton.setSelected(true);
			}
		}
	
		vendorNotes.setText(updateWmkLog.get_sourceVendorNotes());
		
		winemakerLogger.writeLog("<< BatchDetailController.loadUpdateObject() return", debugLogging);
	} // end of loadUpdateObject()
		
	/*
	 *	Verify the selected batch not part of an existing blend
	 * 	but why couldn't it be part of two blends?
	 */
	private boolean batchNotPartOfOldBlend(WineMakerLog wmk) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.batchNotPartOfOldBlend('%s')", wmk.get_batchKey()), debugLogging);

		boolean batchIsAvailable = wmk.get_batchBlendKey().length() == 0 && 
				HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPEFAMILY.getValue())
				.keySet().contains(wmk.get_batchGrape());
		
		if (!batchIsAvailable)
		{
			Integer thisBatchYear = Integer.parseInt(wmk.get_batchKey().substring(0, 1));
			List<String> oldBatches = this.wmkSets.stream().filter(wmkOld -> thisBatchYear > Integer.parseInt(wmkOld.get_batchKey().substring(0, 1)))
					.map(wmkOldYear -> wmkOldYear.get_batchKey().substring(0, 1))
					.collect(Collectors.toList());
			batchIsAvailable = (oldBatches.size() > 0);
		}

		winemakerLogger.writeLog(String.format("<< BatchDetailController.batchNotPartOfOldBlend('%s')", wmk.get_batchKey()), debugLogging);
		return batchIsAvailable;
	} // end of batchNotPartOfOldBlend()

	/*
	 * Verify the selected batch not already part of the new blend
	 */
	private boolean batchNotPartOfNewBlend(WineMakerLog wmk) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.batchNotPartOfNewBlend(wmk '%s') ", wmk.get_batchKey()), debugLogging);

		boolean returnState = createBatch
					.stream()
					.filter(wmkPending -> wmk.get_batchGrape().equals(wmkPending.get_batchGrape()))
					.collect(Collectors.toList()).size() == 0;
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.batchNotPartOfNewBlend(wmk '%s') return state = %b ", wmk.get_batchKey(), returnState), debugLogging);
		return returnState;
	} // end of batchNotPartOfNewBlend()

	/*
	 * For the input batch record, return all Checkpoint activity records 
	 * post-fermentation that have non-zero volumes
	 */
	private boolean batchHasCheckpoints(WineMakerLog wmk)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.batchHasCheckpoints(wmk '%s') ", wmk.get_batchKey()), debugLogging);
		boolean returnState = false;
		
		ArrayList<WineMakerFerment> wmfSets = wmk.getWmkFerments();
		returnState = wmfSets.stream()
			.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()))
			.filter(wmf -> wmf.get_stageCycle() > 1)
			.filter(wmf -> wmf.get_currentStageJuiceVol() > 0)
			.collect(Collectors.toList()).size() > 0;
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.batchHasCheckpoints(wmk '%s'): return state = %b ", wmk.get_batchKey(), returnState), debugLogging);
		return returnState;
	} // end of batchHasCheckpoints()
	
	/*
	 * Generate the selected date for the batch key
	 */
	private String getBatchDate() 
	{
		if (batchDate.getValue() == null)
		{
			batchDate.setValue(LocalDate.of(1900, 1, 1));
			winemakerLogger.displayAlert("Task date not selected, default date of 1/1/1900 was set");
			return LocalDate.of(1900, 1, 1).format(DateTimeFormatter.ofPattern("YYMMdd"));
		}
		else
			return batchDate.getValue().format(DateTimeFormatter.ofPattern("YYMMdd"));
	} // end of getBatchDate()

	/**
	 * Action method for the blend Add button. The batches that make up the blend
	 * can be entered here or selected from existing varietal batches. New
	 * WineMakerLog objects or existing ones will be saved in a list for processing
	 * by the Submit method.
	 * 
	 * For an existing varietal batch, a new version is created for the blend. The
	 * existing batch record is updated with values reflecting the portion removed
	 * for the blend (it could be 100%).
	 */
	public void addBlendButton() 
	{
		winemakerLogger.writeLog(">> BatchDetailController.addBlendButton() ", debugLogging);
	
		boolean validated = validateInput();
		if (!validated)
		{
			winemakerLogger.writeLog("<< BatchDetailController.addBlendButton(): validation error", debugLogging);
			return;
		}
	
		String parentBatchID = generateNewBatchKey(grapeSelect.getValue());

		if (this.blendSetting.equals(Blend.JUICEBLEND))
		{
			Optional<WineMakerLog> oldBatch = getExistingBatchesSet()
					.stream()
					.filter(checkBatch -> checkBatch.get_batchKey().equals(HelperFunctions.batchKeyCompress(grapeSelectBlend.getValue())))
					.findAny();		
			if (oldBatch.isPresent())
			{
				addExistingBatchToBlend(parentBatchID, oldBatch.get());
				setSelectedBlendBatch(oldBatch.get());
				loadBlendBatchSets();
				statusUpdates.appendText(String.format("Batch '%s' added to blend%n", HelperFunctions.batchKeyExpand(oldBatch.get())));
			}
			else
			{
				winemakerLogger.writeLog(String.format("   BatchDetailController.addBlendButton(): matching batch '%s' not found", oldBatch.get()), debugLogging);
			}
		}

		if (this.blendSetting.equals(Blend.FIELDBLEND))
		{
			WineMakerLog wmkLog = createBlendComponentBatch();
			Optional<WineMakerLog> wmkExtract = this.wmkSets
					.stream()
					.filter(wmkRecord -> wmkRecord.get_batchKey().equals(wmkLog.get_batchKey()))
					.findFirst();
			if (wmkExtract.isPresent()) 
			{
				winemakerLogger.displayAlert(String.format("Blend batch '%s' already exists ", HelperFunctions.batchKeyExpand(wmkLog)));	
				winemakerLogger.writeLog("<< BatchDetailController.addBlendButton()", debugLogging);
				return;
			}
	
			wmkLog.set_batchBlendKey(parentBatchID);
			this.createBatch.add(wmkLog);

			initFieldBlendUI();
			
			statusUpdates.appendText(String.format("Batch '%s' added to blend %s%n", HelperFunctions.batchKeyExpand(wmkLog.get_batchKey()), HelperFunctions.batchKeyExpand(parentBatchID)));
			winemakerLogger.writeLog(String.format("   BatchDetailController.addBlendButton(): updated record object: '%s'", wmkLog),debugLogging);
		} 

		grapeSelect.setEditable(false);

		winemakerLogger.writeLog("<< BatchDetailController.addBlendButton()", debugLogging);
	} // end of addBlendButton()

	/**
	 * Update the UI fields to reflect the calculated contents of blend to populate the parent batch
	 * 
	 * If this is a field blend the fields can remain as is.
	 * If this is a juice blend, set the parent to be a juice batch with volumes, not unit counts....
	 */
	public void doneBuildingBlend() 
	{
		winemakerLogger.writeLog(">> BatchDetailController.doneBuildingBlend() ", debugLogging);

		if (this.blendSetting.equals(Blend.JUICEBLEND))
		{
			updateJuiceBlendUI();			
		}

		/*
		 * Done adding batches, now create the parent record
		 */
		WineMakerLog wmkLog = new WineMakerLog(winemakerModel);

		wmkLog.set_batchKey(generateNewBatchKey(grapeSelect.getValue()));	
		wmkLog.set_batchGrape(HelperFunctions.getCodeValueEntry(FamilyCode.BLENDFAMILY.getValue(), grapeSelect.getValue()));

		HashMap<String, Double> summedDoubleValues = totalParentDoubleValues(getBatchCreateSet());
		HashMap<String, Integer> summedIntegerValues = totalParentIntegerValues(getBatchCreateSet());

		if (this.blendSetting.equals(Blend.JUICEBLEND))
		{
			juiceBlendNewRecords(wmkLog, summedIntegerValues);

			unitCountLabel.setText("Total for Blend");
			itemCount.setText(Integer.toString(summedIntegerValues.get("totalUnits")) + " " + extractPattern(itemCount.getText(), 2));
			vendorNotes.setText("");
		}

		if (this.blendSetting.equals(Blend.FIELDBLEND))
		{
			averageParentBatchValues(summedDoubleValues, summedIntegerValues);

			itemCount.setText(Integer.toString(summedIntegerValues.get("totalCount")));
			itemPrice.setText(Double.toString(summedDoubleValues.get("avgCost")));
			itemUnits.setText(Integer.toString(summedIntegerValues.get("avgUnits")) + " " + createBatch.get(0).get_sourceScale());
			vendorNotes.setText("");

			fieldBlendNewRecords(wmkLog, summedDoubleValues, summedIntegerValues);
		}

		winemakerLogger.writeLog(String.format("   BatchDetailController.doneBuildingBlend(), %s", wmkLog), debugLogging);

		unitPriceLabel.setText("Average Price");
		addBlendGrape.setVisible(false);
		doneBlendMix.setVisible(false);
		submitBatch.setVisible(true);

		statusUpdates.appendText("All batches collected for blend set\n");

		winemakerLogger.writeLog("<< BatchDetailController.doneBuildingBlend()", debugLogging);
	} // end of doneBuildingBlend()

	/*
	 * Input parms:
	 * 		parentBatchID - record key of the parent blend batch 
	 * 		existingBatchRecord - retrieved record of the selected existing batch
	 *	Tasks:
	 *		1 - Create a copy of the input batch record
	 *		2 - Set new parent batch id in the record copy
	 * 		3 - Set specified juice volume for the new batch 
	 * 		4 - Retrieve the old batch's last Checkpoint
	 * 		5 - Create new Checkpoint for the old batch to reflect its reduced juice volume
	 */
	private void addExistingBatchToBlend(String parentBatchID, WineMakerLog existingBatchRecord) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.addExistingBatchToBlend(parentBatchID %s, existingBatchRecord %s)", parentBatchID, existingBatchRecord.get_batchKey()), debugLogging);

		WineMakerFerment wmfUpdate = null;
		
		int setJuiceVolume = Integer.parseInt(extractPattern(itemCount.getText(), 1));
		int newBatchVolume = Integer.parseInt(this.saveLastBatchVol.get(existingBatchRecord.get_batchKey()));
		newBatchVolume -= setJuiceVolume;
		this.saveLastBatchVol.put(existingBatchRecord.get_batchKey(), Integer.toString(newBatchVolume));
		
		/*
		 * Create new Checkpoint for old batch to reflect volume adjustment
		 */
		wmfUpdate = getLastCheckpoint();
		
		winemakerLogger.writeLog(String.format("   BatchDetailController.addExistingBatchToBlend('%s', '%s'): old vol = %d, UI vol = %d", parentBatchID, existingBatchRecord.get_batchKey(), wmfUpdate.get_currentStageJuiceVol(), setJuiceVolume), debugLogging);
		
		wmfUpdate.set_batchKey(existingBatchRecord.get_batchKey());
		wmfUpdate.set_entry_date(Timestamp.valueOf(LocalDateTime.now()));
		wmfUpdate.set_outputJuiceVol(0);
		wmfUpdate.set_currentTemp(0);
		wmfUpdate.set_currBrix(0);
		wmfUpdate.set_currpH(0);
		wmfUpdate.set_currTA(0);
		wmfUpdate.set_currentStageJuiceVol(wmfUpdate.get_currentStageJuiceVol() - setJuiceVolume);
		wmfUpdate.set_currentStageJuiceScale(extractPattern(itemCount.getText(), 2));
		wmfUpdate.set_fermentNotes("New juice volume set due to extraction for blend " + HelperFunctions.batchKeyExpand(parentBatchID));
		winemakerLogger.writeLog(String.format("   BatchDetailController.addExistingBatchToBlend(), built update record%n%s", wmfUpdate), debugLogging);

		/*
		 * Create new batch record, changing the source to Juice and setting the blend parent key
		 */
		winemakerLogger.writeLog(String.format("   BatchDetailController.addExistingBatchToBlend(): Copy existing batch record"), debugLogging);
		WineMakerLog newBatch = existingBatchRecord.newCopy();
		newBatch.set_batchKey(getBatchDate() + newBatch.get_batchGrape());
		newBatch.set_batchBlendKey(parentBatchID);
		newBatch.set_batchSource(BatchSource.JUICESOURCE.getValue());
		newBatch.set_sourceScale(extractPattern(itemCount.getText(), 2));
		newBatch.set_sourceItemCount(1);
		newBatch.set_sourceItemMeasure(setJuiceVolume);
		newBatch.set_sourceItemPrice(0);
		newBatch.set_sourceVendorNotes(String.format("Batch created as part of blend %s", parentBatchID));
		winemakerLogger.writeLog(String.format("   BatchDetailController.addExistingBatchToBlend(), built new batch record%n%s", newBatch), debugLogging);

		this.createBatch.add(newBatch);
		this.createFerment.add(wmfUpdate);

		winemakerLogger.writeLog(String.format("   BatchDetailController.addExistingBatchToBlend(), new Batch & Checkpoint:%n%s%n%s", newBatch, wmfUpdate), debugLogging);
		winemakerLogger.writeLog(String.format("<< BatchDetailController.addExistingBatchToBlend()"), debugLogging);
	} // end of addExistingBatchToBlend(String parentBatchID, WineMakerLog existingBatch)

	/*
	 * Retrieve the current batch record and then extract its most current Checkpoint activity record
	 */
	private WineMakerFerment getLastCheckpoint()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.getLastCheckpoint()"), debugLogging);
	
		Optional<WineMakerLog> wmkExtract = this.wmkSets
				.stream()
				.filter(wmkTest -> wmkTest.get_batchKey().equals(HelperFunctions.batchKeyCompress(grapeSelectBlend.getValue())))
				.findFirst();
	
		WineMakerLog wmkLog = wmkExtract.get();
		ArrayList<WineMakerFerment> wmfSets = wmkLog.getWmkFerments();
		
		List<WineMakerFerment> checkPointList = wmfSets
				.stream()
				.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()))
				.collect(Collectors.toList());
			
		winemakerLogger.writeLog(String.format("<< BatchDetailController.getLastCheckpoint()"), debugLogging);
		return checkPointList.get(checkPointList.size() - 1).newCopy();
	} // end of getLastCheckpoint()

	private void averageParentBatchValues(HashMap<String, Double> summedDoubleValues, HashMap<String, Integer> summedIntegerValues)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.averageParentBatchValues(%s, %s)", summedDoubleValues, summedIntegerValues), debugLogging);

		DecimalFormat df = new DecimalFormat("###.##");
		int avgUnits = 0;
		double avgCost = 0;
		
		try 
		{
			avgUnits = summedIntegerValues.get("totalUnits") / summedIntegerValues.get("totalCount");
			avgCost = Double.parseDouble(df.format(summedDoubleValues.get("totalCost") / summedIntegerValues.get("totalCount")));
		} 
		catch (ArithmeticException em) 
		{
			winemakerLogger.displayAlert(em.getMessage());
			winemakerLogger.writeLog(String.format("<< BatchDetailController.averageParentBatchValues()"), debugLogging);
			return;
		}
		catch (NullPointerException en)
		{
			winemakerLogger.displayAlert(en.getMessage());
			winemakerLogger.writeLog(String.format("<< BatchDetailController.averageParentBatchValues()"), debugLogging);
			return;
		}
		summedIntegerValues.put("avgUnits", avgUnits);
		summedDoubleValues.put("avgCost", avgCost);
		
		winemakerLogger.writeLog(String.format("   BatchDetailController.averageParentBatchValues():%n%s%n%s", summedDoubleValues, summedIntegerValues), debugLogging);
		winemakerLogger.writeLog(String.format("<< BatchDetailController.averageParentBatchValues()"), debugLogging);
	} // end of averageParentBatchValues()

	private HashMap<String, Double> totalParentDoubleValues(ArrayList<WineMakerLog> pendingBatch)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.totalParentDoubleValues(%s)", pendingBatch), debugLogging);

		HashMap<String, Double> summedDoubleValues = new HashMap<>();
		
		double totalCost = pendingBatch
				.stream()
				.map(wmk -> wmk.get_sourceItemPrice() * wmk.get_sourceItemCount())
				.reduce(0.0,Double::sum);

		summedDoubleValues.put("totalCost", Double.valueOf(totalCost));
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.totalParentDoubleValues(): %n\t%s", summedDoubleValues), debugLogging);
		return summedDoubleValues;
	} // end of totalParentDoubleValues()
	
	private HashMap<String, Integer> totalParentIntegerValues(ArrayList<WineMakerLog> pendingBatch)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.totalParentIntegerValues(%s)", pendingBatch), debugLogging);

		HashMap<String, Integer> summedIntegerValues = new HashMap<>();
		
		int totalCount = pendingBatch
				.stream()
				.map(wmk -> wmk.get_sourceItemCount())
				.reduce(0, Integer::sum);

		int	totalUnits = pendingBatch
				.stream()
				.map(wmk -> wmk.get_sourceItemMeasure() * wmk.get_sourceItemCount())
				.reduce(0,Integer::sum);

		summedIntegerValues.put("totalCount", totalCount);
		summedIntegerValues.put("totalUnits", totalUnits);

		winemakerLogger.writeLog(String.format("<< BatchDetailController.totalParentIntegerValues(): %n\t%s", summedIntegerValues), debugLogging);
		return summedIntegerValues;
	} // end of totalParentIntegerValues()
	
	/*
	 * For blend batches, create default Stage and Checkpoint records
	 */
	private void fieldBlendNewRecords(WineMakerLog wmkLog, HashMap<String, Double> summedDoubleValues, HashMap<String, Integer> summedIntegerValues)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.fieldBlendNewRecords(): Input: %n\t%s%n\t%s%n\t%s", wmkLog.get_batchKey(), summedDoubleValues, summedIntegerValues), debugLogging);
		
		vendorNotes.setText("");
		
		wmkLog.set_sourceItemCount(summedIntegerValues.get("totalCount"));
		wmkLog.set_sourceItemPrice(summedDoubleValues.get("avgCost"));
		wmkLog.set_sourceItemMeasure(summedIntegerValues.get("avgUnits"));

		wmkLog.set_batchVineyard(HelperFunctions.getCodeValueEntry(FamilyCode.VINEYARDFAMILY.getValue(), vineyardSelect.getValue()));
		wmkLog.set_batchSource(HelperFunctions.getCodeValueEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), batchInputSelect.getValue()));
		wmkLog.set_sourceScale(extractPattern(itemUnits.getText(), 2));
		createBatch.add(wmkLog);
		
		int stageCycle = (wmkLog.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue())) ? 0 : 1;
		WineMakerFerment wmfStage = createNewStage(wmkLog, stageCycle, summedIntegerValues.get("totalUnits"));
		createFerment.add(wmfStage);

		WineMakerFerment wmfCheckpoint = createNewCheckpoint(wmkLog, wmfStage, summedIntegerValues.get("totalUnits"));
		createFerment.add(wmfCheckpoint);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.fieldBlendNewRecords()"), debugLogging);
	} // end of fieldBlendNewRecords()

	/*
	 * For blend batches, create default Stage and Checkpoint records
	 */
	private void juiceBlendNewRecords(WineMakerLog wmkLog, HashMap<String, Integer> summedIntegerValues)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.juiceBlendNewRecords(), Input: %n\t%s%n\t%s", wmkLog.get_batchKey(), summedIntegerValues), debugLogging);

		wmkLog.set_sourceItemCount(1);
		wmkLog.set_sourceItemPrice(0);
		wmkLog.set_sourceItemMeasure(summedIntegerValues.get("totalUnits"));

		wmkLog.set_batchVineyard("redmisc");
		wmkLog.set_batchSource(BatchSource.JUICESOURCE.getValue());
		wmkLog.set_sourceScale(extractPattern(itemCount.getText(), 2));
		createBatch.add(wmkLog);

		WineMakerFerment wmfStage = createNewStage(wmkLog, 2, summedIntegerValues.get("totalUnits"));

		createFerment.add(wmfStage);

		WineMakerFerment wmfCheckpoint = createNewCheckpoint(wmkLog, wmfStage, summedIntegerValues.get("totalUnits"));
		createFerment.add(wmfCheckpoint);		

		winemakerLogger.writeLog(String.format("   BatchDetailController.juiceBlendNewRecords(), %nnew Batch:%s%nStage:%s%nCheckpoint:%s", wmkLog, wmfStage, wmfCheckpoint), debugLogging);
		winemakerLogger.writeLog(String.format("<< BatchDetailController.juiceBlendNewRecords()"), debugLogging);
	} // end of juiceBlendNewRecords()

	private WineMakerFerment createNewStage(WineMakerLog wmkLog, int cycleNumber, Integer totalUnits)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.createNewStage('%s', %d, %d)", wmkLog.get_batchKey(), cycleNumber, totalUnits), debugLogging);
		
		Timestamp entryDate = createTimeStamp();
		
		WineMakerFerment wmfStage = new WineMakerFerment(this.winemakerModel);
		wmfStage.set_batchKey(wmkLog.get_batchKey());
		wmfStage.set_entry_date(entryDate);
		wmfStage.set_fermentActivity(ActivityName.FERMENT.getValue());
		wmfStage.set_stageCycle(cycleNumber);
		wmfStage.set_startDate(entryDate);
		if (wmkLog.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue()))
			wmfStage.set_inputGrapeAmt(totalUnits);
		else
			wmfStage.set_outputJuiceVol(totalUnits);
		
		wmfStage.set_outputJuiceScale(wmkLog.get_sourceScale());
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.createNewStage()"), debugLogging);
		return wmfStage;
	} // end of createNewStage()

	private WineMakerFerment createNewCheckpoint(WineMakerLog wmkLog, WineMakerFerment wmfStage, Integer totalUnits)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.createNewCheckpoint('%s', %d, %d)", wmkLog.get_batchKey(), wmfStage.get_stageCycle(), totalUnits), debugLogging);

		Timestamp entryDate = createTimeStamp();
		
		Timestamp adjustedTime = Timestamp.from(entryDate.toInstant().plusSeconds(2));
		adjustedTime.setNanos(0);
		
		WineMakerFerment wmfCheckpoint = new WineMakerFerment(this.winemakerModel);
		wmfCheckpoint.set_batchKey(wmkLog.get_batchKey());
		wmfCheckpoint.set_entry_date(adjustedTime);
		wmfCheckpoint.set_fermentActivity(ActivityName.CHECKPOINT.getValue());
		wmfCheckpoint.set_stageCycle(wmfStage.get_stageCycle());
		wmfCheckpoint.set_currentStageJuiceVol(totalUnits);
		wmfCheckpoint.set_currentStageJuiceScale(wmkLog.get_sourceScale());
		wmfCheckpoint.set_currentTemp(0);
		wmfCheckpoint.set_currBrix(0);
		wmfCheckpoint.set_currpH(0);
		wmfCheckpoint.set_currTA(0);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.createNewCheckpoint(): New Checkpoint %n%s", wmfCheckpoint), debugLogging);
		return wmfCheckpoint;
	} // end of createNewCheckpoint()
	
	/*
	 * Generate new batch key
	 * If grape code not found in grapes list, it must be a blend name
	 */
	private String generateNewBatchKey(String grapeSelection) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.generateNewBatchKey('%s') ", grapeSelection), debugLogging);

		String grapeCode = (HelperFunctions.getCodeValueEntry(FamilyCode.GRAPEFAMILY.getValue(), grapeSelection) == null) ? 
				HelperFunctions.getCodeValueEntry(FamilyCode.BLENDFAMILY.getValue(), grapeSelection) : 
				HelperFunctions.getCodeValueEntry(FamilyCode.GRAPEFAMILY.getValue(), grapeSelection);

		String batchKey = String.format("%s%s", getBatchDate(), grapeCode);
		winemakerLogger.writeLog(String.format("<< BatchDetailController.generateNewBatchKey(): New batch key = %s", batchKey), debugLogging);
		
		return batchKey;
	} // end of generateNewBatchKey()

	/**
	 * Validate the UI contents, specific validation tests depending on the type of batch:
	 * 		1. Straight varietal batch calls default validation from this method
	 * 		2. Field blend calls no validation methods as they are performed by the ADD and DONE button handler methods
	 * 		3. Juice blend calls additional validation and updates the pending Ferment record
	 */
	public void submitBatch() 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.submitBatch(): Blend setting = %s", this.blendSetting.toString()), debugLogging);
	
		if (this.blendSetting.equals(Blend.NOTBLEND))
		{
			winemakerLogger.writeLog(String.format("   BatchDetailController.submitBatch() validating varietal batch"), debugLogging);
	
			boolean isValidated = validateInput();
			
			if (!isValidated)
			{
				statusUpdates.setText("Batch not submitted, validation errors");
				return;
			}
			
			createVarietalBatch();
		}
		
		if (this.blendSetting.equals(Blend.JUICEBLEND))
		{
			/*
			 * Extract new Ferment record and update final values
			 */
			Optional<WineMakerFerment> wmfExtract = this.createFerment
					.stream()
					.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.FERMENT.getValue()))
					.findFirst();
			WineMakerFerment wmfFerment = wmfExtract.get();
			wmfFerment.set_outputJuiceScale(extractPattern(itemCount.getText(), 2));
			winemakerLogger.writeLog(String.format("   BatchDetailController.submitBatch(): Extracted record:%n%s", wmfFerment), debugLogging);

			Optional<WineMakerFerment> wmfExtract2 = this.createFerment
					.stream()
					.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()))
					.findFirst();
			WineMakerFerment wmfCheck = wmfExtract2.get();
			wmfCheck.set_currentStageJuiceScale(extractPattern(itemCount.getText(), 2));			

			winemakerLogger.writeLog(String.format("   BatchDetailController.submitBatch(): Extracted record:%n%s", wmfCheck), debugLogging);
		}
	
		/*
		 * Process the generated record objects
		 */
		updateBatch	
			.stream()
			.forEach(wmk -> winemakerModel.updateBatch(wmk));
	
		createBatch
			.stream()
			.forEach(wmk -> winemakerModel.insertBatch(wmk));
	
		createFerment
			.stream()
			.forEach(wmf -> winemakerModel.insertFermentData(wmf));
		
		updateInventoryActivity
			.stream()
			.forEach(wmi -> winemakerModel.updateInventoryBatch(wmi));
		
		insertInventoryActivity
			.stream()
			.forEach(wmi -> winemakerModel.insertInventory(wmi));
		
		Optional<WineMakerLog> newBatch = createBatch
				.stream()
				.filter(wmk -> wmk.get_batchBlendKey().length() == 0)
				.findFirst();
		
		batchTitle.setText("For batch " + HelperFunctions.batchKeyExpand(newBatch.get().get_batchKey()));
		
		grapeSelect.setEditable(true);
		createBatch.clear();
		updateBatch.clear();
		createFerment.clear();
		updateInventoryActivity.clear();
		insertInventoryActivity.clear();
	
		statusUpdates.setText(String.format("Batch '%s' successfully saved", HelperFunctions.batchKeyExpand(newBatch.get().get_batchKey())));
		
		winemakerLogger.writeLog("<< BatchDetailController.submitBatch()", debugLogging);
	} // end of submitBatch()	

	/*
	 * Call the batch validation routine for this batch type
	 */
	private boolean validateInput() 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateInput(): Blend setting = '%s'", this.blendSetting.toString()), debugLogging);
	
		boolean isValidated = false;
	
		Method validateMethod;
		try
		{
			validateMethod = BatchDetailController.class.getDeclaredMethod(validationMethods.get(this.blendSetting.toString()));
			isValidated = (boolean) validateMethod.invoke(this);
		}
		catch (NoSuchMethodException | SecurityException | NullPointerException e) 
		{
			statusUpdates.setText("Activity not yet implemented");
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
		{
			statusUpdates.setText("Programming error, contact the vendor");
			winemakerLogger.showIOException(e, "Batch processing, failed to load validation method");
		}
		
		if (!isValidated)
			statusUpdates.appendText(String.format("Errors validating input"));
	
		winemakerLogger.writeLog(String.format("<< BatchDetailController.validateInput()"), debugLogging);
		return isValidated;
	} // end of validateInput()

	/*
	 * Build the new batch record and add it to the queue
	 */
	private void createVarietalBatch()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.createVarietalBatch()"), debugLogging);

		WineMakerLog wmkLog = new WineMakerLog(winemakerModel);

		String batchID = (updateMode) ? this.updateWmkLog.get_batchKey() : generateNewBatchKey(grapeSelect.getValue());		

		wmkLog.set_batchKey(batchID);
		wmkLog.set_batchSource(HelperFunctions.getCodeValueEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), batchInputSelect.getValue()));
		wmkLog.set_batchGrape(HelperFunctions.getCodeValueEntry(FamilyCode.GRAPEFAMILY.getValue(), grapeSelect.getValue()));
		wmkLog.set_sourceItemCount(Integer.parseInt(itemCount.getText()));
		wmkLog.set_sourceItemPrice(Double.parseDouble(itemPrice.getText()));
		wmkLog.set_sourceItemMeasure(Integer.parseInt(extractPattern(itemUnits.getText(), 1)));
		wmkLog.set_sourceScale(extractPattern(itemUnits.getText(), 2).toLowerCase());
		wmkLog.set_batchVineyard(HelperFunctions.getCodeValueEntry(FamilyCode.VINEYARDFAMILY.getValue(), vineyardSelect.getValue()));
		wmkLog.set_sourceVendor(HelperFunctions.getCodeValueEntry(FamilyCode.GRAPESUPPLYFAMILY.getValue(), vendorSelect.getValue()));
		wmkLog.set_sourceVendorNotes(vendorNotes.getText());

		if (this.updateMode)
			this.updateBatch.add(wmkLog);
		else
			this.createBatch.add(wmkLog);
		
		int stageCycle = (wmkLog.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue())) ? 0 : 1;
		WineMakerFerment wmfStage = createNewStage(wmkLog, stageCycle, Integer.parseInt(itemCount.getText()) * Integer.parseInt(extractPattern(itemUnits.getText(), 1)));
		createFerment.add(wmfStage);

		WineMakerFerment wmfCheckpoint = createNewCheckpoint(wmkLog, wmfStage, Integer.parseInt(itemCount.getText()) * Integer.parseInt(extractPattern(itemUnits.getText(), 1)));
		createFerment.add(wmfCheckpoint);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.createVarietalBatch(): %s", wmkLog), debugLogging);
		return;
	} // end of createVarietalBatch()

	/*
	 * Check if the new candidate batch already exists
	 */
	private String checkForExisting(ComboBox<String> grapeSelection)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.checkForExisting(%s)", grapeSelection.getValue()), debugLogging);

		String returnMsg = "";

		if (grapeSelection.getValue() == null)
			return returnMsg;
		
		String batchID = generateNewBatchKey(grapeSelection.getValue());		
		Optional<WineMakerLog> oldBatch = wmkSets
				.stream()
				.filter(checkBatch -> checkBatch.get_batchKey().equals(batchID))
				.findAny();		
		if (oldBatch.isPresent())
			returnMsg = String.format("Duplicate batch: %s", HelperFunctions.batchKeyExpand(batchID));						

		winemakerLogger.writeLog(String.format("<< BatchDetailController.checkForExisting()"), debugLogging);
		return returnMsg;
	} // end of checkForExisting()
	
	/*
	 * Validate UI for a straight varietal batch
	 * Fields:
	 * 		batchDate
	 * 		batchInputSelect
	 * 		grapeSelect
	 * 		vineyardSelect
	 * 		itemCount
	 * 		itemPrice
	 * 		itemUnits
	 * 		vendorSelect
	 * 		vendorNotes
	 */
	@SuppressWarnings("unused")
	private boolean validateVarietalBatch()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateVarietalBatch()"), debugLogging);

		boolean returnState = true;
		String validationMessage = "";
		
		validationMessage += (batchDate.getValue() != null && batchDate.getValue().getYear() == 1900) ? 
				String.format("Invalid date string was converted to 1/1/1900 from %s\n", datePickerOriginal) : "";
		if ((batchDate.getValue() == null))
		{
			validationMessage += "Task date not selected, default date of 1/1/1900 was set";
			batchDate.setValue(LocalDate.of(1900, 1, 1));			
		}
		validationMessage += (grapeSelect.getValue() == null) ? 
				"Select the batch grape\n" : "";
		validationMessage += (batchInputSelect.getValue() == null || batchInputSelect.getValue().contains("Select")) ? 
				"Select source type, grapes or juice\n" : "";
		validationMessage += (vineyardSelect.getValue() == null) ? 
				"Select the sourcing vineyard\n" : "";
		validationMessage += (!HelperFunctions.validateInteger(itemCount.getText())) ? 
				"Item Count is not a valid number\n" : "";
		validationMessage += (!HelperFunctions.validateDouble(itemPrice.getText())) ? 
				"Item Price is not a valid number\n" : "";
		validationMessage += validateVolumeField(unitPerItemLabel.getText(), itemUnits.getText(), batchInputSelect.getValue());
		validationMessage += (vendorSelect.getValue() == null || vendorSelect.getValue().contains("Select")) ? 
				"Select a vendor\n" : "";
		validationMessage += checkForExisting(grapeSelect);						
		
		if (validationMessage.length() > 0)
		{
			winemakerLogger.displayAlert(validationMessage);
			returnState = false;
		}

		winemakerLogger.writeLog(String.format("<< BatchDetailController.validateVarietalBatch()"), debugLogging);
		return returnState;
	} // end of validateVarietalBatch()

	/*
	 * Build a new batch record
	 */
	private WineMakerLog createBlendComponentBatch()
	{		
		winemakerLogger.writeLog(String.format(">> BatchDetailController.createBlendComponentBatch()"), debugLogging);
		
		String batchID = generateNewBatchKey(grapeSelectBlend.getValue());
		winemakerLogger.writeLog(String.format("   BatchDetailController.createBlendComponentBatch(): batchID '%s'", batchID), debugLogging);

		WineMakerLog wmkLog = new WineMakerLog(winemakerModel);
		
		wmkLog.set_batchKey(batchID);
		wmkLog.set_batchSource(HelperFunctions.getCodeValueEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), batchInputSelect.getValue()));
		wmkLog.set_batchGrape(HelperFunctions.getCodeValueEntry(FamilyCode.GRAPEFAMILY.getValue(), grapeSelectBlend.getValue()));
		wmkLog.set_batchVineyard(HelperFunctions.getCodeValueEntry(FamilyCode.VINEYARDFAMILY.getValue(), vineyardSelect.getValue()));
		wmkLog.set_sourceItemCount((this.blendSetting.equals(Blend.FIELDBLEND)) ? 
				Integer.parseInt(itemCount.getText()) : 1);
		wmkLog.set_sourceItemPrice((this.blendSetting.equals(Blend.FIELDBLEND)) ? 
				Double.parseDouble(itemPrice.getText()) : 0);
		wmkLog.set_sourceItemMeasure(Integer.parseInt(extractPattern(itemUnits.getText(), 1)));
		wmkLog.set_sourceScale(extractPattern(itemUnits.getText(), 2));
		wmkLog.set_sourceVendor(HelperFunctions.getCodeValueEntry(FamilyCode.GRAPESUPPLYFAMILY.getValue(), vendorSelect.getValue()));
		wmkLog.set_sourceVendorNotes(vendorNotes.getText());
				
		winemakerLogger.writeLog(String.format("<< BatchDetailController.createBlendComponentBatch()"), debugLogging);
		return wmkLog;
	} // end of createBlendComponentBatch()

	/*
	 * Validate UI for a field blend batch
	 * Fields:
	 * 		batchDate = Batch date
	 * 		grapeSelect = Blend type
	 * 		grapeSelectBlend = Component grape selection
	 * 		vineyardSelect = Vineyard selection
	 * 		itemCount = Item count
	 * 		itemPrice = Item price
	 * 		itemUnits = Units per item
	 * 		vendorSelect = Vendor name
	 * 		vendorNotes = Batch notes
	 */
	@SuppressWarnings("unused")
	private boolean validateFieldBlend()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateFieldBlend() blend mode %s", this.blendSetting), debugLogging);

		String validationMessage = "";
		boolean returnState = true;
		
		validationMessage += (batchDate.getValue() != null && batchDate.getValue().getYear() == 1900) ? 
				String.format("Invalid date string was converted to 1/1/1900 from %s\n", datePickerOriginal) : "";
		if ((batchDate.getValue() == null))
		{
			validationMessage += "Task date not selected, default date of 1/1/1900 was set";
			batchDate.setValue(LocalDate.of(1900, 1, 1));			
		}
		validationMessage += (batchInputSelect.getValue() == null || batchInputSelect.getValue().contains("Select")) ? 
				"Select batch input, grapes or juice\n" : "";
		validationMessage += (grapeSelect.getValue() == null) ? 
				"A blend type selection must be made\n" : "";
		validationMessage += (grapeSelectBlend.getValue() == null) ? 
				"A grape selection must be made\n" : "";
		validationMessage += (vineyardSelect.getValue() == null) ? 
				"A vineyard selection must be made\n" : "";
		validationMessage += (!HelperFunctions.validateInteger(itemCount.getText())) ? 
				"Item Count is not valid\n" : "";
		validationMessage += (!HelperFunctions.validateDouble(itemPrice.getText())) ? 
				"Item Price is not valid\n" : "";
		validationMessage += validateVolumeField("Volume per Item", itemUnits.getText(), batchInputSelect.getValue());
		validationMessage += (vendorSelect.getValue() == null || vendorSelect.getValue().contains("Select")) ? 
				"Select a vendor\n" : "";
		validationMessage += checkForExisting(grapeSelectBlend);

		if (validationMessage.length() > 0)
		{
			winemakerLogger.displayAlert(validationMessage);
			returnState = false;;
		}
		
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateFieldBlend() blend mode %s", this.blendSetting), debugLogging);
		return returnState;
	} // end of validateFieldBlend()

	
	/*
	 * Validate UI for Juice blend component batch
	 * 
	 * Initial checks are for null fields and invalid integers.
	 * The selected stage will reference the related Checkpoint records, as the 
	 * 		UI juice volume will be compared against the current batch's available juice volume.
	 */
	@SuppressWarnings("unused")
	private boolean validateJuiceBlend()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateJuiceBlend()"), debugLogging);

		WineMakerLog wmkLog = null;
		boolean returnState = true;
		String validationMessage = "";

		validationMessage += (grapeSelectBlend.getValue() == null) ? "An existing batch selection must be made" : "";

		if (grapeSelectBlend.getValue() != null)
		{		
			Optional<WineMakerLog> wmkExtract = getExistingBatchesSet()
				.stream()
				.filter(wmkTest -> wmkTest.get_batchKey().equals(HelperFunctions.batchKeyCompress(grapeSelectBlend.getValue())))
				.findFirst();
			wmkLog = wmkExtract.get();
			
			batchInputSelect.setValue(HelperFunctions.getCodeKeyEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), wmkLog.get_batchSource()));
		}
		
		validationMessage += validateVolumeField("Volume", itemCount.getText(), HelperFunctions.getCodeKeyEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), BatchSource.JUICESOURCE.getValue()));
		
		if (validationMessage.length() == 0)
		{
			ArrayList<Integer> checkpointVolumes = wmkLog.getWmkFerments()
			.stream()
			.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()) && wmf.get_currentStageJuiceVol() > 0)
			.map(wmf -> Integer.valueOf(wmf.get_currentStageJuiceVol()))
			.collect(Collectors.toCollection(ArrayList::new));
			
			if (checkpointVolumes.get((checkpointVolumes.size() - 1)) < Integer.parseInt(extractPattern(itemCount.getText(), 1)))
			{
				validationMessage += String.format("The existing batch doesn't have that much available: %d < %d", checkpointVolumes.get((checkpointVolumes.size() - 1)), Integer.parseInt(extractPattern(itemCount.getText(), 1))) ;
			}
		}
		
		if (validationMessage.length() > 0)
		{
			winemakerLogger.displayAlert(validationMessage);
			winemakerLogger.writeLog(String.format("   BatchDetailController.validateJuiceBlend() '%s'", validationMessage), debugLogging);
			return false;
		}

		winemakerLogger.writeLog(String.format("<< BatchDetailController.validateJuiceBlend()"), debugLogging);
		return returnState;
	} // end of validateJuiceBlend()
	
	/*
	 * Validate a weight|volume field, like "34.2 g" or "4 l"
	 */
	private String validateVolumeField(String fieldLabel, String fieldValue, String referenceScale)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.validateVolumeField('%s', '%s', '%s')", fieldLabel, fieldValue, referenceScale), debugLogging);
	
		Matcher matcher = regexAmtAndScalePattern.matcher(fieldValue);		
		ArrayList<String> volumeOrWeightOptions = new ArrayList<String>(2);
		
		if (referenceScale.equals(HelperFunctions.getCodeKeyEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), BatchSource.GRAPESOURCE.getValue())))
		{
			volumeOrWeightOptions.add(WeightsAndMeasures.USWEIGHT.getValue());
			volumeOrWeightOptions.add(WeightsAndMeasures.METRICWEIGHT.getValue());
		}
		else
		{
			volumeOrWeightOptions.add(WeightsAndMeasures.USVOLUME.getValue());
			volumeOrWeightOptions.add(WeightsAndMeasures.METRICVOLUME.getValue());		
		}

		String validateMessage = (!matcher.matches() || !intVerification(fieldLabel, matcher.group(1))) ? 
				String.format("Invalid number value '%s' for '%s'%n", fieldValue, fieldLabel) : "";	
		
		if (validateMessage.length() == 0)
		{
			Optional<String> checkMeasure = volumeOrWeightOptions
					.stream()
					.filter(value -> value.equalsIgnoreCase(matcher.group(2)))
					.findAny();
			
			validateMessage += (matcher.matches() && checkMeasure.isEmpty()) ? 
					String.format("Invalid measurement specification '%s' for '%s' (gal, lbs...?)%n", matcher.group(2), fieldLabel) : "";		
		}
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.validateVolumeField('%s', '%s', '%s'), %s", fieldLabel, fieldValue, referenceScale, validateMessage), debugLogging);
		return validateMessage;
	} // end of validateVolumeField()
	
	/*
	 * Validate integer input
	 */
	private boolean intVerification(String fieldName, String intValue) 
	{
		boolean rc = true;
		
		try 
		{
			Integer.parseInt(intValue);
		} 
		catch (NumberFormatException e) 
		{
			rc = false;
		}
	
		return rc;
	}
	
	/*
	 * Return to home Scene
	 */
	@FXML
	public void returnToMain(ActionEvent e) {
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

			if (batchSubmitted)
				winemakerController.loadBatchDisplay(null);

			window.show();
		} catch (IOException e1) 
		{
			winemakerLogger.displayAlert(e1.getMessage());
			return;
		}
	}

	/*
	 * Use the method map to dynamically call UI Build method associated with the current activity
	 */
	private void buildUIDirector(String activityCode)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.buildUIDirector(activityCode '%s'): call method '%s'", activityCode, buildUIMethods.get(activityCode)), debugLogging);
	
		Method uiBuildMethod;        
		try
		{
			uiBuildMethod = BatchDetailController.class.getDeclaredMethod(buildUIMethods.get(activityCode));
			uiBuildMethod.invoke(this);
		}
		catch (NoSuchMethodException | SecurityException | NullPointerException e) 
		{
			statusUpdates.setText("Activity not yet implemented");
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
		{
			e.printStackTrace();
		}
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.buildUIDirector(activityCode '%s'): call method '%s'", activityCode, buildUIMethods.get(activityCode)), debugLogging);
		return;
	} // end of buildUIDirector()

	/*
	 * Dynamically build UI for a straight varietal batch
	 * Fields:
	 * 		Batch date in batchDate
	 * 		Source buttons in sourceTG
	 * 		Grape selection in grapeSelect
	 * 		Vineyard selection in vineyardSelect
	 * 		Item count in itemCount
	 * 		Item price in itemPrice
	 * 		Units per item in itemUnits
	 * 		Measure system in measureTG
	 * 		Vendor name in vendorTG
	 * 		Batch notes in vendorNotes
	 */
	@SuppressWarnings("unused")
	private void buildVarietalBatchUI()
	{
		winemakerLogger.writeLog(">> BatchDetailController.buildVarietalBatchUI()", debugLogging);

		batchTypeHeader.setText("New Varietal Batch");
		batchSourceLabel = new Label("Batch Source");
		
		loadChoiceBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.BATCHSOURCEFAMILY.getValue()), batchInputSelect, "input type");
		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPEFAMILY.getValue()), grapeSelect);
		grapeSelect.setPromptText("Select a Grape");

		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.VINEYARDFAMILY.getValue()), vineyardSelect);
		vineyardSelect.setPromptText("Select the Vineyard");

		loadChoiceBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPESUPPLYFAMILY.getValue()), vendorSelect, "vendor");
		
		gp.add(batchSourceLabel, 0, 1);
		gp.add(batchInputSelect, 1, 1);
		
		gp.add(batchGrapeLabel, 0, 2);
		gp.add(grapeSelect, 1, 2);
		
		gp.add(batchVineyardLabel, 0, 3);
		gp.add(vineyardSelect, 1, 3);

		itemCount.clear();
		gp.add(unitCountLabel, 0, 4);
		gp.add(itemCount, 1, 4);

		itemPrice.clear();
		gp.add(unitPriceLabel, 0, 5);
		gp.add(itemPrice, 1, 5);

		itemUnits.clear();		
		gp.add(unitPerItemLabel, 0, 6);
		gp.add(itemUnits, 1, 6);
		
		gp.add(batchVendorLabel, 0, 7);
		gp.add(vendorSelect, 1, 7);

		gp.add(batchNotesLabel, 0, 9);
		gp.add(vendorNotes, 1, 9);

		addBlendGrape.setVisible(false);
		doneBlendMix.setVisible(false);
		
		winemakerLogger.writeLog("<< BatchDetailController.buildVarietalBatchUI()", debugLogging);
		return;
	} // end of buildVarietalBatchUI()
	
	/*
	 * Dynamically build UI for a field blend batch
	 * Fields:
	 * 		batchDate = Batch date
	 * 		grapeSelect = Blend type
	 * 		grapeSelectBlend = Component grape selection
	 * 		vineyardSelect = Vineyard selection
	 * 		itemCount = Item count
	 * 		itemPrice = Item price
	 * 		itemUnits = Units per item
	 * 		measureTG = Measure system
	 * 		vendorTG = Vendor name
	 * 		vendorNotes = Batch notes
	 */
	@SuppressWarnings("unused")
	private void buildBlendFieldUI()
	{
		winemakerLogger.writeLog(">> BatchDetailController.buildBlendFieldUI()", debugLogging);

		batchTypeHeader.setText("New Field Blend Batch");
		batchSourceLabel = new Label("Batch Source");

		loadChoiceBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.BATCHSOURCEFAMILY.getValue()), batchInputSelect, "Source");
		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.BLENDFAMILY.getValue()), grapeSelect);
		batchGrapeLabel.setText("Blend Style");
		grapeSelect.setPromptText("Select a Blend Style");		
		
		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPEFAMILY.getValue()), grapeSelectBlend);
		blendGrapeLabel.setText("Blend Grape");
		grapeSelectBlend.setPromptText("Select a Grape");
		
		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.VINEYARDFAMILY.getValue()), vineyardSelect);
		vineyardSelect.setPromptText("Select the Vineyard");

		loadChoiceBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPESUPPLYFAMILY.getValue()), vendorSelect, "vendor");
		
		gp.add(batchSourceLabel, 0, 1);
		gp.add(batchInputSelect, 1, 1);
		
		gp.add(batchGrapeLabel, 0, 2);
		gp.add(grapeSelect, 1, 2);

		gp.add(blendGrapeLabel, 0, 3);
		gp.add(grapeSelectBlend, 1, 3);

		gp.add(batchVineyardLabel, 0, 4);
		gp.add(vineyardSelect, 1, 4);

		itemCount.clear();		
		unitCountLabel.setText("Item Count");
		gp.add(unitCountLabel, 0, 5);
		gp.add(itemCount, 1, 5);

		itemPrice.clear();		
		gp.add(unitPriceLabel, 0, 6);
		gp.add(itemPrice, 1, 6);

		itemUnits.clear();		
		gp.add(unitPerItemLabel, 0, 7);
		gp.add(itemUnits, 1, 7);
				
		gp.add(batchVendorLabel, 0, 8);
		gp.add(vendorSelect, 1, 8);
		
		gp.add(batchNotesLabel, 0, 10);
		gp.add(vendorNotes, 1, 10);

		submitBatch.setVisible(false);
		addBlendGrape.setVisible(true);
		doneBlendMix.setVisible(true);

		winemakerLogger.writeLog("<< BatchDetailController.buildBlendFieldUI()", debugLogging);
		return;
	} // end of buildBlendFieldUI()
	
	/*
	 * Dynamically build UI for a juice blend batch
	 * Fields:
	 * 		Batch date in batchDate
	 * 		Blend grape selection in grapeSelect
	 * 		Component batch selection in grapeSelectBlend
	 * 		Item count in itemCount
	 * 		Measure system in measureTG
	 * 		Batch notes in vendorNotes
	 */
	@SuppressWarnings("unused")
	private void buildBlendJuiceUI()
	{
		winemakerLogger.writeLog(">> BatchDetailController.buildBlendJuiceUI()", debugLogging);

		batchTypeHeader.setText("New Juice Blend Batch");
		batchGrapeLabel.setText("Blend Style");
		grapeSelect.setPromptText("Select a Blend Style");
		blendGrapeLabel.setText("Blend Batch");

		loadComboBoxSelections(HelperFunctions.getCodeKeyFamily(FamilyCode.BLENDFAMILY.getValue()), grapeSelect);
		loadBlendBatchSets();
		
		itemCount.clear();
		
		unitCountLabel.setText("Added to Blend");

		gp.add(batchGrapeLabel, 0, 1);
		gp.add(grapeSelect, 1, 1);
		gp.add(blendGrapeLabel, 0, 2);
		gp.add(grapeSelectBlend, 1, 2);
		gp.add(unitCountLabel, 0, 3);
		gp.add(itemCount, 1, 3);
		gp.add(batchNotesLabel, 0, 8);
		gp.add(vendorNotes, 1, 8);

		submitBatch.setVisible(false);
		addBlendGrape.setVisible(true);
		doneBlendMix.setVisible(true);		
		
		winemakerLogger.writeLog("<< BatchDetailController.buildBlendJuiceUI()", debugLogging);
		return;
	} // end of buildBlendJuiceUI()

	/*
	 * Reset the UI after a grape selection in addBlendButton 
	 */
	private void initFieldBlendUI()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.initFieldBlendUI()"),debugLogging);
	
		grapeSelectBlend.setPromptText("Select a Blend Style");
		vineyardSelect.setPromptText("Select the Vineyard");
		
		itemCount.setText("");
		itemUnits.setText("");
		itemPrice.setText("");
		vendorNotes.setText("");
	
		winemakerLogger.writeLog(String.format("<< BatchDetailController.initFieldBlendUI()"),debugLogging);
	}

	/*
	 * Batch is defined, now add source and target containers
	 */
	private void updateJuiceBlendUI()
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.updateJuiceBlendUI()"), debugLogging);

		HelperFunctions.activityDefaultLayoutContainerSetup(usedSourceContainers, HelperFunctions.buildSourceContainerSet(getSelectedBlendBatchesSet(), getLocalInventorySet()), "Select Sourcce Container(s)");	
		HelperFunctions.activityDefaultLayoutContainerSetup(emptyTargetContainers, buildEligibleContainerSet(), "Select Target Container(s)");
		
		defaultLayoutHBoxSetup(hBox_1, usedSourceContainers, addSourceContainerButton);
		defaultLayoutHBoxSetup(hBox_2, emptyTargetContainers, addTargetContainerButton);
	
		juiceBlendContainerLabel1.setText("Source Containers");
		juiceBlendContainerLabel2.setText("Target Containers");
		
		gp.add(juiceBlendContainerLabel1, 0, 4);
		gp.add(hBox_1, 1, 4);
		gp.add(juiceBlendContainerLabel2, 0, 5);
		gp.add(hBox_2, 1, 5);
		gp.add(displayContainerSelections, 1, 6, 1, 2);
		
		GridPane.setValignment(hBox_1, javafx.geometry.VPos.CENTER);
		GridPane.setValignment(hBox_2, javafx.geometry.VPos.CENTER);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.updateJuiceBlendUI()"), debugLogging);
	}
	
	@SuppressWarnings("unused")
	private void activityDefaultLayoutContainerSetup(ComboBox<String> uiComboBox, ObservableList<String> containerContent, String promptText)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.activityDefaultLayoutContainerSetup('%s')", uiComboBox.getId()), debugLogging);

		uiComboBox.setItems(containerContent);
		uiComboBox.setPromptText(promptText);
		uiComboBox.setButtonCell(new ButtonCell());

		winemakerLogger.writeLog(String.format("<< BatchDetailController.activityDefaultLayoutContainerSetup('%s')", uiComboBox.getId()), debugLogging);
	}

	private void defaultLayoutHBoxSetup(HBox uiHBox, ComboBox<String> uiComboBox, Button uiButton)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.defaultLayoutHBoxSetup('%s', '%s', '%s')", uiHBox.getId(), uiComboBox.getId(), uiButton.getId()), debugLogging);
	
		uiHBox.setSpacing(8);
		uiHBox.getChildren().add(uiComboBox);
		uiHBox.getChildren().add(uiButton);
	
		winemakerLogger.writeLog(String.format("<< BatchDetailController.defaultLayoutHBoxSetup('%s', '%s', '%s')", uiHBox.getId(), uiComboBox.getId(), uiButton.getId()), debugLogging);
	}

	/*
	 * 
	 */
	private void loadComboBoxSelections(HashMap<String, String> codeSet, ComboBox<String> selectionList) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.loadComboBoxSelections('%s'...)", codeSet.values().toArray()[0]), debugLogging);
	
		selectionList.getItems().clear();
		
		ObservableList<String> comboBoxSelections = FXCollections.observableArrayList();
		comboBoxSelections.addAll(codeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		
		selectionList.setItems(comboBoxSelections);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.loadComboBoxSelections('%s'...)", codeSet.values().toArray()[0]), debugLogging);
	} // end of loadComboBoxSelections()

	/*
	 * 
	 */
	private void loadChoiceBoxSelections(HashMap<String, String> codeSet, ChoiceBox<String> choiceBoxValues, String valuesType) 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.loadChoiceBoxSelections('%s'...)", codeSet.values().toArray()[0]), debugLogging);
	
		choiceBoxValues.getItems().clear();
		
		ObservableList<String> choiceBoxSelections = FXCollections.observableArrayList();
		choiceBoxSelections.add("_");
		choiceBoxSelections.addAll(codeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		choiceBoxSelections.set(0, "Select " + valuesType);
		
		choiceBoxValues.setItems(choiceBoxSelections);
		choiceBoxValues.setValue(choiceBoxSelections.get(0));
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.loadChoiceBoxSelections('%s'...)", codeSet.values().toArray()[0]), debugLogging);
	} // end of loadComboBoxSelections(HashMap<String, String> codeSet, ComboBox<String> selectionList)

	/*
	 * Get set of existing candidate batches for populating the blend batch list.
	 * A batch must meet these requirements:
	 * 		> Have at least a Stage 2 ferment record
	 * 		> Include a Stage 2 Checkpoint record containing a current juice volume
	 * 		> Not be part of the new blend
	 */
	private void loadBlendBatchSets() 
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.loadBlendBatchSets() "), debugLogging);		
	
		this.existingBatchesList.clear();
		this.existingBatchesList.addAll(
			this.wmkSets
				.stream()
				.filter(wmk -> batchNotPartOfOldBlend(wmk))
				.filter(wmk -> batchNotPartOfNewBlend(wmk))
				.filter(wmk -> batchHasCheckpoints(wmk))
				.map(wmk -> HelperFunctions.batchKeyExpand(wmk))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		grapeSelectBlend.setItems(this.existingBatchesList);
		grapeSelectBlend.setPromptText((this.existingBatchesList.size() > 0) ? "Select an existing Batch": "No eligible batches");		
		grapeSelectBlend.setButtonCell(new ButtonCell());
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.loadBlendBatchSets() "), debugLogging);
	} // end of loadBlendBatchSets()

	/*
	 * Process the container selection buttons
	 */
	private void processContainerSelectButton(ActionEvent event)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.processContainerSelectButton()"), debugLogging);
	
		Button containerButton = (Button) event.getSource();
		
		winemakerLogger.writeLog(String.format("   BatchDetailController.processContainerSelectButton(): Process button '%s'", containerButton.getId()), debugLogging);
		if (containerButton.getId().equals("source"))
		{
			if (usedSourceContainers.getValue() != null)
			{
				processContainerSelection(usedSourceContainers);
				this.sourceContainerCount++;
			}
		}
		else
		{
			if (emptyTargetContainers.getValue() != null)
			{
				processContainerSelection(emptyTargetContainers);
				this.targetContainerCount++;
			}
		}
	
		winemakerLogger.writeLog(String.format("<< BatchDetailController.processContainerSelectButton()"), debugLogging);
	} // end of processContainerSelectButton()

	/*	
	 * Process the source and target selection buttons
	 */
	private void processContainerSelection(ComboBox<String> referencedContainer)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.processContainerSelection()"), debugLogging);
		
		WineMakerInventory inventoryUpdateRecord = HelperFunctions.findAssetItemRecord(getLocalInventorySet(), referencedContainer.getValue()).get(0);
		WineMakerInventory inventoryActivityRecord = inventoryUpdateRecord.createActivityRecord();

		inventoryActivityRecord.setItemTaskTime(Timestamp.valueOf(batchDate.getValue().atTime(LocalTime.now())));		
		inventoryActivityRecord.setItemTaskId(HelperFunctions.getCodeKeyEntry(FamilyCode.ACTIVITYFAMILY.getValue(), ActivityName.TRANSFER.getValue()));

		if (referencedContainer.getId().contains("target"))
		{
			displayContainerSelections.appendText("Target '" + referencedContainer.getValue() + "'\n");
			inventoryUpdateRecord.setItemBatchId(generateNewBatchKey(grapeSelect.getValue()));			
		}
		
		if (referencedContainer.getId().contains("source"))
		{
			displayContainerSelections.appendText("Source '" + referencedContainer.getValue() + "'\n");
			if (Integer.parseInt(saveLastBatchVol.get(inventoryUpdateRecord.getItemBatchId())) == 0)
				inventoryUpdateRecord.setItemBatchId("");
		}

		inventoryActivityRecord.setItemBatchId(inventoryUpdateRecord.getItemBatchId());
		
		updateInventoryActivity.add(inventoryUpdateRecord);
		insertInventoryActivity.add(inventoryActivityRecord);
		winemakerLogger.writeLog(String.format("   BatchDetailController.processContainerSelection(): updated inventory rec: %s", inventoryUpdateRecord), debugLogging);
		
		ObservableList<String> listEmptyContainers = referencedContainer.getItems();
		listEmptyContainers.remove(referencedContainer.getValue());
		referencedContainer.setItems(listEmptyContainers);
		
		winemakerLogger.writeLog(String.format("<< BatchDetailController.processContainerSelection()"), debugLogging);
	} // end of processContainerSelection()

	/*
	 * Build list of inventory container objects that are not assigned a batch
	 */
	private ObservableList<String> buildEligibleContainerSet()
	{
		winemakerLogger.writeLog(">> BatchDetailController.buildEligibleContainerSet()", debugLogging);
	
		BiFunction<String, String, String> buildItemDisplay = (itemName, itemId) -> (itemId.length() > 0) ? String.format("%s (%s)", itemId, itemName) : itemName;
		
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue());

		ObservableList<String> containerList = FXCollections.observableArrayList();
		containerList.addAll(getLocalInventorySet()
				.stream()
				.filter(wmi -> codeSet.get(wmi.get_itemName()) != null)
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> wmi.getItemBatchId().length() == 0)
				.map(wmi -> buildItemDisplay.apply(codeSet.get(wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog("<< BatchDetailController.buildEligibleContainerSet()", debugLogging);
		return containerList;
	} // end of buildEligibleContainerSet()

	/*
	 * Perform Regex match and return group value
	 */
	private String extractPattern(String fieldText, int groupNum)
	{
		Matcher matcher = regexAmtAndScalePattern.matcher(fieldText);
		matcher.find();
		return matcher.group(groupNum);
	}
	
	/*
	 * Convert the inventory display value back into its key value(s)
	 * 
	 * 		ie. "BRT10GAL01 (Brute 10 gal Bucket)" -> "BRT10GAL01", "brute10"
	 * 			"Ascorbic Acid" -> "ascorbic"
	 */
	@SuppressWarnings("unused")
	private List<String> convertItemName(String selectedValue)
	{
		winemakerLogger.writeLog(String.format(">> BatchDetailController.convertItemName('%s')", selectedValue), debugLogging);
		
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
	
		Optional<String> itemKey = getInventoryTypes().keySet()
				.stream()
				.filter(code -> returnValues.get(0).equals(getInventoryTypes().get(code)))
				.findFirst();
		if (!itemKey.isEmpty())
		{
			returnValues.set(0, itemKey.get());
		}

		winemakerLogger.writeLog(String.format("<< BatchDetailController.convertItemName('%s'): regex '%s' returns %s", selectedValue, matchItemName.toString(), returnValues), debugLogging);
		return returnValues;
	}

	private Timestamp createTimeStamp()
	{
		LocalTime logEntryTime = parseTimeString("now", "Entry time");
		Timestamp newDateTime = Timestamp.valueOf(batchDate.getValue().atTime(logEntryTime));
		
		return newDateTime;		
	}

	/*
	 * Validate and parse a time string.  The constant "now" is allowed as an alternative.
	 */
	private LocalTime parseTimeString(String uiDate, String fieldName)
	{
		String timeColonPattern = "h:mm a";
		LocalTime parsedTime = LocalTime.now();
		DateTimeFormatter timeParser = DateTimeFormatter.ofPattern(timeColonPattern);
		
		try
		{
			if (uiDate.equalsIgnoreCase("now"))
				parsedTime = LocalTime.now();
			else
				parsedTime = LocalTime.parse(uiDate.toUpperCase(), timeParser);
		}
		catch (DateTimeParseException de)
		{}
		
		return parsedTime;
	}
	
	private void inputTypeHandler(ChoiceBox<String> source)
	{
		if (source.getValue() == null)
			return;
		
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.BATCHSOURCEFAMILY.getValue());
		if (source.getValue().equals(codeSet.get(BatchSource.GRAPESOURCE.getValue())))
			unitPerItemLabel.setText("Weight per item");
		else
			unitPerItemLabel.setText("Volume per item");			
	}
	
	/*
	 * For field blends, no additional action is taken.
	 * 
	 * Otherwise, extract the batch's input type (juice | grape) and the highest stage number.
	 * Load these values into the UI objects for subsequent processing.
	 * These values don't need user input, so the UI objects can be preloaded.
	 */
	private void blendSelectHandler(ComboBox<String> source)
	{	
		if (source.getValue() == null || !this.blendSetting.equals(Blend.JUICEBLEND))
		{
			return;
		}
			
		winemakerLogger.writeLog(String.format(">> BatchDetailController.blendSelectHandler('%s')", source.getValue()), debugLogging);

		Optional<WineMakerLog> wmkReferenceSet = this.wmkSets
				.stream()
				.filter(wmk -> wmk.get_batchKey().equals(HelperFunctions.batchKeyCompress(source.getValue())))
				.findFirst();
		WineMakerLog wmk = wmkReferenceSet.get();
				
		ArrayList<WineMakerFerment> wmfSets = wmk.getWmkFerments();
		if (wmfSets.size() == 0)
		{
			winemakerLogger.displayAlert("No fermentation activities were found");
			winemakerLogger.writeLog(String.format("<< BatchDetailController.blendSelectHandler('%s'): no ferment activity", source.getValue()), debugLogging);
			return;
		}

		Optional<Integer> highStage = wmfSets
				.stream()
				.map(wmfOld -> wmfOld.get_stageCycle())
				.max(Integer::compare);

		if (highStage.isPresent())
		{
			batchInputSelect.setValue(HelperFunctions.getCodeKeyEntry(FamilyCode.BATCHSOURCEFAMILY.getValue(), wmk.get_batchSource()));

			Optional<WineMakerLog> wmkTest = this.wmkSets
					.stream()
					.filter(wmkCheck -> wmkCheck.get_batchKey().equals(HelperFunctions.batchKeyCompress(grapeSelectBlend.getValue())))
					.findFirst();

			WineMakerLog wmkFound = wmkTest.get();
			this.saveLastBatchVol.put("vol", "");

			wmkFound.getWmkFerments()
				.stream()
				.filter(wmf -> wmf.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()))
				.filter(wmf -> wmf.get_currentStageJuiceVol() > 0)
				.forEach(wmf -> this.saveLastBatchVol.put("vol", String.format("%s %s", wmf.get_currentStageJuiceVol(), wmf.get_currentStageJuiceScale())));	

			itemCount.setText(this.saveLastBatchVol.get("vol"));
			this.saveLastBatchVol.put(wmkFound.get_batchKey(), extractPattern(itemCount.getText(), 1));
		}

		if (!highStage.isPresent() || highStage.get() == 0)
		{
			winemakerLogger.writeLog(String.format("   BatchDetailController.blendSelectHandler('%s'): no valid stage records", source.getValue()), debugLogging);
			winemakerLogger.displayAlert("No valid fermentation stages were found");			
		}

		winemakerLogger.writeLog(String.format("<< BatchDetailController.blendSelectHandler('%s')", source.getValue()), debugLogging);
	} // end of blendSelectHandler()

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
	
	private void initUIObjects()
	{
		winemakerLogger.writeLog(">> BatchDetailController.initUIObjects()", debugLogging);

		gp.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		
		batchDate.setConverter(localDateStringConverter);

		setLocalInventorySet(winemakerModel.queryInventory());
		setInventoryTypes(HelperFunctions.getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue()));
		
		DropShadow dS = new DropShadow();
		vendorSelect.setEffect(dS);
		vineyardSelect.setEffect(dS);
		batchInputSelect.setEffect(dS);
		usedSourceContainers.setEffect(dS);
		emptyTargetContainers.setEffect(dS);
		displayContainerSelections.setEffect(dS);
		
		addSourceContainerButton.setId("source");
		addTargetContainerButton.setId("target");
		addSourceContainerButton.setText("Add");
		addTargetContainerButton.setText("Add");
		
		usedSourceContainers.setId("dyn-source");
		emptyTargetContainers.setId("dyn-target");
		
		hBox_1.setId("dyn-hbox1");
		hBox_2.setId("dyn-hbox2");
		
		addSourceContainerButton.setOnAction(e -> {
			processContainerSelectButton(e);
		});
		
		addTargetContainerButton.setOnAction(e -> {
			processContainerSelectButton(e);
		});

		winemakerLogger.writeLog("<< BatchDetailController.initUIObjects()", debugLogging);
	}
	
	/*
	 * Reinitialize the UI for blend type switch
	 */
	private void resetUIFields()
	{
		winemakerLogger.writeLog(">> BatchDetailController.resetUIFields()", debugLogging);
		
		Predicate<Node> nodeSkipDefault = node -> 
			node.getId() != null && 
			!node.getId().contains("batchDate") ;
	
		vendorNotes.clear();
	
		ObservableList<Node> childNodes = gp.getChildren();
		List<Node> childNodeSubset = childNodes
				.stream()
				.collect(Collectors.toList());
		
		childNodeSubset = childNodeSubset
			.stream()
			.filter(nodeSkipDefault)
			.collect(Collectors.toList());
		
		childNodeSubset.stream().forEach(oldNode -> 
		{
			winemakerLogger.writeLog(String.format("   BatchDetailController.resetUIFields(): # remove nodes '%s'", oldNode.getId()), debugLogging);
			
			if (oldNode instanceof HBox)
			{
				HBox hrzBox = (HBox) oldNode;
				hrzBox.getChildren().clear();
			}
			gp.getChildren().remove(oldNode);
		});
	
		winemakerLogger.writeLog("<< BatchDetailController.resetUIFields()", debugLogging);
	} // end of resetUIFields()

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

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		winemakerLogger.writeLog(">> BatchDetailController.initialize()", debugLogging);
		
		initUIObjects();
		resetUIFields();
		buildUIDirector(this.blendSetting.toString());
		
		addBlendGrape.setUserData(this.blendSetting);
		batchTitle.setText(" ");
		
		winemakerLogger.writeLog("<< BatchDetailController.initialize()", debugLogging);

		grapeSelectBlend.setOnAction(e -> {
			blendSelectHandler((ComboBox<String>) e.getSource());
		});

		batchInputSelect.setOnAction(e -> {
			inputTypeHandler((ChoiceBox<String>) e.getSource());
		});
		
	}
}
