package eu.zhincore.bluemapdynamictrees.render;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Axis;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import eu.zhincore.bluemapdynamictrees.resources.BranchModelExtension;

public class BranchModelRenderer extends AbstractDynamicTreeRenderer {
  public static final BlockRendererType TYPE = new BlockRendererType.Impl(
      new Key("bluemapmodeladdon", "dt_branch"), BranchModelRenderer::new);

  public BranchModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery,
      RenderSettings renderSettings) {
    super(resourcePack, textureGallery, renderSettings);
  }

  @Override
  public void render(BlockNeighborhood block, Variant variant, TileModelView blockModel,
      Color color) {
    this.block = block;
    this.blockModel = blockModel;
    this.blockColor = color;
    this.blockColorOpacity = 0f;

    BranchModelExtension model = dynamicTreesResourcePack.getModels().get(variant.getModel());

    if (model == null)
      return;

    ResourcePath<Texture> barkPath = model.getBarkTexture().getTexturePath();
    ResourcePath<Texture> ringsPath = model.getRingsTexture().getTexturePath();

    if (barkPath == null || ringsPath == null)
      return;

    int barkTextureId = textureGallery.get(barkPath);
    int ringsTextureId = textureGallery.get(ringsPath);

    int radius = getRadius(block.getBlockState());
    if (radius < 1)
      return;

    int[] connections = getConnections(block.getBlockState());

    int modelStart = blockModel.getStart();

    Direction sourceDir = getSourceDir(radius, connections);
    int coreAxis = resolveCoreAxis(sourceDir);
    boolean hasRingEnd = shouldRenderRingEnd(connections, sourceDir);

    renderCore(radius, coreAxis, barkTextureId, ringsTextureId, connections,
        hasRingEnd ? getOpposite(sourceDir) : null);

    if (radius < 8) {
      for (Direction dir : Direction.values()) {
        int connRadius = connections[dir.ordinal()];

        if (connRadius > 0) {
          renderSleeve(connRadius, dir, barkTextureId);
        }
      }
    }

    if (color.a > 0) {
      color.flatten().straight();
      color.a = blockColorOpacity;
    }

    blockModel.initialize(modelStart);
    blockModel.transform(createScaleMatrix(BLOCK_SCALE));
  }

  private void renderCore(int radius, int axis, int barkTextureId, int ringsTextureId,
      int[] connections, Direction ringDir) {
    float min = 8 - radius;
    float max = 8 + radius;

    setCorners(min, Math.max(0, min), min, max, Math.min(16, max), max);

    for (Direction face : Direction.values()) {
      boolean useRings = ringDir != null && ringDir == face;
      int textureId = useRings ? ringsTextureId : barkTextureId;

      Axis axisEnum = axis == 0 ? Axis.Y : (axis == 1 ? Axis.Z : Axis.X);
      renderBoxFace(face, textureId, getFaceAngle(axisEnum, face), radius, 0);
    }
  }

  private void renderSleeve(int neighborRadius, Direction dir, int textureId) {
    int coreRadius = getRadius(block.getBlockState());

    // Sleeve tapers from core radius at the core boundary to neighbor radius at
    // block edge
    // Use the smaller of the two for the sleeve cross-section
    int sleeveRadius = Math.min(coreRadius, neighborRadius);
    int squish = coreRadius - sleeveRadius;

    float minX, maxX, minY, maxY, minZ, maxZ;

    switch (dir) {
      case DOWN:
        minX = 8 - sleeveRadius;
        maxX = 8 + sleeveRadius;
        minY = 0;
        maxY = 8 - coreRadius;
        minZ = 8 - sleeveRadius;
        maxZ = 8 + sleeveRadius;
        break;
      case UP:
        minX = 8 - sleeveRadius;
        maxX = 8 + sleeveRadius;
        minY = 8 + coreRadius;
        maxY = 16;
        minZ = 8 - sleeveRadius;
        maxZ = 8 + sleeveRadius;
        break;
      case NORTH:
        minX = 8 - sleeveRadius;
        maxX = 8 + sleeveRadius;
        minY = 8 - sleeveRadius;
        maxY = 8 + sleeveRadius;
        minZ = 0;
        maxZ = 8 - coreRadius;
        break;
      case SOUTH:
        minX = 8 - sleeveRadius;
        maxX = 8 + sleeveRadius;
        minY = 8 - sleeveRadius;
        maxY = 8 + sleeveRadius;
        minZ = 8 + coreRadius;
        maxZ = 16;
        break;
      case WEST:
        minX = 0;
        maxX = 8 - coreRadius;
        minY = 8 - sleeveRadius;
        maxY = 8 + sleeveRadius;
        minZ = 8 - sleeveRadius;
        maxZ = 8 + sleeveRadius;
        break;
      case EAST:
      default:
        minX = 8 + coreRadius;
        maxX = 16;
        minY = 8 - sleeveRadius;
        maxY = 8 + sleeveRadius;
        minZ = 8 - sleeveRadius;
        maxZ = 8 + sleeveRadius;
        break;
    }

    setCorners(minX, minY, minZ, maxX, maxY, maxZ);

    Direction opposite = getOpposite(dir);
    for (Direction face : Direction.values()) {
      // Skip the face pointing back to the core, but render the face toward the
      // neighbor
      if (face == opposite)
        continue;

      int angle = getFaceAngle(dir.getAxis(), face);
      renderBoxFace(face, textureId, angle, sleeveRadius, squish);
    }
  }

}
