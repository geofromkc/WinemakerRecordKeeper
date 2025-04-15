package geo.apps.winemaker.utilities;

public class Constants {
	
	public enum RegistryKeys {
		LOGGER,
		MODEL,
		DBOPS
	}
	
	public enum Blend {
		NOTBLEND,
		FIELDBLEND,
		JUICEBLEND
	}
	
	public enum Validation {
		VALIDATEBLENDCHILD,
		VALIDATEBATCHPARENT,
		PASSED,
		FAILED
	}
	
	public enum Conversions {
		CONVERTGALSLTRS,
		CONVERTLTRSGALS,
		CONVERTLBSKGS,
		CONVERTKGSLBS,
		CONVERTOZSGMS,
		CONVERTGMSOZS,
		CONVERTOZSMLS,
		CONVERTMLSOZS	
	}
	
	public enum SQLSearch {
		PARENTBATCH,
		BLENDCOMPONENT
	}
	
	public enum TimeCheck {
		ONLYENTRY,
		CHKSTARTEND
	}
	
	public enum BatchScene {
		SINGLE,
		BLEND,
		CREATE,
		UPDATE
	}
	
	public enum InventoryScene {
		UPDATE,
		PURCHASE
	}
	
	public enum MethodCodes {
		VALIDATE,
		UISETUP,
		RECORDBUILD
	}
	
	public enum FamilyCode {
		BATCHSOURCEFAMILY("source"),
		GRAPEFAMILY("grape"),
		BLENDFAMILY("blend"),
		ACTIVITYFAMILY("fermentact"),
		ADDITIVEFAMILY("fermentadd"),
		CONTAINERFAMILY("fermentcon"),
		YEASTFAMILY("fermentyst"),
		LABFAMILY("labsupplies"),
		MEASURESFAMILY("measuresys"),
		REMOVALFAMILY("remaction"),
		GRAPESUPPLYFAMILY("vendor"),
		SUPPLIESFAMILY("supplyVendor"),
		LABTESTFAMILY("test"),
		USERFAMILIES("code"),
		VINEYARDFAMILY("vineyard");

	    private final String value;

	    FamilyCode(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
	public enum BatchSource {
		GRAPESOURCE("gp"),
		JUICESOURCE("jc");

	    private final String value;

	    BatchSource(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
	public enum BlendType {
		BLENDFIELD("field"),
		BLENDBATCH("juice");

	    private final String value;

	    BlendType(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}

	public enum WeightsAndMeasures {
		USWEIGHT("lbs"),
		USVOLUME("gal"),
		USTEMP("fahren"),
		METRICWEIGHT("kg"),
		METRICVOLUME("l"),
		METRICTEMP("centgrde");

		private final String value;

	    WeightsAndMeasures(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
	public enum MassAndVolume {
		MASSKG("KG"),
		MASSG("G"),
		MASSMG("MG"),
		MASSLBS("LBS"),
		MASSOZ("OZ"),
		VOLUMEL("L"),
		VOLUMEML("ML"),
		VOLUMEHL("HL"),
		VOLUMEGAL("GAL");
		
		private final String value;
		
		MassAndVolume(final String newValue) {
			value = newValue;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public enum ActivityName {
		AMELIORATION("amel"),
		BOTTLE("bottle"),
		CHECKPOINT("check"),
		CRUSH("crush"),
		PRESS("press"),
		RACK("rack"),
		TRANSFER("txfr"),
		FERMENT("ferment"),
		INVENTORY("invActivity"),
		YEASTPITCH("yeastadd"),
		INVENTORYBUY("purch"),
		INVENTORYSELL("sold");

	    private final String value;

	    ActivityName(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
	public enum YeastPitchDefaults {
		GOFERM("goferm"),
		FERMAIDK("fermk"),
		FERMAIDO("fermo"),
		FTROUGE("ftrouge"),
		OPTIRED("optired");

	    private final String value;

	    YeastPitchDefaults(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
	public enum DatabaseTables {
		PRIMARY("WMK_KEY"),
		CODES("WMK_CODES"),
		TESTS("WMK_TESTING"),
		ACTIVITY("WMK_FERMENT"),
		INVENTORY("WMK_INVENTORY");

	    private final String value;

	    DatabaseTables(final String newValue) {
	        value = newValue;
	    }

	    public String getValue() { return value; }
	}
	
}
