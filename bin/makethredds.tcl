
file delete -force thredds
file mkdir thredds
cd thredds
puts "Unjarring thredds.war"
exec jar -xvf ../thredds.war


cd WEB-INF/classes
if {1} {
##exec rm -r org/apache/log4j
foreach jar [glob ../lib/*.jar] {
    if {[regexp jfree $jar]} {
        puts "skipping $jar"
        continue
    }
    if {[regexp unidatacommon $jar]} {
        puts "skipping $jar"
        continue
    }
    if {[regexp netcdf $jar]} {
        puts "skipping $jar"
        continue
    }

    if {[regexp slf4j $jar]} {
        puts "skipping $jar"
        continue
    }
    puts "Unjarring $jar"
    exec jar -xvf $jar
}

##unjar the common jar
puts "Unjarring unidatacommon.jar"
exec jar -xvf ../../../unidatacommon.jar

puts "Unjarring ncIdv.jar"
exec jar -xvf ../../../ncIdv.jar

puts "Making ramaddatds.jar"
puts "pwd: [pwd]"
puts "exec: [exec pwd]"
set files ""
foreach file [glob *] {
    if {$file=="visad"} continue
    append files " "
    append files "\{[file tail $file]\}"
#    puts "$file"
}
}


file delete -force META-INF/MANIFEST.MF

cd ../../META-INF
file delete -force MANIFEST.MF

cd ../WEB-INF/classes
puts [exec pwd]


set execLine "jar -Mcvf ../../../ramaddatds.jar $files"
eval exec $execLine


