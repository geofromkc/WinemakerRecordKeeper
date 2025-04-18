/**
 * 
 */
package geo.apps.winemaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import geo.apps.winemaker.utilities.DatabaseOperations;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.Registry;
import geo.apps.winemaker.utilities.Constants.*;
import geo.apps.winemaker.utilities.WineMakerLogging;

/**
 * @author geofr
 *
 */
public class WineMakerModel {

	private WineMakerLogging winemakerLogger = null;
	private DatabaseOperations dbOps = null;
	
	private static final String appVersion = "3.2.0";

	private static final String jdbcBase = "jdbc:derby://localhost:1527/";
	private static final String defaultAppName = "/WineMakerApp";
	private static final String defaultBackupDirName = "/WineMakerAppBackup";
	private static final String propertiesFileName = "/WINEMAKER.properties";
	private static final String startupFileName = "/STARTED.txt";
	private static final String localAppdataHome = System.getenv("LOCALAPPDATA") + defaultAppName;
	private static final String appdataHome = System.getenv("APPDATA");
	private static final String defaultAppSearch = System.getenv("USERPROFILE") + "/Documents";
	private static final String defaultAppHome = appdataHome + defaultAppName;
	private static final String defaultBackupHome = appdataHome + defaultBackupDirName;
	private static final File propsFile = new File(localAppdataHome + propertiesFileName);

	private WineMakerLog wmk;

	private boolean haveTable = false;
	private int tableCount = 5;
	private boolean debugProcess = true;

	private String[] databaseTableList = new String[tableCount];

	private File inputFile;
	private Window fxStage;

	/*
	 * 
	private File appFilesDir = null;
	private File backupFilesDir = null;
	 */

	private Properties appProperties = new Properties();
	
	private String codeFilePath = null;
	private boolean debugActive = false;
	private boolean debugLogging = true;

	/*
	 * =======================================================================
	 */
	public WineMakerModel() 
	{
		super();
		
		databaseTableList[0] = DatabaseTables.PRIMARY.getValue();
		databaseTableList[1] = DatabaseTables.CODES.getValue();
		databaseTableList[2] = DatabaseTables.TESTS.getValue();
		databaseTableList[3] = DatabaseTables.ACTIVITY.getValue();
		databaseTableList[4] = DatabaseTables.INVENTORY.getValue();
		
		Registry appRegistry = HelperFunctions.getRegistry();
		this.winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);

		wmk = new WineMakerLog(this);
	}

	public static String getAppVersion()
	{
		return appVersion;
	}

	/*
	 * Object property methods
	 */
	public void setDbOps(DatabaseOperations dbOps)
	{
		this.dbOps = dbOps;
	}
	
	public String getCodeFilePath() {
		return this.codeFilePath;
	}

	public String getLogFilePath() {
		return this.codeFilePath;
	}

	public static String getJdbcbase() {
		return jdbcBase;
	}

	public static String getDefaultappname() {
		return defaultAppName;
	}

	public static String getDefaultbackupdirname() {
		return defaultBackupDirName;
	}

	public static String getPropertiesfilename() {
		return propertiesFileName;
	}

	public static String getStartupfilename() {
		return startupFileName;
	}

	public Properties getAppProperties() {
		return appProperties;
	}

	public static String getLocalappdatahome() {
		return localAppdataHome;
	}

	public static String getDefaultapphome() {
		return defaultAppHome;
	}

	public static String getDefaultbackuphome() {
		return defaultBackupHome;
	}

	public static String getDefaultappsearch() {
		return defaultAppSearch;
	}

	public WineMakerLogging getApplicationLogger() {
		return this.winemakerLogger;
	}
	
	public boolean getDebugActive() {
		return debugActive;
	}

	public void setDebugActive(boolean debugState) {
		this.debugActive = debugState;
	}

	public boolean isHaveTable() {
		return this.haveTable;
	}

	public void setHaveTable(boolean haveTable) {
		this.haveTable = haveTable;
	}

	public Window getFxStage() {
		return this.fxStage;
	}

	public void setFxStage(Window fxStage) {
		this.fxStage = fxStage;
	}

	public WineMakerLog getWmk() {
		return this.wmk;
	}

	public void setWmk(WineMakerLog wmk) {
		this.wmk = wmk;
	}

	public boolean isDebugProcess() {
		return this.debugProcess;
	}

	public void setDebugProcess(boolean debugProcess) {
		this.debugProcess = debugProcess;
	}

	public static File getPropsfile() {
		return propsFile;
	}

	/**
	 * Load the application properties file.  
	 * @return boolean flag representing success or failure
	 */
	public boolean loadProperties()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.loadProperties()"), debugLogging);

		boolean returnState = true;
		InputStream fileStream = null;		
		File propsFile = getPropsfile();
		winemakerLogger.writeLog(String.format("   WineMakerModel.loadProperties(): using properties file %s", propsFile.getPath()), debugLogging);
		
	    try 
	    {
			fileStream = new FileInputStream(propsFile);
		} 
	    catch (FileNotFoundException e1) 
	    {
			winemakerLogger.writeLog(String.format("   WineMakerModel.loadProperties(): properties file not found at %s", propsFile.getPath()), debugLogging);
			winemakerLogger.showIOException(e1, "Application properties file not found at: " + propsFile.getPath());
			winemakerLogger.writeLog(String.format("<< WineMakerModel.loadProperties()"), debugLogging);
			return false;
	    }
	    
		try 
		{
			appProperties.load(fileStream);
			if (appProperties.isEmpty())
			{
				winemakerLogger.writeLog(String.format("   WineMakerModel.loadPropertiesFile(): the application properties file at %s failed to load", propsFile.getPath()), debugLogging);
				winemakerLogger.writeLog(String.format("<< WineMakerModel.loadProperties()"), debugLogging);
				return false;
			}
		}
		catch (IOException e2) 
		{
			winemakerLogger.writeLog(String.format("   WineMakerModel.loadProperties(): IO Exception loading the application properties file %s", propsFile.getPath()), debugLogging);
			winemakerLogger.showIOException(e2, "IO Exception loading the application properties file: " + propsFile.getPath());
			winemakerLogger.writeLog(String.format("<< WineMakerModel.loadProperties()"), debugLogging);
			return false;
		}

		System.setProperty("derby.system.home", appProperties.getProperty("DBAPPDIR"));
		System.setProperty("derby.stream.error.file", appProperties.getProperty("DBAPPDIR") + "/derby.log");
		
		winemakerLogger.writeLog(String.format("   WineMakerModel.loadPropertiesFile(): set derby.system.home to %s", System.getProperty("derby.system.home")), debugLogging);				
		winemakerLogger.writeLog(String.format("   WineMakerModel.loadPropertiesFile(): set derby.stream.error.file to %s", System.getProperty("derby.stream.error.file")), debugLogging);				
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.loadProperties()"), debugLogging);
		return returnState;
	} // end of loadProperties()

	/**
	 * Validate existence of specified table
	 * @param tableName String containing name of table
	 * @return String object with validation error message, or an empty string for success
	 */
	public String validateTable(String tableName) 
	{
		return this.dbOps.validateTable(tableName);
	}

	/*
	 * Common method calling database operations' table create
	 */
	
	/**
	 * Common method to create database tables
	 * @param tableName String containing name of table
	 * @return String containing error message or empty for success
	 */
	public String createTable(String tableName)
	{
		return this.dbOps.createTable(tableName);
	}

	/**
	 * Common method to drop database tables
	 * @param tableName String containing name of table
	 * @return String containing error message or empty for success
	 */
	public String dropTable(String tableName)
	{
		return this.dbOps.dropTable(tableName);
	}

	/**
	 * Call method to create a database backup
	 * @return String containing error message or empty for success
	 */
	public String backupDatabase()
	{
		return this.dbOps.backUpDatabase();
	}

	/**
	 * Call method to restore the database
	 * @param restoreData String containing path of the selected backup
	 */
	public void restoreDatabase(String restoreData)
	{
		this.dbOps.restoreDatabase(restoreData);
	}

	/**
	 * Call methods to move database
	 * @param moveDataTarget String containing target directory for database 
	 * @param moveBackupTarget String containing target directory for backup files
	 * @param moveBackupSource String containing current location of backups
	 */
	public void moveDatabase(String moveDataTarget, String moveBackupTarget, String moveBackupSource)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.moveDatabase('%s', '%s', '%s')", moveDataTarget, moveBackupTarget, moveBackupSource), debugLogging);
        
		this.dbOps.moveDatabase(moveDataTarget);
		this.dbOps.copyDirectory(moveBackupSource, moveBackupTarget);

		winemakerLogger.writeLog(String.format("<< WineMakerModel.moveDatabase('%s', '%s', '%s')", moveDataTarget, moveBackupTarget, moveBackupSource), debugLogging);
	} // end of moveDataBase()

	/**
	 * Add new batch to primary batch table
	 * 
	 * @param New WineMakerLog object with data for new batch
	 * @return True or False reflecting the SQL operation result
	 */
	public boolean insertBatch(WineMakerLog wmk) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.insertBatch(%s)", wmk.get_batchKey()), debugLogging);
		
		boolean returnState = this.dbOps.insertBatch(wmk);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.insertBatch(%s)", wmk.get_batchKey()), debugLogging);
		return returnState;
	} // end of insertBatch()

	/**
	 * Add new ferment data to ferment table
	 * 
	 * @param New WineMakerFerment object with data for a new log entry
	 * @return True or False reflecting the SQL operation result
	 */
	public boolean insertFermentData(WineMakerFerment wmf) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.insertFermentData(%s)", wmf.get_batchKey()), debugLogging);

		boolean returnState = this.dbOps.insertFermentData(wmf);

		winemakerLogger.writeLog(String.format("<< WineMakerModel.insertFermentData(%s)", wmf.get_batchKey()), debugLogging);
		return returnState;
	} // end of insertFermentData()

	/**
	 * Add new test data for the current batch
	 * 
	 * @param wmt WineMakerTesting object to be added
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean insertTestData(WineMakerTesting wmt)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.insertTestData(%s)", wmt.get_batchKey()), debugLogging);

		boolean returnState = this.dbOps.insertNewTestData(wmt);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.insertTestData(%s)", wmt.get_batchKey()), debugLogging);
		return returnState;
	} // end of insertTestData()
	
	/**
	 * Add new resource code
	 * @param codeType String containing resource code category, like 'grape'
	 * @param codeValue String containing resource code record key, like 'carm'
	 * @param codeDesc String containing resource code display value, like 'carmenere'
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean insertNewCode(String codeType, String codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.insertNewCode(%s, %s, %s)", codeType, codeValue, codeDesc), debugLogging);

		boolean returnState = this.dbOps.insertNewCode(codeType, codeValue, codeDesc);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.insertNewCode(%s, %s, %s)", codeType, codeValue, codeDesc), debugLogging);
		return returnState;
	} // end of insertNewCode()

	/**
	 * Add new inventory record
	 * @param wmi WineMakerInventory record to be added
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean insertInventory(WineMakerInventory wmi)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.insertInventory(%s)", wmi.get_itemName()), debugLogging);

		boolean returnState = this.dbOps.insertNewInventoryItem(wmi);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.insertInventory(%s)", wmi.get_itemName()), debugLogging);
		return returnState;
	} // end of insertInventory()
	
	/**
	 * Update current batch record
	 * 
	 * @param batchKey String value of the batch key
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean updateBatch(WineMakerLog wmk) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.updateBatch(%s)", wmk.get_batchKey()), debugLogging);

		boolean returnState = this.dbOps.updateBatch(wmk);
				
		winemakerLogger.writeLog(String.format("<< WineMakerModel.updateBatch(%s)", wmk.get_batchKey()), debugLogging);
		return returnState;
	} // end of updateBatch()

	/**
	 * Call method to update an inventory record
	 * @param wmi WineMakerInventory object containing the updated record data
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean updateInventory(WineMakerInventory wmi) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.updateInventory(%s)", wmi.get_itemName()), debugLogging);

		boolean returnState = this.dbOps.updateInventory(wmi);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.updateInventory(%s)", wmi.get_itemName()), debugLogging);
		return returnState;
	} // end of updateInventory()

	public boolean updateInventoryBatch(WineMakerInventory wmi) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.updateInventoryBatch(%s)", wmi.get_itemName()), debugLogging);

		boolean returnState = this.dbOps.updateInventoryBatch(wmi);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.updateInventoryBatch(%s)", wmi.get_itemName()), debugLogging);
		return returnState;
	} // end of updateInventoryBatch()

	/**
	 * Call method to delete current batch records. This includes all related records in the
	 * Ferment and Testing tables
	 * @param batchKey String value of the batch key
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean deleteBatch(String batchKey) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.deleteBatch(%s)", batchKey), debugLogging);

		boolean returnState = this.dbOps.deleteBatch(batchKey);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.deleteBatch(%s)", batchKey), debugLogging);
		return returnState;
	} // end of deleteBatch()

	/**
	 * Call method to delete records from tables with java.sql.Timestamp keys, currently the Ferment and Testing tables
	 * @param entryDate Timestamp key date
	 * @param tableName String table name
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean deleteDateRecord(Timestamp entryDate, String tableName)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.deleteDateRecord('%s', '%s')", entryDate, tableName), debugLogging);

		boolean returnState = this.dbOps.deleteDateRecord(entryDate, tableName);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.deleteDateRecord('%s', '%s')", entryDate, tableName), debugLogging);
		return returnState;
	} // end of deleteDateRecord()

	/**
	 * Call method to delete a resource code entry
	 * @param codeType String containing resource code category, like 'grape'
	 * @param codeValue String containing resource code record key, like 'carm'
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean deleteCode(String codeType, String codeValue)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.deleteCode(%s, %s)", codeType, codeValue), debugLogging);

		boolean returnState = (this.dbOps).deleteCode(codeType, codeValue);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.deleteCode(%s, %s)", codeType, codeValue), debugLogging);
		return returnState;
	} // end of deleteCode()
	
	/**
	 * Query all inventory records
	 * @return ArrayList<WineMakerInventory> Set of returned inventory records
	 */
	public ArrayList<WineMakerInventory> queryInventory()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryInventory()"), debugLogging);

		ArrayList<WineMakerInventory> returnData = this.dbOps.queryInventoryData();
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryInventory()"), debugLogging);
		return returnData;
	} // end of queryInventory()

	/**
	 * 
	 * @param String ID of batch utilizing this inventory set
	 * @return ArrayList<WineMakerInventory> Set of returned inventory records
	 */
	public ArrayList<WineMakerInventory> queryInventoryByBatch(String batchId)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryInventory(%s)", batchId), debugLogging);

		ArrayList<WineMakerInventory> returnData = this.dbOps.queryInventoryDataByBatch(batchId);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryInventory()"), debugLogging);
		return returnData;
	} // end of queryInventoryBatch()
	
	/**
	 * Query to return a single capital asset inventory record
	 * @param itemName String with the asset item name
	 * @param itemID String with the asset ID value
	 * @return ArrayList<WineMakerInventory> Set of returned inventory records
	 */
	public ArrayList<WineMakerInventory> queryInventory(String itemName, String itemID)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryInventory(%s)", itemName), debugLogging);

		ArrayList<WineMakerInventory> returnItem = this.dbOps.queryInventoryData(itemName, itemID);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryInventory(%s)", itemName), debugLogging);
		return returnItem;
	} // end of queryInventory()

	/**
	 * Call method to return a single batch record
	 * @param batchKey String containing batch key
	 * @param blendSearch Constant indicating whether this is a varietal or blend batfch
	 * @return ArrayList<WineMakerInventory> Set of returned inventory records
	 */
	public ArrayList<WineMakerLog> queryBatch(String batchKey, SQLSearch blendSearch) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryBatch(%s, %s)", batchKey, blendSearch.toString()), debugLogging);

		ArrayList<WineMakerLog> returnItem = this.dbOps.queryBatch(batchKey, blendSearch);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryBatch(%s, %s)", batchKey, blendSearch.toString()), debugLogging);
		return returnItem; 
	} // end of queryBatch()

	/**
	 * Query DB for Ferment table entries.   Specify an empty activity code to retrieve all
	 * ferment records for this batch.
	 * 
	 * @param batchKey String with the key of the parent batch.
	 * @return An ArrayList of WineMakerFerment objects
	 */
	public ArrayList<WineMakerFerment> queryFermentData(String batchKey) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryFermentData(%s)", batchKey), debugActive);

		ArrayList<WineMakerFerment> wmfSet;
		
		if (batchKey.length() == 0)
			wmfSet = this.dbOps.queryFermentData();
		else
			wmfSet = this.dbOps.queryFermentData(batchKey, "");
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryFermentData(%s)", batchKey), debugActive);
		return wmfSet;
	} // end of queryFermentData()

	/**
	 * Query DB for Ferment table entries, filtered to a specific activity code
	 * 
	 * @param batchKey String with the key of the parent batch
	 * @param activity String with the specific activity code
	 * @return An ArrayList of WineMakerFerment objects
	 */
	public ArrayList<WineMakerFerment> queryFermentData(String batchKey, String activity) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryFermentData(%s, %s)", batchKey, activity), debugActive);

		ArrayList<WineMakerFerment> wmfSet = this.dbOps.queryFermentData(batchKey, activity);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryFermentData(%s, %s)", batchKey, activity), debugActive);
		return wmfSet;
	} // end of queryFermentData()

	/**
	 * Query DB for Ferment table entries, filtered to a specific activity code
	 * 
	 * @param batchKey String with the key of the parent batch
	 * @param activity String with the specific activity code
	 * @return An ArrayList of WineMakerFerment objects
	 */
	public ArrayList<WineMakerFerment> queryFermentData(String batchKey, String activity, Timestamp refTime) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryFermentData(%s, %s, %s)", batchKey, activity, refTime.toString()), debugActive);

		ArrayList<WineMakerFerment> wmfSet = this.dbOps.queryFermentData(batchKey, activity, refTime);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryFermentData(%s, %s, %s)", batchKey, activity, refTime.toString()), debugActive);
		return wmfSet;
	} // end of queryFermentData()

	/**
	 * Query DB for Test table entries
	 * 
	 * @param batchKey String with the key of the parent batch
	 * @return An ArrayList of WineMakerTesting objects
	 */
	public ArrayList<WineMakerTesting> queryTestingData(String batchKey) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.queryTestingData(%s)", batchKey), debugLogging);

		ArrayList<WineMakerTesting> returnItems = this.dbOps.queryTestingData(batchKey);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.queryTestingData(%s)", batchKey), debugLogging);
		return returnItems; 
	} // end of queryTestingData()

	
	/**
	 * Update current batch record
	 * 
	 * @param batchKey String value of the batch key
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean updateFermentData(WineMakerFerment wmf) 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.updateFermentData(%s)", wmf.get_batchKey()), debugLogging);

		boolean returnState = this.dbOps.updateFermentData(wmf);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.updateFermentData(%s)", wmf.get_batchKey()), debugLogging);
		return returnState;
	} // end of updateFermentData()

	/**
	 * Update resource code record
	 * @param codeType String containing resource code category, like 'grape'
	 * @param codeValue String containing resource code record key, like 'carm'
	 * @param codeDesc String containing resource code display value, like 'carmenere'
	 * @return boolean True/False flag indicating success or failure
	 */
	public boolean updateCode(String codeType, String codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.updateCode(%s, %s, %s)", codeType, codeValue, codeDesc), debugLogging);

		boolean returnState = this.dbOps.updateCode(codeType, codeValue, codeDesc);
		
		winemakerLogger.writeLog(String.format("<< WineMakerModel.updateCode(%s, %s, %s)", codeType, codeValue, codeDesc), debugLogging);
		return returnState;
	} // end of updateCode()

	/**
	 * Drop all existing tables and recreate them
	 * 
	 * @return True or False reflecting the SQL operation result
	 */
	public boolean resetTables() {
		for (String entry : databaseTableList) 
		{
			if (validateTable(entry).length() == 0) {
				dropTable(entry);
			}
			
			createTable(entry);
		}

		return true;
	} // end of resetTables()
	
	/**
	 * Validate and create a connection to the codes file.
	 * Call method returnFileContents() to read and return contents.
	 * 
	 * @return String array of lines from the input file
	 */
	public String[] readCodesFile() 
	{
		winemakerLogger.writeLog(String.format(">> WineMakerModel.readCodesFile()"), debugActive);

		if (getCodeFilePath() != null && getCodeFilePath().length() > 0) 
		{
			winemakerLogger.writeLog(String.format("   WineMakerModel.readCodesFile(): File path set at runtime = '%s'", getCodeFilePath()), debugActive);
			inputFile = new File(getCodeFilePath());
		}
		else 
		{
			winemakerLogger.writeLog(String.format("   WineMakerModel.readCodesFile(): prompt user for file location"), debugActive);
			try {
				FileChooser fc = new FileChooser();
				fc.setTitle("Code Table Input");
				fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
				inputFile = fc.showOpenDialog(fxStage);

			} catch (Exception e1) 
			{
				winemakerLogger.showAlarm("Failure connecting to designated codes file", AlertType.ERROR);
				winemakerLogger.showIOException(e1, "Failure connecting to designated codes file");
			}
		}

		if (inputFile == null) {
			return null;
		}

		winemakerLogger.writeLog(String.format("<< WineMakerModel.readCodesFile()"), debugActive);
		return HelperFunctions.returnFileContents(inputFile);
	} // end of readCodesFile()
}
