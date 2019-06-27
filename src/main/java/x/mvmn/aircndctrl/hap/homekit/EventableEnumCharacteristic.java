package x.mvmn.aircndctrl.hap.homekit;

import java.util.concurrent.CopyOnWriteArrayList;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.EnumCharacteristic;
import io.github.hapjava.characteristics.EventableCharacteristic;

public abstract class EventableEnumCharacteristic extends EnumCharacteristic implements EventableCharacteristic {

	protected final CopyOnWriteArrayList<HomekitCharacteristicChangeCallback> callbacks = new CopyOnWriteArrayList<>();

	public EventableEnumCharacteristic(String type, boolean isWritable, boolean isReadable, String description, int maxValue) {
		super(type, isWritable, isReadable, description, maxValue);
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
