package ai.userInterface;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JProgressBar;

public final class MyUtility {
	
	public static void enableComponents(Container container,boolean bool){
		
		for (Component c : container.getComponents()){
			c.setEnabled(bool);
			if (c instanceof Container){
				enableComponents((Container)c, bool);
			}
		}
	}
	
	public static class ProgressBarSetting implements Runnable{
		private JProgressBar bar;
		private int progress;
		
		public ProgressBarSetting(JProgressBar bar, int progress){
			this.bar = bar;
			this.progress = progress;
		}
		@Override
		public void run() {
			bar.setValue(progress);			
		}
		
	}
	
	public static BufferedImage getScreenShot(Component component) {

	    BufferedImage image = new BufferedImage(component.getWidth(),component.getHeight(),BufferedImage.TYPE_INT_RGB);
	    Graphics g = image.createGraphics();
	    component.paint(g);
	    g.dispose();
	    return image;
	 }
}
