package arez.downstream;

import gir.Gir;
import gir.GirException;
import gir.delta.Patch;
import gir.git.Git;
import gir.io.FileUtil;
import gir.ruby.Buildr;
import gir.ruby.Ruby;
import gir.sys.SystemProperty;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public final class CollectBuildStats
{
  public static void main( final String[] args )
    throws Exception
  {
    Gir.go( () -> {
      final boolean storeStatistics =
        System.getProperty( "arez.deploy_test.store_statistics", "false" ).equals( "true" );
      final String version = SystemProperty.get( "arez.version" );
      final OrderedProperties overallStatistics = new OrderedProperties();
      final Path workingDirectory =
        Paths.get( SystemProperty.get( "arez.deploy_test.work_dir" ) ).toAbsolutePath().normalize();
      final Path fixtureDirectory =
        Paths.get( SystemProperty.get( "arez.deploy_test.fixture_dir" ) ).toAbsolutePath().normalize();

      final Path path = fixtureDirectory.resolve( "statistics.properties" );
      final OrderedProperties fixtureStatistics = new OrderedProperties();
      fixtureStatistics.load( Files.newBufferedReader( path ) );

      final String localRepositoryUrl = SystemProperty.get( "arez.deploy_test.local_repository_url" );

      if ( !workingDirectory.toFile().exists() )
      {
        Gir.messenger().info( "Working directory does not exist." );
        Gir.messenger().info( "Creating directory: " + workingDirectory );
        if ( !workingDirectory.toFile().mkdirs() )
        {
          Gir.messenger().error( "Failed to create working directory: " + workingDirectory );
        }
      }

      FileUtil.inDirectory( workingDirectory, () -> {
        Gir.messenger().info( "Cloning react4j-todomvc into " + workingDirectory );
        Git.clone( "https://github.com/react4j/react4j-todomvc.git", "react4j-todomvc" );
        final Path appDirectory = workingDirectory.resolve( "react4j-todomvc" );
        FileUtil.inDirectory( appDirectory, () -> {
          Git.fetch();
          Git.resetBranch();
          Git.checkout();
          Git.pull();
          Git.deleteLocalBranches();
          Stream.of( "raw", "arez", "dagger" ).forEach( branch -> {
            Gir.messenger().info( "Processing branch " + branch + "." );

            Git.checkout( branch );
            Git.pull();
            Git.clean();
            final String newBranch = branch + "-ArezUpgrade-" + version;
            System.out.println( "Checking out " + newBranch );
            Git.checkout( newBranch, true );
            if ( Git.remoteTrackingBranches().contains( "origin/" + newBranch ) )
            {
              Git.pull();
            }
            Git.clean();

            Gir.messenger().info( "Building branch " + branch + " prior to modifications." );
            boolean initialBuildSuccess = false;
            try
            {
              final String prefix = branch + ".before";
              final Path archiveDir = workingDirectory.resolve( "archive" ).resolve( prefix );
              buildAndRecordStatistics( archiveDir );
              loadStatistics( overallStatistics, archiveDir, prefix );
              loadStatistics( fixtureStatistics, archiveDir, version + "." + branch );
              initialBuildSuccess = true;
            }
            catch ( final GirException | IOException e )
            {
              Gir.messenger().info( "Failed to build branch '" + branch + "' before modifications.", e );
            }

            Git.clean();

            if ( Buildr.patchBuildYmlDependency( appDirectory, "org.realityforge.arez", version ) )
            {
              Gir.messenger().info( "Building branch " + branch + " after modifications." );
              try
              {
                final String content = "repositories.remote << '" + localRepositoryUrl + "'\n";
                Files.write( appDirectory.resolve( "_buildr.rb" ), content.getBytes() );
              }
              catch ( final IOException ioe )
              {
                Gir.messenger().error( "Failed to emit _buildr.rb configuration file.", ioe );
              }

              try
              {
                final String prefix = branch + ".after";
                final Path archiveDir = workingDirectory.resolve( "archive" ).resolve( prefix );
                buildAndRecordStatistics( archiveDir );
                loadStatistics( overallStatistics, archiveDir, prefix );
                loadStatistics( fixtureStatistics, archiveDir, version + "." + branch );
              }
              catch ( final GirException | IOException e )
              {
                if ( !initialBuildSuccess )
                {
                  Gir.messenger().error( "Failed to build branch '" + branch + "' before modifications " +
                                         "but branch also failed prior to modifications.", e );
                }
                else
                {
                  Gir.messenger().error( "Failed to build branch '" + branch + "' after modifications.", e );
                }
              }
            }
            else
            {
              Gir.messenger().info( "Branch " + branch + " not rebuilt as no modifications made." );
            }
          } );
        } );
      } );

      overallStatistics.keySet().forEach( k -> System.out.println( k + ": " + overallStatistics.get( k ) ) );
      final Path statisticsFile = workingDirectory.resolve( "statistics.properties" );
      Gir.messenger().info( "Writing overall build statistics to " + statisticsFile + "." );
      writeProperties( statisticsFile, overallStatistics );

      if ( storeStatistics )
      {
        Gir.messenger().info( "Updating fixture build statistics at" + path + "." );
        writeProperties( path, fixtureStatistics );
      }
    } );
  }

  private static void loadStatistics( @Nonnull final OrderedProperties statistics,
                                      @Nonnull final Path archiveDir,
                                      @Nonnull final String keyPrefix )
    throws IOException
  {
    final Properties properties = new Properties();
    properties.load( Files.newBufferedReader( archiveDir.resolve( "statistics.properties" ) ) );
    properties.forEach( ( key, value ) -> statistics.put( keyPrefix + "." + key, value ) );
  }

  private static void writeProperties( @Nonnull final Path outputFile, @Nonnull final OrderedProperties properties )
  {
    try
    {
      properties.store( new FileWriter( outputFile.toFile() ), "" );
      Patch.file( outputFile, c -> c.replaceAll( "\\#.*\n", "" ) );
    }
    catch ( final IOException ioe )
    {
      final String message = "Failed to write properties file: " + outputFile;
      Gir.messenger().error( message, ioe );
      throw new GirException( message, ioe );
    }
  }

  private static void buildAndRecordStatistics( @Nonnull final Path archiveDir )
  {
    if ( !archiveDir.toFile().mkdirs() )
    {
      final String message = "Error creating archive directory: " + archiveDir;
      Gir.messenger().error( message );
    }

    // Perform the build
    Ruby.buildr( "clean", "package", "EXCLUDE_GWT_DEV_MODULE=true", "GWT=react4j-todomvc" );

    archiveOutput( archiveDir );
    archiveStatistics( archiveDir );
  }

  private static void archiveStatistics( @Nonnull final Path archiveDir )
  {
    final OrderedProperties properties = new OrderedProperties();
    properties.setProperty( "todomvc.size", String.valueOf( getTodoMvcSize() ) );

    final Path statisticsFile = archiveDir.resolve( "statistics.properties" );
    Gir.messenger().info( "Archiving statistics to " + statisticsFile + "." );
    writeProperties( statisticsFile, properties );
  }

  private static void archiveOutput( @Nonnull final Path archiveDir )
  {
    archiveDirectory( FileUtil.getCurrentDirectory().resolve( "target/assets" ), archiveDir.resolve( "assets" ) );
  }

  private static void archiveDirectory( @Nonnull final Path assetsDir, @Nonnull final Path targetDir )
  {
    Gir.messenger().info( "Archiving output " + assetsDir + " to " + targetDir );
    try
    {
      FileUtil.copyDirectory( assetsDir, targetDir );
    }
    catch ( final GirException e )
    {
      final String message = "Failed to archive directory: " + assetsDir;
      Gir.messenger().error( message, e );
    }
  }

  private static long getTodoMvcSize()
  {
    final Path outputJsFile = FileUtil.getCurrentDirectory()
      .resolve( "target/generated/gwt/react4j.todomvc.TodomvcProd/todomvc/todomvc.nocache.js" );
    final File jsFile = outputJsFile.toFile();
    assert jsFile.exists();
    return jsFile.length();
  }
}