package com.ecrea.elementmagicboss.client.model;

import com.ecrea.elementmagicboss.item.FireSwordItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FireSwordModel extends GeoModel<FireSwordItem> {
    @Override
    public ResourceLocation getModelResource(FireSwordItem animatable) {
        return new ResourceLocation("elementmagicboss", "geo/firesword.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FireSwordItem animatable) {
        return new ResourceLocation("elementmagicboss", "textures/item/firesword.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FireSwordItem animatable) {
        return null; // アニメーションなし
    }
}
