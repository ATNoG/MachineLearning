package pt.it.av.tnav.ml.clustering.curvature;

import pt.it.av.tnav.ml.regression.UnivariateRegression;

import java.lang.ref.WeakReference;

/**
 * A-method to detect knee/elbow points.
 * <p>
 *   Angle based method.
 *   Based on the definition of function curvature.
 * </p>
 *
 * @author <a href="mailto:mariolpantunes@gmail.com">Mário Antunes</a>
 * @version 1.0
 */
public class Amethod extends BaseCurvature {
  private static WeakReference<Curvature> wrc = null;
  protected static final int MINCUTOFF = 20;

  @Override
  public int find_knee(final double x[], final double[] y) {
    return itRefinement(x, y);
  }

  @Override
  public int find_elbow(final double x[], final double[] y) {
    return itRefinement(x, y);
  }

  /**
   *
   * @param x
   * @param y
   * @return
   */
  private int itRefinement(final double x[], final double[] y) {
    int cutoff = x.length, lastPoint, point = x.length;

    do {
      lastPoint = point;
      point = aMethod(x, y, cutoff);
      cutoff = Math.min(point * 2, x.length);
    } while (point < lastPoint && cutoff >= MINCUTOFF);

    return point;
  }

  /**
   *
   * @param x
   * @param y
   * @return
   */
  private int aMethod(final double x[], final double[] y, final int length) {
    int idx = 1;
    UnivariateRegression.LR lrl = UnivariateRegression.lr(x, y,0,0,idx+1),
        lrr = UnivariateRegression.lr(x, y, idx, idx, length - idx);
    double lmetric = Math.pow(90.0 - lrl.angle(lrr), 2.0);;

    for(int i = 2; i < length-1; i++) {
      lrl = UnivariateRegression.lr(x, y,0,0,i+1);
      lrr = UnivariateRegression.lr(x, y, i, i, length - i);

      double clmetric = Math.pow(90.0 - lrl.angle(lrr), 2.0);;

      if(clmetric < lmetric) {
        idx = i;
        lmetric = clmetric;
      }
    }

    return idx;
  }

  /**
   *
   * @param x
   * @param y
   * @param lr
   * @param idx
   * @param length
   * @return
   */
  public static double rmse(final double x[], final double[] y, final UnivariateRegression.LR lr,
                            final int idx, final int length){
    double mse = 0.0;

    for(int i = idx; i < length; i++) {
      mse += Math.pow(y[i] - lr.solve(x[i]), 2.0);
    }

    return Math.sqrt(mse);
  }

  /**
   * Builds a static {@link WeakReference} to a {@link Curvature} class.
   * <p>
   *   This method should be used whenever the {@link Curvature} will be built and destroy multiple times.
   *   It will also share a single stemmer through several process/threads.
   * </p>
   *
   * @return {@link Curvature} reference that points to a {@link Amethod}.
   */
  public synchronized static Curvature build() {
    Curvature rv = null;
    if (wrc == null) {
      rv = new DFDT();
      wrc = new WeakReference<>(rv);
    } else {
      rv = wrc.get();
      if(rv == null) {
        rv = new DFDT();
        wrc = new WeakReference<>(rv);
      }
    }
    return rv;
  }
}
