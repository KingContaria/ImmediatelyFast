package net.raphimc.immediatelyfast.feature.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.render.*;
import net.minecraft.screen.PlayerScreenHandler;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;

import java.util.Map;

/**
 * Class which holds various allocated buffers used for batching various rendered elements.
 * <p>
 * Also contains references to vertex consumers which are called within mixins to redirect the vertex data into the batching buffer.
 * <p>
 * Once a begin method is called, all vertex data between the begin and end method will be redirected into the batching buffer and drawn in one batch at the end.
 */
public class BatchingBuffers {

    /*
     * The references into which specific vertex data is redirected.
     *
     * Set to null if batching is disabled and the data should be drawn immediately as usual.
     */
    public static VertexConsumerProvider FILL_CONSUMER = null;
    public static VertexConsumerProvider TEXTURE_CONSUMER = null;
    public static VertexConsumerProvider TEXT_CONSUMER = null;
    public static VertexConsumerProvider LIT_ITEM_MODEL_CONSUMER = null;
    public static VertexConsumerProvider UNLIT_ITEM_MODEL_CONSUMER = null;
    public static VertexConsumerProvider ITEM_OVERLAY_CONSUMER = null;

    /*
     * The batching buffers which hold the vertex data of the batch.
     */
    private static final BatchableImmediate FILL_BATCH = new BatchableImmediate();
    private static final BatchableImmediate TEXTURE_BATCH = new BatchableImmediate();
    private static final BatchableImmediate TEXT_BATCH = new BatchableImmediate();
    private static final BatchableImmediate LIT_ITEM_MODEL_BATCH = new BatchableImmediate(createLayerBuffers(
            RenderLayer.getArmorGlint(),
            RenderLayer.getArmorEntityGlint(),
            RenderLayer.getGlint(),
            RenderLayer.getDirectGlint(),
            RenderLayer.getGlintTranslucent(),
            RenderLayer.getEntityGlint(),
            RenderLayer.getDirectEntityGlint()
    ));
    private static final BatchableImmediate UNLIT_ITEM_MODEL_BATCH = new BatchableImmediate(createLayerBuffers(
            RenderLayer.getArmorGlint(),
            RenderLayer.getArmorEntityGlint(),
            RenderLayer.getGlint(),
            RenderLayer.getDirectGlint(),
            RenderLayer.getGlintTranslucent(),
            RenderLayer.getEntityGlint(),
            RenderLayer.getDirectEntityGlint()
    ));
    private static final BatchableImmediate ITEM_OVERLAY_BATCH = new BatchableImmediate();

    public static void beginHudBatching() {
        beginFillBatching();
        beginTextureBatching();
        beginTextBatching();
    }

    public static void endHudBatching() {
        endFillBatching();
        endTextureBatching();
        endTextBatching();
    }

    public static void beginItemBatching() {
        beginItemModelBatching();
        beginItemOverlayBatching();
    }

    public static void endItemBatching() {
        endItemModelBatching();
        endItemOverlayBatching();
    }


    public static void beginTextureBatching() {
        TEXTURE_BATCH.close();
        TEXTURE_CONSUMER = TEXTURE_BATCH;
    }

    public static void endTextureBatching() {
        TEXTURE_CONSUMER = null;
        TEXTURE_BATCH.draw();
    }

    public static void beginFillBatching() {
        FILL_BATCH.close();
        FILL_CONSUMER = FILL_BATCH;
    }

    public static void endFillBatching() {
        FILL_CONSUMER = null;
        FILL_BATCH.draw();
    }

    public static void beginTextBatching() {
        TEXT_BATCH.close();
        TEXT_CONSUMER = TEXT_BATCH;
    }

    public static void endTextBatching() {
        TEXT_CONSUMER = null;
        TEXT_BATCH.draw();
    }

    public static void beginItemModelBatching() {
        LIT_ITEM_MODEL_BATCH.close();
        UNLIT_ITEM_MODEL_BATCH.close();
        LIT_ITEM_MODEL_CONSUMER = LIT_ITEM_MODEL_BATCH;
        UNLIT_ITEM_MODEL_CONSUMER = UNLIT_ITEM_MODEL_BATCH;
    }

    public static void endItemModelBatching() {
        LIT_ITEM_MODEL_CONSUMER = null;
        UNLIT_ITEM_MODEL_CONSUMER = null;

        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        DiffuseLighting.disableGuiDepthLighting();
        UNLIT_ITEM_MODEL_BATCH.draw();
        DiffuseLighting.enableGuiDepthLighting();
        LIT_ITEM_MODEL_BATCH.draw();
        RenderSystem.disableBlend();
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

    public static void beginItemOverlayBatching() {
        ITEM_OVERLAY_BATCH.close();
        ITEM_OVERLAY_CONSUMER = ITEM_OVERLAY_BATCH;
    }

    public static void endItemOverlayBatching() {
        ITEM_OVERLAY_CONSUMER = null;
        ITEM_OVERLAY_BATCH.draw();
    }

    /**
     * Creates a map of layer buffers for the given RenderLayer's.
     *
     * @param layers The RenderLayer's for which to create the layer buffers.
     * @return A map of layer buffers for the given RenderLayer's.
     */
    public static Map<RenderLayer, BufferBuilder> createLayerBuffers(final RenderLayer... layers) {
        final Object2ObjectMap<RenderLayer, BufferBuilder> layerBuffers = new Object2ObjectLinkedOpenHashMap<>(layers.length);
        for (final RenderLayer layer : layers) {
            layerBuffers.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
        }
        return layerBuffers;
    }

}