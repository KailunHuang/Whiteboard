package Connections;
import CreateWhiteBoard.Manager;
import JoinWhiteBoard.joiner;

import java.awt.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Connection {

	private JFrame StartConnection;
	static boolean MExisted = false;
	static boolean jExisted = false;


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Connection window = new Connection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		while(true){
			System.out.println("Waiting...");
			if(MExisted){
				Manager manager = new Manager();
			}
			if(jExisted){
				jExisted = false;

				joiner j = new joiner();
			}
		}
	}


	public Connection() {
		initialize();
	}

	private void initialize() {

        StartConnection = new JFrame();
        StartConnection.getContentPane().setBackground(new Color(60, 179, 113));
        StartConnection.setTitle("Whiteborad");
        StartConnection.setBounds(100, 100, 256, 235);
        StartConnection.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StartConnection.getContentPane().setLayout(null);

        // Initial the components
        JLabel lblWelcome = new JLabel("WhiteBoard");
        JButton btnCreate = new JButton("Create");
        JButton btnNewButton = new JButton("Join");

        lblWelcome.setFont(new Font("YuKyokasho", Font.BOLD, 25));
        lblWelcome.setBounds(52, 27, 144, 38);
        btnCreate.setBounds(30, 109, 184, 29);
        btnNewButton.setBounds(30, 150, 184, 29);

        StartConnection.getContentPane().add(lblWelcome);
        StartConnection.getContentPane().add(btnCreate);
        StartConnection.getContentPane().add(btnNewButton);

        JLabel lblNewLabel = new JLabel("NoBug Team");
        lblNewLabel.setBounds(158, 67, 98, 16);
        StartConnection.getContentPane().add(lblNewLabel);

        StartConnection.setVisible(true);


        btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					StartConnection.setVisible(false);
					MExisted = true;
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					StartConnection.setVisible(false);
					jExisted = true;
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}
}
