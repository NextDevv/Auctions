package com.nextdevv.auctions.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.Base64;

public class ClassSerializer {
	public static Object FromString(String s) {
		Object o = null;
		try {
			byte[] data = Base64.getDecoder().decode(s);
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return o;
	}

	public static String ToStringItemStack(ItemStack itemStack) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
			boos.writeObject(itemStack);
			boos.flush();
			boos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return Base64Coder.encodeLines(baos.toByteArray());
	}

	public static ItemStack FromStringItemStack(String s) {
		ItemStack itemStack = null;
		try {
			byte[] data = Base64Coder.decodeLines(s);
			BukkitObjectInputStream bois = new BukkitObjectInputStream(
					new ByteArrayInputStream(data)
			);
			itemStack = (ItemStack) bois.readObject();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return itemStack;
	}

	// Modified from source: http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
	public static String ToString( Serializable o ) {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
