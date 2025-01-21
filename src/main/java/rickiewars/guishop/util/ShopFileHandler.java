package rickiewars.guishop.util;

import rickiewars.guishop.GUIShop;
import rickiewars.guishop.config.ConfigManager;

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

    private static final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

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
        String jsonString = ConfigManager.GSON.toJson(GUIShop.config);

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
