package arez.downstream;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.intellij.lang.annotations.RegExp;
import static org.testng.Assert.*;

/**
 * Index into which symbolMap can be loadded and assertions invoked on contents of index.
 */
@SuppressWarnings( "WeakerAccess" )
final class SymbolEntryIndex
{
  private final HashMap<String, ArrayList<SymbolEntry>> _classNameToEntry = new HashMap<>();

  /**
   * Read and build an index from symbolMap file.
   *
   * @param symbolMapPath the path to load symbolMap from.
   * @return the new index.
   * @throws IOException    if there is an error reading file.
   * @throws ParseException if there is an error parsing file.
   */
  @Nonnull
  static SymbolEntryIndex readSymbolMapIntoIndex( @Nonnull final Path symbolMapPath )
    throws IOException, ParseException
  {
    final SymbolEntryIndex index = new SymbolEntryIndex();
    SymbolEntry.readSymbolMap( symbolMapPath, index::addEntry );
    return index;
  }

  private void addEntry( @Nonnull final SymbolEntry entry )
  {
    _classNameToEntry
      .computeIfAbsent( entry.getClassName(), e -> new ArrayList<>() )
      .add( entry );
  }

  /**
   * Verify no entry for ClassName exists in index.
   * If a match is found then fail assertion error.
   *
   * @param pattern the pattern
   */
  void assertNoClassNameMatches( @RegExp( prefix = "^", suffix = "$" ) @Nonnull final String pattern )
  {
    assertNoClassNameMatches( Pattern.compile( "^" + pattern + "$" ) );
  }

  /**
   * Verify no entry for ClassName exists in index.
   * If a match is found then fail assertion error.
   *
   * @param pattern the pattern
   */
  void assertNoClassNameMatches( @Nonnull final Pattern pattern )
  {
    final List<String> matches =
      _classNameToEntry.keySet().stream().filter( n -> pattern.matcher( n ).matches() ).collect( Collectors.toList() );
    if ( !matches.isEmpty() )
    {
      fail( "Expected that the SymbolMap would have no classNames that match pattern " + pattern +
            " but the following classNames match: " + matches );
    }
  }

  /**
   * Verify no entry for member exists in index.
   * If a match is found then fail assertion error.
   *
   * @param classNamePattern  the pattern to match className.
   * @param memberNamePattern the pattern to match member name.
   */
  void assertNoMemberMatches( @RegExp( prefix = "^", suffix = "$" ) @Nonnull final String classNamePattern,
                              @RegExp( prefix = "^", suffix = "$" ) @Nonnull final String memberNamePattern )
  {
    assertNoMemberMatches( Pattern.compile( "^" + classNamePattern + "$" ),
                           Pattern.compile( "^" + memberNamePattern + "$" ) );
  }

  /**
   * Verify no entry for member exists in index.
   * If a match is found then fail assertion error.
   *
   * @param classNamePattern  the pattern to match className.
   * @param memberNamePattern the pattern to match member name.
   */
  void assertNoMemberMatches( @Nonnull final Pattern classNamePattern, @Nonnull final Pattern memberNamePattern )
  {
    final List<SymbolEntry> matches =
      _classNameToEntry
        .entrySet()
        .stream()
        .filter( e -> classNamePattern.matcher( e.getKey() ).matches() &&
                      e.getValue().stream().anyMatch( s -> memberNamePattern.matcher( s.getMemberName() ).matches() ) )
        .flatMap( e -> e.getValue().stream() )
        .collect( Collectors.toList() );
    if ( !matches.isEmpty() )
    {
      fail( "Expected that the SymbolMap would have no members that match: classNamePattern '" + classNamePattern +
            "', memberPattern '" + memberNamePattern + "' but the following entries match: " + matches );
    }
  }
}