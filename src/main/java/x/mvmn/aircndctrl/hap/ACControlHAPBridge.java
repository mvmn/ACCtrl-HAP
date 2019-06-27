package x.mvmn.aircndctrl.hap;

import java.io.File;

import io.github.hapjava.HomekitRoot;
import io.github.hapjava.HomekitServer;
import x.mvmn.aircndctrl.hap.accessory.ACAccessory;
import x.mvmn.aircndctrl.hap.service.AuthInfoService;
import x.mvmn.aircndctrl.model.addr.ACAddress;
import x.mvmn.aircndctrl.service.EncryptionService;
import x.mvmn.aircndctrl.service.impl.ACControlServiceImpl;
import x.mvmn.aircndctrl.service.impl.ACDiscoverServiceImpl;
import x.mvmn.aircndctrl.service.impl.EncryptionServiceImpl;

public class ACControlHAPBridge {

	public static void main(String args[]) {
		try {
			File dataFolder = new File(new File(System.getProperty("user.home")), ".achap");
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}

			AuthInfoService authInfoService = new AuthInfoService(dataFolder);
			HomekitServer homekit = new HomekitServer(Integer.parseInt(System.getProperty("achp.server.port", "9123")));
			HomekitRoot bridge = homekit.createBridge(authInfoService, "Test Bridge", "TestBridge, Inc.", "G6", "111abe234");

			bridge.start();

			EncryptionService encryptionService = new EncryptionServiceImpl();
			ACControlServiceImpl acControlService = new ACControlServiceImpl(encryptionService);
			new ACDiscoverServiceImpl(encryptionService).discover(10000, discovery -> {
				bridge.addAccessory(new ACAccessory(discovery.getData().getMac().hashCode(), discovery.getData().getName(), discovery.getData().getMac(),
						discovery.getData().getModel(), discovery.getData().getSeries(), ACAddress.ofDiscoveryResponse(discovery), acControlService));
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
