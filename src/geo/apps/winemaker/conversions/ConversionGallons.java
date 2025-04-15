package geo.apps.winemaker.conversions;

import geo.apps.winemaker.utilities.Constants.MassAndVolume;

public class ConversionGallons implements ConversionTemplate {

	@Override
	public Double apply(MassAndVolume targetType, Double convertFromValue) {
		switch (targetType)
		{
		case VOLUMEL:
			return convertFromValue * 3.7854;
		case VOLUMEHL:
			return convertFromValue * 0.03785;
		case VOLUMEML:
			return convertFromValue * 3785.4;
		default:
			return 0.0;	
		}
	}
}
