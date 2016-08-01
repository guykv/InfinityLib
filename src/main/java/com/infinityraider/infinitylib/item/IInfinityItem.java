package com.infinityraider.infinitylib.item;

import com.infinityraider.infinitylib.utility.IToggleable;

import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IInfinityItem extends IToggleable {

	String getInternalName();

	List<String> getOreTags();
	
	@SideOnly(Side.CLIENT)
	void registerItemRenderer();

}
