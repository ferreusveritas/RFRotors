package com.ferreusveritas.rfrotors;

public class ModConstants {
		
    public static final String MODID = "rfrotors";
    public static final String NAME = "RFRotors";
	public static final String VERSION = "1.12.2-9999.9999.9999z";//Maxed out version to satisfy dependencies during dev, Assigned from gradle during build, do not change
    //public static final String VERSION = "0.5b";
    public static final String CLIENT_PROXY_CLASS = "com.ferreusveritas.rfrotors.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "com.ferreusveritas.rfrotors.proxy.CommonProxy";
    
    public static final String THERMALEXPANSION = "thermalexpansion";
    public static final String THERMALFOUNDATION = "thermalfoundation";
	
	public static final String OPTAFTER = "after:";
	public static final String OPTBEFORE = "before:";
	public static final String REQAFTER = "required-after:";
	public static final String REQBEFORE = "required-before:";
	public static final String NEXT = ";";
	public static final String AT = "@[";
	public static final String GREATERTHAN = "@(";
	public static final String ORGREATER = ",)";
	
	//Forge
	private static final String FORGE = "forge";
	public static final String FORGE_VER = FORGE + AT + "14.23.5.2768" + ORGREATER;
	
	public static final String DEPENDENCIES
		= REQAFTER + FORGE_VER
		+ NEXT
		+ REQAFTER + THERMALEXPANSION
		+ NEXT
		+ REQAFTER + THERMALFOUNDATION
		;
	
}
