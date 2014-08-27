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

	print "$s_linea$s_lineb$s_linec$s_lined$s_linee\n";
}

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
