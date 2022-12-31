package tech.goksi.remoteconsole.utility.versioncontrol;


import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import tech.goksi.remoteconsole.RemoteConsole;
import tech.goksi.remoteconsole.helpers.GsonSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VersionControlUtility {
    private static final String API_URL = "https://api.github.com/repos/GoksiOrg/RemoteConsolePlugin/releases/latest";
    private static final Logger logger = RemoteConsole.getInstance().getLogger();

    private VersionControlUtility() {
    }

    public static void checkVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(RemoteConsole.getInstance(), () -> {
            SemVer currentVer = new SemVer(RemoteConsole.getInstance().getDescription().getVersion());
            if ("canary".equals(currentVer.getVersion())) {
                logger.warning("You are running canary version of RemoteConsole !");
                return;
            }
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "application/vnd.github+json");
                if (connection.getResponseCode() != 200) return;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String result = reader.lines().collect(Collectors.joining("\n"));
                JsonObject fetchedObject = GsonSingleton.getInstance().fromJson(result, JsonObject.class);
                SemVer fetchedVer;
                if ((fetchedVer = new SemVer(fetchedObject.get("tag_name").getAsString())).compareTo(currentVer) > 0) { // TODO check
                    logger.warning(String.format("You are not running latest version of RemoteConsole. Current: %s Latest: %s",
                            "v" + currentVer.getVersion(), fetchedVer.getVersion()));
                } else logger.info("You are running latest version of RemoteConsole !");

            } catch (IOException exception) {
                logger.log(Level.WARNING, "Unexpected error while trying to check plugin version !", exception);
            }
        });
    }


}
