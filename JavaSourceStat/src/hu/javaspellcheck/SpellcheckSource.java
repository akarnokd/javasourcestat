/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package hu.javaspellcheck;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Utility class to extract words from the sources and create a text file 
 * to run through any spell checker.
 * @author karnokd, 2011.10.17.
 */
public final class SpellcheckSource {
	/**
	 * Utility class.
	 */
	private SpellcheckSource() {
	}

	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		final Set<String> words = Sets.newHashSet();
		Files.walkFileTree(Paths.get(args[0]), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				if (file.toString().endsWith(".java") || file.toString().endsWith(".xml")) {
					parseFile(file, words);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		Set<String> check = Sets.newHashSet(Files.readAllLines(Paths.get("dict.txt"), Charset.defaultCharset()));
		Path da = Paths.get("dict-add.txt");
		if (Files.exists(da)) {
			check.addAll(Files.readAllLines(da, Charset.defaultCharset()));
		}
		PrintWriter bout = new PrintWriter(new FileWriter("spell.txt"));
		try {
			List<String> w = Lists.newArrayList(words);
			Collections.sort(w);
			for (String s : w) {
				if (s.length() > 1 && Character.isAlphabetic(s.charAt(0))) {
					if (!check.contains(s.toUpperCase())) {
						bout.println(s);
					}
				}
			}
		} finally {
			bout.close();
		}
	}
	/**
	 * Parse the given text file.
	 * @param file the file
	 * @param words the words to output
	 * @throws IOException on error
	 */
	static void parseFile(Path file, Set<String> words) throws IOException {
		System.out.println(file);
		for (String line : Files.readAllLines(file, Charset.forName("UTF-8"))) {
			String[] ws = line.split("\\b+|_");
			for (String w : ws) {
				words.addAll(camelCaseSplit(w));
			}
		}
	}
	/**
	 * Split the string along camelcase and allcaps boundaries.
	 * @param word the word to split
	 * @return the words
	 */
	static List<String> camelCaseSplit(String word) {
		List<String> result = Lists.newArrayList();
		char[] chars = word.toCharArray();
		int idx = 0;
		for (int i = 0; i < chars.length - 1; i++) {
			char c1 = chars[i];
			char c2 = chars[i + 1];
			if (Character.isLowerCase(c1) && Character.isUpperCase(c2)) {
				result.add(word.substring(idx, i + 1));
				idx = i + 1;
			} else
			if (Character.isUpperCase(c1) && Character.isLowerCase(c2) && i > 0) {
				result.add(word.substring(idx, i));
				idx = i;
			}
		}
		result.add(word.substring(idx));
		return result;
	}
}
