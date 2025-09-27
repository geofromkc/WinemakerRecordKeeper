package geo.apps.winemaker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.derby.drda.NetworkServerControl;

import geo.apps.winemaker.utilities.Constants.RegistryKeys;
import geo.apps.winemaker.utilities.DatabaseOperations;
import geo.apps.winemaker.utilities.HelperFunctions;
import geo.apps.winemaker.utilities.Registry;
import geo.apps.winemaker.utilities.WineMakerLogging;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * The WineMaker application is designed to support the basic steps involved in making wine and mead.
 * It records the various steps, from purchasing the grapes, to crushing and pressing, and to bottling.
 * The various intermediate steps, like racking and testing and amelioration, are all recorded.
 * All the collected data can be exported to a CSV file for subsequent analysis and storage.
 * 
 *  @author geo
 *  @version 1.0
 *  @since 2022-03-01
 */

/*
 * Build Path depends on whether or not the application is modular.
 *  	Module Path: 	JavaFX 21.0.2 (User-Defined  Library)
 *  					JavaFX SDK (Added automatically)
 * 						JRE System Library [jdk-21.0.2]
 *  						
 * 		Class Path:		DB2 Drivers jar db2jcc4.jar
 * 						Derby Drivers: db-derby-10.16.1.1-lib
 * 										derbyclient.jar, 
 * 										derbyshared.jar, 
 * 										derbytools.jar
 * 
 * Update JRE
 * 		Window/Preferences/Java/Installed JREs
 * 		Add "C:\MiscSoftware\jdk-21.0.2" and select it
 * 
 * Install javafx eclipse support:
 * 		e(fx)clipse - http://download.eclipse.org/efxclipse/updates-released/3.7.0/site
 * 
 * Unpack JavaFx SDK and JMods
 * 		C:\MiscSoftware\javafx-jmods-21.0.2
 * 		C:\MiscSoftware\javafx-sdk-21.0.2
 * 
 * Install SceneBuilder
 * 		C:\Users\geo\Box\Install Files\SceneBuilder-19.0.0.msi
 * 
 * 		Then update JavaFX and SceneBuilder paths in Window/Preferences/JavaFX
 * 
 * 		Import JFoenix library to SceneBuilder
 * 		Create User Library from JFoenix jar and add to project ModulePath
 * 
 * jdk.module.path = 
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.base.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.controls.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.fxml.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.graphics.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.media.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.swing.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx.web.jar;
 * 		C:\MiscSoftware\javafx-sdk-24.0.1\lib\javafx-swt.jar;
 * 		C:\MiscSoftware\jfoenix-9.0.10.jar
 * 
 * 	Add JFoenix to run configuration: and buildWinemaker.sh
 * 			... --add-modules javafx.controls,javafx.fxml,com.jfoenix
 * 
 * --module-path "C:\MiscSoftware\javafx-sdk-21.0.2.1\lib" 
 * --add-modules javafx.controls,javafx.fxml,com.jfoenix 
 * --enable-native-access=javafx.graphics 
 * --sun-misc-unsafe-memory-access=allow 
 * --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix 
 * --add-exports javafx.graphics/com.sun.javafx.stage=com.jfoenix 
 * --add-exports javafx.controls/com.sun.javafx.scene.control=com.jfoenix 
 * --add-exports javafx.base/com.sun.javafx.binding=com.jfoenix 
 * --add-exports javafx.base/com.sun.javafx.event=com.jfoenix 
 * --add-opens java.base/java.lang.reflect=com.jfoenix
 * 
 * Edit Project Properties
 * 		Run/Debug
 * 			Edit the project start module
 * 				Add to the "(x) Arguments" tab these VM arguments:
 * 				--module-path "C:\MiscSoftware\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml
 * 
 * Typical C:\Users\barry\AppData\Local\WineMakerApp\WINEMAKER.properties:
 * 		DBTYPE=Derby
 * 		DBAPPDIR=C:/Users/barry/AppData/Roaming/WineMakerApp
 * 		DBSHUTDOWN=jdbc:derby://localhost:1527/;shutdown=true
 * 		DBPATH=jdbc:derby://localhost:1527/C:/Users/barry/AppData/Roaming/WineMakerApp/winemaker;create=true
 * 		DBRESTORE=jdbc:derby://localhost:1527/C:/Users/barry/AppData/Roaming/WineMakerApp/winemaker;restoreFrom=
 * 		DBBACKUP=C:/Users/barry/AppData/Roaming/WineMakerAppBackup
 * 		DBDEFAULTS=1
 * 
 * Export JAR file:
 * 		File/Export/Runnable Jar File
 * 			Launch Config: WineMakerMain - WineMaker
 * 			Export Destination: .../dev/exported-jars/WineMaker-V1.1.0.jar, or C:\SoftwarePackaging\WineMaker\input\WineMaker-V1.1.0.jar
 * 			Library Handling: Extract required libraries into generated JAR

 * 
 * Command line execution:
 * 
 * > java --module-path "C:\MiscSoftware\javafx-sdk-19.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar Winemaker.jar 
 * 				-Lc:/documents/winemakerlogging.txt -Cc:/documents/winemakercodes.txt -D
 */

public class WineMakerMain extends Application {
	
	WineMakerLogging winemakerLogger = null;
	WineMakerModel winemakerModel = null;
	Registry appRegistry = HelperFunctions.getRegistry();
	
	static NetworkServerControl server;
	static Properties prop = new Properties();
	static InputStream ins;
	static File propsFolder;
	static File startupLocation;
	static File startupFile;
	static boolean isInstalledApp = true;
	static boolean isFirstTime = true;
	static boolean performCleanup = true;
	static long installedTime = 0;
	
	public WineMakerMain()
	{
		Runtime.getRuntime().addShutdownHook(new Thread() 
		{
		    @Override
		    public void run() {
				try 
				{
					server.ping();
					server.shutdown();
				} catch (Exception e1) 
				{}
		    }
		});		
	}
    
	@Override
	public void start(Stage primaryStage) 
	{	
		appRegistry.register(RegistryKeys.LOGGER, new WineMakerLogging());
		this.winemakerLogger = (WineMakerLogging) appRegistry.get(RegistryKeys.LOGGER);
		
		appRegistry.register(RegistryKeys.MODEL, new WineMakerModel());
		this.winemakerModel = (WineMakerModel) appRegistry.get(RegistryKeys.MODEL);
	
		appRegistry.register(RegistryKeys.DBOPS, new DatabaseOperations());
		this.winemakerModel.setDbOps((DatabaseOperations) appRegistry.get(RegistryKeys.DBOPS));
		
		winemakerLogger.loadDependencies(winemakerModel);
		winemakerModel.setDebugActive(true);
	
		winemakerLogger.writeLog(String.format(">> WineMakerMain.start()"), true);
		
		
		/*
		 * if this is the first time, prompt the user to accept defaults or 
		 * specify explicit application file directory.
		 * 
		 * First, ask if user wants to review the Installation and Setup Guide.
		 */
		if (setStartupState())
		{
			Alert alertWarning = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);		
			alertWarning.setTitle("Install Guide Download");
			alertWarning.setContentText("Do you want to download and review the Install Guide before installation?");
			Optional<ButtonType> result = alertWarning.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.YES)
			{
				HelperFunctions.showInstallGuide();
				System.exit(0);
			}
			
			setApplicationDir();
		}
	
		if (!winemakerModel.loadProperties())
		{
			winemakerLogger.displayAlert("Could not load the default properties file, the application will now exit");
			System.exit(4);			
		}
		
		/*
		 * Start the Derby Network Server
		 */
		winemakerLogger.writeLog(String.format("   WineMakerMain.start(): start Network Server"), true);
	
		try 
		{
			server = new NetworkServerControl(InetAddress.getByName("localhost"),1527);
			server.start(null);
			winemakerLogger.writeLog(String.format("   WineMakerMain.start(): Network Server started"), true);
		} 
		catch (Exception e1) 
		{
			winemakerLogger.displayAlert("The database server could not be started, the application will now exit");
			winemakerLogger.showIOException(e1, "Failed starting the Derby network server");
			System.exit(4);
		}
	
		/*
		 * Start the UI Stage
		 */
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WineMakerMD.fxml"));
		
		/*
		 * MD Update
		FXMLLoader menuBarLoader = new FXMLLoader(getClass().getResource("DrawerPane.fxml"));
		 */
		
		try 
		{
			winemakerLogger.loadDependencies(winemakerModel);
			winemakerModel.setDebugActive(true);
			
			WineMakerController winemakerController = new WineMakerController();
			loader.setController(winemakerController);
			winemakerModel.setWinemakerController(winemakerController);
			
			/*
			 * MD Update
			MenuBarController menuBarController = new MenuBarController();
			menuBarController.setMainController(winemakerController);
			menuBarLoader.setController(menuBarController);
			 */
			
			Parent batchDetailParent = loader.load();
			Scene winemakerScene = new Scene(batchDetailParent);
			winemakerScene
				.getStylesheets()
				.add(getClass()
				.getResource("modena.css")
				.toExternalForm());
			
			primaryStage.setTitle("Winemaker's Logging Application");
			primaryStage.setScene(winemakerScene);
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			winemakerLogger.showIOException(e, "Failed initial start");
			
			File outputFile;
			File logDir = HelperFunctions.directoryPrompt("WineMaker App Log Export Directory Selection", "");

			if (logDir != null)
			{
				String dayStamp = String.format("%s", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				String timeStamp = String.format("%s", LocalTime.now().format(DateTimeFormatter.ofPattern("-HHmm")));
				String fileName = "/WineMakerApp_Log_" + dayStamp + timeStamp + ".txt";

				outputFile = new File(logDir.getPath() + fileName);	
				File inputFile = new File(winemakerLogger.getLogFile().getPath());
				
				HelperFunctions.copyFile(inputFile.toPath(), outputFile.toPath());
				winemakerLogger.displayAlert(String.format("Fatal error starting the application. Send in the log file located at %s to geofromkc@gmail.com", outputFile.getPath()));
			}
			
			e.printStackTrace();
			System.exit(8);
		}
		
		winemakerLogger.writeLog(String.format("<< WineMakerMain.start()"), true);
	}

	/*
	 * Check for initial state.  There are various possible paths:
	 * 
	 * 		Scenario 1: no startup file exists, so assume a clean environment
	 * 		Scenario 2: startup file exists and matches install state, no further action needed
	 * 		Scenario 3/4 check: startup file exists but does not match install state, so prompt for next step
	 * 			Scenario 3: application was reinstalled, user will keep existing data, and startup file will be synched
	 * 			Scenario 4: application was reinstalled, next step will cleanup and create new directories
	 */
	private boolean setStartupState()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.setStartupState()"), true);

		propsFolder = new File(WineMakerModel.getLocalappdatahome());
		startupFile = new File(propsFolder.getPath() + File.separator + WineMakerModel.getStartupfilename());
		installedTime = new File(System.getProperty("user.dir")).lastModified();

		Instant installedInstant = Instant.ofEpochSecond(installedTime);
		
		winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): propsFolder: %s", propsFolder.getPath()), true);
		winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): startupFile: %s", startupFile.getPath()), true);
		winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): compare user.dir and user.home: '%s' & '%s'", System.getProperty("user.dir"), System.getProperty("user.home")), true);			

		boolean isFirstTime = true;
		
		/*
		 * For debugging purposes, log the system properties and environment variables
		 */
		winemakerLogger.writeLog(" ", true);
		winemakerLogger.writeLog("   WineMakerMain.setStartupState(): System Properties", true);
		Properties props = System.getProperties();
		props.keySet()
			.stream()
			.forEach(key -> winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): %s = '%s'", key, props.get(key)), true));

		winemakerLogger.writeLog(" ", true);
		winemakerLogger.writeLog("   WineMakerMain.setStartupState(): Environment Variables", true);
		Map<String, String> env = System.getenv();
		env
			.forEach((k, v) -> winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): %s = '%s'", k, v), true));
		
		/*
		 * analyze the runtime environment.   if the runtime location is somewhere in the user's
		 * default document path then this isn't a Windows installation
		 */
		if (System.getProperty("user.dir").contains(System.getProperty("user.home")))
		{
			isInstalledApp = false;
		}
		
		// if running in development environment, existence of startup file is enough 
		if (!isInstalledApp && startupFile.exists())
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): dev environment, found startup file %s", startupFile.toPath()), true);
			return !isFirstTime;
		}
		
		// Scenario 1: no startup file exists, so assume a clean environment
		if (!startupFile.exists())
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): no startup file found"), true);
			return isFirstTime;
		}
		
		Instant startupInstant = Instant.ofEpochSecond(startupFile.lastModified());
		winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): have startup file and existing environment, installed = %s, startup = %s", installedInstant.toString(), startupInstant.toString()), true);

		// Scenario 2: startup file exists and matches install state, no further action needed
		if (startupFile.lastModified() == installedTime)
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): installed environment, found synched startup file"), true);
			return !isFirstTime;
		}
		
		// Scenario 3/4 check: startup file exists but does not match install state, so prompt for next step
		Optional<ButtonType> result = setupPrompt("WineMakerRecordKeeper found existing setup", "Reply YES to keep existing application data.  Reply NO to delete any existing data.");

		// Scenario 3: application was reinstalled, user will keep existing data, and startup file will be synched
		if (result.get() == ButtonType.YES) 
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): new installed environment with old startup file in '%s', user will keep data", startupFile.getParent()), true);

			isFirstTime = false;
        	performCleanup = false;
        	startupFile.setLastModified(installedTime);
        }

		// Scenario 4: application was reinstalled, next step will cleanup and create new directories
		winemakerLogger.writeLog(String.format("   WineMakerMain.setStartupState(): new installed environment with old startup file, user will reinit data"), true);
		
		// quit if user cancelled
		if (result.get() == ButtonType.CANCEL)
			System.exit(0);
		
		winemakerLogger.writeLog(String.format("<< WineMakerMain.setStartupState()"), true);
		return isFirstTime;
	} // end of setStartupState()
	
	/*
	 * Prompt user for new location of application data, then create the tables
	 */
	private void setApplicationDir()
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.setApplicationDir()"), true);

		boolean setDefaults = false;

		Optional<ButtonType> result = setupPrompt("WineMaker Create Application Files", "Reply YES to use application defaults.  Reply NO to select application directories.");
		
		if (result.get() == ButtonType.CANCEL)
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user cancelled startup"), true);
			winemakerLogger.writeLog(String.format("<< WineMakerMain.setApplicationDir()"), true);
			System.exit(0);			
		}
	
		if (result.get() == ButtonType.YES) 
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user will take defaults"), true);
			setDefaults = true;
		}
		
		File appFilesDir = null;
		File appDataBackupDir = null;
		
		if (setDefaults)
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): define default directories"), true);
			appFilesDir = new File(WineMakerModel.getDefaultapphome());
			appDataBackupDir = new File(WineMakerModel.getDefaultbackuphome());
		}
		else
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): prompt user for new directories"), true);
			appFilesDir = promptForDir("WineMaker Select Home for Application Data", WineMakerModel.getDefaultappname());
			
			if (appFilesDir != null)
				appDataBackupDir = promptForDir("WineMaker Select Home for Backup Storage", WineMakerModel.getDefaultbackupdirname());
		}
		
		/*
		 * Quick exit if user cancels selection step
		 */
		if (appFilesDir == null || appDataBackupDir == null)
			System.exit(0);
		
		if (appFilesDir.getPath().equals(appDataBackupDir.getPath()))
		{
			winemakerLogger.displayAlert("The application data and backup directories are the same.  Please try again and select a different directory for one of them");
			System.exit(0);	
		}

		File testForDb = new File(String.format("%s/%s", appFilesDir.getPath(), "/winemaker/service.properties"));
		winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): test if properties file at %s already exist", testForDb.getPath()), true);

		if (testForDb.exists())
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): data already exists, prompt user for decision"), true);
			Optional<ButtonType> overwritePrompt = setupPrompt("WineMaker Create Application Files", "It appears that data already exists at the selected location.  Reply YES to use the existing data.  Reply NO to create new data files.");

			if (overwritePrompt.get() == ButtonType.CANCEL)
			{
				winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user cancelled startup"), true);
				winemakerLogger.writeLog(String.format("<< WineMakerMain.setApplicationDir()"), true);
				System.exit(0);			
			}

			if (overwritePrompt.get() == ButtonType.YES) 
			{
				winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user will reuse data files"), true);
				performCleanup = false;
			}
			else
				winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user will create new data files"), true);				
		}
		
		winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user selected '%s' and '%s'", appFilesDir.getPath(), appDataBackupDir.getPath()), true);
		
		if (performCleanup)
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.setApplicationDir(): user selected clean install, so remove existing data"), true);

			if (appFilesDir.exists())
				directoryCleanup(appFilesDir);
			if (appDataBackupDir.exists())
				directoryCleanup(appDataBackupDir);
		}

		/*
		 * Whichever path was taken, create the application data and backup directories
		 */
		directoryCreate(appFilesDir);
		createAppFiles(appFilesDir, appDataBackupDir, setDefaults);
		
		winemakerLogger.writeLog(String.format("<< WineMakerMain.setApplicationDir()"), true);
		
		return;
	} // end of setApplicationDir()

	/*
	 * Prompt for location of application files: default or user-selected
	 */
	private Optional<ButtonType> setupPrompt(String alertTitle, String alertText)
	{
		Alert initialStatePrompt = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		
		initialStatePrompt.setTitle(alertTitle);
		initialStatePrompt.setContentText(alertText);
		
		Optional<ButtonType> result = initialStatePrompt.showAndWait();
	
		return result;
	} // end of setupPrompt()

	/*
	 * Create application files for this installation
	 */
	private void createAppFiles(File appFilesDir, File appDataBackupDir, boolean useDefaults)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.createAppFiles(%s, %s)", appFilesDir.getPath(), appDataBackupDir.getPath()), true);

		File propsFolder = new File(WineMakerModel.getLocalappdatahome());
		File propsFile = new File(propsFolder + WineMakerModel.getPropertiesfilename());
		File startupFile = new File(propsFolder + WineMakerModel.getStartupfilename());

		/*
		 * create default properties folder	
		 */
		if (propsFolder.exists())
			directoryCleanup(propsFolder);
		
		directoryCreate(propsFolder);
		directoryCreate(appDataBackupDir);
		
		winemakerLogger.writeLog(String.format("   WineMakerMain.createAppFiles(): already created: %n\t\t'%s'", appFilesDir.getPath()), true);
		winemakerLogger.writeLog(String.format("   WineMakerMain.createAppFiles(): now created: %n\t\t'%s'%n\t\t'%s'", propsFolder.getPath(), appDataBackupDir.getPath()), true);

		String setDefault = (useDefaults) ? "1" : "0";
		writePropsAndStartupFile(appFilesDir, appDataBackupDir, propsFile, startupFile, setDefault);
		
		if (isInstalledApp)
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.createAppFiles(): set timestamp on startup file '%s'", startupFile.getPath()), true);

			startupFile.setLastModified(installedTime);				
		}
		winemakerLogger.writeLog(String.format("<< WineMakerMain.createAppFiles(%s, %s)", appFilesDir.getPath(), appDataBackupDir.getPath()), true);
		return;
	} // end of createAppFiles()
	
	/*
	 * Create new installation directory
	 */
	private void directoryCreate(File newDir)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.directoryCreate(%s)", newDir.getPath()), true);

		try 
		{
			Files.createDirectory(Paths.get(newDir.getPath()));
		} 
		catch (FileAlreadyExistsException ef)
		{}
		catch (IOException e) 
		{
			winemakerLogger.showIOException(e, "Failed to create application directory", true);
			winemakerLogger.displayAlert("Could not create directory '" + newDir.getPath() + "', the application will now exit");
			System.exit(0);	
		}

		winemakerLogger.writeLog(String.format("<< WineMakerMain.directoryCreate(%s)", newDir.getPath()), true);
		
		return;
	} // end of directoryCreate()

	/*
	 * Delete file structure of prior installation
	 */
	private void directoryCleanup(File existingDir)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.directoryCleanup(%s)", existingDir.getPath()), true);

		try (Stream<Path> walk = Files.walk(Paths.get(existingDir.getPath()))) {
            walk
            	.map(filePath -> filePath.toFile())
            	.sorted(Comparator.reverseOrder())
            	.forEach(fileTarget -> fileTarget.delete());
        } 
		catch (IOException e) 
		{
            winemakerLogger.showIOException(e, "Failed to cleanup app folder");
            winemakerLogger.displayAlert("Application failed to cleanup previous install, will now exit");
            System.exit(0);
        }
		
		winemakerLogger.writeLog(String.format("<< WineMakerMain.directoryCleanup(%s)", existingDir.getPath()), true);
		return;
	} // end of directoryCleanup()
	
	/*
	 * Prompt user for customer directory to store the database files and backups
	 */
	private File promptForDir(String selectTitle, String terminalEnd)	
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.promptForDir('%s', '%s')", selectTitle, terminalEnd), true);
		
		File newDir = null;
		
		try 
		{
			DirectoryChooser dc = new DirectoryChooser();
	        dc.setInitialDirectory(new File(WineMakerModel.getDefaultappsearch()));
	        dc.setTitle(selectTitle);

	        File logRoot = dc.showDialog(winemakerModel.getFxStage());
	        newDir = new File(logRoot.getPath() + terminalEnd);
		} 
		catch (NullPointerException e) 
		{
			winemakerLogger.writeLog(String.format("   WineMakerMain.promptForDir(): user cancelled prompt"), true);
		}
		catch (Exception e1) 
		{
			winemakerLogger.showIOException(e1, "");
		}

		winemakerLogger.writeLog(String.format("<< WineMakerMain.promptForDir('%s')", selectTitle), true);
		
		return newDir;
	} // end of promptForDir()

	/*
	 * Using the default or custom locations, create the database properties file
	 */
	private boolean writePropsAndStartupFile(File appFilesDir, File backupDir, File propsFile, File startupFile, String setDefault)
	{
		winemakerLogger.writeLog(String.format(">> WineMakerMain.writePropsFile()"), true);

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
			myWriter.write("DBDEFAULTS=" + setDefault);
			myWriter.close();
			
			startupFile.createNewFile();
			
			winemakerLogger.writeLog(String.format("   WineMakerMain.writePropsFile(): created properties file '%s'", propsFile.getPath()), true);
			winemakerLogger.writeLog(String.format("   WineMakerMain.writePropsFile(): created startup file '%s'", startupFile.getPath()), true);
		} 
		catch (IOException e) 
		{
			winemakerLogger.showIOException(e, "Failed to write properties and/or startup file");
			writeStatus = false;
		}

		winemakerLogger.writeLog(String.format("<< WineMakerMain.writePropsFile()"), true);
		return writeStatus;
	} // end of writePropsFile()
	
	public static void main(String[] args) 
	{	
		launch(args);
	}
}
