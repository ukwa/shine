$(function () {

	$('#reset').click(function(event) { 
		event.preventDefault();
		$('.search-field').each(function() {
			$(this).attr('value', ''); 
		});
	});
	
	$('#sort').change(function() {
		$('form').submit();
	});

	$('#order').change(function() {
		$('form').submit();
	});

	$(".add-more-button").each(function(index) {
		$(this).click(function(event) {
			event.preventDefault();
			var addMoreSelector = "#add-more-option-" + index;
			var buttonText = "#add-more-button-text-" + index;
			if ($(addMoreSelector).hasClass('hide')) {
				$(addMoreSelector).removeClass('hide');
				$(buttonText).html("CLOSE")
			} else {
				$(addMoreSelector).addClass('hide');
				$(buttonText).html("ADD")
			}
		});
	});
	
	function facetToggle($element, $button) {
		if ($element.attr('checked') !== undefined) {
			$button.removeClass('facet-deselected');
			$button.addClass('facet-selected');
		} else {
			$button.removeClass('facet-selected');
			$button.addClass('facet-deselected');
		}
	}

	function facetClickToggle($button, $element) {
		if ($button.hasClass('facet-deselected')) {
			// SELECTED
			$button.removeClass('facet-deselected');
			$button.addClass('facet-selected');
			$element.attr('checked', '');
		} else {
			// DESELECTED
			$button.removeClass('facet-selected');
			$button.addClass('facet-deselected');
			$element.removeAttr('checked');
		}
	}
	
	$(".facet-options").each(function(index) {
		// check form fields
		var $facet_option = $(this);
		var $input_include = $facet_option.find('input.include');
		var $input_exclude = $facet_option.find('input.exclude');

		var $link_span_include = $facet_option.find('a.facet.include span');
		var $link_span_exclude = $facet_option.find('a.facet.exclude span');

		// to read back facet options after submit
		facetToggle($input_include, $link_span_include);
		facetToggle($input_exclude, $link_span_exclude);

		// for clicking on facet options (includes)
		$(this).find('a.facet.include').click(function(event) {
			event.preventDefault();
			// change +/-
			facetClickToggle($link_span_include, $input_include);
 			$('form').submit();
		});
		
		// for clicking on facet options (excludes)
		$(this).find('a.facet.exclude').click(function(event) {
			event.preventDefault();
			facetClickToggle($link_span_exclude, $input_exclude);
 			$('form').submit();
		});
		
	});

	$('.show-more').each(function(index) {
		var $show_more = $(this);
		$(this).click(function(event) {
			event.preventDefault();
			$(this).parent().parent().find('li.facet-options').each(function(index) {
				$link = $(this);
				var $show_more_icon = $show_more.find('span:nth-child(1)');
				var $show_more_span = $show_more.find('span:nth-child(2)');
				if ($link.hasClass('hide')) {
					$link.removeClass('hide');
					$show_more_icon.removeClass('glyphicon-plus-sign');
					$show_more_icon.addClass('glyphicon-minus-sign');
					$show_more_span.html("Hide");
				} else {
					$link.addClass('hide');
					$show_more_icon.removeClass('glyphicon-minus-sign');
					$show_more_icon.addClass('glyphicon-plus-sign');
					$show_more_span.html("Show more...");
				}
			});
		});
	});
	
	if ($('.max-viewable-reached').attr('class') != undefined) {
		$('#max-view-reached').removeClass('hide');
	}
	
	// read in what was submitted
	$facet_sort_input = $('#facet-sort');
	$link_sort_count = $('a#facet-sort-count').find('span.label');
	$link_sort_value = $('a#facet-sort-value').find('span.label');

	if ($facet_sort_input.attr('checked') === undefined) {
		$("a.facet-sort").each(function(index) {
			// unchecked then reset all buttons
			var $link_span = $(this).find('span.label');
			$link_span.removeClass('label-success');
			$link_span.addClass('label-primary');
		});
	} else {
		if ($facet_sort_input.val() == 'count') {
			$link_sort_count.removeClass('label-primary');
			$link_sort_count.addClass('label-success');
		} else if ($facet_sort_input.val() == 'index') {
			$link_sort_value.removeClass('label-primary');
			$link_sort_value.addClass('label-success');
		}
	}

	// on clicks
	$("a.facet-sort").each(function(index) {
		var $link_span = $(this).find('span.label');
		$(this).click(function(event) {
			event.preventDefault();
			if ($link_span.hasClass('label-primary')) {
				// SELECTED
				$link_span.removeClass('label-primary');
				$link_span.addClass('label-success');
				if ($link_span.text() == 'count') {
					$facet_sort_input.val("count");
				} else if ($link_span.text() == 'value') {
					$facet_sort_input.val("index");
				}
				$facet_sort_input.attr("checked", "checked");
			} else {
				// DESELECTED
				$link_span.removeClass('label-success');
				$link_span.addClass('label-primary');
				$facet_sort_input.val("");
				$facet_sort_input.removeAttr("checked");
			}
 			$('form').submit();
		});
		
	});
	
	var inList = [];
	
	$('input.add-facet-field').each(function() {
	    $(this).keyup(function() {
	    	var $search_field = $(this);
	    	console.log($search_field.val());
			$add_more_options = $(this).parent().parent();
			if ($add_more_options.find('span.tt-dropdown-menu').css('display') == 'none') {
				$search_field = $add_more_options.find('input.form-control');
				console.log("$search_field: " + $search_field.val());
				$hidden_facets = $add_more_options.parent().find('li.facet-options.hide');
				$hidden_facets.each(function(index) {
					$value = $(this).find('a:nth-child(3)');
					$value.find("span").remove();
					console.log($value.html() + " " + $search_field.val());
					if ($value.html().trim().indexOf($search_field.val().trim()) == 0) {
						console.log("matches");
						var found = $.inArray($value.html().trim(), inList) > -1;
						if (!found) {
							inList.push($value.html().trim());
							console.log("inList: " + inList);
							$dropdown = $add_more_options.find('span.tt-dropdown-menu');
							// check if you already got one in the list
							$dropdown.append(
								'<div class=\"tt-dataset-' + index + '\"><span class=\"tt-suggestions\" style=\"display: block;\"><div class=\"tt-suggestion\"><p style=\"white-space: normal;\"><a>' + $value.html().trim() + '</a></p></div></span></div>'
							);
						}
						$dropdown.show();
					} else {
						
					}
				});
			} else {
				if ($search_field.val() == '') {
		    		$add_more_options.find('span.tt-dropdown-menu').css('display', 'none');
				}
			}
	    });
	
	});

	$(".add-facet-button").each(function() {
		$(this).click(function(event) {
			event.preventDefault();

		});
	});
	
});
