package eu.zhincore.bluemapdynamictrees.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
public class BranchModelExtension {
  private static final String BARK = "bark";
  private static final String RINGS = "rings";

  private LoaderType loader;
  private Map<String, TextureVariable> textures;

  private Map<String, TextureVariable> getTextures() {
    if (textures == null) {
      textures = new HashMap<>();
    }
    return textures;
  }

  public @Nullable TextureVariable getBarkTexture() {
    return getTextures().get(BARK);
  }

  public @Nullable TextureVariable getRingsTexture() {
    return getTextures().get(RINGS);
  }

  public Stream<ResourcePath<Texture>> getUsedTextures() {
    return getTextures().values().stream().map(TextureVariable::getTexturePath).filter(Objects::nonNull);
  }
}
