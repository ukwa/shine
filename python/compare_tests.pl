#!/bin/env perl
use strict;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Spec;

# Globals --------------------------------------------------
my ($S_A, $S_B, $s_a, $s_b, $b_verbose);		# file arguments and resolved, & verbose mode flag
my ($S_C, $S_D, $S_E, $s_c, $s_d, $s_e);		# arguments and resolved for possible cmdline files, for aggregating
my (@a_a, @a_b, @a_c, @a_d, @a_e);			# arrays of log contents
my ($s_linea, $s_lineb, $s_linec, $s_lined, $s_linee);	# strings of each log line
my $i_url;						# counter of tests, determined by line starting with 'URL'
my %h_urls;						# hash of test results
my ($i_files, $i_a, $i_b, $i_same);			# counters of number of file arguments, and for better results

# Main -----------------------------------------------------
getargs();
open A, $s_a or die "Failed to read-open $s_a\n";
open B, $s_b or die "Failed to read-open $s_b\n";
if ($s_c) { open C, $s_c or die "Failed to read-open $s_c\n"; }
if ($s_d) { open D, $s_d or die "Failed to read-open $s_d\n"; }
if ($s_e) { open E, $s_e or die "Failed to read-open $s_e\n"; }

check_log_lines();

# for each line of each log
while ($s_linea = shift @a_a) {
	$s_lineb = shift @a_b;
	$s_linec = shift @a_c if $s_c;
	$s_lined = shift @a_d if $s_d;
	$s_linee = shift @a_e if $s_e;

	if (($s_linea =~ m@^URL@) && ($s_lineb =~ m@^URL@)) {
		if ($i_url) {				# new test, print previous results
			printf "%4d)  A  %8d %12d %6s  B  %8d %12d %6s", $i_url, $h_urls{a}{$i_url}{qtime}, $h_urls{a}{$i_url}{numfound}, ' '
				, $h_urls{b}{$i_url}{qtime}, $h_urls{b}{$i_url}{numfound}, ' ';

			if ($s_c) {
				if ($s_linec =~ m@^URL@) { printf "  C  %8d %12d %6s", $h_urls{c}{$i_url}{qtime}, $h_urls{c}{$i_url}{numfound}, ' '; }
				else { die "C line not URL\n"; }
			}
			if ($s_d) {
				if ($s_lined =~ m@^URL@) { printf "  D  %8d %12d %6s", $h_urls{d}{$i_url}{qtime}, $h_urls{d}{$i_url}{numfound}, ' '; }
				else { die "D line not URL\n"; }
			}
			if ($s_e) {
				if ($s_linee =~ m@^URL@) { printf "  E  %8d %12d %6s", $h_urls{e}{$i_url}{qtime}, $h_urls{e}{$i_url}{numfound}, ' '; }
				else { die "E line not URL\n"; }
			}
			printf "  %12d\n", $h_urls{avg}{$i_url}/$i_files;

		} else {				# first test, print column names
			printf "%4s) Log %8s %12s %6s Log %8s %12s %6s", 'URL', 'QTime', 'numFound', ' ', 'QTime', 'numFound', ' ';
			printf " Log %8s %12s %6s", 'QTime', 'numFound', ' ' if $s_c;
			printf " Log %8s %12s %6s", 'QTime', 'numFound', ' ' if $s_d;
			printf " Log %8s %12s %6s", 'QTime', 'numFound', ' ' if $s_e;
			printf "  %12s\n", 'Avg';
		}
		$i_url++;
	}
	elsif ($s_linea =~ m@QTime\s+(\d+)@) {		# collect test qtimes
		$h_urls{a}{$i_url}{qtime} = $1;
		$h_urls{avg}{$i_url} += $h_urls{a}{$i_url}{qtime};
		$s_lineb =~ m@QTime\s+(\d+)@; $h_urls{b}{$i_url}{qtime} = $1; $h_urls{avg}{$i_url} += $h_urls{b}{$i_url}{qtime};
		if ($s_c) { $s_linec =~ m@QTime\s+(\d+)@; $h_urls{c}{$i_url}{qtime} = $1; $h_urls{avg}{$i_url} += $h_urls{c}{$i_url}{qtime}; }
		if ($s_d) { $s_lined =~ m@QTime\s+(\d+)@; $h_urls{d}{$i_url}{qtime} = $1; $h_urls{avg}{$i_url} += $h_urls{d}{$i_url}{qtime}; }
		if ($s_e) { $s_linee =~ m@QTime\s+(\d+)@; $h_urls{e}{$i_url}{qtime} = $1; $h_urls{avg}{$i_url} += $h_urls{e}{$i_url}{qtime}; }

		if ($h_urls{a}{$i_url}{qtime} < $h_urls{b}{$i_url}{qtime}) { $i_a++; }
		elsif ($h_urls{a}{$i_url}{qtime} > $h_urls{b}{$i_url}{qtime}) { $i_b++; }
		else { $i_same++; }

		
	}
	elsif ($s_linea =~ m@numFound\s+(\d+)@) {	# collect test numfounds
		$h_urls{a}{$i_url}{numfound} = $1;
		$s_lineb =~ m@numFound\s+(\d+)@; $h_urls{b}{$i_url}{numfound} = $1;
		if ($s_c) { $s_linec =~ m@numFound\s+(\d+)@; $h_urls{c}{$i_url}{numfound} = $1; }
		if ($s_d) { $s_lined =~ m@numFound\s+(\d+)@; $h_urls{d}{$i_url}{numfound} = $1; }
		if ($s_e) { $s_linee =~ m@numFound\s+(\d+)@; $h_urls{e}{$i_url}{numfound} = $1; }
	}

	if ($b_verbose) {				# print log lines 
		print "$s_linea$s_lineb\n";
		print "$s_linec" if $s_c;
		print "$s_lined" if $s_d;
		print "$s_linee" if $s_e;
	}
}

printf "%4d)  A  %8d %12d %6s  B  %8d %12d %6s", $i_url, $h_urls{a}{$i_url}{qtime}, $h_urls{a}{$i_url}{numfound}, ' ', $h_urls{b}{$i_url}{qtime}, $h_urls{b}{$i_url}{numfound}, ' ';
if ($s_c) { printf "  C  %8d %12d %6s", $h_urls{c}{$i_url}{qtime}, $h_urls{c}{$i_url}{numfound}, ' '; }
if ($s_d) { printf "  D  %8d %12d %6s", $h_urls{d}{$i_url}{qtime}, $h_urls{d}{$i_url}{numfound}, ' '; }
if ($s_e) { printf "  E  %8d %12d %6s", $h_urls{e}{$i_url}{qtime}, $h_urls{e}{$i_url}{numfound}, ' '; }
printf "  %12d\n", $h_urls{avg}{$i_url}/$i_files;
#printf "%4d)  A  %8d %12d %6s  B  %8d %12d\n", $i_url, $h_urls{a}{$i_url}{qtime}, $h_urls{a}{$i_url}{numfound}, ' ', $h_urls{b}{$i_url}{qtime}, $h_urls{b}{$i_url}{numfound};
print "QTime better for a: $i_a\tFor b: $i_b".($i_same?"\tSame: $i_same":'')."\n";
close A or warn "Failed to read-close $s_a\n";
close B or warn "Failed to read-close $s_b\n";
if ($s_c) { close C or warn "Failed to read-close $s_c\n"; }
if ($s_d) { close D or warn "Failed to read-close $s_d\n"; }
if ($s_e) { close E or warn "Failed to read-close $s_e\n"; }
print "\n";
exit 0;

# Subroutines ----------------------------------------------
sub getargs {
# Read script arguments, ensuring that resolved files exist
	my $s_error;
	GetOptions (
		'a=s' => \$S_A,
		'b=s' => \$S_B,
		'c=s' => \$S_C,
		'd=s' => \$S_D,
		'e=s' => \$S_E,
		'v' => \$b_verbose,
	);
	$s_a = arg2int($S_A);
	$s_b = arg2int($S_B);
	if ($S_C) { $s_c = arg2int($S_C); }
	if ($S_D) { $s_d = arg2int($S_D); }
	if ($S_E) { $s_e = arg2int($S_E); }
	unless (-f $s_a) { $s_error .= "- Argument file a ($S_A) missing\n"; }
	unless (-f $s_b) { $s_error .= "- Argument file b ($S_B) missing\n"; }
	if ($s_error) {
		print "Usage: $PROGRAM_NAME -a (log) -b (log)\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($b_verbose) {
		print "Log a: $s_a\nLog b: $s_b\n";
		print "Log c: $s_c\n" if $s_c;
		print "Log d: $s_d\n" if $s_d;
		print "Log e: $s_e\n\n" if $s_e;
	}
}

sub check_log_lines {
	my $s_error;
# Check that logs have same number of lines; if not, logs probably not comparable
	@a_a = <A>;
	@a_b = <B>; $i_files=2;
	if ($s_c) { @a_c = <C>; $i_files=3; }
	if ($s_d) { @a_d = <D>; $i_files=4; }
	if ($s_e) { @a_e = <E>; $i_files=5; }
	unless ((scalar @a_a) == (scalar @a_b)) { $s_error = "Number of log lines differ - a=".scalar @a_a." b=".scalar @a_b."\n"; }
	if (($s_c) && (scalar @a_a) != (scalar @a_c)) { $s_error .= "Number of log lines differ - a=".scalar @a_a." c=".scalar @a_c."\n"; }
	if (($s_d) && (scalar @a_a) != (scalar @a_d)) { $s_error .= "Number of log lines differ - a=".scalar @a_a." d=".scalar @a_d."\n"; }
	if (($s_e) && (scalar @a_a) != (scalar @a_e)) { $s_error .= "Number of log lines differ - a=".scalar @a_a." e=".scalar @a_e."\n"; }
	if ($s_error) {
		print $s_error;
		print "Not probable that log line data will be matching\n";
		exit 1;
	}
}

# Functions ------------------------------------------------
sub arg2int {
# convert argument to full path
	my $s = File::Spec->rel2abs(shift);
	if (-l $s) { $s = readlink $s; }
	return $s;
}
