package unsafedodo.guishop.util;

import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.config.ConfigData;
import unsafedodo.guishop.config.ConfigManager;
import unsafedodo.guishop.shop.Shop;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Runnable command to save shops to guishop.json
 * - Once initialized, it will be scheduled to run every 30 minutes
 * - The command can be triggered using the `guishop forcesave` command
 * - The command will be triggered on server shutdown
 */
public class ShopFileHandler implements Runnable{

    private static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

    private static final long TIME = 30;

    public boolean initialize(){
        try {
            executorService.scheduleAtFixedRate(this, TIME, TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void saveToFile() throws IOException {
        Shop[] shops = new Shop[GUIShop.shops.size()];
        String jsonString = ConfigManager.GSON.toJson(new ConfigData(GUIShop.shops.toArray(shops)));

        File configDir = Paths.get("", "config").toFile();
        File configFile = new File(configDir, "guishop.json");

        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            writer.write(jsonString);
            writer.close();
        }
    }

    public void killTask(){
        executorService.shutdown();
    }

    @Override
    public void run() {
        try {
            saveToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
