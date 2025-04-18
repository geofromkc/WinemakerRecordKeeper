package geo.apps.winemaker;

import java.sql.Timestamp;
//import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class WineMakerTesting {

	private final String empty = "";
	
	private String _batchKey = empty;
	private Timestamp _entry_date = null;
	private String _testType;
	private double _testValue;
	private String _testScale;
	private double _testTemp;
	private String _tempScale;
	private String _testNotes;
	
	private WineMakerModel winemakerModel = null;
	
	public WineMakerTesting(WineMakerModel winemakerModel) {
		this.winemakerModel = winemakerModel;
	}

	public String get_batchKey() {
		return _batchKey;
	}

	public void set_batchKey(String _batchKey) {
		this._batchKey = _batchKey;
	}

	public Timestamp get_entry_date() {
		return _entry_date;
	}

	public void set_entry_date(Timestamp _entry_date) {
		this._entry_date = _entry_date;
	}

	public String get_testType() {
		return _testType;
	}

	public void set_testType(String _testType) {
		this._testType = _testType;
	}

	public double get_testValue() {
		return _testValue;
	}

	public void set_testValue(double _testValue) {
		this._testValue = _testValue;
	}

	public String get_testScale() {
		return _testScale;
	}

	public void set_testScale(String _testScale) {
		this._testScale = _testScale;
	}

	public double get_testTemp() {
		return _testTemp;
	}

	public void set_testTemp(double _testTemp) {
		this._testTemp = _testTemp;
	}

	public String get_tempScale() {
		return _tempScale;
	}

	public void set_tempScale(String _tempScale) {
		this._tempScale = _tempScale;
	}

	public String get_testNotes() {
		return _testNotes;
	}

	public void set_testNotes(String _testNotes) {
		this._testNotes = _testNotes;
	}

	public WineMakerTesting newCopy(WineMakerTesting sourceRecord)
	{
		WineMakerTesting targetRecord = new WineMakerTesting(winemakerModel);

		Timestamp selectedDate = sourceRecord.get_entry_date();
		long newTime = selectedDate.getTime() + 1000;
		selectedDate.setTime(newTime);
		
		targetRecord.set_entry_date(selectedDate);
		targetRecord.set_testType(sourceRecord.get_testType());
		targetRecord.set_testValue(sourceRecord.get_testValue());
		targetRecord.set_testScale(sourceRecord.get_testScale());
		targetRecord.set_testTemp(sourceRecord.get_testTemp());	
		targetRecord.set_tempScale(sourceRecord.get_tempScale());
		targetRecord.set_testNotes(sourceRecord.get_testNotes());

		return targetRecord;
	} // end of newCopy()
	
	@Override
	public String toString()
	{
		String showObject = "";
		
		showObject += String.format("WineMakerTest record:%nBatch id = %s%n", this.get_batchKey());
		
		Timestamp ts = this.get_entry_date();
		LocalDateTime entryDate = ts.toLocalDateTime();
		
		showObject += String.format("Entry date = '%s' %n", entryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		
		showObject += String.format("Test Type = '%s' %n", this.get_testType());
		showObject += String.format("Test Value = '%1.2f' %n", this.get_testValue());
		showObject += String.format("Test Unit = '%s' %n", this.get_testScale());
		showObject += String.format("Temperature = '%1.2f %s' %n", this.get_testTemp(), this.get_tempScale());
		showObject += (this.get_testNotes().length() > 0) ?
				String.format("Stage notes = %n\t'%s' %n", this.get_testNotes()) : "";
		
		return showObject;
	} // end of toString()
	
	public static String toCSVHeader()
	{
		return "Entry Date,Batch,Test,Value,Unit,Temp,Notes";
	} // end of toCSVHeader()

	public String toCSV()
	{
		String showObject = "";
		HashMap<String, String> codeSet;
		
		Timestamp ts = this.get_entry_date();
		LocalDateTime entryDate = ts.toLocalDateTime();
		
		showObject += String.format("%s,", entryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		showObject += String.format("%s,", HelperFunctions.batchKeyExpand(this.get_batchKey()));
		
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.LABTESTFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_testType()));
		
		showObject += String.format("%1.2f%s,", this.get_testValue(), this.get_testScale());
		showObject += String.format("%1.2f%s,", this.get_testTemp(), this.get_tempScale());

		showObject += String.format("%s", this.get_testNotes().replace(",", " - "));
		
		return showObject;
	} // end of toCSV()
}
