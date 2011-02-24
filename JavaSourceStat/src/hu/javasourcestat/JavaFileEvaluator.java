
package hu.javasourcestat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.ws.jaxme.js.JavaComment;
import org.apache.ws.jaxme.js.JavaField;
import org.apache.ws.jaxme.js.JavaInnerClass;
import org.apache.ws.jaxme.js.JavaMethod;
import org.apache.ws.jaxme.js.JavaQName;
import org.apache.ws.jaxme.js.JavaSource;
import org.apache.ws.jaxme.js.JavaSourceFactory;
import org.apache.ws.jaxme.js.util.JavaParser;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * @author karnokd, 2008.03.12.
 * @version $Revision 1.0$
 */
public class JavaFileEvaluator implements Runnable {
	/** The source file. */
	private final File srcFile;
	/** The counters. */
	private final Counters counters = new Counters();
	/** The parent evaluator. */
	private final CountUpDown count;
	/** The executor service. */
	private final ExecutorService service;
	/** List of the child evaluators. */
	private List<JavaFileEvaluator> children;
	/**
	 * Constructor.
	 * @param srcFile the source file
	 * @param count the termination notifier
	 * @param service the executor service
	 */
	public JavaFileEvaluator(File srcFile, 
			CountUpDown count, ExecutorService service) {
		this.srcFile = srcFile;
		this.count = count;
		this.service = service;
	}
	/**
	 * Evaluate the given file.
	 */
	@Override
	public void run() {
		try {
			if (srcFile.isFile()) {
				JavaSourceFactory jsf = new JavaSourceFactory();
				JavaParser jp = new JavaParser(jsf);
				try {
					counters.add(Constants.FILE_SIZE, srcFile.length());
					countLines();
					jp.parse(srcFile);
					Iterator<?> it = jsf.getJavaSources();
					while (it.hasNext()) {
						JavaSource js = (JavaSource)it.next();
						js.toString();
						processSource(js);
					}
				} catch (IOException ex) {
					System.err.println("Parser error in file " + srcFile + ": " + ex);
				} catch (RecognitionException ex) {
					System.err.println("Parser error in file " + srcFile + ": " + ex);
				} catch (TokenStreamException ex) {
					System.err.println("Parser error in file " + srcFile + ": " + ex);
				} catch (IllegalStateException ex) {
					System.err.println("Parser error in file " + srcFile + ": " + ex);
				}
			} else
			if (srcFile.isDirectory()) {
				processDirectory(srcFile);
			}
		} finally {
			count.decrement();
		}
	}
	/**
	 * Get an unmodifiable list of children.
	 * @return the list of child evaluator, non null
	 */
	public List<JavaFileEvaluator> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(children);
	}
	/**
	 * Evaluate source file.
	 * @param js the JavaSource
	 */
	public void processSource(JavaSource js) {
		processModifiers(js);
		
		JavaQName[] imports = js.getImports();
		counters.add(Constants.IMPORT, imports.length);
		int count = 0;
		for (JavaQName imp : imports) {
			if (imp.getPackageName().startsWith("java")) {
				count++;
			}
		}
		counters.add(Constants.NON_JAVA_IMPORT, count);
		
		processInnerClasses(js.getInnerClasses());
		
		JavaMethod[] methods = js.getMethods();
		counters.add(Constants.METHOD, methods.length);
		for (JavaMethod m : methods) {
			processModifiers(m, false);
			processModifiers(m, true);
		}
		
		JavaField[] fields = js.getFields();
		counters.add(Constants.FIELD, fields.length);
		for (JavaField f : fields) {
			processModifiers(f, false);
			processModifiers(f, true);
		}
	}
	/**
	 * Recursively process inner classes.
	 * @param inners the array of inner classes
	 */
	private void processInnerClasses(JavaInnerClass[] inners) {
		if (inners != null && inners.length > 0) { 
			for (JavaInnerClass inn : inners) {
				processModifiers(inn);
				
				processInnerClasses(inn.getInnerClasses());
				
				JavaMethod[] methods = inn.getMethods();
				counters.add(Constants.METHOD, methods.length);
				for (JavaMethod m : methods) {
					processModifiers(m, false);
					processModifiers(m, true);
				}
				
				JavaField[] fields = inn.getFields();
				counters.add(Constants.FIELD, fields.length);
				for (JavaField f : fields) {
					processModifiers(f, false);
					processModifiers(f, true);
				}
			}
		}

	}
	/** 
	 * Count the source lines.
	 * @throws IOException on error 
	 */
	private void countLines() throws IOException {
		FileReader fin = new FileReader(srcFile);
		try {
			BufferedReader br = new BufferedReader(fin);
			String line = null;
			boolean commentmode = false;
			boolean multiliner = false;
			boolean javadocer = false;
			while ((line = br.readLine()) != null) {
				int bytes = line.getBytes(fin.getEncoding()).length;
				if (commentmode) {
					counters.add(Constants.LINE_COMMENT, 1);
					counters.add(Constants.LINE_COMMENT + Constants.SIZE, bytes);
					if (multiliner) {
						counters.add(Constants.LINE_COMMENT_MULTILINE, 1);
						counters.add(Constants.LINE_COMMENT_MULTILINE + Constants.SIZE, bytes);
					}
					if (javadocer) {
						counters.add(Constants.LINE_COMMENT_JAVADOC, 1);
						counters.add(Constants.LINE_COMMENT_JAVADOC + Constants.SIZE, bytes);
					}
					if (isEndComment(line)) {
						commentmode = false;
						multiliner = false;
						javadocer = false;
					}
				} else {
					if (isEmptyOrWhitespace(line)) {
						counters.add(Constants.LINE_EMPTY, 1);
						counters.add(Constants.LINE_EMPTY + Constants.SIZE, bytes);
					} else {
						counters.add(Constants.LINE_NON_EMPTY, 1);
						counters.add(Constants.LINE_NON_EMPTY + Constants.SIZE, bytes);
						if (isOneLineComment(line)) {
							counters.add(Constants.LINE_COMMENT_ONELINER, 1);
							counters.add(Constants.LINE_COMMENT_ONELINER + Constants.SIZE, bytes);
							counters.add(Constants.LINE_COMMENT, 1);
							counters.add(Constants.LINE_COMMENT + Constants.SIZE, bytes);
						} else {
							if (isMultiLineComment(line)) {
								commentmode = true;
								multiliner = true;
								counters.add(Constants.LINE_COMMENT_MULTILINE, 1);
								counters.add(Constants.LINE_COMMENT_MULTILINE + Constants.SIZE, bytes);
								counters.add(Constants.LINE_COMMENT, 1);
								counters.add(Constants.LINE_COMMENT + Constants.SIZE, bytes);
							} else
							if (isJavadocComment(line)) {
								commentmode = true;
								javadocer = true;
								counters.add(Constants.LINE_COMMENT_JAVADOC, 1);
								counters.add(Constants.LINE_COMMENT_JAVADOC + Constants.SIZE, bytes);
								counters.add(Constants.LINE_COMMENT, 1);
								counters.add(Constants.LINE_COMMENT + Constants.SIZE, bytes);
							} else {
								counters.add(Constants.LINE_NON_COMMENT, 1);
								counters.add(Constants.LINE_NON_COMMENT + Constants.SIZE, bytes);
							}
						}
					}
				}
				counters.add(Constants.LINE, 1);
				counters.add(Constants.LINE + Constants.SIZE, bytes);
			}
		} finally {
			fin.close();
		}
	}
	/**
	 * Checks if the string is only whitespace. 
	 * @param s the string
	 * @return true if whitespace
	 */
	private boolean isEmptyOrWhitespace(String s) {
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				if (!Character.isWhitespace(s.charAt(i))) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Is one line comment.
	 * @param s the string to check
	 * @return the comment
	 */
	private boolean isOneLineComment(String s) {
		int idx = s.indexOf("//");
		if (idx >= 0) {
			return isEmptyOrWhitespace(s.substring(0, idx));
		}
		return false;
	}
	/**
	 * Is multiline comment?
	 * @param s the string to test
	 * @return true if it is a multiline comment
	 */
	private boolean isMultiLineComment(String s) {
		int idx = s.indexOf("/**");
		if (idx >= 0) {
			return false;
		}
		idx = s.indexOf("/*");
		if (idx >= 0) {
			return isEmptyOrWhitespace(s.substring(0, idx));
		}
		return false;
	}
	/**
	 * Is multiline comment?
	 * @param s the string to test
	 * @return true if it is a multiline comment
	 */
	private boolean isJavadocComment(String s) {
		int idx = s.indexOf("/**");
		if (idx >= 0) {
			return isEmptyOrWhitespace(s.substring(0, idx));
		}
		return false;
	}
	/**
	 * Is end comment.
	 * @param s the string to test
	 * @return true if end comment
	 */
	private boolean isEndComment(String s) {
		return s.indexOf("*/") >= 0;
	}
	/**
	 * 
	 * @param dir the directory to process
	 */
	public void processDirectory(File dir) {
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return 
				(pathname.isDirectory() && !pathname.getName().startsWith("CVS") && !pathname.getName().startsWith("."))
				|| (pathname.isFile() && pathname.getName().endsWith(".java") && pathname.canRead());
			}
		});
		if (files != null) {
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && o2.isFile()) {
						return -1;
					} else
					if (o1.isFile() && o2.isDirectory()) {
						return 1;
					}
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (File f : files) {
				if (f.isFile()) {
					counters.add(Constants.FILE, 1);
				} else {
					counters.add(Constants.DIR, 1);
				}
				JavaFileEvaluator ev = new JavaFileEvaluator(f, count, service);
				if (children == null) {
					children = new ArrayList<JavaFileEvaluator>();
				}
				children.add(ev);
				count.increment();
				service.execute(ev);
			}
		}
	}
	/**
	 * @return the source file.
	 */
	public File getSrcFile() {
		return srcFile;
	}
	/**
	 * @return the counters
	 */
	public Counters getCounters() {
		return counters;
	}
	/** Aggregate measures. */
	public void aggregate() {
		// recursively aggregate non-leaf nodes
		if (!hasChildren()) {
			count.decrement();
			return;
		}
		for (final JavaFileEvaluator e : children) {
			count.increment();
			service.execute(new Runnable() {
				@Override
				public void run() {
					e.aggregate();
				}
			});
		}
		Counters[] aggregates = {
				new Counters(),
				new Counters(),
				new Counters(),
				new Counters(),
				new Counters()
			};
		String[] aggregatePrefixes = {
			Constants.COUNT,
			Constants.SUM,
			Constants.MIN,
			Constants.MAX,
			Constants.AVG,
		};
		getLeafs(this, aggregates, 0);
		long filecount = aggregates[0].get(Constants.FILE);
		if (filecount != 0L) {
			aggregates[4].avgAll(filecount);
		}
		for (int i = 0; i < aggregates.length; i++) {
			for (Map.Entry<String, LongValue> e : aggregates[i].counters.entrySet()) {
				counters.add(aggregatePrefixes[i] + e.getKey(), e.getValue().longValue());
			}
		}
		count.decrement();
	}
	/**
	 * @return does this instance have children
	 */
	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}
	/**
	 * Recursively collect the leaf nodes of the given node.
	 * @param node the current node
	 * @param aggregates the count, sum, min, max and avg counters.
	 * @param level the level of aggregation
	 */
	private void getLeafs(JavaFileEvaluator node, Counters[] aggregates, int level) {
		if (node.hasChildren()) {
			if (level > 0)  {
				aggregates[0].add(Constants.DIR, 1);
			}
			for (JavaFileEvaluator e : node.children) {
				getLeafs(e, aggregates, level + 1);
			}
		} else {
			node.updateCounters(aggregates);
		}
	}
	/**
	 * Update counters based on the current executor.
	 * @param aggregates the counters: count, sum, min, max, avg
	 */
	private void updateCounters(Counters[] aggregates) {
		//assert aggregates.length == 5;
		aggregates[0].add(Constants.FILE, 1);
		for (Map.Entry<String, LongValue> e : this.counters.counters.entrySet()) {
			aggregates[1].add(e.getKey(), e.getValue().longValue());
			aggregates[2].min(e.getKey(), e.getValue().longValue());
			aggregates[3].max(e.getKey(), e.getValue().longValue());
			aggregates[4].add(e.getKey(), e.getValue().longValue());
		}
	}
	/**
	 * Check documentation status on the given java source.
	 * @param comment the java comment
	 * @param b the builder
	 */
	private void checkDocumented(JavaComment comment, StringBuilder b) {
		/* FIXME: JaxWsJS does not support comments when parsing. * / 
		if (comment != null) {
			counters.add(b.toString() + Constants.DOCUMENTED, 1);
		} else {
			counters.add(b.toString() + Constants.UNDOCUMENTED, 1);
		}
		/ * */
	}
	/**
	 * Process modifiers of a java source.
	 * (public | private | protected | default) [static] (abstract|final) (class|interface)
	 * @param src the source
	 */
	private void processModifiers(JavaSource src) {
		StringBuilder b = new StringBuilder();
		if (src.isInterface()) {
			b.append(Constants.INTERFACE);
			counters.add(b.toString(), 1);
		} else {
			b.append(Constants.CLASS);
			counters.add(b.toString(), 1);
		}
		checkDocumented(src.getComment(), b);
		// -------------------
		if (src.getProtection().equals(JavaSource.PUBLIC)) {
			b.append(Constants.PUBLIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PRIVATE)) {
			b.append(Constants.PRIVATE);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PROTECTED)) {
			b.append(Constants.PROTECTED);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.DEFAULT_PROTECTION)) {
			b.append(Constants.DEFAULT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isAbstract()) {
			b.append(Constants.ABSTRACT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
	}
	/**
	 * Process modifiers of a java method.
	 * (public | private | protected | default) [static] (abstract|final)
	 * @param src the source
	 * @param includeClassType include class type into name (class, interface)?
	 */
	private void processModifiers(JavaMethod src, boolean includeClassType) {
		StringBuilder b = new StringBuilder();
		if (includeClassType) {
			if (src.getJavaSource().isInnerClass()) {
				if (src.getJavaSource().isInterface()) {
					b.append(Constants.INNER_INTERFACE);
				} else {
					b.append(Constants.INNER_CLASS);
				}
			} else {
				if (src.getJavaSource().isInterface()) {
					b.append(Constants.INTERFACE);
				} else {
					b.append(Constants.CLASS);
				}
			}
			b.append('.');
		}
		b.append(Constants.METHOD);
		checkDocumented(src.getComment(), b);
		// -------------------
		if (src.getProtection().equals(JavaSource.PUBLIC)) {
			b.append(Constants.PUBLIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PRIVATE)) {
			b.append(Constants.PRIVATE);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PROTECTED)) {
			b.append(Constants.PROTECTED);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.DEFAULT_PROTECTION)) {
			b.append(Constants.DEFAULT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isStatic()) {
			b.append(Constants.STATIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isSynchronized()) {
			b.append(Constants.SYNCHRONIZED);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isAbstract()) {
			b.append(Constants.ABSTRACT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.isFinal()) {
			b.append(Constants.FINAL);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isVoid()) {
			b.append(Constants.VOID);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
	}
	/**
	 * Process modifiers of a java field.
	 * (public | private | protected | default) [static] (abstract|final)
	 * @param src the source
	 * @param includeClassType include class type into name (class, interface)?
	 */
	private void processModifiers(JavaField src, boolean includeClassType) {
		StringBuilder b = new StringBuilder();
		if (includeClassType) {
			if (src.getJavaSource().isInnerClass()) {
				if (src.getJavaSource().isInterface()) {
					b.append(Constants.INNER_INTERFACE);
				} else {
					b.append(Constants.INNER_CLASS);
				}
			} else {
				if (src.getJavaSource().isInterface()) {
					b.append(Constants.INTERFACE);
				} else {
					b.append(Constants.CLASS);
				}
			}
			b.append('.');
		}
		b.append(Constants.FIELD);
		checkDocumented(src.getComment(), b);
		// -------------------
		if (src.getProtection().equals(JavaSource.PUBLIC)) {
			b.append(Constants.PUBLIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PRIVATE)) {
			b.append(Constants.PRIVATE);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PROTECTED)) {
			b.append(Constants.PROTECTED);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.DEFAULT_PROTECTION)) {
			b.append(Constants.DEFAULT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isStatic()) {
			b.append(Constants.STATIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isAbstract()) {
			b.append(Constants.ABSTRACT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.isFinal()) {
			b.append(Constants.FINAL);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isTransient()) {
			b.append(Constants.TRANSIENT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
	}
	/**
	 * Process modifiers of a java source.
	 * (public | private | protected | default) [static] (abstract|final) (class|interface)
	 * @param src the source
	 */
	private void processModifiers(JavaInnerClass src) {
		StringBuilder b = new StringBuilder();
		if (src.isInterface()) {
			b.append(Constants.INNER_INTERFACE);
		} else {
			b.append(Constants.INNER_CLASS);
		}
		counters.add(b.toString(), 1);
		checkDocumented(src.getComment(), b);
		// -------------------
		if (src.getProtection().equals(JavaSource.PUBLIC)) {
			b.append(Constants.PUBLIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PRIVATE)) {
			b.append(Constants.PRIVATE);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.PROTECTED)) {
			b.append(Constants.PROTECTED);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		} else
		if (src.getProtection().equals(JavaSource.DEFAULT_PROTECTION)) {
			b.append(Constants.DEFAULT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.getStatic()) {
			b.append(Constants.STATIC);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
		// -------------------
		if (src.isAbstract()) {
			b.append(Constants.ABSTRACT);
			counters.add(b.toString(), 1);
			checkDocumented(src.getComment(), b);
		}
	}
}
