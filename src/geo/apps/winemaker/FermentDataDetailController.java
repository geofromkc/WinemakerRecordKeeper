package geo.apps.winemaker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.Registry;
import geo.apps.winemaker.utilities.WineMakerLogging;
import geo.apps.winemaker.utilities.Constants.*;

/**
 * 
 */
public class FermentDataDetailController implements Initializable {

	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;

	private HashMap<String, String> codeSet = null;
	private HashMap<String, String> inventoryTypes = new HashMap<>(100);	
	private HashMap<String, String> uiLoadMethodsMap = new HashMap<>(15);
	private HashMap<String, String> validationMethodsMap = new HashMap<>(15);
	private HashMap<String, String> buildRecordMethodsMap = new HashMap<>(15);
	private HashMap<String, String> additionalRecordMethodsMap = new HashMap<>(15);
	
	private ArrayList<WineMakerFerment> wmfSets = new ArrayList<>();
	private ArrayList<WineMakerInventory> wmiInsertSet = new ArrayList<>();
	private ArrayList<WineMakerInventory> wmiUpdateSet = new ArrayList<>();
	private ArrayList<String> inventoryCodes = new ArrayList<>();
	private ArrayList<WineMakerInventory> updateInventoryStock = new ArrayList<WineMakerInventory>();
	private ArrayList<WineMakerInventory> updateInventoryBatch = new ArrayList<WineMakerInventory>();
	private ArrayList<WineMakerInventory> insertInventoryActivity = new ArrayList<WineMakerInventory>();
	private ArrayList<WineMakerInventory> inventoryExistingSetsQueryList = new ArrayList<WineMakerInventory>(100);
		
	private WineMakerLog wmk = null;

	private String activityCode = "";
	private String regexNumber = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([A-BD-EG-bd-eg-z%\\/]*)$";
	private Pattern matchAmountPattern = Pattern.compile(regexNumber);	
	private String regexTemp = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([cCfF]*)$";
	private Pattern matchTempPattern = Pattern.compile(regexTemp);

	private final String volumeMsg = "%s should be like '48 oz, or 100 g', not '%s'%n";

	@FXML GridPane gp;
	@FXML Pane titlePane;
	@FXML TextArea statusDisplay;

	/*
	 * Scene ComboBoxes
	 */
	@FXML private ComboBox<String> activitySelect;
	ObservableList<String> activitiesList = FXCollections.observableArrayList();

	ComboBox<String> stageSelect = new ComboBox<String>();

	@FXML DatePicker activityDate;
	@FXML TextField entryTime = new TextField();
	
	TextField startingTime = new TextField();
	TextField endingTime = new TextField();

	@FXML Label bannerText;
	@FXML Label batchTitle;
	
	@FXML Label fieldLabel_0;
	@FXML Label fieldLabel_1;
	@FXML Label fieldLabel_2;
	@FXML Label fieldLabel_3;
	@FXML Label fieldLabel_4;
	@FXML Label fieldLabel_5;
	@FXML Label fieldLabel_6;
	@FXML Label fieldLabel_7;
	@FXML Label fieldLabel_8;
	@FXML Label fieldLabel_9;
	@FXML Label fieldLabel_10;

	TextField field_1 = new TextField();
	TextField field_2 = new TextField();
	TextField field_3 = new TextField();
	TextField field_4 = new TextField();
	TextField field_5 = new TextField();
	TextField field_6 = new TextField();
	TextField field_7 = new TextField();
	TextField field_8 = new TextField();
	TextField field_9 = new TextField();
	TextField field_10 = new TextField();

	Label notesLabel = new Label();
	TextArea fieldNotes = new TextArea();

	ComboBox<String> usedSourceContainers = new ComboBox<String>();
	ComboBox<String> emptyTargetContainers = new ComboBox<String>();
	ComboBox<String> fieldContainers3 = new ComboBox<String>();
	ComboBox<String> fieldContainers4 = new ComboBox<String>();
	ComboBox<String> fieldContainers5 = new ComboBox<String>();
	ComboBox<String> fieldContainers6 = new ComboBox<String>();
	ComboBox<String> fieldContainers7 = new ComboBox<String>();
	ComboBox<String> fieldContainers8 = new ComboBox<String>();
	
	HBox hbStartTime = new HBox();
	HBox hbEndTime = new HBox();
	HBox hBox_1 = new HBox();
	HBox hBox_2 = new HBox();
	HBox hBox_3 = new HBox();
	HBox hBox_4 = new HBox();
	HBox hBox_5 = new HBox();
	HBox hBox_6 = new HBox();
	HBox hBox_7 = new HBox();
	HBox hBox_8 = new HBox();

	Button addTargetContainerButton = new Button();
	Button addSourceContainerButton = new Button();
	TextArea displayContainerSelections = new TextArea();
	
	ArrayList<String> sfieldSet = new ArrayList<String>();
	ArrayList<String> efieldLabelSet = new ArrayList<String>();
	ArrayList<String> dhboxSet = new ArrayList<String>();
	
	ArrayList<HBox> objectHboxSet = new ArrayList<HBox>();
	ArrayList<TextField> objectFieldSet = new ArrayList<TextField>();
	ArrayList<ComboBox<String>> objectComboBoxSet = new ArrayList<ComboBox<String>>();
	ArrayList<Label> objectLabelSet = new ArrayList<Label>();

	String datePart = "";
	String timePart = "";
	String datePickerOriginal = "";
	Timestamp ts = null;
	boolean debugLogging = true;
	
	Object classInstance;
	Class<?> classRef;

	private Function<String, String> testForEmptyField = 
			textField -> (textField.length() > 0) ? textField : "0";
	private Function<String, String> testForEmptyTemp = 
			textField -> (textField.length() > 0) ? textField : "0 f";
	private Function<String, String> testForEmptyVolume = 
			textField -> (textField.length() > 0) ? textField : "0 ml";

	private Supplier<Integer> getMaxStage = () -> this.wmfSets
			.stream()
			.map(wmfTest -> wmfTest.get_stageCycle())
			.max(Integer::compare)
			.get();
	private BiFunction<ArrayList<WineMakerFerment>, Integer, Optional<WineMakerFerment>> getStageRecord = (recordSet, cycleNum) -> recordSet
			.stream()
			.filter(wmfTest -> wmfTest.get_fermentActivity().equals(ActivityName.CHECKPOINT.getValue()))
			.filter(wmfTest -> wmfTest.get_stageCycle() == cycleNum)
			.findFirst();
	private BiFunction<String, String, String> buildItemDisplay = 
			(itemName, itemId) -> (itemId.length() > 0) ? 
					String.format("%s (%s)", itemId, itemName) : itemName;
			
	@SuppressWarnings("unused")
	private Integer stageValue = 0;
	private Integer sourceContainerCount = 0;
	private Integer targetContainerCount = 0;
	
	/*
	 * ==============================================================================
	 */
	public FermentDataDetailController()
	{
		Registry appRegistry = HelperFunctions.getRegistry();

		this.winemakerModel = (WineMakerModel) appRegistry.get(RegistryKeys.MODEL);
		this.winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		
		loadMethodMaps();
	}

	private ArrayList<WineMakerInventory> getLocalInventorySet()
	{
		return inventoryExistingSetsQueryList;
	}
	
	private void setLocalInventorySet(ArrayList<WineMakerInventory> newInventorySet)
	{
		inventoryExistingSetsQueryList = newInventorySet;
	}
	
	/*
	 * Load the activity->method maps:
	 * 		Amelioration
	 * 		Bottle
	 * 		Checkpoint
	 * 		Crush
	 * 		Press
	 * 		Rack
	 * 		Transfer
	 * 		Start Ferment
	 * 		Yeast Pitch 
	 * 
	 * Load routines for both map sets, even for unimplemented activities.   The activityRecordMethodCall() method will
	 * handle methods that are not implemented  	 
	 */
	private void loadMethodMaps()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadMethodMaps()"), debugLogging);

		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ACTIVITYFAMILY.getValue());

		methodMapInsert(ActivityName.AMELIORATION.getValue(), codeSet.get(ActivityName.AMELIORATION.getValue()));
		methodMapInsert(ActivityName.CHECKPOINT.getValue(), codeSet.get(ActivityName.CHECKPOINT.getValue()));
		methodMapInsert(ActivityName.CRUSH.getValue(), codeSet.get(ActivityName.CRUSH.getValue()));
		methodMapInsert(ActivityName.RACK.getValue(), codeSet.get(ActivityName.RACK.getValue()));
		methodMapInsert(ActivityName.TRANSFER.getValue(), codeSet.get(ActivityName.TRANSFER.getValue()));
		methodMapInsert(ActivityName.PRESS.getValue(), codeSet.get(ActivityName.PRESS.getValue()));
		methodMapInsert(ActivityName.YEASTPITCH.getValue(), codeSet.get(ActivityName.YEASTPITCH.getValue()));
		methodMapInsert(ActivityName.BOTTLE.getValue(), codeSet.get(ActivityName.BOTTLE.getValue()));
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadMethodMaps(): UI %s", uiLoadMethodsMap), debugLogging);
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadMethodMaps(): Validate %s", validationMethodsMap), debugLogging);
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadMethodMaps(): Rec load %s", buildRecordMethodsMap), debugLogging);
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadMethodMaps(): Extras %s", additionalRecordMethodsMap), debugLogging);

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadMethodMaps()"), debugLogging);
	} // end of loadMethodMaps()

	/*
	 * Populate the HashMaps connecting activities to their dynamically-called methods
	 * For example: uiLoadMethodsMap.put("crush", "activityCrushLayout");
	 */
	private void methodMapInsert(String activityCode, String activityName)
	{		
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.methodMapInsert('%s', '%s')", activityCode, activityName), debugLogging);

		uiLoadMethodsMap.put(activityCode, String.format("activity%sLayout", activityName));
		validationMethodsMap.put(activityCode, String.format("validate%s", activityName));
		buildRecordMethodsMap.put(activityCode, String.format("load%sRecord", activityName));
		additionalRecordMethodsMap.put(activityCode, String.format("submitExtra%s", activityName));
	
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.methodMapInsert('%s', '%s')", activityCode, activityName), debugLogging);
	} // end of methodMapInsert()

	/*
	 * Retrieve parent batch and retrieve the existing Ferment activity records
	 */
	public void setBatchKey(String batchKey) 
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.setBatchKey('%s')", batchKey), debugLogging);

		this.wmk = winemakerModel.queryBatch(batchKey, SQLSearch.PARENTBATCH).get(0);
		this.wmfSets = winemakerModel.queryFermentData(batchKey);
		
		batchTitle.setText("For batch: " + HelperFunctions.batchKeyExpand(this.wmk));
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.setBatchKey('%s')", batchKey), debugLogging);
	}
	
	/*
	 * Dynamically call UI Build method associated with this activity
	 */
	private void activityBuildUI(String activityCode)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityBuildUI(activityCode '%s'): call method '%s'", activityCode, uiLoadMethodsMap.get(activityCode)), debugLogging);

		fieldNotes.clear();
		Method uiBuildMethod;
        
		try
		{
			uiBuildMethod = FermentDataDetailController.class.getDeclaredMethod(uiLoadMethodsMap.get(activityCode));			
			uiBuildMethod.invoke(this);
		}
		catch (NoSuchMethodException | SecurityException | NullPointerException e) 
		{
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.activityBuildUI(activityCode '%s'): method '%s' not implemented", activityCode, uiLoadMethodsMap.get(activityCode)), debugLogging);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
		{
			winemakerLogger.showIOException(e, "Method Invocation");
		}
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityBuildUI(activityCode '%s'): call method '%s'", activityCode, uiLoadMethodsMap.get(activityCode)), debugLogging);
	} // end of activityBuildUI()
	
	/*
	 * Dynamically call Validation method associated with this activity
	 */
	private Validation activityValidation(String activityCode)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityValidation(activityCode '%s'): call method '%s'", activityCode, validationMethodsMap.get(activityCode)), debugLogging);

		Method validationMethod;
        Validation methodReturn = Validation.PASSED;
        
		try 
		{
			validationMethod = FermentDataDetailController.class.getDeclaredMethod(validationMethodsMap.get(activityCode));			
			methodReturn = (Validation) validationMethod.invoke(this);
		}
		catch (NoSuchMethodException | SecurityException e) 
		{
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.activityValidation(activityCode '%s'): method '%s' not implemented", activityCode, validationMethodsMap.get(activityCode)), debugLogging);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
		{
			winemakerLogger.showIOException(e, "Method Invocation");
		}

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityValidation(activityCode '%s'): call method '%s'", activityCode, validationMethodsMap.get(activityCode)), debugLogging);
		return methodReturn;
	} // end of activityValidation()
	
	/*
	 * Dynamically call Record Build method associated with this activity
	 */
	private WineMakerFerment activityRecordMethodCall(String activityCode)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityRecordMethodCall('%s'): call method '%s'", activityCode, buildRecordMethodsMap.get(activityCode)), debugLogging);

		WineMakerFerment wmf = new WineMakerFerment(this.winemakerModel);
        
		try 
		{
			Method buildMethod = FermentDataDetailController.class.getDeclaredMethod(buildRecordMethodsMap.get(activityCode));
			wmf = (WineMakerFerment) buildMethod.invoke(this);
		}
		catch (NoSuchMethodException | SecurityException e) 
		{
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.activityRecordMethodCall(activityCode '%s'): method '%s' not implemented", activityCode, buildRecordMethodsMap.get(activityCode)), debugLogging);
		}
		catch (IllegalAccessException | InvocationTargetException e) 
		{
			winemakerLogger.showIOException(e, "Method Invocation");
		}
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityRecordMethodCall('%s'): call method '%s'", activityCode, buildRecordMethodsMap.get(activityCode)), debugLogging);
		return wmf;
	} // end of activityRecordBuild()

	/*
	 * Dynamically call Record Build method associated with this activity
	 */
	private void activitysSupplementalRecordMethodCall(String activityCode, WineMakerFerment wmfActivity)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activitysSupplementalRecordMethodCall('%s'): call method '%s'", activityCode, additionalRecordMethodsMap.get(activityCode)), debugLogging);

		Method supplementalRecordMethod;
		
		@SuppressWarnings("rawtypes")
		Class[] arg = new Class[1];
        arg[0] = WineMakerFerment.class;
		
		try 
		{
			supplementalRecordMethod = FermentDataDetailController.class.getDeclaredMethod(additionalRecordMethodsMap.get(activityCode), arg[0]);
			supplementalRecordMethod.invoke(this, wmfActivity);
		}
		catch (NoSuchMethodException | SecurityException e) 
		{
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.activitysSupplementalRecordMethodCall(activityCode '%s'): method call error '%s'", activityCode, e.getMessage()), debugLogging);
		}
		catch (IllegalAccessException | InvocationTargetException e) 
		{
			winemakerLogger.showIOException(e, "Method Invocation");
		}
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activitysSupplementalRecordMethodCall('%s'): call method '%s'", activityCode, additionalRecordMethodsMap.get(activityCode)), debugLogging);
	} // end of activitysSupplementalRecordMethodCall()
	
	private ObservableList<String> loadObservableList(HashMap<String, String> codeSet)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadObservableList(%s)", codeSet), debugLogging);
		
		ObservableList<String> valuesList = FXCollections.observableArrayList();
		ArrayList<String> keyList = new ArrayList<>(codeSet.keySet());
		valuesList.addAll(keyList
				.stream()
				.map(code -> codeSet.get(code))
				.sorted()
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadObservableList()"), debugLogging);
		return valuesList;
	}

	/*
	 * 	Initialize the fixed fields
	 */
	private void activityDefaultLayout()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityDefaultLayout()"), debugLogging);
			
		activityDate.getEditor().clear();
		activityDate.setValue(null);
		notesLabel.setText("Notes");
		fieldNotes.setPromptText("Notes");

		this.sourceContainerCount = 0;
		this.targetContainerCount = 0;
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityDefaultLayout()"), debugLogging);
	} // end of activityDefaultLayout()

	/* Build UI layout for the Ameliorate activity
	 * field_1 - Must/Juice volume	
	 * field_2 - Must temperature
	 * fieldContainers & field_3 - Additive 1 and amount	
	 * fieldContainers2 & field_4 - Additive 2 and amount	
	 * fieldContainers3 & field_5 - Additive 3 and amount
	 * fieldContainers4 & field_6 - Additive 4 and amount
	 * fieldContainers5 & field_7 - Additive 5 and amount
	 * fieldContainers6 & field_8 - Additive 6 and amount
	 */
	@SuppressWarnings("unused")
	private void activityAmeliorationLayout()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.activityAmeliorateLayout()", debugLogging);
		
		activityDefaultLayout();
		
		ObservableList<String> chemList =  loadObservableList(HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue()));
	
		fieldLabel_1.setText("Current Volume");
		fieldLabel_2.setText("Current Temperature");
		fieldLabel_3.setText("Select Container");
		fieldLabel_4.setText("Select Additive & Amt");
		fieldLabel_5.setText("Select Additive & Amt");
		fieldLabel_6.setText("Select Additive & Amt");
		fieldLabel_7.setText("Select Additive & Amt");
		fieldLabel_8.setText("Select Additive & Amt");
		fieldLabel_9.setText("Select Additive & Amt");
	
		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");

		activityDefaultLayoutContainerSetup(fieldContainers3, chemList, "Select Additive");
		activityDefaultLayoutContainerSetup(fieldContainers4, chemList, "Select Additive");
		activityDefaultLayoutContainerSetup(fieldContainers5, chemList, "Select Additive");
		activityDefaultLayoutContainerSetup(fieldContainers6, chemList, "Select Additive");
		activityDefaultLayoutContainerSetup(fieldContainers7, chemList, "Select Additive");
		activityDefaultLayoutContainerSetup(fieldContainers8, chemList, "Select Additive");
	
		activityDefaultLayoutFieldSetup(field_1, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_2, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_3, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_4, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_5, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_6, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_7, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_8, "", "0", 60);
		
		activityDefaultLayoutHBoxSetup(hBox_1, fieldContainers3, field_3);
		activityDefaultLayoutHBoxSetup(hBox_2, fieldContainers4, field_4);
		activityDefaultLayoutHBoxSetup(hBox_3, fieldContainers5, field_5);
		activityDefaultLayoutHBoxSetup(hBox_4, fieldContainers6, field_6);
		activityDefaultLayoutHBoxSetup(hBox_5, fieldContainers7, field_7);
		activityDefaultLayoutHBoxSetup(hBox_6, fieldContainers8, field_8);
	
		gp.add(field_1, 1, 3);
		gp.add(field_2, 1, 4);
		gp.add(usedSourceContainers, 1, 5);
		gp.add(hBox_1, 1, 6);
		gp.add(hBox_2, 1, 7);
		gp.add(hBox_3, 1, 8);
		gp.add(hBox_4, 1, 9);
		gp.add(hBox_5, 1, 10);
		gp.add(hBox_6, 1, 11);
		
		winemakerLogger.writeLog("<< FermentDataDetailController.activityAmeliorateLayout()", debugLogging);
	}
	
	/*
	 *	Build UI layout for the Bottle activity
	 *	fieldLabel_1 > usedSourceContainers = source container
	 *	fieldLabel_2 > field_3 = Bottle count
	 *	fieldLabel_3 > Collected containers
	 */
	@SuppressWarnings("unused")
	private void activityBottleLayout()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityBottleLayout()"), debugLogging);

		activityDefaultLayout();
		
		fieldLabel_1.setText("Source Containers");
		fieldLabel_2.setText("Bottle Count");
		fieldLabel_3.setText("Selected Containers");

		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");			
		activityDefaultLayoutHBoxSetup(hBox_1, usedSourceContainers, addSourceContainerButton);
		activityDefaultLayoutFieldSetup(field_3, "1", "", 50);

		gp.add(hBox_1, 1, 3);
		gp.add(field_3, 1, 4);
		gp.add(displayContainerSelections, 1, 5, 1, 2);		

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityBottleLayout()"), debugLogging);
	}

	/*
	 * Build UI for Checkpoint activity 
	 *  field_6 = Total stage volume
	 *	field_1 = Tested volume
	 *	field_2 = Temp
	 *	field_3 = Brix
	 *	field_4 = pH
	 *	field_5 = TA
	 */
	@SuppressWarnings("unused")
	private void activityCheckpointLayout()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityCheckpointLayout()"), debugLogging);
	
		activityDefaultLayout();
	
		fieldLabel_0.setText("Select Container");
		fieldLabel_1.setText("Total Volume");
		fieldLabel_2.setText("Tested Volume");
		fieldLabel_3.setText("Current Temperature");
		fieldLabel_4.setText("Brix");
		fieldLabel_5.setText("pH");
		fieldLabel_6.setText("TA");
		
		activityDefaultLayoutFieldSetup(field_6, "volume", "", 60);
		activityDefaultLayoutFieldSetup(field_1, "volume", "", 60);
		activityDefaultLayoutFieldSetup(field_2, "temp", "", 40);
		activityDefaultLayoutFieldSetup(field_3, "Brix", "", 40);
		activityDefaultLayoutFieldSetup(field_4, "pH", "", 40);
		activityDefaultLayoutFieldSetup(field_5, "TA", "", 40);
	
		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");

		gp.add(usedSourceContainers, 1, 2);
		gp.add(field_6, 1, 3);
		gp.add(field_1, 1, 4);
		gp.add(field_2, 1, 5);
		gp.add(field_3, 1, 6);
		gp.add(field_4, 1, 7);
		gp.add(field_5, 1, 8);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityCheckpointLayout()"), debugLogging);
	} // end of activityCheckpointLayout()

	/*
	 * Build UI for the Crush activity 
	 * 
	 * field-1 = input grape weight 
	 * field-2 = output juice volume 
	 * field-3 = must temperature 
	 * emptyTargetContainers = ComboBox list of eligible containers
	 * 
	 */
	@SuppressWarnings("unused")
	private void activityCrushLayout() 
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.activityCrushLayout()", debugLogging);
	
		activityDefaultLayout();
		
		fieldLabel_1.setText("Input Amt");
		fieldLabel_2.setText("Output Volume");
		fieldLabel_3.setText("Must Temperature");
		fieldLabel_4.setText("Container Selection");
		fieldLabel_5.setText("Target Containers");
			
		activityDefaultLayoutContainerSetup(emptyTargetContainers, buildEligibleContainerSet(), "Select Container");	
		activityDefaultLayoutFieldSetup(field_1, "Grape weight", "", 60);
		activityDefaultLayoutFieldSetup(field_2, "Output volume", "", 60);
		activityDefaultLayoutFieldSetup(field_3, "Must Temp", "", 60);
		activityDefaultLayoutHBoxSetup(hBox_1, emptyTargetContainers, addTargetContainerButton);
	
		gp.add(field_1, 1, 3);
		gp.add(field_2, 1, 4);
		gp.add(field_3, 1, 5);
		gp.add(hBox_1, 1, 6);
		gp.add(displayContainerSelections, 1, 7, 1, 2);
		
		winemakerLogger.writeLog("<< FermentDataDetailController.activityCrushLayout()", debugLogging);
	} // end of activityCrushLayout()

	/*
	 * Build UI layout for the Press activity
	 * 
	 * field_1 => Must volume
	 * usedSourceContainers = ComboBox for source containers already assigned to batch
	 * usedTargetContainers = ComboBox for eligible target containers
	 * displayContainerSelections: TextArea showing container selections
	 * 
	 */
	@SuppressWarnings("unused")
	private void activityPressLayout()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.activityPressLayout()", debugLogging);
	
		activityDefaultLayout();
	
		fieldLabel_1.setText("Output Volume");
		fieldLabel_2.setText("Source Containers");
		fieldLabel_3.setText("Target Containers");
		fieldLabel_4.setText("Selected Containers");
		
		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");	
		activityDefaultLayoutContainerSetup(emptyTargetContainers, buildEligibleContainerSet(), "Select Container");
		
		activityDefaultLayoutFieldSetup(field_1, "Current must volume", "", 60);	
		activityDefaultLayoutHBoxSetup(hBox_1, usedSourceContainers, addSourceContainerButton);
		activityDefaultLayoutHBoxSetup(hBox_2, emptyTargetContainers, addTargetContainerButton);

		gp.add(field_1, 1, 3);
		gp.add(hBox_1, 1, 4);
		gp.add(hBox_2, 1, 5);
		gp.add(displayContainerSelections, 1, 6, 1, 2);
		
		winemakerLogger.writeLog("<< FermentDataDetailController.activityPressLayout()", debugLogging);
	} // end of activityPressLayout()

	/*
	 * Build UI layout for the Rack activity
	 * 
	 * field_1 => Must volume
	 * usedSourceContainers = ComboBox for source containers already assigned to batch
	 * usedTargetContainers = ComboBox for eligible target containers
	 * displayContainerSelections: TextArea showing container selections
	 * 
	 */
	private void activityRackLayout()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.activityRackLayout()", debugLogging);
	
		activityDefaultLayout();
	
		fieldLabel_1.setText("Output Volume");
		fieldLabel_2.setText("Source Containers");
		fieldLabel_3.setText("Target Containers");
		fieldLabel_4.setText("Selected Containers");
		
		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");	
		activityDefaultLayoutContainerSetup(emptyTargetContainers, buildEligibleContainerSet(), "Select Container");
		
		activityDefaultLayoutFieldSetup(field_1, "Current volume", "", 60);	
		activityDefaultLayoutHBoxSetup(hBox_1, usedSourceContainers, addSourceContainerButton);
		activityDefaultLayoutHBoxSetup(hBox_2, emptyTargetContainers, addTargetContainerButton);

		gp.add(field_1, 1, 3);
		gp.add(hBox_1, 1, 4);
		gp.add(hBox_2, 1, 5);
		gp.add(displayContainerSelections, 1, 6, 1, 2);
		
		winemakerLogger.writeLog("<< FermentDataDetailController.activityRackLayout()", debugLogging);
	} // end of activityRackLayout()
		
	/*
	 * Alternative title for the Rack activity
	 */
	@SuppressWarnings("unused")
	private void activityTransferLayout()
	{
		activityRackLayout();
	}

	/* Build UI layout for the Yeast Pitch activity
	 * 
	 * row 2: fieldLabel_0: field_1 - Juice volume
	 * row 3: fieldLabel_1: field_2 - Must temperature
	 * row 4: fieldLabel_2: usedContainers - Container being adjusted
	 * row 5: fieldLabel_3: fieldContainers3 & field_3 - Yeast type and amount	
	 * row 6: fieldLabel_4: field_4 = Go Ferm amt
	 * row 7: fieldLabel_5: field_5 = FT Rouge amt
	 * row 8: fieldLabel_6: field_7 = Fermaid K amt	
	 * row 9: fieldLabel_7: field_7 = Fermaid O amt	
	 * row 10: fieldLabel_8: field_8 = Opti-Red amt
	 * 
	 */
	@SuppressWarnings("unused")
	private void activityYeastPitchLayout()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.activityYeastPitchLayout()", debugLogging);

		activityDefaultLayout();
		
		ObservableList<String> yeastOptions = FXCollections.observableArrayList();
		
		fieldLabel_0.setText("Current Volume");
		fieldLabel_1.setText("Current Temperature");		
		fieldLabel_2.setText("Adjusted Container");		
		fieldLabel_3.setText("Select Yeast and Amt");
		
		activityDefaultLayoutContainerSetup(usedSourceContainers, buildSourceContainerSet(this.wmk.get_batchKey()), "Select Container");	
		
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue());
		ArrayList<String> keyList = new ArrayList<>(codeSet.keySet());
	
		yeastOptions.addAll(keyList
				.stream()
				.map(code -> codeSet.get(code))
				.sorted()
				.collect(Collectors.toList()));
		
		fieldContainers3.setItems(yeastOptions);
		fieldContainers3.setPromptText("Select Yeast");	
		fieldContainers3.setButtonCell(new ButtonCell());

		activityDefaultLayoutContainerSetup(fieldContainers3, yeastOptions, "Select Yeast");	
		activityDefaultLayoutHBoxSetup(hBox_2, fieldContainers3, field_3);

		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		
		fieldLabel_4.setText(codeSet.get(YeastPitchDefaults.GOFERM.getValue()));
		fieldLabel_5.setText(codeSet.get(YeastPitchDefaults.FTROUGE.getValue()));
		fieldLabel_6.setText(codeSet.get(YeastPitchDefaults.FERMAIDK.getValue()));
		fieldLabel_7.setText(codeSet.get(YeastPitchDefaults.FERMAIDO.getValue()));
		fieldLabel_8.setText(codeSet.get(YeastPitchDefaults.OPTIRED.getValue()));
		
		activityDefaultLayoutFieldSetup(field_1, "Current volume", "", 60);
		activityDefaultLayoutFieldSetup(field_2, "Current temp", "", 60);
		activityDefaultLayoutFieldSetup(field_3, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_4, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_5, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_6, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_7, "", "0", 60);
		activityDefaultLayoutFieldSetup(field_8, "", "0", 60);
		
		gp.add(field_1, 1, 2);
		gp.add(field_2, 1, 3);
		gp.add(usedSourceContainers, 1, 4);
		gp.add(hBox_2, 1, 5);
		gp.add(field_4, 1, 6);
		gp.add(field_5, 1, 7); 
		gp.add(field_6, 1, 8);
		gp.add(field_7, 1, 9);
		gp.add(field_8, 1, 10);
		
		winemakerLogger.writeLog("<< FermentDataDetailController.activityYeastPitchLayout()", debugLogging);		
	}

	/*
	 * Build list of inventory container objects in this batch
	 */
	private ObservableList<String> buildSourceContainerSet(String batchId)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.buildSourceContainerSet('%s')", batchId), debugLogging);
		
		BiFunction<String, String, String> buildItemDisplay = (itemName, itemId) -> (itemId.length() > 0) ? String.format("%s (%s)", itemId, itemName) : itemName;
		
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue());
		ObservableList<String> containerList = FXCollections.observableArrayList();
	
		containerList.addAll(getLocalInventorySet()
				.stream()
				.filter(wmi -> codeSet.get(wmi.get_itemName()) != null)
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> wmi.getItemBatchId().equals(batchId))
				.map(wmi -> buildItemDisplay.apply(codeSet.get(wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.buildSourceContainerSet()"), debugLogging);
		return containerList;
	} // end of buildSourceContainerSet(<batch id>)
	
	/*
	 * Build list of inventory container objects that are not assigned a batch
	 */
	private ObservableList<String> buildEligibleContainerSet()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.buildEligibleContainerSet()", debugLogging);
	
		BiFunction<String, String, String> buildItemDisplay = (itemName, itemId) -> (itemId.length() > 0) ? String.format("%s (%s)", itemId, itemName) : itemName;
		
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue());
		ObservableList<String> containerList = FXCollections.observableArrayList();
	
		containerList.addAll(getLocalInventorySet()
				.stream()
				.filter(wmi -> codeSet.get(wmi.get_itemName()) != null)
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> wmi.getItemBatchId().length() == 0)
				.map(wmi -> buildItemDisplay.apply(codeSet.get(wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog("<< FermentDataDetailController.buildEligibleContainerSet()", debugLogging);
		return containerList;
	} // end of buildEligibleContainerSet()

	private void activityDefaultLayoutContainerSetup(ComboBox<String> uiComboBox, ObservableList<String> containerContent, String promptText)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityDefaultLayoutContainerSetup('%s')", uiComboBox.getId()), debugLogging);

		uiComboBox.setItems(containerContent);
		uiComboBox.setPromptText(promptText);
		uiComboBox.setButtonCell(new ButtonCell());

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityDefaultLayoutContainerSetup('%s')", uiComboBox.getId()), debugLogging);
	} // end of activityDefaultLayoutContainerSetup()

	private void activityDefaultLayoutFieldSetup(TextField uiField, String promptText, String displayText, double maxWidth)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityDefaultLayoutFieldSetup('%s', %1.0f)", uiField.getId(), maxWidth), debugLogging);

		uiField.setPromptText(promptText);
		uiField.setText(displayText);
		uiField.setMaxWidth(maxWidth);

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityDefaultLayoutFieldSetup('%s')", uiField.getId()), debugLogging);
	} // end of activityDefaultLayoutFieldSetup()

	private void activityDefaultLayoutHBoxSetup(HBox uiHBox, ComboBox<String> uiComboBox, TextField uiField)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityDefaultLayoutHBoxSetup('%s', '%s', '%s' (%s))", uiHBox.getId(), uiComboBox.getId(), uiField.getId(), uiField.getText()), debugLogging);

		uiHBox.setSpacing(8);
		uiHBox.getChildren().add(uiComboBox);
		uiHBox.getChildren().add(uiField);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityDefaultLayoutHBoxSetup('%s', '%s', '%s')", uiHBox.getId(), uiComboBox.getId(), uiField.getId()), debugLogging);
	} // end of activityDefaultLayoutHBoxSetup()

	private void activityDefaultLayoutHBoxSetup(HBox uiHBox, ComboBox<String> uiComboBox, Button uiButton)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityDefaultLayoutHBoxSetup('%s', '%s', '%s')", uiHBox.getId(), uiComboBox.getId(), uiButton.getId()), debugLogging);

		uiHBox.setSpacing(8);
		uiHBox.getChildren().add(uiComboBox);
		uiHBox.getChildren().add(uiButton);

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityDefaultLayoutHBoxSetup('%s', '%s', '%s')", uiHBox.getId(), uiComboBox.getId(), uiButton.getId()), debugLogging);
	} // end of activityDefaultLayoutHBoxSetup()

	/*
	 * Validate default fields for Ferment activities
	 * 
	 * 		activityDate = required field
	 * 		stageSelect = required field, plus check for existence of selected stage # 
	 * 
	 * Optionally:
	 * 		entryTime = required field, plus check for valid time string
	 * 		startingTime = actual beginning of this activity
	 * 		endingTime = actual ending of this activity
	 */
	private String validateDefaultFields(TimeCheck fieldSelect)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.validateDefaultFields('%s')", fieldSelect.toString()), debugLogging);

		String validateMessages = "";
	
		if (entryTime == null || entryTime.getText().isEmpty())
			entryTime.setText("now");
		
		validateMessages += (activityDate.getValue() == null) ? 
			"Entry date was not selected\n" : "";
		validateMessages += (activityDate.getValue() != null && activityDate.getValue().getYear() == 1900) ? 
				String.format("Invalid date string was converted to 1/1/1900 from %s%n", datePickerOriginal) : "";
		validateMessages += (HelperFunctions.parseTimeString(entryTime.getText()) == null) ? 
			String.format("%n%s value '%s' is not a recognized time string (hh:mm am|pm): %n", "Entry Time", entryTime.getText()) : "";

		datePickerOriginal = "";
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.validateDefaultFields('%s')", fieldSelect.toString()), debugLogging);
		return validateMessages; 
	} // end of validateDefaultFields()
	
	private String validateAdditiveField(ComboBox<String> inputCategory, TextField inputValue)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.validateAdditiveField(%s, '%s' (%s))", inputCategory.getId(), inputValue.getText(), inputValue.getId()), debugLogging);

		String errorMsg = "";
		if (inputValue.getText() == null || inputCategory.getValue() == null)
		{
			winemakerLogger.writeLog(String.format("<< FermentDataDetailController.validateAdditiveField(%s (%s), '%s' (%s)): empty input", inputCategory.getId(), inputCategory.getValue(), inputValue.getText(), inputValue.getId()), debugLogging);
			return errorMsg;
		}
		
		List<WineMakerInventory> searchSet = HelperFunctions.findAssetItemRecord(this.getLocalInventorySet(), inputCategory.getValue());

		Matcher validateMatcher = HelperFunctions.validateNumberInput(inputValue.getText());
		errorMsg += (!inputValue.getText().isEmpty() && validateMatcher.groupCount() < 2) ?
				String.format(volumeMsg, inputCategory.getValue(), inputValue.getText()) : "";
		
		if (validateMatcher.matches() && (validateMatcher.groupCount() == 2) && (searchSet.size() > 0))
		{
			if (!searchSet.get(0).get_itemAmountScale().equalsIgnoreCase(validateMatcher.group(2)))
				HelperFunctions.convertSizeField(searchSet.get(0).get_itemAmountScale(), validateMatcher, inputValue);
		}
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.validateAdditiveField(%s, '%s' (%s))", inputCategory.getId(), inputValue.getText(), inputValue.getId()), debugLogging);
		return errorMsg;
	} // end of validateAdditiveField()
	
	
	/*
	 * Validate new Amelioration record
	 * 
	 * field_1 - Must/Juice volume	
	 * field_2 - Must temperature
	 * fieldContainers3 & field_3 - Additive 1	
	 * fieldContainers4 & field_4 - Additive 2	
	 * fieldContainers5 & field_5 - Additive 3	
	 * fieldContainers6 & field_6 - Additive 4	
	 * fieldContainers7 & field_7 - Additive 5	
	 * fieldContainers8 & field_8 - Additive 6	
	 */
	@SuppressWarnings("unused")
	private Validation validateAmelioration()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateAmelioration()", debugLogging);
		
		Validation checkResults = Validation.PASSED;
		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		BiFunction<String, String, String> buildItemDisplay = 
				(itemName, itemId) -> (itemId.length() > 0) ? 
						String.format("%s (%s)", itemId, itemName) : itemName;

		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_1.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_2.getText())) ?
				String.format(volumeMsg, fieldLabel_2.getText(), field_2.getText()) : "";

		errorMsg += (usedSourceContainers.getValue() == null) ? 
				(String.format("Source container was not selected%n")) : "";

		errorMsg += validateAdditiveField(fieldContainers3, field_3);
		errorMsg += validateAdditiveField(fieldContainers4, field_4);
		errorMsg += validateAdditiveField(fieldContainers5, field_5);
		errorMsg += validateAdditiveField(fieldContainers6, field_6);
		errorMsg += validateAdditiveField(fieldContainers7, field_7);
		errorMsg += validateAdditiveField(fieldContainers8, field_8);
		
		if (errorMsg.length() > 0)
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED;
		}
		
		winemakerLogger.writeLog("<< FermentDataDetailController.validateAmelioration()", debugLogging);
		return checkResults;				
	} // end of validateAmelioration()
	
	/*
	 *	Validate new Bottle record
	 *	fieldLabel_1 > fieldContainers = source container
	 *	fieldLabel_2 > field_3 = Bottle count
	 */
	@SuppressWarnings("unused")
	private Validation validateBottle()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateBottle()", debugLogging);

		Validation checkResults = Validation.PASSED; 
		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		
		errorMsg += (sourceContainerCount < 1) ? 
				"At least one source container must be selected\n" : "";
		errorMsg += (!HelperFunctions.validateInteger(field_3.getText())) ? 
				String.format(volumeMsg, fieldLabel_2.getText(), field_3.getText()) : "";

		if (errorMsg.length() > 0)
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED;
		}
		
		winemakerLogger.writeLog("<< FermentDataDetailController.validateBottle()", debugLogging);
		return checkResults;
	} // end of validateBottle()
	
	/*
	 * Validate new Checkpoint record
	 *  field_6 = Total stage volume
	 *	field_1 = Tested volume
	 *	field_2 = Juice Temp
	 *	field_3 = Brix
	 *	field_4 = pH
	 *	field_5 = TA
	 */
	@SuppressWarnings("unused")
	private Validation validateCheckpoint() 
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateCheckpoint()", debugLogging);
	
		Validation checkResults = Validation.PASSED; 
		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		
		field_1.setText(testForEmptyVolume.apply(field_1.getText()));
		field_2.setText(testForEmptyTemp.apply(field_2.getText()));
		field_3.setText(testForEmptyField.apply(field_3.getText()));
		field_4.setText(testForEmptyField.apply(field_4.getText()));
		field_5.setText(testForEmptyField.apply(field_5.getText()));
		
		errorMsg += (usedSourceContainers.getValue() == null) ? 
				(String.format("Source container was not selected%n")) : "";

		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_6.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_6.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_2.getText(), field_1.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_2.getText())) ?
				String.format(volumeMsg, fieldLabel_3.getText(), field_2.getText()) : "";

		errorMsg += (!HelperFunctions.validateDouble(field_3.getText())) ?
				String.format(volumeMsg, fieldLabel_4.getText(), field_3.getText()) : "";
		errorMsg += (!HelperFunctions.validateDouble(field_4.getText())) ?
				String.format(volumeMsg, fieldLabel_5.getText(), field_4.getText()) : "";
		errorMsg += (!HelperFunctions.validateDouble(field_5.getText())) ?
				String.format(volumeMsg, fieldLabel_6.getText(), field_5.getText()) : "";
		
		if (errorMsg.length() > 0)
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED;
		}
	
		winemakerLogger.writeLog("<< FermentDataDetailController.validateCheckpoint()", debugLogging);
		return checkResults;
	} // end of validateCheckpoint()

	/*
	 * Validate new Crush and Destem record
	 * 
	 * field-1 = input grape weight 
	 * field-2 = output must volume 
	 * field-3 = temperature of must
	 * emptyTargetContainers = ComboBox for eligible containers
	 */
	@SuppressWarnings("unused")
	private Validation validateCrush()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateCrush()", debugLogging);
		
		Validation checkResults = Validation.PASSED;
		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
	
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_1.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_2.getText())) ?
				String.format(volumeMsg, fieldLabel_2.getText(), field_2.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_3.getText())) ?
				String.format(volumeMsg, fieldLabel_3.getText(), field_3.getText()) : "";
		
		errorMsg += (targetContainerCount < 1) ? 
				"At least one target container must be selected" : "";	
		
		if (errorMsg.length() > 0)
		{
			winemakerLogger.displayAlert(errorMsg);
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.validateCrush(): validation error '%s'", errorMsg), debugLogging);

			checkResults = Validation.FAILED;			
		}
		
		winemakerLogger.writeLog("<< FermentDataDetailController.validateCrush()", debugLogging);
		return checkResults;
	} // end of validateCrush()

	/*
	 * Validate new Press record
	 * 
	 * field_1 = must volume
	 * emptyTargetContainers = ComboBox for eligible containers
	 * emptyTargetContainers = ComboBox for eligible containers
	 * field_4 = container 1 count
	 * field_5 = container 2 count
	 * field_6 = container 3 count
	 */
	@SuppressWarnings("unused")
	private Validation validatePress()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validatePress()", debugLogging);
	
		Validation checkResults = Validation.PASSED;

		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_1.getText()) : "";

		errorMsg += (sourceContainerCount < 1) ? 
				"At least one source container must be selected\n" : "";		
		errorMsg += (targetContainerCount < 1) ? 
				"At least one target container must be selected" : "";		
		
		if (errorMsg.length() > 0) 
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED; 
		}
		
		winemakerLogger.writeLog("<< FermentDataDetailController.validatePress()", debugLogging);
		return checkResults;
	} // end of validatePress()

	/*
	 * Validate new Rack or Transfer record
	 * 
	 * sourceContainerCount = integer count of selected source containers
	 * targetContainerCount = integer count of selected target containers
	 * field_1 = output volume
	 */
	@SuppressWarnings("unused")
	private Validation validateTransfer()
	{
		return validateRack();
	}

	private Validation validateRack()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateRack()", debugLogging);
	
		Validation checkResults = Validation.PASSED;

		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_1.getText()) : "";

		errorMsg += (sourceContainerCount < 1) ? "At least one source container must be selected" : "";		
		errorMsg += (targetContainerCount < 1) ? "At least one target container must be selected" : "";		
		
		if (errorMsg.length() > 0) 
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED; 
		}
		
		winemakerLogger.writeLog("<< FermentDataDetailController.validateRack()", debugLogging);
		return checkResults;
	} // end of validateRack()
	
	/*
	 * Validate new Yeast Pitch record
	 * 
	 * row 2: fieldLabel_0: field_1 - Juice volume
	 * row 3: fieldLabel_1: field_2 - Must temperature
	 * row 4: fieldLabel_2: usedContainers - Container being adjusted
	 * row 5: fieldLabel_3: fieldContainers3 & field_3 - Yeast type and amount	
	 * row 6: fieldLabel_4: field_4 = Go Ferm amt
	 * row 7: fieldLabel_5: field_5 = FT Rouge amt
	 * row 8: fieldLabel_6: field_7 = Fermaid K amt	
	 * row 9: fieldLabel_7: field_7 = Fermaid O amt	
	 * row 10: fieldLabel_8: field_8 = Opti-Red amt
	 */
	@SuppressWarnings("unused")
	private Validation validateYeastPitch()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.validateYeastPitch()", debugLogging);
	
		Validation checkResults = Validation.PASSED;
		String errorMsg = validateDefaultFields(TimeCheck.ONLYENTRY);
		
		errorMsg += (usedSourceContainers.getValue() == null) ? 
				String.format("Identify the target container%n") : "";		
		errorMsg += (fieldContainers3.getValue() == null) ? 
				String.format("Select Yeast%n") : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_1.getText())) ?
				String.format(volumeMsg, fieldLabel_0.getText(), field_1.getText()) : "";
		errorMsg += (!HelperFunctions.validateVolumeWeightTemp(field_2.getText())) ?
				String.format(volumeMsg, fieldLabel_1.getText(), field_2.getText()) : "";
		
		field_4.setText(testForEmptyField.apply(field_4.getText()));
		field_5.setText(testForEmptyField.apply(field_5.getText()));
		field_6.setText(testForEmptyField.apply(field_6.getText()));
		field_7.setText(testForEmptyField.apply(field_7.getText()));
		field_8.setText(testForEmptyField.apply(field_8.getText()));
		
		errorMsg += validateAdditiveField(fieldContainers3, field_3);
		errorMsg += validateAdditiveField(fieldContainers4, field_4);
		errorMsg += validateAdditiveField(fieldContainers5, field_5);
		errorMsg += validateAdditiveField(fieldContainers6, field_6);
		errorMsg += validateAdditiveField(fieldContainers7, field_7);
		errorMsg += validateAdditiveField(fieldContainers8, field_8);

		if (errorMsg.length() > 0)
		{
			winemakerLogger.displayAlert(errorMsg);
			checkResults = Validation.FAILED;			
		}
	
		winemakerLogger.writeLog("<< FermentDataDetailController.validateYeastPitch()", debugLogging);
		return checkResults;
	} // end of validateYeastPitch()

	/*
	 * Generate the key TimeStamp value and the Notes field.  
	 * Optionally, process the input starting and ending times of this activity. 
	 */
	private WineMakerFerment loadDefaultRecordFields(WineMakerFerment wmf)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadDefaultRecordFields('%s')", wmf.get_batchKey()), debugLogging);
	
		Integer stageCycle = this.getMaxStage.get();
		
		wmf.set_batchKey(this.wmk.get_batchKey());
		wmf.set_stageCycle(stageCycle);		
		wmf.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 0));
				
		if (fieldNotes.getText().length() > 0)
			wmf.set_fermentNotes(fieldNotes.getText());
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadDefaultRecordFields('%s')", wmf.get_batchKey()), debugLogging);
		return wmf;
		
	} // end of loadDefaultRecordFields()

	/*
	 * activity 'amel'
	 * field_1 - Must/Juice vol	
	 * field_2 - Must temp
	 * fieldContainers & field_3 - Additive 1	
	 * fieldContainers2 & field_4 - Additive 2	
	 * fieldContainers3 & field_5 - Additive 3	
	 * fieldContainers4 & field_6 - Additive 4	
	 * fieldContainers5 & field_7 - Additive 5	
	 * fieldContainers6 & field_8 - Additive 6	
	 * 
	 * Create an array of values from the individual chem fields.  The keys will the chemical code, like "goferm", 
	 * to store the selected chemical, and the chem code suffixed with "-m", like "goferm-m", to store the measurement code, like "g". 
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadAmeliorationRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadAmeliorationRecord()"), debugLogging);
		
		WineMakerFerment wmfNull = null;
		String chemKey;
		
		HashMap<String, String> valuesMap = new HashMap<>();
		HashMap<String, String> fieldMap = new HashMap<>();
		
		fieldMap.put(fieldContainers3.getValue(), field_3.getText());
		fieldMap.put(fieldContainers4.getValue(), field_4.getText());
		fieldMap.put(fieldContainers5.getValue(), field_5.getText());
		fieldMap.put(fieldContainers6.getValue(), field_6.getText());
		fieldMap.put(fieldContainers7.getValue(), field_7.getText());
		fieldMap.put(fieldContainers8.getValue(), field_8.getText());
	
		fieldMap.keySet()
			.stream()
			.filter(key -> key != null)
			.collect(Collectors.toSet())
			.forEach(key -> collectChemicalEntry(key, fieldMap.get(key), valuesMap));
		
		ArrayList<WineMakerFerment> wmfCollectFields = loadChemAdditions(valuesMap, field_1.getText(), field_2.getText(), HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 0));
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		
		for (WineMakerFerment wmfChem : wmfCollectFields)
		{
			if (winemakerModel.insertFermentData(wmfChem)) 
			{
				statusDisplay.appendText(String.format("Fermentation Amelioration %s added successfully%n", codeSet.get(wmfChem.get_chemAdded())));
			}
			else
			{
				statusDisplay.appendText(String.format("Failure adding amelioration records %s%n", codeSet.get(wmfChem.get_chemAdded())));
				winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadAmeliorationRecord(): Failure adding amelioration records %s", wmfChem), debugLogging);
			}
		}
	
		this.wmiInsertSet
			.stream()
			.forEach(wmiNew -> winemakerModel.insertInventory(wmiNew));
		
		wmiUpdateSet
			.stream()
			.forEach(wmiUpdate -> winemakerModel.updateInventory(wmiUpdate));
	
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadAmeliorationRecord()"), debugLogging);
		
		return wmfNull;
	} // end of loadAmeliorateRecords()

	/*
	 * Load a new Bottle record
	 * 
 	 * fieldContainers = ComboBox for source container
	 * field_3 = bottle count
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadBottleRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadBottleRecord()"), debugLogging);
		
		WineMakerFerment wmfBottle = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));
		
		int updateIndex = 0;
		for (WineMakerInventory ss: updateInventoryBatch)
		{
			updateIndex++;
			if (updateIndex == 1)
				wmfBottle.set_containerType(ss.get_itemName());
			if (updateIndex == 2)
				wmfBottle.set_containerType2(ss.get_itemName());
			if (updateIndex == 3)
				wmfBottle.set_containerType3(ss.get_itemName());
		}
		
		wmfBottle.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 5));
		wmfBottle.set_fermentActivity(ActivityName.BOTTLE.getValue());
		wmfBottle.set_bottleCount(Integer.parseInt(field_3.getText()));
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadBottleRecord(): new record: %s", wmfBottle), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadBottleRecord()"), debugLogging);
		return wmfBottle;
	} // end of loadBottleRecord()
	
	/*
	 *	Load a new Checkpoint record
	 * 
	 *  field_6 = Total stage volume
	 *	field_1 = Tested volume
	 *	field_2 = Temp
	 *	field_3 = Brix
	 *	field_4 = pH
	 *	field_5 = TA
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadCheckpointRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadCheckPointRecord()"), debugLogging);
			
		WineMakerFerment wmfCheckpoint = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));
		Integer stageCycle = wmfCheckpoint.get_stageCycle();
		
		wmfCheckpoint.set_fermentActivity(ActivityName.CHECKPOINT.getValue());
		wmfCheckpoint.set_currentTemp(Integer.parseInt(extractVolumeOrTemp(field_2.getText(), 1, matchTempPattern)));
		wmfCheckpoint.set_tempScale(extractVolumeOrTemp(field_2.getText(), 2, matchTempPattern).toLowerCase());
		wmfCheckpoint.set_currBrix(Double.parseDouble(field_3.getText()));
		wmfCheckpoint.set_currpH(Double.parseDouble(field_4.getText()));
		wmfCheckpoint.set_currTA(Double.parseDouble(field_5.getText()));
		wmfCheckpoint.set_currentStageJuiceVol(Integer.parseInt(extractVolumeOrTemp(field_6.getText(), 1, matchAmountPattern)));
		wmfCheckpoint.set_currentStageJuiceScale(extractVolumeOrTemp(field_6.getText(), 2, matchAmountPattern).toLowerCase());

		if (stageCycle == 1)
			wmfCheckpoint.set_outputMustVolume(wmfCheckpoint.get_currentStageJuiceVol());
		if (stageCycle == 2)
			wmfCheckpoint.set_outputJuiceVol(wmfCheckpoint.get_currentStageJuiceVol());
		
		Integer testedVolume = Integer.parseInt(extractVolumeOrTemp(field_1.getText(), 1, matchAmountPattern));
		String showTestVolume = (testedVolume > 0) ? String.format("%nTested volume was %s", field_1.getText()) : "";
		String updatedNotes = (field_1.getText() != null && field_1.getText().length() > 0) ? 
				wmfCheckpoint.get_fermentNotes() + showTestVolume : wmfCheckpoint.get_fermentNotes();
		updatedNotes += (!fieldNotes.getText().contains(usedSourceContainers.getValue())) ?
				String.format("%nContainer tested: %s%n", usedSourceContainers.getValue()) : "";
		
		wmfCheckpoint.set_fermentNotes(updatedNotes);
		wmfCheckpoint.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(field_6.getText(), 2, matchAmountPattern)));
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadCheckpointRecord(): new record: %s", wmfCheckpoint), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadCheckPointRecord()"), debugLogging);
		return wmfCheckpoint;
	} // end of loadCheckPointRecord()
	
	/*
	 * Create new Crush record
	 * Use the batch's initial stage record as the time-stamp reference to ensure sequential order
	 * 
	 * field_1 = input grape weight 
	 * field-2 = output must volume 
	 * field_3 = temperature of must
	 * fieldContainers = ComboBox for 1st container
	 * fieldContainers2 = ComboBox for 2nd container
	 * fieldContainers3 = ComboBox for 3rd container
	 * field_4 = container 1 count
	 * field_5 = container 2 count
	 * field_6 = container 3 count
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadCrushRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadCrushRecord()"), debugLogging);
	
		WineMakerFerment wmfCrush = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));

		Integer stageCycle = wmfCrush.get_stageCycle();
		
		wmfCrush.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 5));
		wmfCrush.set_fermentActivity(ActivityName.CRUSH.getValue());
		wmfCrush.set_inputGrapeAmt(Integer.parseInt(extractVolumeOrTemp(field_1.getText(), 1, matchAmountPattern)));
		wmfCrush.set_chemScale(getScaleKey(extractVolumeOrTemp(field_1.getText(), 2, matchAmountPattern)));
		wmfCrush.set_outputMustVolume(Integer.parseInt(extractVolumeOrTemp(field_2.getText(), 1, matchAmountPattern)));
		wmfCrush.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(field_2.getText(), 2, matchAmountPattern)));
		wmfCrush.set_currentTemp(Integer.parseInt(extractVolumeOrTemp(field_3.getText(), 1, matchTempPattern)));
		wmfCrush.set_tempScale(extractVolumeOrTemp(field_3.getText(), 2, matchTempPattern).toLowerCase());
		wmfCrush.set_stageCycle(++stageCycle);
		wmfCrush.set_fermentNotes(collectContainerNotes(wmfCrush, fieldNotes));
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadCrushRecord(): new record: %s", wmfCrush), debugLogging);
		return wmfCrush;
	} // end of loadCrushRecord()

	/*
	 * Create new Press activity record
	 * 
	 * This activity specifies an output volume of juice.   If the ferment activity record for this
	 * stage has already been created, update with the volume number from this activity.
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadPressRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadPressRecord()"), debugLogging);
		
		WineMakerFerment wmfPress = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));

		Integer stageCycle = wmfPress.get_stageCycle();
		
		wmfPress.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 5));
		wmfPress.set_fermentActivity(ActivityName.PRESS.getValue());
		wmfPress.set_outputJuiceVol(Integer.parseInt(extractVolumeOrTemp(field_1.getText(), 1, matchAmountPattern)));
		wmfPress.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(field_1.getText(), 2, matchAmountPattern)));
		wmfPress.set_stageCycle(++stageCycle);
		wmfPress.set_fermentNotes(collectContainerNotes(wmfPress, fieldNotes));

		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadPressRecord(): new record: %s", wmfPress), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadPressRecord()"), debugLogging);
		return wmfPress;
	} // end of loadPressRecord()

	/*
	 * Create new Rack activity record
	 * 
	 * field_1 = output volume
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadTransferRecord()
	{
		return loadRackRecord();
	}
	
	private WineMakerFerment loadRackRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadRackRecord()"), debugLogging);
		
		WineMakerFerment wmfRack = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));

		Integer stageCycle = wmfRack.get_stageCycle();
		
		wmfRack.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 5));
		wmfRack.set_fermentActivity(ActivityName.RACK.getValue());
		wmfRack.set_outputJuiceVol(Integer.parseInt(extractVolumeOrTemp(field_1.getText(), 1, matchAmountPattern)));
		wmfRack.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(field_1.getText(), 2, matchAmountPattern)));
		wmfRack.set_stageCycle(++stageCycle);
		wmfRack.set_fermentNotes(collectContainerNotes(wmfRack, fieldNotes));
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadRackRecord(): new record: %s", wmfRack), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadRackRecord()"), debugLogging);
		return wmfRack;
	} // end of loadRackRecord()

	/*
	 * row 2: fieldLabel_0: field_1 - Juice volume
	 * row 3: fieldLabel_1: field_2 - Must temperature
	 * row 4: fieldLabel_2: usedContainers - Container being adjusted
	 * row 5: fieldLabel_3: fieldContainers3 & field_3 - Yeast type and amount	
	 * row 6: fieldLabel_4: field_4 = Go Ferm amt
	 * row 7: fieldLabel_5: field_5 = Opti-Red amt
	 * row 8: fieldLabel_6: field_6 = FT Rouge amt	
	 * row 9: fieldLabel_7: field_7 = Fermaid K amt	
	 * row 10: fieldLabel_8: field_8 = Fermaid O amt
	 */
	@SuppressWarnings("unused")
	private WineMakerFerment loadYeastPitchRecord()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadYeastPitchRecord()"), debugLogging);
	
		String updatedNotes = "";
		WineMakerFerment wmfYeastPitch = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));
		
		wmfYeastPitch.set_fermentActivity(ActivityName.YEASTPITCH.getValue());
		
		wmfYeastPitch.set_outputMustVolume(Integer.parseInt(extractVolumeOrTemp(field_1.getText(), 1, matchAmountPattern)));
		wmfYeastPitch.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(field_1.getText(), 2, matchAmountPattern)));
		wmfYeastPitch.set_currentTemp(Integer.parseInt(extractVolumeOrTemp(field_2.getText(), 1, matchTempPattern)));
		wmfYeastPitch.set_tempScale(extractVolumeOrTemp(field_2.getText(), 2, matchTempPattern).toLowerCase());
		wmfYeastPitch.set_yeastStrain(HelperFunctions.getCodeValueEntry(FamilyCode.YEASTFAMILY.getValue(), fieldContainers3.getValue()));	
		wmfYeastPitch.set_starterYeastAmt(Double.parseDouble(extractVolumeOrTemp(field_3.getText(), 1, matchAmountPattern)));
		wmfYeastPitch.set_chemScale(getScaleKey(extractVolumeOrTemp(field_3.getText(), 2, matchAmountPattern)));		
		wmfYeastPitch.set_fermentNotes(collectContainerNotes(wmfYeastPitch, fieldNotes));
		
		loadInventoryRecords(wmfYeastPitch, HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue()), wmfYeastPitch.get_yeastStrain());
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadYeastPitchRecord(): new record: %s", wmfYeastPitch), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadYeastPitchRecord()"), debugLogging);
		return wmfYeastPitch;
	} // end of loadYeastPitchRecord()

	/*
	 * row 2: fieldLabel_0: field_1 - Juice volume
	 * row 3: fieldLabel_1: field_2 - Must temperature
	 * row 4: fieldLabel_2: usedContainers - Container being adjusted
	 * row 5: fieldLabel_3: fieldContainers3 & field_3 - Yeast type and amount	
	 * row 6: fieldLabel_4: field_4 = Go Ferm amt
	 * row 7: fieldLabel_5: field_5 = FT Rouge amt
	 * row 8: fieldLabel_6: field_7 = Fermaid K amt	
	 * row 9: fieldLabel_7: field_7 = Fermaid O amt	
	 * row 10: fieldLabel_8: field_8 = Opti-Red amt
	 * 
	 */
	private ArrayList<WineMakerFerment> loadYeastPitchAdjustmentRecords(Timestamp parentEntryDate)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadYeastPitchAdjustmentRecords()"), debugLogging);
	
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		HashMap<String, String> fieldMap = new HashMap<>();
		HashMap<String, String> valuesMap = new HashMap<>();
	
		fieldMap.put(codeSet.get(YeastPitchDefaults.GOFERM.getValue()), field_4.getText());
		fieldMap.put(codeSet.get(YeastPitchDefaults.FTROUGE.getValue()), field_5.getText());
		fieldMap.put(codeSet.get(YeastPitchDefaults.FERMAIDK.getValue()), field_6.getText());
		fieldMap.put(codeSet.get(YeastPitchDefaults.FERMAIDO.getValue()), field_7.getText());
		fieldMap.put(codeSet.get(YeastPitchDefaults.OPTIRED.getValue()), field_8.getText());
		
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue());
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadYeastPitchAdjustmentRecords(): calling collectChemicalEntry() for %s", fieldMap), debugLogging);

		fieldMap.keySet()
			.stream()
			.filter(key -> key != null)
			.collect(Collectors.toSet())
			.forEach(key -> collectChemicalEntry(key, fieldMap.get(key), valuesMap));
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadYeastPitchAdjustmentRecords()"), debugLogging);
		return loadChemAdditions(valuesMap, field_1.getText(), field_2.getText(), parentEntryDate);
	} // end of loadYeastPitchAdjustmentRecords()

	/*
	 * Add a UI chemical entry to the collection of field values
	 * 		'Enzymes' = "2.4', 'Enzymes-m' = 'mL'
	 */
	private void collectChemicalEntry(String chemName, String amountValue, HashMap<String, String> valuesMap)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.captureChemicalEntry(chemName '%s', value '%s', valuesMap)", chemName, amountValue), debugLogging);
	
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		Optional<String> codeValue = codeSet.keySet()
				.stream()
				.filter(codeKey -> chemName.equals(codeSet.get(codeKey)))
				.findFirst();
		String chemKey = codeValue.get();
	
		valuesMap.put(chemKey, extractVolumeOrTemp(amountValue, 1, matchAmountPattern));
		valuesMap.put(chemKey + "-m", extractVolumeOrTemp(amountValue, 2, matchAmountPattern));
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.captureChemicalEntry(chemName %s, value %s, valuesMap)", chemName, amountValue), debugLogging);
	} // end of captureChemicalEntry()

	/*
	 * Convert the UI chemical values into WineMakerFerment and WineMakerInventory objects.
	 * Skip the label entries and yeast entries, which are included to activate inventory checking 
	 * 
	 */
	private ArrayList<WineMakerFerment> loadChemAdditions(HashMap<String, String> chemAmounts, String targetVol, String currTemp, Timestamp parentEntryTime)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadChemAdditions('%s', '%s', '%s')", chemAmounts.toString(), targetVol, currTemp), debugLogging);
	
		ArrayList<WineMakerFerment> wmfSets = new ArrayList<WineMakerFerment>();
		WineMakerFerment wmf;
		int timeTweak = 0;

		Integer stageCycle = this.getMaxStage.get();
				 
		for (String chemName : chemAmounts.keySet())
		{	
			if (chemName.endsWith("-m"))
				continue;
			
			if (Double.parseDouble(chemAmounts.get(chemName)) > 0.0)
			{
				winemakerLogger.writeLog(String.format("      FermentDataDetailController.loadChemAdditions(...): %s added, amt = %s", 
						chemName, Double.parseDouble(chemAmounts.get(chemName))), debugLogging);
	
				String updatedNotes = "";
				wmf = loadDefaultRecordFields(new WineMakerFerment(this.winemakerModel));
				
				wmf.set_entry_date(HelperFunctions.buildTimeStamp(parentEntryTime, ++timeTweak));
				wmf.set_fermentActivity(ActivityName.AMELIORATION.getValue());
								
				wmf.set_outputMustVolume(Integer.parseInt(extractVolumeOrTemp(targetVol, 1, matchAmountPattern)));
				wmf.set_outputJuiceScale(getScaleKey(extractVolumeOrTemp(targetVol, 2, matchAmountPattern)));
				wmf.set_currentTemp(Integer.parseInt(extractVolumeOrTemp(currTemp, 1, matchTempPattern)));
				wmf.set_tempScale(extractVolumeOrTemp(currTemp, 2, matchTempPattern).toLowerCase());
				
				wmf.set_chemAdded(chemName);
				wmf.set_chemAmount(Double.parseDouble(chemAmounts.get(chemName)));
				wmf.set_chemScale(getScaleKey(chemAmounts.get(chemName + "-m")));
				wmf.set_stageCycle(stageCycle);
				
				updatedNotes += (fieldNotes.getText().length() > 0) ?
						"\n" : "";
				updatedNotes += (!fieldNotes.getText().contains(usedSourceContainers.getValue())) ?
						String.format("Container adjusted: %s%n", usedSourceContainers.getValue()) : "";
				fieldNotes.appendText(updatedNotes);
				
				wmf.set_fermentNotes(fieldNotes.getText());
				wmfSets.add(wmf);
				
				loadInventoryRecords(wmf, HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue()), chemName);
				winemakerLogger.writeLog(String.format("   FermentDataDetailController.loadChemAdditions(...): new rec on queue %s", wmf), debugLogging);
			}
		}
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadChemAdditions(...)"), debugLogging);
		return wmfSets;
	} // end of loadChemAdditions()

	/*
	 * For the current Fermentation activity, create Inventory entries reflecting inventory reductions for
	 * all chemicals used in the activity
	 * 
	 * Issue user alert if item inventory is reduced to 0
	 */
	private void loadInventoryRecords(WineMakerFerment wmf, HashMap<String, String> codeSet, String wmfItemName)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.loadInventoryRecords('%s%s')", wmf.get_yeastStrain(), wmf.get_chemAdded()), debugLogging);
		ArrayList<WineMakerInventory> wmiQuerySet = new ArrayList<WineMakerInventory>(8);
	
		wmiQuerySet = winemakerModel.queryInventory(wmfItemName, "");
		
		Optional<WineMakerInventory> wmiParent = wmiQuerySet
				.stream()
				.filter(oldWmi -> oldWmi.get_itemName().equals(wmfItemName))
				.filter(assetUse -> assetUse.getItemTaskId().isEmpty())
				.findFirst();

		double stockOnHand = (wmiParent.isPresent()) ?  
				wmiParent.get().get_itemStockOnHand() : 0.0;
		stockOnHand -= (wmf.get_chemAmount() + wmf.get_starterYeastAmt());
				
		if (stockOnHand <= 0)
		{
			stockOnHand = 0;
			winemakerLogger.displayAlert(String.format("Note: No inventory for %s", codeSet.get(wmfItemName)));
		}
		else
		{
			stockOnHand = Math.floor(stockOnHand * 10000) / 10000;

			wmiParent.get().set_itemStockOnHand(stockOnHand);
			wmiUpdateSet.add(wmiParent.get());			
		}
		
		WineMakerInventory wmi = new WineMakerInventory();
		wmi = new WineMakerInventory();
		wmi.set_itemName(wmf.get_chemAdded());
		wmi.setItemTaskTime(wmf.get_entry_date());
		wmi.setItemTaskId(wmf.get_fermentActivity());
		wmi.setItemBatchId(wmf.get_batchKey());
		wmi.set_itemActivityAmount(wmf.get_chemAmount());
		wmi.set_itemAmountScale(wmf.get_chemScale());
		wmiInsertSet.add(wmi);
	
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.loadInventoryRecords(%s)", wmf.get_chemAdded()), debugLogging);
	} // end of loadInventoryRecords()

	private String collectContainerNotes(WineMakerFerment wmf, TextArea fieldNotes)	
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.collectContainerNotes()"), debugLogging);		
		
		String rackContainers = "\nContainers used:\nSource:";
		
		List<WineMakerInventory> sourceAssets = updateInventoryBatch.stream()
				.filter(wmi -> wmi.getItemBatchId().length() == 0)
				.collect(Collectors.toList());
		List<WineMakerInventory> targetAssets = updateInventoryBatch.stream()
				.filter(wmi -> wmi.getItemBatchId().length() > 0)
				.collect(Collectors.toList());
			
		if (sourceAssets.size() > 0)
		{
			rackContainers += "\n\tSource:";

			for (WineMakerInventory sourceAsset: sourceAssets)
				rackContainers += "\n\t\t" + buildItemDisplay.apply(HelperFunctions.getCodeKeyEntry(FamilyCode.CONTAINERFAMILY.getValue(), sourceAsset.get_itemName()), sourceAsset.getItemId());
		}
		
		if (targetAssets.size() > 0)
		{
			rackContainers += "\n\tTarget:";

			for (WineMakerInventory sourceAsset: targetAssets)
				rackContainers += "\n\t\t" + buildItemDisplay.apply(HelperFunctions.getCodeKeyEntry(FamilyCode.CONTAINERFAMILY.getValue(), sourceAsset.get_itemName()), sourceAsset.getItemId());
		}
		
		rackContainers += "\n";
		
		String updatedNotes = wmf.get_fermentNotes().concat(rackContainers);

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.collectContainerNotes()"), debugLogging);		
		return updatedNotes;
	} // end of collectContainerNotes()

	/*
	 * Submit new ferment log entry.
	 * Activity-specific methods are dynamically called.
	 * All methods return a single Ferment record except for Amelioration and YeastPitch which return a set of records.
	 */
	public void submitNewFermentDataLog() 
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitNewFermentDataLog()"), debugLogging);
		
		if (this.activityCode.length() == 0) 
		{
			winemakerLogger.displayAlert("No activity selected, what do you think should happen?");
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.submitNewFermentDataLog(): error 'No activity selected'"), debugLogging);

			return;
		}
	
		if (activityValidation(this.activityCode).equals(Validation.FAILED))
		{
			statusDisplay.setText("Ferment data not saved due to validation errors");
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.submitNewFermentDataLog(): error in validation"), debugLogging);

			return;
		}
		
		/*
		 * The Amelioration activity consists of a set of similar records, 
		 * so the inserts are done in the activity method code and a null is returned
		 * if no adjustment data was entered
		 */
		WineMakerFerment wmfNew = activityRecordMethodCall(this.activityCode);

		if (wmfNew == null || !winemakerModel.insertFermentData(wmfNew)) 
		{
			winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitNewFermentDataLog()"), debugLogging);
			statusDisplay.setText("No Fermentation activity logged");

			return;
		}

		statusDisplay.setText("Fermentation activity logged successfully\n");

		activitysSupplementalRecordMethodCall(this.activityCode, wmfNew);
		
		/*
		 * The date in the UI might have been set after these records were created by the container button handler code,
		 * so make sure they have a current date value.
		 */
		long timestampTime = 0;
		Calendar myCalendar = Calendar.getInstance();
		
		for (WineMakerInventory wmi: insertInventoryActivity)
		{
			timestampTime = wmi.getItemTaskTime().getTime(); 
			myCalendar.setTimeInMillis(timestampTime);
			
			if (myCalendar.get(Calendar.YEAR) == 1900)
				wmi.setItemTaskTime(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 1));
		}

		updateInventoryStock.stream()
			.forEach(wmiRecord -> winemakerModel.updateInventory(wmiRecord));
		updateInventoryBatch.stream()
			.forEach(wmiRecord -> winemakerModel.updateInventoryBatch(wmiRecord));
		insertInventoryActivity.stream()
			.forEach(wmiRecord -> winemakerModel.insertInventory(wmiRecord));

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitNewFermentDataLog()"), debugLogging);
	} // end of submitNewFermentDataLog()
	
	/*
	 * The Crush, Press activities starts a new stage, so a new Ferment activity record is
	 * created with an incremented cycle number.
	 * In addition, the current stage record's end date is updated with this stage's start date.
	 */
	private void submitNewStageActivity(WineMakerFerment wmfActivity)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitNewStageActivity(): %s", wmfActivity), debugLogging);
		
		Integer stageCycle = this.getMaxStage.get();
		WineMakerFerment wmfStageUpdate = this.getStageRecord.apply(this.wmfSets, stageCycle).get();
		
		WineMakerFerment wmfStart = createNewStageActivity(wmfActivity);

		wmfStageUpdate.set_endDate(wmfStart.get_startDate());

		if (winemakerModel.insertFermentData(wmfStart)) 
		{			
			wmfStageUpdate.set_endDate(wmfStart.get_startDate());
			winemakerModel.updateFermentData(wmfStageUpdate);		
		}
		else
			winemakerLogger.writeLog(String.format("   FermentDataDetailController.submitNewStageActivity('%s'): Failure adding stage record %s", wmfStart), debugLogging);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitNewStageActivity()"), debugLogging);
	} // end of submitNewStageActivity()

	/*
	 * For Bottle activities, set the end date of this stage
	 */
	@SuppressWarnings("unused")
	private void submitExtraBottle(WineMakerFerment wmfBottle)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitExtraBottle()"), debugLogging);

		Integer stageCycle = this.getMaxStage.get();
		WineMakerFerment wmfStageUpdate = this.getStageRecord.apply(this.wmfSets, stageCycle).get();
		
		wmfStageUpdate.set_endDate(wmfBottle.get_entry_date());
		winemakerModel.updateFermentData(wmfStageUpdate);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitExtraBottle()"), debugLogging);
	} // end of submitExtraBottle()

	/*
	 * For Checkpoint activities, update the current stage record with the current checkpoint juice volume (if any)
	 */
	@SuppressWarnings("unused")
	private void submitExtraCheckpoint(WineMakerFerment wmfCheckpoint)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitExtraCheckpoint('%s'): %s", wmfCheckpoint.get_batchKey(), wmfCheckpoint), debugLogging);
	
		Integer stageCycle = this.getMaxStage.get();
		WineMakerFerment wmfStage = this.getStageRecord.apply(this.wmfSets, stageCycle).get();
		
		if (wmfCheckpoint.get_currentStageJuiceVol() > 0)
			wmfStage.set_currentStageJuiceVol(wmfCheckpoint.get_currentStageJuiceVol());
		
		winemakerModel.updateFermentData(wmfStage);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitExtraCheckpoint('%s')", wmfCheckpoint.get_batchKey()), debugLogging);
	} // end of submitExtraCheckpoint()
	
	@SuppressWarnings("unused")
	private void submitExtraCrush(WineMakerFerment wmfCrush)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitExtraCrush(): %s", wmfCrush), debugLogging);

		submitNewStageActivity(wmfCrush);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitExtraCrush()"), debugLogging);
	} // end of submitExtraCrush()

	@SuppressWarnings("unused")
	private void submitExtraPress(WineMakerFerment wmfPress)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitExtraPress(): %s", wmfPress), debugLogging);

		submitNewStageActivity(wmfPress);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitExtraPress()"), debugLogging);
	} // end of submitExtraPress()
	
	/*
	 * The YeastPitch activity has optional data that will generate additional records
	 */
	@SuppressWarnings("unused")
	private void submitExtraYeastPitch(WineMakerFerment wmfYeast)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.submitExtraYeastPitch(): %s", wmfYeast), debugLogging);

		ArrayList<WineMakerFerment> wmfSets = loadYeastPitchAdjustmentRecords(wmfYeast.get_entry_date());
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		
		for (WineMakerFerment wmfAdd : wmfSets)
		{
			if (winemakerModel.insertFermentData(wmfAdd)) 
			{
				statusDisplay.appendText(String.format("Yeast Pitch record for %s was added%n", codeSet.get(wmfAdd.get_chemAdded())));
			}
			else
			{
				statusDisplay.appendText(String.format("Failure adding yeast pitch amelioration records %s%n", codeSet.get(wmfAdd.get_chemAdded())));
				winemakerLogger.writeLog(String.format("Failure adding yeast pitch amelioration records %s", wmfAdd), debugLogging);
			}
		}

		wmiInsertSet
			.stream()
			.forEach(wmiNew -> winemakerModel.insertInventory(wmiNew));
				
		wmiUpdateSet
			.stream()
			.forEach(wmiUpdate -> winemakerModel.updateInventory(wmiUpdate));
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.submitExtraYeastPitch()"), debugLogging);
	} // end of submitExtraYeastPitch()
	
	/*
	 * Certain activities auto-generate a new Stage activity.
	 * The new stage cycle number will have been incremented in the new activity record.
	 */
	private WineMakerFerment createNewStageActivity(WineMakerFerment wmfActivity)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.createNewStageActivity(): %s", wmfActivity), debugLogging);
		
		WineMakerFerment wmfStart = new WineMakerFerment(this.winemakerModel);
		
		Timestamp entryDate = wmfActivity.get_entry_date();
		Timestamp adjustedTime = Timestamp.from(entryDate.toInstant().plusSeconds(-10));
		adjustedTime.setNanos(0);
	
		wmfStart.set_batchKey(wmfActivity.get_batchKey());
		wmfStart.set_entry_date(adjustedTime);
		wmfStart.set_fermentActivity(ActivityName.FERMENT.getValue());
		wmfStart.set_startDate(adjustedTime);
		wmfStart.set_stageCycle(wmfActivity.get_stageCycle());

		if (HelperFunctions.validateVolumeWeightTemp(field_3.getText()))
		{
			wmfStart.set_currentTemp(Integer.parseInt(extractVolumeOrTemp(field_3.getText(), 1, matchTempPattern)));
			wmfStart.set_startTemp(wmfStart.get_currentTemp());
			wmfStart.set_tempScale(extractVolumeOrTemp(field_3.getText(), 2, matchTempPattern).toLowerCase());			
		}
	
		wmfStart.set_outputJuiceScale(wmfActivity.get_outputJuiceScale());

		if (wmfStart.get_stageCycle() == 1)
			wmfStart.set_outputMustVolume(wmfActivity.get_outputMustVolume());
		if (wmfStart.get_stageCycle() == 2)
			wmfStart.set_outputJuiceVol(wmfActivity.get_outputJuiceVol());
	
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.createNewStageActivity(), new record %s", wmfStart), debugLogging);
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.createNewStageActivity()"), debugLogging);
		return wmfStart;
	} // end of createNewStageActivity()

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
	} // end of returnToMain()

	/*
	 * Return the resource key for an amount scale, like 'g/l', that could entered as 'G/L' or 'G/l' 
	 */
	private String getScaleKey(String fieldText)
	{
		codeSet = HelperFunctions.getCodeValueMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		
		Optional<String> valueToKey = codeSet.keySet().stream()
				.filter(compareValue -> compareValue.equalsIgnoreCase(fieldText))
				.map(valueAsKey -> codeSet.get(valueAsKey))
				.findFirst();
		
		return valueToKey.get();
	} // end of getScaleKey()

	/*
	 * Perform Regex match and return group value
	 */
	private String extractVolumeOrTemp(String fieldText, int groupNum, Pattern matchPattern)
	{
		Matcher matcher = matchPattern.matcher(fieldText);
		matcher.find();
	
		return matcher.group(groupNum);
	} // end of extractVolumeOrTemp()

	/*
	 * Add set of resource codes to the collected map
	 * Return a list of the values from the resource key-value pairs
	 */
	private List<String> getResourceValues(HashMap<String, String> resourceCodes)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.getResourceValues('%s')", resourceCodes), debugLogging);

		this.inventoryTypes.putAll(resourceCodes);	

		HashMap<String, String> valueCodeSet = new HashMap<>();
		valueCodeSet.putAll(resourceCodes);
		
		List<String> resourceValueList = valueCodeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.getResourceValues('')"), debugLogging);
		return resourceValueList;
	} // end of getResourceValues()
	
	/*
	 * Process the container selection buttons
	 */
	private void processContainerSelectButton(ActionEvent event)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.processContainerSelectButton()"), debugLogging);
	
		Button containerButton = (Button) event.getSource();
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.processContainerSelectButton(): button '%s', list '%s'", containerButton.getId(), emptyTargetContainers.getValue()), debugLogging);
			
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
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.processContainerSelectButton()"), debugLogging);
	} // end of processContainerSelectButton()

	/*
	 * Update batch id property for selected container inventory record.
	 * Add batch id to a target container, or remove batch id from source container 
	 */
	private void processContainerSelection(ComboBox<String> referencedContainer)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.processContainerSelection()"), debugLogging);
		
		WineMakerInventory inventoryUpdateRecord = HelperFunctions.findAssetItemRecord(this.getLocalInventorySet(),	referencedContainer.getValue()).get(0);
		WineMakerInventory inventoryActivityRecord = inventoryUpdateRecord.createActivityRecord();
		
		Timestamp tempDate = (activityDate.getValue() != null ) ? 
				Timestamp.valueOf(activityDate.getValue().atTime(LocalTime.now())) : Timestamp.valueOf(LocalDate.of(1900, 1, 1).atTime(LocalTime.now()));
		
		inventoryActivityRecord.setItemTaskTime(tempDate);
		inventoryActivityRecord.setItemTaskId(HelperFunctions.getCodeValueEntry(FamilyCode.ACTIVITYFAMILY.getValue(), activitySelect.getValue()));

		if (referencedContainer.getId().equals("dyn-target"))
		{
			displayContainerSelections.appendText("Target '" + referencedContainer.getValue() + "'\n");
			inventoryUpdateRecord.setItemBatchId(this.wmk.get_batchKey());			
		}
		if (referencedContainer.getId().equals("dyn-source"))
		{
			displayContainerSelections.appendText("Source '" + referencedContainer.getValue() + "'\n");
			inventoryUpdateRecord.setItemBatchId("");
		}
		inventoryActivityRecord.setItemBatchId(inventoryUpdateRecord.getItemBatchId());
		
		insertInventoryActivity.add(inventoryActivityRecord);
		updateInventoryBatch.add(inventoryUpdateRecord);
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.processContainerSelection(): created inventory rec: %s", inventoryActivityRecord), debugLogging);
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.processContainerSelection(): updated inventory rec: %s", inventoryUpdateRecord), debugLogging);
		
		ObservableList<String> listEmptyContainers = referencedContainer.getItems();

		listEmptyContainers.remove(referencedContainer.getValue());
		referencedContainer.setItems(listEmptyContainers);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.processContainerSelection()"), debugLogging);
	} // end of processContainerSelection()
	
    /*
	 * Build the default set of UI controls
	 * When building the activity list, skip the Ferment activity
	 */
	private void setUIDefaults()
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.setUIDefaults()"), debugLogging);
	
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ACTIVITYFAMILY.getValue());
		ArrayList<String> keyList = new ArrayList<>(codeSet.keySet());
	
		gp.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		
		this.activitiesList.addAll(keyList
				.stream()
				.filter(code -> uiLoadMethodsMap.get(code) != null)
				.filter(checkCode -> !(checkCode.equals(ActivityName.FERMENT.getValue())))
				.map(code -> codeSet.get(code))
				.sorted()
				.collect(Collectors.toList()));
		
		activitySelect.setItems(this.activitiesList);
		
		this.setLocalInventorySet(winemakerModel.queryInventory());
		this.inventoryCodes.addAll(getResourceValues(HelperFunctions.getCodeKeyMappings().get(FamilyCode.LABFAMILY.getValue())));
		this.inventoryCodes.addAll(getResourceValues(HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue())));
		this.inventoryCodes.addAll(getResourceValues(HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue())));
		this.inventoryCodes.addAll(getResourceValues(HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue())));
	
		/*
		 * Set ids for all of the dynamically-added controls The ids are used to
		 * identify controls to be deleted when activities are switched
		 */
		field_1.setId("dyn-field1");
		field_2.setId("dyn-field2");
		field_3.setId("dyn-field3");
		field_4.setId("dyn-field4");
		field_5.setId("dyn-field5");
		field_6.setId("dyn-field6");
		field_7.setId("dyn-field7");
		field_8.setId("dyn-field8");
		field_9.setId("dyn-field9");
		field_10.setId("dyn-field10");
	
		hbStartTime.setId("dyn-hboxS");
		hbEndTime.setId("dyn-hboxE");
	
		hBox_1.setId("dyn-hbox1");
		hBox_2.setId("dyn-hbox2");
		hBox_3.setId("dyn-hbox3");
		hBox_4.setId("dyn-hbox4");
		hBox_5.setId("dyn-hbox5");
		hBox_6.setId("dyn-hbox6");
		hBox_7.setId("dyn-hbox7");
		hBox_8.setId("dyn-hbox8");
	
		fieldNotes.setId("dyn-notes1");
		notesLabel.setId("dyn-notes2");
	
		usedSourceContainers.setId("dyn-source");
		emptyTargetContainers.setId("dyn-target");
		fieldContainers3.setId("dyn-combo3");
		fieldContainers4.setId("dyn-combo4");
		fieldContainers5.setId("dyn-combo5");
		fieldContainers6.setId("dyn-combo6");
		fieldContainers7.setId("dyn-combo7");
		fieldContainers8.setId("dyn-combo8");
		
		DropShadow dS = new DropShadow();
		usedSourceContainers.setEffect(dS);
		emptyTargetContainers.setEffect(dS);
		fieldContainers3.setEffect(dS);
		fieldContainers4.setEffect(dS);
		fieldContainers5.setEffect(dS);
		fieldContainers6.setEffect(dS);
		fieldContainers7.setEffect(dS);
		fieldContainers8.setEffect(dS);
		
		field_1.setEffect(dS);
		field_2.setEffect(dS);
		field_3.setEffect(dS);
		field_4.setEffect(dS);
		field_5.setEffect(dS);
		field_6.setEffect(dS);
		field_7.setEffect(dS);
		field_8.setEffect(dS);
		field_9.setEffect(dS);
		field_10.setEffect(dS);
		
		addSourceContainerButton.setOnAction(event -> processContainerSelectButton(event));
		addTargetContainerButton.setOnAction(event -> processContainerSelectButton(event));
		addSourceContainerButton.setId("source");
		addTargetContainerButton.setId("target");
		addSourceContainerButton.setText("Add");
		addTargetContainerButton.setText("Add");
		
		displayContainerSelections.setId("dyn-showcontainers");
		
		statusDisplay.setEditable(false);
		displayContainerSelections.setEditable(false);
		
		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.setUIDefaults()"), debugLogging);
	} // end of setUIDefaults()

	private void resetUIFields()
	{
		winemakerLogger.writeLog(">> FermentDataDetailController.resetUIFields()", debugLogging);
		
		String empty = "";
		ArrayList<Node> oldNodes = new ArrayList<>();
	
		fieldLabel_0.setText(empty);
		fieldLabel_1.setText(empty);
		fieldLabel_2.setText(empty);
		fieldLabel_3.setText(empty);
		fieldLabel_4.setText(empty);
		fieldLabel_5.setText(empty);
		fieldLabel_6.setText(empty);
		fieldLabel_7.setText(empty);
		fieldLabel_8.setText(empty);
		fieldLabel_9.setText(empty);
		fieldLabel_10.setText(empty);
		
		field_1.clear();
		field_2.clear();
		field_3.clear();
		field_4.clear();
		field_5.clear();
		field_6.clear();
		field_7.clear();
		field_8.clear();
		field_9.clear();
		field_10.clear();
		
		fieldNotes.clear();
	
		ObservableList<Node> childNodes = this.gp.getChildren();
	
		Predicate<Node> findCustom = node -> node.getId() != null && node.getId().contains("dyn-");
		oldNodes.addAll(childNodes
							.stream()
							.filter(findCustom)
							.collect(Collectors.toList()));
		
		statusDisplay.clear();
		displayContainerSelections.clear();
		
		winemakerLogger.writeLog(String.format("   FermentDataDetailController.resetUIFields(): collected nodes: %s", oldNodes.toString()), debugLogging);
		
		/*
		 * Remove the collected nodes from the GridPane
		 */
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
			else if (oldNode instanceof Label)
			{
				Label labelField = (Label) oldNode;
				gp.getChildren().remove(labelField);
			}
			else if (oldNode instanceof ComboBox<?>)
			{
				@SuppressWarnings("unchecked")
				ComboBox<String> cmbBox = (ComboBox<String>) oldNode;
				gp.getChildren().remove(cmbBox);
			}
			else if (oldNode instanceof HBox)
			{
				HBox hrzBox = (HBox) oldNode;
				hrzBox.getChildren().clear();
				gp.getChildren().remove(hrzBox);
			}
		}
	
		setLocalInventorySet(winemakerModel.queryInventory());
		
		winemakerLogger.writeLog("<< FermentDataDetailController.resetUIFields()", debugLogging);
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
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		setUIDefaults();
		activityDate.setConverter(localDateStringConverter);

		EventHandler<ActionEvent> activitiesHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				winemakerLogger.writeLog(String.format(">> FermentDataDetailController.activityHandler(): activity %s", activitySelect.getValue()), debugLogging);

				resetUIFields();

				activityCode = HelperFunctions.getCodeValueEntry(FamilyCode.ACTIVITYFAMILY.getValue(), activitySelect.getValue());
				activityBuildUI(activityCode);
				String bannerString = (bannerText.getText().length() > 12) ? 
						bannerText.getText().substring(0, 13) : bannerText.getText();
				bannerText.setText(bannerString + " - " + activitySelect.getValue()); 
				
				gp.setVgap(4);
				gp.add(notesLabel, 0, 12);
				gp.add(fieldNotes, 1, 12);
				GridPane.setValignment(notesLabel, VPos.TOP);
				GridPane.setValignment(fieldNotes, VPos.TOP);

				winemakerLogger.writeLog(String.format("<< FermentDataDetailController.activityHandler(): activity %s", activitySelect.getValue()), debugLogging);
			}
		};
		activitySelect.setOnAction(activitiesHandler);
	}
}