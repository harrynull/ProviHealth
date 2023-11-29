package com.provismet.provihealth.config;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalEntityTypeTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.provismet.provihealth.ProviHealthClient;

public class Options {
    public static boolean shouldRenderHUD = true;
    public static int maxHealthBarTicks = 40;

    public static List<String> blacklist = Arrays.asList("minecraft:armor_stand");
    public static List<String> blacklistHUD = Arrays.asList("minecraft:armor_stand");

    public static VisibilityType bosses = VisibilityType.ALWAYS_HIDE;
    public static VisibilityType hostile = VisibilityType.ALWAYS_SHOW;
    public static VisibilityType players = VisibilityType.HIDE_IF_FULL;
    public static VisibilityType others = VisibilityType.HIDE_IF_FULL;

    public static boolean bossesVisibilityOverride = false;
    public static boolean hostileVisibilityOverride = true;
    public static boolean playersVisibilityOverride = true;
    public static boolean othersVisibilityOverride = true;

    public static HUDType bossHUD = HUDType.FULL;
    public static HUDType hostileHUD = HUDType.FULL;
    public static HUDType playerHUD = HUDType.FULL;
    public static HUDType otherHUD = HUDType.FULL;

    public static float hudGlide = 0.5f;
    public static float worldGlide = 0.5f;

    @SuppressWarnings("resource")
    public static boolean shouldRenderHealthFor (LivingEntity livingEntity) {
        if (blacklist.contains(EntityType.getId(livingEntity.getType()).toString())) return false;

        Entity target = MinecraftClient.getInstance().targetedEntity;
        if (livingEntity.getType().isIn(ConventionalEntityTypeTags.BOSSES)) {
            if (bossesVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(bosses, livingEntity);
        }
        else if (livingEntity instanceof HostileEntity) {
            if (hostileVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(hostile, livingEntity);
        }
        else if (livingEntity instanceof PlayerEntity) {
            if (playersVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(players, livingEntity);
        }
        else {
            if (othersVisibilityOverride && livingEntity == target) return true;
            return shouldRenderHealthFor(others, livingEntity);
        }
    }

    public static HUDType getHUDFor (LivingEntity livingEntity) {
        if (blacklistHUD.contains(EntityType.getId(livingEntity.getType()).toString())) return HUDType.NONE;
        else if (livingEntity.getType().isIn(ConventionalEntityTypeTags.BOSSES)) return bossHUD;
        else if (livingEntity instanceof HostileEntity) return hostileHUD;
        else if (livingEntity instanceof PlayerEntity) return playerHUD;
        else return otherHUD;
    }

    public static void save () {
        JsonHelper json = new JsonHelper();
        String jsonData = json.start()
            .append("hudDuration", maxHealthBarTicks).newLine()
            .append("hudGlide", hudGlide).newLine()
            .append("worldGlide", worldGlide).newLine()
            .append("bossHealth", bosses.name()).newLine()
            .append("bossTarget", bossesVisibilityOverride).newLine()
            .append("hostileHealth", hostile.name()).newLine()
            .append("hostileTarget", hostileVisibilityOverride).newLine()
            .append("playerHealth", players.name()).newLine()
            .append("playerTarget", playersVisibilityOverride).newLine()
            .append("otherHealth", others.name()).newLine()
            .append("otherTarget", othersVisibilityOverride).newLine()
            .append("bossHUD", bossHUD.name()).newLine()
            .append("hostileHUD", hostileHUD.name()).newLine()
            .append("playerHUD", playerHUD.name()).newLine()
            .append("otherHUD", otherHUD.name()).newLine()
            .createArray("healthBlacklist", blacklist).newLine()
            .createArray("hudBlacklist", blacklistHUD).newLine(false)
            .closeObject()
            .toString();

        try {
            FileWriter writer = new FileWriter("config/provihealth.json");
            writer.write(jsonData);
            writer.close();
        }
        catch (IOException e) {
            ProviHealthClient.LOGGER.error("Error whilst saving config: ", e);
        }
    }

    public static void load () {
        try {
            FileReader reader = new FileReader("config/provihealth.json");
            JsonReader parser = new JsonReader(reader);

            parser.beginObject();
            while (parser.hasNext()) {
                final String label = parser.nextName();

                switch (label) {
                    case "hudDuration":
                        maxHealthBarTicks = parser.nextInt();
                        break;
                    
                    case "hudGlide":
                        hudGlide = (float)parser.nextDouble();
                        break;
                    
                    case "worldGlide":
                        worldGlide = (float)parser.nextDouble();
                        break;

                    case "bossHealth":
                        bosses = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "bossTarget":
                        bossesVisibilityOverride = parser.nextBoolean();
                        break;

                    case "bossHUD":
                        bossHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "hostileHealth":
                        hostile = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "hostileTarget":
                        hostileVisibilityOverride = parser.nextBoolean();
                        break;
                    
                    case "hostileHUD":
                        hostileHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "playerHealth":
                        players = VisibilityType.valueOf(parser.nextString());
                        break;
                    
                    case "playerTarget":
                        playersVisibilityOverride = parser.nextBoolean();
                        break;

                    case "playerHUD":
                        playerHUD = HUDType.valueOf(parser.nextString());
                        break;
                    
                    case "otherHealth":
                        others = VisibilityType.valueOf(parser.nextString());
                        break;

                    case "otherTarget":
                        othersVisibilityOverride = parser.nextBoolean();
                        break;

                    case "otherHUD":
                        otherHUD = HUDType.valueOf(parser.nextString());
                        break;

                    case "healthBlacklist":
                        ArrayList<String> tempBlacklist = new ArrayList<>();
                        parser.beginArray();
                        while (parser.hasNext()) {
                            tempBlacklist.add(parser.nextString());
                        }
                        parser.endArray();
                        blacklist = tempBlacklist;
                        break;

                    case "hudBlacklist":
                        ArrayList<String> tempHudBlacklist = new ArrayList<>();
                        parser.beginArray();
                        while (parser.hasNext()) {
                            tempHudBlacklist.add(parser.nextString());
                        }
                        parser.endArray();
                        blacklistHUD = tempHudBlacklist;
                        break;
                
                    default:
                        break;
                }
            }
            parser.close();
        }
        catch (FileNotFoundException e) {
            ProviHealthClient.LOGGER.info("No config found, creating new one.");
            save();
        }
        catch (IOException e2) {
            ProviHealthClient.LOGGER.error("Error whilst parsing config: ", e2);
        }
    }

    private static boolean shouldRenderHealthFor (VisibilityType type, LivingEntity livingEntity) {
        switch (type) {
            case ALWAYS_HIDE:
                return false;
        
            case HIDE_IF_FULL:
                if (livingEntity.getHealth() < livingEntity.getMaxHealth()) return true;
                else if (livingEntity.hasVehicle()) {
                    Entity vehicle = livingEntity.getVehicle();
                    while (vehicle != null) {
                        if (vehicle instanceof LivingEntity livingVehicle) {
                            if (livingVehicle.getHealth() < livingVehicle.getMaxHealth()) return true;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                return false;
            
            case ALWAYS_SHOW:
                return true;
            
            default:
                return true;
        }
    }

    public static enum VisibilityType {
        ALWAYS_HIDE,
        HIDE_IF_FULL,
        ALWAYS_SHOW;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }

    public static enum HUDType {
        NONE,
        PORTRAIT_ONLY,
        FULL;

        @Override
        public String toString () {
            return "enum.provihealth." + super.toString().toLowerCase();
        }
    }
}
