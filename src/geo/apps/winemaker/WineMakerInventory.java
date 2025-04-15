package geo.apps.winemaker;

import java.math.BigDecimal;
/**
 * 
 */
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import geo.apps.winemaker.utilities.Constants.ActivityName;
import geo.apps.winemaker.utilities.Constants.FamilyCode;
import geo.apps.winemaker.utilities.HelperFunctions;

public class WineMakerInventory {
	
	HashMap<String, HashMap<String, String>> codeMapping = HelperFunctions.getCodeKeyMappings();
	HashMap<String, String> itemVendorSet = new HashMap<>();
	HashMap<String, String> activitySet = new HashMap<>();
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	private final String empty = "";
	
	private String itemName = empty;
	private String itemId = empty;
	private Timestamp itemTaskTime = null;
	private double itemStockOnHand = 0;
	private String itemBatchId = empty;
	private String itemTaskId = empty;
	private double itemActivityAmount = 0;
	private double itemPurchaseCost = 0;
	private String itemAmountScale = empty;
	private String itemPurchaseVendor = empty;
	
	private String exportActivity = empty;
	private String exportVendor = empty;
	
	public WineMakerInventory() {
	}

	public String get_itemName() {
		return itemName;
	}

	public void set_itemName(String itemName) {
		this.itemName = itemName;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemBatchId() {
		return itemBatchId;
	}

	public void setItemBatchId(String itemBatchId) {
		this.itemBatchId = itemBatchId;
	}

	public String getItemTaskId() {
		return itemTaskId;
	}

	public void setItemTaskId(String itemTaskId) {
		this.itemTaskId = itemTaskId;
	}

	public Timestamp getItemTaskTime() {
		return itemTaskTime;
	}

	public void setItemTaskTime(Timestamp itemTaskTime) {
		this.itemTaskTime = itemTaskTime;
	}

	public double get_itemStockOnHand() {
		return itemStockOnHand;
	}

	public void set_itemStockOnHand(double itemStockOnHand) {
		this.itemStockOnHand = itemStockOnHand;
	}

	public double get_itemActivityAmount() {
		return itemActivityAmount;
	}

	public void set_itemActivityAmount(double itemActivityAmount) {
		this.itemActivityAmount = itemActivityAmount;
	}
	
	public double get_itemPurchaseCost() {
		return itemPurchaseCost;
	}

	public void set_itemPurchaseCost(double itemPurchaseCost) {
		this.itemPurchaseCost = itemPurchaseCost;
	}

	public String get_itemAmountScale() {
		return itemAmountScale;
	}

	public void set_itemAmountScale(String itemAmountScale) {
		this.itemAmountScale = itemAmountScale;
	}

	public String get_itemPurchaseVendor() {
		return itemPurchaseVendor;
	}

	public void set_itemPurchaseVendor(String itemPurchaseVendor) {
		this.itemPurchaseVendor = itemPurchaseVendor;
	}
	
	@Override
	public String toString()
	{
		String showObject = "";
		HashMap<String, String> itemNameSet = new HashMap<>(200);
		ArrayList<String> loadingCodes = new ArrayList<>(4);
		
		loadingCodes.add(FamilyCode.LABFAMILY.getValue());
		loadingCodes.add(FamilyCode.CONTAINERFAMILY.getValue());
		loadingCodes.add(FamilyCode.ADDITIVEFAMILY.getValue());
		loadingCodes.add(FamilyCode.YEASTFAMILY.getValue());
		
		loadingCodes
			.stream()
			.forEach(loadCode -> itemNameSet.putAll(codeMapping.get(loadCode)));
		
		itemVendorSet = codeMapping.get(FamilyCode.SUPPLIESFAMILY.getValue());
		activitySet.putAll(codeMapping.get(FamilyCode.REMOVALFAMILY.getValue()));
		activitySet.putAll(codeMapping.get(ActivityName.INVENTORY.getValue()));
		activitySet.putAll(codeMapping.get(FamilyCode.ACTIVITYFAMILY.getValue()));
		
		showObject += String.format("%nWineMakerInventory record:%n");
		showObject += String.format("Item Name = '%s' (%s)%n", this.get_itemName(), itemNameSet.get(this.get_itemName()));
		showObject += (this.getItemId().equals(empty)) ? "" : String.format("Item ID = %s%n", this.getItemId());
		
		showObject += String.format("Current Stock on Hand = %s %s%n", formatDouble(this.get_itemStockOnHand()), this.itemAmountScale);
		showObject += (this.getItemTaskTime() != null) ? String.format("Record Date = %s%n", this.getItemTaskTime().toLocalDateTime().format(dateFormatter)) : "<no task time>";
		showObject += (this.getItemBatchId().length() > 0) ? String.format("Current batch = '%s'%n", this.getItemBatchId()) : "Current batch = <no batch id>\n";
		
		if (this.getItemTaskId().equals(ActivityName.INVENTORYBUY.getValue()))
		{
			showObject += String.format("Amount Purchased = %s%n", formatDouble(this.get_itemActivityAmount()));
			showObject += String.format("Purchase Cost = $%1.2f%n",  this.get_itemPurchaseCost());
			showObject += String.format("Purchase Vendor = %s (%s)%n", this.get_itemPurchaseVendor(), itemVendorSet.get(this.get_itemPurchaseVendor()));
		}
		
		if (this.getItemTaskId() != null && this.getItemTaskId().length() > 0)
		{
			String typeLabel = (this.getItemTaskId().equals(ActivityName.INVENTORYSELL.getValue()) || this.getItemTaskId().equals(ActivityName.INVENTORYBUY.getValue())) ?
					"Item Count " : "Amount Used";
			showObject += String.format("Activity = %s (%s)%n", this.getItemTaskId(), activitySet.get(this.getItemTaskId()));
			showObject += (this.get_itemActivityAmount() > 0) ? String.format("%s = %s%n", typeLabel, formatDouble(this.get_itemActivityAmount())) : "";
		}
				
		return showObject;
	}
	
	public static String toCSVHeader()
	{
		return "Item Name,Item ID,Entry Date,Stock on Hand,Batch Id,Activity Name,Activity Amount,Purchase Cost,Amount Scale,Purchase Vendor";
	}

	/**
	 * 
	 * @return
	 */
	public String toCSV()
	{
		String showObject = "";
		
		HashMap<String, String> itemNameSet = new HashMap<>(200);
		ArrayList<String> loadingCodes = new ArrayList<>(3);
	
		loadingCodes.add(FamilyCode.CONTAINERFAMILY.getValue());
		loadingCodes.add(FamilyCode.ADDITIVEFAMILY.getValue());
		loadingCodes.add(FamilyCode.YEASTFAMILY.getValue());
		
		loadingCodes
			.stream()
			.forEach(loadCode -> itemNameSet.putAll(codeMapping.get(loadCode)));
		
		itemVendorSet = codeMapping.get(FamilyCode.SUPPLIESFAMILY.getValue());
		activitySet = codeMapping.get(FamilyCode.REMOVALFAMILY.getValue());
		activitySet.putAll(codeMapping.get(FamilyCode.ACTIVITYFAMILY.getValue()));		
		activitySet.putAll(codeMapping.get(ActivityName.INVENTORY.getValue()));

		exportVendor = (itemVendorSet.get(this.get_itemPurchaseVendor()) != null) ? 
				this.get_itemPurchaseVendor() : "";
		exportActivity = (activitySet.get(this.getItemTaskId()) != null) ? 
				this.getItemTaskId() : "";

		String itemId = (!this.getItemId().equals(empty)) ? 
				this.getItemId() : "";
	
		showObject = String.format("%s,", this.get_itemName());
		showObject += String.format("%s,", itemId);
		showObject += String.format("%s,", this.getItemTaskTime().toLocalDateTime().format(dateFormatter));
		showObject += String.format("%s,", formatDouble(this.get_itemStockOnHand()));
		showObject += String.format("%s,", this.getItemBatchId());
		showObject += String.format("%s,", exportActivity);
		showObject += String.format("%s,", formatDouble(this.get_itemActivityAmount()));
		showObject += String.format("%s,", formatDouble(this.get_itemPurchaseCost()));
		showObject += String.format("%s,", this.get_itemAmountScale());
		showObject += String.format("%s,", exportVendor);
	
		return showObject;
	}

	public String toReport()
	{
		String showObject = "";

		HashMap<String, String> itemNameSet = new HashMap<>(200);
		ArrayList<String> loadingCodes = new ArrayList<>(4);
		
		loadingCodes.add(FamilyCode.LABFAMILY.getValue());
		loadingCodes.add(FamilyCode.CONTAINERFAMILY.getValue());
		loadingCodes.add(FamilyCode.ADDITIVEFAMILY.getValue());
		loadingCodes.add(FamilyCode.YEASTFAMILY.getValue());
		
		loadingCodes
			.stream()
			.forEach(loadCode -> itemNameSet.putAll(codeMapping.get(loadCode)));
		
		itemVendorSet = codeMapping.get(FamilyCode.SUPPLIESFAMILY.getValue());
		activitySet.putAll(codeMapping.get(FamilyCode.REMOVALFAMILY.getValue()));
		activitySet.putAll(codeMapping.get(ActivityName.INVENTORY.getValue()));
		activitySet.putAll(codeMapping.get(FamilyCode.ACTIVITYFAMILY.getValue()));

		if (this.getItemTaskId().length() == 0 || this.getItemTaskId().equals(ActivityName.INVENTORYBUY.getValue()))
		{
			String showID = (this.getItemId().length() > 0) ?
					String.format(" (%s)", this.getItemId()) : "";
			if (this.getItemTaskId().length() == 0)
			{
				showObject += String.format("%s%s, stock on hand = %s%s", itemNameSet.get(this.get_itemName()), showID, formatDouble(this.get_itemStockOnHand()), this.itemAmountScale);			
				showObject += (this.getItemBatchId().length() > 0) ? String.format(", current batch = '%s'", HelperFunctions.batchKeyExpand(this.getItemBatchId())) : "";
			}
			
			if (this.getItemTaskId().equals(ActivityName.INVENTORYBUY.getValue()))
			{
				showObject += String.format("%s%s, purchased %s%s for $%1.2f from %s", itemNameSet.get(this.get_itemName()), showID, formatDouble(this.get_itemActivityAmount()), this.itemAmountScale, this.get_itemPurchaseCost(), itemVendorSet.get(this.get_itemPurchaseVendor()));
			}			
		}
		
		return showObject;		
	}
	
	public WineMakerInventory createActivityRecord()
	{
		WineMakerInventory wmiActivity = new WineMakerInventory();
		wmiActivity.set_itemName(this.get_itemName());
		wmiActivity.setItemId(this.getItemId());
		wmiActivity.setItemBatchId(this.getItemBatchId());
		
		return wmiActivity;
	}
	
	private String formatDouble(Double someNumber)
	{
		String showDouble = "";		
		BigDecimal testDecimal = new BigDecimal(String.valueOf(someNumber));
		
		try
		{
			showDouble = Integer.toString(testDecimal.intValueExact());
		}
		catch (ArithmeticException  e)
		{
			showDouble = testDecimal.toPlainString();
		}
						
		return showDouble;
	}
}
