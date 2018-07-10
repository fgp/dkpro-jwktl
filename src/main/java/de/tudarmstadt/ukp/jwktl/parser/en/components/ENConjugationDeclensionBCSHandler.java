/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.jwktl.parser.en.components;

import de.tudarmstadt.ukp.jwktl.api.IWiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryEntry;
import de.tudarmstadt.ukp.jwktl.api.entry.WiktionaryWordForm;
import de.tudarmstadt.ukp.jwktl.api.util.*;
import de.tudarmstadt.ukp.jwktl.api.util.TemplateParser.Template;
import de.tudarmstadt.ukp.jwktl.parser.util.ParsingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Support for parsing word forms for non-English entries in the English Wiktionary.
 */
public class ENConjugationDeclensionBCSHandler extends ENBlockHandler implements TemplateParser.ITemplateHandler {

    private static final Logger logger = Logger.getLogger(ENConjugationDeclensionBCSHandler.class.getName());

    protected List<IWiktionaryWordForm> wordForms;

    public ENConjugationDeclensionBCSHandler() {
        super("Conjugation", "Declension");
    }

    @Override
    public boolean processHead(String textLine, ParsingContext context) {
        wordForms = new ArrayList<IWiktionaryWordForm>();
        return true;
    }

    @Override
    public boolean processBody(String textLine, ParsingContext context) {
        if (textLine.startsWith("{{sh-"))
            TemplateParser.parse(textLine, this);
        return true;
    }

    public String handle(final Template template) {
        if (template.getName().startsWith("sh-decl-noun")) {
            handleNounTemplate(template, template.getName());
        } else if ("sh-conj".equals(template.getName())) {
            handleVerbTemplate(template);
        } else if ("sh-adj-full".equals(template.getName())) {
            handleAdjectiveTemplate(template);
        } else if (template.getName().startsWith("sh-")) {
            logger.warning(String.format("Unknown BCS wordform template %s", template.getName()));
        }
        return null;
    }

    protected void handleNounTemplate(final Template template, String templateName) {
        /* See https://en.wiktionary.org/wiki/Template:sh-decl-noun */
        GrammaticalCase[] cases = { GrammaticalCase.NOMINATIVE,
                                    GrammaticalCase.GENITIVE,
                                    GrammaticalCase.DATIVE,
                                    GrammaticalCase.ACCUSATIVE,
                                    GrammaticalCase.VOCATIVE,
                                    GrammaticalCase.LOCATIVE,
                                    GrammaticalCase.INSTRUMENTAL };
        GrammaticalNumber[] numbers;
        if (templateName.equals("sh-decl-noun-plural"))
            numbers = new GrammaticalNumber[] { GrammaticalNumber.PLURAL };
        else
            numbers = new GrammaticalNumber[] { GrammaticalNumber.SINGULAR,
                                                GrammaticalNumber.PLURAL };
        int p = 0;
        for(GrammaticalCase c: cases) {
            for(GrammaticalNumber n: numbers) {
                String w = template.getNumberedParam(p++);
                if (w == null) {
                    logger.warning(String.format("Missing entry number %d", p));

                }
                WiktionaryWordForm nounform = new WiktionaryWordForm(w);
                nounform.setCase(c);
                nounform.setNumber(n);
                wordForms.add(nounform);
            }
        }
    }

    protected void handleVerbTemplate(final Template template) {
        /* See https://en.wiktionary.org/wiki/Template:sh-conj */
        for(Map.Entry<String, String> e: template.getNamedParams()) {
            /* Get parameter (which are of the form x.y) and split into x and y) */
            String p_raw = e.getKey();
            String[] p = p_raw.split("\\.");
            /* Create verbform object */
            WiktionaryWordForm verbform = new WiktionaryWordForm(e.getValue());
            /* Set tense, person, number, aspect and gender of the verb form */
            switch(p[0]) {
                case "pr":
                    /* present tense (incl. present verbal adverb 'pr.va' for impf./nesv. verbs)
                     */
                    verbform.setTense(GrammaticalTense.PRESENT);
                    switch(p[1]) {
                        case "va": verbform.setNonFiniteForm(NonFiniteForm.VERBALADVERB); break;
                        default:
                            if (!decodeGrammaticalPerson(verbform, p[1].charAt(0)) ||
                                !decodeGrammaticalNumber(verbform, p[1].charAt(1)))
                            {
                                logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                                verbform = null;
                            }
                    }
                    break;
                case "a":
                    /* aorist (pf./sv. verbs only)
                     * Key format: a.{1,2,3}{s,p}
                     * Marking aorist forms as perfective (nesvršen) is a tad redundent,
                     * because these forms only exist for perfective (svrsšen) verbs. But having
                     * to check the aspect of the entry to decide whether a particular form
                     * is an aorist or imperfect seems inconvenient and error-prone, and wouldn't
                     * work for bi-aspectual verbs anyway, so we just live with the slight redundancy
                     * and mark all aorist forms as perfective.
                     */
                    verbform.setTense(GrammaticalTense.PAST);
                    verbform.setAspect(GrammaticalAspect.PERFECT);
                    if (!decodeGrammaticalPerson(verbform, p[1].charAt(0)) ||
                        !decodeGrammaticalNumber(verbform, p[1].charAt(1)))
                    {
                        logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                        verbform = null;
                    }
                    break;
                case "impt":
                    /* imperfect tense (impf./nesv. verbs).
                     * Key format: impt.{1,2,3}{s,p}
                     * Marking imperfect forms as
                     * See the discussion above for the aorist as to why we mark all imperfect
                     * forms as having the imperfective (nesvršen) aspect even though such forms
                     * exist only for imperfective (nesvršen) verbs anyway.
                     */
                    verbform.setTense(GrammaticalTense.PAST);
                    verbform.setAspect(GrammaticalAspect.IMPERFECT);
                    if (!decodeGrammaticalPerson(verbform, p[1].charAt(0)) ||
                        !decodeGrammaticalNumber(verbform, p[1].charAt(1)))
                    {
                        logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                        verbform = null;
                    }
                    break;
                case "app":
                    /* past participle active.
                     * Key format: app.{m,f,n}{s,p}
                     */
                    verbform.setNonFiniteForm(NonFiniteForm.PARTICIPLE);
                    if (!decodeGrammaticalGender(verbform, p[1].charAt(0)) ||
                        !decodeGrammaticalNumber(verbform, p[1].charAt(1)))
                    {
                        logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                        verbform = null;
                    }
                    break;
                case "p":
                    /* past (only past verbal adverb 'p.va', for pf/.sv. verbs, unenforced)
                     * Key format: p.va
                     */
                    switch (p[1]) {
                        case "va": verbform.setNonFiniteForm(NonFiniteForm.VERBALADVERB); break;
                        default:
                            logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                            verbform = null;
                    }
                case "f1":
                    /* future (infinitive form for croatian (hrvatski) form of future tense 'fl.hr',
                     *         stem used for serbian (srpski) form of future tense 'fl.stem')
                     * These forms are easily reconstructed from the infinitive. For infinitives
                     * ending on '-ći', both are identical with the infinitive. For infinitives
                     * ending on '-ti', the croatien form 'fl.hr' is formed by removing the final 'i',
                     * and the serbian form 'fl.stem' by removing the whole ending '-ti'. Since
                     * the rules are that simple and don't have a single exception as far as I'm
                     * aware, we skip these forms here.
                     */
                    break;
                case "vn":
                    /* verbal noun
                     * Key format: vn */
                    verbform.setNonFiniteForm(NonFiniteForm.VERBALNOUN);
                    break;
                default:
                    logger.warning(String.format("Unknown verb conjugation parameter %s", p_raw));
                    verbform = null;
                    break;
            }

            if (verbform != null)
                wordForms.add(verbform);
        }
    }

    protected void handleAdjectiveTemplate(final Template template) {
        /* See https://en.wiktionary.org/wiki/Template:sh-adj-full */
        String pos_stem = template.getNumberedParam(0);
        String pos_suffix_neu = template.getNumberedParam(1);
        String cmp_stem = template.getNumberedParam(2);
        String cmp_suffix_neu = template.getNumberedParam(3);

        WiktionaryWordForm pos_neu_nom_sg = new WiktionaryWordForm(pos_stem + pos_suffix_neu);
        pos_neu_nom_sg.setDegree(GrammaticalDegree.POSITIVE);
        pos_neu_nom_sg.setGender(GrammaticalGender.NEUTER);
        pos_neu_nom_sg.setCase(GrammaticalCase.NOMINATIVE);
        wordForms.add(pos_neu_nom_sg);

        WiktionaryWordForm cmp_neu_nom_sg = new WiktionaryWordForm(cmp_stem + cmp_suffix_neu);
        cmp_neu_nom_sg.setDegree(GrammaticalDegree.COMPARATIVE);
        cmp_neu_nom_sg.setGender(GrammaticalGender.NEUTER);
        cmp_neu_nom_sg.setCase(GrammaticalCase.NOMINATIVE);
        wordForms.add(cmp_neu_nom_sg);
    }

    protected boolean decodeGrammaticalPerson(WiktionaryWordForm form, char person) {
        switch (person) {
            case '1':
                form.setPerson(GrammaticalPerson.FIRST);
                return true;
            case '2':
                form.setPerson(GrammaticalPerson.SECOND);
                return true;
            case '3':
                form.setPerson(GrammaticalPerson.THIRD);
                return true;
            default:
                return false;
        }
    }

    protected boolean decodeGrammaticalNumber(WiktionaryWordForm form, char number) {
        switch (number) {
            case 's':
                form.setNumber(GrammaticalNumber.SINGULAR);
                return true;
            case 'p':
                form.setNumber(GrammaticalNumber.PLURAL);
                return true;
            default:
                return false;
        }
    }

    protected boolean decodeGrammaticalGender(WiktionaryWordForm form, char gender) {
        switch (gender) {
            case 'm':
                form.setGender(GrammaticalGender.MASCULINE);
                return true;
            case 'f':
                form.setGender(GrammaticalGender.FEMININE);
                return true;
            case 'n':
                form.setGender(GrammaticalGender.NEUTER);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void fillContent(final ParsingContext context) {
        WiktionaryEntry entry = context.findEntry();
        wordForms.forEach(entry::addWordForm);
    }
}
