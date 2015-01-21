#!/bin/env perl
use strict;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use File::Spec;
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
		'verbose' => \$B_VERBOSE,
	);

	unless ($S_INPUTFILE) { $s_error .= "Input file missing\n"; }

	$s_inf = File::Spec->rel2abs($S_INPUTFILE);
	unless (-f $s_inf) { $s_error .= "Input file [$s_inf] missing\n"; }

	$s_outf = $s_inf.'.png';

	if ($s_error) {
		print "Usage: $PROGRAM_NAME --inputfile\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($B_VERBOSE) {
		print "Input file:\t$s_inf\n";
		print "Output file:\t$s_outf\n";
	}
}

sub read_data {
	open IN, "< $s_inf" or die "Failed to read-open [$s_inf]: $!\n";
	while (my $s_line = <IN>) {
		chomp $s_line;
		my @a = eval $s_line;
		push @a_data, @a;
	}
	close IN or die "Failed to read-close [$s_inf]: $!\n";
}

sub generate_graph {
	my @all_points = map {@$_[1 .. 4]} @a_data;
	my $min_point  = min(@all_points);
	my $max_point  = max(@all_points);

	my $graph = GD::Graph::candlesticks->new(800, 400);
	$graph->set( 
		x_labels_vertical => 1,
		x_label           => 'NumFound per magnitude',
		y_label           => 'QTime',
		title             => "Solr QTimes",
		transparent       => 0,
		candlestick_width => 12,
		dclrs             => [qw(blue)],
		y_min_value       => 0,
		y_max_value       => $max_point+1000,
		y_number_format   => '%0d',
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
