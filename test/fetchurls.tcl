set urls [list]

foreach file $argv {
    foreach url [split [read [open $file r]]]  {
        set url [string trim $url]
        if {$url == ""} {continue;}
        lappend  urls  $url
    }
}


set start [clock milliseconds]
set cnt 0
set cntInterval 0
set errors 0
for {set i 0} {$i <5} {incr i} {
    foreach url $urls {
        set url [string trim $url]
        if {$url == ""} {continue;}
        incr cnt
        if {[expr ($cnt%5) == 0]} {
            set end [clock milliseconds]
            set delta [expr $end-$start]
            set minutes [expr ${delta}/1000.0/60.0]
            if {$minutes>0} {
                puts "#$cnt urls [format {%.1f} [expr ($end-$start)/1000.0]] seconds [expr int($cnt/$minutes)] urls/minute "
            }
        }
        if {[catch {exec curl -silent -f test.out $url} err]} {
            puts "Error: $err"
            puts "$url"
            incr errors
            if {$errors>10} {
                puts "Too many errors"
                exit;
            }
        }
    }
}

if {$cnt == 0} {
    puts "None read"
} else {
    set end [clock milliseconds]
    set delta [expr $end-$start]
    set minutes [expr $delta/1000/60.0]
    puts "$cnt urls in [format {%.1f} [expr $delta/1000.0]] seconds [expr int($cnt/$minutes)] urls/minute "
}