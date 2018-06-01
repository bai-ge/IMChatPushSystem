package com.baige.imchat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.baige.ApplicationConfig;
import com.baige.callback.PushCallback;
import com.baige.data.entity.Candidate;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.button.StandardButtonShaper;

import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.callback.CallbackManager;
import com.baige.connect.msg.MessageManager;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.IPUtil;
import com.baige.util.LogBean;
import com.baige.util.LogHelper;
import com.baige.util.LogHelper.LogListener;
import com.baige.util.LogLevel;
import com.baige.util.Tools;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.awt.Font;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JTextPane logPane;

	private Color defaulColor = new Color(230, 180, 180);
	private Color dateColor = new Color(192, 192, 192);
	private Color headerColor = new Color(128, 230, 210);
	private Color separatorColor = new Color(198, 244, 1);
	private Color verboseColor = new Color(230, 180, 180);
	private Color debugColor = new Color(230, 180, 180);
	private Color infoColor = new Color(230, 180, 180);
	private Color warnColor = new Color(255, 255, 0);
	private Color errorColor = new Color(255, 0, 0);

	private LogHelper logHelper;

	private LogLevel level = LogLevel.DEBUG;

	public final static SimpleDateFormat dateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public NetServerManager netServerManager;
	
	private CacheRepository cacheRepository;

	private String header;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// 要使用上述皮肤很简单，只要在main函数中调用下面代码即可：
					// 设置外观
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					// JFrame.setDefaultLookAndFeelDecorated(true);
					// 设置主题
					// SubstanceLookAndFeel.setCurrentTheme(new
					// SubstanceEbonyTheme());
					// 设置按钮外观
					SubstanceLookAndFeel.setCurrentButtonShaper(new StandardButtonShaper());

					// 主要设置皮肤、主题还有按钮、水印、选项卡、滑动条以及水印等

					// UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");

					// UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeSilver2007LookAndFeel");

					// UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceSaharaLookAndFeel");

					// UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel");

					// UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel");

					// 设置外观
					// UIManager.setLookAndFeel(new SubstanceLookAndFeel());

					// 改变标题栏
					// JFrame.setDefaultLookAndFeelDecorated(true);

					// 设置主题
					// SubstanceLookAndFeel.setCurrentTheme(new
					// SubstanceCremeTheme());//SubstanceCremeTheme

					// 设置按钮外观
					// SubstanceLookAndFeel.setCurrentButtonShaper(new
					// StandardButtonShaper());

					// 设置皮肤
					// SubstanceLookAndFeel.setSkin(new OfficeBlue2007Skin());

					// 设置水印
					// SubstanceLookAndFeel.setCurrentWatermark(new
					// SubstanceBinaryWatermark());
					// 设置边框

					// SubstanceLookAndFeel.setCurrentBorderPainter(new
					// StandardBorderPainter());
					// 设置渐变渲染
					// SubstanceLookAndFeel.setCurrentGradientPainter(new
					// StandardGradientPainter());

					// 设置标题
					// SubstanceLookAndFeel.setCurrentTitlePainter(new
					// FlatTitlePainter());

					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 50, 1150, 600);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		JMenu mnNewMenu = new JMenu("选项");
		menuBar.add(mnNewMenu);

		JMenuItem mntmHelp = new JMenuItem("帮助");
		mntmHelp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

			}
		});
		mnNewMenu.add(mntmHelp);

		JMenuItem mntmShell = new JMenuItem("Shell窗口");
		mntmShell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getSource().toString());
				ShellFrame shellFrame = new ShellFrame();
				// TODO设置监听器
				shellFrame.registerShellListener(shellListener);
				shellFrame.setVisible(true);
			}
		});
		// 调用将加速器ctrl+T关联到mntmShell菜单项,T一定要大写
		mntmShell.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
		mnNewMenu.add(mntmShell);

		// 添加分割栏
		mnNewMenu.addSeparator();

		JMenuItem mntmSaveLog = new JMenuItem("保存日志");
		mntmSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveLog(logHelper.getLogs());
			}
		});
		mntmSaveLog.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		mnNewMenu.add(mntmSaveLog);

		JMenuItem mntmClearLog = new JMenuItem("清除日志");
		mntmClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		mnNewMenu.add(mntmClearLog);

		JMenu mnNewMenu_1 = new JMenu("日志输出等级");
		mnNewMenu.add(mnNewMenu_1);

		ButtonGroup group = new ButtonGroup();
		JRadioButton rdbtnVerbose = new JRadioButton("Verbose");
		rdbtnVerbose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				level = LogLevel.VERBOSE;
				clearScreen();
				showLog(logHelper.getLogs());
			}
		});
		mnNewMenu_1.add(rdbtnVerbose);
		group.add(rdbtnVerbose);

		JRadioButton rdbtnDebug = new JRadioButton("Debug");
		rdbtnDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				level = LogLevel.DEBUG;
				clearScreen();
				showLog(logHelper.getLogs());
			}
		});
		mnNewMenu_1.add(rdbtnDebug);
		group.add(rdbtnDebug);

		JRadioButton rdbtnInfo = new JRadioButton("Info");
		rdbtnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				level = LogLevel.INFO;
				clearScreen();
				showLog(logHelper.getLogs());
			}
		});
		mnNewMenu_1.add(rdbtnInfo);
		group.add(rdbtnInfo);

		JRadioButton rdbtnWarn = new JRadioButton("Warn");
		rdbtnWarn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				level = LogLevel.WARN;
				clearScreen();
				showLog(logHelper.getLogs());
			}
		});
		mnNewMenu_1.add(rdbtnWarn);
		group.add(rdbtnWarn);

		JRadioButton rdbtnError = new JRadioButton("Error");
		rdbtnError.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				level = LogLevel.ERROR;
				clearScreen();
				showLog(logHelper.getLogs());
			}
		});
		mnNewMenu_1.add(rdbtnError);
		group.add(rdbtnError);

		rdbtnDebug.setSelected(true);
		level = LogLevel.DEBUG;

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		header = "root@" + System.getProperty("user.name");
		logPane = new JTextPane() {
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}

			public void setSize(Dimension d) {
				if (d.width < getParent().getSize().width) {
					d.width = getParent().getSize().width;
				}
				d.width += 100;
				super.setSize(d);
			}
		};
		logPane.setBackground(new Color(43, 43, 43));
		logPane.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
		logPane.setEditable(false);
		scrollPane.setViewportView(logPane);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				super.windowClosing(e);
				//TODO 保存服务器的连接信息
			}
		});

		
		
		logHelper = LogHelper.getInstance();
		logHelper.setLogListener(logListener);
		
		cacheRepository = CacheRepository.getInstance();
		netServerManager = NetServerManager.getInstance();
	}

	public void showLog(List<LogBean> logs) {
		if (logs == null || logs.isEmpty()) {
			return;
		}
		for (LogBean log : logs) {
			if (log != null && log.getLevel().compareTo(level) >= 0) {
				AttributeSet attributeSet = getAttribte(debugColor);
				switch (log.getLevel()) {
				case VERBOSE:
					attributeSet = getAttribte(verboseColor);
					break;
				case DEBUG:
					attributeSet = getAttribte(debugColor);
					break;
				case INFO:
					attributeSet = getAttribte(infoColor);
					break;
				case WARN:
					attributeSet = getAttribte(warnColor);
					break;
				case ERROR:
					attributeSet = getAttribte(errorColor);
					break;
				default:
					break;
				}
				insert(logPane.getDocument(), dateFromat.format(new Date(log.getTime())).toString() + "    ",
						getAttribte(dateColor));
				insert(logPane.getDocument(), log.getLevel() + ":" + log.getFrom(), getAttribte(headerColor));
				insert(logPane.getDocument(), "~$ ", getAttribte(separatorColor));
				insert(logPane.getDocument(), log.getText() + "\n", attributeSet);
			}
		}

	}

	public void clearScreen() {
		try {
			logPane.getDocument().remove(0, logPane.getDocument().getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearLog() {
		try {
			logPane.getDocument().remove(0, logPane.getDocument().getLength());
			logHelper.getLogs().clear();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveLog(List<LogBean> logs) {

		System.out.println("保存日志");
		if (logs != null && !logs.isEmpty()) {
			FileDialog fileDialog = new FileDialog(this, "保存日志", FileDialog.SAVE);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			fileDialog.setFile(formatter.format(new Date()) + ".txt");
			fileDialog.setVisible(true);
			String fileName = fileDialog.getFile();
			if (fileName == null) {
				return;
			}
			File file = new File(fileDialog.getDirectory() + fileName);
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PrintWriter writer = null;
			try {
				FileOutputStream out = new FileOutputStream(file);
				writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));// 发送数据
				for (LogBean log : logs) {
					if (log.getLevel().compareTo(level) >= 0) {
						writer.println(log.toString());
					}
				}
				writer.flush();
				writer.close();
				writer = null;
				logHelper.debug(this, "保存日志成功" + file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}

	}

	private AttributeSet getAttribte(Color color) {
		SimpleAttributeSet attrSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSet, color);
		return attrSet;
	};

	private void insert(Document doc, String str, AttributeSet attrSet) {
		try {
			synchronized (doc.getClass()) {// 插入数据是进行同步处理
				doc.insertString(doc.getLength(), str, attrSet);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private LogListener logListener = new LogListener() {

		@Override
		public synchronized void showLog(LogBean log) {
			// TODO Auto-generated method stub
			if (log != null && log.getLevel().compareTo(level) >= 0) {
				AttributeSet attributeSet = getAttribte(debugColor);
				switch (log.getLevel()) {
				case VERBOSE:
					attributeSet = getAttribte(verboseColor);
					break;
				case DEBUG:
					attributeSet = getAttribte(debugColor);
					break;
				case INFO:
					attributeSet = getAttribte(infoColor);
					break;
				case WARN:
					attributeSet = getAttribte(warnColor);
					break;
				case ERROR:
					attributeSet = getAttribte(errorColor);
					break;
				default:
					break;
				}
				insert(logPane.getDocument(), dateFromat.format(new Date(log.getTime())).toString() + "    ",
						getAttribte(dateColor));
				insert(logPane.getDocument(), "["+log.getLevel() + "]:" + log.getFrom(), getAttribte(headerColor));
				insert(logPane.getDocument(), "~$ ", getAttribte(separatorColor));
				insert(logPane.getDocument(), log.getText() + "\n", attributeSet);
				System.out.println(log.toString());
			}
		}

	};
	private OnShellListener shellListener = new OnShellListener.SimpleOnShellListener() {
		private  String RegexClientCmd = "^client\\s-l$";
		private  String RegexUdpCmd = "(udp\\s-t\\s(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)[:\\s](6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3}))|(udp\\s-t)";
		
		private boolean canExecute(String command){
			if(Tools.isEmpty(command)){
				return false;
			}
			if(command.matches(RegexClientCmd) || command.matches(RegexUdpCmd)){
				return true;
			}
			return false;
		}
		public int usage(StringBuffer result) {
			// TODO Auto-generated method stub
//			result.append("添加服务器\t\tserver <ip>\n");
//			result.append("查看服务器\t\tserver -all\n");
//			result.append("删除服务器\t\tserver -rm <ip>\n");
			result.append("查看在线客户\tclient -l\n");
			result.append("查看远程在线客户\tclient -a\n");
			result.append("P2P连接\t\tptp -t id\n");
			result.append("UDP连接测试\tudp -t [<ip><port>]\n");
			return 0;
		}

		public void showError(StringBuffer error) {
			// TODO Auto-generated method stub

		}

		public String getHeader() {
			// TODO Auto-generated method stub
			return header;
		}
		
		public int autoCompletion(StringBuffer result, String command) {
			// TODO Auto-generated method stub
			CacheRepository cacheRepository = CacheRepository.getInstance();
			List<String> list = cacheRepository.startWith(command);
			if(list.size() == 1){
				result.append(list.get(0));
				return 1;
			}else if(list.size() > 1){
				for(String key : list){
					result.append(key+" ");
				}
			}
			return list.size();
		}
		@Override
		public PipedInputStream execute(String command) {
			// TODO Auto-generated method stub
			if(canExecute(command)){
				final PipedOutputStream out = new PipedOutputStream();
				PipedInputStream ins = new PipedInputStream();
				try {
					ins.connect(out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new Thread(){
					public void run() {
						String[] cmds = command.split(" "); // 分解
						boolean  res;
						StringBuffer result = new StringBuffer();
						try{
							if (cmds[0].equals("client")) {
								for (int i = 1; i < cmds.length; i++) {
									if (cmds[i].equals("-l")) {
										// 所有详情
										System.out.println("准备获取所有详情");
										List<DeviceModel> devices = netServerManager.getAllDevices();
										System.out.println("输出所有设备详情");

										// deviceId = deviceId,localAddress =
										// localIp:localPort,remoteAddress =
										// remoteIp:remotePort,localUdpPort =;
										// localUdpPort,remoteUdpPort = remoteUdpPort;

										String headerTab = String.format("%-17s\t%-25s\t%-25s\t%-17s\t%-17s\n", "device Id","local address","remote address","local udpPort","remote udpPort");
										out.write(headerTab.getBytes());
										
										for (int j = 0; j < devices.size(); j++) {
											int len = devices.get(j).getDeviceId().length();
											
											String IDstr = devices.get(j).getDeviceId().substring(0, 4) + "***"
													+ devices.get(j).getDeviceId().substring(len - 4, len - 1);
											StringBuffer deviceMsg = new StringBuffer();
											deviceMsg.append(String.format("%-17s\t", IDstr));
											deviceMsg.append(String.format("%-25s\t", devices.get(j).getLocalIp() + ":" + devices.get(j).getLocalPort()));
											if(devices.get(j).getConnectedByTCP() == null || !devices.get(j).getConnectedByTCP().isConnected()){
												deviceMsg.append(String.format("#%-25s\t",devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()));
											}else{
												deviceMsg.append(String.format("%-25s\t",devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()));
											}
											deviceMsg.append(String.format("%-17s\t",devices.get(j).getLocalUdpPort()));
											deviceMsg.append(String.format("%-17s\t",devices.get(j).getRemoteUdpPort()) + "\n");
											
//											deviceMsg.append(IDstr + "\t");
//											deviceMsg.append(devices.get(j).getLocalIp() + ":" + devices.get(j).getLocalPort() + " \t");
//											deviceMsg.append(devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()+ " \t");
//											deviceMsg.append(devices.get(j).getLocalUdpPort() + " \t\t\t" );
//											deviceMsg.append(devices.get(j).getRemoteUdpPort() + "\n");
											out.write(deviceMsg.toString().getBytes());
										}
										out.close();
									} else {
										System.out.println("无法识别命令" + cmds[i]);
									}
								}

							}else if(cmds[0].equals("udp") && cmds.length >= 2){
								if(cmds[1].equals("-t")){
									String callbackId = Tools.ramdom();
									PushCallback pushCallback = new PushCallback(){
										@Override
										public void loadObject(Object obj) {
											super.loadObject(obj);
											if(obj instanceof Candidate){
												Candidate candidate = (Candidate) obj;
												StringBuffer stringBuffer = new StringBuffer();
												stringBuffer.append("From\t"+candidate.getFrom()+"\n");

												stringBuffer.append("Local\t"+candidate.getLocalIp()+":"+candidate.getLocalPort()+"\n");
												stringBuffer.append("Remote\t"+candidate.getRemoteIp()+":"+candidate.getRemotePort()+"\n");
												stringBuffer.append("Relay\t"+candidate.getRelayIp()+":"+candidate.getRelayPort()+"\n");
												stringBuffer.append("Delay\t"+candidate.getDelayTime()+" ms"+"\n\n");
												try{
													out.write(stringBuffer.toString().getBytes());
													out.flush();
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
											this.finishOneTask();
										}

										public void timeout() {
											try {
												String text = new String("时间超时\n".getBytes(), Tools.DEFAULT_ENCODE);
												out.write(text.getBytes());
												out.flush();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											this.finishOneTask();
										};
										public void onFinish(){
											try {
												out.close();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									};
									
									String localIp = IPUtil.getLocalIPAddress(true);
									String localPort = netServerManager.getDatagramSocketServer().getLocalPort()+"";
									//out.write(("Local\t"+localIp + ":"+localPort+"\n\n").getBytes());
									String msg = MessageManager.udpTest(CacheRepository.getInstance().getDeviceId(), callbackId, localIp, localPort);
									if(cmds.length == 2){
										pushCallback.setTaskcount(2);
										pushCallback.setId(callbackId);
										pushCallback.setTimeout(8000);
										CallbackManager.getInstance().put(pushCallback);
										
										ConnectedByUDP connectedByUDP = netServerManager.getUDPConnectorByAddress(ApplicationConfig.mainServerIp, 12059);
										connectedByUDP.sendString(msg);
										
										connectedByUDP = netServerManager.getUDPConnectorByAddress(ApplicationConfig.secondaryServerIp, 12059);
										connectedByUDP.sendString(msg);
										
										//TODO 默认地址
//										serverNet.sendMessage(ApplicationConfig.mainServerIp, 12059, MessageManager.udpLogin(serverNet.getid()));
//										
//										serverNet.sendMessage(ApplicationConfig.secondaryServerIp, 12059, MessageManager.udpLogin(serverNet.getid()));
//										
//										result.append("发送udp到 120.78.148.* 成功\n");
//										result.append("发送udp到 39.108.74.* 成功\n");
									}else if(cmds.length == 4){
										pushCallback.setTaskcount(1);
										pushCallback.setId(callbackId);
										pushCallback.setTimeout(8000);
										CallbackManager.getInstance().put(pushCallback);
										String ip = cmds[2];
										int port = Integer.valueOf(cmds[3]);
										
										ConnectedByUDP connectedByUDP = netServerManager.getUDPConnectorByAddress(ip, port);
										connectedByUDP.sendString(msg);
										
//										serverNet.sendMessage(ip, port, MessageManager.udpLogin(serverNet.getid()));
//										result.append("发送udp到 "+ip+":"+port+"\n");
										//TODO waiting
									}
								}
							}
						}catch(IOException e){
							e.printStackTrace();
							try {
								out.write(e.getMessage().getBytes());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					};
				}.start();
				return ins;
			}
			return null;
		}
	};

}
