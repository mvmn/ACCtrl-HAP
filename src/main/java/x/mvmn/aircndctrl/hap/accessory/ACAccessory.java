package x.mvmn.aircndctrl.hap.accessory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.hapjava.HomekitAccessory;
import io.github.hapjava.Service;
import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.impl.ExceptionalConsumer;
import io.github.hapjava.impl.characteristics.common.Name;
import io.github.hapjava.impl.characteristics.common.PowerStateCharacteristic;
import x.mvmn.aircndctrl.hap.homekit.EventableEnumCharacteristic;
import x.mvmn.aircndctrl.hap.homekit.EventableFloatCharacteristic;
import x.mvmn.aircndctrl.hap.homekit.EventableIntegerCharacteristic;
import x.mvmn.aircndctrl.hap.homekit.HAPAccessoryCharacteristicType;
import x.mvmn.aircndctrl.hap.homekit.HAPAccessoryService;
import x.mvmn.aircndctrl.hap.homekit.HAPAccessoryServiceType;
import x.mvmn.aircndctrl.model.addr.ACAddress;
import x.mvmn.aircndctrl.model.addr.ACBinding;
import x.mvmn.aircndctrl.service.ACControlService;
import x.mvmn.aircndctrl.util.LangUtil;

public class ACAccessory implements HomekitAccessory {

	/*
	 * CurrentHeatingCoolingState (int [0,1,2]): rn TargetHeatingCoolingState (int [0,1,2,3]): rwn CurrentTemperature (float): rn TargetTemperature (float): rwn
	 * TemperatureDisplayUnits (int [0,1,2]): rwn
	 * 
	 * Characteristic.TargetHeatingCoolingState.OFF = 0; Characteristic.TargetHeatingCoolingState.HEAT = 1; Characteristic.TargetHeatingCoolingState.COOL = 2;
	 * Characteristic.TargetHeatingCoolingState.AUTO = 3;
	 * 
	 * Characteristic.CurrentHeatingCoolingState.OFF = 0; Characteristic.CurrentHeatingCoolingState.HEAT = 1; Characteristic.CurrentHeatingCoolingState.COOL =
	 * 2;
	 * 
	 */

	protected int id;
	protected String label;
	protected String serialNumber;
	protected String model;
	protected String manufacturer;
	private List<Service> services;

	public ACAccessory(int id, String label, String serialNumber, String model, String manufacturer, ACAddress addr, ACControlService controlService) {
		this.id = id;
		this.label = label;
		this.serialNumber = serialNumber;
		this.model = model;
		this.manufacturer = manufacturer;

		Characteristic curTemp = new EventableFloatCharacteristic(HAPAccessoryCharacteristicType.CURRENT_TEMPERATURE.getCode(), false, true, "Temp", 0, 100,
				1.0d, "celsius") {
			@Override
			protected void setValue(Double value) throws Exception {
				controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)),
						LangUtil.mapBuilder("SetTem", (Object) value.intValue()).build());
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Double> getDoubleValue() {
				return CompletableFuture.supplyAsync(() -> {
					Double result = 0.0d;
					try {
						result = Double.parseDouble(controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "SetTem").getData()
								.valuesMap().get("SetTem").toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return result;
				});
			}
		};

		Characteristic targetTemp = new EventableFloatCharacteristic(HAPAccessoryCharacteristicType.TARGET_TEMPERATURE.getCode(), true, true, "Temp", 16, 30,
				1.0d, "celsius") {
			@Override
			protected void setValue(Double value) throws Exception {
				controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)),
						LangUtil.mapBuilder("SetTem", (Object) value.intValue()).build());
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Double> getDoubleValue() {
				return CompletableFuture.supplyAsync(() -> {
					Double result = 0.0d;
					try {
						result = Double.parseDouble(controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "SetTem").getData()
								.valuesMap().get("SetTem").toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return result;
				});
			}
		};

		Characteristic targetCoolTemp = new EventableFloatCharacteristic(HAPAccessoryCharacteristicType.COOLING_THRESHOLD_TEMPERATURE.getCode(), true, true,
				"Temp", 16, 30, 1.0d, "celsius") {
			@Override
			protected void setValue(Double value) throws Exception {
				controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)),
						LangUtil.mapBuilder("SetTem", (Object) value.intValue()).build());
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Double> getDoubleValue() {
				return CompletableFuture.supplyAsync(() -> {
					Double result = 0.0d;
					try {
						result = Double.parseDouble(controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "SetTem").getData()
								.valuesMap().get("SetTem").toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return result;
				});
			}
		};

		Characteristic tempUnit = new EventableEnumCharacteristic(HAPAccessoryCharacteristicType.TEMPERATURE_DISPLAY_UNITS.getCode(), false, true, "Unit", 1) {
			@Override
			protected void setValue(Integer value) throws Exception {}

			@Override
			protected CompletableFuture<Integer> getValue() {
				return CompletableFuture.completedFuture(0);
			}
		};

		Supplier<Integer> getState = () -> {
			// OFF = 0;
			// HEAT = 1;
			// COOL = 2;
			// AUTO = 4;
			Integer result = 0;
			try {
				Map<String, Object> status = controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "Pow", "Mod").getData().valuesMap();
				boolean on = "1".equals(status.get("Pow").toString());
				int mode = Integer.parseInt(status.get("Mod").toString());
				if (on) {
					// modes
					// 0: auto
					// 1: cool
					// 2: dry
					// 3: fan
					// 4: heat
					switch (mode) {
						case 0:
							result = 4;
						break;
						case 4:
							result = 1;
						break;
						case 1:
						case 2:
						case 3:
						default:
							result = 2;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		};

		ExceptionalConsumer<Integer> setState = val -> {
			boolean power = val != 0;
			int mode;
			switch (val) {
				case 4:
					mode = 0;
				break;
				case 1:
					mode = 4;
				break;
				case 2:
				default:
					mode = 1;
			}
			controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)),
					LangUtil.mapBuilder("Pow", (Object) (power ? 1 : 0)).set("Mod", mode).build());
		};

		Characteristic targetHCS = new EventableEnumCharacteristic(HAPAccessoryCharacteristicType.TARGET_HEATING_COOLING_STATE.getCode(), true, true, "Mode",
				3) {
			@Override
			protected void setValue(Integer value) throws Exception {
				setState.accept(value);
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Integer> getValue() {
				return CompletableFuture.supplyAsync(getState);
			}
		};

		Characteristic currentHCS = new EventableEnumCharacteristic(HAPAccessoryCharacteristicType.CURRENT_HEATING_COOLING_STATE.getCode(), false, true, "Mode",
				3) {
			@Override
			protected void setValue(Integer value) throws Exception {
				setState.accept(value);
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Integer> getValue() {
				return CompletableFuture.supplyAsync(getState);
			}
		};

		Characteristic fanSpeed = new EventableIntegerCharacteristic(HAPAccessoryCharacteristicType.ROTATION_SPEED.getCode(), true, true, "fan", 0, 5,
				"Amount") {
			@Override
			protected void setValue(Integer value) throws Exception {
				controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)), LangUtil.mapBuilder("WdSpd", (Object) value).build())
						.getData().valuesMap();
				notifySubscribers();
			}

			@Override
			protected CompletableFuture<Integer> getValue() {
				return CompletableFuture.supplyAsync(() -> {
					Integer result = 1;
					try {
						result = Integer.parseInt(controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "WdSpd").getData().valuesMap()
								.get("WdSpd").toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return result;
				});
			}
		};

		Characteristic fanAuto = new PowerStateCharacteristic(() -> {
			return CompletableFuture.supplyAsync(() -> {
				int fanMode = 0;
				try {
					fanMode = Integer.parseInt(controlService.getStatus(ACBinding.ofBindResponse(controlService.bind(addr)), "WdSpd").getData().valuesMap()
							.get("WdSpd").toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return fanMode != 0;
			});
		}, v -> {
			// controlService.setParameters(ACBinding.ofBindResponse(controlService.bind(addr)), LangUtil.mapBuilder("WdSpd", (Object) (v ? 1 : 0)).build());
		}, s -> {}, () -> {});

		this.services = Arrays.asList(
				new HAPAccessoryService(HAPAccessoryServiceType.THERMOSTAT, targetHCS, currentHCS, curTemp, targetTemp, targetCoolTemp, tempUnit),
				new HAPAccessoryService(HAPAccessoryServiceType.FAN, fanSpeed, fanAuto, new Name(label + " fan")));
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void identify() {}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getModel() {
		return model;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public Collection<Service> getServices() {
		return services;
	}
}
