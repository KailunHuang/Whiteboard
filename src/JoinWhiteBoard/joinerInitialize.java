package JoinWhiteBoard;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.*;

public class joinerInitialize {

	private JFrame frame;
	private JTextField textFieldServer;
	private String serverIPAddress;
	private Socket socket;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					joinerInitialize window = new joinerInitialize();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public joinerInitialize() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 329, 304);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblIpAddressserver = new JLabel("IP Address (Server)");
		lblIpAddressserver.setBounds(103, 180, 131, 28);
		frame.getContentPane().add(lblIpAddressserver);

		JLabel lblLogo = new JLabel("");
		lblLogo.setIcon(new ImageIcon(joinerInitialize.class.getResource("/resources/logo.png")));
		lblLogo.setBounds(6, 16, 240, 152);
		frame.getContentPane().add(lblLogo);

		textFieldServer = new JTextField();
		textFieldServer.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldServer.setText("192.168.43.200");
		textFieldServer.setBounds(76, 211, 188, 26);
		frame.getContentPane().add(textFieldServer);
		textFieldServer.setColumns(10);

		ActionListener connectRequest = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverIPAddress = textFieldServer.getText();
				System.out.println("目标IP是：" + serverIPAddress);
				try {
					socket = new Socket(serverIPAddress, 8888);
					//---------更新在线用户👇----------
//					joiner.updateThread updateThread = new joiner.updateThread(socket.getLocalPort() - 3000);
//					updateThread.start();

					//-------传输画板数据👇---------
					OutputStream outputStream = socket.getOutputStream();
					InputStream inputStream = socket.getInputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					String ServerAns = bufferedReader.readLine();
					System.out.println(ServerAns);
					if(ServerAns.equals("ack") ){
						System.out.println("与Server建立连接成功!");
						frame.setVisible(false);
						joiner joinerAccpet = new joiner(serverIPAddress, socket);
					}else{
						// Access Denied!
						System.out.println("与Server建立连接失败!");
						JOptionPane.showMessageDialog(null, " Access Denied! ", " Denied", JOptionPane.ERROR_MESSAGE);
					}
				} catch (ConnectException connect) {
					System.out.println("Server的IP地址错误!");
					JOptionPane.showMessageDialog(null, " Connection Error! ", " Error", JOptionPane.ERROR_MESSAGE);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};

		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(106, 247, 117, 29);
		btnConnect.addActionListener(connectRequest);
		frame.getContentPane().add(btnConnect);
	}

	public Socket getSocket(){
		return socket;
	}
}
