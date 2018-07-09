/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.jwktl.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionarySense;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.util.ILanguage;
import de.tudarmstadt.ukp.jwktl.parser.util.IBlockHandler;
import de.tudarmstadt.ukp.jwktl.parser.util.ParsingContext;

/**
 * Base implementation for parsing the textual contents of an article page in
 * order to construct {@link IWiktionaryEntry} and {@link IWiktionarySense}
 * instances. The parser is based on a finite state machine using a set
 * of block handlers that are being asked if they want to process the current 
 * line of text. If so, the handler is in a position to process the subsequent 
 * lines until the entire block has been processed and the next line is
 * subject to initialize a different block handler. Since there are large
 * differences between the individual Wiktionary language editions, there
 * should be one subclass of this parser for each language edition, which
 * cares about language-specific adaptation and the selection of the 
 * block handlers used.
 * @author Christian M. Meyer
 * @author Christof Müller
 */
public abstract class WiktionaryEntryParser implements IWiktionaryEntryParser {

	private static enum ParseStatus{
		IN_BODY,
		IN_HEAD
	}
	
	private static Logger logger = Logger.getLogger(WiktionaryEntryParser.class.getName());
	
	protected static final Pattern COMMENT_PATTERN = Pattern.compile("\\<!--((?!--\\>)[^\0])*?--\\>");
	protected static final Pattern IMAGE_PATTERN = Pattern.compile("\\[\\[Image:([^\\]]+?)\\|[^\\]]+?\\]\\]");
	protected static final Pattern REFERENCES_PATTERN = Pattern.compile("<ref[^>]*>.+?</ref>");
	protected static final Pattern TEMPLATE_START_PATTERN = Pattern.compile("\\{\\{");
	protected static final Pattern TEMPLATE_END_PATTERN = Pattern.compile("\\}\\}");

	protected ILanguage language;
	protected String redirectTemplate;
	protected long entryId;
	protected List<IBlockHandler> handlers;

	/** Instanciates the entry parser for the given language. 
	 *  @param redirectName denotes the language-specific prefix used for 
	 *    redirections. */
	public WiktionaryEntryParser(final ILanguage language,
			final String redirectName) {
		handlers = new LinkedList<>();
		this.language = language;
		this.redirectTemplate = "#" + redirectName + " [[";
		entryId = 0;
	}
	
	
	public void parse(final WiktionaryPage page, String text) {
		//if (!"yuga".equals(page.getTitle()))return;
		//if (!page.getTitle().startsWith("a")) return;
		
		// Handle redirects.
		if (checkForRedirect(page, text))
			return;
		
		final String LINE_SEPERATOR = "\n";	
		
		// Remove html comments.
		text = COMMENT_PATTERN.matcher(text).replaceAll("");      
		text = IMAGE_PATTERN.matcher(text).replaceAll("");
		text = REFERENCES_PATTERN.matcher(text).replaceAll("");
		BufferedReader reader = new BufferedReader(new StringReader(text));
		try {
		
		// contains information shared by workers. 
		ParsingContext context = createParsingContext(page);
		String currentLine = reader.readLine();
		IBlockHandler handler = null;
		IBlockHandler unfinishedHandler = null;
		ParseStatus status = ParseStatus.IN_HEAD;
		
		// Decides if parser take control of the parsing or let a worker object decide if its work is finished.
		boolean parserTakeControl = false;
		boolean EOT = (currentLine == null);
		while (!EOT) {
			currentLine = currentLine.trim();
			String bufferedLinesSep = currentLine + LINE_SEPERATOR;
			if (status == ParseStatus.IN_HEAD) {
				// HEAD
				if (isStartOfBlock(currentLine)) {
					handler = selectHandler(currentLine);
					logger.fine("preprocessing " + currentLine + " worker is " + handler);
				}
				
				// continue only when the worker finishes processing head part.
				if (handler != null && handler.processHead(bufferedLinesSep, context)){
					logger.fine("processing " + currentLine);
					status = ParseStatus.IN_BODY;
					unfinishedHandler = handler;
				}
				currentLine = reader.readLine();
				
			} else 
			if (status == ParseStatus.IN_BODY) {
				/* If a template reference is split over multiple lines, keep accumulating
				 * text in {@currentLine} until the reference is completed.
				 */
				int incomplete_templates = 0;
				boolean fetch_next_line = false;
				do {
					if (fetch_next_line) {
						logger.fine(String.format("%d incomplete templates, buffering '%s'",
								incomplete_templates, currentLine));
						currentLine = reader.readLine();
						if (currentLine == null)
							break;
						bufferedLinesSep = bufferedLinesSep + currentLine + LINE_SEPERATOR;
					}
					incomplete_templates += countMatches(TEMPLATE_START_PATTERN, currentLine);
					incomplete_templates -= countMatches(TEMPLATE_END_PATTERN, currentLine);
					if (incomplete_templates < 0) {
						logger.warning(String.format("Too many '}}' on page %s", page.getTitle()));
						incomplete_templates = 0;
					}
					fetch_next_line = true;
				} while (incomplete_templates != 0);

				// BODY
				if (!parserTakeControl) {						
					parserTakeControl = !handler.processBody(bufferedLinesSep, context);
					if (parserTakeControl) {
						if (isStartOfBlock(bufferedLinesSep)) {
							handler.fillContent(context);
							unfinishedHandler = null;								
							handler = null;
							status = ParseStatus.IN_HEAD;
							parserTakeControl = false;
						} else
							currentLine = reader.readLine();
					} else
						currentLine = reader.readLine();
				} else {						
					if (isStartOfBlock(currentLine)) {
						handler.fillContent(context);
						unfinishedHandler = null;
						handler = null;
						status = ParseStatus.IN_HEAD;
						parserTakeControl = false;
					} else {
						handler.processBody(bufferedLinesSep, context);
						currentLine = reader.readLine();
					}
				}
			}
			
			if (currentLine == null) {
				if (unfinishedHandler != null)
					unfinishedHandler.fillContent(context);
				EOT = true;
			}
		}

		} catch (IOException e) {
			throw new RuntimeException("Error while parsing text of article " + page.getTitle(), e);
		}
	}
	
	protected abstract ParsingContext createParsingContext(
			final WiktionaryPage page);


	/** Check if the specified text is a redirect and set the redirect target of 
	 *  the given Wiktionary page. */
	protected boolean checkForRedirect(final WiktionaryPage page, 
			final String text) {
		if (text.endsWith("]]") && text.startsWith(redirectTemplate)) {
			page.setRedirectTarget(text.substring(redirectTemplate.length(),
					text.length() - 2));
			//System.err.println("REDIRECT " + page.getTitle() + " -> " + page.getRedirectTarget());
			return true;
		} else
			return false;
	}

	protected int countMatches(Pattern pattern, String string) {
		Matcher m = pattern.matcher(string);
		int count = 0;
		while (m.find())
			count++;
		return count;
	}

	/** Hotspot for deciding if the given line is a potential start of a new
	 *  article constituent. This may include headlines, templates, or other 
	 *  typographic variants. */
	protected abstract boolean isStartOfBlock(final String line);

	/** Find a handler that is willing to handle the given line. */
	protected IBlockHandler selectHandler(final String line) {
		for (IBlockHandler handler : handlers) {
			if (handler.canHandle(line))
				return handler;
		}
		return null;
	}

	/** Register the given handler that will be invoked during the parsing. */
	protected void register(final IBlockHandler handler) {
		handlers.add(handler);
	}
	
	/** Returns the language of this parser's Wiktionary edition. */
	public ILanguage getLanguage() {
		return language;
	}
			
}
