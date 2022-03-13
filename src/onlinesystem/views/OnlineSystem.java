package onlinesystem.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;

import org.eclipse.swt.widgets.Display;

public class OnlineSystem extends ViewPart {

	public static final String ID = "onlinesystem.views.OnlineSystem"; //$NON-NLS-1$
	private Table table;
	private TableItem[] tableItem;
	private Combo selectcombo;
	private Label thrNmLabel;
	
	public static int thrtotal;

	public OnlineSystem() {
		
	}
	
	private Vector doscmd(String cmd) {
		Vector buf = new Vector();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String content = br.readLine();
            while (content != null) {
                content = br.readLine();
                buf.addElement(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
	}
	
	private void sysdatashow() {
		Vector buf = doscmd("tasklist /fo csv");
		this.thrtotal = buf.size()-1;
		
		if(tableItem == null)
			tableItem = new TableItem[buf.size()-1];
		
		for(int i=0;i<buf.size()-1;i++) {
			String[] sltstr = buf.get(i).toString().split("\",\"");
			
			if(table.getItemCount() < buf.size()-1)
				tableItem[i] = new TableItem(table, SWT.NONE);
			
			for(int j=0;j<sltstr.length;j++) {
				switch(j) {
					case 0:
						selectcombo.add(sltstr[j].replace("\"", "") + "#" + sltstr[j+1].replace("\"", ""),i);
					case 1:
					case 2:
					case 3:
						tableItem[i].setText(j, sltstr[j].replace("\"", ""));
						break;
					case 4:
						tableItem[i].setText(j, "00");
						tableItem[i].setText(j+1, sltstr[j].replace("\"", ""));
						tableItem[i].setText(j+2, "x64");
						break;
				}
			}
		}
		buf.removeAllElements();
	}
	
	class sysThread implements Runnable{
		private static Thread thr;
		private String threadname;
		private static volatile  boolean mark = false;
		
		sysThread(String name){
			this.threadname = name;
		}
		
	    @Override
	    public void run() {
        	while(!mark) {
        		Display.getDefault().syncExec(new Runnable() {
        			public void run() {	
        				try {
			        		table.removeAll();
			        		selectcombo.removeAll();
			        		sysdatashow();
			        		thrNmLabel.setText(Integer.toString(thrtotal));
				        	Thread.sleep(1000);
        				}
        				catch (InterruptedException e) {
        					e.printStackTrace();
        				}
        			}
        		});
        	}
	    }

	    public void start() {
	    	if(thr == null) {
	    		mark = false;
	    		thr  = new Thread(this,threadname);
	    		thr.start();
	    	}
	    }
	    
	    public void stop() {
	    	if(thr != null) {
		    	mark = true;
		    	thr.interrupt();
		    	thr = null;
	    	}
	    }
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(null);
		
		Button refreshButton = new Button(container, SWT.NONE);
		refreshButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				table.removeAll();
				selectcombo.removeAll();
				sysdatashow();
				thrNmLabel.setText(Integer.toString(thrtotal));
			}
		});
		refreshButton.setBounds(0, 0, 78, 25);
		refreshButton.setText("Refresh");
		
		Button autorefreshButton = new Button(container, SWT.NONE);
		autorefreshButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				sysThread trd = new sysThread("Test123");
				if(autorefreshButton.getText() == "Auto Refresh") {
					trd.start();
					autorefreshButton.setText("Stop Auto");
				}
				else if(autorefreshButton.getText() == "Stop Auto") {
					trd.stop();
					autorefreshButton.setText("Auto Refresh");
				}
			}
		});
		autorefreshButton.setBounds(84, 0, 84, 25);
		autorefreshButton.setText("Auto Refresh");
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.WRAP);
		label.setBounds(174, 0, 10, 30);
		
		TabFolder tabFolder = new TabFolder(container, SWT.NONE);
		tabFolder.setBounds(0, 29, 907, 516);
		
		TabItem tbtmDetail = new TabItem(tabFolder, SWT.NONE);
		tbtmDetail.setText("Detail");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmDetail.setControl(composite);
		
		table = new Table(composite, SWT.FULL_SELECTION);
		table.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				//resize
				tabFolder.setBounds(0, 29, parent.getBounds().width, parent.getBounds().height);
				table.setBounds(0, 0, parent.getBounds().width - 8, parent.getBounds().height - 60);
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		table.getScrollbarsMode();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(0, 0, 887, 489);
		
		TableColumn nameColumn = new TableColumn(table, SWT.CENTER);
		nameColumn.setWidth(100);
		nameColumn.setText("Name");
		
		TableColumn pidColumn = new TableColumn(table, SWT.CENTER);
		pidColumn.setWidth(100);
		pidColumn.setText("PID");
		
		TableColumn wkstgnameColumn = new TableColumn(table, SWT.CENTER);
		wkstgnameColumn.setWidth(126);
		wkstgnameColumn.setText("Work stage name");
		
		TableColumn wkphaseeColumn = new TableColumn(table, SWT.CENTER);
		wkphaseeColumn.setWidth(100);
		wkphaseeColumn.setText("Work Phase");
		
		TableColumn cpuColumn = new TableColumn(table, SWT.CENTER);
		cpuColumn.setWidth(100);
		cpuColumn.setText("CPU");
		
		TableColumn memeryuseColumn = new TableColumn(table, SWT.CENTER);
		memeryuseColumn.setWidth(100);
		memeryuseColumn.setText("Memery use");
		
		TableColumn architectureColumn = new TableColumn(table, SWT.CENTER);
		architectureColumn.setWidth(100);
		architectureColumn.setText("Architecture");
		
		Label sspLabel = new Label(container, SWT.NONE);
		sspLabel.setBounds(190, 5, 122, 15);
		sspLabel.setText("Select Stop Process :");
		
		Button stopButton = new Button(container, SWT.NONE);
		stopButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String[] str = selectcombo.getText().split("#");
				int YNFlag = JOptionPane.showConfirmDialog( null , 
										"Make sure to close " + str[0] + " ?", 
										"Message",
										JOptionPane.YES_NO_OPTION );
				if(YNFlag == 0) {
					Vector msg = doscmd("taskkill -f -t -im " + str[1]);
					selectcombo.setText("");
					selectcombo.removeAll();
					table.removeAll();
					sysdatashow();
					thrNmLabel.setText(Integer.toString(thrtotal));
					msg.removeAllElements();
				}
			}
		});
		stopButton.setBounds(532, 0, 78, 25);
		stopButton.setText("Stop");
		
		selectcombo = new Combo(container, SWT.NONE);
		selectcombo.setBounds(318, 0, 201, 23);
		
		Label totalthrLabel = new Label(container, SWT.NONE);
		totalthrLabel.setBounds(643, 5, 91, 15);
		totalthrLabel.setText("Total  threads :");
		
		thrNmLabel = new Label(container, SWT.NONE);
		thrNmLabel.setAlignment(SWT.CENTER);
		thrNmLabel.setBounds(727, 5, 60, 15);
		thrNmLabel.setText("0");
		
		//System data show to table
		sysdatashow();
		thrNmLabel.setText(Integer.toString(thrtotal));

		createActions();
		// Uncomment if you wish to add code to initialize the toolbar
		// initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		//IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}
