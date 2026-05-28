package me.ariscore;

import me.ariscore.auth.AuthManager;
import me.ariscore.auth.OpProtection;
import me.ariscore.command.CommandRegistry;
import me.ariscore.crate.CrateManager;
import me.ariscore.donate.DonateManager;
import me.ariscore.economy.EconomyManager;
import me.ariscore.kit.KitManager;
import me.ariscore.listener.PlayerLifecycleListener;
import me.ariscore.region.RegionManager;
import me.ariscore.scoreboard.ScoreboardService;
import me.ariscore.shop.ShopGui;
import me.ariscore.storage.PlayerDataManager;
import me.ariscore.storage.StatsManager;
import me.ariscore.tab.TabService;
import me.ariscore.teleport.TeleportManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisCorePlugin extends JavaPlugin {

    private static ArisCorePlugin instance;

    private PlayerDataManager dataManager;
    private StatsManager      statsManager;
    private EconomyManager    economyManager;
    private AuthManager       authManager;
    private OpProtection      opProtection;
    private DonateManager     donateManager;
    private KitManager        kitManager;
    private TeleportManager   teleportManager;
    private RegionManager     regionManager;
    private CrateManager      crateManager;
    private ShopGui           shopGui;
    private ScoreboardService scoreboardService;
    private TabService        tabService;

    private NamespacedKey keyShopItem;
    private NamespacedKey keyDonateRank;
    private NamespacedKey keyKitId;
    private NamespacedKey keyCrate;
    private NamespacedKey keyRegionBlock;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("operators.yml",  false);
        saveResource("donates.yml",    false);
        saveResource("kits.yml",       false);
        saveResource("shop.yml",       false);
        saveResource("crates.yml",     false);
        saveResource("messages.yml",   false);

        keyShopItem    = new NamespacedKey(this, "shop_item");
        keyDonateRank  = new NamespacedKey(this, "donate_rank");
        keyKitId       = new NamespacedKey(this, "kit_id");
        keyCrate       = new NamespacedKey(this, "crate_id");
        keyRegionBlock = new NamespacedKey(this, "region_block");

        dataManager       = new PlayerDataManager(this);
        statsManager      = new StatsManager(this);
        economyManager    = new EconomyManager(this);
        authManager       = new AuthManager(this);
        opProtection      = new OpProtection(this);
        donateManager     = new DonateManager(this);
        kitManager        = new KitManager(this);
        teleportManager   = new TeleportManager(this);
        regionManager     = new RegionManager(this);
        crateManager      = new CrateManager(this);
        shopGui           = new ShopGui(this);
        scoreboardService = new ScoreboardService(this);
        tabService        = new TabService(this);

        opProtection.enforceOnStartup();

        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);
        getServer().getPluginManager().registerEvents(authManager,      this);
        getServer().getPluginManager().registerEvents(opProtection,     this);
        getServer().getPluginManager().registerEvents(donateManager,    this);
        getServer().getPluginManager().registerEvents(kitManager,       this);
        getServer().getPluginManager().registerEvents(teleportManager,  this);
        getServer().getPluginManager().registerEvents(regionManager,    this);
        getServer().getPluginManager().registerEvents(crateManager,     this);
        getServer().getPluginManager().registerEvents(shopGui,          this);
        getServer().getPluginManager().registerEvents(statsManager,     this);

        CommandRegistry.registerAll(this);

        scoreboardService.start();
        tabService.start();

        getLogger().info("ArisCore enabled (Auth+Economy+Kits+Shop+Regions+Donates+Crates+Scoreboard+TAB)");
    }

    @Override
    public void onDisable() {
        if (scoreboardService != null) scoreboardService.stop();
        if (tabService != null) tabService.stop();
        if (dataManager != null) dataManager.saveAll();
        if (regionManager != null) regionManager.save();
        if (crateManager != null) crateManager.save();
    }

    public static ArisCorePlugin get() { return instance; }

    public PlayerDataManager data()          { return dataManager; }
    public StatsManager      stats()         { return statsManager; }
    public EconomyManager    economy()       { return economyManager; }
    public AuthManager       auth()          { return authManager; }
    public OpProtection      ops()           { return opProtection; }
    public DonateManager     donates()       { return donateManager; }
    public KitManager        kits()          { return kitManager; }
    public TeleportManager   teleport()      { return teleportManager; }
    public RegionManager     regions()       { return regionManager; }
    public CrateManager      crates()        { return crateManager; }
    public ShopGui           shop()          { return shopGui; }
    public ScoreboardService scoreboard()    { return scoreboardService; }
    public TabService        tab()           { return tabService; }

    public NamespacedKey keyShopItem()    { return keyShopItem; }
    public NamespacedKey keyDonateRank()  { return keyDonateRank; }
    public NamespacedKey keyKitId()       { return keyKitId; }
    public NamespacedKey keyCrate()       { return keyCrate; }
    public NamespacedKey keyRegionBlock() { return keyRegionBlock; }
}
