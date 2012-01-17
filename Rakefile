BIGGIE_SERVER=ENV['BIGGIE_SERVER']
EC2_KEY="~/.ec2/ftv.pem"
CONNECTION="trophyConnection"
PORT="8085"

require 'build/common.rb'

desc "run the app"
task :run do
  sh "mvn jetty:run -Djetty.port="+PORT
end

p = projectInfo()
version = p[0]
scalaVersion = [1]
artifactId = p[2]
groupId = p[3]
packaging = p[4]

productionWar = artifactId+"-"+version+"."+packaging

desc "deploy to server"
task :deploy do
    Rake::Task['deploysteps:flush'].invoke
end
namespace :package do
  desc "package for production"
  task :production do
    TO_ADD="<Set name=\"war\"><SystemProperty name=\"jetty.home\" default=\".\"/>/webapps/"+productionWar+"</Set><Set name=\"connectorNames\"><Array type=\"String\"><Item>"+CONNECTION+"</Item></Array></Set></Configure>"
    JETTY_WEB="src/main/webapp/WEB-INF/jetty-web.xml"
    sh "cp "+JETTY_WEB+" tmp.xml"
    sh "cat tmp.xml | sed \'s#</Configure># #' > "+JETTY_WEB
    File.open(JETTY_WEB, 'a') {|f| f.write(TO_ADD) }
    sh "mvn package"
    sh "mv tmp.xml "+JETTY_WEB
    sh "mkdir -p deployment"
    sh "cp target/"+productionWar+" deployment/"
    puts "the war file is located in deployment/"+productionWar+"\n"
  end
end


namespace :deploysteps do
  desc "clean up after last install"
  task :clean do
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" rm -rf /home/ubuntu/newapps/"
  end
  desc "make a directory to deploy from"
  task :dir do
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" mkdir /home/ubuntu/newapps/"
  end
  desc "upload the war file"
  task :upload do
    sh "scp -i "+EC2_KEY+" deployment/"+productionWar+" ubuntu@"+BIGGIE_SERVER+":~/newapps/"
  end
  desc "reload in jetty"
  task :reload do
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" sudo cp /home/ubuntu/newapps/"+productionWar+" /usr/share/jetty/webapps/"
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" sudo /etc/init.d/jetty restart"
  end

  desc "do all deploy tasks and flush server"
  task :flush do
    Rake::Task['clean'].invoke
    Rake::Task['package'].invoke
    Rake::Task['deploysteps:clean'].invoke
    Rake::Task['deploysteps:dir'].invoke
    Rake::Task['deploysteps:upload'].invoke
    Rake::Task['deploysteps:reload'].invoke
  end
end

