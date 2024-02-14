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
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
    @Final
    public static StitchedSprite OAK_LOG_TEMPLATE;

    @Shadow
    @Final
    public static StitchedSprite OAK_LOG_TOP_TEMPLATE;

    @Shadow
    @Final
    private static String[] LOG_SUFFIXES;

    public WaterWheelRendererMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }


    @Inject(method = "generateModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("TAIL"), cancellable = true)
    private static void onGenerateModel(BakedModel template, BlockState planksBlockState, CallbackInfoReturnable<BakedModel> cir) {
        Block planksBlock = planksBlockState.getBlock();
        ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
        ItemStack planksStack = new ItemStack(planksBlock.asItem());
        TagKey<Item> planksTag = ItemTags.PLANKS;
        String namespace = id.getNamespace();
        Language lang = Language.getInstance();

        //DEBUG: logger.info("Plank namespace is " + namespace);

        if (planksStack.is(planksTag)) {
            String planksName = planksStack.getDisplayName()
                    .getString()
                    .strip()
                    .toLowerCase();
            String wood = planksName.replace(" planks", "")
                    .replace(" ", "_")
                    .replace("[", "")
                    .replace("]", "");

            BlockState logBlockState = getLogBlockState(namespace, wood);

            Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
            map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
            map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));
            map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));

            cir.setReturnValue(BakedModelHelper.generateModel(template, map::get));
        }
    }

    /**
     * @author iylo
     * @reason Compatibility with non-Vanilla woods
     */


    @Overwrite
    private static BlockState getLogBlockState(String namespace, String wood) {
        Block logBlock;
        TagKey<Block> logsTag = BlockTags.LOGS;
        Object[] blockList = ForgeRegistries.BLOCKS.tags().getTag(logsTag).stream().toArray();
        //DEBUG: logger.info("There are " + blockList.length + " Blocks in the blockList!");
        for (Object object : blockList) {
            Block block = (Block) object;
            ItemStack logStack = new ItemStack(block.asItem());
            String logName = logStack.getDisplayName().getString().strip().toLowerCase();
            //DEBUG: logger.info("logName is " + logName);
            for (String suffix : LOG_SUFFIXES) {
                //DEBUG: logger.info("suffix is " + suffix);
                try {
                    String logWood = logName
                            .replace(" ", "_")
                            .replace("[", "")
                            .replace("]", "");
                    logWood = logWood.replace(suffix, "");
                    //DEBUG: logger.info("logWood is " + logWood);
                    //DEBUG: logger.info("wood is " + wood);
                    if (logWood.equals(wood)) {
                        logBlock = block;
                        ResourceLocation id = RegisteredObjects.getKeyOrThrow(logBlock);
                        //DEBUG: logger.info("Log namespace is " + id.getNamespace());
                        String logNamespace = id.getNamespace();
                        if(logNamespace.equals(namespace)) {
                            return logBlock.defaultBlockState();
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Blocks.OAK_LOG.defaultBlockState();
    }
}
