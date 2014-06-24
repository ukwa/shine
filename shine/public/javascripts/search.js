$(function () {

	$('#query').keypress(function(event){
		var keycode = (event.keyCode ? event.keyCode : event.which);
		if(keycode == '13'){
		    $('#search').click();
		}
	});
	
	$('#reset').click(function(event) {
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
				$(buttonText).html("Close")
			} else {
				$(addMoreSelector).addClass('hide');
				$(buttonText).html("Add")
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
			// TODO: grab current url from somewhere
			//alert('test');
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
			// click on link and do something....
			$(this).parent().parent().find('li.facet-options').each(function(index) {
				$li = $(this);
				var $show_more_icon = $show_more.find('span:nth-child(1)');
				var $show_more_span = $show_more.find('span:nth-child(2)');
				var $link_text = $show_more.find('span:nth-child(2)').html();
				var $default_show = $li.attr('data-attr');
				if ($li.hasClass('hide')) {
					$li.addClass('show');
					$li.removeClass('hide');
					$show_more_icon.removeClass('glyphicon-plus-sign');
					$show_more_icon.addClass('glyphicon-minus-sign');
					$show_more_span.html("Hide");
				} 
				else if ($li.hasClass('show') && $default_show !== 'default') {
					console.log($link_text + " " + $default_show);
					$li.addClass('hide');
					$li.removeClass('show');
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
	
	$(".add-facet-button").each(function() {
		$(this).click(function(event) {
			event.preventDefault();
			var $search_field = $(this).parent().parent().find('input.form-control.add-facet-field')
	    	searchFacetValues($search_field);
		});
	});
	
	$('input.add-facet-field').each(function() {
	    $(this).keyup(function() {
	    	searchFacetValues($(this));
	    });
	});

	searchFacetValues = function($element) {
		var $facet_index = $element.parent().parent();
		if ($facet_index.find('span.tt-dropdown-menu').css('display') == 'none') {
			var $search_field = $element;
			var $hidden_facets = $facet_index.find('ul.list-unstyled li.facet-options.hide');
			$hidden_facets.each(function(index) {
				var $value = $(this).find('a:nth-child(3)');
				var $copied_value = $value.clone();
				$copied_value.find("span").remove();
				var $value = $copied_value.html().trim(); 
				console.log($value + " " + $search_field.val());
				if ($value.indexOf($search_field.val().trim()) == 0) {
					var found = $.inArray($value, inList) > -1;
					if (!found) {
						inList.push($value.trim());
						$dropdown = $facet_index.find('span.tt-dropdown-menu');
						// check if you already got one in the list
						$dropdown.append(
							'<div class=\"tt-dataset-' + index + '\"><span class=\"tt-suggestions\" style=\"display: block;\"><div class=\"tt-suggestion\"><p style=\"white-space: normal;\"><a href="#" class=\"suggested-facet-value\">' + $value + '</a></p></div></span></div>'
						);
					}
					$dropdown.show();
				}
			});
		} else {
			if ($element.val() == '') {
				$facet_index.find('span.tt-dropdown-menu').css('display', 'none');
			}
		}
		applyClicks();
	}
	
	function applyClicks() {
		$('.suggested-facet-value').each(function() {
			$(this).click(function(event) {
				event.preventDefault();
				// copy field
				var $face_value = $(this).html();
				console.log("$face_value: " + $face_value);
				var $add_more_options = $(this).parent().parent().parent().parent().parent().parent();
				var $add_field = $add_more_options.find('input.form-control.add-facet-field');
				$add_field.val($face_value);
				
				// add to facet list
				$('.add-facet-value').each(function() {
					$(this).click(function(event) {
						event.preventDefault();
						var $link = $(this);
						var $hidden_list = $(this).parent().parent().parent().find('ul.list-unstyled li.facet-options.hide');
						$hidden_list.each(function(index) {
							// find the correct one
							var $anchor = $(this).find('a:nth-child(3)');
							var $copied_value = $anchor.clone();
							$copied_value.find("span").remove();
							$value = $copied_value.html().trim(); 
							var $local_field = $link.parent().parent().find('input.form-control.add-facet-field');
							console.log(index + " " + $value + " " + $local_field.val());
							if ($local_field.val().trim() == $value) {
								$(this).removeClass('hide');
								$(this).addClass('show');
							}
						});
					});
				});
				
				// remove from facet list
				$('.remove-facet-value').each(function() {
					$(this).click(function(event) {
						event.preventDefault();
						var $link = $(this);
						var $show_list = $(this).parent().parent().parent().find('ul.list-unstyled li.facet-options.show');
						$show_list.each(function(index) {
							// find the correct one
							var $anchor = $(this).find('a:nth-child(3)');
							var $copied_value = $anchor.clone();
							$copied_value.find("span").remove();
							$value = $copied_value.html().trim();
							var $local_field = $link.parent().parent().find('input.form-control.add-facet-field');
							console.log(index + " " + $value + " " + $local_field.val());
							if ($local_field.val().trim() == $value) {
								$(this).removeClass('show');
								$(this).addClass('hide');
							}
						});
					});
				});
			});
		});
	}
	
	$('#reset-facets').click(function(event) {
		event.preventDefault();
		
	});
	
	$('.facet-remove').each(function() {
		$(this).click(function(event) {
			event.preventDefault();
			var value = $(this).parent().find("input").val();
			console.log(value);
			var action = $("<input>").attr("type", "hidden").attr("name", "action").val("remove-facet");
			var input = $("<input>").attr("type", "hidden").attr("name", "remove.facet").val(value);
			$('form').append($(action));
			$('form').append($(input));
			$("form").submit();
		});
	});
});

function getURLParameter(param) {
	var pageUrl = window.location.search.substring(1);
	var urlVariables = pageUrl.split('&');
	for (var i=0; i<urlVariables.length; i++) {
		var parameterName = urlVariables[i].split('=');
		if (parameterName[0] == param) {
			return parameterName[1];
		}
	}				
}
