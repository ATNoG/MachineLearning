package pt.it.av.tnav.ml.tm.dp.dpwOpt;

import pt.it.av.tnav.ml.tm.dp.DPW;
import pt.it.av.tnav.ml.tm.ngrams.NGram;

import java.util.List;

/**
 * @author Mário Antunes
 * @version 1.0
 */
public interface DPWOpt {
  /**
   * @param dpDimensions
   * @return
   */
  List<DPW.DpDimension> optimize(final NGram term, final List<DPW.DpDimension> dpDimensions);
}
