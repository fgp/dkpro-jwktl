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
package de.tudarmstadt.ukp.jwktl.parser.en;

import de.tudarmstadt.ukp.jwktl.api.RelationType;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.util.Language;
import de.tudarmstadt.ukp.jwktl.parser.IWiktionaryEntryParser;
import de.tudarmstadt.ukp.jwktl.parser.WiktionaryEntryParser;
import de.tudarmstadt.ukp.jwktl.parser.components.CategoryHandler;
import de.tudarmstadt.ukp.jwktl.parser.components.InterwikiLinkHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENDescendantRelationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENEntryFactory;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENEtymologyHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENPronunciationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENQuotationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENReferenceHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENRelationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENSemanticRelationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENSenseHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENTranslationHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENUsageNotesHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENWordLanguageHandler;
import de.tudarmstadt.ukp.jwktl.parser.en.components.ENConjugationDeclensionBCSHandler;
import de.tudarmstadt.ukp.jwktl.parser.util.ParsingContext;

/**
 * An implementation of the {@link IWiktionaryEntryParser} interface for 
 * parsing the contents of article pages from the English Wiktionary. 
 * @author Christian M. Meyer
 */
public class ENWiktionaryEntryParser extends WiktionaryEntryParser {

	/** Initializes the English entry parser. That is, the language and the
	 *  redirection pattern is defined, and the handlers for extracting
	 *  the information from the article constituents are registered. */
	public ENWiktionaryEntryParser() {
		super(Language.ENGLISH, "REDIRECT");
		
		// Fixed name content handlers.
		register(new ENSemanticRelationHandler(RelationType.SYNONYM, "Synonyms", "Synomyms", "Synoynms"));
		register(new ENSemanticRelationHandler(RelationType.ANTONYM, "Antonyms"));
		register(new ENSemanticRelationHandler(RelationType.HYPERNYM, "Hypernyms"));
		register(new ENSemanticRelationHandler(RelationType.HYPONYM, "Hyponyms"));
		register(new ENSemanticRelationHandler(RelationType.HOLONYM, "Holonyms"));
		register(new ENSemanticRelationHandler(RelationType.MERONYM, "Meronyms"));
		register(new ENSemanticRelationHandler(RelationType.TROPONYM, "Troponyms"));
		register(new ENSemanticRelationHandler(RelationType.COORDINATE_TERM, "Coordinate terms"));
		register(new ENSemanticRelationHandler(RelationType.SEE_ALSO, "See also"));
		register(new ENRelationHandler(RelationType.DERIVED_TERM, "Derived terms"));
		register(new ENRelationHandler(RelationType.ETYMOLOGICALLY_RELATED_TERM, "Related terms"));
		register(new ENDescendantRelationHandler("Descendants"));
		register(new ENTranslationHandler());
		register(new ENEtymologyHandler());
		register(new ENConjugationDeclensionBCSHandler());
		register(new ENReferenceHandler());
		register(new ENQuotationHandler());
		register(new ENPronunciationHandler());
		register(new ENUsageNotesHandler());

		// Pattern
		register(new CategoryHandler("Category"));
		register(new InterwikiLinkHandler("Category"));
		register(new ENWordLanguageHandler());
		register(new ENSenseHandler());
	}
	
	@Override
	protected ParsingContext createParsingContext(final WiktionaryPage page) {
		return new ParsingContext(page, new ENEntryFactory());
	}

	/** Checks if it is start of new section. Symbols are =, [[ */
	protected boolean isStartOfBlock(String line) {
		line = line.trim();
		if (line.startsWith("----")) {
			return true;
		}
		if (line.startsWith("="))
			return true;
		if (line.startsWith("[[") && line.endsWith("]]"))
			return true;
		
		return false;
	}

}
