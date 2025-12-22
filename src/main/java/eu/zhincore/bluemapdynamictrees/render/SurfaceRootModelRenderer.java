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
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import eu.zhincore.bluemapdynamictrees.resources.BranchModelExtension;

public class SurfaceRootModelRenderer extends AbstractDynamicTreeRenderer {
  public static final BlockRendererType TYPE = new BlockRendererType.Impl(
      new Key("bluemapmodeladdon", "dt_surface_root"), SurfaceRootModelRenderer::new);
  private static final Direction[] HORIZONTALS = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

  public SurfaceRootModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery,
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

    if (barkPath == null)
      return;

    int barkTextureId = textureGallery.get(barkPath);

    int radius = getRadius(block.getBlockState());
    if (radius < 1 || radius > 8)
      return;

    ConnectionInfo connInfo = getConnections();

    int modelStart = blockModel.getStart();

    Direction sourceDir = getHorizontalSourceDir(radius, connInfo.horizontalRoots);
    int coreAxis = sourceDir != null && sourceDir.getAxis() == Axis.X ? 1 : 0;

    renderCore(radius, coreAxis, barkTextureId);

    if (radius < 8) {
      for (int i = 0; i < HORIZONTALS.length; i++) {
        int connRadius = connInfo.horizontalRoots[i];
        if (connRadius > 0) {
          renderSleeve(connRadius, HORIZONTALS[i], barkTextureId);
        }
      }
    }

    // Render vertical connections
    for (int i = 0; i < HORIZONTALS.length; i++) {
      int connRadius = connInfo.verticalBranches[i];
      if (connRadius > 0) {
        renderVert(connRadius, HORIZONTALS[i], barkTextureId);
      }
    }

    if (color.a > 0) {
      color.flatten().straight();
      color.a = blockColorOpacity;
    }

    blockModel.initialize(modelStart);
    blockModel.transform(createScaleMatrix(BLOCK_SCALE));
  }

  private void renderCore(int radius, int axis, int barkTextureId) {
    float radialHeight = radius * 2;
    float min = 8 - radius;
    float max = 8 + radius;

    setCorners(min, 0, min, max, radialHeight, max);

    for (Direction face : Direction.values()) {
      Axis axisEnum = axis == 0 ? Axis.Z : Axis.X;
      int angle = getFaceAngle(axisEnum, face);

      renderBoxFace(face, barkTextureId, angle, radius, 0);
    }
  }

  private void renderSleeve(int radius, Direction dir, int barkTextureId) {
    float radialHeight = radius * 2;
    int dradius = radius * 2;
    int halfSize = (16 - dradius) / 2;
    int move = 16 - halfSize;

    float minX = dir.toVector().getX() != 0 ? (16 + dir.toVector().getX() * move - halfSize) / 2f
        : (8 - radius);
    float maxX = dir.toVector().getX() != 0 ? (16 + dir.toVector().getX() * move + halfSize) / 2f
        : (8 + radius);
    float minZ = dir.toVector().getZ() != 0 ? (16 + dir.toVector().getZ() * move - halfSize) / 2f
        : (8 - radius);
    float maxZ = dir.toVector().getZ() != 0 ? (16 + dir.toVector().getZ() * move + halfSize) / 2f
        : (8 + radius);

    setCorners(minX, 0, minZ, maxX, radialHeight, maxZ);

    for (Direction face : Direction.values()) {
      if (face == getOpposite(dir))
        continue;

      int angle = getFaceAngle(dir.getAxis(), face);

      renderBoxFace(face, barkTextureId, angle, radius, 0);
    }
  }

  private void renderVert(int radius, Direction dir, int barkTextureId) {
    float radialHeight = radius * 2;

    // Render two segments: one in current block, one extending up
    for (int i = 0; i < 2; i++) {
      float minX = (8 - radius) + dir.toVector().getX() * 7;
      float maxX = (8 + radius) + dir.toVector().getX() * 7;
      float minY = radialHeight + i * 16;
      float maxY = Math.min(16 + radialHeight + i * 16, 32);
      float minZ = (8 - radius) + dir.toVector().getZ() * 7;
      float maxZ = (8 + radius) + dir.toVector().getZ() * 7;

      // Clip to block boundaries
      if (minY >= 16 && i == 0)
        continue;
      if (maxY > 16 && i == 0)
        maxY = 16;
      if (i == 1 && minY < 16)
        minY = 16;

      setCorners(minX, minY, minZ, maxX, maxY, maxZ);

      for (Direction face : Direction.values()) {
        renderBoxFace(face, barkTextureId, getFaceAngle(Axis.Y, face), radius, 0);
      }
    }
  }

  private static class ConnectionInfo {
    int[] horizontalRoots = new int[4];
    int[] verticalBranches = new int[4];
  }

  private ConnectionInfo getConnections() {
    ConnectionInfo info = new ConnectionInfo();
    int ownRadius = getRadius(block.getBlockState());

    for (int i = 0; i < HORIZONTALS.length; i++) {
      Direction dir = HORIZONTALS[i];
      var vec = dir.toVector();
      var neighbor = block.getNeighborBlock(vec.getX(), vec.getY(), vec.getZ());

      if (neighbor != null) {
        BlockState neighborState = neighbor.getBlockState();
        if (isSameSpecies(block.getBlockState(), neighborState)) {
          int neighborRadius = getRadius(neighborState);
          if (neighborRadius > 0) {
            info.horizontalRoots[i] = Math.min(neighborRadius, ownRadius);
          }
        }
      }
    }

    return info;
  }

  private Direction getHorizontalSourceDir(int coreRadius, int[] connections) {
    int largestConnection = 0;
    Direction sourceDir = null;

    for (int i = 0; i < HORIZONTALS.length; i++) {
      int connRadius = connections[i];
      if (connRadius > largestConnection) {
        largestConnection = connRadius;
        sourceDir = HORIZONTALS[i];
      }
    }

    if (largestConnection < coreRadius) {
      sourceDir = null;
    }

    return sourceDir;
  }
}
