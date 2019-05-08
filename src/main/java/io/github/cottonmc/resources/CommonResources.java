package io.github.cottonmc.resources;


import io.github.cottonmc.cotton.Cotton;
import io.github.cottonmc.cotton.datapack.recipe.RecipeUtil;
import io.github.cottonmc.resources.oregen.OreGeneration;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommonResources {
    private static String[] MACHINE_AFFIXES = new String[] {"gear", "plate"};
    private static String[] RADIOACTIVE_AFFIXES = new String[] {"dust"};

    public static final Map<String, ResourceType> BUILTINS = new HashMap<>();

    /** more resources which mods requested to add. These resources are also in builtins,
     *  they are here so they can be enabled in the registration without being present in the JSON config. */
    public static final Map<String, ResourceType> ENABLED_ADDITIONAL_RESOURCES = new HashMap<>();

    public static void initialize() {
        builtinMetal("copper", BlockSuppliers.STONE_TIER_ORE, MACHINE_AFFIXES);
        builtinMetal("silver", BlockSuppliers.IRON_TIER_ORE,  MACHINE_AFFIXES);
        builtinMetal("lead",   BlockSuppliers.IRON_TIER_ORE,  MACHINE_AFFIXES);
        builtinMetal("zinc",   BlockSuppliers.STONE_TIER_ORE);
        builtinMetal("aluminum", BlockSuppliers.IRON_TIER_ORE);
        builtinMetal("cobalt", BlockSuppliers.IRON_TIER_ORE);
        builtinMetal("tin", BlockSuppliers.STONE_TIER_ORE);
        builtinMetal("tungsten", BlockSuppliers.IRON_TIER_ORE);

        builtinMetal("uranium",   BlockSuppliers.IRON_TIER_ORE);

        builtinMetal("steel",    null, MACHINE_AFFIXES);
        builtinMetal("brass",    null, MACHINE_AFFIXES);
        builtinMetal("electrum", null); //no ore, no gears/plates

        builtinItem("coal", "dust");
        BUILTINS.put("coal_coke", new GenericResourceType("coal_coke").withBlockAffix("block", BlockSuppliers.COAL_BLOCK).withItemAffixes(""));
        builtinItem("mercury");
        
        builtinItem("iron", "gear", "plate", "dust");
        builtinItem("gold", "gear", "plate", "dust");

        //These might get rods or molten capsules. They'd just need to be added to the end.
        builtinItem("plutonium", RADIOACTIVE_AFFIXES);
        builtinItem("thorium",   RADIOACTIVE_AFFIXES);

        boolean enableAllResources = CottonResources.config.enabledResources.contains("*");

        for (ResourceType resource : BUILTINS.values()) {
            if (enableAllResources ||
                    CottonResources.config.enabledResources.contains(resource.getBaseResource()) ||
                    ENABLED_ADDITIONAL_RESOURCES.values().contains(resource)
            ) {
                resource.registerAll();
            }
            else nullifyRecipes(resource);
        }

        OreGeneration.registerOres();
    }

    private static void builtinMetal(String id, Supplier<Block> oreSupplier, String... extraAffixes) {
        if (Cotton.isDevEnv) CottonResources.logger.info("registering " + id);
        MetalResourceType result = new MetalResourceType(id).withOreSupplier(oreSupplier);
        if (extraAffixes.length > 0){
            result.withItemAffixes(extraAffixes);
        }

        BUILTINS.put(id, result);
    }

    private static void builtinItem(String id, String... extraAffixes) {
        GenericResourceType result = new GenericResourceType(id).withItemAffixes(extraAffixes);
        if (extraAffixes.length == 0){
            result.withItemAffixes(""); //This is just a base type with no affixes, like "mercury".
        }
        BUILTINS.put(id, result);
    }


    /** PUBLIC API METHOD **/
    public static void requestResource(String name) {
        ENABLED_ADDITIONAL_RESOURCES.put(name, BUILTINS.get(name));
        CottonResources.logger.info(name + " requested by another mod!");
    }

    // nullify recipes for metals not currently enabled
    private static void nullifyRecipes(ResourceType resource) {
        if (resource instanceof MetalResourceType) {
            MetalResourceType metal = (MetalResourceType)resource;
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_block"));
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_ingot"));
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_ingot_from_blasting"));
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_ingot_from_" + metal.name + "_block"));
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_ingot_from_nuggets"));
            RecipeUtil.removeRecipe(new Identifier(Cotton.SHARED_NAMESPACE, metal.name + "_nugget"));
        }
    }
}
