#!/bin/env perl
use strict;
use warnings;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Basename;
use List::Util qw(min max);
use GD::Graph::candlesticks;

# Globals --------------------------------------------------
my ($S_INPUTFILE, $S_OUTPUTFILE);
my $B_VERBOSE;

# Variables ------------------------------------------------
my ($s_inf, $s_outf);
my @a_data;

# Main -----------------------------------------------------
getargs();
read_data();
generate_graph();
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
	open IN, $s_inf or die "Failed to read-open [$s_inf]: $!\n";
	@a_data = <IN>;
	close IN or die "Failed to read-close [$s_inf]: $!\n";
}

sub generate_graph {
	my @all_points = map {@$_[1 .. 4]} @a_data;
	my $min_point  = min(@all_points);
	my $max_point  = max(@all_points);

	my $graph = GD::Graph::candlesticks->new(800, 400);
	$graph->set( 
		x_labels_vertical => 1,
		x_label           => 'Trade Date',
		y_label           => 'NASDAQ:MSFT',
		title             => "Example OHLC",
		transparent       => 0,
		candlestick_width => 7,
		dclrs             => [qw(blue)],
		y_min_value       => $min_point-0.2,
		y_max_value       => $max_point+0.2,
		y_number_format   => '%0.2f',
	) or warn $graph->error;

	my $data_candlesticks = [
		[ map {$_->[0]} @a_data ],       # date
		[ map {[@$_[1 .. 4]]} @a_data ], # candlesticks
	];

	my $gd = $graph->plot($data_candlesticks) or die $graph->error;
	open my $dump, ">", $s_outf or die "Failed to write-open [$s_outf]: $!\n";
	print $dump $gd->png;
	close $dump or die "Failed to write-close [$s_outf]: $!\n";
}

__END__
Data format
(     #  open       high       low        close
["2007/12/18", "34.6400", "35.0000", "34.2100", "34.7400"], #
);
