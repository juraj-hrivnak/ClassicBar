package tfar.classicbar.overlays.vanilla;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.gui.ForgeIngameGui;
import tfar.classicbar.util.Color;
import tfar.classicbar.impl.BarOverlayImpl;
import tfar.classicbar.util.ColorUtils;
import tfar.classicbar.util.HealthEffect;
import tfar.classicbar.util.ModUtils;

import static tfar.classicbar.util.ModUtils.*;
import static tfar.classicbar.config.ClassicBarsConfig.showHealthNumbers;

public class Health extends BarOverlayImpl {

  private double playerHealth = 0;
  private long healthUpdateCounter = 0;
  private double lastPlayerHealth = 0;

  public Health() {
    super("health");
  }

  @Override
  public boolean shouldRender(Player player) {
    return true;
  }

  @Override
  public void renderBar(ForgeIngameGui gui, PoseStack stack, Player player, int screenWidth, int screenHeight, int vOffset) {
    int updateCounter = gui.getGuiTicks();

    double health = player.getHealth();
    boolean highlight = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3 % 2 == 1;

    //player is damaged and resistant
    if (health < playerHealth && player.invulnerableTime > 0) {
      healthUpdateCounter = updateCounter + 20;
      lastPlayerHealth = playerHealth;
    } else if (health > playerHealth && player.invulnerableTime > 0) {
      healthUpdateCounter = updateCounter + 10;
      /* lastPlayerHealth = playerHealth;*/
    }
    playerHealth = health;
    double displayHealth = health + (lastPlayerHealth - health) * ((double) player.invulnerableTime / player.invulnerableDuration);

    int xStart = screenWidth / 2 + getHOffset();
    int yStart = screenHeight - vOffset;
    double maxHealth = player.getAttribute(Attributes.MAX_HEALTH).getValue();

    HealthEffect effect = getHealthEffect(player);

    int i4 = (highlight) ? 18 : 0;

    //Bar background
    drawTexturedModalRect(stack,xStart, yStart, 0, i4, 81, 9);

    double f = xStart + (rightHandSide() ? ModUtils.WIDTH - ModUtils.getWidth(health, maxHealth) : 0);

    //is the bar changing
    //Pass 1, draw bar portion
    //interpolate the bar
    if (displayHealth != health) {
      //reset to white
      Color.reset();
      if (displayHealth > health) {
        //draw interpolation
        drawTexturedModalRect(stack,f + 1, yStart + 1, 1, 10, getWidth(displayHealth, maxHealth), 7);
        //Health is increasing, IDK what to do here
      } else {/*
                  f = xStart + getWidth(health, maxHealth);
                  drawTexturedModalRect(f, yStart + 1, 1, 10, getWidth(health - displayHealth, maxHealth), 7, general.style, true, true);*/
      }
    }
    //calculate bar color
    Color primary = getPrimaryBarColor(0,player);

    primary.color2Gl();
    //draw portion of bar based on health remaining
    drawTexturedModalRect(stack,f + 2, yStart + 1, 2, 10, getWidth(health, maxHealth), 7);

    if (effect == HealthEffect.POISON) {
      //draw poison overlay
      RenderSystem.setShaderColor(0, .5f, 0, .5f);
      drawTexturedModalRect(stack,f + 1, yStart + 1, 1, 36, getWidth(health, maxHealth), 7);
    }

    Color.reset();
  }

  HealthEffect getHealthEffect(Player player) {
    HealthEffect effects = HealthEffect.NONE;//16
    if (player.hasEffect(MobEffects.POISON)) effects = HealthEffect.POISON;//evaluates to 52
    else if (player.hasEffect(MobEffects.WITHER)) effects = HealthEffect.WITHER;//evaluates to 88
    return effects;
  }

  @Override
  public Color getPrimaryBarColor(int index, Player player) {
    double health = player.getHealth();
    double maxHealth = player.getAttribute(Attributes.MAX_HEALTH).getValue();
    HealthEffect effect = getHealthEffect(player);
    return ColorUtils.calculateScaledColor(health,maxHealth,effect);
  }

  @Override
  public boolean shouldRenderText() {
    return showHealthNumbers.get();
  }

  @Override
  public void renderText(PoseStack stack,Player player, int width, int height, int vOffset) {
    double health = player.getHealth();

    int xStart = width / 2 + getIconOffset();
    int yStart = height - vOffset;
    textHelper(stack,xStart,yStart,health,getPrimaryBarColor(0,player).colorToText());
  }

  @Override
  public void renderIcon(PoseStack stack, Player player, int width, int height, int vOffset) {
    HealthEffect effect = getHealthEffect(player);

    int xStart = width / 2 + getIconOffset();
    int yStart = height - vOffset;
    int i5 = (player.level.getLevelData().isHardcore()) ? 5 : 0;
    //Draw health icon
    //heart background
    Color.reset();
    drawTexturedModalRect(stack,xStart, yStart, 16, 9 * i5, 9, 9);
    //heart
    drawTexturedModalRect(stack,xStart, yStart, 36 + effect.i, 9 * i5, 9, 9);
  }
}