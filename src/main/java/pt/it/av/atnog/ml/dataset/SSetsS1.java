package pt.it.av.atnog.ml.dataset;

import pt.it.av.atnog.utils.bla.Vector;
import pt.it.av.atnog.utils.structures.Point2D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SSetsS1 implements Dataset<Point2D> {
  private final List<Point2D> dataset = new ArrayList<>(5000);

  public SSetsS1(final String filename) {
    this(new File(filename));
  }

  public SSetsS1() {
    this(ClassLoader.getSystemClassLoader().getResource("s1.csv").getFile());
  }

  public SSetsS1(final File file) {
    try(BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line = br.readLine();
      while (line != null) {
        String split[] = line.split(",");

        dataset.add(new Point2D(Double.parseDouble(split[0]), Double.parseDouble(split[1])));
        line = br.readLine();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Override
  public List<Point2D> load() {
    return dataset;
  }
}
