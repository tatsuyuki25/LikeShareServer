package com.likeshare.server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerUI extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7223003568191993181L;
	private JPanel contentPane;
	private static JTextArea info = null;
	private JScrollPane scrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ServerUI frame = new ServerUI();
					frame.setVisible(true);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ServerUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,486,380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5,5,5,5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		final JButton btnOpenServer = new JButton("open server");

		btnOpenServer.setBounds(361,50,109,23);
		contentPane.add(btnOpenServer);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10,10,353,322);
		getContentPane().add(scrollPane);

		info = new JTextArea();
		scrollPane.setViewportView(info);
		contentPane.add(scrollPane);

		btnOpenServer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MultServer ms = new MultServer();
				ms.start();
				btnOpenServer.setEnabled(false);
				append("Server Run");
			}
		});
	}

	public static void append(String str)
	{
		SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
		info.append(df.format(new Date()) + " > " + str + "\n");

	}
}
