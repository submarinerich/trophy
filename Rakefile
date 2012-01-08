BIGGIE_SERVER="50.16.245.7"
EC2_KEY="~/.ec2/ftv.pem"
PRODUCTION_WAR="trophyservice-1.0.war"
CONNECTION="trophyConnection"
PORT="8085"

desc "run the app"
task :run do
  sh "mvn jetty:run -Djetty.port="+PORT
end

desc "clean up"
task :clean do
  sh "rm -rf deployment/"
  sh "rm -rf target/"
end

desc "test cases"
task :test do
    sh "mvn test"
end

namespace :package do
  desc "package for production"
  task :production do
    TO_ADD="<Set name=\"war\"><SystemProperty name=\"jetty.home\" default=\".\"/>/webapps/"+PRODUCTION_WAR+"</Set><Set name=\"connectorNames\"><Array type=\"String\"><Item>"+CONNECTION+"</Item></Array></Set></Configure>"
    JETTY_WEB="src/main/webapp/WEB-INF/jetty-web.xml"
    sh "cp "+JETTY_WEB+" tmp.xml"
    sh "cat tmp.xml | sed \'s#</Configure># #' > "+JETTY_WEB
    File.open(JETTY_WEB, 'a') {|f| f.write(TO_ADD) }
    sh "mvn package"
    sh "mv tmp.xml "+JETTY_WEB
    sh "mkdir -p deployment"
    sh "cp target/"+PRODUCTION_WAR+" deployment/"
    puts "the war file is located in deployment/"+PRODUCTION_WAR+"\n"
  end
end

namespace :deploy do
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
    sh "scp -i "+EC2_KEY+" deployment/"+PRODUCTION_WAR+" ubuntu@"+BIGGIE_SERVER+":~/newapps/"
  end
  desc "reload in jetty"
  task :reload do
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" sudo cp /home/ubuntu/newapps/"+PRODUCTION_WAR+" /usr/share/jetty/webapps/"
    sh "ssh -i "+EC2_KEY+" ubuntu@"+BIGGIE_SERVER+" sudo /etc/init.d/jetty restart"
  end

  desc "do all deploy tasks and flush server"
  task :flush do
    Rake::Task['clean'].invoke
    Rake::Task['package'].invoke
    Rake::Task['deploy:clean'].invoke
    Rake::Task['deploy:dir'].invoke
    Rake::Task['deploy:upload'].invoke
    Rake::Task['deploy:reload'].invoke
  end
end

desc "package for the server (Production)"
task :package do
  Rake::Task['package:production'].invoke
end
