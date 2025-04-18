package geo.apps.winemaker.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
//import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import geo.apps.winemaker.WineMakerModel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;

/**
 * Provide application-level logging
 */
public class WineMakerLogging {

	File logFile = new File("/");
	
	WineMakerModel model = null;
	FileWriter myWriter;
	Runtime runtime = Runtime.getRuntime();
	
	int tabLevel = 2;
	boolean errorLogging = false;
	String logFileName = "/WineMakerLog.txt";
	String tabAdjust = "";
	String methodEnd = "";
	
	public WineMakerLogging()
	{
		try
		{
		    runtime.addShutdownHook(new Thread()
		    {
	    	@Override
	    	public void run()
	    	{
	    		try 
	    		{
	    			myWriter.write("\n\nApplication shutting down, initiate close of log file");
					myWriter.close();
				} 
	    		catch (IOException e) 
	    		{}
	    	}
		    });
		}
		catch (Exception e)
		{}		
	}
	
	public File getLogFile() {
		return logFile;
	}

	@SuppressWarnings("unused")
	private boolean defineDocumentsLogFile()
	{
		boolean returnStatus = true;

		if (model.getLogFilePath() != null && model.getLogFilePath().length() > 0)
			this.logFile = new File(model.getLogFilePath());
		else
		{
			File logFileFolder = null;
			try 
			{
				File userDocumentsFolder = new File(System.getenv("HOMEPATH") + "/Documents");
				if (userDocumentsFolder.exists())
				{
					logFileFolder = new File(System.getenv("HOMEPATH") + "/Documents/WineMakerRecordKeeper");
					if (!logFileFolder.exists())
						logFileFolder.mkdir();
					
					this.logFile = new File(System.getenv("HOMEPATH") + "/Documents/WineMakerRecordKeeper" + this.logFileName);
			        returnStatus = this.logFile.exists() && this.logFile.canRead() && this.logFile.canWrite();
				}
			} 
			catch (NullPointerException e) 
			{
				returnStatus = false;
				displayAlert(String.format("An error occurred trying to access log file in %s: '%s' %n", logFileFolder.getPath(), e.getMessage()));
			}
		}
		
		return returnStatus;
	}

	private boolean defineLogFile()
	{
		boolean returnStatus = true;

		if (model.getLogFilePath() != null && model.getLogFilePath().length() > 0)
			this.logFile = new File(model.getLogFilePath());
		else
		{
			File logFileFolder = null;
			try 
			{
				File userAppDataFolder = new File(System.getenv("APPDATA"));
				if (userAppDataFolder.exists())
				{
					logFileFolder = new File(userAppDataFolder.getPath() + "/WineMakerRecordKeeper");
					if (!logFileFolder.exists())
						if (!logFileFolder.mkdir())
							throw new NullPointerException();
					
					this.logFile = new File(logFileFolder.getPath() + this.logFileName);
				}
			} 
			catch (NullPointerException e) 
			{
				returnStatus = false;
				displayAlert(String.format("An error occurred trying to access/create log file folder %s: '%s' %n", logFileFolder.getPath(), e.getMessage()));
			}
		}
		
		return returnStatus;
	}

	private boolean openLogFile()
	{
		boolean returnStatus = true;

		try 
		{
			boolean appendLog = (this.logFile.exists() && Files.size(this.logFile.toPath()) > 500000) ? false : true;
			if (!appendLog)
			{

				String[] currentContent = returnFileContents(logFile);
				int startLine = (int) (currentContent.length * 0.2);
				this.myWriter = new FileWriter(this.logFile, false);

				for (int cursor = startLine; cursor < currentContent.length; cursor++)
				{
					myWriter.write(currentContent[cursor] + "\n");
				}
				myWriter.close();
			}
			
			this.myWriter = new FileWriter(this.logFile, true);
		} 
		catch (IOException e) 
	    {
			returnStatus = false;
			displayAlert(String.format("An error occurred opening log file %s: '%s' %n", this.logFile.getPath(), e.getMessage()));
	    }

		return returnStatus;
	}
	
	private String[] returnFileContents(File inputFile)
	{
		List<String> lines = new ArrayList<String>();
		String oneLine = null;

		try {
			FileReader fileReader = new FileReader(inputFile.getPath());
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((oneLine = bufferedReader.readLine()) != null) {
				lines.add(oneLine);
			}

			bufferedReader.close();
		} 
		catch (IOException e2) 
		{
			// winemakerLogger.showIOException(e2, "Failure reading codes file");
		}
		
		return lines.toArray(new String[lines.size()]);
	}
	
	private boolean promptForLogfile()
	{
		boolean returnStatus = true;
		
		try 
		{
			DirectoryChooser dc = new DirectoryChooser();
	        dc.setInitialDirectory(new File(System.getenv("HOMEPATH")));
	        dc.setTitle("Log File Define");
	        File logDir = dc.showDialog(this.model.getFxStage());
	        this.logFile = new File(logDir.getPath() + this.logFileName);
	        
			this.myWriter = new FileWriter(logFile, false);

			returnStatus = this.logFile.exists() && this.logFile.canRead() && this.logFile.canWrite();
		} 
		catch (Exception e)
		{
			returnStatus = false;
			displayAlert(String.format("An error occurred choosing log file %s: '%s' %n", this.logFile.getPath(), e.getMessage()));
		}
	
		return returnStatus;
	} // end of promptForLogfile()
		
	/**
	 * Two main functions, to load the pointer to the Model object, and to define the logging file
	 * @param model pointer to the common Model object
	 */
	public void loadDependencies(WineMakerModel model)
	{
		this.model = model;
		boolean isLogFileValid = true;

		/*
		 * First attempt to define the log file in the user's APPDATA folder.
		 * If for some reason the definition fails, or the file can't be written to, prompt the user
		 * 		for a new location for the file.
		 * If all attempts fails, null out the Log object and alert the user 
		 */
		if (defineLogFile())
		{
			if (!openLogFile())
				isLogFileValid = promptForLogfile();
		}
		else
			isLogFileValid = promptForLogfile();

		if (!isLogFileValid)
		{
			displayAlert(String.format("Failed to define the log file '%s', no logging will be done", this.logFile.getPath()));
			this.logFile = null;
		}
		else
			reinitFile();
	}

	private void reinitFile()
	{
	    try {
	    	myWriter.write("\n\n=========================");
	    	myWriter.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")));
	        myWriter.write("=========================");
	    } 
	    catch (IOException e) 
	    {
			displayAlert(String.format("An error occurred writing to log file %s: '%s' %n", logFile.getPath(), e.getMessage()));
	    }
	    
	    return;
	}
	
	/**
	 * Write current entry to log file.  If no log file was created or found, skip the output
	 * @param logEntry String log entry
	 * @param debugMode switch enabling logging
	 */
	public void writeLog(String logEntry, boolean debugMode)
	{
		if (logEntry != null && logEntry.contains(">>"))
			tabAdjust += "   ";
		
		if ((debugMode && model.getDebugActive()) || !debugMode)
		{
			if (logFile != null)
			{
			    try {
			        myWriter.write(String.format("%n%s(%d): %s%s%s", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SS")), tabAdjust.length(), tabAdjust, logEntry, methodEnd));
			        if (logEntry.contains("<<"))
			        	myWriter.write(String.format("%n"));

			        if (logEntry.contains("<<"))
						tabAdjust = tabAdjust.substring(0, tabAdjust.length() - 3);

			        myWriter.flush();
			      } 
			    catch (IOException e) 
			    {
					displayAlert(String.format("An error occurred Writing to log file %s: '%s' %n", logFile.getPath(), e.getMessage()));
			    }
			}
			else
			{
				LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SS"));
				System.out.printf("%n%s: %s", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SS")), logEntry);
			}			
		}
	}
	
	public boolean showAlarm(String alarmMsg, AlertType msgType)
	{
		Alert alertWarning = new Alert(msgType);

		alertWarning.setTitle("WineMaker Process Confirmation");
		alertWarning.setContentText(alarmMsg);

		Optional<ButtonType> result = alertWarning.showAndWait();
		if (result.get() == ButtonType.OK){
			return true;
		} else {
			return false;
		}
	}
	
	public void displayAlert(String alertMessage)
	{
		Alert alertWarning = new Alert(AlertType.ERROR, "", ButtonType.OK);
		
		alertWarning.setTitle("WineMaker Error Alert");
 		alertWarning.setContentText(alertMessage);
 		alertWarning.showAndWait();
	}

	@SuppressWarnings("unused")
	private String showAnyException(StackTraceElement[] stackTrace, String eMessage, String titleText, StackTraceElement[] cause)
	{
		String statusText = titleText;
		
		writeLog(eMessage, errorLogging);
				
		for (StackTraceElement st : stackTrace)
		{
			if (st.getClassName().substring(0, 9).equals("winemaker")) 
			{
				statusText += String.format("\t%s%n", st.toString());
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}
			else
				writeLog(String.format("\t%s", st.toString()), errorLogging);
		}
		
		if (cause != null)
		{
			writeLog("\nCaused by:", errorLogging);
			
			for (StackTraceElement st : cause)
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}
		}
				
		return statusText;
	}
	
	private String showAnyException(StackTraceElement[] stackTrace, String eMessage, String titleText, StackTraceElement[] cause, boolean allOfIt)
	{
		String statusText = titleText;
		
		writeLog(String.format("Message: %s", eMessage), errorLogging);
				
		for (StackTraceElement st : stackTrace)
		{
			writeLog(String.format("\t%s", st.toString()), errorLogging);
		}
		
		if (cause != null)
		{
			writeLog("\nCaused by:", errorLogging);
			
			for (StackTraceElement st : cause)
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}
		}
		
		return statusText;
	}
	
	public String showIOException(Exception e, String titleText)
	{
		writeLog(String.format("Message: %s%n", e.getMessage()), errorLogging);
		
		for (StackTraceElement st : e.getStackTrace())
		{
			writeLog(String.format("\t%s", st.toString()), errorLogging);
		}
		
		writeLog(" ", errorLogging);
		writeLog("Caused by:", errorLogging);
		
		if (e.getCause() != null)
		{
			writeLog(String.format("Message: %s%n", e.getCause().getMessage()), errorLogging);
			for (StackTraceElement st : e.getCause().getStackTrace())
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}			
		}
		else
		{
			writeLog(String.format("Message: %s%n", e.getMessage()), errorLogging);			
			for (StackTraceElement st : e.getStackTrace())
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}			
		}
		
		
		return titleText;
	}

	public String showIOException(IOException e, String titleText)
	{
		writeLog(String.format(titleText), errorLogging);
		writeLog(String.format("Message: %s%n", e.getMessage()), errorLogging);
		e.printStackTrace();
		
		for (StackTraceElement st : e.getStackTrace())
		{
			writeLog(String.format("\t%s", st.toString()), errorLogging);
		}
		
		writeLog(" ", errorLogging);
		writeLog("Caused by:", errorLogging);
		writeLog(String.format("Message: %s%n", e.getMessage()), errorLogging);
		
		if (e.getCause() != null)
		{
			for (StackTraceElement st : e.getCause().getStackTrace())
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}
		}
		else
		{
			for (StackTraceElement st : e.getStackTrace())
			{
				writeLog(String.format("\t%s", st.toString()), errorLogging);
			}
		}
		
		return titleText;
	}
	
	public String showIOException(Exception e, String titleText, boolean allOfIt)
	{
		
		return showAnyException(e.getStackTrace(), e.getMessage(), titleText, e.getCause().getStackTrace(), allOfIt);
	}

	public String showSqlException(SQLException e, String titleText)
	{
		int errorCode = 0;
		String errorState = "";
		String statusText = titleText;
		StackTraceElement[] stackTrace = null;

		statusText += titleText + ": Database Exception";
		writeLog(String.format(statusText), errorLogging);
		
		// Unwrap the entire exception chain to unveil the real cause of the Exception.
		while (e != null)
		{
			errorCode = e.getErrorCode();
			errorState = e.getSQLState();
			
			writeLog("\n----- SQLException -----", errorLogging);
			writeLog("  SQL State:  " + errorState, errorLogging);
			writeLog("  Error Code: " + errorCode, errorLogging);

			if (errorCode == -4229)
			{
				writeLog("  Message: " + e.getMessage(), errorLogging);
				e = e.getNextException();
				continue;
			}
			else if (errorCode == -727 && errorState.equals("56098"))
			{
				writeLog("  Message: " + e.getMessage(), errorLogging);
				e = e.getNextException();
				continue;
			}
			else if (errorCode == -204 && errorState.equals("42704"))
			{
				writeLog("  Message: " + "missing table", errorLogging);
				statusText += "Missing database table";
				break;
			}
			else if (errorCode == -803 && errorState.equals("23505"))
			{
				writeLog("  Message: " + "duplicate key id", errorLogging);
				statusText += "Duplicate key";
				break;
			}
			else if (errorCode == -302 && errorState.equals("22001"))
			{
				writeLog("  Message: " + "some input value too large for definition\n\t" + e.getMessage(), errorLogging);
				statusText += "Some input value too large: " + e.getMessage().substring(0, e.getMessage().indexOf(":"));
				break;
			}
			else
			{
				writeLog("  Message: " + e.getMessage(), errorLogging);
				statusText += e.getMessage();
			}
			
			if (errorCode != -4471)
			{
				stackTrace = e.getStackTrace();

				for (StackTraceElement st : stackTrace)
				{
					writeLog(String.format("\t%s", st.toString()), errorLogging);
					if (st.getClassName().substring(0, 9).equals("winemaker")) 
					{
						writeLog(String.format("\t%s", st.toString()), errorLogging);
					}
				}
			}

			e = e.getNextException();
		}

		writeLog("\n==========================================================================", errorLogging);
		
		return statusText;
	}	
}
