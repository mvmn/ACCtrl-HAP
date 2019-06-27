package x.mvmn.aircndctrl.hap.homekit;

import java.util.concurrent.CopyOnWriteArrayList;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.EventableCharacteristic;
import io.github.hapjava.characteristics.IntegerCharacteristic;

public abstract class EventableIntegerCharacteristic extends IntegerCharacteristic implements EventableCharacteristic {

	public EventableIntegerCharacteristic(String type, boolean isWritable, boolean isReadable, String description, int minValue, int maxValue, String unit) {
		super(type, isWritable, isReadable, description, minValue, maxValue, unit);
	}

	protected final CopyOnWriteArrayList<HomekitCharacteristicChangeCallback> callbacks = new CopyOnWriteArrayList<>();

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
