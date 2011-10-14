/**
 * 
 */
package fr.galize.desktopsms.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import fr.galize.desktopsms.comm.Communication;
import fr.galize.desktopsms.model.Conversation;
import fr.galize.desktopsms.model.SMSSender;

final class SendSMS extends AbstractAction {
	/**
	 * 
	 */
	private Conversation conversation;
	private JTextField field;
	private static final long serialVersionUID = -6376296068167825254L;

	SendSMS(JTextField field) {
		super("Send SMS");
		this.conversation = null;
		this.field = field;
	}

	SendSMS(Conversation conversation, String name, JTextField field) {
		super(name);
		this.conversation = conversation;
		this.field = field;
	}

	public void actionPerformed(ActionEvent e) {
		final String text;
		final String number;
		if (conversation == null) {
			text = JOptionPane.showInputDialog(null, "Message", "Message", JOptionPane.PLAIN_MESSAGE);
			if ((text == null) || (text.length() == 0)) {
				return;
			}
			number = field.getText();
		}
		else {
			text = field.getText();
			number = conversation.getContact().getNumber();
		}
		field.setText("");
		new Thread(new Runnable(){

			public void run() {
				String body = text;
				long id=0;
				id = Communication.getInstance().sendSMS(number, body);
				if (id>0) {
					if (conversation != null) {
						SMSSender.getInstance().register(number,body,conversation,id);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(field, "Error while sending", "Sending error", JOptionPane.ERROR_MESSAGE);
					field.setText(text);
				}

			}},"SendSMS").start();

	}

}
