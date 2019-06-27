package x.mvmn.aircndctrl.hap.homekit;

import java.util.Arrays;
import java.util.List;

import io.github.hapjava.Service;
import io.github.hapjava.characteristics.Characteristic;

public class HAPAccessoryService implements Service {

	protected final List<Characteristic> characteristics;
	protected final HAPAccessoryServiceType type;

	public HAPAccessoryService(HAPAccessoryServiceType type, Characteristic... characteristics) {
		this.type = type;
		this.characteristics = Arrays.asList(characteristics);
	}

	@Override
	public List<Characteristic> getCharacteristics() {
		return characteristics;
	}

	@Override
	public String getType() {
		return type.getCode();
	}
}