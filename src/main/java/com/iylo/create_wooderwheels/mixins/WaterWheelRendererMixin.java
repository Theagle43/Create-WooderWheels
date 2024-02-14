package com.iylo.create_wooderwheels.mixins;

import com.jozufozu.flywheel.core.StitchedSprite;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = WaterWheelRenderer.class, remap = false)
public abstract class WaterWheelRendererMixin<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T> {

    @Shadow
    @Final
    public static StitchedSprite OAK_PLANKS_TEMPLATE;

    @Shadow
    private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
        return null;
    }

    @Shadow
    private static BlockState getLogBlockState(String namespace, String wood){
        return null;
    }

    @Shadow
    @Final
    public static StitchedSprite OAK_LOG_TEMPLATE;

    @Shadow
    @Final
    public static StitchedSprite OAK_LOG_TOP_TEMPLATE;

    public WaterWheelRendererMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "generateModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("TAIL"), cancellable = true)
    private static void onGenerateModel(BakedModel template, BlockState planksBlockState, CallbackInfoReturnable<BakedModel> cir) {
        Block planksBlock = planksBlockState.getBlock();
        planksBlock.getDescriptionId();
        ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
        ItemStack planksStack = new ItemStack(planksBlock.asItem());
        TagKey<Item> planksTag = ItemTags.PLANKS;
        String namespace = id.getNamespace();
        String planksPath = id.getPath();

        //DEBUG: logger.info("Plank namespace is " + namespace);

        if (planksStack.is(planksTag)) {

            BlockState logBlockState = getLogBlockState(namespace, planksPath);

            Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
            map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
            map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));
            map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));

            cir.setReturnValue(BakedModelHelper.generateModel(template, map::get));
        }
    }

    @Inject(method = "getLogBlockState", at=@At("TAIL"), cancellable = true)
    private static void onGetLogBlockState(String namespace, String planksPath, CallbackInfoReturnable<BlockState> cir) {
        TagKey<Block> logsTag = BlockTags.LOGS;
        Object[] blockList = ForgeRegistries.BLOCKS.tags().getTag(logsTag).stream().toArray();
        String[] logPathMarkers = new String[]{"log","stem"};
        //DEBUG: logger.info("There are " + blockList.length + " Blocks in the blockList!");
        for (Object object : blockList) {
            Block logBlock = (Block) object;
            ResourceLocation id = RegisteredObjects.getKeyOrThrow(logBlock);
            String path = id.getPath();
            String logNamespace = id.getNamespace();
            for(String marker : logPathMarkers){
                String logPath = planksPath.replace("planks", marker);
                if(path.equals(logPath) && logNamespace.equals(namespace)){
                    cir.setReturnValue(logBlock.defaultBlockState());
                    return;
                }
            }
        }
    }
}
