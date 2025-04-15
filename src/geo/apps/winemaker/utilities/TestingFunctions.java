package geo.apps.winemaker.utilities;

import java.util.function.Function;

import geo.apps.winemaker.utilities.Constants.Conversions;

public class TestingFunctions {

	public static void main()
	{

	}
	
	public static Double valueConversion(Double convertInput, Conversions whichConversion)
	{
		if (whichConversion.equals(Conversions.CONVERTLBSKGS))
		{
			return convertLBS2KGS.apply(convertInput);
		}

		if (whichConversion.equals(Conversions.CONVERTKGSLBS))
		{
			return convertKGS2LBS.apply(convertInput);
		}

		return 0.0;
	}
	
	public static Function<Double, Double> convertLBS2KGS = toConvert -> toConvert / 2.2046226218;
	public static Function<Double, Double> convertKGS2LBS = toConvert -> toConvert * 2.2046226218;
	public static Function<Double, Double> convertOZS2MLS = toConvert -> toConvert / 0.033814;
	public static Function<Double, Double> convertMLS2OZS = toConvert -> toConvert * 0.033814;
	
}
