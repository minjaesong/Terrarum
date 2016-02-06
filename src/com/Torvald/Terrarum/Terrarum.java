package com.Torvald.Terrarum;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.Torvald.ImageFont.GameFontWhite;
import com.Torvald.Terrarum.GameControl.GameController;
import com.Torvald.Terrarum.GameControl.KeyMap;
import com.Torvald.Terrarum.LangPack.Lang;
import org.newdawn.slick.*;
import org.newdawn.slick.state.StateBasedGame;

/**
 * Created by minjaesong on 15-12-30.
 */
public class Terrarum extends StateBasedGame {

    public static AppGameContainer appgc;
    public static final int WIDTH = 960;
    public static final int HEIGHT = 720;
    public static Game game;

    public static String OSName;
    public static String OSVersion;
    public static String OperationSystem;
    public static String defaultDir;
    public static String defaultSaveDir;

    public static String gameLocale = "ko";

    public static Font gameFontWhite;

    public static final int SCENE_ID_HOME = 1;
    public static final int SCENE_ID_GAME = 3;

    public Terrarum(String gamename) throws SlickException {
        super(gamename);

        getDefaultDirectory();
        createDirs();
        try {
            createFiles();
            new Lang();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initStatesList(GameContainer gameContainer) throws SlickException {
        gameFontWhite = new GameFontWhite();

        game = new Game();
        addState(game);
    }

    public static void main(String[] args)
    {
        try
        {
            appgc = new AppGameContainer(new Terrarum("Terrarum"));
            appgc.setDisplayMode(WIDTH, HEIGHT, false);
            appgc.setTargetFrameRate(Game.TARGET_INTERNAL_FPS);
            appgc.setVSync(true);
            appgc.setShowFPS(false);
            appgc.setUpdateOnlyWhenVisible(false);
            appgc.setMaximumLogicUpdateInterval(1000 / Game.TARGET_INTERNAL_FPS);
            appgc.start();
        }
        catch (SlickException ex)
        {
            Logger.getLogger(Terrarum.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getDefaultDirectory(){
        OSName = System.getProperty("os.name");
        OSVersion = System.getProperty("os.version");

        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")){
            OperationSystem = "WINDOWS";
            defaultDir = System.getenv("APPDATA") + "/Terrarum";
        }
        else if (OS.contains("OS X")){
            OperationSystem = "OSX";
            defaultDir = System.getProperty("user.home") + "/Library/Application "
                    + "Support" + "/Terrarum";
        }
        else if (OS.contains("NUX") || OS.contains("NIX")){
            OperationSystem = "LINUX";
            defaultDir = System.getProperty("user.home") + "/.terrarum";
        }
        else if (OS.contains("SUNOS")){
            OperationSystem = "SOLARIS";
            defaultDir = System.getProperty("user.home") + "/.terrarum";
        }
        else{
            OperationSystem = "UNKNOWN";
            defaultDir = System.getProperty("user.home") + "/.terrarum";
        }

        defaultSaveDir = defaultDir + "/Saves";
    }

    private static void createDirs(){
        File[] dirs = {
                new File(defaultSaveDir),
        };

        for (File d : dirs){
            if (!d.exists()){
                d.mkdirs();
            }
        }
    }

    private static void createFiles() throws IOException {
        File[] files = {
                new File(defaultDir + "/properties")
        };

        for (File f : files){
            if (!f.exists()){
                f.createNewFile();
            }
        }
    }
}
