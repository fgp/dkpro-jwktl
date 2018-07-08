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
package de.tudarmstadt.ukp.jwktl.api.util;

import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;

/**
 * Enumeration for modelling non-finite {@link IWiktionaryWordForm}s.
 * Although other form properties (like {@link GrammaticalTense}) are
 * predominantly used to represent finite forms, such properties can
 * also be used to describe non-finite forms. For example, the English 
 * present participle (tense = PRESENT) and past participle (tense = PAST).
 * @author Christian M. Meyer
 */
public enum NonFiniteForm {

	/** The infinitive form of a verb (e.g., "(to) do"). */
	INFINITIVE,
	
	/** The participle form of a verb (e.g., "done"). Participle forms should
	 *  be combined with a {@link GrammaticalTense}. */
	PARTICIPLE,

	/** Verbal adverb (e.g. "holding" in "they walk holding their hands").
	 *  While in english (and german) these forms are often called present
	 *  participle, for slavic languages (BCS in particular) it makes sense
	 *  to separate them from participles because:
	 *
	 *  1. Their form is very different from that of participles (verbal
	 *     adverbs are in formed in BCS by appending -ući or -vši)
	 *  2. They are used with all verbs, not only auxillary verbs
	 *  3. They are not associated with a particular tense.
	 */
	VERBALADVERB,

	/** Verbal noun (e.g. in german forms ending in "-ung", in BCS forms
	 *  ending in "-enje" or "-anje")
	 */
	VERBALNOUN;

//	ATTRIBUTIVE,
//	CONVERB,	
//	GERUNDIVE,
//	GERUND;
	
}
