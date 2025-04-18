package geo.apps.winemaker;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import geo.apps.winemaker.utilities.WineMakerLogging;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;
import geo.apps.winemaker.utilities.Constants.SQLSearch;
import geo.apps.winemaker.utilities.HelperFunctions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TestDataDetailController implements Initializable {
	
	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;
	private WineMakerLog wmk = null;
	
	private boolean debugLogging = true;
	
	private HashMap<String, String> codeSet = null;

	private final String regexDouble = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([A-BD-EG-bd-eg-z%\\/]*)$";
	private final String regexTemp = "^([-+]?[0-9]*\\.?[0-9]+)\\s*([cCfF]*)$";
	private Pattern matchAmountPattern = Pattern.compile(regexDouble);
	private Pattern matchTempPattern = Pattern.compile(regexTemp);

	@FXML ComboBox<String> testSelect;
	ObservableList<String> testsList = FXCollections.observableArrayList();
	@FXML ComboBox<String> containerSelect;
	ObservableList<String> containersList = FXCollections.observableArrayList();

	@FXML HBox dateHBox;
	@FXML DatePicker activityDate;
	@FXML TextField entryTime = new TextField();
	@FXML TextField testValue = new TextField();
	@FXML TextField testTemp = new TextField();
	@FXML TextArea testNotes = new TextArea();
	@FXML TextArea statusDisplay = new TextArea();
	
	public TestDataDetailController() {
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);
	}

	/*
	 * Set the parent batch
	 * Load set of connected containers
	 */
	public void setBatchKey(String batchKey) 
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.setBatchKey(batchKey '%s')", batchKey), debugLogging);

		this.wmk = winemakerModel.queryBatch(batchKey, SQLSearch.PARENTBATCH).get(0);

		containerSelect.setItems(buildSourceContainerSet(getBatchKey()));
		
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.setBatchKey(batchKey '%s')", batchKey), debugLogging);
		return;
	} // end of setBatchKey()
	
	private String getBatchKey()
	{
		return this.wmk.get_batchKey();
	}
	
	private LocalTime parseTimeString(String uiTime, String fieldName)
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.parseTimeString(uiDate '%s', fieldName '%s')", uiTime, fieldName), debugLogging);

		String timeColonPattern = "h:mm a";
		LocalTime parsedTime = LocalTime.now();
		DateTimeFormatter timeParser = DateTimeFormatter.ofPattern(timeColonPattern);
		
		if (uiTime != null)
		{
			try
			{
				if (uiTime.equalsIgnoreCase("now"))
					parsedTime = LocalTime.now();
				else
					parsedTime = LocalTime.parse(uiTime.toUpperCase(), timeParser);
			}
			catch (DateTimeParseException de)
			{}
		}
		
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.parseTimeString(uiDate '%s', fieldName '%s')", uiTime, fieldName), debugLogging);
		return parsedTime;
	} // end of parseTimeString()
	
	private String extractPattern(String fieldText, int groupNum, Pattern matchPattern)
	{
		winemakerLogger.writeLog(String.format(">> FermentDataDetailController.extractPattern('%s', %d)", fieldText, groupNum), debugLogging);

		Matcher matcher = matchPattern.matcher(fieldText);
		matcher.find();

		winemakerLogger.writeLog(String.format("<< FermentDataDetailController.extractPattern('%s', %d)", fieldText, groupNum), debugLogging);
		return matcher.group(groupNum);
	} // end of extractPattern()
	
	/*
	 * 
	 */
	private WineMakerTesting validateFields()
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.validateFields()"), debugLogging);

		boolean scaleRequired = true;
		String validateMessages = "";
		
		if (entryTime == null || entryTime.getText().length() == 0)
			entryTime.setText("now");
	
		validateMessages += (activityDate.getValue() == null) ? 
				"Entry date was not selected\n" : "";
		validateMessages += (parseTimeString(entryTime.getText(), "Entry time") == null) ? 
				"Entry time was not entered or was invalid\n" : "";
		validateMessages += (testSelect.getValue() == null) ? 
				"A test selection must be made\n" : "";
		validateMessages += (containerSelect.getValue() == null) ? 
				(String.format("Source container was not selected%n")) : "";
		validateMessages += validateChemField("Test Value", testValue.getText(), matchAmountPattern, !scaleRequired);
		validateMessages += validateChemField("Temperature", testTemp.getText(), matchTempPattern, scaleRequired);
		
		if (validateMessages.length() > 0)
		{
			winemakerLogger.displayAlert(String.format(validateMessages));
			return null;
		}
				
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.validateFields()"), debugLogging);
		return new WineMakerTesting(winemakerModel);
	} // end of validateFields()
	
	private String validateChemField(String fieldName, String fieldValue, Pattern matchPattern, boolean scaleRequired)
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.validateChemField('%s', '%s', '%s')", fieldName, fieldValue, matchPattern.pattern()), debugLogging);
	
		Matcher matcher = matchPattern.matcher(fieldValue);
		
		HashMap<String, String> codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		String validateMessage = "";
		boolean testOk = matcher.matches();
		
		if (testOk)
		{
			validateMessage = (!doubleVerification(fieldName, matcher.group(1))) ? String.format("Invalid number value '%s' for '%s'%n", fieldValue, fieldName) : "";	
			Optional<String> valueFind = codeSet.values()
					.stream()
					.filter(thisValue -> thisValue.equalsIgnoreCase(matcher.group(2)))
					.findFirst();
			validateMessage += (scaleRequired && valueFind.isEmpty()) ? String.format("Invalid measurement specification '%s' for '%s' (g, ml...?)%n", matcher.group(2), fieldName) : "";			
		}
		else
			validateMessage += String.format("Invalid field value '%s' for '%s'%n", fieldValue, fieldName);
		
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.validateChemField('%s', '%s'): '%s'", fieldName, fieldValue, validateMessage), debugLogging);
		return validateMessage;
	} // end of validateChemField()
	
	private boolean doubleVerification(String fieldName, String doubleValue) 
	{
		boolean rc = true;

		try 
		{
			Double.parseDouble(doubleValue);
		} 
		catch (NumberFormatException e) 
		{
			rc = false;
		}
	
		return rc;
	} // end of doubleVerification()
	
	/**
	 * Validate input, then save record
	 */
	public void submitNewTestData()
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.submitNewTestData()"), debugLogging);
		
		WineMakerTesting wmt = validateFields();
		if (wmt == null)
		{
			statusDisplay.setText("Test data not saved due to validation errors");
			return;
		}
		
		codeSet = HelperFunctions.getCodeValueMappings().get(FamilyCode.LABTESTFAMILY.getValue());

		wmt.set_batchKey(getBatchKey());
		wmt.set_entry_date(HelperFunctions.buildTimeStamp(activityDate, entryTime.getText(), 0));
		wmt.set_testType(codeSet.get(testSelect.getValue()));
		wmt.set_testValue(Double.parseDouble(extractPattern(testValue.getText(), 1, matchAmountPattern)));
		wmt.set_testTemp(Integer.parseInt(extractPattern(testTemp.getText(), 1, matchTempPattern)));
		wmt.set_tempScale(extractPattern(testTemp.getText(), 2, matchTempPattern).toLowerCase());

		/*
		 * The test value scale symbol might not match the resource table key, so have to find it indirectly. 
		 */
		codeSet = HelperFunctions.getCodeValueMappings().get(FamilyCode.MEASURESFAMILY.getValue());
		String getTestScale = extractPattern(testValue.getText(), 2, matchAmountPattern);
		Optional<String> valueToKey = codeSet.keySet().stream()
				.filter(compareValue -> compareValue.equalsIgnoreCase(getTestScale))
				.map(valueAsKey -> codeSet.get(valueAsKey))
				.findFirst();

		String scaleValue = (valueToKey.isPresent()) ? 
				valueToKey.get() : "";
		wmt.set_testScale(scaleValue);
		
		String updatedNotes = (testNotes.getText().length() > 0) ?
				testNotes.getText().concat("\n") : "";
		updatedNotes = updatedNotes.concat(String.format("Container tested: %s%n", containerSelect.getValue()));		
		wmt.set_testNotes(updatedNotes);
		
		if (winemakerModel.insertTestData(wmt))
			statusDisplay.setText("Test data added successfully");
		else
			statusDisplay.setText("Failed to add Test data");
		
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.submitNewTestData()"), debugLogging);
		return;
	} // end of submitNewTestData()

	/*
	 * 
	 */
	private void setUIDefaults()
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.setUIDefaults()"), debugLogging);
	
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.LABTESTFAMILY.getValue());
	
		ArrayList<String> keyList = new ArrayList<>(codeSet.keySet());
		Collections.sort(keyList);
	
		testsList.addAll(keyList
				.stream()
				.map(code -> codeSet.get(code))
				.collect(Collectors.toList()));
		
		Collections.sort(testsList);
		testSelect.setItems(testsList);
		
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.setUIDefaults()"), debugLogging);
		return;
	} // end of setUIDefaults()
	
	/*
	 * Build list of inventory container assets in this batch
	 */
	private ObservableList<String> buildSourceContainerSet(String batchId)
	{
		winemakerLogger.writeLog(String.format(">> TestDataDetailController.buildSourceContainerSet('%s')", batchId), debugLogging);
		
		BiFunction<String, String, String> buildItemDisplay = (itemName, itemId) -> (itemId.length() > 0) ? String.format("%s (%s)", itemId, itemName) : itemName;
		
		codeSet = HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue());
		ObservableList<String> containerList = FXCollections.observableArrayList();
	
		containerList.addAll(winemakerModel.queryInventory()
				.stream()
				.filter(wmi -> codeSet.get(wmi.get_itemName()) != null)
				.filter(wmi -> wmi.get_itemStockOnHand() > 0)
				.filter(wmi -> wmi.getItemBatchId().equals(batchId))
				.map(wmi -> buildItemDisplay.apply(codeSet.get(wmi.get_itemName()), wmi.getItemId()))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
	
		winemakerLogger.writeLog(String.format("<< TestDataDetailController.buildSourceContainerSet()"), debugLogging);
		return containerList;
	} // end of buildSourceContainerSet()
		
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
			winemakerScene.getStylesheets()
				.add(getClass()
				.getResource("modena.css")
				.toExternalForm());

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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		dateHBox.setAlignment(Pos.CENTER_LEFT);
		
		setUIDefaults();	
	}
}
