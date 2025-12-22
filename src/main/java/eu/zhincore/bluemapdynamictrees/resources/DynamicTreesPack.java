package eu.zhincore.bluemapdynamictrees.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.ResourcePool;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import lombok.Getter;

public class DynamicTreesPack implements ResourcePackExtension {
  private ResourcePack blueMapResourcePack;

  @Getter
  private final ResourcePool<BranchModelExtension> models;

  DynamicTreesPack(ResourcePack pack) {
    this.blueMapResourcePack = pack;

    this.models = new ResourcePool<>();
  }

  @Override
  public void loadResources(Iterable<Path> roots) throws IOException, InterruptedException {
    for (Path root : roots) {
      blueMapResourcePack.loadResourcePath(root, this::loadResourcesFromPath);
    }
  }

  public void loadResourcesFromPath(Path root) throws IOException {
    ResourcePack.list(root.resolve("assets"))
        .map(path -> path.resolve("models").resolve("block"))
        .flatMap(ResourcePack::walk)
        .filter(path -> path.getFileName().toString().endsWith(".json"))
        .filter(Files::isRegularFile)
        .forEach(file -> models.load(
            new ResourcePath<>(root.relativize(file), 1, 3), key -> {
              try (BufferedReader reader = Files.newBufferedReader(file)) {
                return ResourcesGson.INSTANCE.fromJson(reader, BranchModelExtension.class);
              }
            }));
  }

  @Override
  public Set<Key> collectUsedTextureKeys() {
    return models.values().stream().flatMap(BranchModelExtension::getUsedTextures)
        .collect(Collectors.toSet());
  }

  @Override
  public void bake() throws IOException {
    blueMapResourcePack.getBlockStates().values().forEach(blockState -> {
      blockState.forEach(variant -> {
        BranchModelExtension model = models.get(variant.getModel());
        if (model == null)
          return;

        LoaderType loader = model.loader();
        if (loader != null) {
          variant.setRenderer(loader.renderer());
        }
      });
    });
  }
}
