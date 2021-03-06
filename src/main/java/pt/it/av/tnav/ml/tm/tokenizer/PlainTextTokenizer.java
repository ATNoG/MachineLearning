package pt.it.av.tnav.ml.tm.tokenizer;

import pt.it.av.tnav.ml.tm.ngrams.NGram;

import java.lang.ref.WeakReference;
import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a tokenizer for plain text.
 *
 * @author <a href="mailto:mariolpantunes@gmail.com">Mário Antunes</a>
 * @version 1.0
 */
public class PlainTextTokenizer implements Tokenizer {
  private static WeakReference<Tokenizer> wrt = null;
  private final Locale locale;
  private final Pattern norm, text;

  /**
   * @param locale
   */
  public PlainTextTokenizer(Locale locale) {
    this.locale = locale;
    norm = Pattern.compile("[\\p{InCombiningDiacriticalMarks}]");
    text = Pattern.compile("('s|[^a-zA-Z-]+)");
  }

  /**
   *
   */
  public PlainTextTokenizer() {
    this(Locale.getDefault());
  }

  /**
   * @param input
   * @return
   */
  private String normalize(String input) {
    String lowerCase = input.toLowerCase(locale);
    String normalize = Normalizer.normalize(lowerCase, Normalizer.Form.NFD);
    Matcher matcher = norm.matcher(normalize);
    StringBuffer sb = new StringBuffer();
    while (matcher.find())
      matcher.appendReplacement(sb, "");
    matcher.appendTail(sb);
    return sb.toString();
  }

  @Override
  public Iterator<String> tokenizeIt(String input) {
    return new TextTokenizerIteratorString(normalize(input));
  }

  /**
   * @param input
   * @param n
   * @return
   */
  public Iterator<NGram> tokenizeIt(String input, int n) {
    return new TextTokenizerIteratorNGram(normalize(input), n);
  }

  @Override
  public List<String> tokenize(String input) {
    Iterator<String> it = this.tokenizeIt(input);
    List<String> rv = new ArrayList<>();
    while (it.hasNext())
      rv.add(it.next());
    return rv;
  }

  /**
   * @param input
   * @param n
   * @return
   */
  public List<NGram> tokenize(String input, int n) {
    Iterator<NGram> it = this.tokenizeIt(input, n);
    List<NGram> rv = new ArrayList<>();
    while (it.hasNext())
      rv.add(it.next());
    return rv;
  }

  /**
   *
   */
  private class TextTokenizerIteratorString implements Iterator<String> {
    private final String input;
    private final IteratorParameters p;
    private boolean hasNext = true;

    public TextTokenizerIteratorString(String input) {
      this.input = input;
      p = new IteratorParameters(BreakIterator.getWordInstance(locale), input);
      findNext(input, p);
      if (p.token == null)
        hasNext = false;
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public String next() {
      String rv = p.token;
      findNext(input, p);
      if (p.token == null)
        hasNext = false;
      return rv;
    }
  }

  /**
   *
   */
  private class TextTokenizerIteratorNGram implements Iterator<NGram> {
    private final String input;
    private final IteratorParameters p;
    private final List<String> buffer;
    private boolean hasNext = true;
    private int idx = 0, n;

    public TextTokenizerIteratorNGram(String input, int n) {
      this.input = input;
      this.n = n;
      buffer = new ArrayList<>(n);
      p = new IteratorParameters(BreakIterator.getWordInstance(locale), input);
      for (int i = 0; i < n && hasNext; i++) {
        findNext(input, p);
        if (p.token != null)
          buffer.add(p.token);
        else
          hasNext = false;
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public NGram next() {
      NGram rv = null;
      if (idx < n) {
        rv = new NGram(buffer.subList(0, idx + 1));
        idx++;
      } else {
        idx = 0;
        findNext(input, p);
        if (p.token != null) {
          buffer.remove(0);
          buffer.add(p.token);
          rv = new NGram(buffer.subList(0, idx + 1));
          idx++;
        } else
          hasNext = false;
      }
      return rv;
    }
  }

  /**
   * @param input
   * @param p
   */
  private void findNext(String input, IteratorParameters p) {
    p.token = null;
    boolean done = false;

    while (p.end != BreakIterator.DONE && !done) {
      String candidate = input.substring(p.start, p.end);
      Matcher matcher = text.matcher(candidate);

      StringBuffer sb = new StringBuffer();
      while (matcher.find())
        matcher.appendReplacement(sb, "");
      matcher.appendTail(sb);

      p.start = p.end;
      p.end = p.it.next();
      if (sb.length() > 0) {
        done = true;
        p.token = sb.toString();
      }
    }
  }

  /**
   *
   */
  private class IteratorParameters {
    protected BreakIterator it;
    protected int start, end;
    protected String token = null;

    public IteratorParameters(BreakIterator it, String input) {
      this.it = it;
      it.setText(input);
      start = it.first();
      end = it.next();
    }
  }

  /**
   * Builds a static {@link WeakReference} to a {@link Tokenizer} class.
   * <p>
   *   This method should be used whenever the {@link Tokenizer} will be built and destroy multiple times.
   *   It will also share a single stemmer through several process/threads.
   * </p>
   *
   * @return {@link Tokenizer} reference that points to a {@link PlainTextTokenizer}
   */
  public synchronized static Tokenizer build() {
    Tokenizer rv = null;
    if (wrt == null) {
      rv = new PlainTextTokenizer();
      wrt = new WeakReference<>(rv);
    } else {
      rv = wrt.get();
      if(rv == null) {
        rv = new PlainTextTokenizer();
        wrt = new WeakReference<>(rv);
      }
    }
    return rv;
  }
}
