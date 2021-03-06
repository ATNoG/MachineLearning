package pt.it.av.tnav.ml.clustering.density;

import pt.it.av.tnav.ml.clustering.cluster.Cluster;
import pt.it.av.tnav.ml.clustering.curvature.Curvature;
import pt.it.av.tnav.utils.ArrayUtils;
import pt.it.av.tnav.utils.structures.Distance;
import pt.it.av.tnav.ml.clustering.curvature.DFDT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;


/**
 * DBSCAN algorithm implementation.
 *
 * @author <a href="mailto:mariolpantunes@gmail.com">Mário Antunes</a>
 * @version 1.0
 */
public class DBSCAN implements Density{

  public <D extends Distance<D>> List<Cluster<D>> clustering(final List<D> dps, final int minPts) {
    return clustering(dps, minPts, new DFDT());
  }

  public <D extends Distance<D>> List<Cluster<D>> clustering(final List<D> dps, final int minPts,
                                                             final Curvature curvature) {
    // array with average distance to closest minPts
    double dist[] = new double[dps.size()];
    double x[] = new double[dps.size()];

    // Find minPits closer points
    for (int i = 0; i < dps.size(); i++) {
      dist[i] = ArrayUtils.mean(kCloserPoints(dps, i, minPts));
      x[i] = i;
    }
    // Sort distances
    Arrays.sort(dist);

    // Find ideal EPS with elbow detection or Median
    int elbow = curvature.elbow(x, dist);
    double eps = (elbow>-1)?dist[elbow]:ArrayUtils.median(dist);

    // Find curvature and use it as EPS
    return clustering(dps, eps, minPts);
  }

  public static <D extends Distance<D>> double[] kCloserPoints(List<D> dps, final int idx, final int k) {
    double rv[] = new double[k];

    D dp = dps.get(idx);

    // Init the return array with the first k element distance, that are not the idx point
    int i = 0, rvIdx = 0;
    while (rvIdx < k) {
      if (i != idx) {
        rv[rvIdx] = dp.distanceTo(dps.get(i));
        rvIdx++;
      }
      i++;
    }

    // Find the index of the larger distance
    int maxIdx = ArrayUtils.max(rv);

    for (; i < dps.size(); i++) {
      if (i != idx) {
        double distance = dp.distanceTo(dps.get(i));
        if (distance < rv[maxIdx]) {
          rv[maxIdx] = distance;
          maxIdx = ArrayUtils.max(rv);
        }
      }
    }

    return rv;
  }

  /**
   *
   * @param dps
   * @param eps
   * @param minPts
   * @param <D>
   * @return
   */
  public <D extends Distance<D>> List<Cluster<D>> clustering(final List<D> dps, final double eps,
                                                             final int minPts) {
    int clusterCount = -1;
    int mapping[] = new int[dps.size()];
    Arrays.fill(mapping, -1);

    for (int i = 0; i < dps.size(); i++) {
      if (mapping[i] == -1) {
        List<Integer> neighbors = rangeQuery(i, dps, eps);
        if (neighbors.size() < minPts) {
          mapping[i] = -2; // Represents Noise
        } else {
          ++clusterCount;
          mapping[i] = clusterCount;
          Deque<Integer> seeds = new ArrayDeque<>(neighbors);
          while(!seeds.isEmpty()) {
            int q = seeds.pollFirst();
            if(mapping[q] == -2) {
              mapping[q] = clusterCount;
            } else if(mapping[q] == -1) {
              mapping[q] = clusterCount;
              List<Integer> neighborsQ = rangeQuery(q, dps, eps);
              if (neighborsQ.size() >= minPts) {
                for(int j = 0; j < neighborsQ.size(); j++) {
                  seeds.addLast(neighborsQ.get(j));
                }
              }
            }
          }
        }
      }
    }

    List<Cluster<D>> clusters = new ArrayList<>();
    for (int i = 0; i <= clusterCount; i++) {
      clusters.add(new Cluster<D>());
    }

    for (int i = 0; i < mapping.length; i++) {
      if (mapping[i] >= 0) {
        clusters.get(mapping[i]).add(dps.get(i));
      }
    }

    return clusters;
  }

  /**
   * Returns a list with the indexes of the closest neighbors.
   * This method implements the sequencial search.
   *
   * @param idx
   * @param dps
   * @param eps
   * @param <D>
   * @return
   */
  private <D extends Distance<D>> List<Integer> rangeQuery(final int idx, final List<D> dps,
                                                           final double eps) {
    List<Integer> rv = new ArrayList<>();
    D dp = dps.get(idx);

    for(int i = 0;  i < dps.size(); i++) {
      if(i != idx && dp.distanceTo(dps.get(i)) <= eps) {
        rv.add(i);
      }
    }

    return rv;
  }
}
