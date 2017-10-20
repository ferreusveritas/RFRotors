package com.ferreusveritas.rfrotors.lib;

import com.ferreusveritas.rfrotors.blocks.BlockRotor;
import com.google.common.base.Preconditions;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

/**
 * Handles configuration of the mod via a configuration file, and sets up the
 * default configuration if one doesn't exist.
 */
public class ModConfiguration {

	private static Configuration config;
	
	private static float windGenerationBase;
	private static float weatherMultiplierRain;
	private static float weatherMultiplierThunder;
	private static int windMinHeight;
	private static int windMaxHeight;
	private static float windNether;
	private static float windAboveNether;
	private static float windEnd;
	
	private static float generatorEfficiency;
	private static int generatorEnergyStorage;
	private static int generatorEnergyTransferMultiplier;
	
	private static float[] rotorEnergyMultiplier = new float[4];
	private static float angularVelocityPerRF;
	
	public static void init(File pConfigFile) {
		if(pConfigFile != null) {
			config = new Configuration(pConfigFile);
		}
		FMLCommonHandler.instance().bus().register(new ModConfiguration());
		loadConfig();
	}

	/**
	 * Load the configuration file or set defaults if one doesn't exist.
	 * Mostly handled by Forge.
	 */
	private static void loadConfig() {
		Preconditions.checkNotNull(config);
		
		//Wind
		windGenerationBase = config.getFloat("WindGenerationBase", "wind", 64.0f, 0, 1000, "The amount of energy in the wind in RF/t");
		weatherMultiplierRain = config.getFloat("WeatherRainEnergyGenerationMultiplier", "wind", 1.2f, 0.0f, 5.0f, "Multiplier applied to the windmill generation when it's raining");
		weatherMultiplierThunder = config.getFloat("WeatherThunderEnergyGenerationMultiplier", "wind", 1.5f, 0, 5.0f, "Multiplier applied to the windmill generation when it's storming");
		windMinHeight = config.getInt("WindMinHeight", "wind", 60, 0, 255, "Below this height no wind energy is generated");
		windMaxHeight = config.getInt("WindMinHeight", "wind", 100, 0, 255, "Above this height maximum wind energy is generated");
		windNether = config.getFloat("WindNether", "wind", 0.25f, 0.0f, 5.0f, "Constant wind energy level captured in the Nether from height level 0 to 128 inclusive");
		windAboveNether = config.getFloat("WindAboveNether", "wind", 0.25f, 0.0f, 5.0f, "Constant wind energy level captured in the Nether from level 129 to 255 inclusive");
		windEnd = config.getFloat("WindEnd", "wind", 0.0f, 0.0f, 5.0f, "Constant wind energy level captured in the End at any height");
		
		//Generator
		generatorEfficiency = config.getFloat("GeneratorEfficiency", "generator", 0.5f, 0.0f, 1.0f, "How good the generator is at extracting energy from the rotor");
		generatorEnergyStorage = config.getInt("GeneratorBasicEnergyStorage", "generator", 16000, 1000, 16000000, "Energy storage capacity of the generator in RF");
		generatorEnergyTransferMultiplier = config.getInt("GeneratorEnergyTransferMultiplier", "generator", 4, 1, 16, "Multiply by the base wind energy generation to get the rate of energy transfer in RF/t");
		
		//Rotors
		rotorEnergyMultiplier[0] = config.getFloat("RotorSailEnergyMultiplier", "rotors", 0.5f, 0.0f, 10.0f, "Energy multiplier applied to the Sail Wind Rotor");
		rotorEnergyMultiplier[1] = config.getFloat("RotorModernEnergyMultiplier", "rotors", 1.0f, 0.0f, 10.0f, "Energy multiplier applied to the Modern Wind Rotor");
		rotorEnergyMultiplier[2] = config.getFloat("RotorWaterWoodEnergyMultiplier", "rotors", 0.8f, 0.0f, 10.0f, "Energy multiplier applied to the Wooden Water Rotor");
		rotorEnergyMultiplier[3] = config.getFloat("RotorWaterIronEnergyMultiplier", "rotors", 1.0f, 0.0f, 10.0f, "Energy multiplier applied to the Iron Water Rotor");
		angularVelocityPerRF = config.getFloat("AngularVelocityPerRF", "rotors", 0.25f, 0.0f, 10.0f, "Degrees per RF per tick that the rotor rotates by");
		
		if(config.hasChanged()) {
			config.save();
		}
	}
	
	//Wind
	
	public static float getWindGenerationBase() {
		return windGenerationBase;
	}
	
	public static float getWeatherMultiplierRain() {
		return weatherMultiplierRain;
	}
	
	public static float getWeatherMultiplierThunder() {
		return weatherMultiplierThunder;
	}
	
	public static int getWindMinHeight() {
		return windMinHeight;
	}
	
	public static int getWindMaxHeight() {
		return windMaxHeight;
	}
	
	public static float getWindNether(){
		return windNether;
	}
	
	public static float getWindAboveNether(){
		return windAboveNether;
	}
	
	public static float getWindEnd(){
		return windEnd;
	}
	
	
	//Generator
	
	public static float getGeneratorEfficiency() {
		return generatorEfficiency;
	}
	
	public static int getGeneratorEnergyStorage() {
		return generatorEnergyStorage;
	}
	
	public static int getGeneratorEnergyTransferMultiplier() {
		return generatorEnergyTransferMultiplier;
	}
	
	//Rotors
	
	public static float getRotorEnergyMultiplier(BlockRotor.EnumType pType) {
		return rotorEnergyMultiplier[pType.getMetadata()];
	}
	
	public static float getAngularVelocityPerRF() {
		return angularVelocityPerRF;
	}
}
