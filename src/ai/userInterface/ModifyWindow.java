package ai.userInterface;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;

import ai.exception.BopomofoException;
import ai.net.BopomofoCrawler;
import ai.word.ChineseWord;
import ai.word.Relation;

public class ModifyWindow extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5071777012395856257L;
	private RelationPanel relationPanel;
	private FilterPanel filterPanel;
	private WordListPanel wordListPanel;
	private AddTopicPanel addTopicPanel;
	
	private String topic;
	private int topicWordType;
	private ArrayList<ChineseWord> wordPile;
	private ArrayList<Boolean> isVisible;
	private DefaultListModel<ChineseWord> listModel;
	private Relation selectedRelation;
	private Stack<Integer> deletedWords;
	private JTextField txt_file;
	private File saveJson;
	
	public ModifyWindow(MainWindow parent, String topic, Integer topicWordType, ArrayList<ChineseWord> wordPile) {
		
		
		this.wordPile = new ArrayList<>(wordPile);
		
		isVisible = new ArrayList<>();
		for (int i = 0 ; i < wordPile.size() ; i++)
			isVisible.add(true);
		this.topic = topic;
		this.topicWordType = topicWordType;
		deletedWords = new Stack<>();
		listModel = new DefaultListModel<ChineseWord>();
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				ArrayList<ChineseWord> newList = new ArrayList<>();
				for (int i = 0; i < wordPile.size() ; i++){
					if (!deletedWords.contains(i)){
						newList.add(wordPile.get(i));
					}
				}
				parent.updateStatisticValue(newList);
				parent.setVisible(true);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		setLayout(null);
		setResizable(false);
		setSize(600,435);
		setTitle("修改詞庫");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		relationPanel = new RelationPanel();
		relationPanel.setLocation(10,10);
		add(relationPanel);
		
		filterPanel = new FilterPanel();
		filterPanel.setLocation(200,10);
		add(filterPanel);
		
		addTopicPanel = new AddTopicPanel();
		addTopicPanel.setLocation(440,10);
		add(addTopicPanel);
		
		wordListPanel = new WordListPanel();
		wordListPanel.setLocation(200,120);
		add(wordListPanel);
		updateWordJlist();
		
		JButton btn_save, btn_back;
		
		btn_save = new JButton("儲存");
		btn_save.setBounds(440, 370, 60, 25);
		btn_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveToJsonFile();
			}
		});
		add(btn_save);
		
		btn_back = new JButton("返回");
		btn_back.setBounds(510, 370, 60, 25);
		btn_back.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ModifyWindow.this.setVisible(false);
				ArrayList<ChineseWord> newList = new ArrayList<>();
				for (int i = 0; i < wordPile.size() ; i++){
					if (!deletedWords.contains(i)){
						newList.add(wordPile.get(i));
					}
				}
				parent.updateStatisticValue(newList);
				parent.setVisible(true);
			}
		});
		add(btn_back);
		
		
		txt_file = new JTextField();
		txt_file.setBounds(330, 370, 100, 25);
		txt_file.setText(topic+".json");
		saveJson = new File(topic+".json");
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
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("選擇要儲存的json檔案");
				fc.setCurrentDirectory(new File("."));
				int result = fc.showSaveDialog(ModifyWindow.this);
				
				if (result == JFileChooser.APPROVE_OPTION) {
					saveJson = fc.getSelectedFile();
					tmp.setText(saveJson.getName());
				}
			}
		});
		add(txt_file);
		
		//setVisible(true);
	}
	
	private void updateWordJlist(){
		
		listModel.clear();
		if (selectedRelation == Relation.TOPIC)
			MyUtility.enableComponents(addTopicPanel, true);
		else
			MyUtility.enableComponents(addTopicPanel, false);
		for (int i = 0 ; i < wordPile.size() ; i++){
			ChineseWord word = wordPile.get(i);
			if (word.getRelation() == selectedRelation && filterPanel.isWordValid(word) && isVisible.get(i) == true)
				listModel.addElement(word);
		}
		if (listModel.getSize() == 0){
			wordListPanel.txt_noData.setVisible(true);
		}
		else{
			wordListPanel.txt_noData.setVisible(false);
		}
		wordListPanel.wordList.setModel(listModel);
		wordListPanel.wordList.setSelectedIndex(0);
	}
	
	private void deleteWord(ChineseWord word){
		
		int result1 = wordPile.indexOf(word);
		int listIndex = listModel.indexOf(word);
		boolean result2 = listModel.removeElement(word);
		
		if ( result1 == -1 || !result2){
			JOptionPane.showMessageDialog(ModifyWindow.this,"刪除 \""+word.getWord()+"\" 失敗","錯誤",JOptionPane.ERROR_MESSAGE);
		}
		
		isVisible.set(result1,false);
		deletedWords.push(result1);
		wordListPanel.btn_undo.setEnabled(true);
		
		if (listModel.getSize() == 0){
			wordListPanel.txt_noData.setVisible(true);
		}
		else{
			wordListPanel.txt_noData.setVisible(false);
			wordListPanel.wordList.setSelectedIndex(Math.min(listIndex,listModel.size()-1));
		}
		
	}
	
	private void restoreDeletion(){
		int index1 = deletedWords.pop();
		isVisible.set(index1, true);
		ChineseWord word = wordPile.get(index1);
		updateWordJlist();
		
		int index2 = listModel.indexOf(word);
		if (index2 != -1){
			wordListPanel.wordList.ensureIndexIsVisible(index2);
			wordListPanel.wordList.setSelectedIndex(index2);
		}
		
		if(deletedWords.empty())
			wordListPanel.btn_undo.setEnabled(false);
	}
	
	private void saveToJsonFile(){
		JSONObject root = new JSONObject();
		ArrayList<ChineseWord> newList = new ArrayList<>();
		for (int i = 0; i < wordPile.size() ; i++){
			if (!deletedWords.contains(i)){
				newList.add(wordPile.get(i));
			}
		}
		try {
			root.put("topic", topic);
			root.put("topicWordType", topicWordType);
			JSONArray wordArray = new JSONArray();
			for (ChineseWord word : newList){
				wordArray.put(new JSONObject(word));
			}
			root.put("wordList", wordArray);
		
			if (saveJson.exists()){
				int result = JOptionPane.showConfirmDialog(ModifyWindow.this,"檔案已經存在，請問要覆蓋嗎?","警告",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if (result != 0)
					return;
			}
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveJson));
			out.write(root.toString().getBytes());
			out.close();
			JOptionPane.showMessageDialog(ModifyWindow.this, "存檔成功","訊息",JOptionPane.INFORMATION_MESSAGE);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(ModifyWindow.this, "存檔失敗","錯誤",JOptionPane.ERROR_MESSAGE);
	}
	
	private class RelationPanel extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3682950670794912301L;
		private Relation[] rel = Relation.values();
				
		public RelationPanel() {

			setSize(170, 350);
			setLayout(null);
			setBorder(BorderFactory.createTitledBorder("Relation"));
			
			JList<Relation> relationList = new JList<>(rel);
			relationList.setSelectedIndex(0);
			selectedRelation = relationList.getSelectedValue();
			relationList.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					selectedRelation = relationList.getSelectedValue();
					updateWordJlist();
				}
			});
			JScrollPane scrollPane = new JScrollPane(relationList);
			scrollPane.setBounds(10, 20, 150, 320);
			add(scrollPane);
			
			setVisible(true);
		}
		
		
	}
	
	private class FilterPanel extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3881086768886314627L;
		private JRadioButton[] rad_countLetter;
		private JRadioButton rad_start, rad_end;
		
		public FilterPanel() {
			setLayout(null);
			setSize(230, 100);
			setBorder(BorderFactory.createTitledBorder("篩選器"));
			
			JLabel countLetter = new JLabel("字數 :");
			countLetter.setBounds(20,25,40,25);
			add(countLetter);
			
			rad_countLetter = new JRadioButton[3];
			String[] name1 = {"1","2","3"};
			for (int i = 0 ; i < rad_countLetter.length ; i++){
				rad_countLetter[i] = new JRadioButton(name1[i]);
				rad_countLetter[i].setBounds(70+50*i, 25, 40, 25);
				rad_countLetter[i].setSelected(true);
				rad_countLetter[i].addActionListener(radioButtonListener);
				add(rad_countLetter[i]);
			
			}
			
			JLabel position = new JLabel("位置 :");
			position.setBounds(20,60,40,25);
			add(position);
			
			rad_start = new JRadioButton("start");
			rad_start.setBounds(70,60,60,25);
			rad_start.setSelected(true);
			rad_start.addActionListener(radioButtonListener);
			add(rad_start);
		
			rad_end = new JRadioButton("end");
			rad_end.setBounds(140,60,60,25);
			rad_end.setSelected(true);
			rad_end.addActionListener(radioButtonListener);
			add(rad_end);
			
			setVisible(true);
		}
		
		private ActionListener radioButtonListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateWordJlist();
			}
		};
		public boolean isWordValid(ChineseWord word) {
			boolean validLetter = false, validPosition = false;
			for ( int i = 0 ; i < 3 ; i++){
				if (rad_countLetter[i].isSelected()){
					validLetter |= (word.getLength() == (i+1));
				}
			}
			
			if (rad_start.isSelected()){
				validPosition |= (word.getStartOrEnd() == Relation.START);
			}
			
			if (rad_end.isSelected()){
				validPosition |= (word.getStartOrEnd() == Relation.END);
			}
			
			return validLetter && validPosition;
		}
	}
	
	private class WordListPanel extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1409594781354983192L;
		private JList<ChineseWord> wordList;
		private JLabel txt_noData;
		private JButton btn_undo;
		
		public WordListPanel() {
			setLayout(null);
			setSize(380, 240);
			setBorder(BorderFactory.createTitledBorder("詞彙清單"));
			
			
			wordList = new JList<>();
			wordList.setCellRenderer(new ChineseWordCellRender());
	
			JScrollPane scrollPane = new JScrollPane(wordList);
			scrollPane.setBounds(10,20,360,175);
			add(scrollPane);
			
			txt_noData = new JLabel("沒有符合篩選條件的詞彙");
			txt_noData.setBounds(50,205,150,25);
			txt_noData.setForeground(Color.RED);
			add(txt_noData);
			
			JButton btn_delete;

			btn_undo = new JButton("復原");
			btn_undo.setBounds(240,205,60,25);
			btn_undo.setEnabled(false);
			btn_undo.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					restoreDeletion();
				}
			});
			add(btn_undo);
			
			btn_delete = new JButton("刪除");
			btn_delete.setBounds(310,205,60,25);
			btn_delete.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteWord(wordList.getSelectedValue());
				}
			});
			add(btn_delete);
		}
		
		private class ChineseWordCellRender implements ListCellRenderer<ChineseWord>{
					
			@Override
			public Component getListCellRendererComponent(JList<? extends ChineseWord> list, ChineseWord value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				
				JLabel renderer = (JLabel) new DefaultListCellRenderer().getListCellRendererComponent(list, value, index,isSelected, cellHasFocus);
				renderer.setText(value.getSurfaceText());
				return renderer;
			}
			
		}
	}
	
	private class AddTopicPanel extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 616728237478438517L;
		
		public AddTopicPanel(){
			setLayout(null);
			setSize(140, 100);
			JTextField txt_input = new JTextField();
			txt_input.setBounds(10, 20, 120, 30);
			add(txt_input);
			
			JButton btn_addTopic = new JButton("增加");
			btn_addTopic.setBounds(70, 60, 60, 30);
			add(btn_addTopic);
			btn_addTopic.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String topic = txt_input.getText();
					if (topic.length() > 0){
						try {
							wordPile.add(new ChineseWord(topic, BopomofoCrawler.getBopomofo(topic), topicWordType, Relation.TOPIC, Relation.START,"主題：" + topic));
							isVisible.add(true);
							txt_input.setText("");
							updateWordJlist();
						} catch (BopomofoException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			
			setBorder(BorderFactory.createTitledBorder("增加主題"));
			
			
			setVisible(true);
		}
		
	}
}
