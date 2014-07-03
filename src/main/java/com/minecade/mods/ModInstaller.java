package com.minecade.mods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ModInstaller {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    public final static String version = "1.7.2";
    public final static String stage = "Release";
    public static String installation = "Minecade Mod";

    public static void main(String[] args) {
        System.out.println("Loading Mod Installer for version " + version);
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // handle exception
        }

        Frame frame = new Frame();
        frame.setVisible(true);
    }

    public static void install() throws IOException {
        System.out.println("Installing " + installation);
        File minecraftFolder;
        if (isMac()) {
            System.out.println("Using: Mac");
            minecraftFolder = new File(System.getProperty("user.home"), "Library/Application Support/minecraft");
        } else if (isWindows()) {
            System.out.println("Using: Windows");
            minecraftFolder = new File(System.getenv("APPDATA"), ".minecraft");
        } else {
            System.out.println("Using: Linux");
            // I guess it's a linux of sorts
            minecraftFolder = new File(System.getProperty("user.home"), ".minecraft");
        }
        System.out.println("Found Minecraft Folder: " + minecraftFolder.getAbsolutePath());
        File versionFolder = new File(minecraftFolder, "versions");
        File profiles = new File(minecraftFolder, "launcher_profiles.json");
        File minecadeFolder = new File(versionFolder, "MinecadeMod-" + version);
        if (!profiles.exists()) {
            JOptionPane.showMessageDialog(Frame.getInstance(), "You need to play Minecraft first!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        if (!minecadeFolder.exists() && !minecadeFolder.mkdir()) {
            JOptionPane.showMessageDialog(Frame.getInstance(), "Failed to create Minecade folder!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        minecadeFolder.deleteOnExit();
        // Looks good!
        try {
            File temp = new File(System.getenv("APPDATA"), "MinecadeModInstaller");
            if (!temp.exists() && !temp.mkdir()) {
                JOptionPane.showMessageDialog(Frame.getInstance(), "Failed to create Minecade Mod folder!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                return;
            } else if (temp.exists()) {
                for (File file : temp.listFiles()) {
                    file.delete();
                }
            }
            File source = new File(temp, "sources");
            // let's load our json file first
            IOUtils.copy(ModInstaller.class.getClassLoader().getResourceAsStream("MinecadeMod-" + version + ".json"), new FileOutputStream(new File(minecadeFolder, "MinecadeMod-1.7.2.json")));
            // now, let's load our Minecade Mod files
            IOUtils.copy(ModInstaller.class.getClassLoader().getResourceAsStream("MinecadeMod_" + version + ".zip"), new FileOutputStream(new File(temp, "minecademod.zip")));
            extract(new File(temp, "minecademod.zip"), source);

            File jar = new File(minecadeFolder, "MinecadeMod-" + version + ".jar");
            if (installation.toLowerCase().contains("optifine")) {
                // we're installing optifine, let's add it to our source pool
                IOUtils.copy(ModInstaller.class.getClassLoader().getResourceAsStream("Optifine_" + version + ".zip"), new FileOutputStream(new File(temp, "optifine.zip")));
                extract(new File(temp, "optifine.zip"), source);
            }
            if (installation.toLowerCase().contains("camstudio")) {
                // we're installing camstudio, we need to download it first however. It doesn't come packaged.
                File camstudio = new File(temp, "camstudio" + version + ".zip");
                if (!camstudio.exists()) {
                    System.out.println(camstudio + " not found, downloading now.");
                    // so we don't have to download it multiple times, let's use the cached version
                    Frame.getInstance().download();
                    FileUtils.copyURLToFile(new URL("http://paulbgd.me/storage/modinstaller/CamStudio_" + version + ".zip"), camstudio);
                    Frame.getInstance().update();
                }
                extract(camstudio, source);
            }

            try {
                Packager.packZip(jar, Arrays.asList(source.listFiles()));
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(Frame.getInstance(), "Failed to pack!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                return;
            }

            // now for adding the profile
            JSONParser parser = new JSONParser();
            File launcherProfiles = new File(minecraftFolder, "launcher_profiles.json");
            JSONObject object = (JSONObject) parser.parse(new FileReader(launcherProfiles));
            JSONObject profileObject = (JSONObject) object.get("profiles");

            JSONObject minecadeMod = new JSONObject();
            minecadeMod.put("name", "MinecadeMod-" + version);
            minecadeMod.put("lastVersionId", "MinecadeMod-" + version);
            profileObject.put("MinecadeMod-" + version, minecadeMod);
            object.put("profiles", profileObject);

            System.out.println(object.toJSONString());
            FileWriter fw = new FileWriter(launcherProfiles.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(object.toJSONString());
            bw.close();

            JOptionPane.showMessageDialog(Frame.getInstance(), "You have finished installing the " + installation + " for version " + version + "!", "Installed!", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        } catch (Exception e) {
            System.exit(0);
            minecadeFolder.deleteOnExit();
            JOptionPane.showMessageDialog(Frame.getInstance(), e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
    }

    public static String getOS() {
        if (isMac()) {
            return "Mac";
        } else if (isWindows()) {
            return "Win";
        } else {
            return "Linux";
        }
    }

    public static void extract(File zip, File destinationFolder) {
        try {
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(new FileInputStream(zip));

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                String entryName = zipentry.getName();
                File newFile = new File(destinationFolder, entryName);
                int n;
                FileOutputStream fileoutputstream;
                if (newFile.getParent() != null && !newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }

                if (zipentry.isDirectory()) {
                    newFile.mkdir();
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                fileoutputstream = new FileOutputStream(new File(destinationFolder, entryName));

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
                    fileoutputstream.write(buf, 0, n);

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }

            zipinputstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

}
