package fr.galize.desktopsms.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.galize.desktopsms.ApplicationContexte;
import fr.galize.desktopsms.actions.Connection;
import fr.galize.desktopsms.actions.PreferenceSettings;
import fr.galize.desktopsms.actions.RefreshHisto;
import fr.galize.desktopsms.comm.Communication;
import fr.galize.desktopsms.model.Contact;
import fr.galize.desktopsms.model.Conversation;
import fr.galize.desktopsms.model.MainModel;
import fr.galize.desktopsms.model.PersistenceModel;
import fr.galize.desktopsms.ui.component.ButtonTabComponent;

public class MainGui extends JFrame {

	private static final long serialVersionUID = 8940535937031738625L;

	public MainGui() {
		super("Desktop SMS");
		setIconImage(AppRessources.DSMS_ICON.getImage());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){

			public void windowClosing(WindowEvent e) {
				if (ApplicationContexte.getId()==null)
					System.exit(0);
				File f = new File(ApplicationContexte.path2save+"/SMSDesktop/"+ApplicationContexte.getId()+"/histo.xml");
				System.out.println("Saving historique :"+f.getAbsolutePath());
				try {
					try {
						f.getParentFile().mkdirs();
					} catch (Exception e1) {
					}
					
					XMLEncoder encoder = new XMLEncoder(new FileOutputStream(f));
					PersistenceModel pm= new PersistenceModel();
					pm.setContacts(MainModel.getInstance().getContacts());
					pm.setEmits(MainModel.getInstance().getEmits());
					pm.setSmss(MainModel.getInstance().getSmss());
					pm.setLastDate(MainModel.getInstance().getLastDate());
					encoder.writeObject(pm);
					encoder.flush();
					encoder.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				System.out.println("Stopping application");
				System.exit(0);
			}

		});
		initFrame();
		//		pack();
	}

	private void initFrame() {
		final JTabbedPane pane = new JTabbedPane();
		final ContactListModel contacts = new ContactListModel();
		final JList list = new JList(contacts);
		// Menu
		JMenu menu = new JMenu("?");
		JMenuItem aboutMenu = new JMenuItem("About");
		aboutMenu.setMnemonic('A');
		aboutMenu.setAction(new AbstractAction("About"){

			private static final long serialVersionUID = -7395043876733277434L;

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(MainGui.this, "About...");
			}});

		menu.add(aboutMenu);
		JMenuBar barreMenu= new JMenuBar();
		barreMenu.add(menu);
		//setJMenuBar(barreMenu);
		// Barre d'etat
		JPanel statebarre= new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel lastReceive = new JLabel("--/--/---- --:--:--");
		if (MainModel.getInstance().getLastDate().getTime()>0)
			lastReceive.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(MainModel.getInstance().getLastDate()));
		MainModel.getInstance().addPropertyChangeListener("lastDate", new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				Date lastDate = MainModel.getInstance().getLastDate();
				SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				lastReceive.setText(sdf.format(lastDate));
			}});
		final JLabel statut = new JLabel(AppRessources.DISCONNECT_ICON);
		Communication.getInstance().addPropertyChangeListener("closed", new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				if (Communication.getInstance().isClosed())
				{
					//					statut.setText("Off");
					statut.setIcon(AppRessources.DISCONNECT_ICON);
				}
				else
				{
					statut.setIcon(AppRessources.CONNECT_ICON);
					//					statut.setText("On");

				}
			}});

		final JButton logbutton= new JButton(new AbstractAction("Show Logs",AppRessources.COMPUTER_ICON){

			private static final long serialVersionUID = 8751441815217104332L;

			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable(){

					public void run() {
						Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
						for (Entry<Thread, StackTraceElement[]> en:allStackTraces.entrySet())
						{
							String name2 = en.getKey().getName();
							System.out.println(name2);
							String string = Arrays.toString(en.getValue());
							System.out.println(string);
							System.out.println();
						}						
					}}).start();
				
				if (1>0) return;
				JScrollPane message = new JScrollPane(new JTextArea());
				JFrame p =new JFrame("Logs");
				p.getContentPane().add(message,BorderLayout.CENTER);
				p.setPreferredSize(new Dimension(400,400));
				p.pack();
				p.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				p.setVisible(true);
			}});
		JProgressBar progressBar=new JProgressBar();
		Communication.getInstance().register(progressBar);
		statebarre.add(statut);
		statebarre.add(lastReceive);
//		statebarre.add(progressBar);
		statebarre.add(logbutton);

		// Barre d'outils
		JPanel barre= new JPanel(new GridLayout(1,4));
		barre.add(new JButton(new Connection()));
		barre.add(new JButton(new RefreshHisto()));
		barre.add(new JLabel(""));
		barre.add(new JButton(new PreferenceSettings()));

		// Liste des contacts
		list.setCellRenderer(new ContactCellRenderer());
		list.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if ((e.getClickCount()>1&&e.getButton()==MouseEvent.BUTTON1) && (list.getSelectedIndex() > 0))
				{
					MainModel.getInstance().addConversation((Contact) contacts.getElementAt(list.getSelectedIndex()));
				}
			}

		});
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				int indexOfTab = pane.indexOfTab(((Contact) contacts.getElementAt(list.getSelectedIndex())).getId());
				pane.setSelectedIndex(indexOfTab);
			}});

		// Onglets de conversation
		MainModel.getInstance().addPropertyChangeListener("addConversation", new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				Conversation c = (Conversation) evt.getNewValue();

				int indexOfTabExisting = pane.indexOfTab(((Contact) contacts.getElementAt(list.getSelectedIndex())).getId());
				if (indexOfTabExisting!=-1)
				{
					pane.setSelectedIndex(indexOfTabExisting);
					return;
				}
				String title = c.getId();
				Component add = pane.add(title, new ConversationPanel(c));
				int indexOfTab = pane.indexOfComponent(add);
				// Incompatible MAC
				try {
					pane.setTabComponentAt(indexOfTab,
							new ButtonTabComponent(pane));
				} catch (Exception e) {}
				pane.setSelectedIndex(indexOfTab);

			}});
		/*pane.removeAll();
        for (int i = 0; i < 5; i++) {
            String title = "Tab " + i;
            pane.add(title, new JLabel(title));
            pane.setTabComponentAt(i,
                    new ButtonTabComponent(pane));

        }*/
		pane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);


		// Content Pane
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(new JScrollPane(list));
		split.setRightComponent(pane);
		setSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		setVisible(true);
		
		JPanel barrePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		barrePane.add(barre);
		getContentPane().add(split, BorderLayout.CENTER);
		getContentPane().add(barrePane, BorderLayout.NORTH);
		getContentPane().add(statebarre, BorderLayout.SOUTH);

	}

	public static void main(String[] args) {
		
		for (UIManager.LookAndFeelInfo laf :
			UIManager.getInstalledLookAndFeels() ){
			if ("Nimbus".equals(laf.getName())) {
				try {
					UIManager.setLookAndFeel(laf.getClassName());

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		ApplicationContexte.init();
		/*if (!DEBUG)
		try {
			PipedOutputStream pout=new PipedOutputStream(pin);
			System.setErr(new PrintStream(pout,true));
		} catch (java.io.IOException io) {

		} catch (SecurityException se) {
		}*/
		AppRessources.loadImages();
		try {
			SwingUtilities.invokeAndWait(new Runnable(){

				public void run() {
					new MainGui().setVisible(true);
				}});
		} catch (Exception e) {
		}
		MainModel.getInstance();


	}
}
