/**
 * 
 */
package apu.scratch.hax;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * @author "MegaApuTurkUltra"
 * 
 */
public class GuiHackerProjectsView extends JPanel {
	public static class Project {
		public String title;
		public int id;

		public String toString() {
			return title;
		}

		public Project(String title, int id) {
			this.title = title;
			this.id = id;
		}
	}

	private static final long serialVersionUID = -8777426689312871264L;
	public JList<Project> list;
	public DefaultListModel<Project> listModel;
	public JButton btnChangeIt;
	private JScrollPane scrollPane;

	/**
	 * Create the panel.
	 */
	public GuiHackerProjectsView() {
		setLayout(new BorderLayout(0, 0));

		JLabel lblSelectAProject = new JLabel(
				"Select a project to change its thumbnail");
		add(lblSelectAProject, BorderLayout.NORTH);

		btnChangeIt = new JButton("Change it!");
		btnChangeIt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() == -1)
					return;
				Project proj = list.getSelectedValue();
				ThumbnailGuiHacker.INSTANCE.goToFileChooser(proj);
			}
		});
		btnChangeIt.setEnabled(false);
		add(btnChangeIt, BorderLayout.SOUTH);
		listModel = new DefaultListModel<Project>();

		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		list = new JList<Project>();
		list.setEnabled(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setModel(new AbstractListModel<Project>() {
			private static final long serialVersionUID = -8079045657394409854L;
			@Override
			public int getSize() {
				return 1;
			}
			@Override
			public Project getElementAt(int index) {
				return new Project("Loading...", -1);
			}
		});
		scrollPane.setViewportView(list);
	}
}
