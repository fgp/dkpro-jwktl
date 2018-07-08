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
 * Enumeration of the grammatical case of a {@link IWiktionaryWordForm}. 
 * @author Christian M. Meyer
 */
public enum GrammaticalCase {

	/** Indicates the subject of a finite verb. Ask "Wer/Was?" in
	 *  German sentences (e.g., "_Peter_ liest"). */
	NOMINATIVE,
	
	/** Indicates the direct object of a verb. Ask "Wen/Was?" in
	 *  German sentences (e.g., "Peter liest _ein Buch_"). */
	ACCUSATIVE, 
	
	/** Indicates the indirect object of a verb. Ask "Wem?" in
	 *  German sentences (e.g., "Peter liest _ihr_ vor").*/
	DATIVE, 
	
	/** Indicates possession. Ask "Wessen?" in German sentences 
	 *  (e.g., "_Peters_ Buch ist spannend").*/
	GENITIVE, //

	/** Used when directly addressing someone (rarely also something),
	 *  (e.g., in BCS "Gdje si Brat_e_?", "Where are you brother?")
	 */
	VOCATIVE,

	/** Indicates the location of something. Ask "Na/U/O čemu?" in BCS
	  * (e.g. "slika je na stol_u_", "the image is on the table") */
	LOCATIVE,

	/** Indicates the instrument of an action. Ask "Čime?" in BCS
	 *  (e.g. in BCS "Idem autobus_om_", "travelling by bus") */
	INSTRUMENTAL;

	//ABLATIVE, // indicates movement from smth. or cause

}
