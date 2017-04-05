package com.ferreusveritas.rfrotors.util;

import java.util.Random;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class WindManager {

	NoiseGeneratorPerlin noiseGenerator;
	
	public WindManager(){
		noiseGenerator = new NoiseGeneratorPerlin(new Random(5465), 1);
	}
	
	public float getWindSpeed(World world, int x, int y, int z){
		switch(world.provider.dimensionId){
			default:
			case 0: return getOverworldWindSpeed(world, x, y, z);
			case -1: return getNetherWindSpeed(world, x, y, z);
			case 1: return getEndWindSpeed(world, x, y, z);
		}
	}
	
	float getOverworldWindSpeed(World world, int x, int y, int z){
		//The idea here is that the noiseGenerator will make a value as a function of game time between 0.0 and 2.0 with a mean average of 1.0
		// 3/4 of the wind energy comes from linear wind.. the remaining 1/4 of the energy comes from wind gusts
		float basewind = (float) (noiseGenerator.func_151601_a( world.getTotalWorldTime() / 12000.0D, 1.0D) + 1.0D); //Gives 0.0 to 2.0
		float gust = (float) (noiseGenerator.func_151601_a( world.getTotalWorldTime() / 100.0D, 1.0D) + 1.0D); //Gives 0.0 to 2.0
		float wind =  (basewind * 0.75f) + (gust * 0.25f);
		
        int deltaHeight = ModConfiguration.getWindMaxHeight() - ModConfiguration.getWindMinHeight();
        if(deltaHeight <= 0) deltaHeight = 1;

        float heightModifier = (float)Math.min(Math.max(y - ModConfiguration.getWindMinHeight(), 0), deltaHeight) / (float)deltaHeight;

        float weatherModifier = 1.0f;
        if(world.isThundering()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierThunder();
        }
        else if(world.isRaining()) {
            weatherModifier = ModConfiguration.getWeatherMultiplierRain();
        }

		return wind * heightModifier * weatherModifier;
	}
	
	float getNetherWindSpeed(World world, int x, int y, int z){
		if(y > 128){
			return ModConfiguration.getWindAboveNether();
		}
		
		return ModConfiguration.getWindNether();
	}
	
	float getEndWindSpeed(World world, int x, int y, int z){
		return ModConfiguration.getWindEnd();
	}
}
