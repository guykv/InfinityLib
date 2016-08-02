/*
 */
package com.infinityraider.infinitylib.render.block;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.infinityraider.infinitylib.block.BlockBase;
import com.infinityraider.infinitylib.block.ICustomRenderedBlock;
import com.infinityraider.infinitylib.block.blockstate.IBlockStateSpecial;
import com.infinityraider.infinitylib.render.DefaultTransforms;
import com.infinityraider.infinitylib.render.item.BakedInfItemSuperModel;
import com.infinityraider.infinitylib.render.tessellation.TessellatorBakedQuad;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *
 * @author RlonRyan
 */
public class BakedInfBlockModel<B extends BlockBase & ICustomRenderedBlock> implements IBakedModel {

	private final B block;
	private final VertexFormat format;
	private final RenderBlock<B> renderer;
	private final Function<ResourceLocation, TextureAtlasSprite> textures;
	private final BakedInfItemSuperModel itemRenderer;

	private Map<EnumFacing, List<BakedQuad>> cachedQuads;
	private IBlockState prevState;

	public BakedInfBlockModel(B block, VertexFormat format, RenderBlock<B> renderer, Function<ResourceLocation, TextureAtlasSprite> textures, boolean inventory) {
		this.block = block;
		this.format = format;
		this.renderer = renderer;
		this.textures = textures;
		this.itemRenderer = inventory ? new BakedInfItemSuperModel(format, this.renderer, textures, DefaultTransforms::getBlockMatrix) : null;
		this.cachedQuads = new HashMap<>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if ((state instanceof IBlockStateSpecial)) {
			IBlockStateSpecial bs = (IBlockStateSpecial) state;
			World world = Minecraft.getMinecraft().theWorld;
			IBlockState extendedState = bs.getWrappedState();
			BlockPos pos = bs.getPos();

			boolean update = !cachedQuads.containsKey(side) || this.block.needsRenderUpdate(world, pos, extendedState);

			if (update) {
				TessellatorBakedQuad tessellator = TessellatorBakedQuad.getInstance().setTextureFunction(this.textures).setCurrentFace(side);

				tessellator.startDrawingQuads(this.format);

				this.renderer.renderStatic(tessellator, world, state, pos);

				cachedQuads.put(side, tessellator.getQuads());
				prevState = extendedState;

				tessellator.draw();
			}

		} else {
			return ImmutableList.of();
		}
		return cachedQuads.get(side);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return renderer.applyAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return renderer.hasInventoryRendering();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return renderer.getIcon();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return itemRenderer.getOverrides();
	}

}