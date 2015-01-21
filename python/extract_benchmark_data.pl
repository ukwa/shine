#!/bin/env perl
use strict;
use warnings;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Spec;
use File::Basename;

# Globals --------------------------------------------------
my ($S_INPUTFILE, $S_OUTPUTFILE);
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
		'outputfile=s' => \$S_OUTPUTFILE,
		'verbose' => \$B_VERBOSE,
	);

	unless ($S_INPUTFILE) { $s_error .= "Input file missing\n"; }
	unless ($S_OUTPUTFILE) { $s_error .= "Output file missing\n"; }

	$s_inf = File::Spec->rel2abs($S_INPUTFILE);
	unless (-f $s_inf) { $s_error .= "Input file [$s_inf] missing\n"; }

	$s_outf = File::Spec->rel2abs($S_OUTPUTFILE);
	my $s_outdir = dirname $s_outf;
	unless (-d $s_outdir) { $s_error .= "Output directory [$s_outdir] missing\n"; }

	if ($s_error) {
		print "Usage: $PROGRAM_NAME --inputfile --outputfile\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($B_VERBOSE) {
		print "Input file:\t$s_inf\n";
		print "Output file:\t$s_outf\n";
		print "Output dir:\t$s_outdir\n";
	}
}

sub read_data {
	my ($i_low, $i_high);

	open IN, $s_inf or die "Failed to read-open [$s_inf]: $!\n";
	foreach my $s_line (<IN>) {
		next unless $s_line =~ m@numFound@;

		# ALL-FACETS-chervil.QTime.[ms] 8806 numFound 45997 wallclock.[ms] 8811.698
 		$s_line =~ m@QTime.\[ms\] (\d+) numFound (\d+)@;
		my $i_qt = $1;
		my $i_nf = $2;

		# Determine magnitude of nf; store magnitude > numFound > QTime
		my $i_mag = length $i_nf;
		$h_raw{$i_mag}{$i_nf}{$i_qt}++;
	}
	close IN or die "Failed to read-close [$s_inf]: $!\n";
}

sub save_data {
	open OUT, '>', $s_outf or die "Failed to write-open [$s_outf]: $!\n";

	my %h_data;
	foreach my $i_mag (sort keys %h_raw) {
		foreach my $i_nf (sort keys %{$h_raw{$i_mag}}) {
			my ($i_highest, $i_num);
			foreach my $i_qt (sort keys %{$h_raw{$i_mag}{$i_nf}}) {
				# Store lowest value
				if (! defined $h_data{$i_mag}{low}) {
					$h_data{$i_mag}{low} = $i_qt;
				} elsif ($h_data{$i_mag}{low} > $i_qt) {
					$h_data{$i_mag}{low} = $i_qt;
				}

				# Capture highest value
				unless (defined $i_highest) { $i_highest = $i_qt; }

				# Increment count of values in magnitude
				$i_num++;
			}

			# Calculate 95% high value, 25th quartile, 75th quartile
			my $i_h95 = ($i_highest - $h_data{$i_mag}{low}) * 0.95;
			my $i_o25 = $i_num * 0.25;
			my $i_c75 = $i_num * 0.75;

			my $i_pos;
			foreach my $i_qt (sort keys %{$h_raw{$i_mag}{$i_nf}}) {
				# Store 95th value
				if ($i_qt <= $i_h95) {
					if (! defined $h_data{$i_mag}{high}) {
						$h_data{$i_mag}{high} = $i_qt;
					} elsif ($h_data{$i_mag}{high} < $i_qt) {
						$h_data{$i_mag}{high} = $i_qt;
					}
				}

				$i_pos++;
				if (($i_pos >= $i_o25) && (! defined $h_data{$i_mag}{open}))
					{ $h_data{$i_mag}{open} = $i_qt; }
				if ($i_pos <= $i_c75)
					{ $h_data{$i_mag}{close} = $i_qt; }
			}
		}

		print "Magnitude: 10^$i_mag\n";
		print "Low:\t$h_data{$i_mag}{low}\n";
		print "Open:\t$h_data{$i_mag}{open}\n";
		print "Close:\t$h_data{$i_mag}{close}\n";
		print "High:\t$h_data{$i_mag}{high}\n";
	}

	close OUT or die "Failed to write-close [$s_outf]: $!\n";
}

__END__
Data format
(     #  open       high       low        close
["2007/12/18", "34.6400", "35.0000", "34.2100", "34.7400"], #
);
