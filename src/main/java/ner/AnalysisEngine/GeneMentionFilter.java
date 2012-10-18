package ner.AnalysisEngine;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import ner.TypeSystem.BaseAnnotation;

/**
 * The GeneMentionFilter class is responsible to filter out duplicate annotations and annotations
 * with low confidences. It will filter all annotations whose confidence is lower than a threshold,
 * or annotations which match a group of filtering regular expressions. It can be configured with
 * the following parameters:
 * <ul>
 * <li><code>ConfidenceThreshold</code> - confidence threshold to filter annotations</li>
 * <li><code>FilteringRegularExpressions</code> - regular expressions to filter annotations</li>
 * </ul>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GeneMentionFilter extends JCasAnnotator_ImplBase {

  /**
   * Name of configuration parameter which set the confidence threshold.
   */
  public static final String PARAM_CONFIDENCETHRESHOLD = "ConfidenceThreshold";

  /**
   * Name of configuration parameter which set the filtering regular expressions.
   */
  public static final String PARAM_FILTERREGEX = "FilteringRegularExpressions";

  /**
   * The confidence threshold to filter annotations.
   */
  private float mConfidenceThreshold;

  /**
   * Compiled regular expressions
   */
  private Pattern[] mPatterns;

  /**
   * Iterates over the selected annotations in the CAS and filter annotations with low confidence
   * 
   * @param aCAS
   *          CasContainer which has been populated by the TAEs
   * 
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {

    LinkedList<BaseAnnotation> removedAnnotation = new LinkedList<BaseAnnotation>();

    // iterate and filter annotations with lower confidence than threshold
    Iterator annotationIter = aJCas.getAnnotationIndex(BaseAnnotation.type).iterator();
    while (annotationIter.hasNext()) {
      BaseAnnotation annot = (BaseAnnotation) annotationIter.next();
      if (annot.getConfidence() < mConfidenceThreshold) {
        removedAnnotation.add(annot);
      } else if (annot.getCoveredText().length() <= 1) {
        removedAnnotation.add(annot);
      } else if ((annot.getCoveredText().contains("(") && annot.getCoveredText().contains(")"))
              || (annot.getCoveredText().contains(")") && annot.getCoveredText().contains("("))) {
        removedAnnotation.add(annot);

      } else if (mPatterns != null) {
        for (int i = 0; i < mPatterns.length; i++) {
          Matcher matcher = mPatterns[i].matcher(annot.getCoveredText());
          if (matcher.find()) {
            removedAnnotation.add(annot);
          }
        }
      }
    }

    // iterate and remove annotations
    annotationIter = removedAnnotation.iterator();
    while (annotationIter.hasNext()) {
      BaseAnnotation annot = (BaseAnnotation) annotationIter.next();
      annot.removeFromIndexes(aJCas);
    }

  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // get the confidence threshold
    mConfidenceThreshold = (Float) aContext.getConfigParameterValue(PARAM_CONFIDENCETHRESHOLD);

    // get filtering regular expressions strings
    String[] patternStrings = (String[]) aContext.getConfigParameterValue(PARAM_FILTERREGEX);

    // compile filtering regular expressions
    if (patternStrings != null) {
      mPatterns = new Pattern[patternStrings.length];
      for (int i = 0; i < patternStrings.length; i++) {
        mPatterns[i] = Pattern.compile(patternStrings[i]);
      }
    } else
      mPatterns = null;
  }
}