import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class FileManager extends JFrame implements ActionListener, ClipboardOwner{
	//외형
	private JPanel mainpanel = new JPanel(new BorderLayout());
	private JPanel panel1 = new JPanel(new BorderLayout());
	private JPanel panel2 = new JPanel(new BorderLayout());
	private JTextField textfield = new JTextField();
	private JList<String> list = new JList<>();
	private JTable table = new JTable();
	private JLabel label = new JLabel("File Manager");
	private String[] language = {"한글", "English"};
	private JComboBox combobox= new JComboBox(language);
	private JScrollPane splist = new JScrollPane();
    private JScrollPane sptable = new JScrollPane();
    
    //Item
    private JMenuItem ItemKshow = new JMenuItem("폴더로 보기");
    private JMenuItem ItemKcopy = new JMenuItem("복사");
    private JMenuItem ItemKpaste = new JMenuItem("붙여넣기");
    private JMenuItem ItemKdelete = new JMenuItem("삭제");
    private JMenuItem ItemEshow = new JMenuItem("Show Item in the Folder");
    private JMenuItem ItemEcopy = new JMenuItem("Copy");
    private JMenuItem ItemEpaste = new JMenuItem("Paste");
    private JMenuItem ItemEdelete = new JMenuItem("Delete");
    private File back;
    
    //list에 필요한 것들
    private File file;
    private String address = "C:\\";
    private String[] listItem;
    private String[][] listdata;
    
    //table에 필요한 것들
    private DefaultTableModel tablemodel;
    private String[] titleK = {"이름", "크기", "수정한 날짜"};
    private String[] titleE = {"Name", "Size", "Modified"};
    
    //이벤트처리
    private int[] copy;
    private Vector<String> copyFile;
    
	FileManager(){
		JFrame frame = new JFrame("FileManager");
		frame.setLayout(new BorderLayout());
		table.getTableHeader().setReorderingAllowed(false);
		splist.setPreferredSize(new Dimension(200, -1));
		textfield.setText(address);
		
		getlist();//list 정보 받아옴
		
		list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String clicked;
                    back = new File(address, "..");
                    clicked = list.getSelectedValue();
                    if (clicked.equals("..")) {
                        try {
                        	address = back.getCanonicalPath();
                        } catch (Exception ee) {

                        }
                    } else {
                    	address = file.getPath() + File.separator + clicked;
                        if (address.contains("C:\\\\"))
                        	address = file.getPath() + clicked;
                    }
                    
                } catch (NullPointerException ee) {
                    
                }
                textfield.setText(address);
                getlist();
            }
        });
		sptable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu PopMenu = new JPopupMenu();
                    if (combobox.getSelectedItem() == "한글") {
                            PopMenu.add(ItemKshow);
                            PopMenu.add(ItemKcopy);
                            PopMenu.add(ItemKpaste);
                            PopMenu.add(ItemKdelete);
                    } else {
                            PopMenu.add(ItemEshow);
                            PopMenu.add(ItemEcopy);
                            PopMenu.add(ItemEpaste);
                            PopMenu.add(ItemEdelete);
                    }
                    PopMenu.show(table, e.getX(), e.getY());
                    PopMenu.setVisible(true);
                }
            }
        });
		table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu PopMenu = new JPopupMenu();
                    if (combobox.getSelectedItem() == "한글") {
                            PopMenu.add(ItemKshow);
                            PopMenu.add(ItemKcopy);
                            PopMenu.add(ItemKpaste);
                            PopMenu.add(ItemKdelete);
                    } else {
                            PopMenu.add(ItemEshow);
                            PopMenu.add(ItemEcopy);
                            PopMenu.add(ItemEpaste);
                            PopMenu.add(ItemEdelete);
                    }
                    PopMenu.show(table, e.getX(), e.getY());
                    PopMenu.setVisible(true);
                }
            }
        });
		ItemKshow.addActionListener(this);
		ItemKcopy.addActionListener(this);
		ItemKpaste.addActionListener(this);
		ItemKdelete.addActionListener(this);
		ItemEshow.addActionListener(this);
		ItemEcopy.addActionListener(this);
		ItemEpaste.addActionListener(this);
		ItemEdelete.addActionListener(this);
		
		splist.setViewportView(list);
		sptable.setViewportView(table);
		panel1.add(label,BorderLayout.WEST);
		panel1.add(combobox,BorderLayout.EAST);
		panel2.add(sptable,BorderLayout.CENTER);
		panel2.add(splist,BorderLayout.WEST);
		mainpanel.add(textfield, BorderLayout.NORTH);
		mainpanel.add(panel1,BorderLayout.SOUTH);
		mainpanel.add(panel2,BorderLayout.CENTER);
		frame.setSize(800,600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainpanel);
		frame.setVisible(true);
		
		combobox.addActionListener(e -> gettable());
    }
	@Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
        
    }

    public void actionPerformed(ActionEvent e) {
    	//삭제
        if (e.getSource() == ItemKdelete || e.getSource() == ItemEdelete) {
            int[] columns = table.getSelectedRows();
            for (int column : columns) {
                System.out.println(column);
                System.out.println(address + File.separator + table.getValueAt(column, 0));
            }
            for (int column : columns) {
                File delete = new File(address + File.separator + table.getValueAt(column, 0));
                delete.delete();
            }
            for (int i = 0; i < columns.length; i++)
                tablemodel.removeRow(columns[i] - i);
            tablemodel.fireTableDataChanged();
            table.updateUI();
        }
        //폴더로 보기
        if (e.getSource() == ItemKshow || e.getSource() == ItemEshow) {
            File open_Directory = new File(address);
            try {
                Desktop.getDesktop().open(open_Directory);
            } catch (IOException ee) {

            }
        }
        //복사
        if (e.getSource() == ItemKcopy || e.getSource() == ItemEcopy) {
        	StringSelection data;
			try {
				data = new StringSelection(new String(Files.readAllBytes(Paths.get(address)), StandardCharsets.UTF_8));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(data, data);
			} catch (IOException e1) {
			}
        	
        }
        //붙여넣기
        if (e.getSource() == ItemKpaste || e.getSource() == ItemEpaste) {
        	Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        	Transferable data = clipBoard.getContents(this);
        	try {
				String s = (String) data.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException | IOException e1) {
			}
        }
    }
    //테이블 만들기
  	private void gettable() {
          if (combobox.getSelectedItem() == "한글") {
          	tablemodel = new DefaultTableModel(listdata, titleK);
              table.setModel(tablemodel);
              label.setText("파일 관리자");
          }
          if (combobox.getSelectedItem() == "English") {
          	tablemodel = new DefaultTableModel(listdata, titleE);
              table.setModel(tablemodel);
              label.setText("File Manager");
          }
      }
	//리스트 만들기
	private void getlist() {
		file = new File(address);
		File[] directory_list = file.listFiles(File::isDirectory);
        File[] file_list = file.listFiles(File::isFile);
        
        int count = 1;
        listItem = new String[0];
        if (directory_list != null) {
        	listItem = new String[directory_list.length + 1];
            for (int i = -1; i < directory_list.length; i++) {
                if (i == -1) listItem[0] = "..";
                else {
                    if (directory_list[i].getName().contains("$") ||
                            directory_list[i].getName().contains("Recovery") ||
                            directory_list[i].getName().contains("System") ||
                            directory_list[i].getName().contains("Temp") ||
                            directory_list[i].getName().contains("PerfLogs") ||
                            directory_list[i].getName().contains("Documents and Settings") ||
                            !directory_list[i].canRead()) continue;

                    listItem[count] = directory_list[i].getName();
                    count++;
                }
            }
        }

        list.setListData(listItem);
        if (list.getVisibleRowCount() != 0)
            listdata = new String[0][3];
        if (file_list != null) {
            {
                listdata = new String[file_list.length][3];
                for (int i = 0; i < file_list.length; i++) {
                    listdata[i][0] = file_list[i].getName();
                    
                    String file_size;
                    long size = file_list[i].length();
                    if (size < 1024) {
                        file_size = String.format("%d B", size);
                    } else if (size < 1024 * 1024) {
                        file_size = String.format("%.2f KB", size / 1024.0);
                    } else if (size < 1024 * 1024 * 1024) {
                        file_size = String.format("%.2f MB", size / 1048576.0);
                    } else {
                        file_size = String.format("%.2f GB", size / 1073741824.0);
                    }
                    listdata[i][1] = file_size;
                    Date dt = new Date(file_list[i].lastModified());
                    SimpleDateFormat formatter = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
                    String date = formatter.format(dt);
                    listdata[i][2] = String.valueOf(date);
                }
            }

            gettable();
            
        } else {
            back = new File(address, "..");
            try {
            	address = back.getCanonicalPath();
            } catch (Exception ee) {

            }
            textfield.setText(address);
            
            getlist();

        }
    }
	
	public static void main(String[] args) {
		new FileManager();
	}
}
