package eu.zhincore.bluemapdynamictrees.resources;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import eu.zhincore.bluemapdynamictrees.BlueMapDynamicTreesAddon;

public class DynamicTreesPackFactory implements ResourcePack.Extension<DynamicTreesPack> {

  public static final DynamicTreesPackFactory INSTANCE = new DynamicTreesPackFactory();

  @Override
  public DynamicTreesPack create(ResourcePack pack) {
    return new DynamicTreesPack(pack);
  }

  @Override
  public Key getKey() {
    return new Key(BlueMapDynamicTreesAddon.ID, "resourcepack");
  }
}
