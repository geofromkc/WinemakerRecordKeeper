package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionOunces implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		
		switch (targetType)
		{
		case MASSKG:
			return convertFromValue * 0.02835;
		case MASSMG:
			return convertFromValue * 28350;
		case MASSG:
			return convertFromValue * 28.35;
		case MASSLBS:
			return convertFromValue * 0.0625;			
		default:
			return 0.0;	
		}
	}
}
