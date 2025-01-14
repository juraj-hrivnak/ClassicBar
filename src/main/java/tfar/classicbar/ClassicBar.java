package tfar.classicbar;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import tfar.classicbar.config.ModConfig;
import tfar.classicbar.network.SyncHandler;
import tfar.classicbar.overlays.modoverlays.*;
import tfar.classicbar.overlays.vanillaoverlays.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(Side.CLIENT)
@Mod(modid = ClassicBar.MODID, name = ClassicBar.MODNAME, version = ClassicBar.MODVERSION,
        useMetadata = true,dependencies = ClassicBar.DEPENDENCIES,clientSideOnly = true)
public class ClassicBar {

  public static final String MODID = "classicbar";
  public static final String MODNAME = "Classic Bar";
  public static final String MODVERSION = "@VERSION@";
  public static final String DEPENDENCIES = "after:randomtweaks@[1.12.2-2.7.1.0,);";

  public static final String[] problemMods = new String[]{"mantle", "toughasnails"};

  public static final boolean TOUGHASNAILS = Loader.isModLoaded("toughasnails");
  public static final boolean IBLIS = Loader.isModLoaded("iblis");
  public static final boolean BAUBLES = Loader.isModLoaded("baubles");
  public static final boolean RANDOMTWEAKS = Loader.isModLoaded("randomtweaks");
  public static final boolean BETWEENLANDS = Loader.isModLoaded("thebetweenlands");

  public static final boolean VAMPIRISM = Loader.isModLoaded("vampirism");

  public static final boolean HUNGERCHANGED = IBLIS || RANDOMTWEAKS;


  public static Logger logger;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    SyncHandler.init();
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {

    MinecraftForge.EVENT_BUS.register(new ModConfig.ConfigEventHandler());
    //Register renderers for events
    ClassicBar.logger.info("Registering Vanilla Overlay");
    MinecraftForge.EVENT_BUS.register(new EventHandler());

    EventHandler.registerAll(new AbsorptionRenderer(),new AirRenderer(), new ArmorRenderer(), new ArmorToughnessRenderer(),
            new HealthRenderer(), new HungerRenderer(), new MountHealthRenderer());

    //mod renderers
    ClassicBar.logger.info("Registering Mod Overlays");
    if (Loader.isModLoaded("randomthings")) EventHandler.register(new LavaCharmRenderer());
    if (Loader.isModLoaded("lavawaderbauble")) EventHandler.register(new LavaWaderBaubleRenderer());
    if (BETWEENLANDS) EventHandler.register(new DecayRenderer());
    //if (Loader.isModLoaded("superiorshields"))
    //  MinecraftForge.EVENT_BUS.register(new SuperiorShieldRenderer());
    if (TOUGHASNAILS) EventHandler.register(new ThirstBarRenderer());
    if (Loader.isModLoaded("botania")) EventHandler.register(new TiaraBarRenderer());
    if (VAMPIRISM) EventHandler.register(new VampireRenderer());
    EventHandler.setup();

    boolean areProblemModsPresent = Arrays.stream(problemMods).anyMatch(Loader::isModLoaded);
    if (areProblemModsPresent) {
      logger.info("Unregistering problematic overlays.");
      ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;
      try {
        Field f = EventBus.class.getDeclaredField("listeners");
        f.setAccessible(true);
        listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) f.get(MinecraftForge.EVENT_BUS);
        listeners.keySet().forEach(key -> {
          String s = key.getClass().getCanonicalName();
          if ("slimeknights.mantle.client.ExtraHeartRenderHandler".equals(s)) {
            logger.info("Unregistered Mantle bar");
            MinecraftForge.EVENT_BUS.unregister(key);
          }
          else if ("toughasnails.handler.thirst.ThirstOverlayHandler".equals(s)) {
            logger.info("Unregistered Thirst bar");
            MinecraftForge.EVENT_BUS.unregister(key);
          }
        });
      } catch (IllegalAccessException | NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
  }
}