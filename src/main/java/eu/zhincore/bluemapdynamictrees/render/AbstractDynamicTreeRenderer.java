package eu.zhincore.bluemapdynamictrees.render;

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Axis;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.util.math.VectorM2f;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import eu.zhincore.bluemapdynamictrees.resources.DynamicTreesPack;
import eu.zhincore.bluemapdynamictrees.resources.DynamicTreesPackFactory;

public abstract class AbstractDynamicTreeRenderer implements BlockRenderer {
  protected static final float BLOCK_SCALE = 1f / 16f;

  protected final ResourcePack blueMapResourcePack;
  protected final DynamicTreesPack dynamicTreesResourcePack;
  protected final TextureGallery textureGallery;
  protected final RenderSettings renderSettings;

  protected final VectorM3f[] corners = new VectorM3f[8];
  protected final VectorM2f[] uvRect = new VectorM2f[2];
  protected final Color mapColor = new Color();

  protected BlockNeighborhood block;
  protected TileModelView blockModel;
  protected Color blockColor;
  protected float blockColorOpacity;

  public AbstractDynamicTreeRenderer(ResourcePack resourcePack, TextureGallery textureGallery,
      RenderSettings renderSettings) {
    this.blueMapResourcePack = resourcePack;
    this.textureGallery = textureGallery;
    this.renderSettings = renderSettings;
    this.dynamicTreesResourcePack = resourcePack.getExtension(DynamicTreesPackFactory.INSTANCE);

    for (int i = 0; i < corners.length; i++) {
      corners[i] = new VectorM3f(0, 0, 0);
    }
    for (int i = 0; i < uvRect.length; i++) {
      uvRect[i] = new VectorM2f(0, 0);
    }
  }

  protected void setCorners(float minX, float minY, float minZ, float maxX, float maxY,
      float maxZ) {
    corners[0].set(minX, minY, minZ);
    corners[1].set(minX, minY, maxZ);
    corners[2].set(maxX, minY, minZ);
    corners[3].set(maxX, minY, maxZ);
    corners[4].set(minX, maxY, minZ);
    corners[5].set(minX, maxY, maxZ);
    corners[6].set(maxX, maxY, minZ);
    corners[7].set(maxX, maxY, maxZ);
  }

  protected void renderBoxFace(Direction face, int textureId, int uvRotation, int radius, int squish) {
    VectorM3f[] c = corners;
    VectorM3f c0, c1, c2, c3;

    switch (face) {
      case DOWN:
        c0 = c[0];
        c1 = c[2];
        c2 = c[3];
        c3 = c[1];
        uvRect[0].x = c0.x;
        uvRect[0].y = c0.z;
        uvRect[1].x = c2.x;
        uvRect[1].y = c2.z;
        break;
      case UP:
        c0 = c[5];
        c1 = c[7];
        c2 = c[6];
        c3 = c[4];
        uvRect[0].x = c0.x;
        uvRect[0].y = c0.z;
        uvRect[1].x = c2.x;
        uvRect[1].y = c2.z;
        break;
      case NORTH:
        c0 = c[2];
        c1 = c[0];
        c2 = c[4];
        c3 = c[6];
        uvRect[0].x = c0.x;
        uvRect[0].y = c0.y;
        uvRect[1].x = c2.x;
        uvRect[1].y = c2.y;
        break;
      case SOUTH:
        c0 = c[1];
        c1 = c[3];
        c2 = c[7];
        c3 = c[5];
        uvRect[0].x = c0.x;
        uvRect[0].y = c0.y;
        uvRect[1].x = c2.x;
        uvRect[1].y = c2.y;
        break;
      case WEST:
        c0 = c[0];
        c1 = c[1];
        c2 = c[5];
        c3 = c[4];
        uvRect[0].x = c0.z;
        uvRect[0].y = c0.y;
        uvRect[1].x = c2.z;
        uvRect[1].y = c2.y;
        break;
      case EAST:
      default:
        c0 = c[3];
        c1 = c[2];
        c2 = c[6];
        c3 = c[7];
        uvRect[0].x = c0.z;
        uvRect[0].y = c0.y;
        uvRect[1].x = c2.z;
        uvRect[1].y = c2.y;
        break;
    }

    Vector3i faceVec = face.toVector();
    ExtendedBlock facedBlock = block.getNeighborBlock(faceVec.getX(), faceVec.getY(), faceVec.getZ());
    LightData blockLight = block.getLightData();
    LightData facedLight = facedBlock.getLightData();

    int sunLight = Math.max(blockLight.getSkyLight(), facedLight.getSkyLight());
    int blockLightLevel = Math.max(blockLight.getBlockLight(), facedLight.getBlockLight());

    if (block.isRemoveIfCave()
        && (renderSettings.isCaveDetectionUsesBlockLight() ? Math.max(blockLightLevel, sunLight)
            : sunLight) == 0) {
      return;
    }

    blockModel.initialize();
    blockModel.add(2);

    TileModel tileModel = blockModel.getTileModel();
    int face1 = blockModel.getStart();
    int face2 = face1 + 1;

    // @formatter:off
    tileModel.setPositions(face1,
      c0.x, c0.y, c0.z,
      c1.x, c1.y, c1.z,
      c2.x, c2.y, c2.z
    );
    tileModel.setPositions(face2,
      c0.x, c0.y, c0.z,
      c2.x, c2.y, c2.z,
      c3.x, c3.y, c3.z
    );
    // @formatter:on

    tileModel.setMaterialIndex(face1, textureId);
    tileModel.setMaterialIndex(face2, textureId);

    float[][] uvs = calculateUVs(uvRotation, uvRect[0], uvRect[1]);
    // @formatter:off
    tileModel.setUvs(face1,
      uvs[0][0], uvs[0][1],
      uvs[1][0], uvs[1][1],
      uvs[2][0], uvs[2][1]
    );
    tileModel.setUvs(face2,
      uvs[0][0], uvs[0][1],
      uvs[2][0], uvs[2][1],
      uvs[3][0], uvs[3][1]
    );
    // @formatter:on

    tileModel.setColor(face1, 1f, 1f, 1f);
    tileModel.setColor(face2, 1f, 1f, 1f);

    tileModel.setBlocklight(face1, blockLightLevel);
    tileModel.setBlocklight(face2, blockLightLevel);
    tileModel.setSunlight(face1, sunLight);
    tileModel.setSunlight(face2, sunLight);

    tileModel.setAOs(face1, 1f, 1f, 1f);
    tileModel.setAOs(face2, 1f, 1f, 1f);
  }

  protected MatrixM4f createScaleMatrix(float scale) {
    return new MatrixM4f().scale(scale, scale, scale);
  }

  protected float[][] calculateUVs(int uvRotation, VectorM2f uv0, VectorM2f uv1) {
    float minU = Math.min(uv0.x, uv1.x) * BLOCK_SCALE;
    float minV = Math.min(uv0.y, uv1.y) * BLOCK_SCALE;
    float maxU = Math.max(uv0.x, uv1.x) * BLOCK_SCALE;
    float maxV = Math.max(uv0.y, uv1.y) * BLOCK_SCALE;

    float[][] result = new float[4][2];

    int steps = (uvRotation / 90) % 4;
    switch (steps) {
      case 0:
        result[0][0] = minU;
        result[0][1] = maxV;
        result[1][0] = maxU;
        result[1][1] = maxV;
        result[2][0] = maxU;
        result[2][1] = minV;
        result[3][0] = minU;
        result[3][1] = minV;
        break;
      case 1:
        result[0][0] = minV;
        result[0][1] = minU;
        result[1][0] = minV;
        result[1][1] = maxU;
        result[2][0] = maxV;
        result[2][1] = maxU;
        result[3][0] = maxV;
        result[3][1] = minU;
        break;
      case 2:
        result[0][0] = maxU;
        result[0][1] = minV;
        result[1][0] = minU;
        result[1][1] = minV;
        result[2][0] = minU;
        result[2][1] = maxV;
        result[3][0] = maxU;
        result[3][1] = maxV;
        break;
      case 3:
        result[0][0] = maxV;
        result[0][1] = maxU;
        result[1][0] = maxV;
        result[1][1] = minU;
        result[2][0] = minV;
        result[2][1] = minU;
        result[3][0] = minV;
        result[3][1] = maxU;
        break;
    }

    return result;
  }

  protected int getFaceAngle(Axis axis, Direction face) {
    if (axis == Axis.Y) {
      return 0;
    } else if (axis == Axis.Z) {
      switch (face) {
        case UP:
          return 0;
        case WEST:
          return 270;
        case DOWN:
          return 180;
        default:
          return 90;
      }
    } else {
      return (face == Direction.NORTH) ? 270 : 90;
    }
  }

  protected int getRadius(BlockState state) {
    String radiusStr = state.getProperties().get("radius");
    if (radiusStr == null)
      return 0;
    try {
      return Integer.parseInt(radiusStr);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  protected int[] getConnections(BlockState state) {
    int[] connections = new int[6];

    // Compute connections by querying neighbor block radii (like TreeHelper does)
    for (Direction dir : Direction.values()) {
      int idx = dir.ordinal();
      var vec = dir.toVector();
      var neighbor = block.getNeighborBlock(vec.getX(), vec.getY(), vec.getZ());

      if (neighbor != null) {
        BlockState neighborState = neighbor.getBlockState();
        if (isSameSpecies(state, neighborState)) {
          int neighborRadius = getRadius(neighborState);
          if (neighborRadius > 0) {
            connections[idx] = neighborRadius;
          }
        } else if (isLeavesBlock(neighborState)) {
          connections[idx] = 1;
        } else if (dir == Direction.DOWN && isRootySoil(neighborState)) {
          // Connect to ground with core radius
          connections[idx] = getRadius(state);
        }
      }
    }

    return connections;
  }

  protected boolean isSameSpecies(BlockState state1, BlockState state2) {
    return state1.getId().equals(state2.getId());
  }

  protected boolean isLeavesBlock(BlockState state) {
    return "dynamictrees".equals(state.getId().getNamespace())
        && state.getId().getValue().contains("leaves");
  }

  protected boolean isRootySoil(BlockState state) {
    return "dynamictrees".equals(state.getId().getNamespace())
        && state.getId().getValue().contains("rooty");
  }

  protected Direction getSourceDir(int coreRadius, int[] connections) {
    int largestConnection = 0;
    Direction sourceDir = null;

    for (Direction dir : Direction.values()) {
      int connRadius = connections[dir.ordinal()];
      if (connRadius > largestConnection) {
        largestConnection = connRadius;
        sourceDir = dir;
      }
    }

    if (largestConnection < coreRadius) {
      sourceDir = null;
    }

    return sourceDir;
  }

  protected int resolveCoreAxis(Direction dir) {
    if (dir == null)
      return 0;
    return dir.ordinal() >> 1;
  }

  protected boolean shouldRenderRingEnd(int[] connections, Direction sourceDir) {
    if (sourceDir == null)
      return false;

    int numConnections = 0;
    for (int conn : connections) {
      if (conn != 0)
        numConnections++;
    }

    return numConnections == 1;
  }

  protected Direction getOpposite(Direction dir) {
    switch (dir) {
      case UP:
        return Direction.DOWN;
      case DOWN:
        return Direction.UP;
      case NORTH:
        return Direction.SOUTH;
      case SOUTH:
        return Direction.NORTH;
      case WEST:
        return Direction.EAST;
      case EAST:
        return Direction.WEST;
      default:
        return dir;
    }
  }
}
