package geo.apps.winemaker.utilities;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

import geo.apps.winemaker.WineMakerLog;
import geo.apps.winemaker.WineMakerModel;
import geo.apps.winemaker.WineMakerTesting;
import geo.apps.winemaker.WineMakerFerment;
import geo.apps.winemaker.WineMakerInventory;
import geo.apps.winemaker.utilities.Constants.*;
import javafx.scene.control.Alert.AlertType;

public class DatabaseOperations {
	
	// Database Table names
	
	/*
	 *
	private final String winemkrKeyTable = "WMK_KEY";	
	private final String winemkrCodesTable = "WMK_CODES";
	private final String winemkrTestingTable = "WMK_TESTING";
	private final String winemkrFermentTable = "WMK_FERMENT";
	private final String winemkrInventoryTable = "WMK_INVENTORY";
	 */
	
	// SQL Query templates
	private final String QUERY_ALL = "SELECT * FROM $table";
	private final String QUERY_ALL_1 = "SELECT * FROM $table FETCH FIRST ROW ONLY";
	private final String QUERY_BY_KEY = "SELECT * FROM $table WHERE batch LIKE '$key%'";
	private final String QUERY_BLEND_BY_KEY = "SELECT * FROM $table WHERE batch_blend_key LIKE '$key%'";
	private final String QUERY_FERMENT_BY_ACTIVITY = "SELECT * FROM $table WHERE "
			+ "batch LIKE '$key%' AND "
			+ "ferm_act LIKE '$activity'";
	private final String QUERY_FERMENT_BY_ENTRYDATE = "SELECT * FROM $table WHERE "
			+ "batch LIKE '$key%' AND "
			+ "ferm_act LIKE '$activity' AND "
			+ "entry_date BETWEEN timestamp('$start-date') AND timestamp('$end-date')";
	@SuppressWarnings("unused")
	private final String QUERY_INVENTORY_PARENT = "SELECT * FROM $table WHERE "
			+ "inv_item LIKE '$key%' AND "
			+ "(inv_activity IS NULL OR inv_activity = '')";
	@SuppressWarnings("unused")
	private final String QUERY_INVENTORY_PARENT_WITHSTOCK = "SELECT * FROM $table WHERE "
			+ "inv_item LIKE '$key%' AND "
			+ "inv_stock_on_hand > 0 AND "
			+ "(inv_activity IS NULL OR inv_activity = '')";
	@SuppressWarnings("unused")
	private final String QUERY_INVENTORY_PARENT_BYID = "SELECT * FROM $table WHERE "
			+ "inv_item LIKE '$key%' AND "
			+ "inv_item_id LIKE '$id' AND "
			+ "(inv_activity IS NULL OR inv_activity = '')";
	private final String QUERY_INVENTORY_BYID = "SELECT * FROM $table WHERE "
			+ "inv_item LIKE '$key%' AND "
			+ "inv_item_id LIKE '$id'";
	private final String QUERY_INVENTORY_BYBATCH = "SELECT * FROM $table WHERE "
			+ "inv_batch_id LIKE '$batch'";
	
	// SQL Delete templates
	private final String DELETE_BY_KEY = "DELETE FROM $table WHERE batch = ?";
	private final String DELETE_BLEND_BY_KEY = "DELETE FROM $table WHERE batch_blend_key = ?";
	private final String DELETE_BY_DATE = "DELETE FROM $table WHERE entry_date = ?";
	private final String DELETE_BY_CODE = "DELETE FROM $table WHERE code_type = ? AND code_value = ?";
	
	// SQL Insert templates
	private final String INSERT_NEW_BATCH = "INSERT INTO " + DatabaseTables.PRIMARY.getValue() 
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final String INSERT_NEW_FERMENT = "INSERT INTO " + DatabaseTables.ACTIVITY.getValue() 
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final String INSERT_NEW_TESTING = "INSERT INTO " + DatabaseTables.TESTS.getValue() 
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private final String INSERT_NEW_CODE = "INSERT INTO " + DatabaseTables.CODES.getValue() 
		+ " VALUES (?, ?, ?)";
	private final String INSERT_NEW_INVENTORY = "INSERT INTO " + DatabaseTables.INVENTORY.getValue() 
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	// SQL Update templates
	private final String UPDATE_INVENTORY = "UPDATE " + DatabaseTables.INVENTORY.getValue() +
			" SET inv_stock_on_hand=? " +
			" WHERE inv_item=? AND inv_activity = ''";
	private final String UPDATE_INVENTORY_BATCH = "UPDATE " + DatabaseTables.INVENTORY.getValue() +
			" SET inv_batch_id=?, " +
			" inv_stock_on_hand=? " +
			" WHERE inv_item=? AND inv_item_id=? AND inv_activity = ''";
	private final String UPDATE_CODE = "UPDATE " + DatabaseTables.CODES.getValue() +
			" SET code_desc=? " +
			" WHERE code_type=? AND code_value=?";	
	private final String UPDATE_BATCH = "UPDATE " + DatabaseTables.PRIMARY.getValue() + 
			" SET batch_blend_key=?," +
			" source_count=?," +
			" source_price=?," +
			" source_measure=?," +
			" source_scale=?," +
			" quality_rating=?," +
			" waste_percent=?," +
			" source_vendor=?," +
			" vendor_notes=? " +
			" WHERE batch=?";
	private final String UPDATE_FERMENT_DATA = "UPDATE " + DatabaseTables.ACTIVITY.getValue() + 
			" SET ferm_grape_amt = ?," +
			" ferm_must_vol = ?," +
			" ferm_yeast_strain = ?," +
			" ferm_additive_name = ?," +
			" ferm_additive_amt = ?," +
			" ferm_additive_scale = ?," +
			" ferm_starter_yeast_amt = ?," +
			" ferm_starter_h2o_amt = ?," +
			" ferm_starter_juice_amt = ?," +
			" ferm_brix = ?," +
			" ferm_pH = ?," +
			" ferm_TA = ?," +
			" ferm_cold_location = ?," +
			" ferm_stage_start_date = ?," +
			" ferm_stage_end_date = ?," +
			" ferm_puncher = ?," +
			" ferm_container_type = ?," +
			" ferm_container_type2 = ?," +
			" ferm_container_type3 = ?," +
			" ferm_container_count = ?," +
			" ferm_container2_count = ?," +
			" ferm_container3_count = ?," +
			" ferm_container_vol = ?," +
			" ferm_container2_vol = ?," +
			" ferm_container3_vol = ?," +
			" ferm_rack_source = ?," +
			" ferm_rack_target1 = ?," +
			" ferm_rack_target2 = ?," +
			" ferm_rack_target3 = ?," +
			" ferm_rack_target1_count = ?," +
			" ferm_rack_target2_count = ?," +
			" ferm_rack_target3_count = ?," +
			" ferm_bottle_count = ?," +
			" ferm_current_temp = ?," +
			" ferm_start_temp = ?," +
			" ferm_end_temp = ?," +
			" ferm_press_cycle = ?," +
			" ferm_stage_cycle = ?," +
			" ferm_stage_duration = ?," +
			" ferm_yeast_activity = ?," +
			" ferm_temp_scale = ?," +
			" ferm_input_juice_vol = ?," +
			" ferm_output_juice_vol = ?," +
			" ferm_stage_curr_juice_vol = ?," +
			" ferm_input_juice_scale = ?," +
			" ferm_output_juice_scale = ?," +
			" ferm_stage_curr_juice_scale = ?," +
			" ferm_ferment_notes = ?" +
			" WHERE entry_date = timestamp('$key')";
	private final String UPDATE_TESTING_DATA = "";

	// Database Table definitions
	private final String WINEMKR_KEY_TABLE_DEF = "CREATE TABLE " 
			+ DatabaseTables.PRIMARY.getValue() 
			+ " (batch VARCHAR(14) NOT NULL, "
			+ "batch_blend_key VARCHAR(14), "
			+ "batch_blend_seq INT, "
			+ "batch_source VARCHAR(8), "
			+ "batch_grape VARCHAR(8), "
			+ "batch_vineyard VARCHAR(8), "
			+ "source_count INT, "
			+ "source_price DOUBLE, "
			+ "source_measure INT, "
			+ "source_scale VARCHAR(8), "
			+ "quality_rating INT, "
			+ "waste_percent INT, "
			+ "source_vendor VARCHAR(8), "
			+ "vendor_notes VARCHAR(500), "
			+ "bottle_count INT, "
			+ "blend_ratio INT, "
			+ "PRIMARY KEY(batch))";
	private final String WINEMKR_INVENTORY_TABLE_DEF = "CREATE TABLE "
			+ DatabaseTables.INVENTORY.getValue()
			+ " ("
			+ "inv_item VARCHAR(16) NOT NULL, " 
			+ "inv_item_id VARCHAR(16) NOT NULL, "
			+ "entry_date TIMESTAMP NOT NULL, "
			+ "inv_stock_on_hand DOUBLE, "
			+ "inv_batch_id VARCHAR(14), "
			+ "inv_activity VARCHAR(8), "
			+ "inv_activity_amt DOUBLE, "
			+ "inv_purchase_cost DOUBLE, "
			+ "inv_item_scale VARCHAR(8), "			
			+ "inv_purchase_vendor VARCHAR(50), "
			+ "PRIMARY KEY(entry_date)"
			+ ")";
	private final String WINEMKR_TESTING_TABLE_DEF = "CREATE TABLE " 
			+ DatabaseTables.TESTS.getValue() 
			+ " (batch VARCHAR(14) NOT NULL, "
			+ "entry_date TIMESTAMP NOT NULL, "
			+ "test_type VARCHAR(8), "
			+ "test_value DOUBLE, "
			+ "test_scale VARCHAR(8), "
			+ "test_temp INT, "
			+ "temp_scale VARCHAR(8), "
			+ "test_notes VARCHAR(500), "
			+ "PRIMARY KEY(entry_date))";
	private final String WINEMKR_FERMENT_TABLE_DEF = "CREATE TABLE " 
			+ DatabaseTables.ACTIVITY.getValue() 
			+ " (batch VARCHAR(14) NOT NULL, "
			+ "entry_date TIMESTAMP NOT NULL, "
			+ "ferm_act VARCHAR(8), "
			+ "ferm_grape_amt INT, "
			+ "ferm_must_vol INT, "
			+ "ferm_yeast_strain VARCHAR(8), "
			+ "ferm_additive_name VARCHAR(8), "
			+ "ferm_additive_amt DOUBLE, "
			+ "ferm_additive_scale VARCHAR(8), "
			+ "ferm_starter_yeast_amt DOUBLE, "
			+ "ferm_starter_h2o_amt INT, "
			+ "ferm_starter_juice_amt INT, "
			+ "ferm_brix DOUBLE, "
			+ "ferm_pH DOUBLE, "
			+ "ferm_TA DOUBLE, "
			+ "ferm_cold_location VARCHAR(8), "			
			+ "ferm_stage_start_date TIMESTAMP, "
			+ "ferm_stage_end_date TIMESTAMP, "
			+ "ferm_puncher VARCHAR(8), "
			+ "ferm_container_type VARCHAR(8), "
			+ "ferm_container_type2 VARCHAR(8), "
			+ "ferm_container_type3 VARCHAR(8), "
			+ "ferm_container_count INT, "
			+ "ferm_container2_count INT, "
			+ "ferm_container3_count INT, "
			+ "ferm_container_vol INT, "
			+ "ferm_container2_vol INT, "
			+ "ferm_container3_vol INT, "
			+ "ferm_rack_source VARCHAR(8), "
			+ "ferm_rack_target1 VARCHAR(8), "
			+ "ferm_rack_target2 VARCHAR(8), "
			+ "ferm_rack_target3 VARCHAR(8), "
			+ "ferm_rack_target1_count INT, "
			+ "ferm_rack_target2_count INT, "
			+ "ferm_rack_target3_count INT, "
			+ "ferm_bottle_count INT, "
			+ "ferm_current_temp INT, "
			+ "ferm_start_temp INT, "
			+ "ferm_end_temp INT, "
			+ "ferm_press_cycle INT, "
			+ "ferm_stage_cycle INT, "
			+ "ferm_stage_duration INT, "
			+ "ferm_yeast_activity INT, "
			+ "ferm_temp_scale VARCHAR(8), "
			+ "ferm_input_juice_vol INT, "
			+ "ferm_output_juice_vol INT, "
			+ "ferm_stage_curr_juice_vol INT, "
			+ "ferm_input_juice_scale VARCHAR(8), "
			+ "ferm_output_juice_scale VARCHAR(8), "
			+ "ferm_stage_curr_juice_scale VARCHAR(8), "
			+ "ferm_ferment_notes VARCHAR(500), "
			+ "PRIMARY KEY(entry_date))";
	private final String WINEMKR_CODES_TABLE_DEF = "CREATE TABLE " 
			+ DatabaseTables.CODES.getValue() 
			+ " (code_type VARCHAR(16), "
			+ "code_value VARCHAR(16), "
			+ "code_desc VARCHAR(50))";

	private final String WINEMKR_TESTING_TABLE_ALTER = "ALTER TABLE " 
			+ DatabaseTables.TESTS.getValue() 
			+ " ADD FOREIGN KEY (batch) REFERENCES " 
			+ DatabaseTables.PRIMARY.getValue() 
			+ "(batch) ON UPDATE NO ACTION ON DELETE CASCADE";
	private final String WINEMKR_FERMENT_TABLE_ALTER = "ALTER TABLE " 
			+ DatabaseTables.ACTIVITY.getValue() 
			+ " ADD FOREIGN KEY (batch) REFERENCES " 
			+ DatabaseTables.PRIMARY.getValue() 
			+ "(batch) ON UPDATE NO ACTION ON DELETE CASCADE";
	private final String WINEMKR_TABLE_DROP = "DROP TABLE $table";

	private HashMap<String, String> databaseTableValidationSet = new HashMap<>();
	private HashMap<String, String> databaseTableSetDefines = new HashMap<>();
	private HashMap<String, String> databaseTableSetInserts = new HashMap<>();
	private HashMap<String, String> databaseTableSetUpdates = new HashMap<>();
	private HashMap<String, String> databaseTableSetAlters = new HashMap<>();

	private WineMakerLogging winemakerLogger = null;
	private WineMakerModel winemakerModel = null;
	private Connection conn = null;
		
	private Properties prop = new Properties();
	private File dbHome;
	private final String DB_USER = "db2.user_fc4";
	private final String DB_PASSWORD = "db2.password_fc4";
	private final String DB_URL = "db2.url_fc4";
	
	boolean debugLogging = true;
	boolean errorLogging = false;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Initialize HashMaps for table SQL operations
	 * Each HashMap connects a specific table's action (create, delete, update) to the SQL string
	 */
	public DatabaseOperations()
	{
		super();
		
		this.winemakerLogger = (WineMakerLogging) HelperFunctions.getRegistry().get(RegistryKeys.LOGGER);
		this.winemakerModel = (WineMakerModel) HelperFunctions.getRegistry().get(RegistryKeys.MODEL);
		
		databaseTableSetDefines.put(DatabaseTables.PRIMARY.getValue(), WINEMKR_KEY_TABLE_DEF);
		databaseTableSetDefines.put(DatabaseTables.ACTIVITY.getValue(), WINEMKR_FERMENT_TABLE_DEF);
		databaseTableSetDefines.put(DatabaseTables.TESTS.getValue(), WINEMKR_TESTING_TABLE_DEF);
		databaseTableSetDefines.put(DatabaseTables.CODES.getValue(), WINEMKR_CODES_TABLE_DEF);
		databaseTableSetDefines.put(DatabaseTables.INVENTORY.getValue(), WINEMKR_INVENTORY_TABLE_DEF);
		
		databaseTableSetInserts.put(DatabaseTables.PRIMARY.getValue(), INSERT_NEW_BATCH);
		databaseTableSetInserts.put(DatabaseTables.ACTIVITY.getValue(), INSERT_NEW_FERMENT);
		databaseTableSetInserts.put(DatabaseTables.TESTS.getValue(), INSERT_NEW_TESTING);
		databaseTableSetInserts.put(DatabaseTables.CODES.getValue(), INSERT_NEW_CODE);
		databaseTableSetInserts.put(DatabaseTables.INVENTORY.getValue(), INSERT_NEW_INVENTORY);

		databaseTableSetUpdates.put(DatabaseTables.PRIMARY.getValue(), UPDATE_BATCH);
		databaseTableSetUpdates.put(DatabaseTables.ACTIVITY.getValue(), UPDATE_FERMENT_DATA);
		databaseTableSetUpdates.put(DatabaseTables.TESTS.getValue(), UPDATE_TESTING_DATA);

		databaseTableSetAlters.put(DatabaseTables.ACTIVITY.getValue(), WINEMKR_FERMENT_TABLE_ALTER);
		databaseTableSetAlters.put(DatabaseTables.TESTS.getValue(), WINEMKR_TESTING_TABLE_ALTER);

		databaseTableValidationSet.put(DatabaseTables.PRIMARY.getValue(), "Connection failed to the primary batch table");
		databaseTableValidationSet.put(DatabaseTables.ACTIVITY.getValue(), "Connection failed to the fermentation data table");
		databaseTableValidationSet.put(DatabaseTables.TESTS.getValue(), "Connection failed to the testing data table");
		databaseTableValidationSet.put(DatabaseTables.CODES.getValue(), "Connection failed to the codes table");
		databaseTableValidationSet.put(DatabaseTables.INVENTORY.getValue(), "Connection failed to the inventory table");

		prop = this.winemakerModel.getAppProperties();
	}	
	
	public File getDbHome() {
		return dbHome;
	}

	public Properties getProperties()
	{
		return prop;
	}
	
	public HashMap<String, String> getValidationSet()
	{
		return databaseTableValidationSet;
	}
	
	public HashMap<String, String> getTableValidationSet()
	{
		return databaseTableValidationSet;
	}
	
	private void setConn()
	{
		this.conn = connectDatabase();
	}
	
	private Connection getConn()
	{
		return this.conn;
	}
	
	/**
	 * Property-driven method to create configured connection type
	 * @return A new SQL Connection object
	 */
	public Connection connectDatabase()
	{
		String databaseType = prop.getProperty("DBTYPE");

		if (databaseType.equals("DB2"))
		{
			return connectDatabaseSSL();
		}
		
		if (databaseType.equals("Derby"))
		{
			return connectDatabaseDerby("");
		}
		
		return null;
	}
	
	/*
	 * Create connection to the database.  The optional input argument is used when moving
	 * or restoring the database files
	 */
	private Connection connectDatabaseDerby(String connURL)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.connectDatabaseDerby('%s')", connURL), debugLogging);

		String errorMsgStart = "   DatabaseOperations.connectDatabaseDerby(): exception";
		Connection newConn = null;
		connURL = (connURL.length() == 0) ? prop.getProperty("DBPATH") : connURL;

		winemakerLogger.writeLog(String.format("   DatabaseOperations.connectDatabaseDerby() URL = '%s'", connURL), debugLogging);
		
		try 
		{
			Class.forName("org.apache.derby.jdbc.ClientDriver").getDeclaredConstructor().newInstance();
			newConn = DriverManager.getConnection(connURL);
		} 
		catch (SQLNonTransientConnectionException se)
		{} 
		catch (InstantiationException e) {
			winemakerLogger.writeLog(String.format("%s 2 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (IllegalAccessException e) {
			winemakerLogger.writeLog(String.format("%s 3 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (ClassNotFoundException e) {
			winemakerLogger.writeLog(String.format("%s 4 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (IllegalArgumentException e) {
			winemakerLogger.writeLog(String.format("%s 5 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (InvocationTargetException e) {
			winemakerLogger.writeLog(String.format("%s 6 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (NoSuchMethodException e) {
			winemakerLogger.writeLog(String.format("%s 7 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		} catch (SecurityException e) {
			winemakerLogger.writeLog(String.format("%s 8 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Connecting to Derby Database");
		}
		catch (SQLException e) 
		{
			winemakerLogger.writeLog(String.format("%s 1 %s", errorMsgStart, e.getMessage()), debugLogging);
			winemakerLogger.showSqlException(e, "Connecting to Derby Database");
			if (e.getErrorCode() == 40000 && e.getSQLState().equals("XJ040"))
			{
				winemakerLogger.displayAlert("Connection error to database, it's possible another application is already connected");
			}
		} 
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.connectDatabaseDerby()"), debugLogging);
		return newConn;
	}
	
	/*
	 * SSL Connect to cloud-based DB2 database instance.
	 */
	private Connection connectDatabaseSSL()
	{		
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.connectDatabaseSSL()"), debugLogging);

		Connection newConn = null;
		
		try
		{
			newConn = DriverManager.getConnection(prop.getProperty(DB_URL), prop.getProperty(DB_USER), prop.getProperty(DB_PASSWORD));
			newConn.setAutoCommit(false);
		}
		catch (SQLException sqle)
		{
			winemakerLogger.writeLog(String.format("   DatabaseOperations.connectDatabaseSSL(): exception %s", sqle.getMessage()), debugLogging);
			
			if (sqle.getErrorCode() == -4214)
			{
				//winemakerLogger.displayAlert("Authorization failure to database");
			}
			else
			{
				//winemakerLogger.displayAlert("Connection failure to database");
			}
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.connectDatabaseSSL()"), debugLogging);
		return newConn;
	} // end of connectDatabaseSSL()
		
	/**
	 * Perform backup of current database files and directories
	 */
	public String backUpDatabase()
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.backUpDatabase()"), debugLogging);
		
		String returnMsg = "Backup complete to ";
		Connection conn = connectDatabase();
		
		java.text.SimpleDateFormat todaysDate = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String backupdirectory = prop.getProperty("DBBACKUP") + "/" + todaysDate.format((java.util.Calendar.getInstance()).getTime());
		winemakerLogger.writeLog(String.format("   DatabaseOperations.backUpDatabase(): backup dir = %s", backupdirectory), debugLogging);

		try 
		{
			CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
			cs.setString(1, backupdirectory);
			winemakerLogger.writeLog(String.format("   DatabaseOperations.backUpDatabase(): starting"), debugLogging);
			cs.execute(); 
			winemakerLogger.writeLog(String.format("   DatabaseOperations.backUpDatabase(): complete"), debugLogging);
			cs.close();	
			
			returnMsg += backupdirectory;
		}
		catch (SQLException e) 
		{
			winemakerLogger.showAlarm("Database backup failed, probably because the target directory was invalid", AlertType.ERROR);
			winemakerLogger.writeLog(String.format("   DatabaseOperations.backUpDatabase(): backup exception %s", e.getMessage()), debugLogging);
			winemakerLogger.showIOException(e, "Backing up Derby Database");
			
			returnMsg = "Database backup failed, check the target directory.";
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.backUpDatabase()"), debugLogging);
		return returnMsg;
	}

	/**
	 * Restore the database from a selected backup
	 * @param restoreLocation full path to the location of the selected backup files
	 * 
	 * https://derby-user.db.apache.narkive.com/caYnYzAq/unable-to-restore-a-derby-database
	 */
	public void restoreDatabase(String restoreLocation)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.restoreDatabase('%s')", restoreLocation), debugLogging);
	
		String restoreURL = prop.getProperty("DBRESTORE") + restoreLocation + "/winemaker";
	
		Connection newConn = connectDatabaseDerby(prop.getProperty("DBSHUTDOWN"));
		closeResources(newConn, null, null);
		
		newConn = connectDatabaseDerby(restoreURL);
		closeResources(newConn, null, null);
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.restoreDatabase('%s'):", restoreLocation), debugLogging);
		return;
	}

	/*
	 * Sample: jdbc:derby:c:/Users/geo/Box/WinemakerTest/winemaker;createFrom=c:/Users/geo/Documents/Apps/WineMakerApp/Backup/2023-05-03_09-23/winemaker
	 * 
	 * Will need to also move the Backup folder (if exists) and rebuild the properties file
	 */
	
	/**
	 * Close and reconnect database to set it in a new location
	 * @param toLocation String containing target location for the database files
	 */
	public void moveDatabase(String toLocation)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.moveDatabase('%s')", toLocation), debugLogging);
		
		String moveURL = WineMakerModel.getJdbcbase() + toLocation.replace("\\", "/") + "/winemaker;createFrom=" + prop.getProperty("DBAPPDIR") + "/winemaker";

		Connection newConn = connectDatabaseDerby(prop.getProperty("DBSHUTDOWN"));
		closeResources(newConn, null, null);
		
		newConn = connectDatabaseDerby(moveURL);
		closeResources(newConn, null, null);
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.moveDatabase('%s')", toLocation), debugLogging);
		
		return;
	}

	/**
	 * Move directory files to a new location.  Primarily used by the Move Database option to move backup files
	 * @param sourceDirectoryLocation String containing the source directory
	 * @param destinationDirectoryLocation String containing the target directory
	 */
	public void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.copyDirectory('%s', '%s')", sourceDirectoryLocation, destinationDirectoryLocation), debugLogging);

		try {
			Files.walk(Paths.get(sourceDirectoryLocation))
			.forEach(source -> {
				Path destination = Paths.get(destinationDirectoryLocation, source.toString()
						.substring(sourceDirectoryLocation.length()));
				try 
				{
					Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
				} 
				catch (FileAlreadyExistsException ea)
				{}
				catch (IOException e) 
				{
					winemakerLogger.showIOException(e, "1. Copy backup directory");
				}
			});
		} 
		catch (IOException e) 
		{
			winemakerLogger.showIOException(e, "2. Copy backup directory");
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.copyDirectory('%s', '%s')", sourceDirectoryLocation, destinationDirectoryLocation), debugLogging);
		return;
	}
	
	/**
	 * Create new table in the database, using the supplied table name
	 * @param tableName String containing table name
	 * @return String Text result of the operation
	 */
	public String createTable(String tableName)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.createTable('%s')", tableName), debugLogging);
		
		setConn();
		Connection conn = getConn();
		ArrayList<Statement> statements = new ArrayList<Statement>();		
		String returnState = "";
				
		try
		{
			if (conn != null)
			{
				Statement stmt = conn.createStatement();
				statements.add(stmt);

				winemakerLogger.writeLog(String.format("   DatabaseOperations.createTable(%s) create SQL '%s'%n", tableName, databaseTableSetDefines.get(tableName)), debugLogging);
				stmt.execute(databaseTableSetDefines.get(tableName));
				
				if (databaseTableSetAlters.get(tableName) != null)
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.createTable(%s) alter SQL '%s'%n", tableName, databaseTableSetAlters.get(tableName)), debugLogging);
					stmt.execute(databaseTableSetAlters.get(tableName));
				}

				conn.commit();
			}
			else
			{
				returnState = String.format("Database connection failed for %s, check logs for error code %n", tableName);
			}			
		}
		catch (SQLException sqle)
		{
			returnState += winemakerLogger.showSqlException(sqle, "Failure to create table");
		}
		finally
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.createTable('%s')", tableName), debugLogging);
		return returnState;
	}

	/**
	 * Drop the current table
	 * @param tableName String containing table name
	 * @return String Text result of the operation
	 */
	public String dropTable(String tableName)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.dropTable('%s')", tableName), debugLogging);

		setConn();
		Connection conn = getConn();
		ArrayList<Statement> statements = new ArrayList<Statement>();		
		String returnState = "";
		
		try
		{
			if (conn != null)
			{
				Statement stmt = conn.createStatement();
				statements.add(stmt);

				stmt.execute(WINEMKR_TABLE_DROP.replace("$table", tableName));
				
				conn.commit();
			}
			else
			{
				returnState = String.format("Database connection failed for %s, check logs for error code %n", tableName);
			}			
		}
		catch (SQLException sqle)
		{
			returnState += winemakerLogger.showSqlException(sqle, "Failure to drop table");
	 	}
		finally
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.dropTable('%s')", tableName), debugLogging);
		return returnState;
	} // end of dropTable(String tableName)
		
	/*
	 * Validation method to check existence of a table.  Takes table name as input.
	 * Returns the table name if validation failed.
	 */
	private String doesTableExist(String tableName)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.doesTableExist('%s')", tableName), debugLogging);

		String returnTable = "";
		
		try 
		{
			ResultSet rSet = this.getConn()
				.getMetaData()
				.getTables(null, "APP", tableName, null);
			
			if (!rSet.next())
				returnTable = tableName;
		} 
		catch (SQLException e) 
		{
			winemakerLogger.showSqlException(e, tableName);
			returnTable = tableName;
		}			
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.doesTableExist('%s')", tableName), debugLogging);
		return returnTable;
	}
	
	/**
	 * Call database table validation for each required table.
	 * The validation consists of testing the connection to the table. There are three reason for failure:
	 * 		1. The database instance is missing
	 * 		2. The table is missing in the database
	 * 		3. The table might exist but the SQL query statement failed
	 * 
	 * For reasons 1 and 2, attempt to create the table(s).  Take special action to create the
	 * main batch table first, as the FERMENT and TEST tables are dependent tables.
	 * 
	 * @return ArrayList<String> Collected list of result messages for each validated table
	 */
	public ArrayList<String> validateAllTables()
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.validateAllTables()"), debugLogging);

		ArrayList<String> validateStates = new ArrayList<String>();		
		ArrayList<String> validateExisting = new ArrayList<String>();		
		this.setConn();
		
		if (this.getConn() != null)
		{	
			/*
			 * Find missing tables
			 */
			validateExisting = databaseTableValidationSet.keySet()
				.stream()
				.map(table -> doesTableExist(table))
				.collect(Collectors.toCollection(ArrayList::new));
			validateExisting.removeIf(a -> (a.length() == 0));
			
			/*
			 * Create main batch table if its one of the missing
			 */
			validateExisting
				.stream()
				.filter(tableName -> tableName.equals(DatabaseTables.PRIMARY.getValue()))			
				.forEach(tableName -> createTable(tableName));
			validateExisting.removeIf(tableName -> tableName.equals(DatabaseTables.PRIMARY.getValue()));

			/*
			 * Create any remaining missing tables
			 */
			validateExisting
				.stream()
				.forEach(tableName -> createTable(tableName));
			
			this.setConn();
			
			/*
			 * Re-validate tables
			 */
			validateStates = databaseTableValidationSet.keySet()
					.stream()
					.map(table -> validateTable(table))
					.collect(Collectors.toCollection(ArrayList::new));
			
			/*
			 * If the resources codes table is empty, populate it now with the default set of codes.
			 */
			if (queryCodes().size() == 0)
			{
				winemakerLogger.writeLog(String.format("   DatabaseOperations.validateAllTables(): codes table is empty, ready to populate"), debugLogging);

				String installPath = System.getProperty("user.dir") + File.separator + "Resources" + File.separator;

				File codesFile = new File(installPath + "WineMakerApp_ResourceCodes.csv");
				winemakerLogger.writeLog(String.format("   DatabaseOperations.validateAllTables(): First, try File path from user.dir URL '%s' (%b)", codesFile.getPath(), codesFile.exists()), debugLogging);

				if (!codesFile.exists())
				{
					Path classPathResource = null;
					
					try
					{
						URL classPathURL = getClass().getClassLoader().getResource("WineMakerApp_ResourceCodes.csv");
						classPathResource = Paths.get(classPathURL.toURI());
						codesFile = classPathResource.toFile();
						
						winemakerLogger.writeLog(String.format("   DatabaseOperations.validateAllTables(): alternate properties of Path resource: '%s' & '%s'", classPathResource.getFileName(), classPathResource.toString()), debugLogging);
					}
					catch (URISyntaxException e)
					{
						winemakerLogger.showIOException(e, "Failed to find WineMakerApp_ResourceCodes.csv");						
					}
					
					winemakerLogger.writeLog(String.format("   DatabaseOperations.validateAllTables(): alternate File from Path resource: '%s' (%b)", codesFile.getPath(), codesFile.exists()), debugLogging);
				}

				insertCodes(HelperFunctions.returnFileContents(codesFile));
			}
		}
		else 
		{
			validateStates.add("Failure to connect to database\n");
			
			if (prop.getProperty("DBTYPE").equals("Derby"))
				validateStates.add("Make sure the Network Server is started\n");				
			else
				validateStates.add("Verify the internet connection\n");
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.validateAllTables()"), debugLogging);
		return validateStates;
	}

	/**
	 * Validate connection to requested table.  Validation checks both the existence of the table
	 * and the ability to make a simple query.
	 * 
	 * @param String name of table to be validated 
	 * @return String database connection error, if any, empty string if successful
	 */
	public String validateTable(String tableName)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.validateTable('%s')", tableName), debugLogging);
		
		String returnState = "";
		
		ArrayList<Statement> statements = new ArrayList<Statement>();

		this.setConn();
		Connection conn = this.getConn();
		
		if (conn == null)
		{
			this.setConn();
			conn = getConn();
		}
		
		try 
		{
			if (conn.isClosed())
				setConn();
		} 
		catch (SQLException e) 
		{
			winemakerLogger.showSqlException(e, "Failed to get database connection");
			winemakerLogger.writeLog(String.format("<< DatabaseOperations.validateTable('%s')", tableName), debugLogging);
			return "Connection failure";
		}
		
		ResultSet resultSet = null;
		
		try
		{
			String sqlText = QUERY_ALL_1.replace("$table", tableName);
			winemakerLogger.writeLog(String.format("   DatabaseOperations.validateTable('%s') query '%s' ", tableName, sqlText), debugLogging);

			Statement s = conn.createStatement();
			statements.add(s);
			resultSet = s.executeQuery(sqlText);

			conn.commit();
		}
		catch (SQLException sqle)
		{
			returnState = (sqle.getSQLState().equals("42X05")) ? "Table does not exist" : sqle.getMessage();  
			winemakerLogger.showSqlException(sqle, String.format("Failure to validate table %s%n", tableName));				
		}
		finally
		{
			closeResources(null, statements, resultSet);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.validateTable('%s'): returning '%s'", tableName, returnState), debugLogging);
		return returnState;
	} // end of validateTable(String tableName)

	/**
	 * Insert new batch record into the database
	 * @param wmk New WineMakerLog object
	 * @return boolean value reflecting success or failure of SQL operation 
	 */
	public boolean insertBatch(WineMakerLog wmk)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertBatch('%s'): %s", wmk.get_batchKey(), wmk), debugLogging);

		ArrayList<Statement> statements = new ArrayList<Statement>();

		setConn();
		Connection conn = getConn();
		boolean returnCode = false;

		try {
			if (conn != null) 
			{
				PreparedStatement psInsert = conn.prepareStatement(INSERT_NEW_BATCH);
				statements.add(psInsert);

				psInsert.setString(1, wmk.get_batchKey());
				psInsert.setString(2, wmk.get_batchBlendKey());
				psInsert.setInt(3, wmk.get_batchBlendSequence());
				psInsert.setString(4, wmk.get_batchSource());
				psInsert.setString(5, wmk.get_batchGrape());
				psInsert.setString(6, wmk.get_batchVineyard());
				psInsert.setInt(7, wmk.get_sourceItemCount());
				psInsert.setDouble(8, wmk.get_sourceItemPrice());
				psInsert.setInt(9, wmk.get_sourceItemMeasure());
				psInsert.setString(10, wmk.get_sourceScale());
				psInsert.setInt(11, wmk.get_qualityRating());
				psInsert.setInt(12, wmk.get_wastePercent());
				psInsert.setString(13, wmk.get_sourceVendor());
				psInsert.setString(14, wmk.get_sourceVendorNotes());
				psInsert.setInt(15, wmk.get_bottleCount());
				psInsert.setInt(16, wmk.get_blendRatio());

				psInsert.executeUpdate();

				conn.commit();

				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			if (sqle.getSQLState().equals("23505"))
				winemakerLogger.showAlarm(String.format("The date and time of entry %s duplicates an existing entry, adjust the time and resubmit", HelperFunctions.batchKeyExpand(wmk)), AlertType.ERROR);
			
			winemakerLogger.showSqlException(sqle, "Failure in batch insert");
		}
		catch (Exception e)
		{
			winemakerLogger.showIOException(e, "Failure in batch insert");
		}
		finally 
		{
			closeResources(conn, statements, null);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertBatch('%s')", wmk.get_batchKey()), debugLogging);
		return returnCode;
	} // end of insertBatch(wineMakerLog wmk)
	
	/**
	 * Insert new ferment record into the database
	 * @param wmk New WineMakerFerment object
	 * @return boolean value reflecting success or failure of SQL operation 
	 */
	public boolean insertFermentData(WineMakerFerment wmf) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertFermentData('%s'): %s", wmf.get_batchKey(), wmf), debugLogging);
	
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try
		{
			if (conn != null)
			{
				PreparedStatement psInsert = conn.prepareStatement(INSERT_NEW_FERMENT);
				statements.add(psInsert);
	
				psInsert.setString(1, wmf.get_batchKey());
				psInsert.setTimestamp(2, wmf.get_entry_date());
				psInsert.setString(3, wmf.get_fermentActivity());
				psInsert.setInt(4, wmf.get_inputGrapeAmt());
				psInsert.setInt(5, wmf.get_outputMustVolume());
				psInsert.setString(6, wmf.get_yeastStrain());
				psInsert.setString(7, wmf.get_chemAdded());
				psInsert.setDouble(8, wmf.get_chemAmount());
				psInsert.setString(9, wmf.get_chemScale());
				psInsert.setDouble(10, wmf.get_starterYeastAmt());
				psInsert.setInt(11, wmf.get_starterH2OAmt());
				psInsert.setInt(12, wmf.get_starterJuiceAmt());
				psInsert.setDouble(13, wmf.get_currBrix());
				psInsert.setDouble(14, wmf.get_currpH());
				psInsert.setDouble(15, wmf.get_currTA());
				psInsert.setString(16, wmf.get_coldLocation());
				psInsert.setTimestamp(17, wmf.get_startDate());
				psInsert.setTimestamp(18, wmf.get_endDate());
				psInsert.setString(19, wmf.get_punchTool());
				psInsert.setString(20, wmf.get_containerType());
				psInsert.setString(21, wmf.get_containerType2());
				psInsert.setString(22, wmf.get_containerType3());
				psInsert.setInt(23, wmf.get_containerCount());
				psInsert.setInt(24, wmf.get_container2Count());
				psInsert.setInt(25, wmf.get_container3Count());				
				psInsert.setInt(26, wmf.get_containerVol());
				psInsert.setInt(27, wmf.get_container2Vol());
				psInsert.setInt(28, wmf.get_container3Vol());
				psInsert.setString(29, wmf.get_rackSource());
				psInsert.setString(30, wmf.get_rackTarget1());
				psInsert.setString(31, wmf.get_rackTarget2());
				psInsert.setString(32, wmf.get_rackTarget3());
				psInsert.setInt(33, wmf.get_rackTarget1Count());
				psInsert.setInt(34, wmf.get_rackTarget2Count());
				psInsert.setInt(35, wmf.get_rackTarget3Count());
				psInsert.setInt(36, wmf.get_bottleCount());
				psInsert.setInt(37, wmf.get_currentTemp());
				psInsert.setInt(38, wmf.get_startTemp());
				psInsert.setInt(39, wmf.get_endingTemp());
				psInsert.setInt(40, wmf.get_pressCycle());
				psInsert.setInt(41, wmf.get_stageCycle());
				psInsert.setInt(42, wmf.get_stageDuration());
				psInsert.setInt(43, wmf.get_yeastActiveLevel());
				psInsert.setString(44, wmf.get_tempScale());
				psInsert.setInt(45, wmf.get_inputJuiceVol());
				psInsert.setInt(46, wmf.get_outputJuiceVol());
				psInsert.setInt(47, wmf.get_currentStageJuiceVol());
				psInsert.setString(48, wmf.get_inputJuiceScale());
				psInsert.setString(49, wmf.get_outputJuiceScale());
				psInsert.setString(50, wmf.get_currentStageJuiceScale());
				psInsert.setString(51, wmf.get_fermentNotes());
	
				psInsert.executeUpdate();
	
				conn.commit();
	
				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in ferment data insert");
			if (sqle.getSQLState().equals("23505"))
				winemakerLogger.showAlarm("The date and time of this activity duplicates an existing entry, adjust the time and resubmit", AlertType.ERROR);
		}
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertFermentData('%s')", wmf.get_batchKey()), debugLogging);
		return returnCode;
	} // end of insertFermentData(WineMakerFerment wmf)

	/**
	 * Insert new inventory record into the database
	 * @param wmk New WineMakerInventory object
	 * @return boolean value reflecting success or failure of SQL operation 
	 */
	public boolean insertNewInventoryItem(WineMakerInventory wmi)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertNewInventoryItem('%s'): %s", wmi.get_itemName(), wmi), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try {
			if (conn != null) 
			{
				PreparedStatement psInsert = conn.prepareStatement(INSERT_NEW_INVENTORY);
				statements.add(psInsert);

				psInsert.setString(1, wmi.get_itemName());
				psInsert.setString(2, wmi.getItemId());
				psInsert.setTimestamp(3, wmi.getItemTaskTime());
				psInsert.setDouble(4, wmi.get_itemStockOnHand());
				psInsert.setString(5, wmi.getItemBatchId());
				psInsert.setString(6, wmi.getItemTaskId());
				psInsert.setDouble(7, wmi.get_itemActivityAmount());
				psInsert.setDouble(8, wmi.get_itemPurchaseCost());
				psInsert.setString(9, wmi.get_itemAmountScale());
				psInsert.setString(10, wmi.get_itemPurchaseVendor());
				
				psInsert.executeUpdate();
				
				conn.commit();

				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			if (sqle.getSQLState().equals("23505"))
			{
				winemakerLogger.writeLog(String.format("   DatabaseOperations.insertNewInventoryItem(): duplicate key %s", wmi.getItemTaskTime().toLocalDateTime().format(dateFormatter)), debugLogging);
			}
			winemakerLogger.showSqlException(sqle, "Failure in inventory data insert");
		}
		finally 
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertNewInventoryItem()"), debugLogging);
		return returnCode;
	} // end of insertNewInventoryItem(WineMakerInventory inv)
	
	/**
	 * Insert new test record into the database
	 * @param wmk New WineMakerTesting object
	 * @return boolean value reflecting success or failure of SQL operation 
	 */
	public boolean insertNewTestData(WineMakerTesting wmt)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertNewTestData('%s'): %s", wmt.get_batchKey(), wmt), debugLogging);

		ArrayList<Statement> statements = new ArrayList<Statement>();

		setConn();
		Connection conn = getConn();
		boolean returnCode = false;

		try {
			if (conn != null) 
			{
				PreparedStatement psInsert = conn.prepareStatement(INSERT_NEW_TESTING);
				statements.add(psInsert);

				psInsert.setString(1, wmt.get_batchKey());
				psInsert.setTimestamp(2, wmt.get_entry_date());
				psInsert.setString(3, wmt.get_testType());
				psInsert.setDouble(4, wmt.get_testValue());
				psInsert.setString(5, wmt.get_testScale());
				psInsert.setDouble(6, wmt.get_testTemp());
				psInsert.setString(7, wmt.get_tempScale());
				psInsert.setString(8, wmt.get_testNotes());
				
				psInsert.executeUpdate();
				
				conn.commit();

				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in test data insert");
		}
		finally 
		{
			closeResources(conn, statements, null);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertNewTestData('%s')", wmt.get_batchKey()), debugLogging);
		return returnCode;
		
	} // end of insertNewTestData(WineMakerTesting wmt)
	
	/**
	 * Insert new resource record into the database
	 * @param codeType String code category, like 'grape' or 'fermentadd'
	 * @param codeValue String internal resource code key, like 'carm' or 'potmeta'
	 * @param codeDesc String displayed text of the resource, like 'Carmenere' or 'Potassium Metabisulfate'
	 * @return value reflecting success or failure of SQL operation
	 */
	public boolean insertNewCode(String codeType, String codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertNewCode(codeType '%s', codeValue '%s', codeDesc '%s')", codeType, codeValue, codeDesc), debugLogging);

		ArrayList<Statement> statements = new ArrayList<Statement>();

		setConn();
		Connection conn = getConn();
		boolean returnCode = false;

		try {
			if (conn != null) 
			{
				PreparedStatement psInsert = conn.prepareStatement(INSERT_NEW_CODE);
				statements.add(psInsert);

				psInsert.setString(1, codeType);
				psInsert.setString(2, codeValue);
				psInsert.setString(3, codeDesc);
				psInsert.executeUpdate();

				conn.commit();

				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in code insert");
		}
		finally 
		{
			closeResources(conn, statements, null);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertNewCode(codeType '%s', codeValue '%s', codeDesc '%s')", codeType, codeValue, codeDesc), debugLogging);
		return returnCode;
	} // end of insertNewCode(String codeType, String codeValue, String codeDesc)

	/**
	 * Bulk insertion of new resource codes
	 * @param codesData String array of new field codes and descriptions, like 'grape,carm,Carmenere'
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean insertCodes(String[] codesData)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertCodes()"), debugLogging);
	
		ArrayList<Statement> statements = new ArrayList<Statement>();
		String exMsg = "";
		String[] lineTokens = null;
	
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try
		{
			if (conn != null)
			{
				String sqlText = INSERT_NEW_CODE;
				PreparedStatement psInsert = conn.prepareStatement(sqlText);
				statements.add(psInsert);
	
				for (String dataRecord : codesData) {
					lineTokens = dataRecord.split(",");
					psInsert.setString(1, lineTokens[0]);
					psInsert.setString(2, lineTokens[1]);
					psInsert.setString(3, lineTokens[2]);
					psInsert.addBatch();
				}
	
				psInsert.executeBatch();
	
				conn.commit();
				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			SQLException sqleNext = sqle.getNextException();

			exMsg = winemakerLogger.showSqlException(sqle, "Failure in codes insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertCodes(): Insert failure: '%s'", exMsg), debugLogging);
			exMsg = winemakerLogger.showSqlException(sqleNext, "2nd Failure in codes insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertCodes(): Insert failure: '%s'", exMsg), debugLogging);
			
			winemakerLogger.displayAlert(String.format("Loading failure: %s", sqleNext.getMessage().split(":")[0]));
		}
		catch (ArrayIndexOutOfBoundsException ae)
		{
			exMsg = winemakerLogger.showIOException(ae, "Failure in codes insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertCodes(): Insert failure: '%s'", exMsg), debugLogging);

			winemakerLogger.displayAlert(String.format("An error exists in the input file: '%s'", exMsg));		
		}
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertCodes()"), debugLogging);
		return returnCode;
	} // end of insertCodes(String[] codesData)

	/**
	 * Bulk insertion of new inventory assets
	 * @param assetsData String array of new assets, like 'Brute 44 gal Bucket,BRT44GAL02,2023-08-03 15:49:45,...'
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean insertAssets(String[] assetsData)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.insertAssets()"), debugLogging);
	
		ArrayList<Statement> statements = new ArrayList<Statement>();
		String[] lineTokens = null;
		String vendorText = "";
		String scaleText = "";
		String exMsg = "";
		double purchaseCost = 0.0;
		
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try
		{
			if (conn != null)
			{
				String sqlText = INSERT_NEW_INVENTORY;

				PreparedStatement psInsert = conn.prepareStatement(sqlText);
				statements.add(psInsert);
	
				for (String dataRecord : assetsData) {
					lineTokens = dataRecord.split(",");
					if (lineTokens[0].equals("Item Name"))
						continue;
					if (lineTokens.length == 0)
						continue;
					
					vendorText = (lineTokens.length > 9) ? lineTokens[9] : "";
					scaleText = (lineTokens.length > 8) ? lineTokens[8] : "";
					purchaseCost = (lineTokens.length > 7) ? Double.parseDouble(lineTokens[7]) : 0.0;
					
					psInsert.setString(1, lineTokens[0]);
					psInsert.setString(2, lineTokens[1]);
					psInsert.setTimestamp(3, Timestamp.valueOf(lineTokens[2]));
					psInsert.setDouble(4, Double.parseDouble(lineTokens[3]));
					psInsert.setString(5, lineTokens[4]);
					psInsert.setString(6, lineTokens[5]);
					psInsert.setDouble(7, Double.parseDouble(lineTokens[6]));
					psInsert.setDouble(8, purchaseCost);
					psInsert.setString(9, scaleText);
					psInsert.setString(10, vendorText);
										
					psInsert.addBatch();
				}			        
				psInsert.executeBatch();
	
				conn.commit();
				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			SQLException sqleNext = sqle.getNextException();

			exMsg = winemakerLogger.showSqlException(sqle, "Failure in assets insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertAssets(): Insert failure: '%s'", exMsg), debugLogging);
			exMsg = winemakerLogger.showSqlException(sqleNext, "2nd Failure in assets insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertAssets(): Insert failure: '%s'", exMsg), debugLogging);
			
			winemakerLogger.displayAlert(String.format("Loading failure: %s", sqleNext.getMessage().split(":")[0]));
		}
		catch (ArrayIndexOutOfBoundsException ae)
		{
			exMsg = winemakerLogger.showIOException(ae, "Failure in assets insert");
			winemakerLogger.writeLog(String.format("   DatabaseOperations.insertAssets(): Insert failure: '%s'", exMsg), debugLogging);
			winemakerLogger.displayAlert(String.format("An error exists in the input file: '%s'", exMsg));
		}
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.insertAssets()"), debugLogging);
		return returnCode;
	} // end of insertAssets(String[] codesData)
	
	/**
	 * Common method for deleting records from tables with java.sql.Timestamp keys, currently the Ferment and Testing tables
	 * @param entryDate Timestamp key date
	 * @param dbTable String table name
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean deleteDateRecord(Timestamp entryDate, String dbTable)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.deleteDateRecord('%s', '%s')", entryDate, dbTable), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();
		
		PreparedStatement psDelete = null;
		String statementText = DELETE_BY_DATE;
		boolean returnCode = false;
		
		try {
			if (conn != null) 
			{
				statementText = statementText.replace("$table", dbTable);
				winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteDateRecord(): SQL = %s", statementText), debugLogging);

				psDelete = conn.prepareStatement(statementText);
				statements.add(psDelete);
				psDelete.setTimestamp(1, entryDate);
				int count = psDelete.executeUpdate();
				
				conn.commit();
				if (count > 0)
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteDateRecord(): deleted '%s'", entryDate), debugLogging);
					returnCode = true;					
				}
				else
					winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteDateRecord(): entry '%s' not deleted", entryDate), debugLogging);
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in entry delete");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.deleteDateRecord('%s', '%s')", entryDate, dbTable), debugLogging);
		return returnCode;
	}
	
	/**
	 * Delete a batch, which will include all associated Ferment and Testing records
	 * @param batchKey String key of the batch
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean deleteBatch(String batchKey)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.deleteBatch(batchKey '%s')", batchKey), debugLogging);
	
		String[] tableList = {DatabaseTables.PRIMARY.getValue()};
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();

		PreparedStatement psDelete = null;
		String statementText = DELETE_BY_KEY;
		int totalDeletes = 0;
		int tableDeletes = 0;
		boolean returnCode = false;
	
		try {
			if (conn != null) 
			{
				/*
				 * Delete batch records in all related tables
				 */
				for (String table : tableList) {
					statementText = statementText.replace("$table", table);
	
					psDelete = conn.prepareStatement(statementText);
					statements.add(psDelete);
					psDelete.setString(1, batchKey);
					tableDeletes = psDelete.executeUpdate();
					
					winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteBatch(): %d batches deleted from %s", tableDeletes, table), debugLogging);
					totalDeletes += tableDeletes;
				}
	
				/*
				 * Now attempt to delete any blend records
				 */
				statementText = DELETE_BLEND_BY_KEY;
				statementText = statementText.replace("$table", DatabaseTables.PRIMARY.getValue());
	
				psDelete = conn.prepareStatement(statementText);
				statements.add(psDelete);
				psDelete.setString(1, batchKey);
				tableDeletes = psDelete.executeUpdate();
				
				winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteBatch(): %d blend batches deleted from %s", tableDeletes, DatabaseTables.PRIMARY.getValue()), debugLogging);
				totalDeletes += tableDeletes;
	

				conn.commit();
	
				winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteBatch(): %d total records cleaned", totalDeletes), debugLogging);

				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in batch delete");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.deleteBatch(batchKey '%s')", batchKey), debugLogging);
		return returnCode;
	} // end of deleteBatch(String batchKey)

	/**
	 * Delete a resource code
	 * @param codeType String the code category, like 'grape'
	 * @param codeValue String the code value, like 'carm'
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean deleteCode(String codeType, String codeValue)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.deleteCode(codeType '%s', codeValue '%s')", codeType, codeValue), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		setConn();
		Connection conn = getConn();

		PreparedStatement psDelete = null;
		String statementText = DELETE_BY_CODE;
		boolean returnCode = false;

		try {
			if (conn != null) 
			{
				statementText = statementText.replace("$table", DatabaseTables.CODES.getValue());

				psDelete = conn.prepareStatement(statementText);
				statements.add(psDelete);
				psDelete.setString(1, codeType);
				psDelete.setString(2, codeValue);

				if (psDelete.executeUpdate() > 0)
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteCode(): deleted %s=%s", codeType, codeValue), debugLogging);					
					returnCode = true;
				}
				else
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.deleteCode(): failed to delete %s=%s", codeType, codeValue), debugLogging);
					returnCode = false;
				}
				
				conn.commit();
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in code delete");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.deleteCode(codeType '%s', codeValue '%s')", codeType, codeValue), debugLogging);
		return returnCode;
	} // end of deleteCode(String codeType, String codeValue)	

	/**
	 * Update parent Batch record
	 * @param wmk Updated WineMakerLog object
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean updateBatch(WineMakerLog wmk)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.updateBatch('%s') %s", wmk.get_batchKey(), wmk), debugLogging);
	
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try 
		{
			if (conn != null)
			{
				PreparedStatement psInsert = conn.prepareStatement(UPDATE_BATCH);
				statements.add(psInsert);
	
				psInsert.setString(1, wmk.get_batchBlendKey());
				psInsert.setInt(2, wmk.get_sourceItemCount());
				psInsert.setDouble(3, wmk.get_sourceItemPrice());
				psInsert.setInt(4, wmk.get_sourceItemMeasure());
				psInsert.setString(5, wmk.get_sourceScale());
				psInsert.setInt(6, wmk.get_qualityRating());
				psInsert.setInt(7, wmk.get_wastePercent());
				psInsert.setString(8, wmk.get_sourceVendor());
				psInsert.setString(9, wmk.get_sourceVendorNotes());
				psInsert.setString(10, wmk.get_batchKey());
	
				psInsert.executeUpdate();
	
				conn.commit();
	
				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in batch query");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.updateBatch(WineMakerLog '%s') %n%s", wmk.get_batchKey(), wmk), debugLogging);
		return returnCode;
	} // end of updateBatch()

	/**
	 * Update parent Ferment Data record
	 * @param wmf Updated WineMakerFerment object
	 * @return boolean Flag indicating success or failure of SQL operation
	 */
	public boolean updateFermentData(WineMakerFerment wmf)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.updateFermentData('%s'): %s", wmf.get_batchKey(), wmf), debugLogging);
	
		ArrayList<Statement> statements = new ArrayList<Statement>();
		String sqlText = UPDATE_FERMENT_DATA.replace("$key", wmf.get_entry_date().toString());
		
		setConn();
		Connection conn = getConn();
		boolean returnCode = false;
		
		try 
		{
			if (conn != null)
			{
				PreparedStatement psUpdate = conn.prepareStatement(sqlText);
				statements.add(psUpdate);
	
				psUpdate.setInt(1, wmf.get_inputGrapeAmt());
				psUpdate.setInt(2, wmf.get_outputMustVolume());
				psUpdate.setString(3, wmf.get_yeastStrain());
				psUpdate.setString(4, wmf.get_chemAdded());
				psUpdate.setDouble(5, wmf.get_chemAmount());
				psUpdate.setString(6, wmf.get_chemScale());
				psUpdate.setDouble(7, wmf.get_starterYeastAmt());
				psUpdate.setInt(8, wmf.get_starterH2OAmt());
				psUpdate.setInt(9, wmf.get_starterJuiceAmt());
				psUpdate.setDouble(10, wmf.get_currBrix());
				psUpdate.setDouble(11, wmf.get_currpH());
				psUpdate.setDouble(12, wmf.get_currTA());
				psUpdate.setString(13, wmf.get_coldLocation());
				psUpdate.setTimestamp(14, wmf.get_startDate());
				psUpdate.setTimestamp(15, wmf.get_endDate());
				psUpdate.setString(16, wmf.get_punchTool());
				psUpdate.setString(17, wmf.get_containerType());
				psUpdate.setString(18, wmf.get_containerType2());
				psUpdate.setString(19, wmf.get_containerType3());
				psUpdate.setInt(20, wmf.get_containerCount());
				psUpdate.setInt(21, wmf.get_container2Count());
				psUpdate.setInt(22, wmf.get_container3Count());
				psUpdate.setInt(23, wmf.get_containerVol());
				psUpdate.setInt(24, wmf.get_container2Vol());
				psUpdate.setInt(25, wmf.get_container3Vol());
				psUpdate.setString(26, wmf.get_rackSource());
				psUpdate.setString(27, wmf.get_rackTarget1());
				psUpdate.setString(28, wmf.get_rackTarget2());
				psUpdate.setString(29, wmf.get_rackTarget3());
				psUpdate.setInt(30, wmf.get_rackTarget1Count());
				psUpdate.setInt(31, wmf.get_rackTarget2Count());
				psUpdate.setInt(32, wmf.get_rackTarget3Count());
				psUpdate.setInt(33, wmf.get_bottleCount());
				psUpdate.setInt(34, wmf.get_currentTemp());
				psUpdate.setInt(35, wmf.get_startTemp());
				psUpdate.setInt(36, wmf.get_endingTemp());
				psUpdate.setInt(37, wmf.get_pressCycle());
				psUpdate.setInt(38, wmf.get_stageCycle());
				psUpdate.setInt(39, wmf.get_stageDuration());
				psUpdate.setInt(40, wmf.get_yeastActiveLevel());
				psUpdate.setString(41, wmf.get_tempScale());
				psUpdate.setInt(42, wmf.get_inputJuiceVol());
				psUpdate.setInt(43, wmf.get_outputJuiceVol());
				psUpdate.setInt(44, wmf.get_currentStageJuiceVol());
				psUpdate.setString(45, wmf.get_inputJuiceScale());
				psUpdate.setString(46, wmf.get_outputJuiceScale());
				psUpdate.setString(47, wmf.get_currentStageJuiceScale());
				psUpdate.setString(48, wmf.get_fermentNotes());
	
				if (psUpdate.executeUpdate() > 0)
					returnCode = true;
				else
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.updateFermentData(): no matching records for '%s'", UPDATE_FERMENT_DATA), debugLogging);
					returnCode = false;
				}
				
				conn.commit();	
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in Ferment data update");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.updateFermentData(WineMakerFerment '%s')", wmf.get_batchKey()), debugLogging);
		return returnCode;
	} // end of updateFermentData(WineMakerFerment wmf)

	/**
	 * Update existing resource code record
	 * @param codeType String code category, like 'grape' or 'fermentadd'
	 * @param codeValue String internal resource code key, like 'carm' or 'potmeta'
	 * @param codeDesc String displayed text of the resource, like 'Carmenere' or 'Potassium Metabisulfate'
	 * @return value reflecting success or failure of SQL operation
	 */
	public boolean updateCode(String codeType, String codeValue, String codeDesc)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.updateCode(codeType '%s', codeValue '%s', codeDesc '%s')", codeType, codeValue, codeDesc), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();

		boolean returnCode = false;
		
		try 
		{
			if (conn != null)
			{
				PreparedStatement psUpdate = conn.prepareStatement(UPDATE_CODE);
				statements.add(psUpdate);
	
				psUpdate.setString(1, codeDesc);
				psUpdate.setString(2, codeType);
				psUpdate.setString(3, codeValue);
	
				psUpdate.executeUpdate();
	
				conn.commit();
	
				returnCode = true;
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in code update");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.updateCode(codeType '%s', codeValue '%s', codeDesc '%s')", codeType, codeValue, codeDesc), debugLogging);
		return returnCode;
	} // end of updateCode(String codeType, String codeValue, String codeDesc)

	/**
	 * Update existing non-id inventory record
	 * @param wmi Updated WineMakerInventory object
	 * @return value reflecting success or failure of SQL operation
	 */
	public boolean updateInventory(WineMakerInventory wmi)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.updateInventory(%s)", wmi), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();

		boolean returnCode = false;
		
		try 
		{
			if (conn != null)
			{
				PreparedStatement psUpdate = conn.prepareStatement(UPDATE_INVENTORY);
				statements.add(psUpdate);
	
				psUpdate.setDouble(1, wmi.get_itemStockOnHand());
				psUpdate.setString(2, wmi.get_itemName());
				
				if (psUpdate.executeUpdate() > 0)
					returnCode = true;
				else
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.updateInventory(): no matching records for '%s'", UPDATE_INVENTORY), debugLogging);
					returnCode = false;
				}
	
				conn.commit();
			}
		}
		catch (SQLException sqle) 
		{
			if (sqle.getSQLState().equals("23505"))
			{
				winemakerLogger.writeLog(String.format("   DatabaseOperations.insertNewInventoryItem(): duplicate key %s", wmi.getItemTaskTime().toLocalDateTime().format(dateFormatter)), debugLogging);
			}
			winemakerLogger.showSqlException(sqle, "Failure in inventory update");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.updateInventory()"), debugLogging);
		return returnCode;
	} // end of updateInventory(String codeType, String codeValue, String codeDesc)

	/**
	 * Update existing inventory record with id
	 * @param wmi Updated WineMakerInventory object
	 * @return value reflecting success or failure of SQL operation
	 */
	public boolean updateInventoryBatch(WineMakerInventory wmi)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.updateInventoryBatch(%s)", wmi), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
	
		setConn();
		Connection conn = getConn();

		boolean returnCode = false;
		
		try 
		{
			if (conn != null)
			{
				PreparedStatement psUpdate = conn.prepareStatement(UPDATE_INVENTORY_BATCH);
				statements.add(psUpdate);
	
				psUpdate.setString(1, wmi.getItemBatchId());
				psUpdate.setDouble(2, wmi.get_itemStockOnHand());
				psUpdate.setString(3, wmi.get_itemName());
				psUpdate.setString(4, wmi.getItemId());

				winemakerLogger.writeLog(String.format("   DatabaseOperations.updateInventory(): update values: '%s' '%1.4f' '%s' '%s'", wmi.getItemBatchId(), wmi.get_itemStockOnHand(), wmi.get_itemName(), wmi.getItemId()), debugLogging);

				if (psUpdate.executeUpdate() > 0)
					returnCode = true;
				else
				{
					winemakerLogger.writeLog(String.format("   DatabaseOperations.updateInventory(): no matching records for '%s'", UPDATE_INVENTORY_BATCH), debugLogging);
					returnCode = false;
				}

				conn.commit();
			}
		}
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Failure in inventory update");
		} 
		finally 
		{
			closeResources(conn, statements, null);
		}
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.updateInventoryBatch()"), debugLogging);
		return returnCode;
	}

	
	/**
	 * Returns batch records
	 * @param batchKey Optional key of a specific batch
	 * @param blendSearch Constant used to switch query between varietal or blend batches
	 * @return ArrayList<> of batch records
	 */
 	public ArrayList<WineMakerLog> queryBatch(String batchKey, SQLSearch blendSearch)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryBatch('%s', '%s') ", batchKey, blendSearch), debugLogging);
		
		ArrayList<WineMakerLog> queryRecords = new ArrayList<WineMakerLog>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		setConn();
		Connection conn = getConn();
		ResultSet resultSet = null;
		
		try
		{
			if (conn != null)
			{
				/*
				 * Reference the required SQL query string.   Blend queries
				 * will use a different key field.   
				 */
				String sqlText = (batchKey.length() == 0) ? 
						QUERY_ALL : 
							(blendSearch.equals(SQLSearch.BLENDCOMPONENT)) ? 
									QUERY_BLEND_BY_KEY : QUERY_BY_KEY;		
				sqlText = sqlText.replace("$table", DatabaseTables.PRIMARY.getValue());
				sqlText = sqlText.replace("$key", batchKey);
				
				Statement stmt = conn.createStatement();
				statements.add(stmt);

				resultSet = stmt.executeQuery(sqlText);				

				int rsSize = 0;
				while (resultSet.next())
				{
					rsSize++;
					WineMakerLog wmk = new WineMakerLog(winemakerModel);
					
					wmk.set_batchKey(resultSet.getString(1));
					wmk.set_batchBlendKey(resultSet.getString(2));
					wmk.set_batchBlendSequence(resultSet.getInt(3));
					wmk.set_batchSource(resultSet.getString(4));
					wmk.set_batchGrape(resultSet.getString(5));
					wmk.set_batchVineyard(resultSet.getString(6));
					wmk.set_sourceItemCount(resultSet.getInt(7));
					wmk.set_sourceItemPrice(resultSet.getDouble(8));
					wmk.set_sourceItemMeasure(resultSet.getInt(9));
					wmk.set_sourceScale(resultSet.getString(10));
					wmk.set_qualityRating(resultSet.getInt(11));
					wmk.set_wastePercent(resultSet.getInt(12));
					wmk.set_sourceVendor(resultSet.getString(13));
					wmk.set_sourceVendorNotes(resultSet.getString(14));
					wmk.set_bottleCount(resultSet.getInt(15));
					wmk.set_blendRatio(resultSet.getInt(16));
					queryRecords.add(wmk);
					
					winemakerLogger.writeLog(String.format("   DatabaseOperations.queryBatch(): Record %d: %s %s %s", rsSize, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)), debugLogging);
				}
				winemakerLogger.writeLog(String.format("   DatabaseOperations.queryBatch(): resultset size = %d", rsSize), debugLogging);
				
				conn.commit();
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure in batch query");
		}
		finally
		{
			closeResources(conn, statements, resultSet);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryBatch() "), debugLogging);
		return queryRecords;
	} // end of queryBatch(String batchKey, SQLSearch blendSearch)
	 	
	/**
	 * Query to return all Ferment table records without restriction
	 * @return Array of Ferment table records
	 */
	public ArrayList<WineMakerFerment> queryFermentData() 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryFermentData()"), debugLogging);
		
		ArrayList<WineMakerFerment> queryRecords = queryFermentData(QUERY_ALL);
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryFermentData()"), debugLogging);
		return queryRecords;		
	} // end of queryFermentData()

	/*
	 * Common query method for the Ferment table.  The input query string will have been 
	 * customized by public overloaded method versions
	 */
	private ArrayList<WineMakerFerment> queryFermentData(String sqlString) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryFermentData('%s') ", sqlString), debugLogging);
		
		ArrayList<WineMakerFerment> queryRecords = new ArrayList<WineMakerFerment>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		WineMakerFerment wmf;
		
		setConn();
		Connection conn = getConn();

		ResultSet resultSet = null;
		
		try
		{
			if (conn != null)
			{
				String sqlText = sqlString.replace("$table", DatabaseTables.ACTIVITY.getValue());
				
				Statement stmt = conn.createStatement();
				statements.add(stmt);
	
				resultSet = stmt.executeQuery(sqlText);				
	
				while (resultSet.next()) {
					wmf = new WineMakerFerment(this.winemakerModel);
	
					wmf.set_batchKey(resultSet.getString(1));
					wmf.set_entry_date(resultSet.getTimestamp(2));
					wmf.set_fermentActivity(resultSet.getString(3));
					wmf.set_inputGrapeAmt(resultSet.getInt(4));
					wmf.set_outputMustVolume(resultSet.getInt(5));
					wmf.set_yeastStrain(resultSet.getString(6));
					wmf.set_chemAdded(resultSet.getString(7));
					wmf.set_chemAmount(resultSet.getDouble(8));
					wmf.set_chemScale(resultSet.getString(9));
					wmf.set_starterYeastAmt(resultSet.getDouble(10));
					wmf.set_starterH2OAmt(resultSet.getInt(11));
					wmf.set_starterJuiceAmt(resultSet.getInt(12));
					wmf.set_currBrix(resultSet.getDouble(13));
					wmf.set_currpH(resultSet.getDouble(14));
					wmf.set_currTA(resultSet.getDouble(15));
					wmf.set_coldLocation(resultSet.getString(16));
					wmf.set_startDate(resultSet.getTimestamp(17));
					wmf.set_endDate(resultSet.getTimestamp(18));
					wmf.set_punchTool(resultSet.getString(19));
					wmf.set_containerType(resultSet.getString(20));
					wmf.set_containerType2(resultSet.getString(21));
					wmf.set_containerType3(resultSet.getString(22));
					wmf.set_containerCount(resultSet.getInt(23));
					wmf.set_container2Count(resultSet.getInt(24));
					wmf.set_container3Count(resultSet.getInt(25));
					wmf.set_containerVol(resultSet.getInt(26));
					wmf.set_container2Vol(resultSet.getInt(27));
					wmf.set_container3Vol(resultSet.getInt(28));
					wmf.set_rackSource(resultSet.getString(29));
					wmf.set_rackTarget1(resultSet.getString(30));
					wmf.set_rackTarget2(resultSet.getString(31));
					wmf.set_rackTarget3(resultSet.getString(32));
					wmf.set_rackTarget1Count(resultSet.getInt(33));
					wmf.set_rackTarget2Count(resultSet.getInt(34));
					wmf.set_rackTarget3Count(resultSet.getInt(35));
					wmf.set_bottleCount(resultSet.getInt(36));
					wmf.set_currentTemp(resultSet.getInt(37));
					wmf.set_startTemp(resultSet.getInt(38));
					wmf.set_endingTemp(resultSet.getInt(39));
					wmf.set_pressCycle(resultSet.getInt(40));
					wmf.set_stageCycle(resultSet.getInt(41));
					wmf.set_stageDuration(resultSet.getInt(42));
					wmf.set_yeastActiveLevel(resultSet.getInt(43));
					wmf.set_tempScale(resultSet.getString(44));
					wmf.set_inputJuiceVol(resultSet.getInt(45));
					wmf.set_outputJuiceVol(resultSet.getInt(46));
					wmf.set_currentStageJuiceVol(resultSet.getInt(47));
					wmf.set_inputJuiceScale(resultSet.getString(48));
					wmf.set_outputJuiceScale(resultSet.getString(49));
					wmf.set_currentStageJuiceScale(resultSet.getString(50));
					wmf.set_fermentNotes(resultSet.getString(51));
	
					queryRecords.add(wmf);
	
					winemakerLogger.writeLog(String.format("   DatabaseOperations.queryFermentData(): Record: '%s' '%s' '%s'",
							wmf.get_batchKey(), wmf.get_entry_date(), wmf.get_fermentActivity()), debugLogging);
				}
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure in ferment data query");
		}
		finally
		{
			closeResources(conn, statements, resultSet);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryFermentData()"), debugLogging);
		return queryRecords;
	} // end of queryFermentData(String batchKey)

	/**
	 * Query the Ferment table using a batch key and specific activity code
	 * @param batchKey String containing unique batch key
	 * @param activity String containing activity code
	 * @return Array of Ferment table records
	 */
	public ArrayList<WineMakerFerment> queryFermentData(String batchKey, String activity) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryFermentData('%s', '%s')", batchKey, activity), debugLogging);
		
		String sqlText = QUERY_BY_KEY;
		
		if (activity.length() > 0)
			sqlText = QUERY_FERMENT_BY_ACTIVITY.replace("$activity", activity);
		
		sqlText = sqlText.replace("$key", batchKey);
	
		ArrayList<WineMakerFerment> queryRecords = queryFermentData(sqlText);
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryFermentData()"), debugLogging);
		return queryRecords;
	} // end of queryFermentData(String batchKey, String activity)

	/**
	 * Query Ferment data records within a time range.  This is needed when retrieving amelioration records,
	 * 		where multiple records represent a single activity entry. 
	 * @param batchKey String containing unique batch key
	 * @param activity String containing activity code
	 * @param refTime Timestamp, for when multiple records were generated for a single activity
	 * @return Array of Ferment table records
	 */
	public ArrayList<WineMakerFerment> queryFermentData(String batchKey, String activity, Timestamp refTime) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryFermentData('%s', '%s', '%s')", batchKey, activity, refTime.toString()), debugLogging);

		String sqlText = QUERY_FERMENT_BY_ENTRYDATE.replace("$activity", activity);
		sqlText = sqlText.replace("$key", batchKey);
		sqlText = sqlText.replace("$start-date", refTime.toString());
		sqlText = sqlText.replace("$end-date", Timestamp.valueOf(refTime.toLocalDateTime().plusMinutes(1)).toString());
		
		ArrayList<WineMakerFerment> queryRecords = queryFermentData(sqlText);

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryFermentData('%s', '%s', '%s') returned %d records", batchKey, activity, refTime.toString(), queryRecords.size()), debugLogging);
		return queryRecords;
	} // end of queryFermentData(String batchKey, String activity, Timestamp refTime)

	
	/**
	 * Query to return all Inventory table records without restriction
	 * @return Array of Inventory table records
	 */
	public ArrayList<WineMakerInventory> queryInventoryData() 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryInventoryData()"), debugLogging);
		
		String sqlText = QUERY_ALL;
		sqlText = sqlText.replace("$table", DatabaseTables.INVENTORY.getValue());
	
		ArrayList<WineMakerInventory> queryRecords = queryInventoryData(sqlText);

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryInventoryData()"), debugLogging);
		return queryRecords;		
	} // end of queryInventoryData()

	/**
	 * Query to return all Inventory table records with an ID value
	 * @return Array of Inventory table records
	 */
	public ArrayList<WineMakerInventory> queryInventoryData(String itemName, String itemID) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryInventoryData('%s', '%s')", itemName, itemID), debugLogging);
		
		String sqlText = "";
		if (itemID.length() > 0)
		{
			sqlText = QUERY_INVENTORY_BYID.replace("$key", itemName);
			sqlText = sqlText.replace("$id", itemID);
		}
		else
			sqlText = QUERY_INVENTORY_PARENT.replace("$key", itemName);
			
		sqlText = sqlText.replace("$table", DatabaseTables.INVENTORY.getValue());

		ArrayList<WineMakerInventory> queryRecords = queryInventoryData(sqlText);

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryInventoryData(): %d records returned", queryRecords.size()), debugLogging);
		return queryRecords;		
	} // end of queryInventoryData(itemName, itemID)

	/**
	 * Gets the set of inventory records containing the provided batch id
	 * @param batchId string with the id of the referenced batch
	 * @return ArrayList of query results
	 */
	public ArrayList<WineMakerInventory> queryInventoryDataByBatch(String batchId)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryInventoryDataByBatch('%s')", batchId), debugLogging);
		
		String sqlText = QUERY_INVENTORY_BYBATCH.replace("$batch", batchId);
			
		sqlText = sqlText.replace("$table", DatabaseTables.INVENTORY.getValue());

		ArrayList<WineMakerInventory> queryRecords = queryInventoryData(sqlText);

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryInventoryDataByBatch(): %d records returned", queryRecords.size()), debugLogging);
		return queryRecords;		
	}

	/**
	 * Query to potentially return Inventory records
	 * @param sqlText String formatted SQL query string
	 * @return Array of Inventory table records
	 */
	public ArrayList<WineMakerInventory> queryInventoryData(String sqlText) 
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryInventoryData('%s')", sqlText), debugLogging);
	
		ArrayList<WineMakerInventory> queryRecords = new ArrayList<WineMakerInventory>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		WineMakerInventory inv;
		
		setConn();
		Connection conn = getConn();

		ResultSet resultSet = null;
	
		try
		{
			if (conn != null)
			{
				Statement stmt = conn.createStatement();
				statements.add(stmt);
				resultSet = stmt.executeQuery(sqlText);

				while (resultSet.next()) {
					inv = new WineMakerInventory();

					inv.set_itemName(resultSet.getString(1));
					inv.setItemId(resultSet.getString(2));
					inv.setItemTaskTime(resultSet.getTimestamp(3));
					inv.set_itemStockOnHand(resultSet.getDouble(4));
					inv.setItemBatchId(resultSet.getString(5));
					inv.setItemTaskId(resultSet.getString(6));
					inv.set_itemActivityAmount(resultSet.getDouble(7));
					inv.set_itemPurchaseCost(resultSet.getDouble(8));
					inv.set_itemAmountScale(resultSet.getString(9));
					inv.set_itemPurchaseVendor(resultSet.getString(10));

					queryRecords.add(inv);
				}
			}
			else
			{
				winemakerLogger.writeLog(String.format("   DatabaseOperations.queryInventoryData(): Connection is null"), debugLogging);
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure in inventory data query");
		}
		finally
		{
			closeResources(conn, statements, resultSet);
		}	
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryInventoryData('%s')", sqlText), debugLogging);
		return queryRecords;
	}

	/**
	 * Returns all Testing records for a batch
	 * @param batchKey String containing batch key
	 * @return ArrayList of Testing data
	 */
	public ArrayList<WineMakerTesting> queryTestingData(String batchKey)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryTestingData(batchKey '%s')", batchKey), debugLogging);
	
		
		String sqlText = (batchKey.length() == 0) ? QUERY_ALL : QUERY_BY_KEY.replace("$key", batchKey);
	
		ArrayList<WineMakerTesting> queryRecords = new ArrayList<WineMakerTesting>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		WineMakerTesting wmt;
		
		setConn();
		Connection conn = getConn();

		ResultSet resultSet = null;
	
		try
		{
			if (conn != null)
			{
				sqlText = sqlText.replace("$table", DatabaseTables.TESTS.getValue());
				winemakerLogger.writeLog(String.format("   DatabaseOperations.queryTestingData(): SQL '%s'", sqlText), debugLogging);
				
				Statement stmt = conn.createStatement();
				statements.add(stmt);
				resultSet = stmt.executeQuery(sqlText);

				while (resultSet.next()) {
					wmt = new WineMakerTesting(this.winemakerModel);
	
					wmt.set_batchKey(resultSet.getString(1));
					wmt.set_entry_date(resultSet.getTimestamp(2));
					wmt.set_testType(resultSet.getString(3));
					wmt.set_testValue(resultSet.getDouble(4));
					wmt.set_testScale(resultSet.getString(5));
					wmt.set_testTemp(resultSet.getDouble(6));
					wmt.set_tempScale(resultSet.getString(7));
					wmt.set_testNotes(resultSet.getString(8));
	
					queryRecords.add(wmt);
	
					winemakerLogger.writeLog(String.format("   DatabaseOperations.queryTestingData(): Record: '%s' '%s'",
							resultSet.getString(1), resultSet.getString(2)), debugLogging);
				}
			}
			else
			{
				winemakerLogger.writeLog(String.format("   DatabaseOperations.queryTestingData(): Connection is null"), debugLogging);
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure in testing data query");
		}
		finally
		{
			closeResources(conn, statements, resultSet);
		}
	
		
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryTestingData(batchKey '%s')", batchKey), debugLogging);
		return queryRecords;		
	} // end of queryTestingData(String batchKey)

	/**
	 * Returns all resources codes in a CSV format, like 'grape,carm,Carmenere'
	 * @return ArrayList of resources codes
	 */
	public ArrayList<String> queryCodes()
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.queryCodes()"), debugLogging);
		
		ArrayList<String> queryRecords = new ArrayList<String>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		setConn();
		Connection conn = getConn();

		ResultSet resultSet = null;
		
		try
		{
			if (conn != null)
			{
				String sqlText = QUERY_ALL.replace("$table", DatabaseTables.CODES.getValue());
				Statement stmt = conn.createStatement();
				statements.add(stmt);

				resultSet = stmt.executeQuery(sqlText);
				
				while (resultSet.next())
				{
					queryRecords.add(String.format("%s,%s,%s", resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)));
				}
				conn.commit();
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure in codes query");
		}
		finally
		{
			closeResources(conn, statements, resultSet);
		}

		winemakerLogger.writeLog(String.format("<< DatabaseOperations.queryCodes()"), debugLogging);
		return queryRecords;
	} // end of queryCodes()
	
	/**
	 * Returns column names for a table, used for development and debugging
	 * @param tableName String name of requested table
	 * @return ArrayList of table's column names
	 */
	public ArrayList<String> getColumnNames(String tableName)
	{
		winemakerLogger.writeLog(String.format(">> DatabaseOperations.getColumnNames() for %s: %n", tableName), debugLogging);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		ArrayList<String> returnColumns = new ArrayList<String>();
		returnColumns.add(String.format("Column names for %s table %n", tableName));
		
		setConn();
		Connection conn = getConn();

		ResultSet resultset = null;
		
		String statementText = "SELECT * FROM $table";
		statementText = statementText.replace("$table", tableName);

		try
		{
			if (conn != null)
			{
				// Validate table
				Statement s = conn.createStatement();
				statements.add(s);
				resultset = s.executeQuery(statementText);
				ResultSetMetaData rsmd = resultset.getMetaData();

				winemakerLogger.writeLog(String.format("model.getColumnNames() query returned %d columns %n", rsmd.getColumnCount()), debugLogging);

				for (int x = 0; x < rsmd.getColumnCount(); x++) {
					returnColumns.add(rsmd.getColumnName(x + 1) + " (" + rsmd.getColumnTypeName(x + 1) + ")");
				}

				conn.commit();
			}
		}
		catch (SQLException sqle)
		{
			winemakerLogger.showSqlException(sqle, "Failure to validate table");
		}
		finally
		{
			closeResources(conn, statements, resultset);
		}
	
		winemakerLogger.writeLog(String.format("<< DatabaseOperations.getColumnNames() for %s: %n", tableName), debugLogging);
		return returnColumns;
	} // end of getColumnNames(String tableName)
	
	/*
	 * Close open objects: ResultSet, Statements and Connection
	 */
	private void closeResources(Connection conn, ArrayList<Statement> statements, ResultSet rs)
	{

		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Exception trying to close DB2 ResultSet resources");
		}		

		int i = 0;
		while (statements != null && !statements.isEmpty()) {

			Statement st = (Statement)statements.remove(i);
			try {
				if (st != null) {
					st.close();
					st = null;
				}
			} catch (SQLException sqle) 
			{
				winemakerLogger.showSqlException(sqle, "Exception trying to close DB2 Statement resources");
			}
		}

		try 
		{
			if (conn != null) 
			{
				conn.commit();
				conn.close();
				conn = null;
			}
		} 
		catch (SQLException sqle) 
		{
			winemakerLogger.showSqlException(sqle, "Exception trying to close DB2 Connection resource");
		}
	}
}
