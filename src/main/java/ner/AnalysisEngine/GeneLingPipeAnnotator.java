package ner.AnalysisEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import ner.TypeSystem.BaseAnnotation;
import ner.util.PosTagNamedEntityRecognizer;

/**
 * The GeneLingPipeAnnotator class wraps the LingPipe ConfidenceChunker. It utilizes the wrapped
 * Chunker to annotate gene mentions in the document and convert the Chunker's output to {@link
 * <BaseAnnotation> [BaseAnnotation]} and adds them into the CAS's indexes. It can be configured
 * with the following parameters:
 * 
 * <ul>
 * <li><code>BestChunkNumber</code> - number of best chunks when annotating with LingPipe confidence
 * chunker</li>
 * </ul>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GeneLingPipeAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Name of configuration parameter which set the Number of Best Chunk.
   */
  public static final String PARAM_BESTCHUNKNUMBER = "BestChunkNumber";

  /**
   * LingPipe ConfidenceChunker
   */
  private ConfidenceChunker mChunker;

  /**
   * Number of Best Chunk, used by LingPipe ConfidenceChunker
   */
  private int mBestChunkNumber;

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {
    String docText = aJCas.getDocumentText();

    // add annotations to CAS
    Iterator<Chunk> it = mChunker.nBestChunks(docText.toCharArray(), 0, docText.length(),
            mBestChunkNumber);
    while (it.hasNext()) {
      Chunk chunk = it.next();
      int start = chunk.start();
      int end = chunk.end();
      float confidence = (float) Math.pow(2.0, chunk.score());

      BaseAnnotation annotation = new BaseAnnotation(aJCas);
      annotation.setBegin(start);
      annotation.setEnd(end);
      annotation.setSource(getClass().getName());
      annotation.setConfidence(confidence);
      annotation.addToIndexes();
    }
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // get the number of best chunk candidates
    mBestChunkNumber = (Integer) aContext.getConfigParameterValue("BestChunkNumber");

    // initialize LingPipe ConfidenceChunker
    File modelFile;
    try {
      modelFile = new File(getContext().getResourceFilePath("LingPipeGeneTagModel"));
      mChunker = (ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(
              ResourceInitializationException.COULD_NOT_ACCESS_DATA,
              new Object[] { "LingPipeGeneTagModel" }, e);
    } catch (IOException e) {
      throw new ResourceInitializationException(
              ResourceInitializationException.COULD_NOT_ACCESS_DATA,
              new Object[] { "LingPipeGeneTagModel" }, e);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }
}