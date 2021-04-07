package io.github.jgcodes.butool.gui;

import javafx.geometry.Bounds;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.util.Properties;

public final class SVGIcons {
	private static final Properties icons;

	static {
		icons = new Properties();
		try {
			icons.load(SVGIcons.class.getResourceAsStream("/assets/icons.properties"));
		} catch (IOException e) {
			System.exit(69);
		}
	}

	private SVGIcons() {}
	public static Region fromPath(String path, double width, double height, Color color) {
		SVGPath p = new SVGPath();
		p.setContent(path);
		Bounds pBounds = p.getBoundsInLocal();

		Region r = new Region();
		r.setBackground(new Background(new BackgroundFill(color, null, null)));
		r.setShape(p);

		if (width < height) {
			r.setPrefSize(height * (pBounds.getWidth() / pBounds.getHeight()), height);
			r.setMaxSize(height * (pBounds.getWidth() / pBounds.getHeight()), height);
			r.setMinSize(height * (pBounds.getWidth() / pBounds.getHeight()), height);
		}
		else {
			r.setPrefSize(width, width * (pBounds.getHeight() / pBounds.getWidth()));
			r.setMaxSize(width, width * (pBounds.getHeight() / pBounds.getWidth()));
			r.setMinSize(width, width * (pBounds.getHeight() / pBounds.getWidth()));
		}

		return r;
	}
	public static Region fromPath(String path, double width, double height) {
		return fromPath(path, width, height, Color.BLACK);
	}

	public static Region ofName(String name, double width, double height, Color color) {
		String svg = icons.getProperty(name);
		if (svg == null) throw new NullPointerException("Icon with the specified name doesn't exist");
		return fromPath(svg, width, height, color);
	}

	public static Region ofName(String name, double width, double height) {
		return ofName(name, width, height, Color.BLACK);
	}
}
