   package geo.apps.winemaker;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class WineMakerFerment {

	private final String empty = "";
	
	private String _batchKey = empty;
	private Timestamp _entry_date = null;
	private String _fermentActivity = empty;
	private int _inputGrapeAmt = 0;
	private int _outputMustVolume = 0;
	private String _yeastStrain = empty;
	private String _chemAdded  = empty;
	private double _chemAmount = 0;
	private String _chemScale  = empty;
	private double _starterYeastAmt = 0;
	private int _starterH2OAmt = 0;
	private int _starterJuiceAmt = 0;
	private double _currBrix = 0;
	private double _currpH = 0;
	private double _currTA = 0;
	private String _coldLocation = empty;
	private Timestamp _startDate = null;
	private Timestamp _endDate = null;
	private String _punchTool = empty;
	private String _containerType = empty;
	private String _containerType2 = empty;
	private String _containerType3 = empty;
	private int _containerCount = 0;
	private int _container2Count = 0;
	private int _container3Count = 0;
	private int _containerVol = 0;
	private int _container2Vol = 0;
	private int _container3Vol = 0;
	private int _inputJuiceVol = 0;
	private int _outputJuiceVol = 0;
	private int _currentStageJuiceVol = 0;
	private String _inputJuiceScale = empty;
	private String _outputJuiceScale = empty;
	private String _currentStageJuiceScale = empty;	
	private String _rackSource = empty;
	private String _rackTarget1 = empty;
	private String _rackTarget2 = empty;
	private String _rackTarget3 = empty;
	private int _rackTarget1Count = 0;
	private int _rackTarget2Count = 0;
	private int _rackTarget3Count = 0;
	private int _bottleCount = 0;
	private int _currentTemp = 0;
	private int _startTemp = 0;
	private int _endingTemp = 0;
	private int _pressCycle = 0;
	private int _stageCycle = 0;
	private int _stageDuration = 0;
	private int _yeastActiveLevel = 0;
	private String _tempScale = "";
	private String _fermentNotes = empty;

	private WineMakerModel winemakerModel = null;
	
	/*
	 * Constructor + getters and setters
	 */
	public WineMakerFerment(WineMakerModel winemakerModel) 
	{
		this.winemakerModel = winemakerModel;
		
		Timestamp ts = Timestamp.valueOf("2000-01-01 00:00:00.000000");
		this.set_startDate(ts);
		this.set_endDate(ts);
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

	public String get_fermentActivity() {
		return _fermentActivity;
	}

	public void set_fermentActivity(String _fermentActivity) {
		this._fermentActivity = _fermentActivity;
	}

	public int get_inputGrapeAmt() {
		return _inputGrapeAmt;
	}

	public void set_inputGrapeAmt(int _inputGrapeAmt) {
		this._inputGrapeAmt = _inputGrapeAmt;
	}

	public int get_outputMustVolume() {
		return _outputMustVolume;
	}

	public void set_outputMustVolume(int _outputMustVolume) {
		this._outputMustVolume = _outputMustVolume;
	}

	public String get_yeastStrain() {
		return _yeastStrain;
	}

	public void set_yeastStrain(String _yeastStrain) {
		this._yeastStrain = _yeastStrain;
	}

	public String get_chemAdded() {
		return _chemAdded;
	}

	public void set_chemAdded(String _chemAdded) {
		this._chemAdded = _chemAdded;
	}

	public double get_chemAmount() {
		return _chemAmount;
	}

	public void set_chemAmount(double _chemAmount) {
		this._chemAmount = _chemAmount;
	}

	public String get_chemScale() {
		return _chemScale;
	}

	public void set_chemScale(String _chemScale) {
		this._chemScale = _chemScale;
	}

	public double get_starterYeastAmt() {
		return _starterYeastAmt;
	}

	public void set_starterYeastAmt(double _starterYeastAmt) {
		this._starterYeastAmt = _starterYeastAmt;
	}

	public int get_starterH2OAmt() {
		return _starterH2OAmt;
	}

	public void set_starterH2OAmt(int _starterH2OAmt) {
		this._starterH2OAmt = _starterH2OAmt;
	}

	public int get_starterJuiceAmt() {
		return _starterJuiceAmt;
	}

	public void set_starterJuiceAmt(int _starterJuiceAmt) {
		this._starterJuiceAmt = _starterJuiceAmt;
	}

	public double get_currBrix() {
		return _currBrix;
	}

	public void set_currBrix(double _currBrix) {
		this._currBrix = _currBrix;
	}

	public double get_currpH() {
		return _currpH;
	}

	public void set_currpH(double _currpH) {
		this._currpH = _currpH;
	}

	public double get_currTA() {
		return _currTA;
	}

	public void set_currTA(double _currTA) {
		this._currTA = _currTA;
	}

	public String get_coldLocation() {
		return _coldLocation;
	}

	public void set_coldLocation(String _coldLocation) {
		this._coldLocation = _coldLocation;
	}

	public Timestamp get_startDate() {
		return _startDate;
	}

	public void set_startDate(Timestamp _startDate) {
		this._startDate = _startDate;
	}

	public Timestamp get_endDate() {
		return _endDate;
	}

	public void set_endDate(Timestamp _endDate) {
		this._endDate = _endDate;
	}

	public String get_punchTool() {
		return _punchTool;
	}

	public void set_punchTool(String _punchTool) {
		this._punchTool = _punchTool;
	}

	public String get_containerType() {
		return _containerType;
	}

	public void set_containerType(String _containerType) {
		this._containerType = _containerType;
	}

	public String get_containerType2() {
		return _containerType2;
	}

	public void set_containerType2(String _containerType) {
		this._containerType2 = _containerType;
	}

	public String get_containerType3() {
		return _containerType3;
	}

	public void set_containerType3(String _containerType3) {
		this._containerType3 = _containerType3;
	}

	public int get_containerCount() {
		return _containerCount;
	}

	public void set_containerCount(int _containerCount) {
		this._containerCount = _containerCount;
	}

	public int get_container2Count() {
		return _container2Count;
	}

	public void set_container2Count(int _container2Count) {
		this._container2Count = _container2Count;
	}

	public int get_container3Count() {
		return _container3Count;
	}

	public void set_container3Count(int _container3Count) {
		this._container3Count = _container3Count;
	}

	public int get_containerVol() {
		return _containerVol;
	}

	public void set_containerVol(int _containerVol) {
		this._containerVol = _containerVol;
	}

	public int get_container2Vol() {
		return _container2Vol;
	}

	public void set_container2Vol(int _container2Vol) {
		this._container2Vol = _container2Vol;
	}

	public int get_container3Vol() {
		return _container3Vol;
	}

	public void set_container3Vol(int _container3Vol) {
		this._container3Vol = _container3Vol;
	}

	public int get_inputJuiceVol() {
		return _inputJuiceVol;
	}

	public void set_inputJuiceVol(int _inputJuiceVol) {
		this._inputJuiceVol = _inputJuiceVol;
	}

	public int get_outputJuiceVol() {
		return _outputJuiceVol;
	}

	public void set_outputJuiceVol(int _outputJuiceVol) {
		this._outputJuiceVol = _outputJuiceVol;
	}

	public int get_currentStageJuiceVol() {
		return _currentStageJuiceVol;
	}

	public void set_currentStageJuiceVol(int _currentStageJuiceVol) {
		this._currentStageJuiceVol = _currentStageJuiceVol;
	}

	public String get_inputJuiceScale() {
		return _inputJuiceScale;
	}

	public void set_inputJuiceScale(String _inputJuiceScale) {
		this._inputJuiceScale = _inputJuiceScale;
	}

	public String get_outputJuiceScale() {
		return _outputJuiceScale;
	}

	public void set_outputJuiceScale(String _outputJuiceScale) {
		this._outputJuiceScale = _outputJuiceScale;
	}

	public String get_currentStageJuiceScale() {
		return _currentStageJuiceScale;
	}

	public void set_currentStageJuiceScale(String _currentStageJuiceScale) {
		this._currentStageJuiceScale = _currentStageJuiceScale;
	}

	public String get_rackSource() {
		return _rackSource;
	}

	public void set_rackSource(String _rackSource) {
		this._rackSource = _rackSource;
	}

	public String get_rackTarget1() {
		return _rackTarget1;
	}

	public void set_rackTarget1(String _rackTarget1) {
		this._rackTarget1 = _rackTarget1;
	}

	public String get_rackTarget2() {
		return _rackTarget2;
	}

	public void set_rackTarget2(String _rackTarget2) {
		this._rackTarget2 = _rackTarget2;
	}

	public String get_rackTarget3() {
		return _rackTarget3;
	}

	public void set_rackTarget3(String _rackTarget3) {
		this._rackTarget3 = _rackTarget3;
	}

	public int get_rackTarget1Count() {
		return _rackTarget1Count;
	}

	public void set_rackTarget1Count(int _rackTarget1Count) {
		this._rackTarget1Count = _rackTarget1Count;
	}

	public int get_rackTarget2Count() {
		return _rackTarget2Count;
	}

	public void set_rackTarget2Count(int _rackTarget2Count) {
		this._rackTarget2Count = _rackTarget2Count;
	}

	public int get_rackTarget3Count() {
		return _rackTarget3Count;
	}

	public void set_rackTarget3Count(int _rackTarget3Count) {
		this._rackTarget3Count = _rackTarget3Count;
	}

	public int get_bottleCount() {
		return _bottleCount;
	}

	public void set_bottleCount(int _bottleCount) {
		this._bottleCount = _bottleCount;
	}

	public int get_currentTemp() {
		return _currentTemp;
	}

	public void set_currentTemp(int _currentTemp) {
		this._currentTemp = _currentTemp;
	}

	public int get_startTemp() {
		return _startTemp;
	}

	public void set_startTemp(int _startTemp) {
		this._startTemp = _startTemp;
	}

	public int get_endingTemp() {
		return _endingTemp;
	}

	public void set_endingTemp(int _endingTemp) {
		this._endingTemp = _endingTemp;
	}

	public int get_pressCycle() {
		return _pressCycle;
	}

	public void set_pressCycle(int _pressCycle) {
		this._pressCycle = _pressCycle;
	}

	public int get_stageCycle() {
		return _stageCycle;
	}

	public void set_stageCycle(int _stageCycle) {
		this._stageCycle = _stageCycle;
	}

	public int get_stageDuration() {
		return _stageDuration;
	}

	public void set_stageDuration(int _stageDuration) {
		this._stageDuration = _stageDuration;
	}

	public int get_yeastActiveLevel() {
		return _yeastActiveLevel;
	}

	public void set_yeastActiveLevel(int _yeastActiveLevel) {
		this._yeastActiveLevel = _yeastActiveLevel;
	}

	public String get_tempScale() {
		return _tempScale;
	}

	public void set_tempScale(String _tempScale) {
		this._tempScale = _tempScale;
	}

	public String get_fermentNotes() {
		return _fermentNotes;
	}

	public void set_fermentNotes(String _fermentNotes) {
		this._fermentNotes = _fermentNotes;
	}

	/**
	 * Create a new Ferment object from the current instance
	 * @return New Ferment data object
	 */
	public WineMakerFerment newCopy()
	{
		WineMakerFerment targetRecord = new WineMakerFerment(this.winemakerModel);

		targetRecord.set_entry_date(get_entry_date());
		targetRecord.set_fermentActivity(get_fermentActivity());
		targetRecord.set_inputGrapeAmt(get_inputGrapeAmt());
		targetRecord.set_outputMustVolume(get_outputMustVolume());
		targetRecord.set_yeastStrain(get_yeastStrain());
		targetRecord.set_chemAdded(get_chemAdded());
		targetRecord.set_chemAmount(get_chemAmount());
		targetRecord.set_chemScale(get_chemScale());
		targetRecord.set_starterYeastAmt(get_starterYeastAmt());
		targetRecord.set_starterH2OAmt(get_starterH2OAmt());
		targetRecord.set_starterJuiceAmt(get_starterJuiceAmt());
		targetRecord.set_currBrix(get_currBrix());
		targetRecord.set_currpH(get_currpH());
		targetRecord.set_currTA(get_currTA());
		targetRecord.set_coldLocation(get_coldLocation());
		targetRecord.set_startDate(get_startDate());
		targetRecord.set_endDate(get_endDate());
		targetRecord.set_punchTool(get_punchTool());
		targetRecord.set_containerType(get_containerType());
		targetRecord.set_containerType2(get_containerType2());
		targetRecord.set_containerType3(get_containerType3());
		targetRecord.set_containerCount(get_containerCount());
		targetRecord.set_container2Count(get_container2Count());
		targetRecord.set_container3Count(get_container3Count());
		targetRecord.set_containerVol(get_containerVol());
		targetRecord.set_container2Vol(get_container2Vol());
		targetRecord.set_container3Vol(get_container3Vol());
		targetRecord.set_inputJuiceVol(get_inputJuiceVol());
		targetRecord.set_outputJuiceVol(get_outputJuiceVol());
		targetRecord.set_currentStageJuiceVol(get_currentStageJuiceVol());
		targetRecord.set_inputJuiceScale(get_inputJuiceScale());
		targetRecord.set_outputJuiceScale(get_outputJuiceScale());
		targetRecord.set_currentStageJuiceScale(get_currentStageJuiceScale());
		targetRecord.set_rackSource(get_rackSource());
		targetRecord.set_rackTarget1(get_rackTarget1());
		targetRecord.set_rackTarget2(get_rackTarget2());
		targetRecord.set_rackTarget3(get_rackTarget3());
		targetRecord.set_rackTarget1Count(get_rackTarget1Count());
		targetRecord.set_rackTarget2Count(get_rackTarget2Count());
		targetRecord.set_rackTarget3Count(get_rackTarget3Count());
		targetRecord.set_currentTemp(get_currentTemp());
		targetRecord.set_startTemp(get_startTemp());
		targetRecord.set_endingTemp(get_endingTemp());
		targetRecord.set_pressCycle(get_pressCycle());
		targetRecord.set_stageCycle(get_stageCycle());
		targetRecord.set_stageDuration(get_stageDuration());
		targetRecord.set_yeastActiveLevel(get_yeastActiveLevel());
		targetRecord.set_tempScale(get_tempScale());
		targetRecord.set_fermentNotes(get_fermentNotes());
			
		return targetRecord;
	}
	
	/*
	 * Print readable version of this object
	 * @return User-readable version of the current record
	 */
	@Override
	public String toString()
	{
		String showObject = "";
		
		showObject += String.format("WineMakerFerment record:%nBatch id = '%s'%n", this.get_batchKey());
		
		showObject += String.format("Entry date = '%s' %n", this.get_entry_date());
		showObject += String.format("Stage start date = '%s' %n", this.get_startDate());
		showObject += String.format("Stage end date = '%s' %n", this.get_endDate());
		showObject += String.format("Activity code = '%s' %n", this.get_fermentActivity());
		showObject += String.format("Stage cycle = '%d' %n", this.get_stageCycle());
		showObject += (this.get_inputGrapeAmt() > 0) ?  String.format("Input grape amt = '%d' %n", this.get_inputGrapeAmt()) : "";
		showObject += (this.get_outputMustVolume() > 0) ? String.format("Output must vol = '%d' %s%n", this.get_outputMustVolume(), this.get_outputJuiceScale()) : "";
		showObject += (this.get_yeastStrain().length() > 0) ? String.format("Yeast strain = '%s' %n", this.get_yeastStrain()) : "";
		showObject += (this.get_chemAdded().length() > 0) ? String.format("Chem additive = '%s' %n", this.get_chemAdded()) : "";
		showObject += (this.get_chemAmount() > 0) ? String.format("Chem amt = '%1.2f' %s%n", this.get_chemAmount(), this.get_chemScale()) : "";
		showObject += (this.get_starterYeastAmt() > 0) ? String.format("Yeast amt in starter = '%1.2f' %n", this.get_starterYeastAmt()) : "";
		showObject += (this.get_starterH2OAmt() > 0) ? String.format("H2O amt in starter = '%d' %n", this.get_starterH2OAmt()) : "";
		showObject += (this.get_starterJuiceAmt() > 0) ?  String.format("Juice amt in starter = '%d' %n", this.get_starterJuiceAmt()) : "";
		showObject += (this.get_currBrix() > 0) ? String.format("Current Brix = '%1.2f' %n", this.get_currBrix()) : "";
		showObject += (this.get_currpH() > 0) ? String.format("Current pH = '%1.2f' %n", this.get_currpH()) : "";
		showObject += (this.get_currTA() > 0) ? String.format("Current TA = '%1.2f' %n", this.get_currTA()) : "";
		showObject += (this.get_coldLocation().length() > 0) ? String.format("Cold stabilization location = '%s' %n", this.get_coldLocation()) : "";
		showObject += (this.get_punchTool().length() > 0) ? String.format("Punch tool = '%s' %n", this.get_punchTool()) : "";		
		showObject += (this.get_containerType().length() > 0) ? String.format("1st Container type = '%s' %n", this.get_containerType()) : "";
		showObject += (this.get_containerType2().length() > 0) ? String.format("2nd Container type = '%s' %n", this.get_containerType2()) : "";
		showObject += (this.get_containerType3().length() > 0) ? String.format("3nd Container type = '%s' %n", this.get_containerType3()) : "";
		showObject += (this.get_containerCount() > 0) ? String.format("Container 1 count = '%d' %n", this.get_containerCount()) : "";
		showObject += (this.get_container2Count() > 0) ? String.format("Container 2 count = '%d' %n", this.get_container2Count()) : "";
		showObject += (this.get_container3Count() > 0) ? String.format("Container 3 count = '%d' %n", this.get_container3Count()) : "";
		showObject += (this.get_containerVol() > 0) ? String.format("Container 1 volume = '%d' %n", this.get_containerVol()) : "";
		showObject += (this.get_container2Vol() > 0) ? String.format("Container 2 volume = '%d' %n", this.get_container2Vol()) : "";
		showObject += (this.get_container3Vol() > 0) ? String.format("Container 3 volume = '%d' %n", this.get_container3Vol()) : "";
		showObject += (this.get_inputJuiceVol() > 0) ? String.format("Input juice volume = '%d' %s%n", this.get_inputJuiceVol(), this.get_inputJuiceScale()) : "";
		showObject += (this.get_outputJuiceVol() > 0) ? String.format("Output juice volume = '%d' %s%n", this.get_outputJuiceVol(), this.get_outputJuiceScale()) : "";
		showObject += (this.get_currentStageJuiceVol() > 0) ? String.format("Current stage juice volume = '%d' %s%n", this.get_currentStageJuiceVol(), this.get_currentStageJuiceScale()) : "";
		showObject += (this.get_rackSource().length() > 0) ? String.format("Rack source = '%s' %n", this.get_rackSource()) : "";
		showObject += (this.get_rackTarget1().length() > 0) ? String.format("Rack target 1 = '%s' %n", this.get_rackTarget1()) : "";
		showObject += (this.get_rackTarget2().length() > 0) ? String.format("Rack target 2 = '%s' %n", this.get_rackTarget2()) : "";
		showObject += (this.get_rackTarget3().length() > 0) ? String.format("Rack target 3 = '%s' %n", this.get_rackTarget3()) : "";
		showObject += (this.get_rackTarget1Count() > 0) ? String.format("Rack target 1 count = '%d' %n", this.get_rackTarget1Count()) : "";
		showObject += (this.get_rackTarget2Count() > 0) ? String.format("Rack target 2 count = '%d' %n", this.get_rackTarget2Count()) : "";
		showObject += (this.get_rackTarget3Count() > 0) ? String.format("Rack target 3 count = '%d' %n", this.get_rackTarget3Count()) : "";
		showObject += (this.get_bottleCount() > 0) ? String.format("Bottle Count = '%d' %n", this.get_bottleCount()) : "";
		showObject += (this.get_currentTemp() > 0) ? String.format("Current temp = '%d' %n", this.get_currentTemp()) : "";
		showObject += (this.get_startTemp() > 0) ? String.format("Starting temp = '%d' %n", this.get_startTemp()) : "";
		showObject += (this.get_endingTemp() > 0) ? String.format("Ending temp = '%d' %n", this.get_endingTemp()) : "";
		showObject += (this.get_pressCycle() > 0) ? String.format("Press cycle = '%d' %n", this.get_pressCycle()) : "";
		showObject += (this.get_stageDuration() > 0) ? String.format("Stage duration = '%d' %n", this.get_stageDuration()) : "";
		showObject += (this.get_yeastActiveLevel() > 0) ? String.format("Yeast activity level = '%d' %n", this.get_yeastActiveLevel()) : "";
		showObject += (this.get_tempScale().length() > 0) ? String.format("Temperature scale = '%s' %n", this.get_tempScale()) : "";
		showObject += (this.get_fermentNotes().length() > 0) ? String.format("Stage notes = %n\t'%s' %n", this.get_fermentNotes()) : "";
		
		return showObject;
	}

	public static String toCSVHeader()
	{
		return String.format("Entry Date,Batch,Activity,Stage #,Stage Started,Stage Ended,Stage Duration,Input Grape Amt,Must Vol,Yeast Strain,"
				+ "Chem Name,Chem Amt,Chem Scale,Brix,pH,TA,Temp,Starting Temp,Ending Temp,Temp Scale,Yeast in Starter,H2O in Starter,Juice Vol in Starter,"
				+ "Yeast Activity,Cold Location,1st Container,2nd Container,3rd Container,1st Container Count,2nd Container Count,3rd Container Count,1st Container Vol,2nd Container Vol,3rd Container Vol,"
				+ "Input Juice Vol,Input Scale,Output Juice Vol,Output Scale, Current Juice Vol, Current Vol Scale, Rack Source,Rack Target 1,Rack Target 2,Rack Target 3,Rack Target 1 Count,Rack Target 2 Count,Rack Target 3 Count,Bottle Count,Punch Tool,Press Cycle,Stage Notes\n");
	}
	
	public String toCSV()
	{
		String showObject = "";
		HashMap<String, String> codeSet;
		
		Timestamp ts = this.get_entry_date();
		LocalDateTime entryDate = ts.toLocalDateTime();
		
		showObject += String.format("%s,", entryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		showObject += String.format("%s,", HelperFunctions.batchKeyExpand(this.get_batchKey()));
		
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.ACTIVITYFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_fermentActivity()));
		showObject += String.format("%d,", this.get_stageCycle());

		if (this.get_startDate() != null)
		{
			ts = this.get_startDate();
			entryDate = ts.toLocalDateTime();		
			showObject += String.format("%s,", entryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		}
		else
			showObject += ",";

		if (this.get_endDate() != null)
		{
			ts = this.get_endDate();
			entryDate = ts.toLocalDateTime();		
			showObject += String.format("%s,", entryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		}
		else
			showObject += ",";
		
		showObject += String.format("%d,", this.get_stageDuration());
		showObject += String.format("%d,", this.get_inputGrapeAmt());
		showObject += String.format("%d,", this.get_outputMustVolume());

		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.YEASTFAMILY.getValue());
		showObject += (this.get_yeastStrain().length() > 0) ? String.format("%s,", codeSet.get(this.get_yeastStrain())) : "n/a,";
		
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.ADDITIVEFAMILY.getValue());
		showObject += (this.get_chemAdded().length() > 0) ? String.format("%s,", codeSet.get(this.get_chemAdded())) : "n/a,";
		
		showObject += String.format("%1.2f,", this.get_chemAmount());
		showObject += String.format("%s,", this.get_chemScale());
		showObject += String.format("%1.2f,", this.get_currBrix());
		showObject += String.format("%1.2f,", this.get_currpH());
		showObject += String.format("%1.2f,", this.get_currTA());
		showObject += String.format("%d,", this.get_currentTemp());
		showObject += String.format("%d,", this.get_startTemp());
		showObject += String.format("%d,", this.get_endingTemp());
		showObject += String.format("%s,", this.get_tempScale());
		showObject += String.format("%1.2f,", this.get_starterYeastAmt());
		showObject += String.format("%d,", this.get_starterH2OAmt());
		showObject += String.format("%d,", this.get_starterJuiceAmt());
		showObject += String.format("%d,", this.get_yeastActiveLevel());
		showObject += String.format("%s,", this.get_coldLocation());
		
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.CONTAINERFAMILY.getValue());
		showObject += (this.get_containerType().length() > 0) ? String.format("%s,", codeSet.get(this.get_containerType())) : "n/a,";
		showObject += (this.get_containerType2().length() > 0) ? String.format("%s,", codeSet.get(this.get_containerType2())) : "n/a,";
		showObject += (this.get_containerType3().length() > 0) ? String.format("%s,", codeSet.get(this.get_containerType3())) : "n/a,";
		showObject += String.format("%d,", this.get_containerCount());
		showObject += String.format("%d,", this.get_container2Count());
		showObject += String.format("%d,", this.get_container3Count());
		showObject += String.format("%d,", this.get_containerVol());
		showObject += String.format("%d,", this.get_container2Vol());
		showObject += String.format("%d,", this.get_container3Vol());
		showObject += String.format("%d,", this.get_inputJuiceVol());
		showObject += String.format("%s,", this.get_inputJuiceScale());
		showObject += String.format("%d,", this.get_outputJuiceVol());
		showObject += String.format("%s,", this.get_outputJuiceScale());
		showObject += String.format("%d,", this.get_currentStageJuiceVol());
		showObject += String.format("%s,", this.get_currentStageJuiceScale());
		showObject += (this.get_rackSource().length() > 0) ? String.format("%s,", codeSet.get(this.get_rackSource())) : "n/a,";
		showObject += (this.get_rackTarget1().length() > 0) ? String.format("%s,", codeSet.get(this.get_rackTarget1())) : "n/a,";
		showObject += (this.get_rackTarget2().length() > 0) ? String.format("%s,", codeSet.get(this.get_rackTarget2())) : "n/a,";
		showObject += (this.get_rackTarget3().length() > 0) ? String.format("%s,", codeSet.get(this.get_rackTarget3())) : "n/a,";
		showObject += String.format("%d,", this.get_rackTarget1Count());
		showObject += String.format("%d,", this.get_rackTarget2Count());
		showObject += String.format("%d,", this.get_rackTarget2Count());
		showObject += String.format("%d,", this.get_bottleCount());
		showObject += String.format("%s,", this.get_punchTool());
		showObject += String.format("%d,", this.get_pressCycle());
		showObject += String.format("%s", this.get_fermentNotes().replace(",", " - "));
				
		return showObject;
	}
	
}
