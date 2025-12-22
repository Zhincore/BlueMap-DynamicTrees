package eu.zhincore.bluemapdynamictrees.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import eu.zhincore.bluemapdynamictrees.BlueMapDynamicTreesAddon;
import eu.zhincore.bluemapdynamictrees.render.BranchModelRenderer;
import eu.zhincore.bluemapdynamictrees.render.SurfaceRootModelRenderer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@JsonAdapter(LoaderType.Adapter.class)
public enum LoaderType {
  BRANCH("branch", BranchModelRenderer.TYPE), ROOT("surface_root", SurfaceRootModelRenderer.TYPE);

  private static final Map<Key, LoaderType> BY_KEY;
  static {
    Map<Key, LoaderType> map = new HashMap<>();
    for (LoaderType type : values()) {
      map.put(type.key, type);
    }
    BY_KEY = Map.copyOf(map);
  }

  @Getter
  private final Key key;
  @Getter
  private final BlockRendererType renderer;

  LoaderType(String type, BlockRendererType renderer) {
    this.key = new Key(BlueMapDynamicTreesAddon.DT_ID, type);
    this.renderer = renderer;
  }

  public static LoaderType fromKey(Key key) {
    LoaderType type = BY_KEY.get(key);
    if (type == null) {
      throw new IllegalArgumentException("Unknown loader type: " + key.getFormatted());
    }
    return type;
  }

  public static class Adapter extends TypeAdapter<LoaderType> {
    @Override
    public void write(JsonWriter out, LoaderType value) throws IOException {
      out.value(value.key.getFormatted());
    }

    @Override
    public LoaderType read(JsonReader in) throws IOException {
      return fromKey(Key.parse(in.nextString()));
    }
  }
}
