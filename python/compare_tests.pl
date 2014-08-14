#!/bin/env perl
use strict;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Spec;

# Globals --------------------------------------------------
my ($S_A, $S_B, $s_a, $s_b, $b_verbose);		# file arguments and resolved, & verbose mode flag
my (@a_a, @a_b);					# arrays of log contents
my ($s_linea, $s_lineb);				# strings of each log line
my $i_url;						# counter of tests, determined by line starting with 'URL'
my %h_urls;						# hash of test results

# Main -----------------------------------------------------
getargs();
open A, $s_a or die "Failed to read-open $s_a\n";
open B, $s_b or die "Failed to read-open $s_b\n";

check_log_lines();

# for each line of each log
while ($s_linea = shift @a_a) {
	$s_lineb = shift @a_b;

	if (($s_linea =~ m@^URL@) && ($s_lineb =~ m@^URL@)) {
		if ($i_url) {				# new test, print previous results
			printf "%4d)  A  %8d %12d %6s  B  %8d %12d\n", $i_url, $h_urls{a}{$i_url}{qtime}, $h_urls{a}{$i_url}{numfound}, ' ', $h_urls{b}{$i_url}{qtime}, $h_urls{b}{$i_url}{numfound};
		} else {				# first test, print column names
			printf "%4s) Log %8s %12s %6s Log %8s %12s\n", 'URL', 'QTime', 'numFound', ' ', 'QTime', 'numFound';
		}
		$i_url++;
	}
	elsif ($s_linea =~ m@QTime\s+(\d+)@) {		# collect test qtimes
		$h_urls{a}{$i_url}{qtime} = $1;
		$s_lineb =~ m@QTime\s+(\d+)@;
		$h_urls{b}{$i_url}{qtime} = $1;
	}
	elsif ($s_linea =~ m@numFound\s+(\d+)@) {	# collect test numfounds
		$h_urls{a}{$i_url}{numfound} = $1;
		$s_lineb =~ m@numFound\s+(\d+)@;
		$h_urls{b}{$i_url}{numfound} = $1;
	}

	if ($b_verbose) { print "$s_linea$s_lineb\n"; }	# print log lines 
}

close A or warn "Failed to read-close $s_a\n";
close B or warn "Failed to read-close $s_b\n";
exit 0;

# Subroutines ----------------------------------------------
sub getargs {
# Read script arguments, ensuring that resolved files exist
	my $s_error;
	GetOptions (
		'a=s' => \$S_A,
		'b=s' => \$S_B,
		'v' => \$b_verbose,
	);
	$s_a = File::Spec->rel2abs($S_A);
	if (-l $s_a) { $s_a = readlink $s_a; }
	if (-l $s_b) { $s_b = readlink $s_b; }
	$s_b = File::Spec->rel2abs($S_B);
	unless (-f $s_a) { $s_error .= "- Argument file a ($S_A) missing\n"; }
	unless (-f $s_b) { $s_error .= "- Argument file b ($S_B) missing\n"; }
	if ($s_error) {
		print "Usage: $PROGRAM_NAME -a (log) -b (log)\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($b_verbose) { print "Log a: $s_a\nLog b: $s_b\n"; }
}

sub check_log_lines {
# Check that logs have same number of lines; if not, logs probably not comparable
	@a_a = <A>;
	@a_b = <B>;
	unless ((scalar @a_a) == (scalar @a_b)) {
		print "Number of log lines differ - a=".scalar @a_a." b=".scalar @a_b."\n";
		print "Not probable that log line data will be matching\n";
		exit 1;
	}
}
