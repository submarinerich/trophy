desc "run the app"
task :run do
  sh "mvn jetty:run"
end


desc "install the maven scalatest plugin"
task :scalatestplugin do
  sh "git clone http://github.com/teigen/maven-scalatest-plugin.git"
  sh "cd maven-scalatest-plugin && mvn clean install"
  sh "rm -rf maven-scalatest-plugin"
end
