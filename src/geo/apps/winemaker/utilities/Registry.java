package geo.apps.winemaker.utilities;

import java.util.HashMap;
import java.util.Map;
import geo.apps.winemaker.utilities.Constants.RegistryKeys;

public class Registry {
    private static final Registry INSTANCE = new Registry();
    
    private Map<RegistryKeys, Object> registryMap;

    private Registry() {
        registryMap = new HashMap<>();
    }

    public static Registry getInstance() {
        return INSTANCE;
    }

    public void register(RegistryKeys key, Object obj) {
        registryMap.put(key, obj);
    }

    public Object get(RegistryKeys key) {
        return registryMap.get(key);
    }
}
