#!/bin/env perl
use strict;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Spec;
use Data::Dumper;

# Globals --------------------------------------------------
use constant DEBUG => 0;
my $S_INPUTFILE;
my $B_VERBOSE;

# Variables ------------------------------------------------
my ($s_inf, $s_outf);
my %h_raw;

# Main -----------------------------------------------------
getargs();
read_data();
save_data();
exit 0;

# Subroutines ----------------------------------------------
sub getargs {
	my $s_error;
	GetOptions (
		'inputfile=s' => \$S_INPUTFILE,
		'verbose' => \$B_VERBOSE,
	);

	unless ($S_INPUTFILE) { $s_error .= "Input file missing\n"; }

	$s_inf = File::Spec->rel2abs($S_INPUTFILE);
	unless (-f $s_inf) { $s_error .= "Input file [$s_inf] missing\n"; }

	$s_outf = $s_inf.'.graph_data';

	if ($s_error) {
		print "Usage: $PROGRAM_NAME --inputfile --outputfile\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($B_VERBOSE) {
		print "Input file:\t$s_inf\n";
		print "Output file:\t$s_outf\n";
	}
}

sub read_data {
	my ($i_low, $i_high);

	open IN, $s_inf or die "Failed to read-open [$s_inf]: $!\n";
	foreach my $s_line (<IN>) {
		# Skip non-results lines
		next unless $s_line =~ m@QTime\.\[ms\]@;

		# Format of results: ALL-FACETS-chervil.QTime.[ms] 8806 numFound 45997 wallclock.[ms] 8811.698
 		$s_line =~ m@QTime\.\[ms\] (\d+) numFound (\d+)@;
		my $i_qt = $1;
		my $i_nf = $2;

		# Determine magnitude of nf; store magnitude > QTime
		my $i_mag = length $i_nf;
		$h_raw{$i_mag}{$i_qt}++;
	}
	close IN or die "Failed to read-close [$s_inf]: $!\n";
}

sub save_data {
	open OUT, '>', $s_outf or die "Failed to write-open [$s_outf]: $!\n";

	my %h_data;
	# Traverse raw magnitude, numFound, QTime
	foreach my $i_mag (sort {$a <=> $b} keys %h_raw) {

		my ($i_highest, $i_num);
		# Identify lowest and highest QTime values in magnitude, to calculate 95% & quartiles
		foreach my $i_qt (sort {$a <=> $b} keys %{$h_raw{$i_mag}}) {
			if (DEBUG) { print "\t10^$i_mag\t$i_qt\n"; }

			# Store lowest value
			if (! defined $h_data{$i_mag}{low}) {
				$h_data{$i_mag}{low} = $i_qt;
			} elsif ($h_data{$i_mag}{low} > $i_qt) {
				$h_data{$i_mag}{low} = $i_qt;
			}

			# Capture highest value
			if (! defined $i_highest) {
				$i_highest = $i_qt;
			} elsif ($i_highest < $i_qt) {
				$i_highest = $i_qt;
			}

			# Increment count of values in magnitude
			$i_num++;
		}

		# Calculate 95% high value, 25th quartile, 75th quartile
		my $i_h95 = (($i_highest - $h_data{$i_mag}{low}) * 0.95) + $h_data{$i_mag}{low};
		my $i_o25 = $i_num * 0.25;
		my $i_c75 = $i_num * 0.75;
		if (DEBUG) {
			print "Number in magnitude: $i_num\nHighest:\t$i_highest\n";
			print "95%:\t\t$i_h95\n25th:\t\t$i_o25\n75th:\t\t$i_c75\n";
			print "Magnitude:  10^$i_mag\n";
			print "Low:\t$h_data{$i_mag}{low}\n";
		}

		my $i_pos;
		# Determine open, close & high values
		foreach my $i_qt (sort {$a <=> $b} keys %{$h_raw{$i_mag}}) {
			$i_pos++;

			# open - store first value at/after 25th quartile
			if ($i_pos >= $i_o25) {
				if (! defined $h_data{$i_mag}{open}) {
					if (DEBUG) { print "\t\tOpen: defined -> ".$i_qt."\n"; }
					$h_data{$i_mag}{open} = $i_qt;
				}
			}

			# close - store last value within 75th quartile
			if ($i_pos <= $i_c75) {
				if (defined $h_data{$i_mag}{close}) {
					if (DEBUG) { print "\t\tClose: ".$h_data{$i_mag}{close}." -> ".$i_qt."\n"; }
				} else {
					if (DEBUG) { print "\t\tClose: defined -> ".$i_qt."\n"; }
				}
				$h_data{$i_mag}{close} = $i_qt;
			}

			# high - store last value within 95%
			if ($i_qt <= $i_h95) {
				if (defined $h_data{$i_mag}{high}) {
					if (DEBUG) { print "\t\tHigh: ".$h_data{$i_mag}{high}.' -> '.$i_qt."\n"; }
				} else {
					if (DEBUG) { print "\t\tHigh: defined -> ".$i_qt."\n"; }
				}
				$h_data{$i_mag}{high} = $i_qt;
			}
		}

		# If less than 3 values, set high to be close value
		if ($i_num < 3) { $h_data{$i_mag}{high} = $h_data{$i_mag}{close} = $h_data{$i_mag}{open}; }

		if (DEBUG) {
			print "Open:\t$h_data{$i_mag}{open}\n";
			print "Close:\t$h_data{$i_mag}{close}\n";
			print "High:\t$h_data{$i_mag}{high}\n";
			print "\n-------------------------------------------\n";
		}

		# Data format
		# (     #  open       high       low        close
		# ["2007/12/18", "34.6400", "35.0000", "34.2100", "34.7400"], #
		# );
		print OUT '["10^'.$i_mag.'", "'.$h_data{$i_mag}{open}.'", "'.$h_data{$i_mag}{high}.'", "'.$h_data{$i_mag}{low}.'", "'.$h_data{$i_mag}{close}."\"], \n";
	}

	close OUT or die "Failed to write-close [$s_outf]: $!\n";
}
