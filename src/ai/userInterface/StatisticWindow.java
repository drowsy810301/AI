package ai.userInterface;

import java.awt.BasicStroke;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.*;

import ai.poem.PoemTemplate;

public class StatisticWindow extends JFrame{
	
	/**
	 * 最多只會畫出 400 筆資料，超過400筆的話會捨棄較年輕的世代，只把後面的世代資料畫出來
	 * 例如:1000筆資料，只會畫出 601 ~ 1000
	 */
	private static final long serialVersionUID = 1L;
	
	GenerationData dataSet;
	MyPanel myPanel;
	public StatisticWindow(String title,int countPoint,int[] max, int[] min, int[] average, int[][] detailScore,String[] bestPoems){
		this.dataSet = new GenerationData(countPoint, max, min, average,detailScore);

		DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		Date date = new Date();
		setTitle(title+" "+sdf.format(date));
		setLayout(null);
		setBounds(0,0,1300,930);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		myPanel = new MyPanel();
		myPanel.setLocation(10, 10);
		myPanel.setBackground(Color.WHITE);
		add(myPanel);
		
		ShowPoemPanel poemPanel = new ShowPoemPanel(bestPoems);
		poemPanel.setLocation(MyPanel.WIDTH+20,10);
		add(poemPanel);
		
		this.setVisible(true);
	}
	
	public JPanel getGraohicPanel() {
		return myPanel;
	}
	
	private class ShowPoemPanel extends JPanel implements ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8560872406473150050L;
		private String[] poems;
		private JTextArea showPoem;
		private Button previousPoem, nextPoem;
		int poemIndex;
		
		public ShowPoemPanel(String[] poems) {
			
			this.poems = poems;
			poemIndex = 0;
			setLayout(null);
			
			setSize(360, 210);
			setBorder(BorderFactory.createTitledBorder("較好的詩"));
			
			showPoem = new JTextArea(poems[poemIndex]);
			showPoem.setBounds(10,50, 340, 150);
			showPoem.setBackground(Color.WHITE);
			showPoem.setEditable(false);
			showPoem.setLineWrap(true);
			showPoem.setBorder(BorderFactory.createCompoundBorder(
					showPoem.getBorder(), 
			        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			add(showPoem);
			
			previousPoem = new Button("< Previous");
			previousPoem.setBounds(10, 20, 160, 30);
			previousPoem.addActionListener(this);
			add(previousPoem);
			
			nextPoem = new Button("Next >");
			nextPoem.setBounds(170, 20, 160, 30);
			nextPoem.addActionListener(this);
			add(nextPoem);
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("< Previous") && poemIndex > 0){
				showPoem.setText(poems[--poemIndex]);
			}
			else if (e.getActionCommand().equals("Next >") && poemIndex < poems.length-1){
				showPoem.setText(poems[++poemIndex]);
			}
			
		}
	}
	
	private class MyPanel extends JPanel{

		/**
		 * 
		 */
		static final int BORDER_OFFSET = 30;
		static final int WIDTH = 900;
		static final int HEIGHT = 870;
		private double unitX, unitY;
		private int maxValue = dataSet.maxValue;
		private int minValue = dataSet.minValue;
		private int countPoint = dataSet.countPoint;
		
		public MyPanel (){
			super();
			setBorder(BorderFactory.createTitledBorder("演化趨勢圖"));
			setSize(WIDTH,HEIGHT);
		}
		
		private static final long serialVersionUID = 1L;
		
		public void paintComponent(Graphics g){
			
			maxValue = dataSet.maxValue;
			minValue = dataSet.minValue;
			countPoint = dataSet.countPoint;
			super.paintComponent(g);
			
			unitX = (WIDTH - 2 * BORDER_OFFSET) / (countPoint);
			unitY = (HEIGHT - 2 * BORDER_OFFSET) / (maxValue - minValue+1);
			
			/*畫X,Y軸*/
			((Graphics2D)g).setStroke(new BasicStroke(3));
			g.drawLine(BORDER_OFFSET, HEIGHT - BORDER_OFFSET, WIDTH - BORDER_OFFSET, HEIGHT - BORDER_OFFSET);
			g.drawLine(BORDER_OFFSET, HEIGHT - BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET);
			
			/*畫刻度*/
			Stroke bs =   new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{16, 4}, 0);  
			((Graphics2D)g).setStroke(bs);
			g.setFont(new Font(Font.DIALOG,Font.BOLD,12));
			drawScale(g, minValue);
			drawScale(g, (minValue*3+maxValue)/4);
			drawScale(g, (minValue+maxValue)/2);
			drawScale(g, (minValue+maxValue*3)/4);		
			drawScale(g, dataSet.max[dataSet.countPoint-1]);
			if (dataSet.bestPoemValue > dataSet.max[dataSet.countPoint-1]+10)
				drawScale(g, dataSet.bestPoemValue);
			drawScale(g, dataSet.average[dataSet.countPoint-1]);
			drawScale(g, maxValue);

			if ( maxValue > PoemTemplate.MAX_ANTITHESIS_SCORE)
				drawScale(g, PoemTemplate.MAX_ANTITHESIS_SCORE);
			if ( maxValue > PoemTemplate.MAX_DIVERSITY_SCORE)
				drawScale(g, PoemTemplate.MAX_DIVERSITY_SCORE);
			if ( maxValue > PoemTemplate.MAX_RHYTHM_SCORE)
				drawScale(g, PoemTemplate.MAX_RHYTHM_SCORE);
			if ( maxValue > PoemTemplate.MAX_TONE_SCORE)
				drawScale(g, PoemTemplate.MAX_TONE_SCORE);
			
			bs =   new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 6}, 0);  
			((Graphics2D)g).setStroke(bs);
			drawXScale(g, 1);
			for (int i = 10; i < countPoint ; i += 10){
				drawXScale(g, i);
			}
			drawXScale(g, countPoint);
			
			((Graphics2D)g).setStroke(new BasicStroke(3));

			drawLine(g,"Maximum",dataSet.max,Color.red);
			drawLine(g,"Average",dataSet.average,new Color(0xAA, 0x00, 0xAA));
			// TODO 不畫下限，避免畫面雜亂
			//drawLine(g, dataSet.min, Color.pink);
			Color[] color = {Color.blue,Color.orange,Color.green,Color.gray,Color.MAGENTA};
			String[] label = {"Rhythm","Tone","Antithesis","Diversity","Pattern"};
			if (color.length != PoemTemplate.COUNT_FITNESS_TYPE){
				System.err.println("繪製折線圖失敗，請設定足夠的顏色");
				System.exit(1);
			}
			if (color.length != PoemTemplate.COUNT_FITNESS_TYPE){
				System.err.println("繪製折線圖失敗，請設定足夠的標籤");
				System.exit(1);
			}
			for (int i = 0 ; i < PoemTemplate.COUNT_FITNESS_TYPE ; i++){
				drawLine(g,label[i], dataSet.detailScore[i] , color[i]);
				//g.drawString(label[i], BORDER_OFFSET+(int)(countPoint*unitX),(int)(HEIGHT-BORDER_OFFSET-(dataSet.detailScore[i][countPoint-1]-minValue+1)*unitY));
			}
			
		}
		
		private void drawXScale(Graphics g, int index){
			int x = BORDER_OFFSET+(int)(index*unitX);
			g.drawString(String.valueOf(index+dataSet.ignorePoint),x, HEIGHT - BORDER_OFFSET+15);
			g.drawLine(x, HEIGHT - BORDER_OFFSET + 5, x, BORDER_OFFSET);
		}
		
		private void drawScale(Graphics g, int scale){
			int y = (int)(HEIGHT-BORDER_OFFSET-(scale-minValue+1)*unitY);
			g.drawString(String.valueOf(scale),5,y);
			g.drawLine(BORDER_OFFSET-5, y, WIDTH - BORDER_OFFSET, y);
		}
		
		private void drawLine(Graphics g,String label,int[] data,Color color){
			int preX = 0, preY = 0, x, y;
			int countPoint = data.length;
			
			for (int i = 0 ; i < countPoint ; i++){
				g.setColor(color);
				x = BORDER_OFFSET+(int)((i+1)*unitX);
				y = (int)(HEIGHT-BORDER_OFFSET-(data[i]-minValue+1)*unitY);
				if ( i > 0){
					g.drawLine(preX, preY, x, y);
				}
				g.drawOval(x-2, y-2, 4, 4);
				preX = x;
				preY = y;
			}
			g.drawString(label, BORDER_OFFSET+(int)((countPoint+2)*unitX),(int)(HEIGHT-BORDER_OFFSET-(data[countPoint-1]-minValue+1)*unitY));
		}
		
	}
	
	private class GenerationData {
		
		private static final int MAX_POINT = 400;
		private int[] max, min, average;
		private int[][] detailScore;
		private int maxValue,minValue,bestPoemValue;
		private int ignorePoint;
		private int countPoint;
		public GenerationData(int countPoint,int[] max, int[] min, int[] average, int[][] detailScore){
			
			if ( countPoint <= MAX_POINT){	
				ignorePoint = 0;
			}
			else{
				ignorePoint = countPoint - MAX_POINT;
				countPoint = MAX_POINT;
			}
			
			this.countPoint = countPoint;
			this.max = new int[countPoint];
			this.min = new int[countPoint];
			this.average = new int[countPoint];
			this.detailScore = new int[PoemTemplate.COUNT_FITNESS_TYPE][countPoint];
			maxValue = 0;
			minValue = 1000000;
			for ( int i = 0 ; i < countPoint ; i++){
				if ( maxValue < max[i + ignorePoint])
					maxValue = max[i + ignorePoint];
				if (minValue > min[i + ignorePoint])
					minValue = min[i + ignorePoint];
			
				this.max[i] = max[i+ ignorePoint];
				this.min[i] = min[i+ ignorePoint];
				this.average[i] = average[i+ ignorePoint];
				for (int j = 0; j < PoemTemplate.COUNT_FITNESS_TYPE ; j++){
					this.detailScore[j][i] = detailScore[j][i + ignorePoint];
				}
			}
			// TODO 因為樣詳細畫出各個fitnessScore，所以把下限拉到0
			minValue = 0;
			bestPoemValue = maxValue;
			// TODO
			maxValue = PoemTemplate.TOTAL_SCORE;
		}
	}
}


