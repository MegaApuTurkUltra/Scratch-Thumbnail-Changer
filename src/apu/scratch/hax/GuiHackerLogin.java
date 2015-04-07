/**
 * 
 */
package apu.scratch.hax;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Login screen
 * Generated using WindowBuilder for Eclipse
 * @author "MegaApuTurkUltra"
 * 
 */
public class GuiHackerLogin extends JPanel {
	private static final long serialVersionUID = -6357875066223412629L;
	private JTextField username;
	private JPasswordField password;
	private JButton btnLogIn;

	/**
	 * Create the panel.
	 */
	public GuiHackerLogin() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				1.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblLogInTo = new JLabel("Log in to your Scratch Account");
		lblLogInTo.setFont(new Font("Tahoma", Font.PLAIN, 20));
		GridBagConstraints gbc_lblLogInTo = new GridBagConstraints();
		gbc_lblLogInTo.gridwidth = 2;
		gbc_lblLogInTo.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogInTo.gridx = 0;
		gbc_lblLogInTo.gridy = 0;
		add(lblLogInTo, gbc_lblLogInTo);
		
		JLabel lblThumbnailHacker = new JLabel("Thumbnail Hacker - A swaggy program by MegaApuTurkUltra");
		GridBagConstraints gbc_lblThumbnailHacker = new GridBagConstraints();
		gbc_lblThumbnailHacker.gridwidth = 2;
		gbc_lblThumbnailHacker.insets = new Insets(0, 0, 5, 5);
		gbc_lblThumbnailHacker.gridx = 0;
		gbc_lblThumbnailHacker.gridy = 1;
		add(lblThumbnailHacker, gbc_lblThumbnailHacker);

		JLabel lblUsername = new JLabel("Username:");
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.anchor = GridBagConstraints.EAST;
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.gridx = 0;
		gbc_lblUsername.gridy = 2;
		add(lblUsername, gbc_lblUsername);

		username = new JTextField();
		GridBagConstraints gbc_username = new GridBagConstraints();
		gbc_username.insets = new Insets(0, 0, 5, 0);
		gbc_username.fill = GridBagConstraints.HORIZONTAL;
		gbc_username.gridx = 1;
		gbc_username.gridy = 2;
		add(username, gbc_username);
		username.setColumns(10);

		JLabel lblPassword = new JLabel("Password:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 3;
		add(lblPassword, gbc_lblPassword);

		password = new JPasswordField();
		GridBagConstraints gbc_password = new GridBagConstraints();
		gbc_password.insets = new Insets(0, 0, 5, 0);
		gbc_password.fill = GridBagConstraints.HORIZONTAL;
		gbc_password.gridx = 1;
		gbc_password.gridy = 3;
		add(password, gbc_password);

		btnLogIn = new JButton("Log In");
		btnLogIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						username.setEnabled(false);
						password.setEnabled(false);
						btnLogIn.setEnabled(false);
					}
				});
				new Thread(new Runnable(){
					@Override
					public void run() {
						boolean success = ThumbnailGuiHacker.INSTANCE
								.initAndLogin(username.getText(),
										password.getPassword());
						boolean doCont = success;
						if (!success) {
							doCont = JOptionPane.showConfirmDialog(
									ThumbnailGuiHacker.INSTANCE,
									"Login might have failed. Continue?",
									"Message", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
						}
						if (!doCont) {
							username.setEnabled(true);
							password.setEnabled(true);
							btnLogIn.setEnabled(true);
						} else {
							ThumbnailGuiHacker.INSTANCE.goToProjects();
						}
					}
				}).start();
			}
		});
		btnLogIn.setFont(new Font("Tahoma", Font.PLAIN, 20));
		GridBagConstraints gbc_btnLogIn = new GridBagConstraints();
		gbc_btnLogIn.insets = new Insets(0, 0, 5, 0);
		gbc_btnLogIn.fill = GridBagConstraints.BOTH;
		gbc_btnLogIn.gridx = 1;
		gbc_btnLogIn.gridy = 5;
		add(btnLogIn, gbc_btnLogIn);

		JTextArea txtrNoteYourLogin = new JTextArea();
		txtrNoteYourLogin.setFont(new Font("Monospaced", Font.ITALIC, 13));
		txtrNoteYourLogin
				.setText("Note: Your login info is not collected in any way. It is discarded once this application closes");
		txtrNoteYourLogin.setBackground(UIManager.getColor("Panel.background"));
		txtrNoteYourLogin.setEnabled(false);
		txtrNoteYourLogin.setEditable(false);
		txtrNoteYourLogin.setWrapStyleWord(true);
		txtrNoteYourLogin.setLineWrap(true);
		GridBagConstraints gbc_txtrNoteYourLogin = new GridBagConstraints();
		gbc_txtrNoteYourLogin.insets = new Insets(0, 0, 5, 0);
		gbc_txtrNoteYourLogin.gridwidth = 2;
		gbc_txtrNoteYourLogin.fill = GridBagConstraints.BOTH;
		gbc_txtrNoteYourLogin.gridx = 0;
		gbc_txtrNoteYourLogin.gridy = 6;
		add(txtrNoteYourLogin, gbc_txtrNoteYourLogin);

	}
}
