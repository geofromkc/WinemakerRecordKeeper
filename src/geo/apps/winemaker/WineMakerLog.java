package geo.apps.winemaker;

/**
 * Primary object describing a batch.  A batch could be a single grape, or a blend of multiple grapes.
 * The primary key is "_batchKey".   Its value is constructed from a fixed-length date portion and
 * 		a 1-8 character variable-length grape code: such as "202109cabfranc", that equals "Sept 2021 Cab Franc".
 * If it's a blend, the "_batchBlendKey" will have a generic key as in: "202109Blend1", that equals "Sept 2021 Blend #1". 
 * This means that at least two records will have matching _batchBlendKey values, tying them together in a common blend batch.
 * For example:
 * 		Record 1: _batchKey = "202109mrlot", _batchBlendKey = "202109Blend1"
 * 		Record 2: _batchKey = "202109sangio", _batchBlendKey = "202109Blend1"
 *  
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;

import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class WineMakerLog {

	private final String empty = "";
	
	private String _batchKey = empty;
	private String _batchBlendKey = empty;
	private int _batchBlendSequence = 0;
	private String _batchSource = empty;
	private String _batchGrape = empty;
	private String _batchVineyard = empty;
	private int _sourceItemCount = 0;
	private int _sourceItemMeasure = 0;
	private double _sourceItemPrice = 0;
	private String _sourceScale = empty;
	private int _qualityRating = 0;
	private int _wastePercent = 0;
	private String _sourceVendor = empty;
	private String _sourceVendorNotes = empty;
	private int _bottleCount = 0;
	private int _blendRatio = 0;
	
	private ArrayList<WineMakerTesting> wmkTests = new ArrayList<WineMakerTesting>();
	private ArrayList<WineMakerFerment> wmkFerments = new ArrayList<WineMakerFerment>();
	
	private WineMakerModel winemakerModel = null;
	
	/*
	 * Constructor + getters and setters
	 */
	public WineMakerLog(WineMakerModel winemakerModel) 
	{
		this.winemakerModel = winemakerModel;
	}

	public String get_batchKey() {
		return _batchKey;
	}

	public void set_batchKey(String _batchKey) {
		this._batchKey = _batchKey;
	}

	public String get_batchBlendKey() {
		return _batchBlendKey;
	}

	public void set_batchBlendKey(String _batchBlendKey) {
		this._batchBlendKey = _batchBlendKey;
	}

	public int get_batchBlendSequence() {
		return _batchBlendSequence;
	}

	public void set_batchBlendSequence(int _batchBlendSequence) {
		this._batchBlendSequence = _batchBlendSequence;
	}

	public String get_batchSource() {
		return _batchSource;
	}

	public void set_batchSource(String _batchSource) {
		this._batchSource = _batchSource;
	}

	public String get_batchGrape() {
		return _batchGrape;
	}

	public void set_batchGrape(String _batchGrape) {
		this._batchGrape = _batchGrape;
	}

	public int get_qualityRating() {
		return _qualityRating;
	}

	public void set_qualityRating(int _qualityRating) {
		this._qualityRating = _qualityRating;
	}

	public int get_wastePercent() {
		return _wastePercent;
	}

	public void set_wastePercent(int _wastePercent) {
		this._wastePercent = _wastePercent;
	}

	public int get_sourceItemCount() {
		return _sourceItemCount;
	}

	public void set_sourceItemCount(int _sourceItemCount) {
		this._sourceItemCount = _sourceItemCount;
	}

	public String get_batchVineyard() {
		return _batchVineyard;
	}

	public void set_batchVineyard(String _batchVineyard) {
		this._batchVineyard = _batchVineyard;
	}

	public int get_sourceItemMeasure() {
		return _sourceItemMeasure;
	}

	public void set_sourceItemMeasure(int _sourceItemMeasure) {
		this._sourceItemMeasure = _sourceItemMeasure;
	}

	public double get_sourceItemPrice() {
		return _sourceItemPrice;
	}

	public void set_sourceItemPrice(double _sourceItemPrice) {
		this._sourceItemPrice = _sourceItemPrice;
	}

	public String get_sourceScale() {
		return _sourceScale;
	}

	public void set_sourceScale(String _sourceScale) {
		this._sourceScale = _sourceScale;
	}

	public String get_sourceVendor() {
		return _sourceVendor;
	}

	public void set_sourceVendor(String _sourceVendor) {
		this._sourceVendor = _sourceVendor;
	}

	public String get_sourceVendorNotes() {
		return _sourceVendorNotes;
	}

	public void set_sourceVendorNotes(String _sourceVendorNotes) {
		this._sourceVendorNotes = _sourceVendorNotes;
	}

	public int get_bottleCount() {
		return _bottleCount;
	}

	public void set_bottleCount(int _bottleCount) {
		this._bottleCount = _bottleCount;
	}

	public int get_blendRatio() {
		return _blendRatio;
	}

	public void set_blendRatio(int _blendRatio) {
		this._blendRatio = _blendRatio;
	}
	
	public void setWmkFerments(ArrayList<WineMakerFerment> wmfSets)
	{
		this.wmkFerments = wmfSets;
	}
	
	public ArrayList<WineMakerFerment> getWmkFerments()
	{
		return wmkFerments;
	}

	public void setWmkTests(ArrayList<WineMakerTesting> wmtSets)
	{
		this.wmkTests = wmtSets;
	}
	
	public ArrayList<WineMakerTesting> getWmkTests()
	{
		return wmkTests;
	}

	public WineMakerLog newCopy()
	{
		WineMakerLog targetRecord = new WineMakerLog(winemakerModel);
		
		targetRecord.set_batchKey(get_batchKey());
		targetRecord.set_batchGrape(get_batchGrape());
		targetRecord.set_batchBlendKey(get_batchBlendKey());
		targetRecord.set_batchSource(get_batchSource());
		targetRecord.set_batchVineyard(get_batchVineyard());
		targetRecord.set_sourceItemCount(get_sourceItemCount());
		targetRecord.set_sourceItemMeasure(get_sourceItemMeasure());
		targetRecord.set_sourceItemPrice(get_sourceItemPrice());
		targetRecord.set_sourceScale(get_sourceScale());
		targetRecord.set_qualityRating(get_qualityRating());
		targetRecord.set_wastePercent(get_wastePercent());
		targetRecord.set_sourceVendor(get_sourceVendor());
		targetRecord.set_sourceVendorNotes(get_sourceVendorNotes());
		
		wmkFerments = winemakerModel.queryFermentData(get_batchKey());
		wmkTests = winemakerModel.queryTestingData(get_batchKey());
	
		targetRecord.setWmkFerments(wmkFerments);
		targetRecord.setWmkTests(wmkTests);
		return targetRecord;
	}

	/*
	 * Create a copy of the provided object
	 * Also reassign batch key for any ferment and testing records.
	 */
	public WineMakerLog newCopy(WineMakerLog sourceRecord, String batchDate, String parentKey)
	{
		WineMakerLog targetRecord = new WineMakerLog(winemakerModel);
	
		targetRecord.set_batchKey(batchDate + sourceRecord.get_batchGrape());
		targetRecord.set_batchGrape(sourceRecord.get_batchGrape());
		targetRecord.set_batchBlendKey(parentKey);
		targetRecord.set_batchSource(sourceRecord.get_batchSource());
		targetRecord.set_batchVineyard(sourceRecord.get_batchVineyard());
		targetRecord.set_sourceItemCount(sourceRecord.get_sourceItemCount());
		targetRecord.set_sourceItemMeasure(sourceRecord.get_sourceItemMeasure());
		targetRecord.set_sourceItemPrice(sourceRecord.get_sourceItemPrice());
		targetRecord.set_sourceScale(sourceRecord.get_sourceScale());
		targetRecord.set_qualityRating(sourceRecord.get_qualityRating());
		targetRecord.set_wastePercent(sourceRecord.get_wastePercent());
		targetRecord.set_sourceVendor(sourceRecord.get_sourceVendor());
		targetRecord.set_sourceVendorNotes(sourceRecord.get_sourceVendorNotes());
		
		wmkFerments = winemakerModel.queryFermentData(sourceRecord.get_batchKey());		
		wmkFerments
			.stream()
			.forEach(wmf -> wmf.set_batchKey(parentKey));

		wmkTests = winemakerModel.queryTestingData(sourceRecord.get_batchKey());
		wmkTests
			.stream()
			.forEach(wmt -> wmt.set_batchKey(parentKey));

		targetRecord.setWmkFerments(wmkFerments);
		targetRecord.setWmkTests(wmkTests);
		return targetRecord;
	} // end of newCopy(WineMakerLog sourceRecord, String batchDate, String parentKey)

	/*
	 * Create a copy of the provided object
	 * Also reassign batch key for any ferment and testing records.
	 */
	public WineMakerLog newCopy(String batchDate, String parentKey)
	{
		WineMakerLog targetRecord = new WineMakerLog(winemakerModel);
		
		targetRecord.set_batchKey(batchDate + get_batchGrape());
		targetRecord.set_batchGrape(get_batchGrape());
		targetRecord.set_batchBlendKey(parentKey);
		targetRecord.set_batchSource(get_batchSource());
		targetRecord.set_batchVineyard(get_batchVineyard());
		targetRecord.set_sourceItemCount(get_sourceItemCount());
		targetRecord.set_sourceItemMeasure(get_sourceItemMeasure());
		targetRecord.set_sourceItemPrice(get_sourceItemPrice());
		targetRecord.set_sourceScale(get_sourceScale());
		targetRecord.set_qualityRating(get_qualityRating());
		targetRecord.set_wastePercent(get_wastePercent());
		targetRecord.set_sourceVendor(get_sourceVendor());
		targetRecord.set_sourceVendorNotes(get_sourceVendorNotes());
		
		wmkFerments = winemakerModel.queryFermentData(get_batchKey());
		wmkFerments
			.stream()
			.forEach(wmf -> wmf.set_batchKey(parentKey));

		wmkTests = winemakerModel.queryTestingData(get_batchKey());
		wmkTests
			.stream()
			.forEach(wmt -> wmt.set_batchKey(parentKey));
		
		targetRecord.setWmkFerments(wmkFerments);
		targetRecord.setWmkTests(wmkTests);
		return targetRecord;
	}

	/*
	 * Print readable version of this object
	 */
	@Override
	public String toString()
	{
		String showObject = "";
		
		showObject += String.format("%nWineMakerLog record:%n", this.get_batchKey());
		showObject += String.format("Key = '%s' %n", this.get_batchKey());
		showObject += (this.get_batchBlendKey().length() > 0) ? String.format("Blend Key = '%s' %n", this.get_batchBlendKey()) : "";
		showObject += String.format("Source = '%s' %n", this.get_batchSource());
		showObject += String.format("Grape = '%s' %n", this.get_batchGrape());
		showObject += String.format("Vineyard = '%s' %n", this.get_batchVineyard());
		showObject += String.format("Item Count = '%s' %n", this.get_sourceItemCount());
		showObject += String.format("Measure System = '%s' %n", this.get_sourceItemMeasure());
		showObject += String.format("Item Price = '%s' %n", this.get_sourceItemPrice());
		showObject += String.format("Source Scale = '%s' %n", this.get_sourceScale());
		showObject += String.format("Vendor = '%s' %n", this.get_sourceVendor());		
		showObject += String.format("Batch Notes = '%s' %n", this.get_sourceVendorNotes());
		
		return showObject;
	}
	
	public static String toCSVHeader()
	{
		return "Batch Key,BlendKey,Source,Grape,Vineyard,Item Count,Item Price,Units/Item,Scale,Quality,Waste,Vendor,Notes\n";
	}
	
	public String toCSV()
	{
		String showObject = "";
		
//		HashMap<String, HashMap<String, String>> codeMapping = WineMakerModel.codeFamilies;
		HashMap<String, String> codeSet;
		
		showObject += String.format("%s,", HelperFunctions.batchKeyExpand(this.get_batchKey()));
		showObject += (this.get_batchBlendKey().length() > 0) ? 
				String.format("%s,", HelperFunctions.batchKeyExpand(this.get_batchBlendKey())) : ",";
		
//		codeSet = codeMapping.get(FamilyCode.BATCHSOURCEFAMILY.getValue());
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.BATCHSOURCEFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_batchSource()));

//		String grapeName = codeMapping.get(FamilyCode.GRAPEFAMILY.getValue()).get(this.get_batchGrape());
		String grapeName = HelperFunctions.getCodeKeyEntry(FamilyCode.GRAPEFAMILY.getValue(), this.get_batchGrape());
		if (grapeName == null)
			grapeName = HelperFunctions.getCodeKeyEntry(FamilyCode.BLENDFAMILY.getValue(), this.get_batchGrape());
//			grapeName = codeMapping.get(FamilyCode.BLENDFAMILY.getValue()).get(this.get_batchGrape());		
		showObject += String.format("%s,", grapeName);

//		codeSet = codeMapping.get(FamilyCode.VINEYARDFAMILY.getValue());
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.VINEYARDFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_batchVineyard()));
		showObject += String.format("%s,", this.get_sourceItemCount());
		showObject += String.format("%s,", this.get_sourceItemPrice());
		showObject += String.format("%s,", this.get_sourceItemMeasure());
		
//		codeSet = codeMapping.get(FamilyCode.MEASURESFAMILY.getValue());
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.MEASURESFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_sourceScale()));
		showObject += String.format("%s,", this.get_qualityRating());
		showObject += String.format("%s,", this.get_wastePercent());

//		codeSet = codeMapping.get(FamilyCode.GRAPESUPPLYFAMILY.getValue());
		codeSet = HelperFunctions.getCodeKeyFamily(FamilyCode.GRAPESUPPLYFAMILY.getValue());
		showObject += String.format("%s,", codeSet.get(this.get_sourceVendor()));		
		showObject += String.format("%s", this.get_sourceVendorNotes().replaceAll(",", ";"));
		
		return showObject;
		
	}
}
