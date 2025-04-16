package geo.apps.winemaker;

import java.net.URL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;

import geo.apps.winemaker.activity.fermentation.FermentationActivity;
import geo.apps.winemaker.activity.fermentation.FermentationActivityFactory;
import geo.apps.winemaker.utilities.Constants.*;
import geo.apps.winemaker.utilities.DatabaseOperations;
import geo.apps.winemaker.utilities.DisplayFormatter;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.Registry;
import geo.apps.winemaker.utilities.WineMakerLogging;

/**
 * This class is the initial window into the application.   
 * Main tasks include:
 * 		Create new batches
 * 		Add fermentation activities to a batch
 * 		Add Testing activities to a batch
 * 
 * Menu options include:
 * 		Manage resource definitions (like names of grapes)
 * 		Manage inventory (of additives, or assets like glassware)
 * 		Manage app date: move data to a new location, backup and restore
 * 		Manage data tables (create and delete batch data - for advanced users)
 * 		Delete individual batches, fermentation activities and tests
 * 		Export batch data to CSV file
 * 		Import resource definitions
 * 
 * @author geo
 * @version 1.0
 * @since 2022-03-01
 */

public class WineMakerController implements Initializable {

	private WineMakerModel winemakerModel = null;
	private WineMakerLogging winemakerLogger = null;
	private DatabaseOperations dbOps = null;
	private WineMakerLog wmkOnDisplay = null;
	private DisplayFormatter df = null;

	/*
	 * Objects for UI label substitution, as in 'petsyr' = 'Petite Syrah'
	 */
	private ArrayList<WineMakerLog> wmkSets = null;
	private ArrayList<WineMakerLog> wmkBlendSets = null;
	private ArrayList<WineMakerFerment> wmfSets = null;
	private ArrayList<WineMakerTesting> wmtSets = null;
	private ArrayList<String> tasksWithAssets = new ArrayList<String>();

	private String currentBatchKey = null;
	private boolean debugLogging = true;
	private final String entryTimeFormat = "yyyy-MM-dd HH:mm:ss";
	private final String NEWBATCH = "newBatch";
	private final String NEWTASK = "newTask";
	private final String NEWTEST = "newTest";

	/*
	 * JavaFX UI objects
	 * ===============================================================================
	 */	
	@FXML
	MenuBar mb;

	@FXML ComboBox<String> batchSets;	
	@FXML ComboBox<String> testingEntries;
	@FXML ComboBox<String> fermentationEntries;	

	ObservableList<String> batchSetsList = FXCollections.observableArrayList();
	ObservableList<String> testingEntriesList = FXCollections.observableArrayList();
	ObservableList<String> fermentationEntriesList = FXCollections.observableArrayList();

	@FXML Label _batchId;

	@FXML TextArea statusDisplay;
	@FXML TextArea batchDisplay;
	@FXML Pane batchPane;
	@FXML GridPane gp;

	@FXML Button startNewBatch;
	@FXML Button addActivity;
	@FXML Button addTestData;

	private Supplier<ArrayList<WineMakerFerment>> getAllRackRecords = () -> this.wmfSets
			.stream()
			.filter(wmfTest -> wmfTest.get_fermentActivity().equals(ActivityName.RACK.getValue()))
			.collect(Collectors.toCollection(ArrayList::new));

	/*
	 * Custom Constructor, injecting pointers to the Model and Logging objects
	 */
	public WineMakerController()
	{
		Registry appRegistry = HelperFunctions.getRegistry();

		this.winemakerModel = (WineMakerModel) appRegistry.get(RegistryKeys.MODEL);
		this.winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		this.dbOps = (DatabaseOperations) appRegistry.get(RegistryKeys.DBOPS);
	}

	public void sayGoodbye(ActionEvent e)
	{
		System.exit(0);
	}

	private ArrayList<WineMakerLog> getWineMakerLogSet()
	{
		return this.wmkSets;
	}

	private void setWineMakerFermentSet(ArrayList<WineMakerFerment> wmfSets)
	{
		this.wmfSets = wmfSets;
	}

	private ArrayList<WineMakerFerment> getWineMakerFermentSet()
	{
		return this.wmfSets;
	}

	private void setWineMakerTestingSet(ArrayList<WineMakerTesting> wmtSets)
	{
		this.wmtSets = wmtSets;
	}

	private ArrayList<WineMakerTesting> getWineMakerTestingSet()
	{
		return this.wmtSets;
	}

	public void debugOn()
	{
		winemakerModel.setDebugActive(true);
	}

	public void debugOff()
	{
		winemakerModel.setDebugActive(false);
	}

	/*
	 * ===================== Event Handlers for the database table CREATE buttons =====================
	 */
	@FXML
	private void createKeyTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.createKeyTable(ActionEvent e)"), debugLogging);

		if (winemakerModel.validateTable(DatabaseTables.PRIMARY.getValue()).length() == 0)
		{
			winemakerLogger.displayAlert("The primary Batch table appears to already exist.   You must delete it before it can be recreated and reloaded.");
			winemakerLogger.writeLog(String.format("<< WineMakerController.createKeyTable(ActionEvent e)"), debugLogging);
			return;
		}
		if (winemakerModel.createTable(DatabaseTables.PRIMARY.getValue()).length() > 0)
			statusDisplay.setText("Failed to create primary Batch table");
		else
			statusDisplay.setText("Successfully created primary Batch table");

		winemakerLogger.writeLog(String.format("<< WineMakerController.createKeyTable(ActionEvent e)"), debugLogging);
	} // end of createKeyTable()

	@FXML
	private void createTestingTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.createTestingTable(ActionEvent e)"), debugLogging);

		if (winemakerModel.validateTable(DatabaseTables.TESTS.getValue()).length() == 0)
		{
			winemakerLogger.displayAlert("The Test table appears to already exist.   You must delete it before it can be recreated and reloaded.");
			winemakerLogger.writeLog(String.format("<< WineMakerController.createTestingTable(ActionEvent e)"), debugLogging);
			return;
		}

		if (winemakerModel.createTable(DatabaseTables.TESTS.getValue()).length() > 0)
			statusDisplay.setText("Failed to create Test table");
		else
			statusDisplay.setText("Successfully created Test table");

		winemakerLogger.writeLog(String.format("<< WineMakerController.createTestingTable(ActionEvent e)"), debugLogging);
	} // end of createTestingTable()

	@FXML
	private void createFermentTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.createFermentTable(ActionEvent e)"), debugLogging);

		if (winemakerModel.validateTable(DatabaseTables.ACTIVITY.getValue()).length() == 0)
		{
			winemakerLogger.displayAlert("The Ferment table appears to already exist.   You must delete it before it can be recreated and reloaded.");
			winemakerLogger.writeLog(String.format("<< WineMakerController.createFermentTable(ActionEvent e)"), debugLogging);
			return;
		}

		if (winemakerModel.createTable(DatabaseTables.ACTIVITY.getValue()).length() > 0)
			statusDisplay.setText("Failed to create Ferment table");
		else
			statusDisplay.setText("Successfully created Ferment table");

		winemakerLogger.writeLog(String.format("<< WineMakerController.createFermentTable(ActionEvent e)"), debugLogging);
	} // end of createFermentTable()

	@FXML
	private void createCodesTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.createCodesTable(ActionEvent e)"), debugLogging);

		if (winemakerModel.validateTable(DatabaseTables.CODES.getValue()).length() == 0)
		{
			winemakerLogger.displayAlert("The Codes table appears to already exist.   You must delete it before it can be recreated and reloaded.");
			winemakerLogger.writeLog(String.format("<< WineMakerController.createCodesTable(ActionEvent e)"), debugLogging);
			return;
		}
		if (winemakerModel.createTable(DatabaseTables.CODES.getValue()).length() > 0)
			statusDisplay.setText("Failed to create codes table");
		else
			statusDisplay.setText("Successfully created Codes table");

		winemakerLogger.writeLog(String.format("<< WineMakerController.createCodesTable(ActionEvent e)"), debugLogging);
	} // end of createCodesTable()

	@FXML
	private void createInventoryTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.createInventoryTable(ActionEvent e)"), debugLogging);

		if (winemakerModel.validateTable(DatabaseTables.INVENTORY.getValue()).length() == 0)
		{
			winemakerLogger.displayAlert("The Inventory table appears to already exist.   You must delete it before it can be recreated and reloaded.");
			winemakerLogger.writeLog(String.format("<< WineMakerController.createInventoryTable(ActionEvent e)"), debugLogging);
			return;
		}
		if (winemakerModel.createTable(DatabaseTables.INVENTORY.getValue()).length() > 0)
			statusDisplay.setText("Failed to create Inventory table");
		else
			statusDisplay.setText("Successfully created Inventory table");

		winemakerLogger.writeLog(String.format("<< WineMakerController.createInventoryTable(ActionEvent e)"), debugLogging);
	} // end of createInventoryTable()

	/*
	 * ===================== Event Handlers for the database table DROP buttons =====================
	 */
	@FXML
	private void dropBatchTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.dropBatchTable(ActionEvent e)"), debugLogging);

		if (winemakerLogger.showAlarm("You are about to delete the primary batch table.  Are you sure?", AlertType.CONFIRMATION))
		{
			if (winemakerModel.dropTable(DatabaseTables.PRIMARY.getValue()).length() > 0)
				statusDisplay.setText("Failed to drop primary key table");
			else
				statusDisplay.setText("Successfully dropped Batch table");
		}		

		winemakerLogger.writeLog(String.format("<< WineMakerController.dropBatchTable(ActionEvent e)"), debugLogging);
	} // end of dropBatchTable()

	@FXML
	private void dropTestingTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.dropTestingTable(ActionEvent e)"), debugLogging);

		if (winemakerLogger.showAlarm("You are about to delete the test data table.  Are you sure?", AlertType.CONFIRMATION))
		{
			if (winemakerModel.dropTable(DatabaseTables.TESTS.getValue()).length() > 0)
				statusDisplay.setText("Failed to drop testing table");
			else
				statusDisplay.setText("Successfully dropped Testing table");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.dropTestingTable(ActionEvent e)"), debugLogging);
	} // end of dropTestingTable()

	@FXML
	private void dropFermentTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.dropFermentTable(ActionEvent e)"), debugLogging);

		if (winemakerLogger.showAlarm("You are about to delete the ferment data table.  Are you sure?", AlertType.CONFIRMATION))
		{
			if (winemakerModel.dropTable(DatabaseTables.ACTIVITY.getValue()).length() > 0)
				statusDisplay.setText("Failed to drop fermentation table");
			else
				statusDisplay.setText("Successfully dropped Ferment table");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.dropFermentTable(ActionEvent e)"), debugLogging);
	} // end of dropFermentTable()

	@FXML
	private void dropCodesTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.dropCodesTable(ActionEvent e)"), debugLogging);

		if (winemakerLogger.showAlarm("You are about to delete the resource codes data table.  Are you sure?", AlertType.CONFIRMATION))
		{
			if (winemakerModel.dropTable(DatabaseTables.CODES.getValue()).length() > 0)
				statusDisplay.setText("Failed to drop codes table");
			else
				statusDisplay.setText("Successfully dropped Codes table");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.dropCodesTable(ActionEvent e)"), debugLogging);
	} // end of dropCodesTable()

	@FXML
	private void dropInventoryTable(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.dropInventoryTable(ActionEvent e)"), debugLogging);

		if (winemakerLogger.showAlarm("You are about to delete the inventory table.  Are you sure?", AlertType.CONFIRMATION))
		{
			if (winemakerModel.dropTable(DatabaseTables.INVENTORY.getValue()).length() > 0)
				statusDisplay.setText("Failed to drop Inventory table");
			else
				statusDisplay.setText("Successfully dropped Inventory table");
		}		

		winemakerLogger.writeLog(String.format("<< WineMakerController.dropInventoryTable(ActionEvent e)"), debugLogging);
	} // end of dropInventoryTable()

	@FXML
	private void backUpDatabase(ActionEvent e)
	{
		statusDisplay.setText(winemakerModel.backupDatabase());
	}

	/**
	 * Restore database tables.  Prompt user for specific backup files.
	 * @param e Button generated event object
	 */	
	@FXML
	private void restoreDatabase(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.restoreDatabase(ActionEvent e)", debugLogging);

		Properties appProperties = winemakerModel.getAppProperties();
		String[] backupList = new File(appProperties.getProperty("DBBACKUP")).list();
		String[] sortedBackupList = Arrays
				.stream(backupList)
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList())
				.toArray(String[]::new);

		ChoiceDialog<String> restoreDialog = new ChoiceDialog<>(sortedBackupList[0], sortedBackupList);
		restoreDialog.setHeaderText("WineMaker Data Backup Selection");
		restoreDialog.setContentText("Select the date of the backup to be used for the Restore operation");
		restoreDialog.showAndWait();

		winemakerLogger.writeLog(String.format("   WineMakerController.restoreDatabase(): selected '%s'", restoreDialog.getSelectedItem().toString()), debugLogging);

		Alert alertWarning = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);		
		alertWarning.setTitle("WineMaker Data Backup Selection");
		alertWarning.setContentText("This will erase the current set of records.  Is that okay?");
		Optional<ButtonType> result = alertWarning.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.YES)
		{
			winemakerModel.restoreDatabase(appProperties.getProperty("DBBACKUP") + File.separator + restoreDialog.getSelectedItem().toString());
			loadBatchSets();
		}
		else
			winemakerLogger.writeLog("   WineMakerController.restoreDatabase(): user cancelled the restore operation", debugLogging);

		winemakerLogger.writeLog("<< WineMakerController.restoreDatabase()", debugLogging);
	} // end of restoreDatabase()

	/**
	 * Move database to new user-specified location
	 * User is prompted for new locations for the data and backup tables
	 * @param e Button generated event object
	 */
	@FXML
	private void moveDatabase(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.moveDatabase(ActionEvent e)", debugLogging);

		String taskMsg = "";
		Properties appProperties = winemakerModel.getAppProperties();

		File oldAppDir = new File(appProperties.getProperty("DBAPPDIR"));
		File oldBackupDir = new File(appProperties.getProperty("DBBACKUP"));
		File newDatabaseDir = directoryPrompt("WineMaker New Data Directory Selection", WineMakerModel.getDefaultappname());
		File newBackupDir = directoryPrompt("WineMaker New Backup Directory Selection", WineMakerModel.getDefaultbackupdirname());

		if (newDatabaseDir == null || !newDatabaseDir.exists())
		{
			statusDisplay.setText("New DB path is null, operation cancelled");
			winemakerLogger.writeLog("<< WineMakerController.moveDatabase(ActionEvent e)", debugLogging);
			return;			
		}

		if (newBackupDir == null || !newBackupDir.exists())
		{
			statusDisplay.setText("New Backup path is null, operation cancelled");
			winemakerLogger.writeLog("<< WineMakerController.moveDatabase(ActionEvent e)", debugLogging);
			return;			
		}

		winemakerLogger.writeLog(String.format("   WineMakerController.moveDatabase(ActionEvent e): DBAPPDIR = %s", oldAppDir.getPath()), debugLogging);
		winemakerLogger.writeLog(String.format("   WineMakerController.moveDatabase(ActionEvent e): DBBACKUP = %s", oldBackupDir.getPath()), debugLogging);
		winemakerLogger.writeLog(String.format("   WineMakerController.moveDatabase(ActionEvent e): user selected directories '%s' & '%s'", newDatabaseDir.getPath(), newBackupDir.getPath()), true);
		winemakerModel.moveDatabase(newDatabaseDir.getPath(), newBackupDir.getPath(), oldBackupDir.getPath());

		if (!writePropsFile(newDatabaseDir, newBackupDir, new File(WineMakerModel.getLocalappdatahome() + WineMakerModel.getPropertiesfilename()), appProperties.getProperty("DBDEFAULTS")))
		{
			taskMsg = "Operation failed writing new properties file";
			winemakerLogger.writeLog(String.format("   WineMakerController.moveDatabase(ActionEvent e): Failure trying to write new properties file"), true);
		}
		else
			winemakerModel.loadProperties();
		taskMsg = "Move complete, delete folders " + oldAppDir.getPath() + " and \n" + oldBackupDir.getPath() + " at your convenience";

		statusDisplay.setText(taskMsg);
		winemakerLogger.writeLog("<< WineMakerController.moveDatabase(ActionEvent e)", debugLogging);
	} // end of moveDatabase()

	/*
	 * Drop and recreate all of the tables
	 */
	@FXML
	private void tableReset(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.tableReset()", debugLogging);

		if (winemakerModel.resetTables())
			statusDisplay.appendText("All tables have been reset");
		else
			statusDisplay.appendText("Failure to reset all tables, check log for errors");

		winemakerLogger.writeLog("<< WineMakerController.tableReset()", debugLogging);
	} // end of tableReset()

	/*
	 * Generic method to prompt user for location of an input file
	 */
	private File filePrompt(String promptTitle)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.filePrompt('%s')", promptTitle), debugLogging);

		File selectedFile = null;

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(promptTitle);
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text CSV", "*.csv"));
		selectedFile = fileChooser.showOpenDialog(winemakerModel.getFxStage());

		winemakerLogger.writeLog(String.format("<< WineMakerController.filePrompt('%s')", promptTitle), debugLogging);
		return selectedFile;
	} // end of filePrompt()

	/*
	 * Generic method to prompt user for location of an output directory
	 */
	private File directoryPrompt(String promptTitle, String terminalDir)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.directoryPrompt('%s', '%s')", promptTitle, terminalDir), debugLogging);

		File newDir = null;
		try 
		{
			DirectoryChooser dc = new DirectoryChooser();
			dc.setInitialDirectory(new File(WineMakerModel.getDefaultappsearch()));
			dc.setTitle(promptTitle);

			newDir = dc.showDialog(winemakerModel.getFxStage());
		}
		catch (Exception e1) 
		{
			winemakerLogger.showIOException(e1, "Failed operation for " + promptTitle);
			statusDisplay.setText("Failed to set target directory for " + promptTitle);
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.directoryPrompt('%s', '%s'): Set to %s", promptTitle, terminalDir, newDir), debugLogging);
		return newDir;
	} // end of directoryPrompt()

	/*
	 * Create an updated properties file
	 * @param appFilesDir File current application database location
	 * @param backupDir File current backup database location
	 * @param propsFile File file object to be written  
	 * @param defaultsFlag String flag for use of application defaults
	 * @return boolean success/failure of write operation
	 */
	private boolean writePropsFile(File appFilesDir, File backupDir, File propsFile, String defaultsFlag)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.writePropsFile('%s', '%s', '%s')", appFilesDir.getPath(), backupDir.getPath(), propsFile.getPath()), true);

		boolean writeStatus = true;

		try 
		{
			FileWriter myWriter = new FileWriter(propsFile, false);
			String appDir = appFilesDir.getPath().replace("\\", "/");

			myWriter.write("DBTYPE=Derby\n");
			myWriter.write("DBAPPDIR=" + appDir + "\n");
			myWriter.write("DBSHUTDOWN=" + WineMakerModel.getJdbcbase() + ";shutdown=true\n");
			myWriter.write("DBPATH=" + WineMakerModel.getJdbcbase() + appDir + "/winemaker;create=true\n");
			myWriter.write("DBRESTORE=" + WineMakerModel.getJdbcbase() + appDir + "/winemaker;restoreFrom=\n");
			myWriter.write("DBBACKUP=" + backupDir.getPath().replace("\\", "/") + "\n");
			myWriter.write("DBDEFAULTS=" + defaultsFlag);
			myWriter.close();

			winemakerLogger.writeLog(String.format("   WineMakerController.writePropsFile(): recreated properties file '%s'", propsFile.getPath()), true);
		} 
		catch (IOException e) 
		{
			winemakerLogger.showIOException(e, "Failed to write properties and/or startup file");
			writeStatus = false;
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.writePropsFile()"), true);
		return writeStatus;
	} // end of writePropsFile()

	/*
	 * Switch scene to the batch create/update scene.
	 * The variable processMode switches between CREATE and UPDATE modes.
	 * The variable batchType indicates the type of batch, as blends require the creation of multiple records.
	 */
	private void switchBatchScene(ActionEvent e, BatchScene processMode, Blend batchType)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.switchBatchScene(ActionEvent, processMode '%s', batchType '%s'", processMode.toString(), batchType.toString()), debugLogging);

		if ((HelperFunctions.getCodeKeyMappings().size() < 1))
		{
			statusDisplay.setText("It appears that the resource codes have not been loaded into the application, basic functions are not enabled");
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("BatchDetail.fxml"));
		try 
		{
			BatchDetailController batchDetailController = new BatchDetailController(batchType);
			loader.setController(batchDetailController);

			Parent batchDetailParent = loader.load();
			Scene batchScene = new Scene(batchDetailParent);
			batchScene
			.getStylesheets()
			.add(getClass()
					.getResource("modena.css")
					.toExternalForm());

			if (processMode.equals(BatchScene.UPDATE))
				batchDetailController.updateBatchScene(this.wmkOnDisplay);

			Stage currentStage = (Stage) Stage.getWindows()
					.stream()
					.filter(Window::isShowing)
					.findFirst()
					.orElse(null);

			currentStage.setScene(batchScene);
			currentStage.setResizable(true);
			currentStage.show();
		} 
		catch (IOException e1) 
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e1, "Error calling Batch Data Module, see log for details\n"));
		}
		catch (Exception e2)
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e2, "Error calling Batch Data Module, see log for details\n"));
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.switchBatchScene(ActionEvent, processMode '%s', batchType '%s'", processMode.toString(), batchType.toString()), debugLogging);
	} // end of switchBatchScene()

	/*
	 * Open a new Scene and Controller to process new test data
	 */
	@FXML
	private void addNewTestData(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.addNewTestData(ActionEvent e)", debugLogging);

		if ((HelperFunctions.getCodeKeyMappings().size() < 1))
		{
			statusDisplay.setText("It appears that the resource codes have not been loaded into the application, basic functions are not enabled");
			return;
		}

		if (batchSets.getValue() == null)
		{
			statusDisplay.setText("A batch must be selected before test data can be added");
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource("TestDataDetail.fxml"));
		try 
		{
			TestDataDetailController testDataDetailController = new TestDataDetailController();
			loader.setController(testDataDetailController);

			Parent testDataDetailParent = loader.load();
			Scene testDataScene = new Scene(testDataDetailParent);
			testDataScene.getStylesheets().add(getClass().getResource("modena.css").toExternalForm());

			currentBatchKey = HelperFunctions.batchKeyCompress(batchSets.getValue());
			testDataDetailController.setBatchKey(currentBatchKey);				

			Stage currentStage = (Stage) Stage.getWindows()
					.stream()
					.filter(Window::isShowing)
					.findFirst()
					.orElse(null);

			currentStage.setScene(testDataScene);
			currentStage.setResizable(false);
			currentStage.show();
		} 
		catch (IOException e1) 
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e1, "Error calling Test Data Module, see log for details\n"));
			winemakerLogger.showIOException(e1, "Error calling Test Data Module");
		}
		catch (Exception e2)
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e2, "Error calling Test Data Module, see log for details\n"));
			winemakerLogger.showIOException(e2, "Error calling Test Data Module");
		}

		winemakerLogger.writeLog("<< WineMakerController.addNewTestData(ActionEvent e)", debugLogging);
	} // end of addNewTestData()

	/*
	 * Open a new Scene and Controller to process new ferment data
	 */
	@FXML
	private void addNewFermentData(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.addNewFermentData(ActionEvent e)", debugLogging);

		if ((HelperFunctions.getCodeKeyMappings().size() < 1))
		{
			statusDisplay.setText("It appears that the resource codes have not been loaded into the application, basic functions are not enabled");
			return;
		}

		if (batchSets.getValue() == null)
		{
			statusDisplay.setText("A batch must be selected before fermentation data can be added");
			return;
		}

		winemakerLogger.writeLog(">> WineMakerController.addNewFermentData(ActionEvent e)", debugLogging);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("FermentDataDetail.fxml"));

		try 
		{
			FermentDataDetailController fermentDataDetailController = new FermentDataDetailController();
			loader.setController(fermentDataDetailController);

			Parent fermentDataDetailParent = loader.load();
			Scene fermentDataScene = new Scene(fermentDataDetailParent);
			fermentDataScene.getStylesheets()
			.add(getClass()
					.getResource("modena.css")
					.toExternalForm());

			currentBatchKey = HelperFunctions.batchKeyCompress(batchSets.getValue());
			fermentDataDetailController.setBatchKey(currentBatchKey);				

			Stage currentStage = (Stage) Stage.getWindows()
					.stream()
					.filter(Window::isShowing)
					.findFirst()
					.orElse(null);

			currentStage.setScene(fermentDataScene);
			currentStage.setResizable(true);
			currentStage.show();
		} 
		catch (IOException e1) 
		{
			statusDisplay.setText(winemakerLogger.showIOException(e1, "Error calling Ferment Data Module, see log for details\n"));
			winemakerLogger.showIOException(e1, "Error calling Ferment Data Module");
		}
		catch (Exception e2)
		{
			statusDisplay.setText(winemakerLogger.showIOException(e2, "Error calling Ferment Data Module, see log for details\n"));
			winemakerLogger.showIOException(e2, "Error calling Ferment Data Module");
		}

		winemakerLogger.writeLog("<< WineMakerController.addNewFermentData(ActionEvent e)", debugLogging);
	} // end of addNewFermentData()

	/**
	 * Open a new Scene and Controller for resource codes management
	 */	
	@FXML
	private void updateCodesTable(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.updateCodesTable(ActionEvent e)", debugLogging);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ResourceCodesManagement.fxml"));
		try 
		{
			ResourceCodesManagementController resourceCodesManagementController = new ResourceCodesManagementController();
			loader.setController(resourceCodesManagementController);

			Parent resourceCodesUpdateParent = loader.load();
			Scene resourceCodesUpdateScene = new Scene(resourceCodesUpdateParent);
			resourceCodesUpdateScene
			.getStylesheets()
			.add(getClass()
					.getResource("modena.css")
					.toExternalForm());

			Stage currentStage = (Stage) Stage.getWindows()
					.stream()
					.filter(Window::isShowing)
					.findFirst()
					.orElse(null);
			currentStage.setScene(resourceCodesUpdateScene);
			currentStage.setResizable(false);
			currentStage.show();			
		} 
		catch (IOException e1) 
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e1, "Error calling Code Table Update Module, see log for details\n"));
		}

		winemakerLogger.writeLog("<< WineMakerController.updateCodesTable(ActionEvent e)", debugLogging);
	} // end of updateCodesTable()

	/*
	 * Open a new Scene and Controller for inventory management
	 */	
	@FXML
	private void openInventoryManagement(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.openInventoryManagement(ActionEvent e)", debugLogging);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("InventoryManagement.fxml"));
		try 
		{
			InventoryManagementController inventoryManagementController = new InventoryManagementController();
			loader.setController(inventoryManagementController);

			Parent inventoryManagementParent = loader.load();
			Scene inventoryManagementScene = new Scene(inventoryManagementParent);
			inventoryManagementScene
			.getStylesheets()
			.add(getClass()
					.getResource("modena.css")
					.toExternalForm());

			Stage currentStage = (Stage) Stage.getWindows()
					.stream()
					.filter(Window::isShowing)
					.findFirst()
					.orElse(null);
			currentStage.setScene(inventoryManagementScene);
			currentStage.show();
		} 
		catch (IOException e1) 
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e1, "1. Error calling Inventory Management Module, see log for details\n"));
		}
		catch (Exception e2)
		{
			statusDisplay.appendText(winemakerLogger.showIOException(e2, "2. Error calling Inventory Management Module, see log for details\n"));
		}

		winemakerLogger.writeLog("<< WineMakerController.openInventoryManagement(ActionEvent e)", debugLogging);
	} // end of openInventoryManagement()

	/*
	 * Open a new Scene and Controller to process new batch data
	 */
	@FXML
	private void createNewBatch(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.createNewBatch(ActionEvent e)", debugLogging);

		String[] blendOptions = {Blend.NOTBLEND.toString(), Blend.JUICEBLEND.toString(), Blend.FIELDBLEND.toString()};

		ChoiceDialog<String> batchTypeDialog = new ChoiceDialog<>(blendOptions[0], blendOptions);
		batchTypeDialog.setHeaderText("WineMaker Batch Type Selection");
		batchTypeDialog.setContentText("Select the type of batch; straight varietal or a blend");
		batchTypeDialog.showAndWait();

		Blend selectedBlend = Blend.valueOf(batchTypeDialog.getSelectedItem());

		switchBatchScene(e, BatchScene.CREATE, selectedBlend);

		winemakerLogger.writeLog("<< WineMakerController.createNewBatch(ActionEvent e)", debugLogging);
	} // end of createNewBatch()

	/*
	 * Write inventory records to file
	 */
	private void writeInventoryExportFile(File inventoryOutputFile, ArrayList<String> inventorySet)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.writeInventoryExportFile(inventoryOutputFile, inventorySet)"), debugLogging);

		try 
		{
			Files.write(inventoryOutputFile.toPath(), inventorySet, Charset.defaultCharset());

			winemakerLogger.writeLog(String.format("   WineMakerController.writeInventoryExportFile(): export file written"), debugLogging);
			statusDisplay.appendText(String.format("Inventory exported to %s%n", inventoryOutputFile.getPath()));
		} 
		catch (IOException e1) 
		{
			winemakerLogger.showIOException(e1, "Exception writing the batch export file, see log for details");
			statusDisplay.appendText(String.format("Exception writing the inventory export file %s, see log for details %s and %s", inventoryOutputFile.getPath()));
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.writeInventoryExportFile(inventoryOutputFile, inventorySet) return"), debugLogging);
	} // end of writeInventoryExportFile()

	/*
	 * Write operations for export files
	 */
	private void writeBatchExportFile(File batchOutputFile, File fermentOutputFile, File testingOutputFile, ArrayList<String> batchSet, ArrayList<String> fermentSet, ArrayList<String> testingSet)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.writeBatchExportFile(File batchOutputFile, File fermentOutputFile, ArrayList<String> batchSet, ArrayList<String> fermentSet)"), debugLogging);

		try 
		{
			Files.write(batchOutputFile.toPath(), batchSet, Charset.defaultCharset());
			Files.write(fermentOutputFile.toPath(), fermentSet, Charset.defaultCharset(), StandardOpenOption.CREATE);
			Files.write(testingOutputFile.toPath(), testingSet, Charset.defaultCharset(), StandardOpenOption.CREATE);

			winemakerLogger.writeLog(String.format("   WineMakerController.exportExistingBatch(): export files written"), debugLogging);
			statusDisplay.setText(String.format("Batch data exported to %n\t%s%n\t%s%n\t%s", batchOutputFile.getPath(), fermentOutputFile.getPath(), testingOutputFile.getPath()));
		} 
		catch (IOException e1) 
		{
			winemakerLogger.showIOException(e1, "Exception writing the batch export file, see log for details");
			statusDisplay.appendText(String.format("Exception writing one or more of the batch export file %s and %s, see log for details %s and %s", batchOutputFile.getPath(), fermentOutputFile.getPath()));
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.writeBatchExportFile(File batchOutputFile, File fermentOutputFile, ArrayList<String> batchSet, ArrayList<String> fermentSet) return"), debugLogging);
	} // end of writeBatchExportFile()

	/*
	 * Delete an existing batch and its associated ferment and testing records.
	 * Prompt the user for confirmation before proceeding, and reload the batch choices.
	 */
	@FXML
	private void deleteExistingBatch(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.deleteExistingBatch('%s')", batchSets.getValue()), debugLogging);
		String resultText = "";

		if (batchSets.getValue() == null)
			return;

		String deletePrompt = (batchSets.getValue() == null || batchSets.getValue().length() == 0) ? 
				"You are about to delete all of the parent batches. Are you sure?" : 
					String.format("You are about to delete batch '%s'. Are you sure?", batchSets.getValue());

		String batchKey = HelperFunctions.batchKeyCompress(batchSets.getValue());
		if (winemakerLogger.showAlarm(deletePrompt, AlertType.CONFIRMATION))
		{
			if (winemakerModel.deleteBatch(batchKey))
				resultText = String.format("%nDeleted batch '%s' and its associated ferment and testing records", batchSets.getValue());
			else
				resultText = String.format("%nFailed to delete batch '%s' and its associated ferment and testing records", batchSets.getValue());
		}

		ArrayList<WineMakerInventory> batchInventory = winemakerModel.queryInventoryByBatch(batchKey);
		batchInventory
			.stream()
			.forEach(wmi -> 
				{
					wmi.setItemBatchId("");
					winemakerModel.updateInventoryBatch(wmi);
				}
			);

		if (batchInventory.size() > 0)
			resultText.concat(String.format("%nRemoved batch '%s' from container assets", batchSets.getValue()));

		winemakerLogger.showAlarm(resultText, AlertType.INFORMATION);

		loadBatchSets();

		winemakerLogger.writeLog(String.format("<< WineMakerController.deleteExistingBatch('%s')", batchSets.getValue()), debugLogging);
	} // end of deleteExistingBatch()

	/*
	 * Delete Fermentation table record, then redisplay the batch
	 */
	@FXML
	private void deleteFermentRecord(ActionEvent e)	
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.deleteFermentRecord('%s')", fermentationEntries.getValue()), debugLogging);

		if (fermentationEntries.getValue() == null)
			return;

		if (deleteRecordByDate(fermentationEntries, DatabaseTables.ACTIVITY.getValue(), "Ferment Activity"))
		{
			statusDisplay.setText(String.format("%n%nDeleted entry '%s'", fermentationEntries.getValue()));

			_batchId.setText("");
			batchDisplay.setText("");

			getBatchData(HelperFunctions.batchKeyCompress(batchSets.getValue()));
			loadBatchFermentData(HelperFunctions.batchKeyCompress(batchSets.getValue()));
		}
		else
			statusDisplay.setText(String.format("%n%nFailed to delete entry '%s'", fermentationEntries.getValue()));

		winemakerLogger.writeLog(String.format("<< WineMakerController.deleteFermentRecord()"), debugLogging);
	} // end of deleteFermentRecord()

	/*
	 * Delete Test table record
	 */
	@FXML
	private void deleteTestRecord(ActionEvent e)	
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.deleteTestRecord('%s')", testingEntries.getValue()), debugLogging);

		if (testingEntries.getValue() ==null)
			return;

		if (deleteRecordByDate(testingEntries, DatabaseTables.TESTS.getValue(), "Test"))
		{
			statusDisplay.setText(String.format("%n%nDeleted entry '%s'", testingEntries.getValue()));

			_batchId.setText("");
			batchDisplay.setText("");

			getBatchData(HelperFunctions.batchKeyCompress(batchSets.getValue()));
			loadBatchTestData(HelperFunctions.batchKeyCompress(batchSets.getValue()));
		}
		else
			statusDisplay.setText(String.format("%n%nFailed to delete entry '%s'", testingEntries.getValue()));

		winemakerLogger.writeLog(String.format("<< WineMakerController.deleteTestRecord()"), debugLogging);
	} // end of deleteTestRecord()

	/*
	 * Common routine to delete record from any table using a TimeStamp key field
	 */
	private boolean deleteRecordByDate(ComboBox<String> tableEntries, String tableName, String tableType)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.deleteRecordByDate('%s', '%s', '%s')", tableEntries.getValue(), tableName, tableType), debugLogging);

		Alert alertWarning;
		boolean entrySelected = (tableEntries.getValue() != null && tableEntries.getValue().length() > 0) ? true : false;
		if (!entrySelected)
		{
			statusDisplay.setText("No entry selected");
			winemakerLogger.writeLog(String.format("<< WineMakerController.deleteDateRecord() no record selected"), debugLogging);
			return false;
		}

		alertWarning = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
		alertWarning.setContentText(String.format("You are about to delete %s entry: %n'%s'.  %nAre you sure?", tableType, tableEntries.getValue()));
		alertWarning.setTitle(String.format("WineMaker %s Entry Deletion", tableType));

		Optional<ButtonType> result = alertWarning.showAndWait();
		if (!result.isPresent() || result.get() == ButtonType.NO || result.get() == ButtonType.CANCEL)
		{
			statusDisplay.setText("No entry deleted");
			winemakerLogger.writeLog(String.format("<< WineMakerController.deleteDateRecord() cancelled"), debugLogging);
			return false;
		}

		Timestamp extractTimestamp = null;
		if (tableName.equals(DatabaseTables.ACTIVITY.getValue()))
		{
			WineMakerFerment wmf = getFermentEntryData(tableEntries.getValue());
			extractTimestamp = wmf.get_entry_date();
		}
		if (tableName.equals(DatabaseTables.TESTS.getValue()))
		{
			WineMakerTesting wmt = getTestEntryData(tableEntries.getValue());
			extractTimestamp = wmt.get_entry_date();
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.deleteRecordByDate('%s')", tableEntries.getValue()), debugLogging);
		return winemakerModel.deleteDateRecord(extractTimestamp, tableName);
	} // end of deleteRecordByDate()

	/**
	 * From the pre-loaded set of records, extract the selected record.
	 * No validation is done on the Optional object, as this method will only be called if the record exists.
	 * 
	 * @param currentBatchKey the key to the selected database record
	 */
	private void getBatchData(String currentBatchKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.getBatchData('%s')", currentBatchKey), debugLogging);

		Optional<WineMakerLog> wmk = getWineMakerLogSet()
				.stream()
				.filter(wmkFilter -> wmkFilter.get_batchKey().contains(currentBatchKey))
				.findFirst();

		loadBatchDisplay(wmk.get());

		winemakerLogger.writeLog(String.format("<< WineMakerController.getBatchData('%s') return", currentBatchKey), debugLogging);
	} // end of getBatchData()

	/**
	 * Use content in the selected record to populate the UI.  If this is a blend batch, then additional queries will be made
	 * to retrieve the set of related batches. 
	 * 
	 * @param wmk WineMakerLog object representing current batch record
	 */	
	public void loadBatchDisplay(WineMakerLog wmk)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.loadBatchDisplay(WineMakerLog %s): %s", wmk.get_batchKey(), wmk), debugLogging);

		wmkOnDisplay = wmk;

		_batchId.setVisible(true);
		batchDisplay.setVisible(true);

		_batchId.setText(HelperFunctions.batchKeyExpand(wmk));

		/*
		 * Try to convert the grape name from the set of varietal grapes.  If that fails then this
		 * is a blend batch with different requirements.
		 */
		String convertedBatchGrape = HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPEFAMILY.getValue()).get(wmk.get_batchGrape());
		boolean blendFlag = (convertedBatchGrape == null);

		batchDisplay.clear();
		showPrimaryBatchData(wmk, blendFlag);

		/*
		 * If batch is a blend, also include the component grapes in the display
		 */
		if (blendFlag)
		{
			wmkBlendSets = winemakerModel.queryBatch(wmk.get_batchKey(), SQLSearch.BLENDCOMPONENT);
			wmkBlendSets
			.stream()
			.forEach(this::showBlendBatchData);
		}

		if (wmk.get_sourceVendorNotes().length() > 0)
			batchDisplay.appendText(df.displayVendorNotesLine(wmk));

		winemakerLogger.writeLog(String.format("<< WineMakerController.loadBatchDisplay(WineMakerLog %s): return", wmk.get_batchKey()), debugLogging);
	} // end of loadBatchDisplay()

	/**
	 * Get set of the batch's ferment log entries for populating the selection ComboBox.
	 * Keep the record set in a class variable for access in other methods
	 */
	private void loadBatchFermentData(String batchKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.loadBatchFermentData('%s')", batchKey), debugLogging);

		LocalDateTime ld = null;
		String activityName = "";
		String fermentEntry = "";

		setWineMakerFermentSet(winemakerModel.queryFermentData(batchKey));

		fermentationEntriesList.clear();

		fermentationEntries.getItems().clear();
		fermentationEntries.setPromptText("Fermentation Data");
		fermentationEntries.setButtonCell(new ButtonCell());

		for (WineMakerFerment wmf: getWineMakerFermentSet())
		{
			activityName = HelperFunctions.getCodeKeyFamily(FamilyCode.ACTIVITYFAMILY.getValue()).get(wmf.get_fermentActivity());
			ld = wmf.get_entry_date().toLocalDateTime();

			fermentEntry = String.format("%s %s", ld.format(DateTimeFormatter.ofPattern(entryTimeFormat)), activityName);
			fermentationEntriesList.add(fermentEntry);

			winemakerLogger.writeLog(String.format("   WineMakerController.loadBatchFermentData() adding '%s'", fermentEntry), debugLogging);
		}

		fermentationEntries.setItems(fermentationEntriesList.sorted());

		if (fermentationEntriesList.size() > 10)
			fermentationEntries.setVisibleRowCount(12);
		else
			fermentationEntries.setVisibleRowCount(fermentationEntriesList.size());

		winemakerLogger.writeLog(String.format("   WineMakerController.loadBatchFermentData('%s') loading %d entries, showing %d", batchKey, fermentationEntriesList.size(), fermentationEntries.getVisibleRowCount()), debugLogging);

		fermentationEntries.setVisible(true);

		winemakerLogger.writeLog(String.format("<< WineMakerController.loadBatchFermentData('%s')", batchKey), debugLogging);
	} // end of loadBatchFermentData()

	/*
	 * Get set of the batch's test log entries for populating the selection ComboBox.
	 * Keep the record set in a class variable for access in other methods
	 */
	private void loadBatchTestData(String batchKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.loadBatchTestData('%s')", batchKey), debugLogging);

		LocalDateTime ld = null;
		String testName = "";
		String testEntry = "";

		setWineMakerTestingSet(winemakerModel.queryTestingData(batchKey));

		testingEntriesList.clear();

		testingEntries.getItems().clear();
		testingEntries.setPromptText("Test Data");
		testingEntries.setButtonCell(new ButtonCell());

		for (WineMakerTesting wmt: getWineMakerTestingSet())
		{
			testName = HelperFunctions.getCodeKeyFamily(FamilyCode.LABTESTFAMILY.getValue()).get(wmt.get_testType());

			ld = wmt.get_entry_date().toLocalDateTime();
			testEntry = String.format("%s %s", ld.format(DateTimeFormatter.ofPattern(entryTimeFormat)), testName);
			testingEntriesList.add(testEntry);

			winemakerLogger.writeLog(String.format("   WineMakerController.loadBatchTestData('%s') added new entry '%s'", batchKey, testEntry), debugLogging);
		}

		testingEntries.setItems(testingEntriesList.sorted());
		testingEntries.setVisible(true);

		winemakerLogger.writeLog(String.format("<< WineMakerController.loadBatchTestData('%s') return", batchKey), debugLogging);
	} // end of loadBatchTestData()

	private void showPrimaryBatchData(WineMakerLog wmk, boolean isBlendBatch)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.showPrimarybatchData('%b'):%s ", isBlendBatch, wmk), debugLogging);

		String noIndent = "";
		statusDisplay.clear();

		if (wmk.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue()))
		{
			if (!isBlendBatch)
				batchDisplay.appendText(df.displayFirstLineGrapes(wmk));

			batchDisplay.appendText(df.displaySecondLineGrapes(wmk, noIndent));
			batchDisplay.appendText(df.displayThirdLineGrapes(wmk, noIndent));
		}

		if (wmk.get_batchSource().equals(BatchSource.JUICESOURCE.getValue()))
		{
			batchDisplay.appendText(df.displayFirstLineJuice(wmk));
			batchDisplay.appendText(df.displaySecondLineJuice(wmk));
			batchDisplay.appendText(df.displayThirdLineJuice(wmk));
		}

		if (!isBlendBatch)
			batchDisplay.appendText(df.displayVendorLine(wmk, noIndent));

		ArrayList<WineMakerInventory> wmiContainers = winemakerModel.queryInventoryByBatch(wmk.get_batchKey());
		batchDisplay.appendText(df.displayContainersInUse(wmk, wmiContainers));

		/*
		 * get batch's inventory records and summarize additive usage
		 */		
		summarizeAdditives(wmk.get_batchKey());

		winemakerLogger.writeLog(String.format("<< WineMakerController.showPrimarybatchData('%b') return", isBlendBatch), debugLogging);
	} // end of showPrimarybatchData()

	private void showBlendBatchData(WineMakerLog wmkBlend)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.showBlendBatchData():%s ", wmkBlend), debugLogging);

		String indentTwice = "\t\t";
		statusDisplay.clear();

		batchDisplay.appendText(df.displayFirstBlendLine(wmkBlend));

		if (wmkBlend.get_batchSource().equals(BatchSource.GRAPESOURCE.getValue()))
		{
			winemakerLogger.writeLog(String.format("   WineMakerController.showBlendBatchData(): call grape display "), debugLogging);
			batchDisplay.appendText(df.displaySecondLineGrapes(wmkBlend, indentTwice));
			batchDisplay.appendText(df.displayThirdLineGrapes(wmkBlend, indentTwice));
		}

		if (wmkBlend.get_batchSource().equals(BatchSource.JUICESOURCE.getValue()))
		{
			winemakerLogger.writeLog(String.format("   WineMakerController.showBlendBatchData(): call juice display "), debugLogging);
			batchDisplay.appendText(df.displaySecondLineJuice(wmkBlend, indentTwice));
			batchDisplay.appendText(df.displayThirdLineJuice(wmkBlend, indentTwice));
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.showBlendBatchData()"), debugLogging);
	} // end of showBlendBatchData()

	/*
	 * Display summary of fermentation additives used for the batch
	 */
	private void summarizeAdditives(String uiRecordKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.summarizeAdditives(%s)", uiRecordKey), debugLogging);

		HashMap<String, String> chemKeyMap = HelperFunctions.getCodeKeyFamily(FamilyCode.ADDITIVEFAMILY.getValue());
		HashMap<String, Double> chemKeySumMap = new HashMap<>();
		HashMap<String, String> chemKeyScaleMap = new HashMap<>();

		chemKeyMap.keySet()
		.stream()
		.forEach(chemName -> chemKeySumMap.put(chemName, 0.0));

		ArrayList<WineMakerInventory> assetActivityCollection = winemakerModel.queryInventoryByBatch(uiRecordKey)
				.stream()
				.filter(wmi -> wmi.getItemTaskId().length() > 0)
				.filter(wmi -> wmi.get_itemActivityAmount() > 0)
				.filter(wmi -> wmi.get_itemPurchaseCost() == 0)
				.collect(Collectors.toCollection(ArrayList::new));

		for (WineMakerInventory wmi: assetActivityCollection)
		{
			chemKeySumMap.put(wmi.get_itemName(), chemKeySumMap.get(wmi.get_itemName()) + wmi.get_itemActivityAmount());
			chemKeyScaleMap.put(wmi.get_itemName(), wmi.get_itemAmountScale());
		}

		if (assetActivityCollection.size() > 0)
			batchDisplay.appendText("\nSummary of additives used in batch:\n");

		chemKeySumMap.keySet()
		.stream()
		.filter(chemKey -> chemKeySumMap.get(chemKey) > 0)
		.forEach(chemName -> batchDisplay.appendText(String.format("\t%s: %1.2f%s%n", chemKeyMap.get(chemName), chemKeySumMap.get(chemName), chemKeyScaleMap.get(chemName))));

		winemakerLogger.writeLog(String.format("<< WineMakerController.summarizeAdditives(%s)", uiRecordKey), debugLogging);
	} // end of summarizeAdditives()

	/*
	 * Using the provided key, extract the record from the batch's current set of Ferment records.
	 * The key format = "<timestamp> <ferment activity code>"
	 */
	private WineMakerFerment getFermentEntryData(String uiRecordKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.getFermentEntryData('%s')", uiRecordKey), debugLogging);

		WineMakerFerment wmf = null;
		LocalDateTime ld = null;
		String retrievedRecordKey;

		for (WineMakerFerment wmfFind: getWineMakerFermentSet())
		{
			ld = wmfFind.get_entry_date().toLocalDateTime();

			retrievedRecordKey = String.format("%s %s", ld.format(DateTimeFormatter.ofPattern(entryTimeFormat)), HelperFunctions.getCodeKeyFamily(FamilyCode.ACTIVITYFAMILY.getValue()).get(wmfFind.get_fermentActivity()));

			if (retrievedRecordKey.equals(uiRecordKey))
			{
				wmf = wmfFind;
				winemakerLogger.writeLog(String.format("   WineMakerController.getFermentEntryData(): found %s", wmf), debugLogging);
				break;
			}
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.getFermentEntryData('%s')", uiRecordKey), debugLogging);
		return wmf;
	} // end of getFermentEntryData()

	/*
	 * Using the provided key, extract the record from the batch's current set of Test records.
	 * The key format = "<timestamp> <test code>"
	 */	
	private WineMakerTesting getTestEntryData(String uiRecordKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.getTestEntryData('%s')", uiRecordKey), debugLogging);

		WineMakerTesting wmt = null;
		LocalDateTime ld = null;
		String retrievedRecordKey;

		for (WineMakerTesting wmtFind: getWineMakerTestingSet())
		{
			ld = wmtFind.get_entry_date().toLocalDateTime();

			retrievedRecordKey = String.format("%s %s", ld.format(DateTimeFormatter.ofPattern(entryTimeFormat)), HelperFunctions.getCodeKeyFamily(FamilyCode.LABTESTFAMILY.getValue()).get(wmtFind.get_testType()));
			if (retrievedRecordKey.equals(uiRecordKey))
			{
				wmt = wmtFind;
				break;
			}
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.getTestEntryData('%s')", uiRecordKey), debugLogging);
		return wmt;
	} // end of getTestEntryData()

	/*
	 * Display the current batch's selected Fermentation entry
	 * Some activities use related records in their display
	 */
	private void displayBatchFermentEntry(String uiRecordKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.displayBatchFermentEntry('%s') ", uiRecordKey), debugLogging);

		WineMakerFerment wmf = getFermentEntryData(uiRecordKey);

		_batchId.setText(uiRecordKey);
		statusDisplay.clear();

		/*
		 * Call factory to get selected activity's display
		 */
		FermentationActivity fermentActivity = FermentationActivityFactory.getActivity(wmf.get_fermentActivity())
				.orElseThrow( () -> new IllegalArgumentException("Undefined activity " + wmf.get_fermentActivity()) );

		if (wmf.get_fermentActivity().equals(ActivityName.YEASTPITCH.getValue()))
		{
			ArrayList<WineMakerFerment> wmfSets = winemakerModel.queryFermentData(wmf.get_batchKey(), wmf.get_fermentActivity());
			wmfSets.addAll(winemakerModel.queryFermentData(wmf.get_batchKey(), ActivityName.AMELIORATION.getValue(), wmf.get_entry_date()));

			fermentActivity.setRecordList(wmfSets);
		}

		if (wmf.get_fermentActivity().equals(ActivityName.FERMENT.getValue()))
		{
			fermentActivity.setRecordList(this.getAllRackRecords.get());
		}

		if (this.tasksWithAssets.contains(wmf.get_fermentActivity()))
		{	
			ArrayList<WineMakerInventory> displayList = winemakerModel.queryInventoryByBatch(wmf.get_batchKey())
					.stream()
					.filter(wmi -> wmi.get_itemStockOnHand() > 0)
					.collect(Collectors.toCollection(ArrayList::new));

			fermentActivity.setInventoryList(displayList);
		}

		batchDisplay.setText(fermentActivity.apply(wmf));

		winemakerLogger.writeLog(String.format("<< WineMakerController.displayBatchFermentEntry('%s') ", uiRecordKey), debugLogging);
	} // end of displayBatchFermentEntry()

	/*
	 * Display selected Test record
	 */
	private void displayBatchTestEntry(String uiRecordKey)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.displayBatchTestEntry('%s') ", uiRecordKey), debugLogging);

		WineMakerTesting wmt = getTestEntryData(uiRecordKey);	

		_batchId.setText(uiRecordKey);
		statusDisplay.clear();

		String scaleText = (wmt.get_testScale().length() > 0) ? 
				HelperFunctions.getCodeKeyFamily(FamilyCode.MEASURESFAMILY.getValue()).get(wmt.get_testScale()) : "";

		batchDisplay.setText(String.format("%s = %1.1f %s", HelperFunctions.getCodeKeyFamily(FamilyCode.LABTESTFAMILY.getValue()).get(wmt.get_testType()), wmt.get_testValue(), scaleText));

		if (wmt.get_testTemp() > 0)
			batchDisplay.appendText(String.format(" at %1.0f %s", wmt.get_testTemp(), HelperFunctions.getCodeKeyFamily(FamilyCode.MEASURESFAMILY.getValue()).get(wmt.get_tempScale())));

		batchDisplay.appendText(String.format("%n%s", wmt.get_testNotes()));
		
		winemakerLogger.writeLog(String.format("<< WineMakerController.displayBatchTestEntry('%s') ", uiRecordKey), debugLogging);
	} // end of displayBatchTestEntry()

	/**
	 * Prompt user for output file location, then query database for all resource codes and write to file
	 */
	@FXML
	private void exportCodesFile(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.exportCodesFile()"), debugLogging);

		File codesOutputFile = null;
		File logDir = directoryPrompt("WineMaker Resource Codes Export Selection", "");

		if (logDir != null)
		{
			String dayStamp = String.format("%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			String timeStamp = String.format("%s", LocalTime.now().format(DateTimeFormatter.ofPattern("-HHmm")));
			String inventoryFileName = String.format("/WineMakerApp_ResourceCodesExport_%s%s.csv", dayStamp, timeStamp);

			codesOutputFile = new File(logDir.getPath() + inventoryFileName);
		}
		else
		{
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			return;
		}

		winemakerLogger.writeLog(String.format("WineMakerController.exportCodesFile(): exported file = %s", codesOutputFile.getPath()), debugLogging);

		ArrayList<String> codeRecords = this.dbOps.queryCodes();
		Collections.sort(codeRecords);

		try 
		{
			Files.write(codesOutputFile.toPath(), codeRecords, Charset.defaultCharset());
			statusDisplay.appendText(String.format("Resource codes data exported to %s%n", codesOutputFile.getPath()));
		} 
		catch (IOException ex) 
		{
			winemakerLogger.showIOException(ex, "Exception writing the resource codes export file, see log for details");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.exportCodesFile() return"), debugLogging);
	} // end of exportCodesFile()

	/*
	 * Export inventory data.   
	 */
	@FXML
	private void exportInventory(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.exportInventory()", debugLogging);

		ArrayList<WineMakerInventory> wmiQuerySet = new ArrayList<>();

		File inventoryOutputFile;		
		File logDir = directoryPrompt("WineMaker Inventory Export Directory Selection", "");

		if (logDir == null)
		{
			winemakerLogger.writeLog(String.format("<< WineMakerController.exportInventory(): User cancelled operation"), debugLogging);
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			return;
		}

		String dayStamp = String.format("%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		String timeStamp = String.format("%s", LocalTime.now().format(DateTimeFormatter.ofPattern("-HHmm")));
		String inventoryFileName = String.format("/WineMakerApp_InventoryExport_%s%s.csv", dayStamp, timeStamp);

		inventoryOutputFile = new File(logDir.getPath() + inventoryFileName);

		wmiQuerySet = winemakerModel.queryInventory();

		winemakerLogger.writeLog(String.format("   WineMakerController.exportInventory(): export file %s exporting %d records", inventoryOutputFile.getPath(), wmiQuerySet.size()), debugLogging);

		ArrayList<String> inventoryExportSet = new ArrayList<>();
		inventoryExportSet.add(WineMakerInventory.toCSVHeader());

		wmiQuerySet
			.stream()
			.forEach(wmiE -> inventoryExportSet.add(wmiE.toCSV()));

		Collections.sort(inventoryExportSet);
		writeInventoryExportFile(inventoryOutputFile, inventoryExportSet);

		statusDisplay.appendText(String.format("Inventory data exported to %s%n", inventoryOutputFile.getPath()));
		
		winemakerLogger.writeLog(String.format("<< WineMakerController.exportInventory(): %s contains %d rows", inventoryOutputFile.toPath(), inventoryExportSet.size()), debugLogging);
	} // end of exportInventory()

	/*
	 * Export inventory data.   
	 */
	@FXML
	private void reportInventory(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.reportInventory()", debugLogging);

		ArrayList<WineMakerInventory> wmiQuerySet = new ArrayList<>();

		File inventoryOutputFile;		
		File logDir = directoryPrompt("WineMaker Inventory Report Directory Selection", "");

		if (logDir == null)
		{
			winemakerLogger.writeLog(String.format("<< WineMakerController.reportInventory(): User cancelled operation"), debugLogging);
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			return;
		}

		String dayStamp = String.format("%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		String timeStamp = String.format("%s", LocalTime.now().format(DateTimeFormatter.ofPattern("-HHmm")));
		String inventoryFileName = String.format("/WineMakerApp_InventoryReport_%s%s.csv", dayStamp, timeStamp);

		inventoryOutputFile = new File(logDir.getPath() + inventoryFileName);

		wmiQuerySet = winemakerModel.queryInventory();
		HashMap<String, String> containerAssets = HelperFunctions.getCodeKeyMappings().get(FamilyCode.CONTAINERFAMILY.getValue());
		HashMap<String, String> additiveAssets = HelperFunctions.getCodeKeyMappings().get(FamilyCode.ADDITIVEFAMILY.getValue());
		HashMap<String, String> yeastAssets = HelperFunctions.getCodeKeyMappings().get(FamilyCode.YEASTFAMILY.getValue());

		winemakerLogger.writeLog(String.format("   WineMakerController.reportInventory(): export file %s exporting %d records", inventoryOutputFile.getPath(), wmiQuerySet.size()), debugLogging);

		ArrayList<String> inventoryReportSet = new ArrayList<>();
		ArrayList<String> containerReportSet = new ArrayList<>();
		ArrayList<String> additiveReportSet = new ArrayList<>();
		ArrayList<String> yeastReportSet = new ArrayList<>();

		wmiQuerySet
			.stream()
			.filter(wmiTest -> containerAssets.containsKey(wmiTest.get_itemName()))
			.forEach(wmiE -> containerReportSet.add(wmiE.toReport()));
		wmiQuerySet
			.stream()
			.filter(wmiTest -> additiveAssets.containsKey(wmiTest.get_itemName()))
			.forEach(wmiE -> additiveReportSet.add(wmiE.toReport()));
		wmiQuerySet
			.stream()
			.filter(wmiTest -> yeastAssets.containsKey(wmiTest.get_itemName()))
			.forEach(wmiE -> yeastReportSet.add(wmiE.toReport()));

		ArrayList<String> removeEmpty = new ArrayList<>();
		removeEmpty.add("");
		containerReportSet.removeAll(removeEmpty);
		additiveReportSet.removeAll(removeEmpty);
		yeastReportSet.removeAll(removeEmpty);

		Collections.sort(containerReportSet);
		Collections.sort(additiveReportSet);
		Collections.sort(yeastReportSet);
		
		containerReportSet.add(0, "Containers:");
		additiveReportSet.add(0, "\nAdditives:");
		yeastReportSet.add(0, "\nYeasts:");

		inventoryReportSet.addAll(containerReportSet);
		inventoryReportSet.addAll(additiveReportSet);
		inventoryReportSet.addAll(yeastReportSet);
		writeInventoryExportFile(inventoryOutputFile, inventoryReportSet);

		winemakerLogger.writeLog(String.format("<< WineMakerController.reportInventory(): %s contains %d rows", inventoryOutputFile.toPath(), inventoryReportSet.size()), debugLogging);
	} // end of reportInventory()

	/*
	 * Extract all records, send to export method
	 */
	@FXML
	private void exportAllBatches(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.exportAllBatches()", debugLogging);

		File logDir = directoryPrompt("WineMaker All Batches Export Directory Selection", "");
		if (logDir == null)
		{
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			winemakerLogger.writeLog(String.format("<< WineMakerController.exportAllBatches(): User cancelled operation"), debugLogging);
			return;			
		}

		ArrayList<WineMakerLog> wmkSets = new ArrayList<>();
		wmkSets = winemakerModel.queryBatch("", SQLSearch.PARENTBATCH)
				.stream()
				.filter(wmk -> wmk.get_batchBlendKey().length() == 0)
				.collect(Collectors.toCollection(ArrayList::new));

		exportBatchData(wmkSets, logDir);

		winemakerLogger.writeLog("<< WineMakerController.exportAllBatches()", debugLogging);
	} // end of exportAllBatches()

	/*
	 * Extract all records for selected batch, send to export method
	 */
	@FXML
	private void exportExistingBatch(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.exportExistingBatch()", debugLogging);

		if (batchSets.getValue() == null)
		{
			winemakerLogger.showAlarm("You must select a batch", AlertType.CONFIRMATION);
			winemakerLogger.writeLog("<< WineMakerController.exportExistingBatch()", debugLogging);
			return;
		}

		File logDir = directoryPrompt("WineMaker Single Batch Export Directory Selection", "");
		if (logDir == null)
		{
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			winemakerLogger.writeLog(String.format("<< WineMakerController.exportExistingBatch(): User cancelled operation"), debugLogging);
			return;
		}

		ArrayList<WineMakerLog> wmkSets = new ArrayList<>();
		wmkSets = winemakerModel.queryBatch(HelperFunctions.batchKeyCompress(batchSets.getValue()), SQLSearch.PARENTBATCH);

		exportBatchData(wmkSets, logDir);

		winemakerLogger.writeLog("<< WineMakerController.exportExistingBatch()", debugLogging);
	} // end of exportExistingBatch()

	/*
	 * Export batch data to .CSV files.  Output is 3 time-stamped files:
	 * 		Batch table data
	 * 		Ferment table data 
	 * 		Testing table data
	 */
	private void exportBatchData(ArrayList<WineMakerLog> wmkSets, File logDir)
	{
		winemakerLogger.writeLog(">> WineMakerController.exportBatchData()", debugLogging);

		File batchOutputFile, fermentOutputFile, testingOutputFile;

		String dateTimeStamp = String.format("%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
		String batchFileName = String.format("/WineMakerApp_BatchExport_%s.csv", dateTimeStamp);
		String fermentFileName = String.format("/WineMakerApp_BatchFermentExport_%s.csv", dateTimeStamp);
		String testingFileName = String.format("/WineMakerApp_BatchTestingExport_%s.csv", dateTimeStamp);

		batchOutputFile = new File(logDir.getPath() + batchFileName);
		fermentOutputFile = new File(logDir.getPath() + fermentFileName);
		testingOutputFile = new File(logDir.getPath() + testingFileName);

		winemakerLogger.writeLog(String.format("   WineMakerController.exportBatchData(): exporting %d parent records", wmkSets.size()), debugLogging);		
		winemakerLogger.writeLog(String.format("   WineMakerController.exportBatchData(): export files = %n\t'%s'%n\t'%s'%n\t", batchOutputFile.getPath(), fermentOutputFile.getPath(), testingOutputFile.getPath()), debugLogging);

		ArrayList<String> batchSet = new ArrayList<>();
		ArrayList<String> fermentSet = new ArrayList<>();
		ArrayList<String> testingSet = new ArrayList<>();

		/*
		 * Find additional records if this is a blend
		 */
		collectBatchRecordSets(wmkSets, batchSet, fermentSet, testingSet);

		writeBatchExportFile(batchOutputFile, fermentOutputFile, testingOutputFile, batchSet, fermentSet, testingSet);

		winemakerLogger.writeLog(String.format("<< WineMakerController.exportBatchData(): %s contains %d lines, %s contains %d", batchOutputFile.toPath(), batchSet.size(), fermentOutputFile.toPath(), fermentSet.size()), debugLogging);
	} // end of exportBatchData()

	/*
	 * For blends, collect any associated records
	 */
	private void collectBatchRecordSets(ArrayList<WineMakerLog> wmkSets, ArrayList<String> batchSet, ArrayList<String> fermentSet, ArrayList<String> testingSet)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.collectBatchRecordSets()"), debugLogging);

		ArrayList<WineMakerFerment> wmfSets = new ArrayList<>();
		ArrayList<WineMakerTesting> wmtSets = new ArrayList<>();
		ArrayList<WineMakerLog> wmkBlendSets = new ArrayList<>();

		batchSet.add(WineMakerLog.toCSVHeader());
		fermentSet.add(WineMakerFerment.toCSVHeader());
		testingSet.add(WineMakerTesting.toCSVHeader());

		for (WineMakerLog wmkE: wmkSets)
		{	
			if (HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPEFAMILY.getValue()).get(wmkE.get_batchGrape()) == null)
			{
				wmkBlendSets = winemakerModel.queryBatch(wmkE.get_batchKey(), SQLSearch.BLENDCOMPONENT);
				winemakerLogger.writeLog(String.format(   "WineMakerController.collectBatchRecordSets(): exporting %d blend records for '%s'", wmkBlendSets.size(), wmkE.get_batchKey()), debugLogging);
			}			

			wmfSets = winemakerModel.queryFermentData(wmkE.get_batchKey());
			winemakerLogger.writeLog(String.format("   WineMakerController.collectBatchRecordSets(): exporting %d ferment records for '%s'", wmfSets.size(), wmkE.get_batchKey()), debugLogging);

			wmtSets = winemakerModel.queryTestingData(wmkE.get_batchKey());
			winemakerLogger.writeLog(String.format("   WineMakerController.collectBatchRecordSets(): exporting %d test records for '%s'", wmfSets.size(), wmkE.get_batchKey()), debugLogging);

			batchSet.add(wmkE.toCSV());
			batchSet.addAll(
					wmkBlendSets
					.stream()
					.map(wmkBE -> wmkBE.toCSV())
					.collect(Collectors.toList()));
			fermentSet.addAll(
					wmfSets
					.stream()
					.map(wmkFE -> wmkFE.toCSV())
					.collect(Collectors.toList()));
			testingSet.addAll(
					wmtSets
					.stream()
					.map(wmkTE -> wmkTE.toCSV())
					.collect(Collectors.toList()));
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.collectBatchRecordSets()"), debugLogging);
	} // end of collectBatchRecordSets()

	/**
	 * Prompt user for output file location, then copy internal log file to new file 
	 */
	public void exportLogFile(ActionEvent e)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.exportLogFile()"), debugLogging);

		File outputFile;
		File logDir = directoryPrompt("WineMaker App Log Export Directory Selection", "");

		if (logDir != null)
		{
			String dayStamp = String.format("%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			String timeStamp = String.format("%s", LocalTime.now().format(DateTimeFormatter.ofPattern("-HHmm")));
			String fileName = "/WineMakerApp_Log_" + dayStamp + timeStamp + ".txt";

			outputFile = new File(logDir.getPath() + fileName);	
		}
		else
		{
			statusDisplay.setText("Directory selection cancelled, export operation terminated");
			return;
		}

		File inputFile = new File(winemakerLogger.getLogFile().getPath());
		winemakerLogger.writeLog(String.format("   WineMakerController.exportLogFile(): source file = %s", inputFile.getPath()), debugLogging);
		winemakerLogger.writeLog(String.format("   WineMakerController.exportLogFile(): exported file = %s", outputFile.getPath()), debugLogging);

		if (copyFile(inputFile, outputFile))
			statusDisplay.setText(String.format("Failure exporting log file %s, see log for details", outputFile.getPath()));
		else
			statusDisplay.setText(String.format("Log file exported to %s", outputFile.getPath()));

		winemakerLogger.writeLog(String.format("<< WineMakerController.exportLogFile() return"), debugLogging);
	} // end of exportLogFile()

	public void showAbout(ActionEvent ev)
	{
		Alert alertWarning = new Alert(AlertType.INFORMATION, "", ButtonType.OK);

		alertWarning.setTitle("WineMaker Version");
		alertWarning.setContentText("Version " + WineMakerModel.getAppVersion());
		alertWarning.showAndWait();
	}

	/*
	 * Perform file copy operation
	 */
	private boolean copyFile(File inputFile, File outputFile)
	{
		boolean copyFailed = false;

		try 
		{
			Files.copy(inputFile.toPath(), outputFile.toPath());
		} 
		catch (FileAlreadyExistsException ef)
		{
			copyFailed = true;
			winemakerLogger.displayAlert("Duplicate file name, count to 10 and try again");
		}
		catch (IOException e) 
		{
			copyFailed = true;
			winemakerLogger.showIOException(e, "Exception attempting to export log file");
		}

		return copyFailed;
	} // end of copyFile()

	/*
	 * Replace the Inventory table with a new set of assets, or insert new asset entries.
	 */
	@FXML
	private void loadInventory(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.loadInventory()", debugLogging);

		boolean loadOK = true;

		if (winemakerLogger.showAlarm("Replace all of the existing entries? Cancel keeps existing data.", AlertType.CONFIRMATION))
		{
			winemakerModel.dropTable(DatabaseTables.INVENTORY.getValue());
			winemakerModel.createTable(DatabaseTables.INVENTORY.getValue());
		}

		File assetsFile = filePrompt("Select .CSV file of new inventory entries");
		String[] assetsArray = HelperFunctions.returnFileContents(assetsFile);
		loadOK = this.dbOps.insertAssets(assetsArray);

		if (loadOK)
			statusDisplay.appendText("Inventory assets data successfully loaded");
		else
			statusDisplay.appendText("Failure to load Inventory assets data, check log for errors");	

		winemakerLogger.writeLog("<< WineMakerController.loadInventory()", debugLogging);
	} // end of loadInventory()

	/*
	 * Replace the resource codes table with a new set of codes, or insert new codes.
	 * If successful, re-stage the codes into the static Resource Codes objects
	 */
	@FXML
	private void loadCodes(ActionEvent e)
	{
		winemakerLogger.writeLog(">> WineMakerController.loadCodes()", debugLogging);

		boolean loadOK = true;
		if (winemakerLogger.showAlarm("Replace all of the existing codes? Cancel keeps existing data.", AlertType.CONFIRMATION))
		{
			winemakerModel.dropTable(DatabaseTables.CODES.getValue());
			winemakerModel.createTable(DatabaseTables.CODES.getValue());
		}

		File codesFile = filePrompt("Select .CSV file of new resource codes");
		String[] codesArray = HelperFunctions.returnFileContents(codesFile);

		loadOK = this.dbOps.insertCodes(codesArray);

		HelperFunctions.loadCodeRecords(this.dbOps.queryCodes());

		if (loadOK && HelperFunctions.getCodeKeyMappings().size() > 0)
			statusDisplay.appendText("Resource Codes data successfully loaded");
		else
			statusDisplay.appendText("Failure to load Resource Codes data, check log for errors");			

		winemakerLogger.writeLog("<< WineMakerController.loadCodes()", debugLogging);
	} // end of loadCodes()

	/*
	 * Two functions:
	 * 		Stage the resource code mappings in the current model object
	 * 		Load the batch selection list with the identifiers of each batch
	 */
	private void loadCodesAndBatches()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.loadCodesAndBatches()"), debugLogging);

		try
		{
			HelperFunctions.loadCodeRecords(this.dbOps.queryCodes());
			loadBatchSets();
		}
		catch (Exception e)
		{
			statusDisplay.setText("Failure to load resource codes, see log file");
			winemakerLogger.showIOException(e, "loadCodesAndBatches(): couldn't load resource codes");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerController.loadCodesAndBatches()"), debugLogging);
	} // end of loadCodesAndBatches()

	/*
	 * Get set of batches for populating the selection ComboBox.  The query returns all batch records, and then the 
	 * blend component batches will be filtered out before populating the ComboBox record set.
	 */
	private void loadBatchSets()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.loadBatchSets() "), debugLogging);

		batchSets.getItems().clear();
		batchSets.setPromptText("Select a Batch");
		batchSets.setButtonCell(new ButtonCell());
		batchDisplay.clear();
		_batchId.setText("");

		this.wmkSets = winemakerModel.queryBatch("", SQLSearch.PARENTBATCH);

		winemakerLogger.writeLog(String.format("   WineMakerController.loadBatchSets(): retrieved %d records", this.wmkSets.size()), debugLogging);

		/*
		 * Load keys of all of the non-blend batches
		 */
		batchSetsList.addAll(wmkSets
				.stream()
				.filter(wmk -> wmk.get_batchBlendKey().length() == 0)
				.map(wmk -> HelperFunctions.batchKeyExpand(wmk))
				.sorted(Comparator.naturalOrder())
				.collect(Collectors.toList()));
		batchSets.setItems(batchSetsList);

		fermentationEntriesList.clear();
		fermentationEntries.getItems().clear();
		fermentationEntries.setPromptText("Fermentation Data");
		fermentationEntries.setButtonCell(new ButtonCell());

		testingEntriesList.clear();
		testingEntries.getItems().clear();
		testingEntries.setPromptText("Test Data");
		testingEntries.setButtonCell(new ButtonCell());

		addActivity.setVisible(false);
		addTestData.setVisible(false);

		winemakerLogger.writeLog(String.format("<< WineMakerController.loadBatchSets()"), debugLogging);
	} // end of loadBatchSets()

	private void loadToolTips()
	{
		startNewBatch.setTooltip(HelperFunctions.buildTooltip(NEWBATCH));
		addActivity.setTooltip(HelperFunctions.buildTooltip(NEWTASK));
		addTestData.setTooltip(HelperFunctions.buildTooltip(NEWTEST));
	} // end of loadToolTips()

	/*
	 * Analyze the messages returned by the database table validation method.
	 * Messages indicating the absence of a table will include the string 'TBE'. 
	 * If all the tables returned this message then this must be an initial run of the application
	 * In that case, call the method to create all the tables.
	 */
	private void analyzeTableValidation(String messages)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerController.analyzeTableValidation(messages[])"), debugLogging);

		int tableCount = this.dbOps.getTableValidationSet().size();
		int errorCount = 0;
		int startIndex = 0;

		Pattern p = Pattern.compile("TBE", Pattern.LITERAL);
		Matcher m = p.matcher(messages);

		while(m.find(startIndex))
		{
			errorCount++;
			startIndex = m.start() + 1;
		}		
		if (tableCount == errorCount)
		{
			winemakerLogger.writeLog(String.format("   WineMakerController.analyzeTableValidation(): all tables are missing, call reset method"), debugLogging);
			winemakerLogger.displayAlert("Missing tables are being created");

			if (!winemakerModel.resetTables())
				statusDisplay.appendText("Failure to create missing tables, check log for errors");
		}
		else
		{
			messages += "\nOne of more database tables are not defined, or connection to database has failed: functions are disabled\n";
			statusDisplay.setText(messages);
		}
		winemakerLogger.writeLog(String.format("<< WineMakerController.analyzeTableValidation()"), debugLogging);
	} // end of analyzeTableValidation()

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
		df = new DisplayFormatter();
		addActivity.setVisible(false);
		addTestData.setVisible(false);

		HelperFunctions.loadPropertiesFile();
		if (!HelperFunctions.loadCodeRecords(this.dbOps.queryCodes()))
		{
			winemakerLogger.displayAlert("Failed to load data, check connection to database");
			System.exit(8);
		}

		loadToolTips();

		this.tasksWithAssets.add(ActivityName.CRUSH.getValue());
		this.tasksWithAssets.add(ActivityName.PRESS.getValue());
		this.tasksWithAssets.add(ActivityName.TRANSFER.getValue());
		this.tasksWithAssets.add(ActivityName.RACK.getValue());

		/*
		 * Make this an array so the stream can update the accumulated message string directly
		 */
		String displayMessages[] = {""};
		int resetTableCounter = 0;

		winemakerLogger.writeLog(String.format(">> WineMakerController.initialize() call database validate"), debugLogging);

		do 
		{
			displayMessages[0] = "";

			this.dbOps.validateAllTables()
			.stream()
			.forEach(msg -> displayMessages[0] += msg);		

			/*
			 * If all of the tables are validated then load the codes into a static object
			 * and populate the list of existing batches
			 */
			if (displayMessages[0].length() == 0)
			{
				loadCodesAndBatches();
			}
			else
			{
				winemakerLogger.writeLog(String.format("   WineMakerController.initialize() validate returned '%s'", displayMessages[0]), debugLogging);
				analyzeTableValidation(displayMessages[0]);
			}
		} while (displayMessages[0].length() > 0 || resetTableCounter > 1);

		_batchId.setVisible(false);
		batchDisplay.setVisible(false);


		fermentationEntries.setVisible(false);
		testingEntries.setVisible(false);

		winemakerLogger.writeLog(String.format("<< WineMakerController.initialize() Finished%n"), debugLogging);

		/*
		 * Event Handler for batch ComboBox
		 */
		EventHandler<ActionEvent> batchListHandler = new EventHandler<ActionEvent>() 
		{
			@Override
			public void handle(ActionEvent e) 
			{
				winemakerModel.setFxStage(batchSets.getScene().getWindow());

				ComboBox<?> sourceObject = (ComboBox<?>) e.getSource();

				if (sourceObject.getValue() != null)
				{
					winemakerLogger.writeLog(String.format(">> WineMakerController.batchList.handler(): display '%s' ", batchSets.getValue()), debugLogging);

					String compressedBatchKey = HelperFunctions.batchKeyCompress(batchSets.getValue());
					getBatchData(compressedBatchKey);
					loadBatchFermentData(compressedBatchKey);
					loadBatchTestData(compressedBatchKey);

					addActivity.setVisible(true);
					addTestData.setVisible(true);

					winemakerLogger.writeLog(String.format("<< WineMakerController.batchList.handler(): display '%s' ", batchSets.getValue()), debugLogging);
				}
			}
		};
		batchSets.setOnAction(batchListHandler);

		/*
		 * Event Handler for fermentation entries ComboBox
		 */
		EventHandler<ActionEvent> fermentEntryListHandler = new EventHandler<ActionEvent>() 
		{
			@Override
			public void handle(ActionEvent e) 
			{
				winemakerLogger.writeLog(String.format(">> WineMakerController.fermentEntryList.handler(): display '%s' ", fermentationEntries.getValue()), debugLogging);

				ComboBox<?> sourceObject = (ComboBox<?>) e.getSource();
				if (sourceObject.getValue() != null)
					displayBatchFermentEntry(fermentationEntries.getValue());

				winemakerLogger.writeLog(String.format("<< WineMakerController.fermentEntryList.handler(): display '%s' ", fermentationEntries.getValue()), debugLogging);
			}
		};
		fermentationEntries.setOnAction(fermentEntryListHandler);

		/*
		 * Event Handler for test entries ComboBox
		 */
		EventHandler<ActionEvent> testEntryListHandler = new EventHandler<ActionEvent>() 
		{
			@Override
			public void handle(ActionEvent e) 
			{
				winemakerLogger.writeLog(String.format(">> WineMakerController.testEntryList.handler(): display '%s' ", testingEntries.getValue()), debugLogging);

				ComboBox<?> sourceObject = (ComboBox<?>) e.getSource();

				if (sourceObject.getValue() != null)
					displayBatchTestEntry(testingEntries.getValue());

				winemakerLogger.writeLog(String.format("<< WineMakerController.testEntryList.handler(): display '%s' ", testingEntries.getValue()), debugLogging);
			}

		};
		testingEntries.setOnAction(testEntryListHandler);
	}
}
