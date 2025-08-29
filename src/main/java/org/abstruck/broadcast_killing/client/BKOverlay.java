package org.abstruck.broadcast_killing.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.abstruck.broadcast_killing.BroadcastKilling;

import java.util.*;

@Mod.EventBusSubscriber(modid = BroadcastKilling.MOD_ID)
public class BKOverlay {
    private static final List<BKEntry> entries = new ArrayList<>();
    private static final int ENTRY_DURATION = 5000;
    private static final int ANIMATION_DURATION = 1200;
    private static final int ENTRY_SPACING = 15;

    private static int configYOffset = 15;
    private static int configMaxEntries = 3;

    private static final Map<BKEntry, Float> currentYPositions = new HashMap<>();
    private static final Map<BKEntry, Integer> targetYPositions = new HashMap<>();

    public static void addEntry(String killer, String victim, ItemStack weapon,
                                boolean isPlayerKill, boolean isPlayerVictim) {

        removeExpiredEntries();

        if (entries.size() >= configMaxEntries) {
            BKEntry oldest = entries.remove(0);
            currentYPositions.remove(oldest);
            targetYPositions.remove(oldest);

            updateAllTargetPositions();
        }

        long currentTime = System.currentTimeMillis();
        BKEntry entry = new BKEntry(
                killer, victim, weapon,
                isPlayerKill, isPlayerVictim,
                currentTime
        );
        entries.add(entry);

        int targetY = configYOffset + (entries.size() - 1) * ENTRY_SPACING;
        targetYPositions.put(entry, targetY);
        currentYPositions.put(entry, (float)(targetY + ENTRY_SPACING));
    }

    private static void removeExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        Iterator<BKEntry> iterator = entries.iterator();

        while (iterator.hasNext()) {
            BKEntry entry = iterator.next();
            long elapsed = currentTime - entry.timestamp;

            if (elapsed > ENTRY_DURATION + ANIMATION_DURATION) {
                iterator.remove();
                currentYPositions.remove(entry);
                targetYPositions.remove(entry);

                updateAllTargetPositions();
            }
        }
    }

    private static void updateAllTargetPositions() {
        int startY = configYOffset;
        for (int i = 0; i < entries.size(); i++) {
            BKEntry entry = entries.get(i);
            targetYPositions.put(entry, startY + i * ENTRY_SPACING);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (entries.isEmpty()) return;

        for (BKEntry entry : entries) {
            Float currentY = currentYPositions.get(entry);
            Integer targetY = targetYPositions.get(entry);

            if (currentY != null && targetY != null) {
                float diff = targetY - currentY;
                if (Math.abs(diff) > 0.5f) {
                    float speedFactor = Math.min(0.3f, Math.abs(diff) / 10f);
                    float newY = currentY + (diff * speedFactor);
                    currentYPositions.put(entry, newY);
                } else {
                    currentYPositions.put(entry, (float)targetY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        event.getGuiGraphics().setColor(1, 1, 1, 1);

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Window window = minecraft.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < entries.size(); i++) {
            BKEntry entry = entries.get(i);
            long elapsed = currentTime - entry.timestamp;

            Float currentY = currentYPositions.get(entry);
            if (currentY == null) continue;

            boolean isExpired = elapsed > ENTRY_DURATION;
            float animationProgress = isExpired ?
                    Math.min(1.0f, (float)(elapsed - ENTRY_DURATION) / ANIMATION_DURATION) : 0f;

            float easedProgress = easeOutQuad(animationProgress);
            int alpha = 255;
            if (elapsed < 200) {
                alpha = (int)(255 * (elapsed / 200f));
            } else if (isExpired) {
                alpha = (int)(255 * (1 - easedProgress));
            }

            float slideOffset = isExpired ? easedProgress * 25f : 0f;
            float yPos = currentY - slideOffset;

            renderKillEntry(guiGraphics, minecraft, entry, screenWidth, screenHeight, (int)yPos, alpha);
        }

        guiGraphics.setColor(1, 1, 1, 1);
    }

    private static float easeOutQuad(float progress) {
        return 1 - (1 - progress) * (1 - progress);
    }

    private static void renderKillEntry(GuiGraphics guiGraphics, Minecraft minecraft,
                                        BKEntry entry, int screenWidth, int screenHeight,
                                        int y, int alpha) {
        if (alpha < 5) return;

        int goldColor = 0xFFFF55 | (alpha << 24);
        int whiteColor = 0xFFFFFF | (alpha << 24);
        int shadowColor = 0x000000 | (alpha << 24);

        Font font = minecraft.font;

        int killerWidth = font.width(entry.killer);
        int victimWidth = font.width(entry.victim);
        int totalWidth = killerWidth + victimWidth + 35;

        int x = screenWidth - totalWidth - 10;

        x = Math.max(5, Math.min(x, screenWidth - totalWidth - 5));

        drawTextWithShadow(guiGraphics, font, entry.killer,
                x, y,
                entry.isPlayerKill ? goldColor : whiteColor,
                shadowColor
        );

        renderWeaponIcon(guiGraphics, entry.weapon, x + killerWidth + 10, y - 4, alpha);

        drawTextWithShadow(guiGraphics, font, entry.victim,
                x + killerWidth + 35, y,
                entry.isPlayerVictim ? goldColor : whiteColor,
                shadowColor
        );
    }

    private static void drawTextWithShadow(GuiGraphics guiGraphics, Font font, String text,
                                           int x, int y, int color, int shadowColor) {
        guiGraphics.drawString(font, text, x + 1, y + 1, shadowColor, false);
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    private static void renderWeaponIcon(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int alpha) {
        if (stack.isEmpty() || alpha < 5) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        try {
            float scale = 0.7f;
            poseStack.translate(x, y, 0);
            poseStack.scale(scale, scale, 1);

            guiGraphics.setColor(1, 1, 1, alpha/255f);

            guiGraphics.renderItem(stack, 0, 0);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, 0, 0);
        } finally {
            poseStack.popPose();
            guiGraphics.setColor(1, 1, 1, 1); // Alpha=1.0 (不透明)
        }
    }

    private static class BKEntry {
        final String killer;
        final String victim;
        final ItemStack weapon;
        final boolean isPlayerKill;
        final boolean isPlayerVictim;
        final long timestamp;

        float yOffset = 0.0f;
        float scale = 1.0f;
        int animStartTime = -1;

        public BKEntry(String killer, String victim, ItemStack weapon,
                       boolean isPlayerKill, boolean isPlayerVictim, long timestamp) {
            this.killer = killer;
            this.victim = victim;
            this.weapon = weapon;
            this.isPlayerKill = isPlayerKill;
            this.isPlayerVictim = isPlayerVictim;
            this.timestamp = timestamp;
        }
    }
}
