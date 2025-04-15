package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionMilliGrams implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {

		switch (targetType)
		{
		case MASSKG:
			return convertFromValue * 1.00E-06;
		case MASSG:
			return convertFromValue * .001;
		case MASSLBS:
			return convertFromValue * 2.2046E-06;
		case MASSOZ:
			return convertFromValue * 3.5274E-05;			
		default:
			return 0.0;	
		}
	}
}
