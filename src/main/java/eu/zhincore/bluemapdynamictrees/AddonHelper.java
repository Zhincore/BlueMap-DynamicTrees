package eu.zhincore.bluemapdynamictrees;

import de.bluecolored.bluemap.core.util.Key;

public abstract class AddonHelper {

  public static Key addonKey(String value) {
    return new Key(BlueMapDynamicTreesAddon.ID, value);
  }
}
