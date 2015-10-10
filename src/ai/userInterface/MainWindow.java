package ai.userInterface;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.json.JSONObject;

import ai.GeneticAlgorithm.GeneticAlgorithm;
import ai.net.ConceptNetCrawler;
import ai.sentence.SentenceMaker;
import ai.word.ChineseWord;
import ai.word.WordPile;

public class MainWindow extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4615405674885421489L;
	private String topic;
	private int topicWordType;
	private ArrayList<ChineseWord> wordArrayList;
	private WordPile wordPile;
	private SentenceMaker maker;
	private GeneticAlgorithm ga;
	
	private WordSourcePanel sourcePanel = new WordSourcePanel();
	private StatisticalPanel statisticalPanel = new StatisticalPanel();
	private ProgressBarPanel progressPanel = new ProgressBarPanel();
	
	private ModifyWindow modifyWindow = null;
	
	public static void main(String[] srgv){
		new MainWindow();
	}
	
	MainWindow(){
		setLocation(10, 10);
		setSize(455, 310);
		setResizable(false);
		setLayout(null);
		setTitle("電腦作詩");
		setLocationRelativeTo(null);
		
		sourcePanel.setLocation(10,10);
		add(sourcePanel);
		
		statisticalPanel.setLocation(230,10);
		MyUtility.enableComponents(statisticalPanel, false);
		add(statisticalPanel);
		
		progressPanel.setLocation(10, 220);
		add(progressPanel);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void updateStatisticValue(ArrayList<ChineseWord> wordArrayList) {
		wordPile = new WordPile(topic,topicWordType);
		this.wordArrayList = wordArrayList;
		wordPile.addWords(wordArrayList);
		maker = new SentenceMaker(wordPile);
		ga = new GeneticAlgorithm(8, 5, wordPile, maker);
		ga.setProgressBar(progressPanel.progressBar);
		statisticalPanel.txt_countWordValue.setText(Integer.toString(wordPile.getTotalWordCount()));
		statisticalPanel.txt_countSentenceTypeValue.setText(Integer.toString(maker.getAvailableTypeCount()));
		statisticalPanel.txt_countSentenceValue.setText(Integer.toString(maker.getAvailableSentenceCount()));
	}
	
	private class StatisticalPanel extends JPanel implements ActionListener{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7514862625781110568L;
		private JLabel txt_countWordValue,txt_countSentenceTypeValue,txt_countSentenceValue;
		private JButton btn_modify, btn_evolve;
		
		public StatisticalPanel() {
			
			setSize(200, 200);
			setLayout(null);
			setBorder(BorderFactory.createTitledBorder("詞庫統計"));
			
			JLabel txt_countWord = new JLabel("詞彙數 :");
			txt_countWord.setBounds(20,20,60,25);
			add(txt_countWord);
			
			txt_countWordValue = new JLabel("0");
			txt_countWordValue.setBounds(80, 20, 60, 25);
			//txt_countWordValue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			add(txt_countWordValue);
			
			JLabel txt_countSentenceType = new JLabel("句型數 :");
			txt_countSentenceType.setBounds(20,60,60,25);
			add(txt_countSentenceType);
			
			txt_countSentenceTypeValue = new JLabel("0");
			txt_countSentenceTypeValue.setBounds(80, 60, 60, 25);
			//txt_countSentenceTypeValue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			add(txt_countSentenceTypeValue);
			
			JLabel txt_countSentence = new JLabel("句子數 :");
			txt_countSentence.setBounds(20,100,60,25);
			add(txt_countSentence);
			
			txt_countSentenceValue = new JLabel("0");
			txt_countSentenceValue.setBounds(80, 100, 60, 25);
			//txt_countSentenceValue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			add(txt_countSentenceValue);
			
			btn_modify = new JButton("修改");
			btn_modify.setBounds(60,165,60,25);
			btn_modify.addActionListener(this);
			add(btn_modify);
			
			btn_evolve = new JButton("演化");
			btn_evolve.setBounds(130,165,60,25);
			btn_evolve.addActionListener(this);
			add(btn_evolve);
			
			setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getSource().equals(btn_evolve)){
				new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							ga.evolve();
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									MyUtility.enableComponents(sourcePanel, true);
									MyUtility.enableComponents(StatisticalPanel.this, true);
									progressPanel.setTitle("演化完成");
								}
							});
						}
						catch (Exception e){
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									MyUtility.enableComponents(sourcePanel, true);
									MyUtility.enableComponents(StatisticalPanel.this, true);
									progressPanel.setTitle("演化失敗");
									progressPanel.progressBar.setIndeterminate(false);
								}
							});
							JOptionPane.showMessageDialog(MainWindow.this,"作詩失敗","錯誤",JOptionPane.ERROR_MESSAGE);
						}
					}
				}).start();
				progressPanel.progressBar.setStringPainted(false);
				progressPanel.progressBar.setIndeterminate(true);
				MyUtility.enableComponents(sourcePanel, false);
				MyUtility.enableComponents(StatisticalPanel.this, false);
				progressPanel.setTitle("演化中...");
			}
			else if (e.getSource().equals(btn_modify)){
				MainWindow.this.setVisible(false);
				modifyWindow.setVisible(true);
			}
			
		}
	}
	
	private class ProgressBarPanel extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8420800234516334664L;
		JProgressBar progressBar;
		private TitledBorder border;
		public ProgressBarPanel() {
			
			setSize(350,50);
			setLayout(null);
			border = BorderFactory.createTitledBorder("進度");
			setBorder(border);
			
			progressBar = new JProgressBar(0,100);
			progressBar.setValue(0);
			progressBar.setForeground(new Color(0x00CD00));
			progressBar.setStringPainted(true);
			progressBar.setBounds(10,20,330,20);
			add(progressBar);
			
			setVisible(true);
		}
		
		public void setTitle(String title){
			repaint();
			border.setTitle(title);
		}
	}
	
	private class WordSourcePanel extends JPanel implements ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3555715580638648436L;
		private JRadioButton netSource, fileSource;
		
		private File sourceJsonFile = null;
		private JTextField txt_file;
		private JTextField txt_topic;
		private JButton btn_load;
		private JComboBox<String> cbox_wordType;
		private Color defaultBackgroungColor;
		public WordSourcePanel() {
			setSize(210, 200);
			setLayout(null);
			setBorder(BorderFactory.createTitledBorder("詞彙來源"));
			
			netSource = new JRadioButton("Concept Net");
			netSource.setBounds(10, 20,100,25);
			netSource.setSelected(true);
			netSource.addActionListener(this);
			add(netSource);
			
			fileSource = new JRadioButton("Json File");
			fileSource.setBounds(10, 90,100,25);
			fileSource.addActionListener(this);
			add(fileSource);
			
			ButtonGroup group = new ButtonGroup();
			group.add(fileSource);
			group.add(netSource);
			
			JLabel label_topic = new JLabel("主題 :");
			label_topic.setBounds(30,55,40,25);
			add(label_topic);
			JLabel label_file = new JLabel("檔案 :");
			label_file.setBounds(30, 115, 40, 30);
			add(label_file);
			
			txt_topic = new JTextField();
			txt_topic.setBounds(70, 57, 60, 25);
			add(txt_topic);
			String[] wordType = {"名","形","動"};
			cbox_wordType = new JComboBox<String>(wordType);
			cbox_wordType.setBounds(130,57,50,25);
			add(cbox_wordType);
			
			txt_file = new JTextField();
			txt_file.setBounds(70, 117, 110, 25);
			txt_file.setEditable(false);
			defaultBackgroungColor = txt_file.getBackground();
			txt_file.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					JTextField tmp = (JTextField)e.getSource();
					if (!fileSource.isSelected())
						return;
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File("."));
					int result = fc.showOpenDialog(MainWindow.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						sourceJsonFile = fc.getSelectedFile();
			            tmp.setText(sourceJsonFile.getName());
					}
				}
			});
			add(txt_file);
			
			btn_load = new JButton("讀取");
			btn_load.setBounds(140,160,60,25);
			btn_load.addActionListener(this);
			add(btn_load);
			
			setVisible(true);
		}
		
		private Runnable finishLoading = new Runnable(){
			@Override
			public void run() {
				MyUtility.enableComponents(WordSourcePanel.this,true);
				updateStatisticValue(wordArrayList);
				MyUtility.enableComponents(statisticalPanel,true);
				progressPanel.setTitle("讀取完成");
				progressPanel.progressBar.setStringPainted(true);
				progressPanel.progressBar.setIndeterminate(false);
			}
		};
		
		private Runnable failLoading = new Runnable(){
			@Override
			public void run() {
				progressPanel.progressBar.setStringPainted(true);
				progressPanel.progressBar.setIndeterminate(false);
				MyUtility.enableComponents(WordSourcePanel.this,true);
				progressPanel.setTitle("讀取失敗");
				JOptionPane.showMessageDialog(MainWindow.this, "從檔案讀取詞彙失敗","錯誤",JOptionPane.ERROR_MESSAGE);
			}
		};
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(btn_load)){
				if (netSource.isSelected()){
					if (txt_topic.getText().equals("")){
						JOptionPane.showMessageDialog(MainWindow.this,"請輸入主題","錯誤",JOptionPane.ERROR_MESSAGE);
					}
					else{
						System.out.println("read source from Concept Net");
						new Thread(new Runnable() {
							@Override
							public void run() {
								
								String selected = (String)cbox_wordType.getSelectedItem();
								if (selected.equals("名")){
									topicWordType = ChineseWord.NOUN;
								}
								else if (selected.equals("形")){
									topicWordType = ChineseWord.NOUN;
								}
								else if (selected.equals("動")){
									topicWordType = ChineseWord.NOUN;
								}
								else {
									topicWordType = 0;
								}
								ConceptNetCrawler crawler = new ConceptNetCrawler(txt_topic.getText(),topicWordType);
								crawler.setLoadingBar(progressPanel.progressBar);
								ChineseWord[] wordList = crawler.getWordList_ChineseSource();
								wordArrayList = new ArrayList<>(Arrays.asList(wordList));
								MainWindow.this.topic = txt_topic.getText();
								modifyWindow = new ModifyWindow(MainWindow.this,topic,topicWordType,wordArrayList);
								SwingUtilities.invokeLater(finishLoading);
							}
						}).start();
						progressPanel.setTitle("讀取中...");
						progressPanel.progressBar.setStringPainted(false);
						progressPanel.progressBar.setIndeterminate(true);
						MyUtility.enableComponents(WordSourcePanel.this,false);
						MyUtility.enableComponents(statisticalPanel,false);
					}
				}
				else if(fileSource.isSelected()){
					if (sourceJsonFile == null){
						JOptionPane.showMessageDialog(MainWindow.this,"請選擇檔案","錯誤",JOptionPane.ERROR_MESSAGE);
					}
					else{
						System.out.println("read source from file");
						new Thread(new Runnable() {
							@Override
							public void run() {
								JSONObject root = null;
								ChineseWord[] wordList = null;
								try {
									BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceJsonFile));
									int count;
									StringBuilder sb = new StringBuilder();
									byte[] buff = new byte[4096];
									while ((count = in.read(buff)) != -1){
										sb.append(new String(buff,0,count));
									}
									root = new JSONObject(sb.toString());
									in.close();
									topic = root.getString("topic");
									topicWordType = root.getInt("topicWordType");
									wordList = ChineseWord.loadWords(root.getJSONArray("wordList"));
									wordArrayList = new ArrayList<>(Arrays.asList(wordList));
									modifyWindow = new ModifyWindow(MainWindow.this,topic,topicWordType,wordArrayList);
									SwingUtilities.invokeLater(finishLoading);
								} catch (Exception e) {
									SwingUtilities.invokeLater(failLoading);
									e.printStackTrace();
								}
							}
						}).start();
						progressPanel.setTitle("讀取中...");
						progressPanel.progressBar.setStringPainted(false);
						progressPanel.progressBar.setIndeterminate(true);
						MyUtility.enableComponents(WordSourcePanel.this,false);
						MyUtility.enableComponents(statisticalPanel,false);
					}
				}
			}
			else if (e.getSource().equals(netSource) || e.getSource().equals(fileSource)){
				if (netSource.isSelected()){
					txt_topic.setEditable(true);
					cbox_wordType.setEnabled(true);
					txt_file.setBackground(defaultBackgroungColor);
					txt_file.setCursor(Cursor.getDefaultCursor());
				}
				else if (fileSource.isSelected()){
					txt_topic.setEditable(false);
					cbox_wordType.setEnabled(false);
					txt_file.setBackground(Color.white);
					txt_file.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}
			
		}
	}

}
