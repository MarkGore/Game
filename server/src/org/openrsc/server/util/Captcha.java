package org.openrsc.server.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.openrsc.server.Config;
import org.openrsc.server.database.ConnectionFactory;

public class Captcha
{
	private static Vector<Pair<String, BufferedImage>> captchas = null;
	
	static
	{
		try(Connection connection = ConnectionFactory.getDbConnection())
		{
			try(Statement statement = connection.createStatement())
			{
				ResultSet result = statement.executeQuery("SELECT * FROM `"+ Config.getToolsDbName() +"`.`captcha`;");
				captchas = new Vector<Pair<String, BufferedImage>>();
				while (result.next())
				{
					String word = result.getString("string");
					Blob binary = result.getBlob("captcha");
					BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(binary.getBytes(1L, (int)binary.length()))));
					captchas.add(new Pair<String, BufferedImage>(word, image));
				}
			}
		}
		catch(IOException | SQLException e)
		{
			throw (ExceptionInInitializerError)new ExceptionInInitializerError().initCause(e);
		}
	}


	public static synchronized Pair<String, BufferedImage> getCaptcha()
	{
		if (captchas.isEmpty())
		{
			return null;
		}
		return captchas.get(DataConversions.getRandom().nextInt(captchas.size()));
	}
}