package x.mvmn.aircndctrl.hap.homekit;

import java.util.concurrent.CopyOnWriteArrayList;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.EventableCharacteristic;
import io.github.hapjava.characteristics.FloatCharacteristic;

public abstract class EventableFloatCharacteristic extends FloatCharacteristic implements EventableCharacteristic {

	protected final CopyOnWriteArrayList<HomekitCharacteristicChangeCallback> callbacks = new CopyOnWriteArrayList<>();

	public EventableFloatCharacteristic(String type, boolean isWritable, boolean isReadable, String description, double minValue, double maxValue,
			double minStep, String unit) {
		super(type, isWritable, isReadable, description, minValue, maxValue, minStep, unit);
	}

	@Override
	public void subscribe(HomekitCharacteristicChangeCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void unsubscribe() {
		callbacks.clear();
	}

	protected void notifySubscribers() {
		callbacks.forEach(c -> {
			try {
				c.changed();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
}
