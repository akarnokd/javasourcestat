
package hu.javasourcestat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * View results of traversing a source path.
 * @author karnokd, 2008.03.12.
 * @version $Revision 1.0$
 */
public class ResultsViewer {
	/** The underlying frame. */
	private JFrame frame;
	/** The counter model. */
	private CountersModel model;
	/** The tree. */
	private JTree tree;
	/** The tree model. */
	private DefaultTreeModel treeModel;
	/** The root node. */
	private DefaultMutableTreeNode rootNode;
	/** The current directory. */
	private File currentDir = new File(".");
	/** The roots. */
	private JavaFileEvaluator[] roots;
	/** Current evaluator. */
	private JavaFileEvaluator current;
	/**
	 * A name-value record.
	 * @author karnokd, 2008.03.12.
	 * @version $Revision 1.0$
	 */
	public static class NameValue {
		/** Name. */
		public final String name;
		/** Value. */
		public final long value;
		/**
		 * Constructor.
		 * @param name the name
		 * @param value the value
		 */
		public NameValue(String name, long value) {
			this.name = name;
			this.value = value;
		}
	}
	/**
	 * An option element.
	 * @author karnokd, 2008.03.12.
	 * @version $Revision 1.0$
	 */
	public static class Option {
		/** The value. */
		public final Object value;
		/** The text. */
		public final String text;
		/**
		 * Constructor.
		 * @param value the value
		 * @param text the text
		 */
		public Option(Object value, String text) {
			this.value = value;
			this.text = text;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return text;
		}
	}
	/**
	 * Statistics counter table model.
	 * @author karnokd, 2008.03.12.
	 * @version $Revision 1.0$
	 */
	public static class CountersModel extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 1733456936962623137L;
		/** Filter option. */
		private boolean filterCount = true;
		/** Filter option. */
		private boolean filterSum = true;
		/** Filter option. */
		private boolean filterAvg = true;
		/** Filter option. */
		private boolean filterMin = true;
		/** Filter option. */
		private boolean filterMax = true;
		/** The column names. */
		private String[] columnNames = {
			"Name", "Value"
		};
		/** The column classes. */
		private Class<?>[] columnClasses = {
			String.class, Long.class
		};
		/** The rows. */
		private final List<NameValue> allValues = new ArrayList<NameValue>();
		/** The rows. */
		private final List<NameValue> values = new ArrayList<NameValue>();
		/**
		 * Set the values.
		 * @param values map of counters.
		 */
		public void setValues(Map<String, LongValue> values) {
			this.allValues.clear();
			if (values != null) {
				for (Map.Entry<String, LongValue> e : values.entrySet()) {
					this.allValues.add(new NameValue(e.getKey(), e.getValue().longValue()));
				}
				Collections.sort(this.allValues, new Comparator<NameValue>() {
					@Override
					public int compare(NameValue o1, NameValue o2) {
						return o1.name.compareTo(o2.name);
					}
				});
			}
			doFilter();
		}
		/** Do filtering. */
		private void doFilter() {
			this.values.clear();
			for (NameValue nv : allValues) {
				if ((!filterSum && nv.name.startsWith(Constants.SUM))
					|| (!filterAvg && nv.name.startsWith(Constants.AVG))
					|| (!filterMin && nv.name.startsWith(Constants.MIN))
					|| (!filterMax && nv.name.startsWith(Constants.MAX))
					|| (!filterCount && nv.name.startsWith(Constants.COUNT))
				) {
					continue;
				}
				values.add(nv);
			}
			fireTableDataChanged();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRowCount() {
			return values.size();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return values.get(rowIndex).name;
			case 1:
				return values.get(rowIndex).value;
			default:
				return null;
			}
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}
		/**
		 * Set filter by count.
		 * @param value the value
		 */
		public void setFilterCount(boolean value) {
			this.filterCount = value;
			doFilter();
		}
		/**
		 * Set filter by sum.
		 * @param value the value
		 */
		public void setFilterSum(boolean value) {
			this.filterSum = value;
			doFilter();
		}
		/**
		 * Set filter by average.
		 * @param value the value
		 */
		public void setFilterAvg(boolean value) {
			this.filterAvg = value;
			doFilter();
		}
		/**
		 * Set filter by min.
		 * @param value the value
		 */
		public void setFilterMin(boolean value) {
			this.filterMin = value;
			doFilter();
		}
		/**
		 * Set filter by max.
		 * @param value the value
		 */
		public void setFilterMax(boolean value) {
			this.filterMax = value;
			doFilter();
		}
	}
	/**
	 * Constructor.
	 * @param root the root evaluator
	 */
	public ResultsViewer(JavaFileEvaluator[] root) {
		this.roots = root;
		init();
	}
	/** 
	 * Initialize the frame.
	 */
	private void init() {
		frame = new JFrame("Java Source Statistics of ");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		rootNode = new DefaultMutableTreeNode("Statistics");
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
				if (node != null) {
					Object o = node.getUserObject();
					if (o != null && o instanceof Option) {
						Option opt = (Option)node.getUserObject();
						current = (JavaFileEvaluator)opt.value;
						model.setValues(current.getCounters().counters);
					} else {
						current = null;
						model.setValues(null);
					}
				} else {
					current = null;
					model.setValues(null);
				}
			}
		});
		//tree.setRootVisible(false);
		JScrollPane treeScroll = new JScrollPane(tree);
		
		model = new CountersModel();
		JTable table = new JTable(model);
		JScrollPane tableScroll = new JScrollPane(table);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		split.setLeftComponent(treeScroll);
		split.setRightComponent(tableScroll);
		
		for (JavaFileEvaluator e : roots) {
			buildChildren(rootNode, e);
		}

		treeModel.nodeChanged(rootNode);
		tree.expandPath(new TreePath(rootNode.getPath()));
		int i = 0;
		while (i < tree.getRowCount()) {
			tree.expandRow(i);
			i++;
		}
		
		initMenu();
		
		frame.getContentPane().add(split, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	/** Initialize menu. */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem exportAllXML = new JMenuItem("Export All (XML)...");
		exportAllXML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					doExportAll(selectFile("exportall.xml"), roots);
				} catch (IOException ex) {
					showError(ex.toString());
				}
			}
		});
		JMenuItem exportCurrentXML = new JMenuItem("Export Current (XML)...");
		exportCurrentXML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (current != null) {
					doExport(selectFile("export.xml"));
				}
			}
		});
		JMenuItem exportCurrentCSV = new JMenuItem("Export Current (CSV)...");
		exportCurrentCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExportCVS(selectFile("export.csv"));
			}
		});
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		
		fileMenu.add(exportAllXML);
		fileMenu.add(exportCurrentXML);
		fileMenu.add(exportCurrentCSV);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		menuBar.add(fileMenu);
		
		JMenu viewMenu = new JMenu("View");

		final JCheckBoxMenuItem viewCount = new JCheckBoxMenuItem("Count");
		viewCount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilterCount(viewCount.isSelected());
			}
		});
		viewCount.setSelected(true);

		final JCheckBoxMenuItem viewSum = new JCheckBoxMenuItem("Sum");
		viewSum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilterSum(viewSum.isSelected());
			}
		});
		viewSum.setSelected(true);
		
		final JCheckBoxMenuItem viewAvg = new JCheckBoxMenuItem("Average");
		viewAvg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilterAvg(viewAvg.isSelected());
			}
		});
		viewAvg.setSelected(true);
		
		final JCheckBoxMenuItem viewMin = new JCheckBoxMenuItem("Minimum");
		viewMin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilterMin(viewMin.isSelected());
			}
		});
		viewMin.setSelected(true);
		
		final JCheckBoxMenuItem viewMax = new JCheckBoxMenuItem("Maximum");
		viewMax.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilterMax(viewMax.isSelected());
			}
		});
		viewMax.setSelected(true);
		
		viewMenu.add(viewCount);
		viewMenu.add(viewSum);
		viewMenu.add(viewAvg);
		viewMenu.add(viewMin);
		viewMenu.add(viewMax);
		menuBar.add(viewMenu);
		
		frame.setJMenuBar(menuBar);
		
	}
	/**
	 * Export all as XML.
	 * @param f the file, can be null
	 * @param roots the root evaluators
	 * @throws IOException on error
	 */
	public static void doExportAll(File f, JavaFileEvaluator[] roots) 
	throws IOException {
		if (f == null) {
			return;
		}
		Writer fout = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		try {
			PrintWriter out = new PrintWriter(fout);
			try {
				out.println("<?xml version='1.0' encoding='UTF-8'?>");
				out.println("<JavaSourceStat xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='JavaSourceStat.xsd'>");
				for (JavaFileEvaluator e : roots) {
					exportStat(e, out, "  ");
				}
				out.println("</JavaSourceStat>");
			} finally {
				out.close();
			}
		} finally {
			fout.close();
		}
	}
	/**
	 * Export statistics.
	 * @param jfe the java file evaluator
	 * @param out the output stream
	 * @param indent the indentation
	 */
	private static void exportStat(JavaFileEvaluator jfe, PrintWriter out, String indent) {
		out.print(indent);
		if (jfe.hasChildren()) {
			out.print("<Package name='");
		} else {
			out.print("<JavaFile name='");
		}
		out.print(jfe.getSrcFile().getName());
		out.println("'>");
		// -------------------------------------------------------------
		out.print(indent);
		out.println("  <Statistics>");
		for (Map.Entry<String, LongValue> e : jfe.getCounters().counters.entrySet()) {
			out.print(indent);
			out.print("    <Value name='");
			out.print(e.getKey());
			out.print("'>");
			out.print(e.getValue().longValue());
			out.println("</Value>");
		}
		
		out.print(indent);
		out.println("  </Statistics>");
		// -------------------------------------------------------------
		for (JavaFileEvaluator e : jfe.getChildren()) {
			exportStat(e, out, indent + "  ");
		}
		// -------------------------------------------------------------
		out.print(indent);
		if (jfe.hasChildren()) {
			out.println("</Package>");
		} else {
			out.println("</JavaFile>");
		}
	}
	/**
	 * Export current as XML.
	 * @param f the file, can be null
	 */
	private void doExport(File f) {
		if (f == null) {
			return;
		}
		try {
			Writer fout = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			try {
				PrintWriter out = new PrintWriter(fout);
				try {
					out.println("<?xml version='1.0' encoding='UTF-8'?>");
					out.println("<JavaSourceStat xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='JavaSourceStat.xsd'>");
					exportStat(current, out, "  ");
					out.println("</JavaSourceStat>");
				} finally {
					out.close();
				}
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			showError(ex.toString());
		}
	}
	/**
	 * Export current as CVS.
	 * @param f the file, can be null
	 */
	private void doExportCVS(File f) {
		if (f == null) {
			return;
		}
		try {
			FileWriter fout = new FileWriter(f);
			try {
				PrintWriter out = new PrintWriter(fout);
				try {
					for (NameValue nv : model.values) {
						out.print(nv.name);
						out.print(';');
						out.println(nv.value);
					}
				} finally {
					out.close();
				}
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			showError(ex.toString());
		}
	}
	/**
	 * Show an error message.
	 * @param s the message
	 */
	private void showError(String s) {
		JOptionPane.showMessageDialog(frame, s, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
	}
	/**
	 * Build child node.
	 * @param treeNode the parent node
	 * @param javaNode the java node
	 */
	public void buildChildren(DefaultMutableTreeNode treeNode, 
			JavaFileEvaluator javaNode) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new Option(javaNode, javaNode.getSrcFile().getName()));
		treeNode.add(node);
		for (JavaFileEvaluator e : javaNode.getChildren()) {
			buildChildren(node, e);
		}
	}
	/**
	 * Select target file to save.
	 * @param nameHint the filename hint
	 * @return the selected file or null if cancelled
	 */
	private File selectFile(String nameHint) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(currentDir);
		fc.setSelectedFile(new File(nameHint));
		if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			currentDir = f.getParentFile();
			return f;
		}
		return null;
	}
	/**
	 * Process the given list of files parallel.
	 * @param files the non null list of files.
	 * @return the array of processed files
	 * @throws InterruptedException if interrupted
	 */
	private static JavaFileEvaluator[] processFiles(File[] files) throws InterruptedException {
		CountUpDown count = new CountUpDown();
		ThreadPoolExecutor ex = (ThreadPoolExecutor)Executors
		.newFixedThreadPool(1 /*, Runtime.getRuntime().availableProcessors() */);
		
		JavaFileEvaluator[] jfes = new JavaFileEvaluator[files.length];
		for (int i = 0; i < jfes.length; i++) {
			count.increment();
			jfes[i] = new JavaFileEvaluator(files[i], count, ex);
			ex.execute(jfes[i]);
		}
		count.await();
		for (int i = 0; i < jfes.length; i++) {
			count.increment();
			final JavaFileEvaluator jfe = jfes[i];
			ex.execute(new Runnable() {
				public void run() {
					jfe.aggregate();
				}
			});
		
		}
		count.await();
		
		ex.shutdown();
		ex.awaitTermination(0, TimeUnit.MILLISECONDS);
		return jfes;
	}
	/**
	 * @param args the arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		File[] files = null;
		if (args.length == 0) {
			JOptionPane.showMessageDialog(null, "Please specify the path to the source directory in the command line.");
			return;
		} else {
			files = new File[args.length];
			for (int i = 0; i < files.length; i++) {
				files[i] = new File(args[i]);
			}
		}
		final JavaFileEvaluator[] roots = processFiles(files);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ResultsViewer(roots);
			}
		});
	}
}
