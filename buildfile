require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/single_intermediate_layout'
require 'buildr/gwt'
require 'buildr/jacoco'

PROVIDED_DEPS = [:javax_jsr305, :anodoc]
TEST_DEPS = [:guiceyloops]
GWT_DEPS =
  [
    :gwt_user,
    :elemental2_core,
    :elemental2_dom,
    :elemental2_promise,
    :jsinterop_base,
    :jsinterop_base_sources,
    :jsinterop_annotations,
    :jsinterop_annotations_sources
  ]
DAGGER_DEPS =
  [
    :javax_inject,
    :dagger_core,
    :dagger_producers,
    :dagger_compiler,
    :googlejavaformat,
    :errorprone
  ]

GWT_EXAMPLES=%w(IdleStatusExample BrowserLocationExample NetworkStatusExample ObservablePromiseExample TimedDisposerExample IntervalTickerExample).collect {|c| "org.realityforge.arez.gwt.examples.#{c}"}
DOC_EXAMPLES=%w().collect {|c| "org.realityforge.arez.doc.examples.#{c}"}

# JDK options passed to test environment. Essentially turns assertions on.
AREZ_TEST_OPTIONS =
  {
    'braincheck.environment' => 'development',
    'arez.environment' => 'development'
  }

desc 'Arez: Simple, Scalable State Management Library'
define 'arez' do
  project.group = 'org.realityforge.arez'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('arez/arez')
  pom.add_developer('realityforge', 'Peter Donald')

  desc 'Arez Core'
  define 'core' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with PROVIDED_DEPS,
                 :braincheck

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)

    test.using :testng
    test.compile.with TEST_DEPS
  end

  desc 'Arez Annotations'
  define 'annotations' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with PROVIDED_DEPS

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)

    # This dependency is added to make it easy to cross reference
    # core classes in javadocs but code should not make use of it.
    iml.main_dependencies << project('core').package(:jar)
  end

  desc 'Arez Component Support'
  define 'component' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with PROVIDED_DEPS

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)

    test.using :testng
    test.compile.with TEST_DEPS
  end

  desc 'Arez Extras and Addons'
  define 'extras' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with project('annotations').package(:jar, :classifier => :gwt),
                 project('annotations').compile.dependencies,
                 project('core').package(:jar, :classifier => :gwt),
                 project('core').compile.dependencies,
                 project('component').package(:jar, :classifier => :gwt),
                 project('component').compile.dependencies,
                 project('processor').package(:jar),
                 project('processor').compile.dependencies

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)

    test.using :testng
    test.compile.with TEST_DEPS

    # The generators are configured to generate to here.
    iml.main_source_directories << _('generated/processors/main/java')
  end

  desc 'Arez Browser based Extras and Addons'
  define 'browser-extras' do
    pom.provided_dependencies.concat PROVIDED_DEPS + [:jetbrains_annotations]

    compile.with project('extras').package(:jar, :classifier => :gwt),
                 project('extras').compile.dependencies,
                 :jetbrains_annotations,
                 GWT_DEPS

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    package(:jar)
    package(:sources)
    package(:javadoc)

    test.using :testng
    test.compile.with TEST_DEPS

    gwt_enhance(project)

    # The generators are configured to generate to here.
    iml.main_source_directories << _('generated/processors/main/java')

    project.jacoco.enabled = false
  end

  desc 'Arez Annotation processor'
  define 'processor' do
    pom.provided_dependencies.concat [:javax_jsr305]

    compile.with :javax_jsr305,
                 :autoservice,
                 :autocommon,
                 :javapoet,
                 :guava

    test.with :compile_testing,
              Java.tools_jar,
              :truth,
              DAGGER_DEPS,
              project('annotations'),
              project('core').package(:jar),
              project('core').compile.dependencies,
              project('component').package(:jar, :classifier => :gwt),
              project('component').compile.dependencies

    package(:jar)
    package(:sources)
    package(:javadoc)

    package(:jar).enhance do |jar|
      jar.merge(artifact(:javapoet))
      jar.merge(artifact(:guava))
      jar.enhance do |f|
        shaded_jar = (f.to_s + '-shaded')
        Buildr.ant 'shade_jar' do |ant|
          artifact = Buildr.artifact(:shade_task)
          artifact.invoke
          ant.taskdef :name => 'shade', :classname => 'org.realityforge.ant.shade.Shade', :classpath => artifact.to_s
          ant.shade :jar => f.to_s, :uberJar => shaded_jar do
            ant.relocation :pattern => 'com.squareup.javapoet', :shadedPattern => 'org.realityforge.arez.processor.vendor.javapoet'
            ant.relocation :pattern => 'com.google', :shadedPattern => 'org.realityforge.arez.processor.vendor.google'
          end
        end
        FileUtils.mv shaded_jar, f.to_s
      end
    end

    test.using :testng
    test.options[:properties] = { 'arez.fixture_dir' => _('src/test/resources') }
    test.compile.with TEST_DEPS

    iml.test_source_directories << _('src/test/resources/input')
    iml.test_source_directories << _('src/test/resources/expected')
    iml.test_source_directories << _('src/test/resources/bad_input')
  end

  desc 'Arez Integration Tests'
  define 'integration-tests' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    test.options[:properties] = AREZ_TEST_OPTIONS.merge('arez.integration_fixture_dir' => _('src/test/resources'))
    test.options[:java_args] = ['-ea']

    test.using :testng
    test.compile.with TEST_DEPS,
                      :javax_json,
                      :jsonassert,
                      :android_json,
                      DAGGER_DEPS,
                      project('annotations').package(:jar),
                      project('annotations').compile.dependencies,
                      project('core').package(:jar),
                      project('core').compile.dependencies,
                      project('component').package(:jar, :classifier => :gwt),
                      project('component').compile.dependencies,
                      project('extras').package(:jar),
                      project('extras').compile.dependencies,
                      project('processor').package(:jar),
                      project('processor').compile.dependencies

    # The generators are configured to generate to here.
    iml.test_source_directories << _('generated/processors/test/java')
  end

  desc 'Arez GWT Examples'
  define 'gwt-example' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with project('browser-extras').package(:jar, :classifier => :gwt),
                 project('browser-extras').compile.dependencies

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    test.using :testng
    test.compile.with TEST_DEPS

    gwt_modules = {}
    GWT_EXAMPLES.each do |gwt_module|
      gwt_modules[gwt_module] = false
    end
    iml.add_gwt_facet(gwt_modules, :settings => { :compilerMaxHeapSize => '1024' }, :gwt_dev_artifact => :gwt_dev)
    project.jacoco.enabled = false
  end

  desc 'Arez Examples used in documentation'
  define 'doc-examples' do
    pom.provided_dependencies.concat PROVIDED_DEPS

    compile.with project('browser-extras').package(:jar, :classifier => :gwt),
                 project('browser-extras').compile.dependencies

    test.options[:properties] = AREZ_TEST_OPTIONS
    test.options[:java_args] = ['-ea']

    test.using :testng
    test.compile.with TEST_DEPS

    gwt_modules = {}
    DOC_EXAMPLES.each do |gwt_module|
      gwt_modules[gwt_module] = false
    end
    iml.add_gwt_facet(gwt_modules, :settings => { :compilerMaxHeapSize => '1024' }, :gwt_dev_artifact => :gwt_dev)

    # The generators are configured to generate to here.
    iml.main_source_directories << _('generated/processors/main/java')
    project.jacoco.enabled = false
  end

  doc.from(projects(%w(annotations core processor extras browser-extras))).
    using(:javadoc,
          :windowtitle => 'Arez API Documentation',
          :linksource => true,
          :link => %w(https://docs.oracle.com/javase/8/docs/api http://www.gwtproject.org/javadoc/latest/),
          :group => {
            'Core Packages' => 'org.realityforge.arez:org.realityforge.arez.spy*',
            'Annotation Packages' => 'org.realityforge.arez.annotations*:org.realityforge.arez.processor*',
            'Component Packages' => 'org.realityforge.arez.component*',
            'Extras Packages' => 'org.realityforge.arez.extras*',
            'Browser Extras Packages' => 'org.realityforge.arez.browser*'
          }
    )

  iml.excluded_directories << project._('node_modules')
  iml.excluded_directories << project._('tmp/gwt')
  iml.excluded_directories << project._('tmp')

  ipr.add_default_testng_configuration(:jvm_args => '-ea -Dbraincheck.environment=development -Darez.environment=development -Darez.output_fixture_data=false -Darez.fixture_dir=processor/src/test/resources -Darez.integration_fixture_dir=integration-tests/src/test/resources')
  ipr.add_component_from_artifact(:idea_codestyle)
  ipr.extra_modules << '../mobx-docs/mobx-docs.iml'
  ipr.extra_modules << '../mobx-react-devtools/mobx-react-devtools.iml'
  ipr.extra_modules << '../andykog-mobx-devtools/andykog-mobx-devtools.iml'

  GWT_EXAMPLES.each do |gwt_module|
    short_name = gwt_module.gsub(/.*\./, '')
    ipr.add_gwt_configuration(project('gwt-example'),
                              :name => "GWT Example: #{short_name}",
                              :gwt_module => gwt_module,
                              :start_javascript_debugger => false,
                              :vm_parameters => "-Xmx3G -Djava.io.tmpdir=#{_("tmp/gwt/#{short_name}")}",
                              :shell_parameters => "-port 8888 -codeServerPort 8889 -bindAddress 0.0.0.0 -war #{_(:generated, 'gwt-export', short_name)}/")
  end
  DOC_EXAMPLES.each do |gwt_module|
    short_name = gwt_module.gsub(/.*\./, '')
    ipr.add_gwt_configuration(project('doc-examples'),
                              :name => "Doc Example: #{short_name}",
                              :gwt_module => gwt_module,
                              :start_javascript_debugger => false,
                              :vm_parameters => "-Xmx3G -Djava.io.tmpdir=#{_("tmp/gwt/#{short_name}")}",
                              :shell_parameters => "-port 8888 -codeServerPort 8889 -bindAddress 0.0.0.0 -war #{_(:generated, 'gwt-export', short_name)}/")
  end

  ipr.add_component('CompilerConfiguration') do |component|
    component.annotationProcessing do |xml|
      xml.profile(:default => true, :name => 'Default', :enabled => true) do
        xml.sourceOutputDir :name => 'generated/processors/main/java'
        xml.sourceTestOutputDir :name => 'generated/processors/test/java'
        xml.outputRelativeToContentRoot :value => true
      end
    end
  end
end

Buildr.projects.each do |project|
  unless project.name == 'arez'
    project.doc.options.merge!('Xdoclint:all,-reference' => true)
  end
end
