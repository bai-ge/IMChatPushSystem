package com.baige.imchat;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.button.StandardButtonShaper;

import com.baige.util.LogHelper;
import com.baige.util.Tools;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import java.awt.Color;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Array;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ShellFrame extends JFrame implements OnShellListener {

	/*
	 * 系统内置命令 清屏 cls 输出错误 err 使用规则 -usage
	 */

	public final static String copyright = "Copyright(C) 2017@baige Shell Version 1.0";
	private JPanel contentPane;
	private JTextPane textPane;
	private int editControl;
	private String userPath;
	private ExecutorService fixedThreadPool;

	private boolean isExecuting = false;

	private Color defaulColor = new Color(230, 180, 180);
	private Color headerColor = new Color(255, 0, 0);
	private Color separatorColor = new Color(198, 244, 1);
	private Color commandColor = new Color(240, 240, 240);
	private Color resultColor = new Color(128, 255, 255);
	private Color autoColor = new Color(255, 240, 0);
	private ArrayList<String> commands;
	private int commandIndex;

	private ArrayList<OnShellListener> onShellListeners;
	private Object shellListenerLock = new Object();

	private Thread executeThread;

	private boolean keyEnter = false;
	private boolean keyUp = false;
	private boolean keyDown = false;
	private boolean keyTab = false;

	public boolean isExecuting() {
		return isExecuting;
	}

	public void setExecuting(boolean isExecuting) {
		this.isExecuting = isExecuting;
	}

	public Thread getRunningThread() {
		return executeThread;
	}

	public void setRunningThread(Thread thread) {
		synchronized (ShellFrame.class) {
			if (getRunningThread() != null && !getRunningThread().isInterrupted()) {
				getRunningThread().interrupt();
				executeThread = thread;
			} else {
				executeThread = thread;
			}
		}
	}

	public ArrayList<OnShellListener> getOnShellListeners() {
		if (onShellListeners == null) {
			synchronized (shellListenerLock) {
				if (onShellListeners == null) {
					onShellListeners = new ArrayList<>();
				}
			}

		}
		return onShellListeners;
	}

	public void setOnShellListeners(ArrayList<OnShellListener> onShellListeners) {
		synchronized (shellListenerLock) {
			this.onShellListeners = onShellListeners;
		}
	}

	public void registerShellListener(OnShellListener listener) {
		synchronized (shellListenerLock) {
			if (!getOnShellListeners().contains(listener)) {
				getOnShellListeners().add(listener);
			}
		}
	}

	public void UnregisterShellListener(OnShellListener listener) {
		synchronized (shellListenerLock) {
			getOnShellListeners().remove(listener);
		}
	}

	class ExecuteThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			System.out.println("开始执行");
			textPane.setEditable(false);
			setExecuting(true);
			String command = getCommand();
			if (!Tools.isEmpty(command)) {
				commands.add(command);
				if (commands.size() > 100) {
					commands.remove(0);
				}

				commandIndex = commands.size();
				PipedInputStream ins = execute(command);
				if (ins != null) {
					byte[] buf = new byte[1024];
					int size = 0;
					try {
						while (!Thread.interrupted() && (size = ins.read(buf)) != -1) {
							String result = Tools.dataToString(Arrays.copyOfRange(buf, 0, size), Tools.DEFAULT_ENCODE);
							if (!Tools.isEmpty(result)) {
								insert(textPane.getDocument(), result, getAttribte(resultColor));
								textPane.setCaretPosition(textPane.getDocument().getLength());
								editControl = textPane.getDocument().getLength();
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (ins != null) {
							try {
								ins.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {
					insert(textPane.getDocument(), "未知命令:" + command + "\n", getAttribte(headerColor));
				}
			}
			insert(textPane.getDocument(), getHeader(), getAttribte(headerColor));
			insert(textPane.getDocument(), "~$", getAttribte(separatorColor));
			insert(textPane.getDocument(), " ", getAttribte(commandColor));

			editControl = textPane.getDocument().getLength();
			textPane.setCaretPosition(textPane.getDocument().getLength());
			textPane.setEditable(true);
			setExecuting(false);
			System.out.println("执行结束");
			executeThread = null;
		}
	}

	public void showResult(String text) {
		insert(textPane.getDocument(), "\n" + text + "\n", getAttribte(resultColor));

		insert(textPane.getDocument(), getHeader(), getAttribte(headerColor));
		insert(textPane.getDocument(), "~$", getAttribte(separatorColor));
		insert(textPane.getDocument(), " ", getAttribte(commandColor));

		editControl = textPane.getDocument().getLength();
		textPane.setCaretPosition(textPane.getDocument().getLength());
		textPane.setEditable(true);
	}

	/**
	 * 删除最后一个字符（Tab）， 并查询字典，插入符合的数据(自动填充功能)
	 */
	private void executeTab() {
		int len = textPane.getDocument().getLength();
		// 删除Tab
		try {
			textPane.getDocument().remove(len - 1, 1);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		len = textPane.getDocument().getLength();
		try {
			String input = textPane.getDocument().getText(editControl, len - editControl).trim();
			String[] words = input.split(" ");
			if (words.length > 0) {
				StringBuffer result = new StringBuffer();
				String cmd = words[words.length - 1];
				int size = 0;
				for (OnShellListener listener : getOnShellListeners()) {
					size = listener.autoCompletion(result, cmd);
					if (size > 0) {
						break;
					}
				}
				if (size == 1) { // 匹配唯一
					textPane.getDocument().remove(len - cmd.length(), cmd.length());
					insert(textPane.getDocument(), result.toString(), getAttribte(commandColor));
				} else if (size > 1) {// 匹配多个
					insert(textPane.getDocument(), "\n" + result.toString() + "\n", getAttribte(autoColor));
					insert(textPane.getDocument(), getHeader(), getAttribte(headerColor));
					insert(textPane.getDocument(), "~$", getAttribte(separatorColor));
					insert(textPane.getDocument(), " ", getAttribte(commandColor));
					editControl = textPane.getDocument().getLength();
					textPane.setEditable(true);
					insert(textPane.getDocument(), input, getAttribte(commandColor));
					textPane.setCaretPosition(textPane.getDocument().getLength());
				}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textPane.setCaretPosition(textPane.getDocument().getLength());
	}

	private JMenuBar menuBar;
	private JMenu mnNewMenu;
	private JMenuItem mntmStopItem;

	/**
	 * Create the frame.
	 */
	public ShellFrame() {
		fixedThreadPool = Executors.newFixedThreadPool(5);
		setTitle("Shell");
		setBounds(100, 100, 900, 700);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnNewMenu = new JMenu("选项");
		menuBar.add(mnNewMenu);

		mntmStopItem = new JMenuItem("停止运行");
		mntmStopItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getSource().toString());
				if (getRunningThread() != null && !getRunningThread().isInterrupted()) {
					setRunningThread(null);
					editControl = textPane.getDocument().getLength();
					textPane.setCaretPosition(textPane.getDocument().getLength());
					textPane.setEditable(true);
					setExecuting(false);
				}
			}
		});
		// 调用将加速器ctrl+C关联到mntmShell菜单项,T一定要大写
		mntmStopItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
		mnNewMenu.add(mntmStopItem);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setForeground(SystemColor.inactiveCaptionBorder);
		scrollPane.setBackground(Color.BLACK);
		contentPane.add(scrollPane);
		textPane = new JTextPane();
		textPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !isExecuting()) { // 按回车键执行相应操作;
					System.out.println("释放 换行");
					textPane.setCaretPosition(textPane.getDocument().getLength());

					if (getRunningThread() == null) {
						setRunningThread(new ExecuteThread());
						getRunningThread().start();
					}
				}else if (e.getKeyCode() == KeyEvent.VK_TAB) {
					textPane.setEditable(false);
					executeTab();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					keyUp = true;
					System.out.println("上一条命令");
					textPane.setEditable(false);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					System.out.println("下一条命令");
					int len = textPane.getDocument().getLength();
					int post = textPane.getCaretPosition();
					if (len == post) {// 不会触发光标移动
						if (commandIndex + 1 < commands.size()) {
							commandIndex++;
						}
						if (commandIndex >= 0 && commandIndex < commands.size()) {
							try {
								textPane.getDocument().remove(editControl, len - editControl);
								System.out.println("删除" + (len - editControl) + "个字符,显示第" + commandIndex + "条命令");
							} catch (BadLocationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							insert(textPane.getDocument(), commands.get(commandIndex), getAttribte(commandColor));
						}
						textPane.setCaretPosition(textPane.getDocument().getLength());
					} else {// 触发光标移动
						keyDown = true;
					}
					System.out.println("下一条命令");
				} 
			}
		});
		textPane.setCaretColor(SystemColor.inactiveCaptionBorder);
		textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		textPane.setFont(new Font("Microsoft JhengHei UI", Font.PLAIN, 16));
		textPane.setBackground(new Color(48, 10, 36));
		textPane.setForeground(new Color(244, 247, 252));
		textPane.setEditable(false);
		textPane.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				textPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				textPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			}
		});
		textPane.getCaret().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				textPane.getCaret().setVisible(true); // 使Text区的文本光标显示
				System.out.println("光标移动" + textPane.getCaretPosition());
				if (textPane.getCaretPosition() < editControl) {
					textPane.setEditable(false);
				} else {
					textPane.setEditable(true);
				}
				if (keyUp) {
					keyUp = false;
					int len = textPane.getDocument().getLength();
					if (commandIndex - 1 >= 0) {
						commandIndex--;
					}
					if (commandIndex >= 0 && commandIndex < commands.size()) {
						try {
							textPane.getDocument().remove(editControl, len - editControl);
						} catch (BadLocationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						insert(textPane.getDocument(), commands.get(commandIndex), getAttribte(commandColor));
					}
					textPane.setCaretPosition(textPane.getDocument().getLength());
				} else if (keyDown) {
					keyDown = false;
					int len = textPane.getDocument().getLength();
					if (commandIndex + 1 < commands.size()) {
						commandIndex++;
					}
					if (commandIndex >= 0 && commandIndex < commands.size()) {
						try {
							textPane.getDocument().remove(editControl, len - editControl);
						} catch (BadLocationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						insert(textPane.getDocument(), commands.get(commandIndex), getAttribte(commandColor));
					}
					textPane.setCaretPosition(textPane.getDocument().getLength());
				} else if (keyTab) {
					keyTab = false;
					System.out.printf("移动TAB");
					executeTab();
					textPane.setEditable(true);
				}
			}
		});
		textPane.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if (e.getOffset() < editControl) {
					fixedThreadPool.submit(new Runnable() {
						@Override
						public void run() {// 不能删除命令行前面的空格，删除之后将无法输入命令，这里要从新插入空格
							// TODO Auto-generated method stub
							insert(textPane.getDocument(), " ", getAttribte(commandColor));
							textPane.setEditable(true);
							textPane.setCaretPosition(textPane.getDocument().getLength());

						}
					});

				}
				// System.out.println(e.getType() + ",Offse =" + e.getOffset() +
				// ",length =" + e.getLength()
				// + ",editControl=" + editControl + ",docLength=" +
				// e.getDocument().getLength());
			}

			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				String text = "";
				try {
					text = e.getDocument().getText(e.getOffset(), e.getLength());
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// System.out.println(
				// e.getType() + ",Offse =" + e.getOffset() + ",length =" +
				// e.getLength() + ",editControl="
				// + editControl + ",docLength=" + e.getDocument().getLength() +
				// ",text=" + text);
			}

			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				// System.out.println(e.getType() + ",Offse =" + e.getOffset() +
				// ",length =" + e.getLength()
				// + ",editControl=" + editControl + ",docLength=" +
				// e.getDocument().getLength());
			}
		});
		scrollPane.setViewportView(textPane);

		userPath = System.getProperty("user.home");

		commands = new ArrayList<>();
		commandIndex = 0;
	}

	@Override
	public void setVisible(boolean b) {
		// TODO Auto-generated method stub
		if (b) {
			StringBuffer result = new StringBuffer();
			int res = usage(result);
			if (res == 0) {
				insert(textPane.getDocument(), result.toString(), getAttribte(defaulColor));
			}
			insert(textPane.getDocument(), getHeader(), getAttribte(headerColor));
			insert(textPane.getDocument(), "~$", getAttribte(separatorColor));
			insert(textPane.getDocument(), " ", getAttribte(commandColor));
			editControl = textPane.getDocument().getLength();

		}
		super.setVisible(b);

	}

	private String getCommand() {
		if (textPane != null) {
			try {
				int len = textPane.getDocument().getLength();
				return textPane.getText(editControl, len - editControl).trim();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param command
	 * @return
	 */
	private int parser(String command) {
		if (command == null) {
			return -1;
		}
		String[] cmds = command.split(" ");
		for (int i = 0; i < cmds.length; i++) {
			System.out.println("分解：" + cmds[i]);
		}
		return 0;
	}

	private boolean canExecute(String command) {
		if (Tools.isEmpty(command)) {
			return false;
		}
		if (command.equals("cls") || command.equals("err") || command.equals("-usage") || command.equals("time")) {
			return true;
		}
		return false;
	}

	@Override
	public int usage(StringBuffer result) {
		result.append(copyright + "\n");
		result.append("系统命令\n");
		result.append("清屏\t\tcls\n");
		result.append("停止运行\t\tCTRL+Q\n");
		result.append("输出时间\t\ttime\n");
		result.append("显示错误\t\terr\n");
		result.append("命令填充\t\tTABLE\n");
		result.append("查看历史命令\tUP/DOWM\n");
		result.append("使用规则\t\t-usage\n");
		for (OnShellListener listener : getOnShellListeners()) {
			listener.usage(result);
		}
		return 0;
	}

	@Override
	public PipedInputStream execute(String command) {
		if (canExecute(command)) {
			final PipedOutputStream out = new PipedOutputStream();
			PipedInputStream ins = new PipedInputStream();
			try {
				ins.connect(out);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new Thread() {
				public void run() {
					int res = 0;
					StringBuffer result = new StringBuffer();
					try {
						if (command.equals("cls")) {
							try {
								textPane.getDocument().remove(0, textPane.getDocument().getLength());
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else if (command.equals("err")) {
							// 输出上一条命令的错误信息
							for (OnShellListener listener : getOnShellListeners()) {
								listener.showError(result);
							}
							out.write(
									Tools.dataToString(result.toString().getBytes(), Tools.DEFAULT_ENCODE).getBytes());
							out.close();
						} else if (command.equals("-usage")) {
							usage(result);
							System.out.println(result.toString());
							out.write(result.toString().getBytes());
							out.close();
						} else if (command.equals("time")) {
							try {
								while (!Thread.interrupted()) {
									out.write((Tools.formatTime(System.currentTimeMillis())+ "\n").getBytes());
									Thread.sleep(1000);
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				};
			}.start();
			return ins;
		} else {
			System.out.println(command.length() +"命令："+command);
			for (OnShellListener listener : getOnShellListeners()) {
				PipedInputStream ins = listener.execute(command);
				if (ins != null) {
					return ins;
				}
			}
		}
		return null;
	}

	@Override
	public int autoCompletion(StringBuffer result, String command) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void showError(StringBuffer error) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHeader() {
		for (OnShellListener listener : getOnShellListeners()) {
			String header = listener.getHeader();
			if (header != null) {
				return header;
			}
		}
		return userPath;
	}

	public AttributeSet getAttribte(Color color) {
		SimpleAttributeSet attrSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSet, color);
		return attrSet;
	};

	public void insert(Document doc, String str, AttributeSet attrSet) {
		try {
			synchronized (doc.getClass()) {// 插入数据是进行同步处理
				doc.insertString(doc.getLength(), str, attrSet);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

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

					ShellFrame frame = new ShellFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
