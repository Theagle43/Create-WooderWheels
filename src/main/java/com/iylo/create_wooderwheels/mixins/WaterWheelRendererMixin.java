package com.iylo.create_wooderwheels.mixins;

import com.jozufozu.flywheel.core.StitchedSprite;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.foundation.model.BakedModelHelper;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

@Mixin(value = WaterWheelRenderer.class, remap = false)
public abstract class WaterWheelRendererMixin<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T> {


    @Shadow @Final public static StitchedSprite OAK_PLANKS_TEMPLATE;

    public WaterWheelRendererMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "generateModel*", at = @At("TAIL"))
    public BakedModel ongenerateModel(BakedModel template, BlockState planksBlockState){
        Block planksBlock = planksBlockState.getBlock();
        ResourceLocation id = RegisteredObjects.getKeyOrThrow(planksBlock);
        String path = id.getPath();
        Item planksItem = planksBlock.asItem();
        ItemStack planksStack = new ItemStack(planksItem);
        TagKey<Item> planksTag = ItemTags.PLANKS;
        Stream<TagKey<Item>> planksStackTags = planksStack.getTags();
        TagKey<Item> logsTag = ItemTags.LOGS;

        if(planksStack.is(planksTag)){
            String name = String.valueOf(planksStack.getDisplayName()).strip().toLowerCase();
            String[] nameArray = name.split(" ");
            Iterator<String> itNameArray = Arrays.stream(nameArray).iterator();
            StringBuilder plankNameString = new StringBuilder();

            while(itNameArray.hasNext()){
                String next = itNameArray.next();
                    if(next.equals("planks")){
                        itNameArray.remove();
                    } else {
                        plankNameString.append(next);
                        itNameArray.remove();
                        if(itNameArray.hasNext()){
                            plankNameString.append("_");
                        }
                    }
            }
        }

        return BakedModelHelper.generateModel(template, sprite -> null);
    }


}
