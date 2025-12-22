package eu.zhincore.bluemapdynamictrees;

import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import eu.zhincore.bluemapdynamictrees.resources.DynamicTreesPackFactory;

public class BlueMapDynamicTreesAddon implements Runnable {
  public static final String ID = "BlueMapDynamicTrees";

  public static final String DT_ID = "dynamictrees";

  public void run() {
    ResourcePack.Extension.REGISTRY.register(DynamicTreesPackFactory.INSTANCE);

    Logger.global.logInfo("BlueMap DynamicTrees addon registered");
  }

}
