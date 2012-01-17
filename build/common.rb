
xmlfiles = ["pom.xml","example.pom"]

require 'rexml/document'

def projectInfo()
  wholepom = ""
  file = File.new("pom.xml", "r")
  while (line = file.gets)
    wholepom += line
  end
  file.close
  doc = REXML::Document.new(wholepom)
  r = []
  doc.elements.each('project/version') do |ele|
    r.push(ele.text)
  end
  doc.elements.each('project/properties/scala.version') do |ele|
    r.push(ele.text)
  end
  doc.elements.each('project/artifactId') do |ele|
    r.push(ele.text)
  end
  doc.elements.each('project/groupId') do |ele|
    r.push(ele.text)
  end
  doc.elements.each('project/packaging') do |ele|
    r.push(ele.text)
  end
  return r 
end


desc "clean everything"
task :clean do
  sh "rm -rf target/"
  sh "rm -rf deployment/" 
end


desc "xmllint the pom files"
task :tidy do
  xmlfiles.each do | xml | 
    if File.exists? xml
      sh "xmllint --format "+xml+" > temp.xml"
      sh "mv temp.xml "+xml
    end
  end  
end

desc "run scalatest tests"
task :test do
  sh "mvn test"
end

