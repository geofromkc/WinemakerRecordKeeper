package geo.apps.winemaker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import geo.apps.winemaker.utilities.Constants.*;
import geo.apps.winemaker.utilities.DatabaseOperations;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.WineMakerLogging;

public class ResourceCodesManagementController implements Initializable {

	private WineMakerModel winemakerModel;
	private WineMakerLogging winemakerLogger;
	private DatabaseOperations dbOps;
	
	private Properties prop = new Properties();
	private final String CODECATEGORY = "codeCategory";
	private final String CODEVALUE = "codeValue";
	private final String UPDATEVALUE = "updateValue";
	private final String NEWVALUE = "newValue";
	private final String NEWCODE = "newCode";

	private boolean debugLogging = true;

	/*
	 * Scene fields
	 */
	@FXML AnchorPane ap;
	
	@FXML private TextField updateValue;
	@FXML private TextField newValue;
	@FXML private TextField newCode;
	@FXML private TextArea statusDisplay;


	@FXML ComboBox<String> codeCategory;	
	ObservableList<String> categoryList = FXCollections.observableArrayList();
	
	@FXML ComboBox<String> codeValue;	
	ObservableList<String> valueList = FXCollections.observableArrayList();

	@FXML Button insertCodeButton;
	@FXML Button deleteCodeButton;
	@FXML Button updateCodeButton;

	private HashMap<String, HashMap<String, String>> codeMapping = HelperFunctions.getCodeKeyMappings();
	private HashMap<String, String> codeSet = codeMapping.get(FamilyCode.USERFAMILIES.getValue());

	public ResourceCodesManagementController() 
	{
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);
		this.dbOps = (DatabaseOperations) HelperFunctions.getRegistry().get(RegistryKeys.DBOPS);
	}

	/*
	 * Button methods
	 */
	@FXML
	public void deleteSelectedCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.deleteSelectedCode(ActionEvent e)", debugLogging);
		
		Validation validateResults = validateInput(codeCategory, codeValue);
		
		if (validateResults.equals(Validation.PASSED))
		{						
			/*
			 * Get category, like "fermentcon"
			 */
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());

			/*
			 * Get the option, like "carb6"
			 */
			String codeValueKey = getOptionKey(codeCategoryKey, codeValue.getValue());

			winemakerLogger.writeLog(String.format("ResourceCodesManagementController.deleteSelectedCode(ActionEvent e ('%s', '%s')): key set = (%s, %s)", codeCategory.getValue(), codeValue.getValue(), codeCategoryKey, codeValueKey), debugLogging);

			if (winemakerModel.deleteCode(codeCategoryKey, codeValueKey))
				statusDisplay.setText("Code deleted");
			else
				statusDisplay.setText("Code delete failed");
		}

		winemakerLogger.writeLog("<< ResourceCodesManagementController.deleteSelectedCode(ActionEvent e)", debugLogging);
	}

	@FXML
	public void updateSelectedCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.updateSelectedCode(ActionEvent e)", debugLogging);	

		Validation validateResults = validateInput(codeCategory, codeValue, updateValue.getText());
		
		if (validateResults.equals(Validation.PASSED))
		{
			/*
			 * Get category, like "fermentcon"
			 */
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());

			/*
			 * Get the option, like "carb6"
			 */
			String codeValueKey = getOptionKey(codeCategoryKey, codeValue.getValue());

			winemakerLogger.writeLog(String.format("ResourceCodesManagementController.updateSelectedCode(ActionEvent e ('%s', '%s')): key set = (%s, %s, %s)", codeCategory.getValue(), codeValue.getValue(), codeCategoryKey, codeValueKey, updateValue.getText()), debugLogging);

			if (winemakerModel.updateCode(codeCategoryKey, codeValueKey, updateValue.getText()))
				statusDisplay.setText("Code updated");
			else
				statusDisplay.setText("Code update failed");
			
			updateValue.clear();
		}
		
		winemakerLogger.writeLog("<< ResourceCodesManagementController.updateSelectedCode(ActionEvent e)", debugLogging);
	}

	@FXML
	public void insertNewCode(ActionEvent e)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.insertNewCode(ActionEvent e)", debugLogging);
				
		if (validateInput(codeCategory, newCode.getText(), newValue.getText()).equals(Validation.PASSED))
		{
			/*
			 * Get category, like "fermentcon"
			 */
			String codeCategoryKey = getCategoryKey(codeCategory.getValue());
			
			if (winemakerModel.insertNewCode(codeCategoryKey, newCode.getText(), newValue.getText()))
				statusDisplay.setText("Code inserted");
			else
				statusDisplay.setText("Code insert failed");
			
			newCode.clear();
			newValue.clear();
		}
		
		winemakerLogger.writeLog("<< ResourceCodesManagementController.insertNewCode(ActionEvent e)", debugLogging);
	}

	private String getCategoryKey(String categoryValue)
	{
		codeSet = this.codeMapping.get(FamilyCode.USERFAMILIES.getValue());
		return codeSet.keySet()
			.stream()
			.filter(key -> categoryValue.equals(codeSet.get(key)))
			.collect(Collectors.toList())
			.get(0);
	}

	private String getOptionKey(String optionCategory, String optionValue)
	{
		codeSet = this.codeMapping.get(optionCategory);
		return codeSet.keySet()
			.stream()
			.filter(key -> optionValue.equals(codeSet.get(key)))
			.collect(Collectors.toList())
			.get(0);
	}

	/*
	 * Validate delete option
	 */
	private Validation validateInput(ComboBox<String> codeCategory, ComboBox<String> codeValue)
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
	} // end of validateInput(ComboBox<String> codeCategory, ComboBox<String> codeValue)

	/*
	 * Validate update option
	 */
	private Validation validateInput(ComboBox<String> codeCategory, ComboBox<String> codeValue, String codeDesc)
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
	} // end of validateInput(ComboBox<String> codeCategory, ComboBox<String> codeValue, String codeDesc)

	/*
	 * Validate insert option
	 */
	private Validation validateInput(ComboBox<String> codeCategory, String codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(">> ResourceCodesManagementController.validate(ComboBox codeCategory, String codeValue, String codeDesc)", debugLogging);
				
		Validation checkResults = Validation.PASSED;
		String alertMsg = "";
		
		alertMsg = (codeCategory.getValue() == null) ? 
			"A Code Category must be selected\n" : "";
		alertMsg += ((codeValue.length() == 0 || codeValue.length() > 16) || codeValue.contains(" ")) ? 
			"The option value length must be between 1 and 16 and not contain spaces\n" : "";
		alertMsg += (codeDesc.length() == 0 || codeDesc.length() > 50) ? 
			"The option value length must be between 1 and 50" : "";

		ArrayList<String> codeRecords = this.dbOps.queryCodes();
		Optional<String> oldCode = codeRecords
			.stream()
			.filter(codeString -> codeString.split(",")[1].equals(codeValue))
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
	} // end of validateInput(ComboBox<String> codeCategory, String codeValue, String codeDesc)

	/*
	 * UI utility methods
	 */
	private void loadComboBoxSelections(HashMap<String, String> codeSet, ComboBox<String> selectionList) 
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.loadComboBoxSelections(HashMap<S, S> codeSet, ComboBox<S> selectionList)"), debugLogging);

		selectionList.getItems().clear();
		
		ObservableList<String> comboboxSelections = FXCollections.observableArrayList();
		comboboxSelections.addAll(codeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		selectionList.setItems(comboboxSelections);		
		
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.loadComboBoxSelections(HashMap<S, S> codeSet, ComboBox<S> selectionList)%n"), debugLogging);
	}


	/*
	 * Define the new ToolTip to be added to a UI element.  The appearance is set in the local CSS file.
	 */
	@SuppressWarnings("unused")
	private Tooltip buildTooltip(String tipText)
	{
		Tooltip toolTipText = new Tooltip(tipText);
		toolTipText.setTextAlignment(TextAlignment.LEFT);
		toolTipText.setWrapText(true);
		toolTipText.setMaxWidth(400);

		return toolTipText;
	}
	
	private void loadCategoryValues(String categoryCode)
	{
		winemakerLogger.writeLog(String.format(">> ResourceCodesManagementController.loadCategoryValues(String '%s')", categoryCode), debugLogging);

		codeSet = this.codeMapping.get(FamilyCode.USERFAMILIES.getValue());
		String processCode = codeSet.keySet()
			.stream()
			.filter(key -> categoryCode.equals(codeSet.get(key)))
			.collect(Collectors.toList())
			.get(0);
		winemakerLogger.writeLog(String.format("ResourceCodesManagementController.loadCategoryValues(String '%s'): processCode = '%s'", categoryCode, processCode), debugLogging);
		
		codeValue.getItems().clear();
		codeSet = this.codeMapping.get(processCode);
		valueList.addAll(codeSet.values()
				.stream()
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		codeValue.setItems(valueList);
		
		winemakerLogger.writeLog(String.format("<< ResourceCodesManagementController.loadCategoryValues(String '%s')", categoryCode), debugLogging);
	}

	private void loadPrompts()
	{
		winemakerLogger.writeLog(String.format("ResourceCodesManagementController.loadPrompts()"), debugLogging);

		codeCategory.setPromptText("Select Category");
		codeCategory.setButtonCell(new ButtonCell());
		
		codeValue.setPromptText("Select Option");
		codeValue.setButtonCell(new ButtonCell());
	}
	
	private void loadToolTips()
	{
		codeCategory.setTooltip(HelperFunctions.buildTooltip(CODECATEGORY));
		codeValue.setTooltip(HelperFunctions.buildTooltip(CODEVALUE));
		updateValue.setTooltip(HelperFunctions.buildTooltip(UPDATEVALUE));
		newCode.setTooltip(HelperFunctions.buildTooltip(NEWCODE));
		newValue.setTooltip(HelperFunctions.buildTooltip(NEWVALUE));
	}
	
	@SuppressWarnings("unused")
	private void loadPropertiesFile()
	{		
		InputStream ins = this.getClass().getClassLoader().getResourceAsStream("tooltips.properties");

		if (ins == null)
		{
			winemakerLogger.displayAlert("Could not load Tooltip properties file, contact your vendor.");
			winemakerLogger.writeLog("The InputStream for the properties file is null", debugLogging);
		}
		else
		{
			try 
			{
				prop.load(ins);
				if (prop.isEmpty())
				{
					winemakerLogger.displayAlert("The Tooltip properties list is empty, contact your vendor.");
					winemakerLogger.writeLog("The Map object from the Tooltip properties file is empty", debugLogging);
				}
			} 
			catch (IOException e2) 
			{
				winemakerLogger.displayAlert("There was a fatal exception loading the Tooltip properties file, contact your vendor.");
				winemakerLogger.showIOException(e2, "IO Exception loading the Tooltip properties file");
			}		
		}		
	}
	
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
		try
		{
			HashMap<String, String> codeSet = this.codeMapping.get(FamilyCode.USERFAMILIES.getValue());
			
			loadComboBoxSelections(codeSet, codeCategory);
			loadPrompts();
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
			loadPrompts();
		});

		/*
		 * Handle code value selection
		 */
		codeValue.setOnAction(e -> {				
			if (codeValue.getValue() != null)
				updateValue.setText(String.format("Change '%s'", codeValue.getValue()));
		});
	}
}
