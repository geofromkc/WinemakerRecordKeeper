package geo.apps.winemaker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
//import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import geo.apps.winemaker.utilities.Constants.*;
import geo.apps.winemaker.utilities.DatabaseOperations;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;

public class ResourceCodesManagementController implements Initializable {

	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;
	private DatabaseOperations dbOps;
	
	private final String CODECATEGORY = "codeCategory";
	private final String CODEVALUE = "codeValue";
	private final String UPDATEVALUE = "updateValue";
	private final String NEWVALUE = "newValue";

	private boolean debugLogging = true;

	/*
	 * Scene fields
	 */
	@FXML AnchorPane ap;
	
	@FXML private TextField updateValue;
	//@FXML private TextField newValue;
	@FXML private TextField newCode;
	@FXML private TextArea statusDisplay;


	@FXML JFXComboBox<String> codeCategory;	
	ObservableList<String> categoryList = FXCollections.observableArrayList();
	
	@FXML JFXComboBox<String> codeValue;	
	ObservableList<String> valueList = FXCollections.observableArrayList();

	@FXML JFXButton insertCodeButton;
	@FXML JFXButton deleteCodeButton;
	@FXML JFXButton updateCodeButton;

	private HashMap<String, HashMap<String, String>> mapOfCodeFamilies = HelperFunctions.getCodeKeyMappings();
	private HashMap<String, String> familyCodeMap = mapOfCodeFamilies.get(FamilyCode.USERFAMILIES.getValue());

	public ResourceCodesManagementController() 
	{
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);
		this.dbOps = (DatabaseOperations) HelperFunctions.getRegistry().get(RegistryKeys.DBOPS);
	}

	/**
	 * Delete selected code
	 */
	@FXML
	public void deleteSelectedCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.deleteSelectedCode()", debugLogging);
		
		Validation validateResults = validateInput(codeCategory, codeValue);
		
		if (validateResults.equals(Validation.PASSED))
		{
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());
			String codeValueKey = getOptionKey(codeCategoryKey, codeValue.getValue());

			if (winemakerModel.deleteCode(codeCategoryKey, codeValueKey))
				statusDisplay.setText(String.format("Code '%s' deleted", codeValue.getValue()));
			else
				statusDisplay.setText(String.format("Code delete for '%s' failed", codeValue.getValue()));
			
			reloadCategoryValues();
		}

		winemakerLogger.writeLog("<< ResourceCodesManagementController.deleteSelectedCode()", debugLogging);
	} // end of deleteSelectedCode()

	/**
	 * Update table with new code value
	 * @param e
	 */
	@FXML
	public void updateSelectedCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.updateSelectedCode()", debugLogging);	

		Validation validateResults = validateInput(codeCategory, codeValue, updateValue.getText());
		
		if (validateResults.equals(Validation.PASSED))
		{
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());
			String codeValueKey = getOptionKey(codeCategoryKey, codeValue.getValue());

			if (winemakerModel.updateCode(codeCategoryKey, codeValueKey, updateValue.getText()))
				statusDisplay.setText(String.format("Code '%s' updated", updateValue.getText()));
			else
				statusDisplay.setText(String.format("Code update for '%s' failed", updateValue.getText()));
			
			updateValue.clear();
			
			reloadCategoryValues();
		}
		
		winemakerLogger.writeLog("<< ResourceCodesManagementController.updateSelectedCode()", debugLogging);
	} // end of updateSelectedCode()

	/**
	 * Add new code to the table, generating a random value for the key
	 * @param e
	 */
	@FXML
	public void insertNewCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.insertNewCode()", debugLogging);
						
		HashMap<String, String> grapeSet = this.mapOfCodeFamilies.get(FamilyCode.GRAPEFAMILY.getValue());
		winemakerLogger.writeLog(String.format("   ResourceCodesManagementController.insertNewCode(): grapeSet count = %d", grapeSet.size()), debugLogging);
		winemakerLogger.writeLog(String.format("   ResourceCodesManagementController.insertNewCode(): grapeSet = %n\t\t%s", grapeSet), debugLogging);

		newCode.setText(HelperFunctions.createRandomKey(16));
		if (validateInput(codeCategory, newCode.getText(), updateValue.getText()).equals(Validation.PASSED))
		{
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());
			if (winemakerModel.insertNewCode(codeCategoryKey, newCode.getText(), updateValue.getText()))
				statusDisplay.setText(String.format("Code '%s' added", updateValue.getText()));
			else
				statusDisplay.setText(String.format("Code addition for '%s' failed", updateValue.getText()));
			
			updateValue.clear();
			
			reloadCategoryValues();
			//newCode.clear();
			//newValue.clear();
		}
		
		winemakerLogger.writeLog("<< ResourceCodesManagementController.insertNewCode()", debugLogging);
	} // end of insertNewCode()

	private void reloadCategoryValues()
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.reloadCategoryValues()", debugLogging);
	
		HelperFunctions.loadCodeRecords(this.dbOps.queryCodes());
		
		this.mapOfCodeFamilies = HelperFunctions.getCodeKeyMappings();
		this.familyCodeMap = this.mapOfCodeFamilies.get(FamilyCode.USERFAMILIES.getValue());
	
		loadCategoryValues(this.codeCategory.getValue());
		this.codeValue.setPromptText(" Select Option");
		
		winemakerLogger.writeLog(String.format("   ResourceCodesManagementController.reloadCategoryValues(): codeValue = %n%s", this.codeValue.getItems()), debugLogging);
		winemakerLogger.writeLog("<< ResourceCodesManagementController.reloadCategoryValues()", debugLogging);
	}

	private String getCategoryKey(String categoryValue)
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.getCategoryKey('%s')", categoryValue), debugLogging);

		familyCodeMap = this.mapOfCodeFamilies.get(FamilyCode.USERFAMILIES.getValue());
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.getCategoryKey('%s')", categoryValue), debugLogging);

		return familyCodeMap.keySet()
			.stream()
			.filter(key -> categoryValue.equals(familyCodeMap.get(key)))
			.collect(Collectors.toList())
			.get(0);
		
	} // end of getCategoryKey()

	private String getOptionKey(String optionCategory, String optionValue)
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.getOptionKey('%s')", optionValue), debugLogging);

		familyCodeMap = this.mapOfCodeFamilies.get(optionCategory);
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.getOptionKey('%s')", optionValue), debugLogging);

		return familyCodeMap.keySet()
			.stream()
			.filter(key -> optionValue.equals(familyCodeMap.get(key)))
			.collect(Collectors.toList())
			.get(0);
	} // end of getOptionKey()

	/*
	 * Validate delete option
	 */
	private Validation validateInput(JFXComboBox<String> codeCategory, JFXComboBox<String> codeValue)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.validate(ComboBox codeCategory, ComboBox codeValue)", debugLogging);

		Validation checkResults = Validation.PASSED;
		String alertMsg = "";
		
		alertMsg = (codeCategory.getValue() == null) ? 
			"A Code Category must be selected\n" : "";
		alertMsg += (codeValue.getValue() == null) ? 
			"A Code Option must be selected" : ""; 
		
		if (alertMsg.length() > 0)
		{
			winemakerLogger.displayAlert(alertMsg);
			checkResults = Validation.FAILED;
		}

		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.validate(ComboBox codeCategory, ComboBox codeValue)"), debugLogging);		
		return checkResults;
	} // end of validateInput()

	/*
	 * Validate update option
	 */
	private Validation validateInput(JFXComboBox<String> codeCategory, JFXComboBox<String> codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.validate(ComboBox codeCategory, ComboBox codeValue, String codeDesc)", debugLogging);
		
		Validation checkResults = Validation.PASSED;
		String alertMsg = "";
		
		checkResults = validateInput(codeCategory, codeValue);
		if (checkResults.equals(Validation.PASSED))
		{
			alertMsg = (codeDesc.length() == 0 || codeDesc.length() > 50 || codeDesc.contains("Change '")) ? 
				"The code description length must be between 1 and 50" : "";
		}
		
		if (alertMsg.length() > 0)
		{
			winemakerLogger.displayAlert(alertMsg);
			checkResults = Validation.FAILED;
		}

		winemakerLogger.writeLog("<< ResourceCodesManagementController.validate(ComboBox codeCategory, ComboBox codeValue, String codeDesc)", debugLogging);

		return checkResults;
	} // end of validateInput()

	/*
	 * Validate input
	 */
	private Validation validateInput(JFXComboBox<String> codeCategory, String codeKey, String codeDesc)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.validate(ComboBox codeCategory, String codeValue, String codeDesc)", debugLogging);
				
		Validation checkResults = Validation.PASSED;
		String alertMsg = "";
		
		alertMsg = (codeCategory.getValue() == null) ? 
			"A Code Category must be selected\n" : "";
		alertMsg += (codeDesc.length() == 0 || codeDesc.length() > 50) ? 
			"The option value length must be between 1 and 50" : "";
		
		ArrayList<String> codeRecords = this.dbOps.queryCodes();
		Optional<String> oldCode = codeRecords
			.stream()
			.filter(codeString -> codeString.split(",")[1].equals(codeKey))
			.findAny();

		alertMsg += (oldCode.isPresent()) ? 
			"Duplicate category value" : "";
			
		if (alertMsg.length() > 0)
		{
			winemakerLogger.displayAlert(alertMsg);
			checkResults = Validation.FAILED;
		}

		winemakerLogger.writeLog("<< ResourceCodesManagementController.validate(ComboBox codeCategory, String codeValue, String codeDesc)", debugLogging);
		
		return checkResults;
	} // end of validateInput()

	/*
	 * Load values into the provided ComboBox
	 */
	private void loadCategoryOptions(HashMap<String, String> resourceKeysMap, JFXComboBox<String> comboboxTarget) 
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.loadCategoryOptions(ResourceKeys, '%s')", comboboxTarget.getValue()), debugLogging);

		comboboxTarget.getItems().clear();
		
		ObservableList<String> comboboxSelections = FXCollections.observableArrayList();
		comboboxSelections.addAll(resourceKeysMap.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		comboboxTarget.setItems(comboboxSelections);		
		
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.loadCategoryOptions()"), debugLogging);
	} // end of loadComboBoxSelections()

	/*
	 * Retrieve all of the entry values for the provided resource category, like "Grape Varietals'
	 */
	private void loadCategoryValues(String categoryCode)
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.loadCategoryValues('%s'): familyCodeMap = %n%s", categoryCode, this.familyCodeMap.entrySet()), debugLogging);		

		String processCode = this.familyCodeMap.keySet()
			.stream()
			.filter(key -> categoryCode.equals(familyCodeMap.get(key)))
			.collect(Collectors.toList())
			.get(0);
		
		valueList.clear();
		HashMap<String, String> resourceFamilyCodeMap = this.mapOfCodeFamilies.get(processCode);

		valueList.addAll(resourceFamilyCodeMap.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		codeValue.setItems(valueList);
		
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.loadCategoryValues()"), debugLogging);
	} // end of loadCategoryValues()

	/*
	 * Set common attributes for the ComboBoxes
	 */
	private void initPrompts()
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.initPrompts()"), debugLogging);

		codeCategory.setPromptText(" Select Category");
		//codeCategory.setButtonCell(new ButtonCell());
		
		codeValue.setPromptText(" Select Option");
		//codeValue.setButtonCell(new ButtonCell());

		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.initPrompts()"), debugLogging);
	} // end of loadPrompts()
	
	private void loadToolTips()
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.loadToolTips()"), debugLogging);

		codeCategory.setTooltip(HelperFunctions.buildTooltip(CODECATEGORY));
		codeValue.setTooltip(HelperFunctions.buildTooltip(CODEVALUE));
		
		updateCodeButton.setTooltip(HelperFunctions.buildTooltip(UPDATEVALUE));
		insertCodeButton.setTooltip(HelperFunctions.buildTooltip(NEWVALUE));

		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.loadToolTips()"), debugLogging);
	} // end of loadToolTips()
	
	/*
	 * Return to home Scene
	 */
	@FXML
	public void returnToMain(ActionEvent e) 
	{	
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WineMakerMD.fxml"));
	
		try {
			WineMakerController winemakerController = new WineMakerController();
			loader.setController(winemakerController);
	
			Parent batchDetailParent = loader.load();
			Scene winemakerScene = new Scene(batchDetailParent);
			winemakerScene.getStylesheets()
				.add(getClass()
				.getResource("modena.css")
				.toExternalForm());
	
			Stage window = (Stage) ((Node) e.getSource())
				.getScene()
				.getWindow();
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

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.initialize()"), debugLogging);

		winemakerLogger.writeLog(String.format("   ResourceCodesManagementController.initialize(): mapOfCodeFamilies = %n%s", this.mapOfCodeFamilies), debugLogging);
		winemakerLogger.writeLog(String.format("   ResourceCodesManagementController.initialize(): familyCodeMap = %n%s", this.familyCodeMap), debugLogging);
		
		try
		{
			HashMap<String, String> resourceCodeSet = this.mapOfCodeFamilies.get(FamilyCode.USERFAMILIES.getValue());
			
			loadCategoryOptions(resourceCodeSet, codeCategory);
			initPrompts();
			loadToolTips();
		}
		catch (Exception e)
		{
			winemakerLogger.showIOException(e, "Failure loading code mappings");
		}
		
		/*
		 * Handle category selection
		 */
		codeCategory.setOnAction(e -> {
			loadCategoryValues(codeCategory.getValue());
			updateValue.setText("");
			updateValue.setPromptText("new category value");
			initPrompts();
		});

		/*
		 * Handle code value selection
		 */
		codeValue.setOnAction(e -> {				
			if (codeValue.getValue() != null)
				updateValue.setText(codeValue.getValue());
		});
	}
}
