package x.mvmn.aircndctrl.hap;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.hapjava.HomekitRoot;
import io.github.hapjava.HomekitServer;
import x.mvmn.aircndctrl.hap.accessory.ACAccessory;
import x.mvmn.aircndctrl.hap.service.AuthInfoService;
import x.mvmn.aircndctrl.model.addr.ACAddress;
import x.mvmn.aircndctrl.service.EncryptionService;
import x.mvmn.aircndctrl.service.impl.ACControlServiceImpl;
import x.mvmn.aircndctrl.service.impl.ACDiscoverServiceImpl;
import x.mvmn.aircndctrl.service.impl.EncryptionServiceImpl;

@SpringBootApplication
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

			EncryptionService encryptionService = new EncryptionServiceImpl();
			ACControlServiceImpl acControlService = new ACControlServiceImpl(encryptionService);
			Set<String> macs = Collections.synchronizedSet(new HashSet<>());
			System.out.println("Discovering ACs...");
			new ACDiscoverServiceImpl(encryptionService).discover(10000, discovery -> {
				if (macs.add(discovery.getData().getMac())) {
					System.out.println("Discovered AC " + discovery.getData().getName() + " / " + discovery.getData().getMac() + " @ "
							+ discovery.getAddress().getHostString() + ":" + discovery.getAddress().getPort());
					bridge.addAccessory(new ACAccessory(discovery.getData().getMac().hashCode(), discovery.getData().getName(), discovery.getData().getMac(),
							discovery.getData().getModel(), discovery.getData().getSeries(), ACAddress.ofDiscoveryResponse(discovery), acControlService));
				}
			}).join();
			System.out.println("Done discovering. Starting bridge...");
			bridge.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
