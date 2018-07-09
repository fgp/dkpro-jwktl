package de.tudarmstadt.ukp.jwktl.parser.en.components;

import de.tudarmstadt.ukp.jwktl.api.IWiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryPage;
import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.PartOfSpeech;
import de.tudarmstadt.ukp.jwktl.api.util.*;
import de.tudarmstadt.ukp.jwktl.parser.en.ENWiktionaryEntryParserTest;

import java.util.Map;

/**
 * Test case for {@link ENWordFormHandlerBCS}.
 * @author Florian G. Pflug
 */
public class ENWordFormHandlerBCSTest extends ENWiktionaryEntryParserTest {
    static class VerbFormKey {
        public NonFiniteForm nonFiniteForm;
        public GrammaticalTense tense;
        public GrammaticalAspect aspect;
        public GrammaticalPerson person;
        public GrammaticalGender gender;
        public GrammaticalNumber number;

        VerbFormKey() {
        };

        VerbFormKey(IWiktionaryWordForm verbForm) {
            this.nonFiniteForm = verbForm.getNonFiniteForm();
            this.tense = verbForm.getTense();
            this.aspect = verbForm.getAspect();
            this.person = verbForm.getPerson();
            this.gender = verbForm.getGender();
            this.number = verbForm.getNumber();
        }

        VerbFormKey(NonFiniteForm nonFiniteForm, GrammaticalTense tense,
                    GrammaticalAspect aspect, GrammaticalPerson person,
                    GrammaticalGender gender, GrammaticalNumber number)
        {
            this.nonFiniteForm = nonFiniteForm;
            this.tense = tense;
            this.aspect = aspect;
            this.person = person;
            this.gender = gender;
            this.number = number;
        }
    }

    /***/
    public void testProvjeriti() throws Exception {
        IWiktionaryPage page = parse("provjeriti.txt");
        IWiktionaryEntry entry = page.getEntry(0);
        assertEquals(PartOfSpeech.VERB, entry.getPartOfSpeech());
        assertEquals(GrammaticalAspect.PERFECT, entry.getAspect());

        Map<VerbFormKey, String> forms = new java.util.HashMap<VerbFormKey, String>();
        for(IWiktionaryWordForm wordFormEntry: entry.getWordForms())
            forms.put(new VerbFormKey(wordFormEntry), wordFormEntry.getWordForm());

        /* present tense conjugation */
        VerbFormKey present = new VerbFormKey();
        present.tense = GrammaticalTense.PRESENT;
        present.number = GrammaticalNumber.SINGULAR;
        present.person = GrammaticalPerson.FIRST;
        assertEquals("provjerim", forms.get(present));
        present.person = GrammaticalPerson.SECOND;
        assertEquals("provjeriš", forms.get(present));
        present.person = GrammaticalPerson.THIRD;
        assertEquals("provjeri", forms.get(present));
        present.number = GrammaticalNumber.PLURAL;
        present.person = GrammaticalPerson.FIRST;
        assertEquals("provjerimo", forms.get(present));
        present.person = GrammaticalPerson.SECOND;
        assertEquals("provjerite", forms.get(present));
        present.person = GrammaticalPerson.THIRD;
        assertEquals("provjere", forms.get(present));

        /* aorist conjugation */
        VerbFormKey aorist = new VerbFormKey();
        aorist.aspect = GrammaticalAspect.PERFECT;
        aorist.tense = GrammaticalTense.PAST;
        aorist.number = GrammaticalNumber.SINGULAR;
        aorist.person = GrammaticalPerson.FIRST;
        assertEquals("provjerih", forms.get(aorist));
        aorist.person = GrammaticalPerson.SECOND;
        assertEquals("provjeri", forms.get(aorist));
        aorist.person = GrammaticalPerson.THIRD;
        assertEquals("provjeri", forms.get(aorist));
        aorist.number = GrammaticalNumber.PLURAL;
        aorist.person = GrammaticalPerson.FIRST;
        assertEquals("provjerismo", forms.get(aorist));
        aorist.person = GrammaticalPerson.SECOND;
        assertEquals("provjeriste", forms.get(aorist));
        aorist.person = GrammaticalPerson.THIRD;
        assertEquals("provjereše", forms.get(aorist));

        /* active past participle  */
        VerbFormKey app = new VerbFormKey();
        app.nonFiniteForm = NonFiniteForm.PARTICIPLE;
        app.number = GrammaticalNumber.SINGULAR;
        app.gender = GrammaticalGender.MASCULINE;
        assertEquals("provjerio", forms.get(app));
        app.gender = GrammaticalGender.FEMININE;
        assertEquals("provjerila", forms.get(app));
        app.gender = GrammaticalGender.NEUTER;
        assertEquals("provjerilo", forms.get(app));
        app.number = GrammaticalNumber.PLURAL;
        app.gender = GrammaticalGender.MASCULINE;
        assertEquals("provjerili", forms.get(app));
        app.gender = GrammaticalGender.FEMININE;
        assertEquals("provjerile", forms.get(app));
        app.gender = GrammaticalGender.NEUTER;
        assertEquals("provjerila", forms.get(app));

        /* verbal adverb */
        VerbFormKey vn = new VerbFormKey();
        vn.nonFiniteForm = NonFiniteForm.VERBALADVERB;
        vn.aspect = GrammaticalAspect.PERFECT;
        assertEquals("provjerivši", forms.get(vn));

    }
}
