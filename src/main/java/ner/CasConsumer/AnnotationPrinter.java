package ner.CasConsumer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.base_cpm.CasObjectProcessor;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import ner.TypeSystem.BaseAnnotation;
import ner.TypeSystem.SourceSentenceInformation;

/**
 * AnnotationPrinter prints to an output file all annotations in the CAS. <br>
 * Parameters needed by the AnnotationPrinter are
 * <ul>
 * <li><code>OutputFile</code> - where the output files should be written.</li>
 * </ul>
 * <br>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class AnnotationPrinter extends CasConsumer_ImplBase implements CasObjectProcessor {

  /**
   * Name of configuration parameter which set where the output file is.
   */
  public static final String PARAM_OUTPUTFILE = "OutputFile";

  /**
   * Output File
   */
  File outFile;

  /**
   * Output File Writer
   */
  FileWriter fileWriter;

  /**
   * Initializes this CAS Consumer with the parameters specified in the descriptor.
   * 
   * @throws ResourceInitializationException
   *           if there is error in initializing the resources
   */
  public void initialize() throws ResourceInitializationException {

    String oPath = (String) getUimaContext().getConfigParameterValue(PARAM_OUTPUTFILE);

    if (oPath == null) {
      throw new ResourceInitializationException(
              ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {PARAM_OUTPUTFILE});
    }

    outFile = new File(oPath.trim());
    if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
      if (!outFile.getParentFile().mkdirs())
        throw new ResourceInitializationException(
                ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { oPath,
                    PARAM_OUTPUTFILE });
    }
    try {
      fileWriter = new FileWriter(outFile);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * Iterates over the selected annotations in the CAS and prints out into the output file
   * 
   * @param aCAS
   *          CasContainer which has been populated by the TAEs
   * 
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * 
   * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(CAS)
   */
  public synchronized void processCas(CAS aCAS) throws ResourceProcessException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    String identifier = null;
    Iterator it = jcas.getAnnotationIndex(SourceSentenceInformation.type).iterator();
    if (it.hasNext()) {
      SourceSentenceInformation srcDocInfo = (SourceSentenceInformation) it.next();
      identifier = srcDocInfo.getIdentifier();
    }

    // iterate and print annotations
    Iterator annotationIter = jcas.getAnnotationIndex(BaseAnnotation.type).iterator();
    while (annotationIter.hasNext()) {
      BaseAnnotation annot = (BaseAnnotation) annotationIter.next();

      // get the number of spaces in the prefix string of this entity
      int prefixSpaceNum = 0;
      String prefixString = jcas.getDocumentText().substring(0, annot.getBegin());
      for (int i = 0; i < prefixString.length(); i++) {
        if (prefixString.charAt(i) == ' ')
          prefixSpaceNum++;
      }

      // get the number of spaces in the content string of this entity
      int contentSpaceNum = 0;
      String contentString = jcas.getDocumentText().substring(annot.getBegin(), annot.getEnd());
      for (int i = 0; i < contentString.length(); i++) {
        if (contentString.charAt(i) == ' ')
          contentSpaceNum++;
      }

      // calculate the non-space version beg and end position and form the output string
      String outputString = identifier + "|" + (annot.getBegin() - prefixSpaceNum) + " "
              + (annot.getEnd() - prefixSpaceNum - contentSpaceNum - 1) + "|"
              + annot.getCoveredText();

      // output the result
      try {
        fileWriter.write(outputString);
        fileWriter.write("\n");
        fileWriter.flush();
      } catch (IOException e) {
        throw new ResourceProcessException(e);
      }
    }
  }

  /**
   * Called when the entire collection is completed.
   * 
   * @param aTrace
   *          ProcessTrace object that will log events in this method.
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * @throws IOException
   *           if there is an IO Error
   * @see org.apache.uima.collection.CasConsumer#collectionProcessComplete(ProcessTrace)
   */
  public void collectionProcessComplete(ProcessTrace aTrace) throws ResourceProcessException,
          IOException {
    if (fileWriter != null) {
      fileWriter.close();
    }
  }

  /**
   * Called if clean up is needed in case of exit under error conditions.
   * 
   * @see org.apache.uima.resource.Resource#destroy()
   */
  public void destroy() {
    if (fileWriter != null) {
      try {
        fileWriter.close();
      } catch (IOException e) {
        // ignore IOException on destroy
      }
    }
  }

}
