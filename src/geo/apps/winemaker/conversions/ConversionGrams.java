package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionGrams implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		switch (targetType)
		{
		case MASSKG:
			return convertFromValue * 0.001;
		case MASSMG:
			return convertFromValue * 1000;
		case MASSLBS:
			return convertFromValue * .0022;
		case MASSOZ:
			return convertFromValue * .03527;			
		default:
			return 0.0;	
		}
	}
}
