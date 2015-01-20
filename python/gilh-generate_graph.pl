#!/bin/env perl
use strict;
use warnings;
use English;

# Modules --------------------------------------------------
use Getopt::Long;
use List::Util qw(min max);
use GD::Graph::candlesticks;

# Globals --------------------------------------------------
my ($S_INPUTFILE, $S_OUTPUTFILE);
my $B_VERBOSE;

# Variables ------------------------------------------------
my ($s_inf, $s_outf);

# Main -----------------------------------------------------
getargs();

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
	unless (-f $s_outf) { $s_error .= "Output file [$s_outf] missing\n"; }

	if ($s_error) {
		print "Usage: $PROGRAM_NAME --inputfile --outputfile\nERROR/S:\n$s_error\n";
		exit 1;
	}
	if ($b_verbose) {
		print "Input file:\t$s_inf\nOutput file:\t$s_outf\n";
	}
}

# Functions ------------------------------------------------

    my @msft = (     #  open       high       low        close
        ["2007/12/18", "34.6400", "35.0000", "34.2100", "34.7400"], #
        ["2007/12/19", "34.6900", "35.1400", "34.3800", "34.7900"], #
        ["2007/12/20", "35.2900", "35.7900", "35.0800", "35.5200"], #
        ["2007/12/21", "35.9000", "36.0600", "35.7500", "36.0600"], #
        ["2007/12/24", "36.1300", "36.7200", "36.0500", "36.5800"], #
        ["2007/12/26", "36.4100", "36.6400", "36.2600", "36.6100"], #
        ["2007/12/27", "36.3500", "36.5500", "35.9400", "35.9700"], #
        ["2007/12/28", "36.1000", "36.2300", "35.6700", "36.1200"], #
        ["2007/12/31", "35.9000", "35.9900", "35.5200", "35.6000"], #
        ["2008/01/02", "35.7900", "35.9600", "35.0000", "35.2200"], #
        ["2008/01/03", "35.2200", "35.6500", "34.8600", "35.3700"], #
        ["2008/01/04", "35.1900", "35.2000", "34.0900", "34.3800"], #
        ["2008/01/07", "34.5500", "34.8000", "34.2500", "34.6100"], #
        ["2008/01/08", "34.7100", "34.7100", "33.4000", "33.4500"], #
        ["2008/01/09", "33.3600", "34.5400", "33.3500", "34.4400"], #
        ["2008/01/10", "34.3500", "34.5000", "33.7800", "34.3300"], #
        ["2008/01/11", "34.1400", "34.2400", "33.7200", "33.9100"], #
        ["2008/01/14", "34.4600", "34.5700", "34.0800", "34.3900"], #
        ["2008/01/15", "34.0300", "34.3800", "34.0000", "34.0000"], #
        ["2008/01/16", "33.4200", "33.6500", "32.5100", "33.2300"], #
        ["2008/01/17", "33.5400", "33.8000", "32.9700", "33.1100"], #
        ["2008/01/18", "33.1600", "34.0000", "32.9700", "33.0100"], #
        ["2008/01/22", "31.5400", "32.5300", "31.5000", "31.9600"], #
        ["2008/01/23", "31.4800", "32.0500", "31.0400", "31.9300"], #
        ["2008/01/24", "32.3500", "33.3600", "32.1200", "33.2500"], #
        ["2008/01/25", "34.9000", "35.0000", "32.8700", "32.9400"], #
        ["2008/01/28", "33.0200", "33.1000", "32.4200", "32.7200"],
        ["2008/01/29", "32.8500", "32.8900", "32.3500", "32.6000"],
        ["2008/01/30", "32.5600", "32.8000", "32.0500", "32.2000"],
        ["2008/01/31", "31.9100", "32.7400", "31.7200", "32.6000"],
    );

    my @all_points = map {@$_[1 .. 4]} @msft;
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
        [ map {$_->[0]} @msft ],       # date
        [ map {[@$_[1 .. 4]]} @msft ], # candlesticks
    ];

    my $gd = $graph->plot($data_candlesticks) or die $graph->error;
    open my $dump, ">", "/tmp/candlesticks_example.png" or die $!;
    print $dump $gd->png;
    close $dump;
